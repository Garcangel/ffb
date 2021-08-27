package com.fumbbl.ffb.inducement.bb2020;

import java.util.HashMap;
import java.util.Map;

public class Prayers {
	private final Map<Integer, Prayer> exhibitionPrayers = new HashMap<Integer, Prayer>() {{
		put(1, Prayer.TREACHEROUS_TRAPDOOR);
		put(2, Prayer.FRIENDS_WITH_THE_REF);
		put(3, Prayer.STILETTO);
		put(4, Prayer.IRON_MAN);
		put(5, Prayer.KNUCKLE_DUSTERS);
		put(6, Prayer.BAD_HABITS);
		put(7, Prayer.GREASY_CLEATS);
		put(8, Prayer.BLESSED_STATUE_OF_NUFFLE);
	}};

	private final Map<Integer, Prayer> leagueOnlyPrayers = new HashMap<Integer, Prayer>() {{
		put(9, Prayer.MOLES_UNDER_THE_PITCH);
		put(10, Prayer.PERFECT_PASSING);
		put(11, Prayer.FAN_INTERACTION);
		put(12, Prayer.NECESSARY_VIOLENCE);
		put(13, Prayer.FOULING_FRENZY);
		put(14, Prayer.THROW_A_ROCK);
		put(15, Prayer.UNDER_SCRUTINY);
		put(16, Prayer.INTENSIVE_TRAINING);
	}};

	public Map<Integer, Prayer> getExhibitionPrayers() {
		return exhibitionPrayers;
	}

	public Map<Integer, Prayer> getLeagueOnlyPrayers() {
		return leagueOnlyPrayers;
	}
}
