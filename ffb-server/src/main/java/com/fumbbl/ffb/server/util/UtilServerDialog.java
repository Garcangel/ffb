package com.fumbbl.ffb.server.util;

import com.fumbbl.ffb.IDialogParameter;
import com.fumbbl.ffb.model.Game;
import com.fumbbl.ffb.model.GameTimer;
import com.fumbbl.ffb.server.GameState;

/**
 * 
 * @author Kalimar
 */
public class UtilServerDialog {

	public static void showDialog(GameState gameState, IDialogParameter dialogParameter, boolean stopTurnTimer) {
		Game game = gameState.getGame();
		game.setDialogParameter(dialogParameter);
		if (stopTurnTimer) {
			game.setWaitingForOpponent(true);
			UtilServerTimer.stopTurnTimer(gameState, System.currentTimeMillis());
			UtilServerPassiveTimer.startPassiveTimer(gameState, System.currentTimeMillis());
		}
	}

	public static void hideDialog(GameState gameState) {
		Game game = gameState.getGame();
		game.setDialogParameter(null);
		game.setWaitingForOpponent(false);


		GameTimer timer = game.getGameTimer();
		long now = System.currentTimeMillis();

		// Only log if passive timer was running (passiveStart > 0)
		if (timer.getPassiveStart() > 0) {
			long duration = now - timer.getPassiveStart();
			boolean isHomeplaying = game.isHomePlaying();
			String context = "";/* e.g. "SideStep", "Inducement", etc */;
			timer.recordPassiveTime(!isHomeplaying, duration, context);
		}

		UtilServerPassiveTimer.stopPassiveTimer(gameState, System.currentTimeMillis());
		UtilServerTimer.startTurnTimer(gameState, System.currentTimeMillis());
	}
}
