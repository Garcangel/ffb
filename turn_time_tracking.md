# FFB Turn Timing & Passive Time System – Implementation Overview

## 1. Purpose

- **Track timing** of each team's turns and all passive (“waiting for decision”) intervals.
- **Log, analyze, and support future rule enforcement** for slow play, stalling, or timeout decisions.

---

## 2. Main Components Added/Changed

### 2.1 `GameTimer` (model/GameTimer.java)

- **New class** added as a field in `Game`.
- Holds two `TeamTimeTracker` instances: `homeTimer`, `awayTimer`.
- Tracks per-turn timing, total overtime, unused time, and **new**: passive time per team.
- Centralizes all timing-related data for the game.

**Key methods:**

- `recordTurn(Game game, boolean wasHomeTeam, long durationMs, boolean newHalf, boolean touchdown, boolean endGame)`

  - Appends a timing event (half, turn, ms, limit, timestamp, reason) to the relevant team.
  - “Reason” auto-labeled as REGULAR, TOUCHDOWN, HALF_END, GAME_END, or by turn mode.

- `recordPassiveTime(boolean isHomeTeam, long durationMs, String context)`

  - Adds a passive time entry for a team, for e.g. skill selection, inducement, etc.

**Fields for passive timing:**

- `private long passiveTimeStarted = 0;`
- `private long passiveElapsed = 0;`![![alt text](image-1.png)](image.png)

**Methods for passive timer state:**

```java
public void startPassive(long now) {
	passiveStart = now;
	passiveElapsed = 0;
}
public void stopPassive(long now) {
	if (passiveStart > 0) {
		passiveElapsed = now - passiveStart;
		passiveStart = 0;
	}
}
public long getPassiveElapsed() { return passiveElapsed; }
public void clearPassive() {
	passiveElapsed = 0;
	passiveStart = 0;
}
```

---

### 2.2 `TeamTimeTracker` (model/TeamTimeTracker.java)

- Records **per-team** logs of all turn and passive events.
- **New:** Now uses a unified `TimingEvent` class for both turn and passive events (renamed from `TurnTiming`).

**Key fields:**

- `List<TimingEvent> timingLog` – turn-by-turn log, in order.
- `List<TimingEvent> passiveLog` – passive event log (if used; currently passive total is primary stat).

**`TimingEvent` fields:**

- `half`, `turnNumber`, `durationMs`, `turnLimitMs`, `timestampMs`, `reason`

**Main methods:**

- `recordTurn(...)` – logs the turn (updates total time used, overtime, unused).
- `recordPassiveTime(...)` – logs passive event (updates total passive time).
- All relevant **getters** for totals and averages.

---

### 2.3 `UtilServerPassiveTimer` (server/util/UtilServerPassiveTimer.java)

- **New utility** to manage passive timer state, designed to mirror `UtilServerTimer`.
- **Does NOT use or touch legacy `GameState` time fields.**
- All passive timing state lives in `GameTimer`.

**Key methods:**

```java
public static void startPassiveTimer(GameState gameState, long currentTimeMillis) {
	GameTimer timer = gameState.getGame().getGameTimer();
	timer.startPassive(currentTimeMillis);
}

public static void stopPassiveTimer(GameState gameState, long currentTimeMillis) {
	GameTimer timer = gameState.getGame().getGameTimer();
	timer.stopPassive(currentTimeMillis);
}
```

---

### 2.4 `UtilServerDialog` (server/util/UtilServerDialog.java)

- **Central switch:** On dialog events that require pausing the turn timer,
  both timers are now managed together for full timing accuracy.

  - On `showDialog(...)` with `stopTurnTimer=true`:

    - Calls `UtilServerTimer.stopTurnTimer(...)`
    - **And:** `UtilServerPassiveTimer.startPassiveTimer(...)`

  - On `hideDialog(...)`:

    - Calls `UtilServerPassiveTimer.stopPassiveTimer(...)`
    - **Then:** `UtilServerTimer.startTurnTimer(...)`

- **Any direct/manual call to `UtilServerTimer.stopTurnTimer(...)`** elsewhere
  (e.g. inside skill behavior (side step) or steps **not using `showDialog`**)
  **MUST** also call `UtilServerPassiveTimer.startPassiveTimer(...)`
  immediately after, to keep the timing system accurate.

---

### 2.5 Integration in Game Flow

#### a. **End of Turn**

- In `StepEndTurn.java`, after all state updates:

  ```java
  game.getGameTimer().recordTurn(game, isHomeTurnEnding, game.getTurnTime(), fNewHalf, fTouchdown, fEndGame);
  ```

  - **Records**: half, turn, ms, limit, timestamp, reason for just-ended team.

#### b. **Passive Events**

- Whenever a dialog stops the turn timer, passive timer starts.
- When dialog closes, passive timer stops and (optionally) logs to the team tracker.

---

### 2.6 Game Integration

#### a. **Game.java**

Add field:

```java
private final GameTimer gameTimer = new GameTimer();
public GameTimer getGameTimer() { return gameTimer; }
```

---

## 2.7 Sending Passive Time to the Client

**Server Side:**

- The server **now sends passive time** as part of `ServerCommandGameTime`:

  ```java
  // In server communication handler:
  ServerCommandGameTime gameTimeCommand = new ServerCommandGameTime(
      game.getGameTime(),
      game.getTurnTime(),
      game.getGameTimer().getPassiveElapsed()
  );
  sendAllSessions(gameState, gameTimeCommand, false);
  ```

- **Serialization** in `ServerCommandGameTime` adds `PASSIVE_TIME` to the outgoing JSON:

  ```java
  IJsonOption.PASSIVE_TIME.addTo(jsonObject, fPassiveTime);
  ```

---

**Client Side:**

- **Handler (`ClientCommandHandlerGameTime`):**

  - Reads passive time from the incoming `ServerCommandGameTime`.
  - (Extend as needed: For now, you might log, display, or store it in the client.)

  ```java
  gameTitle.setPassiveTime(gameTimeCommand.getPassiveTime());
  // or similar: update passive time in client state/UI
  ```

---

**Key Point:**

- **Passive time** is now sent and handled **exactly like turn time and game time**.
- Only the **new field and handler logic** are needed; all legacy functionality remains untouched.

---

## 3. Example Timing Event

```java
TimingEvent(
  half = 1,
  turnNumber = 3,
  durationMs = 35000,
  turnLimitMs = 240000,
  timestampMs = 1721028898390,
  reason = "REGULAR"  // or "TOUCHDOWN", "HALF_END", etc
)
```

---

## 4. Logging / Debug

- On every record, both turn and passive, events are logged to stdout for debugging and data validation.

---

## 5. Legacy / Technical Notes

- **Passive timing state is NOT stored in GameState or legacy timer fields.**
- All code is **side-effect free:** No change to game rules, enforcement, or user experience yet.
- **Compatible with existing TimerTask tick system**—can be extended to tick passive timer like turn timer.

---

## 6. Future Roadmap

- Unify all timing under a single timer/ticker for turn/passive/game, as legacy is retired.
