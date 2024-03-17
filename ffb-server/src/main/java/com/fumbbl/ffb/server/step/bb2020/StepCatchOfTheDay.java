package com.fumbbl.ffb.server.step.bb2020;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.fumbbl.ffb.FieldCoordinate;
import com.fumbbl.ffb.ReRolledActions;
import com.fumbbl.ffb.RulesCollection;
import com.fumbbl.ffb.SkillUse;
import com.fumbbl.ffb.SoundId;
import com.fumbbl.ffb.factory.IFactorySource;
import com.fumbbl.ffb.json.UtilJson;
import com.fumbbl.ffb.model.ActingPlayer;
import com.fumbbl.ffb.model.Game;
import com.fumbbl.ffb.model.property.NamedProperties;
import com.fumbbl.ffb.model.skill.Skill;
import com.fumbbl.ffb.report.ReportSkillUse;
import com.fumbbl.ffb.report.bb2020.ReportCatchOfTheDayRoll;
import com.fumbbl.ffb.report.bb2020.ReportSkillWasted;
import com.fumbbl.ffb.server.GameState;
import com.fumbbl.ffb.server.IServerJsonOption;
import com.fumbbl.ffb.server.net.ReceivedCommand;
import com.fumbbl.ffb.server.step.AbstractStepWithReRoll;
import com.fumbbl.ffb.server.step.StepAction;
import com.fumbbl.ffb.server.step.StepCommandStatus;
import com.fumbbl.ffb.server.step.StepId;
import com.fumbbl.ffb.server.step.StepParameter;
import com.fumbbl.ffb.server.step.StepParameterKey;
import com.fumbbl.ffb.server.step.StepParameterSet;
import com.fumbbl.ffb.server.util.UtilServerReRoll;
import com.fumbbl.ffb.util.UtilCards;

import java.util.Arrays;

@RulesCollection(RulesCollection.Rules.BB2020)
public class StepCatchOfTheDay extends AbstractStepWithReRoll {

	private boolean endPlayerAction, endTurn;
	private String goToLabelOnFailure;

	public StepCatchOfTheDay(GameState pGameState) {
		super(pGameState);
	}

	@Override
	public StepId getId() {
		return StepId.CATCH_OF_THE_DAY;
	}

	@Override
	public void init(StepParameterSet pParameterSet) {
		super.init(pParameterSet);
		if (pParameterSet != null) {
			Arrays.stream(pParameterSet.values()).forEach(parameter -> {
				if (parameter.getKey() == StepParameterKey.GOTO_LABEL_ON_FAILURE) {
					goToLabelOnFailure = (String) parameter.getValue();
				}
			});
		}
	}

	@Override
	public boolean setParameter(StepParameter parameter) {
		if (parameter != null) {
			switch (parameter.getKey()) {
				case END_TURN:
					endTurn = toPrimitive((Boolean) parameter.getValue());
					return true;
				case END_PLAYER_ACTION:
					endPlayerAction = toPrimitive((Boolean) parameter.getValue());
					return true;
				default:
					break;
			}
		}

		return super.setParameter(parameter);
	}

	@Override
	public StepCommandStatus handleCommand(ReceivedCommand pReceivedCommand) {
		StepCommandStatus stepCommandStatus = super.handleCommand(pReceivedCommand);

		if (stepCommandStatus == StepCommandStatus.EXECUTE_STEP) {
			executeStep();
		}

		return stepCommandStatus;
	}

	@Override
	public void start() {
		super.start();
		executeStep();
	}

