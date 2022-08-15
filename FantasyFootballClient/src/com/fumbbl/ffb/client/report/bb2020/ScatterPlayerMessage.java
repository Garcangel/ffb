package com.fumbbl.ffb.client.report.bb2020;

import com.fumbbl.ffb.Direction;
import com.fumbbl.ffb.RulesCollection;
import com.fumbbl.ffb.RulesCollection.Rules;
import com.fumbbl.ffb.client.TextStyle;
import com.fumbbl.ffb.client.report.ReportMessageBase;
import com.fumbbl.ffb.client.report.ReportMessageType;
import com.fumbbl.ffb.report.ReportId;
import com.fumbbl.ffb.report.ReportScatterPlayer;
import com.fumbbl.ffb.util.ArrayTool;

@ReportMessageType(ReportId.SCATTER_PLAYER)
@RulesCollection(Rules.BB2020)
public class ScatterPlayerMessage extends ReportMessageBase<ReportScatterPlayer> {

	@Override
	protected void render(ReportScatterPlayer report) {
		int[] rolls = report.getRolls();
		if (ArrayTool.isProvided(rolls)) {
			StringBuilder status = new StringBuilder();
			boolean scatters = rolls.length > 1;
			if (scatters) {
				status.append("Scatter Rolls [ ");
			} else {
				status.append("Bounce Roll [ ");
			}
			for (int i = 0; i < rolls.length; i++) {
				if (i > 0) {
					status.append(", ");
				}
				status.append(rolls[i]);
			}
			status.append(" ] ");
			Direction[] directions = report.getDirections();
			for (int i = 0; i < directions.length; i++) {
				if (i > 0) {
					status.append(", ");
				}
				status.append(mapToLocal(directions[i]).getName());
			}
			println(getIndent(), TextStyle.ROLL, status.toString());
			status = new StringBuilder();
			status.append("Player ");
			if (scatters) {
				status.append("scatters");
			} else {
				status.append("bounces");
			}
			status.append(" from square (");
			status.append(report.getStartCoordinate().getX()).append(",").append(report.getStartCoordinate().getY());
			status.append(") to square (");
			status.append(report.getEndCoordinate().getX()).append(",").append(report.getEndCoordinate().getY());
			status.append(").");
			println(getIndent() + 1, status.toString());
		}
	}
}
