# Remove Rogue Player Lists Design

## Goal

Remove the persisted rogue/safe-zone player UUID lists from `RogueData` and use each player's `PlayerPhase` as the single source of truth for rogue participation state.

## Current Problem

`RogueData` persists two player lists:

- `roguePlayerIdStrings`
- `safeZonePlayerIdStrings`

These lists duplicate information already represented by `PlayerRogueData.phaseId`. They also create extra synchronization work: zone changes, login/logout, dimension changes, progress start, node interaction, rewards, and settlement all need to keep the lists aligned with player phase.

The direct fields are only used inside `RogueData`, but the list-style API is used by commands and rogue flow code. The refactor must remove the list model without losing the ability to find active rogue players.

## State Model

Add one player phase:

```java
PREPARE_ROGUE("prepare_rogue")
```

Phase semantics:

- `LOBBY`: player is not participating in the current rogue run.
- `PREPARE_ROGUE`: player has left the safe zone, joined the run, and received a loot bag.
- `PRE_ROGUE`: player opened the loot bag and is ready to start.
- `ON_PROGRESS`, `PRE_NODE`, `PRE_EVENT`, `ON_EVENT`, `PROGRESS_FINISH`, `REWARD`, `SPECTATOR`, `DEAD`: player is participating in the current run.

An online player is considered a rogue participant when `PlayerPhase != LOBBY`.

## Architecture

`PlayerRogueData` remains the persisted owner of player state. `RogueData` keeps only global run data such as phase, seed, progress, node data, and cap data.

`RogueContext` becomes the single query boundary for active participants:

- `playersInRogue(level)` scans the server's online players and returns players whose phase is not `LOBBY`.
- `allPlayersMatchPhase(level, phase)` uses `playersInRogue(level)`.
- Batch operations such as `setAllPlayerPhase` continue to use `playersInRogue(level)`.

Code outside `RogueContext` should not reintroduce UUID list ownership. If a caller needs to know whether a player is participating, it should check `ctx.getPlayerPhase(player) != PlayerPhase.LOBBY` or use a small local helper with that exact meaning.

## Flow Changes

Leaving the safe zone:

- `ProgressStartCap.tryLeaveSafeZone` succeeds only for `LOBBY` players.
- On success, it sets the player phase to `PREPARE_ROGUE`.
- It gives the loot bag and does not write any global player list.

Opening the loot bag:

- `ProgressStartCap.tryOpenLootBag` only accepts `PREPARE_ROGUE`.
- It sets the player phase to `PRE_ROGUE` after giving starting equipment.
- Existing `PRE_ROGUE` behavior remains the "already opened and ready" state.

Returning to the safe zone or leaving the rogue dimension:

- The player phase is set to `LOBBY`.
- If no online players remain in rogue, the global progress is marked inactive and global phase returns to `LOBBY`.

Node and event handling:

- Node interaction checks player participation with `PlayerPhase != LOBBY`.
- Ready counts still compare against `ctx.playersInRogue(level).size()`.
- Reward events grant rewards to `ctx.playersInRogue(level)`.

Settlement:

- Settlement iterates `ctx.playersInRogue(level)`.
- After all rewards are confirmed and players are teleported back, each player is set to `LOBBY`.
- No UUID removal is needed.

Login/logout and dimension changes:

- Logout does not need to mutate a global list.
- Entering the rogue dimension does not need to add a safe-zone entry.
- Leaving the rogue dimension sets the player's phase to `LOBBY`.

## Command Changes

Remove the old list commands:

- `/beyond config playerList ...`
- `/beyond config safeZoneList ...`

Replace them with top-level player phase debug commands under `/beyond`, without the `config` prefix:

- `/beyond playerPhase get <player>`
- `/beyond playerPhase set <player> <phase>`
- `/beyond unlockCurrentNode`

Command behavior:

- `get` prints the target player's current `PlayerPhase`.
- `set` parses an enum name case-insensitively and writes that phase to `PlayerRogueData`.
- Suggestions for `<phase>` use all `PlayerPhase` enum names.
- `unlockCurrentNode` is a player-only debug command. It finds the node containing the executing player's current chunk. If no node contains that chunk, the command fails without changing state.
- `unlockCurrentNode` does not advance progress for an already unlocked node.
- `unlockCurrentNode` reuses the normal node unlock flow: mark the node unlocked, advance the scene index, expand the active zone, sync data, and move the global rogue phase to `PROGRESS_FINISH` if all scenes are complete or `ON_PROGRESS` otherwise.
- These commands are debug/admin tools. They do not pretend to manage separate rogue or safe-zone lists.

`RogueEncounterRunner` should expose the existing unlock behavior as a small public method so command code does not copy the side effects of node completion.

## Migration

No data migration is required for the removed lists. Existing saved `PlayerRogueData.phaseId` remains valid.

If an old world has stale `roguePlayerIdStrings` or `safeZonePlayerIdStrings` entries, they are ignored after this refactor because the fields no longer exist in `RogueData`.

## Validation

Keep verification light:

- Use IDEA MCP inspections on touched Java files.
- Build the project with Gradle.
- Do not add complex tests for this pass; game-flow testing will be done manually in Minecraft.

## Non-Goals

- Do not redesign the full rogue phase system.
- Do not add offline-player participation tracking.
- Do not introduce new global player registries.
- Do not change unrelated zone rendering, node generation, or reward balancing behavior.
