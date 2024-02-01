package com.fumbbl.ffb.client.report.bb2020;

import com.fumbbl.ffb.RulesCollection;
import com.fumbbl.ffb.client.TextStyle;
import com.fumbbl.ffb.client.report.ReportMessageBase;
import com.fumbbl.ffb.client.report.ReportMessageType;
import com.fumbbl.ffb.report.ReportId;
import com.fumbbl.ffb.report.bb2020.ReportBiasedRef;

@RulesCollection(RulesCollection.Rules.BB2020)
@ReportMessageType(ReportId.BIASED_REF)
public class BiasedRefMessage extends ReportMessageBase<ReportBiasedRef> {
	@Override
	protected void render(ReportBiasedRef report) {
		println(getIndent(), TextStyle.ROLL, "Biased Roll [ " + report.getRoll() + " ]");
		if (report.isFoulSpotted()) {
			println(getIndent(), TextStyle.NONE, "The biased referee spots the foul.");
		} else {
			println(getIndent(), TextStyle.NONE, "The biased referee does not spot the foul.");
		}
	}
}
