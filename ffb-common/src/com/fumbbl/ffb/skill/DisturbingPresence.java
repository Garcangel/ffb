package com.fumbbl.ffb.skill;

import com.fumbbl.ffb.RulesCollection;
import com.fumbbl.ffb.SkillCategory;
import com.fumbbl.ffb.RulesCollection.Rules;
import com.fumbbl.ffb.model.property.NamedProperties;
import com.fumbbl.ffb.model.skill.Skill;

/**
 * This player's presence is very disturbing, whether it is caused by a massive
 * cloud of flies, sprays of soporific musk, an aura of random chaos or intense
 * cold, or a pheromone that causes fear and panic. Regardless of the nature of
 * this mutation, any player must subtract 1 from the D6 when they pass,
 * intercept or catch for each opposing player with Disturbing Presence that is
 * within three squares of them, even if the Disturbing Presence player is Prone
 * or Stunned.
 */
@RulesCollection(Rules.COMMON)
public class DisturbingPresence extends Skill {

	public DisturbingPresence() {
		super("Disturbing Presence", SkillCategory.MUTATION);
	}

	@Override
	public void postConstruct() {
		registerProperty(NamedProperties.inflictsDisturbingPresence);
	}

}
