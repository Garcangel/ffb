package com.fumbbl.ffb.client.state.bb2020;

import com.fumbbl.ffb.FieldCoordinate;
import com.fumbbl.ffb.IIconProperty;
import com.fumbbl.ffb.client.ActionKey;
import com.fumbbl.ffb.client.FantasyFootballClientAwt;
import com.fumbbl.ffb.client.UserInterface;
import com.fumbbl.ffb.client.state.ClientStateAwt;
import com.fumbbl.ffb.client.state.IPlayerPopupMenuKeys;
import com.fumbbl.ffb.client.state.MenuItemConfig;
import com.fumbbl.ffb.client.state.logic.ClientAction;
import com.fumbbl.ffb.client.state.logic.Influences;
import com.fumbbl.ffb.client.state.logic.bb2020.ThenIStartedBlastinLogicModule;
import com.fumbbl.ffb.client.state.logic.interaction.ActionContext;
import com.fumbbl.ffb.client.state.logic.interaction.InteractionResult;
import com.fumbbl.ffb.client.util.UtilClientCursor;
import com.fumbbl.ffb.model.FieldModel;
import com.fumbbl.ffb.model.Player;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class ClientStateThenIStartedBlastin extends ClientStateAwt<ThenIStartedBlastinLogicModule> {

	public ClientStateThenIStartedBlastin(FantasyFootballClientAwt pClient) {
		super(pClient, new ThenIStartedBlastinLogicModule(pClient));
	}

	@Override
	public void enterState() {
		super.enterState();
		getClient().getUserInterface().getFieldComponent().refresh();
	}

	@Override
	public void leaveState() {
		FieldModel fieldModel = getClient().getGame().getFieldModel();
		fieldModel.clearMoveSquares();
		getClient().getUserInterface().getFieldComponent().refresh();
	}

	public void clickOnPlayer(Player<?> player) {
		InteractionResult result = logicModule.playerInteraction(player);
		switch (result.getKind()) {
			case SELECT_ACTION:
				createAndShowPopupMenuForActingPlayer(result.getActionContext());
				break;
			default:
				break;
		}
	}


	public boolean mouseOverPlayer(Player<?> player) {
		UserInterface userInterface = getClient().getUserInterface();
		InteractionResult result = logicModule.playerPeek(player);
		determineCursor(result);
		userInterface.refreshSideBars();
		return true;
	}

	@Override
	protected String validCursor() {
		return IIconProperty.CURSOR_BLASTIN;
	}

	@Override
	protected String invalidCursor() {
		return IIconProperty.CURSOR_INVALID_BLASTIN;
	}

	@Override
	public boolean mouseOverField(FieldCoordinate pCoordinate) {
		UserInterface userInterface = getClient().getUserInterface();
		UtilClientCursor.setCustomCursor(userInterface, IIconProperty.CURSOR_INVALID_BLASTIN);
		return super.mouseOverField(pCoordinate);
	}

	@Override
	protected LinkedHashMap<ClientAction, MenuItemConfig> itemConfigs(ActionContext actionContext) {
		LinkedHashMap<ClientAction, MenuItemConfig> itemConfigs = super.itemConfigs(actionContext);
		itemConfigs.put(ClientAction.END_MOVE, new MenuItemConfig("Don't blast", IIconProperty.ACTION_END_MOVE, IPlayerPopupMenuKeys.KEY_END_MOVE));
		return itemConfigs;
	}

	@Override
	protected Map<Influences, Map<ClientAction, MenuItemConfig>> influencedItemConfigs() {
		Map<Influences, Map<ClientAction, MenuItemConfig>> influences = new HashMap<>();
		Map<ClientAction, MenuItemConfig> hasActed = new HashMap<>();
		influences.put(Influences.HAS_ACTED, hasActed);
		hasActed.put(ClientAction.END_MOVE, new MenuItemConfig("End Action", IIconProperty.ACTION_END_MOVE, IPlayerPopupMenuKeys.KEY_END_MOVE));
		return influences;
	}

	@Override
	protected Map<Integer, ClientAction> actionMapping() {
		return new HashMap<Integer, ClientAction>() {{
			put(IPlayerPopupMenuKeys.KEY_END_MOVE, ClientAction.END_MOVE);
		}};
	}

	public boolean actionKeyPressed(ActionKey pActionKey) {
		if (pActionKey == null) {
			return false;
		}
		Player<?> player = getClient().getGame().getActingPlayer().getPlayer();
		switch (pActionKey) {
			case PLAYER_ACTION_END_MOVE:
				menuItemSelected(player, IPlayerPopupMenuKeys.KEY_END_MOVE);
				return true;
			default:
				return super.actionKeyPressed(pActionKey);
		}
	}

}
