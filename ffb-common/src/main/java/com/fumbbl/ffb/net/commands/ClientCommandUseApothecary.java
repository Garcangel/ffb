package com.fumbbl.ffb.net.commands;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.fumbbl.ffb.ApothecaryType;
import com.fumbbl.ffb.factory.IFactorySource;
import com.fumbbl.ffb.json.IJsonOption;
import com.fumbbl.ffb.json.UtilJson;
import com.fumbbl.ffb.net.NetCommandId;

/**
 * 
 * @author Kalimar
 */
public class ClientCommandUseApothecary extends ClientCommand {

	private String fPlayerId;
	private boolean fApothecaryUsed;
	private ApothecaryType apothecaryType;

	public ClientCommandUseApothecary() {
		super();
	}

	public ClientCommandUseApothecary(String pPlayerId, boolean pApothecaryUsed, ApothecaryType apothecaryType) {
		fPlayerId = pPlayerId;
		fApothecaryUsed = pApothecaryUsed;
		this.apothecaryType = apothecaryType;
	}

	public NetCommandId getId() {
		return NetCommandId.CLIENT_USE_APOTHECARY;
	}

	public String getPlayerId() {
		return fPlayerId;
	}

	public boolean isApothecaryUsed() {
		return fApothecaryUsed;
	}

	public ApothecaryType getApothecaryType() {
		return apothecaryType;
	}
// JSON serialization

	public JsonObject toJsonValue() {
		JsonObject jsonObject = super.toJsonValue();
		IJsonOption.PLAYER_ID.addTo(jsonObject, fPlayerId);
		IJsonOption.APOTHECARY_USED.addTo(jsonObject, fApothecaryUsed);
		if (apothecaryType != null) {
			IJsonOption.APOTHECARY_TYPE.addTo(jsonObject, apothecaryType.name());
		}
		return jsonObject;
	}

	public ClientCommandUseApothecary initFrom(IFactorySource source, JsonValue jsonValue) {
		super.initFrom(source, jsonValue);
		JsonObject jsonObject = UtilJson.toJsonObject(jsonValue);
		fPlayerId = IJsonOption.PLAYER_ID.getFrom(source, jsonObject);
		fApothecaryUsed = IJsonOption.APOTHECARY_USED.getFrom(source, jsonObject);
		if (IJsonOption.APOTHECARY_TYPE.isDefinedIn(jsonObject)) {
			apothecaryType = ApothecaryType.valueOf(IJsonOption.APOTHECARY_TYPE.getFrom(source, jsonObject));
		}
		return this;
	}

}
