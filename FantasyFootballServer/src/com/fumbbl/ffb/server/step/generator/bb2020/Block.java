package com.fumbbl.ffb.server.step.generator.bb2020;

import com.fumbbl.ffb.ApothecaryMode;
import com.fumbbl.ffb.RulesCollection;
import com.fumbbl.ffb.server.GameState;
import com.fumbbl.ffb.server.IServerLogLevel;
import com.fumbbl.ffb.server.step.IStepLabel;
import com.fumbbl.ffb.server.step.StepId;
import com.fumbbl.ffb.server.step.StepParameterKey;
import com.fumbbl.ffb.server.step.generator.Sequence;

import static com.fumbbl.ffb.server.step.StepParameter.from;

@RulesCollection(RulesCollection.Rules.BB2020)
public class Block extends com.fumbbl.ffb.server.step.generator.Block {

	@Override
	public void pushSequence(com.fumbbl.ffb.server.step.generator.Block.SequenceParams params) {
		GameState gameState = params.getGameState();
		gameState.getServer().getDebugLog().log(IServerLogLevel.DEBUG, gameState.getId(),
			"push blockSequence onto stack");

		Sequence sequence = new Sequence(gameState);

		sequence.add(StepId.INIT_BLOCKING, from(StepParameterKey.GOTO_LABEL_ON_END, IStepLabel.END_BLOCKING),
			from(StepParameterKey.BLOCK_DEFENDER_ID, params.getBlockDefenderId()), from(StepParameterKey.USING_STAB, params.isUsingStab()),
			from(StepParameterKey.USING_CHAINSAW, params.isUsingChainsaw()), from(StepParameterKey.USING_VOMIT, params.isUsingVomit()),
			from(StepParameterKey.ASK_FOR_BLOCK_KIND, params.isAskForBlockKind()), from(StepParameterKey.PUBLISH_DEFENDER, params.isPublishDefender()));
		sequence.add(StepId.INIT_ACTIVATION);
		sequence.add(StepId.ANIMAL_SAVAGERY,
			from(StepParameterKey.GOTO_LABEL_ON_FAILURE, IStepLabel.END_BLOCKING));
		sequence.add(StepId.HANDLE_DROP_PLAYER_CONTEXT);
		sequence.add(StepId.PLACE_BALL);
		sequence.add(StepId.APOTHECARY,
			from(StepParameterKey.APOTHECARY_MODE, ApothecaryMode.ANIMAL_SAVAGERY));
		sequence.add(StepId.CATCH_SCATTER_THROW_IN);
		sequence.add(StepId.SET_DEFENDER, from(StepParameterKey.BLOCK_DEFENDER_ID, params.getBlockDefenderId()));
		sequence.add(StepId.GOTO_LABEL, from(StepParameterKey.GOTO_LABEL, IStepLabel.NEXT), from(StepParameterKey.ALTERNATE_GOTO_LABEL, IStepLabel.END_BLOCKING));
		sequence.add(StepId.BONE_HEAD, IStepLabel.NEXT, from(StepParameterKey.GOTO_LABEL_ON_FAILURE, IStepLabel.END_BLOCKING));
		sequence.add(StepId.REALLY_STUPID, from(StepParameterKey.GOTO_LABEL_ON_FAILURE, IStepLabel.END_BLOCKING));
		sequence.add(StepId.TAKE_ROOT);
		sequence.add(StepId.UNCHANNELLED_FURY, from(StepParameterKey.GOTO_LABEL_ON_FAILURE, IStepLabel.END_BLOCKING));
		sequence.add(StepId.FOUL_APPEARANCE, from(StepParameterKey.GOTO_LABEL_ON_FAILURE, IStepLabel.END_BLOCKING));
		sequence.add(StepId.DUMP_OFF);
		sequence.add(StepId.JUMP_UP, from(StepParameterKey.GOTO_LABEL_ON_FAILURE, IStepLabel.END_BLOCKING));
		sequence.add(StepId.STAND_UP, from(StepParameterKey.GOTO_LABEL_ON_FAILURE, IStepLabel.END_BLOCKING));
		sequence.add(StepId.BLOCK_STATISTICS);
		sequence.add(StepId.DAUNTLESS);
		sequence.add(StepId.STAB, from(StepParameterKey.GOTO_LABEL_ON_SUCCESS, IStepLabel.DEFENDER_DROPPED));
		sequence.add(StepId.BLOCK_CHAINSAW, from(StepParameterKey.GOTO_LABEL_ON_SUCCESS, IStepLabel.DEFENDER_DROPPED),
			from(StepParameterKey.GOTO_LABEL_ON_FAILURE, IStepLabel.ATTACKER_DROPPED));
		sequence.add(StepId.PROJECTILE_VOMIT, from(StepParameterKey.GOTO_LABEL_ON_SUCCESS, IStepLabel.DEFENDER_DROPPED),
			from(StepParameterKey.GOTO_LABEL_ON_FAILURE, IStepLabel.ATTACKER_DROPPED));
		sequence.add(StepId.HANDLE_DROP_PLAYER_CONTEXT);
		// GFI for ball & chain should go here.
		sequence.add(StepId.GO_FOR_IT, from(StepParameterKey.GOTO_LABEL_ON_FAILURE, IStepLabel.NEXT),
			from(StepParameterKey.BALL_AND_CHAIN_GFI, true));
		sequence.add(StepId.BLOCK_BALL_AND_CHAIN, IStepLabel.NEXT, from(StepParameterKey.GOTO_LABEL_ON_PUSHBACK, IStepLabel.PUSHBACK));
		sequence.add(StepId.BLOCK_ROLL);
		sequence.add(StepId.BLOCK_CHOICE, from(StepParameterKey.GOTO_LABEL_ON_DODGE, IStepLabel.DODGE_BLOCK),
			from(StepParameterKey.GOTO_LABEL_ON_JUGGERNAUT, IStepLabel.JUGGERNAUT),
			from(StepParameterKey.GOTO_LABEL_ON_PUSHBACK, IStepLabel.PUSHBACK));
		sequence.jump(IStepLabel.DROP_FALLING_PLAYERS);
		sequence.add(StepId.HANDLE_DROP_PLAYER_CONTEXT);

		// on blockChoice = BOTH_DOWN
		sequence.add(StepId.JUGGERNAUT, IStepLabel.JUGGERNAUT,
			from(StepParameterKey.GOTO_LABEL_ON_SUCCESS, IStepLabel.PUSHBACK));
		sequence.add(StepId.BOTH_DOWN);
		sequence.add(StepId.WRESTLE);
		sequence.jump(IStepLabel.DROP_FALLING_PLAYERS);

		// on blockChoice = POW_PUSHBACK
		sequence.add(StepId.BLOCK_DODGE, IStepLabel.DODGE_BLOCK);

		// on blockChoice = POW or PUSHBACK
		sequence.add(StepId.PUSHBACK, IStepLabel.PUSHBACK);
		sequence.add(StepId.APOTHECARY, from(StepParameterKey.APOTHECARY_MODE, ApothecaryMode.CROWD_PUSH));
		sequence.add(StepId.FOLLOWUP);
		sequence.add(StepId.TENTACLES, from(StepParameterKey.GOTO_LABEL_ON_SUCCESS, IStepLabel.DROP_FALLING_PLAYERS));
		sequence.add(StepId.SHADOWING);
		sequence.add(StepId.PICK_UP, from(StepParameterKey.GOTO_LABEL_ON_FAILURE, IStepLabel.DROP_FALLING_PLAYERS));
		sequence.jump(IStepLabel.DROP_FALLING_PLAYERS);
		sequence.add(StepId.FALL_DOWN, IStepLabel.FALL_DOWN);
		sequence.jump(IStepLabel.ATTACKER_DROPPED);

		// on blockChoice = SKULL
		sequence.add(StepId.DROP_FALLING_PLAYERS, IStepLabel.DROP_FALLING_PLAYERS);
		sequence.add(StepId.HANDLE_DROP_PLAYER_CONTEXT);
		sequence.add(StepId.PLACE_BALL, IStepLabel.DEFENDER_DROPPED);
		sequence.add(StepId.APOTHECARY,
			from(StepParameterKey.APOTHECARY_MODE, ApothecaryMode.DEFENDER));

		sequence.jump(IStepLabel.ATTACKER_DROPPED);
		sequence.add(StepId.DROP_FALLING_PLAYERS, IStepLabel.DROP_FALLING_PLAYERS);
		sequence.add(StepId.HANDLE_DROP_PLAYER_CONTEXT);
		sequence.add(StepId.FALL_DOWN);

		sequence.add(StepId.PLACE_BALL, IStepLabel.ATTACKER_DROPPED);

		sequence.add(StepId.APOTHECARY,
			from(StepParameterKey.APOTHECARY_MODE, ApothecaryMode.ATTACKER));

		sequence.add(StepId.TRAP_DOOR, IStepLabel.SCATTER_BALL);
		sequence.add(StepId.APOTHECARY, from(StepParameterKey.APOTHECARY_MODE, ApothecaryMode.TRAP_DOOR));
		sequence.add(StepId.CATCH_SCATTER_THROW_IN);

		sequence.add(StepId.END_BLOCKING, IStepLabel.END_BLOCKING);
		// may insert endTurn sequence add this point

		gameState.getStepStack().push(sequence.getSequence());

	}

}
