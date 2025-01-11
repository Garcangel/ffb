package com.fumbbl.ffb.skill.bb2020.special;

import com.fumbbl.ffb.RulesCollection;
import com.fumbbl.ffb.SkillCategory;
import com.fumbbl.ffb.model.property.NamedProperties;
import com.fumbbl.ffb.model.skill.Skill;
import com.fumbbl.ffb.model.skill.SkillUsageType;

@RulesCollection(RulesCollection.Rules.BB2020)
public class FuriousOutburst extends Skill {
	public FuriousOutburst() {
		super("Furious Outburst", SkillCategory.TRAIT, SkillUsageType.ONCE_PER_HALF);
	}

	@Override
	public void postConstruct() {
		registerProperty(NamedProperties.canTeleportBeforeAndAfterAvRollAttack);
		registerProperty(NamedProperties.canPerformArmourRollInsteadOfBlock);
	}
}
