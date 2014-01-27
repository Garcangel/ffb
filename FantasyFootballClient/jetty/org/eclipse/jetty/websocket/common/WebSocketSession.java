//
//  ========================================================================
//  Copyright (c) 1995-2013 Mort Bay Consulting Pty. Ltd.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
//

package org.eclipse.jetty.websocket.common;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jetty.util.MultiMap;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.UrlEncoded;
import org.eclipse.jetty.util.annotation.ManagedAttribute;
import org.eclipse.jetty.util.annotation.ManagedObject;
import org.eclipse.jetty.util.component.ContainerLifeCycle;
import org.eclipse.jetty.util.component.Dumpable;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.websocket.api.CloseStatus;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.SuspendToken;
import org.eclipse.jetty.websocket.api.UpgradeRequest;
import org.eclipse.jetty.websocket.api.UpgradeResponse;
import org.eclipse.jetty.websocket.api.WebSocketException;
import org.eclipse.jetty.websocket.api.WebSocketPolicy;
import org.eclipse.jetty.websocket.api.extensions.ExtensionFactory;
import org.eclipse.jetty.websocket.api.extensions.Frame;
import org.eclipse.jetty.websocket.api.extensions.IncomingFrames;
import org.eclipse.jetty.websocket.api.extensions.OutgoingFrames;
import org.eclipse.jetty.websocket.common.events.EventDriver;
import org.eclipse.jetty.websocket.common.io.IOState;
import org.eclipse.jetty.websocket.common.io.IOState.ConnectionStateListener;

@ManagedObject("A Jetty WebSocket Session")
public class WebSocketSession extends ContainerLifeCycle implements Session, IncomingFrames, ConnectionStateListener
{
    private static final Logger LOG = Log.getLogger(WebSocketSession.class);
    private final URI requestURI;
    private final EventDriver websocket;
    private final LogicalConnection connection;
    private ExtensionFactory extensionFactory;
    private long maximumMessageSize;
    private String protocolVersion;
    private Map<String, String[]> parameterMap = new HashMap<String, String[]>();
    private WebSocketRemoteEndpoint remote;
    private IncomingFrames incomingHandler;
    private OutgoingFrames outgoingHandler;
    private WebSocketPolicy policy;
    private UpgradeRequest upgradeRequest;
    private UpgradeResponse upgradeResponse;

    public WebSocketSession(URI requestURI, EventDriver websocket, LogicalConnection connection)
    {
        if (requestURI == null)
        {
            throw new RuntimeException("Request URI cannot be null");
        }

        this.requestURI = requestURI;
        this.websocket = websocket;
        this.connection = connection;
        this.outgoingHandler = connection;
        this.incomingHandler = websocket;

        this.connection.getIOState().addListener(this);

        // Get the parameter map (use the jetty MultiMap to do this right)
        MultiMap<String> params = new MultiMap<String>();
        String query = requestURI.getQuery();
        if (StringUtil.isNotBlank(query))
        {
            UrlEncoded.decodeTo(query,params,StringUtil.__UTF8_CHARSET,-1);
        }

        for (String name : params.keySet())
        {
            List<String> valueList = params.getValues(name);
            String valueArr[] = new String[valueList.size()];
            valueArr = valueList.toArray(valueArr);
            parameterMap.put(name,valueArr);
        }
    }

    public void close() throws IOException
    {
        this.close(StatusCode.NORMAL,null);
    }

    public void close(CloseStatus closeStatus)
    {
        this.close(closeStatus.getCode(),closeStatus.getPhrase());
    }

    public void close(int statusCode, String reason)
    {
        connection.close(statusCode,reason);
        notifyClose(statusCode,reason);
    }

