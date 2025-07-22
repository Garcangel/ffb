package com.fumbbl.ffb.server.util;

import com.fumbbl.ffb.model.GameTimer;
import com.fumbbl.ffb.server.GameState;

// Call UtilServerPassiveTimer.syncPassive(gameState, currentTimeMillis); 
// in ServerGameTimeTask.java, after UtilServerTimer.syncTime(...)

public class UtilServerPassiveTimer {
	public static void startPassiveTimer(GameState gameState, long currentTimeMillis) {
		GameTimer timer = gameState.getGame().getGameTimer();
		timer.startPassive(currentTimeMillis);
	}

	public static void stopPassiveTimer(GameState gameState, long currentTimeMillis) {
		GameTimer timer = gameState.getGame().getGameTimer();
		timer.stopPassive(currentTimeMillis);
	}

	public static void syncPassiveTimer(GameState gameState, long currentTimeMillis) {
		GameTimer timer = gameState.getGame().getGameTimer();
		timer.syncPassive(currentTimeMillis);
	}
}

