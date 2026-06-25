package com.fumbbl.ffb.server.util;

import com.fumbbl.ffb.IDialogParameter;
import com.fumbbl.ffb.dialog.DialogId;
import com.fumbbl.ffb.dialog.DialogSkillUseParameter;
import com.fumbbl.ffb.model.Game;
import com.fumbbl.ffb.model.GameTimer;
import com.fumbbl.ffb.model.skill.Skill;
import com.fumbbl.ffb.net.commands.ClientCommandUseSkill;
import com.fumbbl.ffb.server.GameState;
import com.fumbbl.ffb.server.net.ReceivedCommand;
import com.fumbbl.ffb.server.step.IStep;
import com.fumbbl.ffb.server.util.DialogDefaultChoices.DefaultChoice;

public class UtilServerTimeout {

  // NOTE: To support timers for all dialogs:
  // 1. Add isTimedChoice()/setTimedChoice(boolean) to IDialogParameter
  // 2. Implement in AbstractDialogParameter
  // 3. Update all dialog parameter classes to extend AbstractDialogParameter
  // Current code only enables timers for DialogSkillUseParameter (and future types via instanceof checks)

  public static void resolveTimeoutIfNeeded(GameState gameState, long now) {
		Game game = gameState.getGame();
		IDialogParameter dialogParameter = game.getDialogParameter();

		if (dialogParameter == null) return;

    // Every type of dialog needs its onw block.
    // To expand use instanceof checks for new dialog types
		if (dialogParameter instanceof DialogSkillUseParameter) {

      
			DialogSkillUseParameter param = (DialogSkillUseParameter) dialogParameter;

      if (!param.isTimedChoice()) return;

      System.out.println("[Timeout] Checking dialog timeout for game " + game.getId());
      final long TIMEOUT_MS = 10_000;

      // Step 5: Check elapsed passive time
      GameTimer timer = game.getGameTimer();
      long elapsed = timer.getPassiveElapsed();

      if (elapsed < TIMEOUT_MS) return; // Not expired, do nothing

			DefaultChoice defaultChoice = DialogDefaultChoices.getDefault(DialogId.SKILL_USE, param.getSkill());

			// Simulate user action - submit to handler
      boolean use = (defaultChoice == DefaultChoice.YES);
			boolean useNever = (defaultChoice == DefaultChoice.NEVER);

			// Simulate user action  - inject the command into the current step
			Skill skill = param.getSkill();
			String playerId = param.getPlayerId();
      System.out.println("[Timeout] Auto-resolving skill dialog for playerId=" + playerId + ", skill=" + skill.getName());

			// Construct the command just like the client would
			ClientCommandUseSkill autoCommand =
				new ClientCommandUseSkill(skill, use, playerId, null, useNever);

			// Wrap as received command
			ReceivedCommand receivedCommand = new ReceivedCommand(autoCommand, null);

			// Inject into the current step
			IStep currentStep = gameState.getCurrentStep();
			System.out.println("[Timeout] Injecting auto-command into current step: " + currentStep.getClass().getSimpleName());
			gameState.handleCommand(receivedCommand);
		}
	}
  
}
