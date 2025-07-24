package com.fumbbl.ffb.model;

import com.fumbbl.ffb.TurnMode;
import com.fumbbl.ffb.option.GameOptionId;
import com.fumbbl.ffb.option.UtilGameOption;

public class GameTimer {
	private final TeamTimeTracker homeTimer = new TeamTimeTracker();
	private final TeamTimeTracker awayTimer = new TeamTimeTracker();

	private long passiveStart = 0;
	private long passiveElapsed = 0;

	public void recordTurn(Game game, boolean wasHomeTeam, long durationMs, boolean newHalf, boolean touchdown, boolean endGame) {
		if (durationMs <= 0) return;
		//if (game.getTurnMode() != TurnMode.REGULAR) return;

    String turnMode = String.valueOf(game.getTurnMode());

		long turnLimitMs = UtilGameOption.getIntOption(game, GameOptionId.TURNTIME) * 1000L;
		int turnNumber = wasHomeTeam
			? game.getTurnDataHome().getTurnNr()
			: game.getTurnDataAway().getTurnNr();

		if (turnNumber <= 0) return; // This filters kickoff events.

		int half = game.getHalf();

		long timestampMs = System.currentTimeMillis();

		String reason;
		if (newHalf) {
			reason = "HALF_END";
		} else if (endGame) {
			reason = "GAME_END";
		} else if (touchdown) {
			reason = "TOUCHDOWN";
		} else {
			reason = turnMode;
		}

		TeamTimeTracker tracker = wasHomeTeam ? homeTimer : awayTimer;
		tracker.recordTurn(half, turnNumber, durationMs, turnLimitMs, timestampMs, reason);

		// Debug/logging
		System.out.println("[GameTimer] Turn end: " +
			(wasHomeTeam ? "HOME" : "AWAY") +
			" | Half: " + half +
			" | Turn: " + turnNumber +
			" | Duration (ms): " + durationMs);

		System.out.println("[GameTimer] " + (wasHomeTeam ? "HOME" : "AWAY")
			+ " timingLog: " + tracker.getTimingLog());
	}

	public void recordPassiveTime(boolean isHomeTeam, long durationMs, String context) {
		if (durationMs <= 0) return;
		TeamTimeTracker tracker = isHomeTeam ? homeTimer : awayTimer;
		tracker.recordPassiveTime(durationMs, context);

		// Debug/logging
		System.out.println("[GameTimer] Passive time: " +
			(isHomeTeam ? "HOME" : "AWAY") +
			" | Duration (ms): " + durationMs +
			" | Context: " + context);
	}

	public TeamTimeTracker getHomeTimer() { return homeTimer; }
	public TeamTimeTracker getAwayTimer() { return awayTimer; }

	public void startPassive(long now) {
		if (passiveStart == 0) {
			passiveStart = now;
		}
	}

	public void pausePassive(long now) {
		if (passiveStart > 0) {
			passiveElapsed += now - passiveStart;
			passiveStart = 0;
		}
	}

	public void stopPassive(long now) {
    passiveStart = 0;
		passiveElapsed = 0;
	}


	public void syncPassive(long now) {
		if (passiveStart > 0) {
			passiveElapsed += now - passiveStart;
			passiveStart = now;
		}
	}

	public long getPassiveElapsed() {
		return passiveElapsed;
	}

	public long getPassiveStart() {
		return passiveStart;
	}
}
