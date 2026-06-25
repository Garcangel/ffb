package com.fumbbl.ffb.server.util;

import com.fumbbl.ffb.IDialogParameter;
import com.fumbbl.ffb.dialog.DialogSkillUseParameter;
import com.fumbbl.ffb.model.Game;
import com.fumbbl.ffb.model.GameTimer;
import com.fumbbl.ffb.model.Player;
import com.fumbbl.ffb.net.commands.ClientCommandUseSkill;
import com.fumbbl.ffb.server.GameState;

/**
 * 
 * @author Kalimar
 */
public class UtilServerDialog {

	public static void showDialog(GameState gameState, IDialogParameter dialogParameter, boolean stopTurnTimer) {
		Game game = gameState.getGame();

		// Centralized automation for timedChoice:
    if (dialogParameter instanceof DialogSkillUseParameter) {
			DialogSkillUseParameter param = (DialogSkillUseParameter) dialogParameter;
			String playerId = param.getPlayerId();
			Player<?> player = game.getPlayerById(playerId);
			boolean isActingTeam = game.getActingTeam().hasPlayer(player);
			// This line automates the timer for all future skills:
			param.setTimedChoice(!isActingTeam); // True for non-acting team, else false
    }
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

	public static boolean isValidSkillDialog(Game game, ClientCommandUseSkill cmd) {
    return game.getDialogParameter() instanceof DialogSkillUseParameter
			&& ((DialogSkillUseParameter) game.getDialogParameter()).getPlayerId().equals(cmd.getPlayerId())
			&& ((DialogSkillUseParameter) game.getDialogParameter()).getSkill().equals(cmd.getSkill());
  }
}
