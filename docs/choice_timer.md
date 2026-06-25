# FFB Skill Choice Timers & Timed Dialogs — Implementation Guide (Server-Driven, Extensible)

## 1. **Overview**

- Timed skill-use dialogs (e.g. Side Step, Stand Firm) now enforce timeouts on the **server**.
- **Client only displays the timer.** All timeout, choice, and validation logic is server-side.
- Designed for extension to other dialogs (apothecary, inducements, etc).

---

## 2. **How the System Works**

1. **Server** triggers a skill dialog.
2. If the player is not on the acting team, timedChoice is set to true automatically.
   -This is handled in the `UtilServerDialog.showDialog`.
3. **Client** shows the dialog with a timer label, updated from server’s passive time tick.
4. **If time expires, server injects the default choice into the step**. Client never triggers auto-resolution.
5. **Step validates every command.** If dialog already resolved/closed, ignores extra commands (prevents race and client hacks).
6. **Client receives a `GAME_SET_DIALOG_PARAMETER = null` and closes the dialog.**

---

## 3. **Key Implementation Points**

### A. Dialog Parameter (`DialogSkillUseParameter`)

- Add field: `boolean timedChoice`
- Getter/setter, JSON serialization
- Used to signal the client to display a timer for the dialog

**Example:**

```java
// DialogSkillUseParameter.java
private boolean timedChoice;
public boolean isTimedChoice() { return timedChoice; }
public void setTimedChoice(boolean val) { timedChoice = val; }
// JSON serialization code: see IJsonOption.TIMED_CHOICE
```

---

### B. Centralized Timed Dialog Automation

All timed skill-use dialogs now have their timer set **automatically** in one place:

```java
// UtilServerDialog.showDialog (or equivalent)
if (dialogParameter instanceof DialogSkillUseParameter) {
    DialogSkillUseParameter param = (DialogSkillUseParameter) dialogParameter;
    String playerId = param.getPlayerId();
    Player<?> player = game.getPlayerById(playerId);
    boolean isActingTeam = game.getActingTeam().hasPlayer(player);
    // Automation: Timer for all future skills!
    param.setTimedChoice(!isActingTeam); // True if not acting team, else false
}
```

- **If the player is NOT on the acting team, the dialog will always be timed.**
- **No need to add or change anything for new skills, only the default-choices table.**

---

### C. Adding a New Timed Skill/Dialog

To add another skill (e.g., Stand Firm) as a timed dialog:

- For skill-use dialogs, no changes to individual SkillBehaviour classes are required.
- The timer logic is automatically applied based on team membership.
- Only update `DialogDefaultChoices` if you want a skill to have a different default (Deault is NO).

---

### D. Server-Side Timeout Logic

- The server timer task regularly calls:

```java
UtilServerTimeout.resolveTimeoutIfNeeded(gameState, now);
```

- For any open, timed dialog, if time exceeded:

  - Injects the default choice as a synthetic client command (`ClientCommandUseSkill` etc).
  - Lets the normal game flow close the dialog.

**Example:**

```java
public static void resolveTimeoutIfNeeded(GameState gameState, long now) {
	Game game = gameState.getGame();
	IDialogParameter param = game.getDialogParameter();
	if (param instanceof DialogSkillUseParameter skillParam && skillParam.isTimedChoice()) {
		final long TIMEOUT_MS = 10_000;
		long elapsed = game.getGameTimer().getPassiveElapsed();
		if (elapsed < TIMEOUT_MS) return;

		// Determine default
		DefaultChoice choice = DialogDefaultChoices.getDefault(DialogId.SKILL_USE, skillParam.getSkill());
		boolean use = (choice == DefaultChoice.YES);
		boolean useNever = (choice == DefaultChoice.NEVER);

		ClientCommandUseSkill autoCommand = new ClientCommandUseSkill(skillParam.getSkill(), use, skillParam.getPlayerId(), null, useNever);
		ReceivedCommand receivedCommand = new ReceivedCommand(autoCommand, null);

		gameState.handleCommand(receivedCommand);
	}
}
```

---

### E. Default Choice Table (`DialogDefaultChoices`)

- Add the skill (or dialog) to the static map to set the default choice on timeout.
- Defaults are YES/NO/NEVER. (You can extend this as needed.)

**Example:**

```java
// DialogDefaultChoices.java
private static final Map<String, DefaultChoice> SKILL_DEFAULTS = Map.of(
	"Side Step", DefaultChoice.NO,
	"Stand Firm", DefaultChoice.NO,
	"Jump Up", DefaultChoice.YES // etc
);

public static DefaultChoice getDefault(DialogId id, Skill skill) {
	if (id == DialogId.SKILL_USE && skill != null) {
		return SKILL_DEFAULTS.getOrDefault(skill.getName(), DefaultChoice.NO);
	}
	return DefaultChoice.NO; // fallback
}
```

---

### F. Client-Side UI

- Checks `timedChoice`, displays a timer label if true.
- Timer updated every server tick, never starts/stops locally.
- **No client logic for timeout or auto-choice.**

---

## 5. **Failsafe / Race Condition Handling**

### **Purpose**

- **Prevents bugs, race conditions, and client exploits.**
- Ensures that the server **never processes a skill-use command unless the dialog is still valid and open.**
- Required for every skill/dialog step that handles timed or user-submitted choices.

---

### **How it works**

**Inside each step’s `handleCommand` method (server-side),** call the **utility method** to check if the dialog is still open and valid before processing any incoming `CLIENT_USE_SKILL` (or similar) command.

**This guarantees all logic is consistent, centralized, and easy to maintain.**

---

### **Example: Centralized Failsafe Utility**

```java
// In UtilServerDialog.java
	public static boolean isValidSkillDialog(Game game, ClientCommandUseSkill cmd) {
    return game.getDialogParameter() instanceof DialogSkillUseParameter
			&& ((DialogSkillUseParameter) game.getDialogParameter()).getPlayerId().equals(cmd.getPlayerId())
			&& ((DialogSkillUseParameter) game.getDialogParameter()).getSkill().equals(cmd.getSkill());
  }
```

---

### **Step Code Example: Side Step (StepPushback.java)**

```java
@Override
public StepCommandStatus handleCommand(ReceivedCommand pReceivedCommand) {
	StepCommandStatus commandStatus = super.handleCommand(pReceivedCommand);
	if (commandStatus == StepCommandStatus.UNHANDLED_COMMAND) {
		switch (pReceivedCommand.getId()) {
			case CLIENT_USE_SKILL:
				ClientCommandUseSkill cmd = (ClientCommandUseSkill) pReceivedCommand.getCommand();
				if (UtilServerDialog.isValidSkillDialog(getGameState().getGame(), cmd)) {
					commandStatus = handleSkillCommand(cmd, state);
				} else {
					// Ignore as stale/late/invalid
					System.out.println("CLIENT_USE_SKILL rejected: no active dialog or already resolved for player " + cmd.getPlayerId());
				}
				break;
			// ... other cases
		}
	}
	if (commandStatus == StepCommandStatus.EXECUTE_STEP) {
		executeStep();
	}
	return commandStatus;
}
```

---

## 6. **Debugging**

- Server log:

  - `[Timeout] Auto-resolving skill dialog for playerId=...`
  - `[StepPushback] handleSkillCommand: skill=Side Step, use=false`
  - `CLIENT_USE_SKILL rejected: no active dialog or already resolved for player ...`

- Client:

  - Receives dialog parameter null, closes dialog.

---
