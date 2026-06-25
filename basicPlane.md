## 1. **FFB Will Eventually Have Multiple Timer “Tiers”**

- **Global Game Time:** Total time allowed per coach, persists entire game.
- **Global Extra Time (per turn):** Bonus time added each turn (like chess increment).
- **Transient Turn Time:** Current 4-minute timer, resets each turn.
- **Reaction/Passive Time:** _Short timer_ (10–30s) for choices _outside your own turn_ (skills, apothecary, inducements, etc).

## 2. **Threaded vs Non-Threaded**

- Threaded: Timers fire events automatically (needs true concurrency; can introduce bugs/locks).
- Non-threaded: Server polls timeouts on its own tick/loop (easier to trace/debug; matches current FFB poll model).

**FFB currently uses non-threaded, poll-on-tick. This is easier for legacy and for your incremental updates.**

## 3. **Dialog/Timer Model**

- Eventually, _all_ dialogs (even legacy) will have a transient timer attached.
- Your timer logic (e.g., `UtilServerTimeout`) will need to generalize to _all_ dialogs, not just skill use.
- Each dialog type will need:

  - _Timer duration_ (by league/game/coach setting, or fixed).
  - _Default/auto-choice_ logic (possibly per-league).

## 4. **League/Settings Support**

- Your filtering logic and timer durations must be _configurable_ (legacy mode, league/competition, admin override).
- Do _not_ hardcode timer values or behaviors in the dialog itself; always check config.

## 5. **Design Guidance**

- **Isolate** all timer/timeout logic in a single subsystem (`UtilServerTimeout` is a good start).
- Design all dialogs to support a timer, even if disabled (legacy mode: timer=off).
- All auto-choice/timeout logic should be per-dialog and centrally managed.
- **No assumptions** about which dialog is timed; must be checked by config at runtime.

## 6. **Next Steps / Planning**

- Design your timer config/strategy pattern.
- List all dialog types; plan which need timers first (skills, apothecary, etc).
- Implement generic timer support for all dialogs; make legacy mode a config option.
- Document how per-league settings feed into the timer logic.

---
