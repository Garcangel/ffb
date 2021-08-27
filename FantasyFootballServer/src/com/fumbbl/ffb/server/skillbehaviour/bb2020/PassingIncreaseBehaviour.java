package com.fumbbl.ffb.server.skillbehaviour.bb2020;

import com.fumbbl.ffb.RulesCollection;
import com.fumbbl.ffb.RulesCollection.Rules;
import com.fumbbl.ffb.server.model.SkillBehaviour;
import com.fumbbl.ffb.skill.bb2020.PassingIncrease;

@RulesCollection(Rules.BB2020)
public class PassingIncreaseBehaviour extends SkillBehaviour<PassingIncrease> {
	public PassingIncreaseBehaviour() {
		super();

		registerModifier(player -> {
			if (player.getPassing() <= 0) {
				player.setPassing(6);
			} else {
				player.setPassing(
					Math.max(
						Math.max(1, player.getPosition().getPassing() - 2),
						player.getPassing() - 1
					)
				);
			}
		});
	}
}
