package com.fumbbl.ffb.server;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.fumbbl.ffb.Weather;
import com.fumbbl.ffb.factory.IFactorySource;
import com.fumbbl.ffb.json.IJsonSerializable;
import com.fumbbl.ffb.json.UtilJson;

public class ActiveEffects implements IJsonSerializable {

	private Weather oldWeather;
	private boolean skipRestoreWeather;

	public Weather getOldWeather() {
		return oldWeather;
	}

	public void setOldWeather(Weather oldWeather) {
		this.oldWeather = oldWeather;
	}

	public boolean isSkipRestoreWeather() {
		return skipRestoreWeather;
	}

	public void setSkipRestoreWeather(boolean skipRestoreWeather) {
		this.skipRestoreWeather = skipRestoreWeather;
	}

	@Override
	public ActiveEffects initFrom(IFactorySource source, JsonValue jsonValue) {
		JsonObject jsonObject = UtilJson.toJsonObject(jsonValue);
		oldWeather = (Weather) IServerJsonOption.WEATHER.getFrom(source, jsonObject);
		skipRestoreWeather = IServerJsonOption.SKIP_RESTORE_WEATHER.getFrom(source, jsonObject);
		return this;
	}

	@Override
	public JsonObject toJsonValue() {
		JsonObject jsonObject = new JsonObject();
		IServerJsonOption.WEATHER.addTo(jsonObject, oldWeather);
		return jsonObject;
	}
}
