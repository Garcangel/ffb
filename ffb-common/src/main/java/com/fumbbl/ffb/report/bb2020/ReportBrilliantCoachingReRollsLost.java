package com.fumbbl.ffb.report.bb2020;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.fumbbl.ffb.RulesCollection;
import com.fumbbl.ffb.factory.IFactorySource;
import com.fumbbl.ffb.json.IJsonOption;
import com.fumbbl.ffb.json.UtilJson;
import com.fumbbl.ffb.report.IReport;
import com.fumbbl.ffb.report.NoDiceReport;
import com.fumbbl.ffb.report.ReportId;
import com.fumbbl.ffb.report.UtilReport;

@RulesCollection(RulesCollection.Rules.BB2020)
public class ReportBrilliantCoachingReRollsLost extends NoDiceReport {

	private String teamId;
	private int amount;

	public ReportBrilliantCoachingReRollsLost() {
	}

	public ReportBrilliantCoachingReRollsLost(String teamId, int amount) {
		this.teamId = teamId;
		this.amount = amount;
	}

	@Override
	public ReportId getId() {
		return ReportId.BRILLIANT_COACHING_RE_ROLLS_LOST;
	}

	public String getTeamId() {
		return teamId;
	}

	public int getAmount() {
		return amount;
	}

	@Override
	public IReport transform(IFactorySource source) {
		return new ReportBrilliantCoachingReRollsLost(teamId, amount);
	}

	@Override
	public Object initFrom(IFactorySource source, JsonValue jsonValue) {
		JsonObject jsonObject = UtilJson.toJsonObject(jsonValue);
		UtilReport.validateReportId(this, (ReportId) IJsonOption.REPORT_ID.getFrom(source, jsonObject));
		teamId = IJsonOption.TEAM_ID.getFrom(source, jsonObject);
		amount = IJsonOption.RE_ROLLS_BRILLIANT_COACHING_ONE_DRIVE.getFrom(source, jsonObject);
		return this;
	}

	@Override
	public JsonValue toJsonValue() {
		JsonObject jsonObject = new JsonObject();
		IJsonOption.REPORT_ID.addTo(jsonObject, getId());
		IJsonOption.TEAM_ID.addTo(jsonObject, teamId);
		IJsonOption.RE_ROLLS_BRILLIANT_COACHING_ONE_DRIVE.addTo(jsonObject, amount);
		return jsonObject;
	}
}
