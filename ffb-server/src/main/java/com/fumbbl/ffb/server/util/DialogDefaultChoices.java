package com.fumbbl.ffb.server.util;

import com.fumbbl.ffb.dialog.DialogId;
import com.fumbbl.ffb.model.skill.Skill; 
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class DialogDefaultChoices {
	private DialogDefaultChoices() {}

	public enum DefaultChoice {
		YES, NO, NEVER
	}

	private static final Map<DialogId, DefaultChoice> DEFAULTS;
	private static final Map<String, DefaultChoice> SKILL_DEFAULTS;

	static {
		Map<DialogId, DefaultChoice> defaults = new HashMap<>();
		// Example: defaults.put(DialogId.APOTHECARY, DefaultChoice.YES);
		// Add more as needed

		DEFAULTS = Collections.unmodifiableMap(defaults);

		Map<String, DefaultChoice> skills = new HashMap<>();
		// Example: skills.put("Side Step", DefaultChoice.YES);
		// skills.put("Dodge", DefaultChoice.NO);
		skills.put("Dodge", DefaultChoice.YES);

		SKILL_DEFAULTS = Collections.unmodifiableMap(skills);
	}

	public static DefaultChoice getDefault(DialogId id, Skill skill) {
		if (id == DialogId.SKILL_USE && skill != null) {
			return SKILL_DEFAULTS.getOrDefault(skill.getName(), DefaultChoice.NO);
		}
		return DEFAULTS.getOrDefault(id, DefaultChoice.NO);
	}
}
