package com.fumbbl.ffb.server.skillbehaviour.bb2020;

import com.fumbbl.ffb.RulesCollection;
import com.fumbbl.ffb.RulesCollection.Rules;
import com.fumbbl.ffb.skill.Dodge;

@RulesCollection(Rules.BB2020)
public class DodgeBehaviour extends AbstractDodgingBehaviour<Dodge> {

	public DodgeBehaviour() {
		super(1, false);
	}

}
