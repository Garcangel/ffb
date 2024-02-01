package com.fumbbl.ffb.report.bb2020;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.fumbbl.ffb.Direction;
import com.fumbbl.ffb.RulesCollection;
import com.fumbbl.ffb.factory.IFactorySource;
import com.fumbbl.ffb.json.IJsonOption;
import com.fumbbl.ffb.json.UtilJson;
import com.fumbbl.ffb.report.IReport;
import com.fumbbl.ffb.report.NoDiceReport;
import com.fumbbl.ffb.report.ReportId;
import com.fumbbl.ffb.report.UtilReport;

@RulesCollection(RulesCollection.Rules.BB2020)
public class ReportHitAndRun extends NoDiceReport {

	private String playerId;
	private Direction direction;

	public ReportHitAndRun() {
		super();
	}

	public ReportHitAndRun(String playerId, Direction direction) {
		this.playerId = playerId;
		this.direction = direction;
	}

	public ReportId getId() {
		return ReportId.HIT_AND_RUN;
	}

	public String getPlayerId() {
		return playerId;
	}

	public Direction getDirection() {
		return direction;
	}


// transformation

	public IReport transform(IFactorySource source) {
		return new ReportHitAndRun(playerId, direction != null ? direction.transform() : null);
	}

	// JSON serialization

	public JsonObject toJsonValue() {
		JsonObject jsonObject = new JsonObject();
		IJsonOption.REPORT_ID.addTo(jsonObject, getId());
		IJsonOption.PLAYER_ID.addTo(jsonObject, playerId);
		IJsonOption.DIRECTION.addTo(jsonObject, direction);
		return jsonObject;
	}

	public ReportHitAndRun initFrom(IFactorySource source, JsonValue jsonValue) {
		JsonObject jsonObject = UtilJson.toJsonObject(jsonValue);
		UtilReport.validateReportId(this, (ReportId) IJsonOption.REPORT_ID.getFrom(source, jsonObject));
		playerId = IJsonOption.PLAYER_ID.getFrom(source, jsonObject);
		direction = (Direction) IJsonOption.DIRECTION.getFrom(source, jsonObject);
		return this;
	}

}