	private void executeStep() {

		getResult().setNextAction(StepAction.NEXT_STEP);

		Game game = getGameState().getGame();
		ActingPlayer actingPlayer = game.getActingPlayer();
		Skill skill = UtilCards.getUnusedSkillWithProperty(actingPlayer, NamedProperties.canGetBallOnGround);
		if (skill != null || getReRolledAction() == ReRolledActions.CATCH_OF_THE_DAY) {

			if (endTurn || endPlayerAction) {
				getResult().addReport(new ReportSkillWasted(actingPlayer.getPlayerId(), skill));
				getResult().setNextAction(StepAction.GOTO_LABEL, goToLabelOnFailure);
				markUsages(game, actingPlayer, skill);
				return;
			}

			if (getReRolledAction() == ReRolledActions.CATCH_OF_THE_DAY) {
				if (getReRollSource() == null || !UtilServerReRoll.useReRoll(this, getReRollSource(), actingPlayer.getPlayer())) {
					getResult().setSound(SoundId.BOUNCE);
					return;
				}
			} else {
				markUsages(game, actingPlayer, skill);
			}


			getResult().addReport(new ReportSkillUse(actingPlayer.getPlayerId(), skill, true, SkillUse.GET_BALL_ON_GROUND));
			FieldCoordinate playerCoordinate = game.getFieldModel().getPlayerCoordinate(actingPlayer.getPlayer());
			FieldCoordinate ballCoordinate = game.getFieldModel().getBallCoordinate();

			if (game.getFieldModel().isBallMoving() && playerCoordinate.distanceInSteps(ballCoordinate) <= 3) {

				int roll = getGameState().getDiceRoller().rollDice(6);
				boolean success = roll >=3;

				if (success) {
					game.getFieldModel().setBallCoordinate(playerCoordinate);
					game.getFieldModel().setBallMoving(false);
					getResult().setSound(SoundId.PICKUP);
				} else {
					if (getReRolledAction() != ReRolledActions.CATCH_OF_THE_DAY && UtilServerReRoll.askForReRollIfAvailable(getGameState(), actingPlayer, ReRolledActions.CATCH_OF_THE_DAY, 3, false)) {
						setReRolledAction(ReRolledActions.CATCH_OF_THE_DAY);
						getResult().setNextAction(StepAction.CONTINUE);
					} else {
						getResult().setSound(SoundId.BOUNCE);
					}
				}

				getResult().addReport(new ReportCatchOfTheDayRoll(actingPlayer.getPlayerId(), success, roll, 3, getReRolledAction() == ReRolledActions.CATCH_OF_THE_DAY));

			} else {
				getResult().addReport(new ReportSkillWasted(actingPlayer.getPlayerId(), skill));
			}




		}
	}

	private void markUsages(Game game, ActingPlayer actingPlayer, Skill skill) {
		markActionUsed(game, actingPlayer);
		actingPlayer.markSkillUsed(skill);
	}

	private void markActionUsed(Game game, ActingPlayer actingPlayer) {
		switch (actingPlayer.getPlayerAction()) {
			case BLITZ:
			case BLITZ_MOVE:
			case KICK_EM_BLITZ:
				game.getTurnData().setBlitzUsed(true);
				break;
			case KICK_TEAM_MATE:
			case KICK_TEAM_MATE_MOVE:
				game.getTurnData().setKtmUsed(true);
				break;
			case PASS:
			case PASS_MOVE:
			case THROW_TEAM_MATE:
			case THROW_TEAM_MATE_MOVE:
				game.getTurnData().setPassUsed(true);
				break;
			case HAND_OVER:
			case HAND_OVER_MOVE:
				game.getTurnData().setHandOverUsed(true);
				break;
			case FOUL:
			case FOUL_MOVE:
				if (!actingPlayer.getPlayer().hasSkillProperty(NamedProperties.allowsAdditionalFoul)) {
					game.getTurnData().setFoulUsed(true);
				}
				break;
			default:
				break;
		}
	}

	@Override
	public JsonObject toJsonValue() {
		JsonObject jsonObject = super.toJsonValue();
		IServerJsonOption.END_TURN.addTo(jsonObject, endTurn);
		IServerJsonOption.END_PLAYER_ACTION.addTo(jsonObject, endPlayerAction);
		IServerJsonOption.GOTO_LABEL_ON_FAILURE.addTo(jsonObject, goToLabelOnFailure);
		return jsonObject;
	}

	@Override
	public AbstractStepWithReRoll initFrom(IFactorySource source, JsonValue jsonValue) {
		super.initFrom(source, jsonValue);
		JsonObject jsonObject = UtilJson.toJsonObject(jsonValue);
		endPlayerAction = IServerJsonOption.END_PLAYER_ACTION.getFrom(source, jsonObject);
		endTurn = IServerJsonOption.END_TURN.getFrom(source, jsonObject);
		goToLabelOnFailure = IServerJsonOption.GOTO_LABEL_ON_FAILURE.getFrom(source, jsonObject);
		return this;
	}
}