    /**
     * Harsh disconnect
     */
    public void disconnect()
    {
        connection.disconnect();

        // notify of harsh disconnect
        notifyClose(StatusCode.NO_CLOSE,"Harsh disconnect");
    }

    
    public void dump(Appendable out, String indent) throws IOException
    {
        super.dump(out,indent);
        out.append(indent).append(" +- incomingHandler : ");
        if (incomingHandler instanceof Dumpable)
        {
            ((Dumpable)incomingHandler).dump(out,indent + "    ");
        }
        else
        {
            out.append(incomingHandler.toString()).append('\n');
        }

        out.append(indent).append(" +- outgoingHandler : ");
        if (outgoingHandler instanceof Dumpable)
        {
            ((Dumpable)outgoingHandler).dump(out,indent + "    ");
        }
        else
        {
            out.append(outgoingHandler.toString()).append('\n');
        }
    }

    
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        WebSocketSession other = (WebSocketSession)obj;
        if (connection == null)
        {
            if (other.connection != null)
            {
                return false;
            }
        }
        else if (!connection.equals(other.connection))
        {
            return false;
        }
        return true;
    }

    public LogicalConnection getConnection()
    {
        return connection;
    }

    public ExtensionFactory getExtensionFactory()
    {
        return extensionFactory;
    }

    /**
     * The idle timeout in milliseconds
     */
    public long getIdleTimeout()
    {
        return connection.getMaxIdleTimeout();
    }

    @ManagedAttribute(readonly = true)
    public IncomingFrames getIncomingHandler()
    {
        return incomingHandler;
    }

    public InetSocketAddress getLocalAddress()
    {
        return connection.getLocalAddress();
    }

    public long getMaximumMessageSize()
    {
        return maximumMessageSize;
    }

    @ManagedAttribute(readonly = true)
    public OutgoingFrames getOutgoingHandler()
    {
        return outgoingHandler;
    }

    public WebSocketPolicy getPolicy()
    {
        return policy;
    }

    public String getProtocolVersion()
    {
        return protocolVersion;
    }

    public RemoteEndpoint getRemote()
    {
        ConnectionState state = connection.getIOState().getConnectionState();

        if ((state == ConnectionState.OPEN) || (state == ConnectionState.CONNECTED))
        {
            return remote;
        }

        throw new WebSocketException("RemoteEndpoint unavailable, current state [" + state + "], expecting [OPEN or CONNECTED]");
    }

    public InetSocketAddress getRemoteAddress()
    {
        return remote.getInetSocketAddress();
    }

    public URI getRequestURI()
    {
        return requestURI;
    }

    public UpgradeRequest getUpgradeRequest()
    {
        return this.upgradeRequest;
    }

    public UpgradeResponse getUpgradeResponse()
    {
        return this.upgradeResponse;
    }

    
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((connection == null)?0:connection.hashCode());
        return result;
    }

    /**
     * Incoming Errors from Parser
     */
    public void incomingError(WebSocketException e)
    {
        if (connection.getIOState().isInputAvailable())
        {
            // Forward Errors to User WebSocket Object
            websocket.incomingError(e);
        }
    }

    /**
     * Incoming Raw Frames from Parser
     */
    public void incomingFrame(Frame frame)
    {
        if (connection.getIOState().isInputAvailable())
        {
            // Forward Frames Through Extension List
            incomingHandler.incomingFrame(frame);
        }
    }

    public boolean isOpen()
    {
        if (this.connection == null)
        {
            return false;
        }
        return this.connection.isOpen();
    }

    public boolean isSecure()
    {
        if (upgradeRequest == null)
        {
            throw new IllegalStateException("No valid UpgradeRequest yet");
        }

        URI requestURI = upgradeRequest.getRequestURI();

        return "wss".equalsIgnoreCase(requestURI.getScheme());
    }

    public void notifyClose(int statusCode, String reason)
    {
        websocket.onClose(new CloseInfo(statusCode,reason));
    }

    public void onConnectionStateChange(ConnectionState state)
    {
        if (state == ConnectionState.CLOSED)
        {
            IOState ioState = this.connection.getIOState();
            // The session only cares about abnormal close, as we need to notify
            // the endpoint of this close scenario.
            if (ioState.wasAbnormalClose())
            {
                CloseInfo close = ioState.getCloseInfo();
                LOG.debug("Detected abnormal close: {}",close);
                // notify local endpoint
                notifyClose(close.getStatusCode(),close.getReason());
            }
        }
    }

    /**
     * Open/Activate the session
     * 
     * @throws IOException
     */
    public void open()
    {
        if (remote != null)
        {
            // already opened
            return;
        }

        // Upgrade success
        connection.getIOState().onConnected();

        // Connect remote
        remote = new WebSocketRemoteEndpoint(connection,outgoingHandler);

        // Open WebSocket
        websocket.openSession(this);

        // Open connection
        connection.getIOState().onOpened();

        if (LOG.isDebugEnabled())
        {
            LOG.debug("open -> {}",dump());
        }
    }

    public void setExtensionFactory(ExtensionFactory extensionFactory)
    {
        this.extensionFactory = extensionFactory;
    }

    /**
     * Set the timeout in milliseconds
     */
    public void setIdleTimeout(long ms)
    {
        connection.setMaxIdleTimeout(ms);
    }

    public void setMaximumMessageSize(long length)
    {
        this.maximumMessageSize = length;
    }

    public void setOutgoingHandler(OutgoingFrames outgoing)
    {
        this.outgoingHandler = outgoing;
    }

    public void setPolicy(WebSocketPolicy policy)
    {
        this.policy = policy;
    }

    public void setUpgradeRequest(UpgradeRequest request)
    {
        this.upgradeRequest = request;
    }

    public void setUpgradeResponse(UpgradeResponse response)
    {
        this.upgradeResponse = response;
    }

    public SuspendToken suspend()
    {
        // TODO Auto-generated method stub
        return null;
    }

    
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("WebSocketSession[");
        builder.append("websocket=").append(websocket);
        builder.append(",behavior=").append(policy.getBehavior());
        builder.append(",connection=").append(connection);
        builder.append(",remote=").append(remote);
        builder.append(",incoming=").append(incomingHandler);
        builder.append(",outgoing=").append(outgoingHandler);
        builder.append("]");
        return builder.toString();
    }
}
