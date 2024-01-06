package com.fumbbl.ffb.server.skillbehaviour.bb2020;

import com.fumbbl.ffb.RulesCollection;
import com.fumbbl.ffb.server.injury.modification.MasterAssassinModification;
import com.fumbbl.ffb.server.model.SkillBehaviour;
import com.fumbbl.ffb.skill.bb2020.special.MasterAssassin;

@RulesCollection(RulesCollection.Rules.BB2020)
public class MasterAssassinBehaviour extends SkillBehaviour<MasterAssassin> {

	public MasterAssassinBehaviour() {
		super();

		registerModifier(new MasterAssassinModification());
	}
}
