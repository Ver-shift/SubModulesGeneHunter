# Remove Rogue Player Lists Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Remove rogue/safe-zone UUID lists from `RogueData`, use `PlayerPhase` as the player participation source, and add debug commands for player phase and current-node unlock.

**Architecture:** `PlayerRogueData.phaseId` owns player state. `RogueContext` scans online players to derive active rogue participants. Commands call existing domain helpers instead of maintaining separate player lists.

**Tech Stack:** NeoForge Minecraft mod, Java, Brigadier commands, LowDragLib persisted data, Gradle.

---

## Files

- Modify: `Beyond/src/main/java/org/galaxy/beyond/api/system/rogue/core/PlayerPhase.java`
- Modify: `Beyond/src/main/java/org/galaxy/beyond/api/system/rogue/RogueData.java`
- Modify: `Beyond/src/main/java/org/galaxy/beyond/api/system/rogue/RogueContext.java`
- Modify: `Beyond/src/main/java/org/galaxy/beyond/api/system/rogue/cap/PlayerInGameCap.java`
- Modify: `Beyond/src/main/java/org/galaxy/beyond/api/system/rogue/cap/ProgressStartCap.java`
- Modify: `Beyond/src/main/java/org/galaxy/beyond/api/system/rogue/cap/NodeCap.java`
- Modify: `Beyond/src/main/java/org/galaxy/beyond/api/system/rogue/cap/PlayerProgressFinishCap.java`
- Modify: `Beyond/src/main/java/org/galaxy/beyond/api/system/rogue/core/RogueEncounterRunner.java`
- Modify: `Beyond/src/main/java/org/galaxy/beyond/rogue_event/RewardEventType.java`
- Modify: `Beyond/src/main/java/org/galaxy/beyond/comand/BeyondCommand.java`
- Modify: `Beyond/src/main/resources/assets/beyond/lang/en_us.json`
- Modify: `Beyond/src/main/resources/assets/beyond/lang/zh_cn.json`

## Task 1: Add Player Phase State

**Files:**
- Modify: `Beyond/src/main/java/org/galaxy/beyond/api/system/rogue/core/PlayerPhase.java`

- [ ] **Step 1: Add `PREPARE_ROGUE`**

Add the enum constant between `LOBBY` and `PRE_ROGUE`:

```java
LOBBY("lobby"),
PREPARE_ROGUE("prepare_rogue"),
PRE_ROGUE("pre_rogue"),
```

- [ ] **Step 2: Run IDEA inspection**

Use IDEA MCP `get_file_problems` on `PlayerPhase.java`.

Expected: no Java errors.

## Task 2: Remove Persisted Player Lists

**Files:**
- Modify: `Beyond/src/main/java/org/galaxy/beyond/api/system/rogue/RogueData.java`

- [ ] **Step 1: Remove list fields and helpers**

Delete these fields:

```java
@Persisted
private List<String> roguePlayerIdStrings = new ArrayList<>();

@Persisted
private List<String> safeZonePlayerIdStrings = new ArrayList<>();
```

Delete these methods:

```java
public Set<UUID> getRoguePlayerIds()
public boolean addRoguePlayer(UUID playerId)
public boolean removeRoguePlayer(UUID playerId)
public boolean isRoguePlayer(UUID playerId)
public Set<UUID> getSafeZonePlayerIds()
public boolean addSafeZonePlayer(UUID playerId)
public boolean removeSafeZonePlayer(UUID playerId)
public boolean isSafeZonePlayer(UUID playerId)
private static Set<UUID> toStringSet(List<String> strings)
```

Remove now-unused imports:

```java
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
```

Keep `CopyOnWriteArrayList`.

- [ ] **Step 2: Run IDEA inspection**

Use IDEA MCP `get_file_problems` on `RogueData.java`.

Expected: no Java errors in this file. Other files may still fail until the caller-replacement tasks run.

## Task 3: Derive Rogue Participants From PlayerPhase

**Files:**
- Modify: `Beyond/src/main/java/org/galaxy/beyond/api/system/rogue/RogueContext.java`

- [ ] **Step 1: Replace UUID-list participant scanning**

Update `playersInRogue`:

```java
@Override
public List<ServerPlayer> playersInRogue(ServerLevel level) {
    List<ServerPlayer> result = new ArrayList<>();
    for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
        if (player.level() != level) continue;
        if (getPlayerPhase(player) != PlayerPhase.LOBBY) result.add(player);
    }
    return result;
}
```

Update `allPlayersMatchPhase`:

```java
@Override
public boolean allPlayersMatchPhase(ServerLevel level, PlayerPhase phase) {
    var players = playersInRogue(level);
    if (players.isEmpty()) return false;
    for (ServerPlayer player : players) {
        if (getPlayerPhase(player) != phase) return false;
    }
    return true;
}
```

Remove unused `java.util.*` if the IDE suggests narrower imports.

- [ ] **Step 2: Run IDEA inspection**

Use IDEA MCP `get_file_problems` on `RogueContext.java`.

Expected: no Java errors.

## Task 4: Update Rogue Flow Callers

**Files:**
- Modify: `Beyond/src/main/java/org/galaxy/beyond/api/system/rogue/cap/PlayerInGameCap.java`
- Modify: `Beyond/src/main/java/org/galaxy/beyond/api/system/rogue/cap/ProgressStartCap.java`
- Modify: `Beyond/src/main/java/org/galaxy/beyond/api/system/rogue/cap/NodeCap.java`
- Modify: `Beyond/src/main/java/org/galaxy/beyond/api/system/rogue/cap/PlayerProgressFinishCap.java`
- Modify: `Beyond/src/main/java/org/galaxy/beyond/rogue_event/RewardEventType.java`

- [ ] **Step 1: Simplify `PlayerInGameCap`**

Remove global list mutations from `changeZone`, `onPlayerLogin`, and `onPlayerLogout`.

Keep dimension leaving cleanup:

```java
@SubscribeEvent
public static void onPlayerChangeDim(PlayerEvent.PlayerChangedDimensionEvent event) {
    if (event.getEntity() instanceof ServerPlayer player) {
        var rogueDim = CommonConfig.getRogueDimension();
        if (event.getFrom().equals(rogueDim) && !event.getTo().equals(rogueDim)) {
            BeyondAPI.getBeyondPlayerData(player).getPlayerRogueData().setPhase(PlayerPhase.LOBBY);
            BeyondAPI.syncPlayerData(player);
        }
    }
}
```

Remove imports that become unused.

- [ ] **Step 2: Update `ProgressStartCap.tryLeaveSafeZone`**

Change the initial participation guard:

```java
if (ctxPhase(player) != PlayerPhase.LOBBY) return Step.NOT_IN_ROGUE;
```

or equivalent local helper:

```java
private static PlayerPhase ctxPhase(ServerPlayer player) {
    return BeyondAPI.getBeyondPlayerData(player).getPlayerRogueData().getPhase();
}
```

After checking progress, replace list add/sync with:

```java
BeyondAPI.getBeyondPlayerData(player).getPlayerRogueData().setPhase(PlayerPhase.PREPARE_ROGUE);
BeyondAPI.syncPlayerData(player);
```

- [ ] **Step 3: Update `ProgressStartCap.tryOpenLootBag`**

Replace the list membership check with:

```java
PlayerPhase playerPhase = ctx.getPlayerPhase(player);
if (playerPhase == PlayerPhase.LOBBY) return Step.NOT_IN_ROGUE;
```

Add a guard so only `PREPARE_ROGUE` can open the bag:

```java
if (playerPhase != PlayerPhase.PREPARE_ROGUE) {
    if (playerPhase == PlayerPhase.PRE_ROGUE) {
        player.sendSystemMessage(Component.translatable("beyond.rogue.already_opened"));
        return Step.ALREADY_OPENED;
    }
    if (playerPhase == PlayerPhase.ON_PROGRESS) {
        player.sendSystemMessage(Component.translatable("beyond.rogue.game_in_progress"));
        return Step.GAME_IN_PROGRESS;
    }
    player.sendSystemMessage(Component.translatable("beyond.rogue.game_already_started"));
    return Step.GAME_ALREADY_STARTED;
}
```

Keep the existing transition to `PRE_ROGUE` after giving equipment.

- [ ] **Step 4: Update `ProgressStartCap.returnToSafeZone`**

Remove calls to `rogueData.removeRoguePlayer`.

Use:

```java
ctx.setPlayerPhase(player, PlayerPhase.LOBBY);

if (ctx.playersInRogue(level).isEmpty()) {
    rogueData.setProgressActive(false);
    BeyondAPI.syncGlobalData(level);
    ctx.setPhase(level, RoguePhase.LOBBY);
}
```

- [ ] **Step 5: Update local `isInRogue` helpers**

In `ProgressStartCap` and `NodeCap`, replace UUID-list checks with:

```java
private static boolean isInRogue(ServerPlayer player) {
    return BeyondAPI.getBeyondPlayerData(player).getPlayerRogueData().getPhase() != PlayerPhase.LOBBY;
}
```

- [ ] **Step 6: Update settlement cleanup**

In `PlayerProgressFinishCap.teleportAllToSafeZone`, remove `rogueData.removeRoguePlayer(p.getUUID())`.

The existing caller already sets all players to `LOBBY` after teleport succeeds.

- [ ] **Step 7: Update reward event**

Change `RewardEventType.cast` to use `RogueContext`:

```java
IRogueContext ctx = new RogueContext();
for (ServerPlayer player : ctx.playersInRogue(level)) {
    RoguePlayerManager.giveItem(player, Items.IRON_INGOT);
}
```

Add imports:

```java
import net.minecraft.server.level.ServerPlayer;
import org.galaxy.beyond.api.system.rogue.IRogueContext;
import org.galaxy.beyond.api.system.rogue.RogueContext;
```

Remove the UUID set code.

- [ ] **Step 8: Run IDEA inspections**

Use IDEA MCP `get_file_problems` on all five files.

Expected: no Java errors.

## Task 5: Reuse Node Unlock Flow

**Files:**
- Modify: `Beyond/src/main/java/org/galaxy/beyond/api/system/rogue/core/RogueEncounterRunner.java`

- [ ] **Step 1: Expose unlock behavior**

Rename:

```java
private void unlockNode()
```

to:

```java
public void forceUnlockNode()
```

Update the existing caller in `advanceEvent`:

```java
forceUnlockNode();
return true;
```

Keep the method body unchanged.

- [ ] **Step 2: Run IDEA inspection**

Use IDEA MCP `get_file_problems` on `RogueEncounterRunner.java`.

Expected: no Java errors.

## Task 6: Replace List Commands With Debug Commands

**Files:**
- Modify: `Beyond/src/main/java/org/galaxy/beyond/comand/BeyondCommand.java`

- [ ] **Step 1: Add imports**

Use these imports as needed:

```java
import net.minecraft.world.level.ChunkPos;
import org.galaxy.beyond.api.system.rogue.RogueContext;
import org.galaxy.beyond.api.system.rogue.RogueNodeData;
import org.galaxy.beyond.api.system.rogue.core.NodePhase;
import org.galaxy.beyond.api.system.rogue.core.PlayerPhase;
import org.galaxy.beyond.api.system.rogue.core.RogueEncounterRunner;
```

Remove no-longer-used `RogueData`, `Set`, and `UUID` imports if they only served list commands.

- [ ] **Step 2: Remove old list command blocks**

Delete the entire `/beyond config playerList` and `/beyond config safeZoneList` blocks.

Keep `/beyond config currentProgress`.

- [ ] **Step 3: Add `/beyond playerPhase get <player>`**

Add a top-level child under `/beyond`:

```java
.then(Commands.literal("playerPhase")
        .then(Commands.literal("get")
                .then(Commands.argument("player", StringArgumentType.word())
                        .suggests((ctx, builder) -> suggestOnlinePlayers(ctx.getSource(), builder))
                        .executes(ctx -> {
                            String name = StringArgumentType.getString(ctx, "player");
                            var player = ctx.getSource().getServer().getPlayerList().getPlayerByName(name);
                            if (player == null) {
                                ctx.getSource().sendFailure(Component.translatable("commands.beyond.playerPhase.not_found", name));
                                return 0;
                            }
                            PlayerPhase phase = BeyondAPI.getBeyondPlayerData(player).getPlayerRogueData().getPhase();
                            ctx.getSource().sendSuccess(
                                    () -> Component.translatable("commands.beyond.playerPhase.get", name, phase.name()),
                                    false);
                            return 1;
                        })))
```

- [ ] **Step 4: Add `/beyond playerPhase set <player> <phase>`**

Inside the same `playerPhase` block, add:

```java
.then(Commands.literal("set")
        .then(Commands.argument("player", StringArgumentType.word())
                .suggests((ctx, builder) -> suggestOnlinePlayers(ctx.getSource(), builder))
                .then(Commands.argument("phase", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            for (PlayerPhase phase : PlayerPhase.values()) {
                                builder.suggest(phase.name());
                            }
                            return builder.buildFuture();
                        })
                        .executes(ctx -> {
                            String name = StringArgumentType.getString(ctx, "player");
                            var player = ctx.getSource().getServer().getPlayerList().getPlayerByName(name);
                            if (player == null) {
                                ctx.getSource().sendFailure(Component.translatable("commands.beyond.playerPhase.not_found", name));
                                return 0;
                            }
                            PlayerPhase phase = parsePlayerPhase(StringArgumentType.getString(ctx, "phase"));
                            if (phase == null) {
                                ctx.getSource().sendFailure(Component.translatable("commands.beyond.playerPhase.invalid", StringArgumentType.getString(ctx, "phase")));
                                return 0;
                            }
                            BeyondAPI.getBeyondPlayerData(player).getPlayerRogueData().setPhase(phase);
                            BeyondAPI.syncPlayerData(player);
                            ctx.getSource().sendSuccess(
                                    () -> Component.translatable("commands.beyond.playerPhase.set", name, phase.name()),
                                    true);
                            return 1;
                        }))))
```

- [ ] **Step 5: Add `/beyond unlockCurrentNode`**

Add another top-level child under `/beyond`:

```java
.then(Commands.literal("unlockCurrentNode")
        .executes(ctx -> {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            ServerLevel level = (ServerLevel) player.level();
            var nodeData = BeyondAPI.findNodeData(level, new ChunkPos(player.blockPosition()));
            if (nodeData == null) {
                ctx.getSource().sendFailure(Component.translatable("commands.beyond.unlockCurrentNode.not_in_node"));
                return 0;
            }
            if (nodeData.getPhase() == NodePhase.UNLOCKED) {
                ctx.getSource().sendFailure(Component.translatable("commands.beyond.unlockCurrentNode.already_unlocked"));
                return 0;
            }

            RogueNodeData rogueNodeData = new RogueNodeData();
            rogueNodeData.setNodeData(nodeData);
            rogueNodeData.setNodeChunk(new ChunkPos(player.blockPosition()));
            BeyondAPI.getRogueData(level).setRogueNodeData(rogueNodeData);

            new RogueEncounterRunner(level, new RogueContext(), rogueNodeData).forceUnlockNode();
            ctx.getSource().sendSuccess(
                    () -> Component.translatable("commands.beyond.unlockCurrentNode.success"),
                    true);
            return 1;
        }))
```

- [ ] **Step 6: Add helper methods**

Add below `register`:

```java
private static CompletableFuture<Suggestions> suggestOnlinePlayers(CommandSourceStack source, SuggestionsBuilder builder) {
    for (var player : source.getServer().getPlayerList().getPlayers()) {
        builder.suggest(player.getName().getString());
    }
    return builder.buildFuture();
}

private static PlayerPhase parsePlayerPhase(String value) {
    for (PlayerPhase phase : PlayerPhase.values()) {
        if (phase.name().equalsIgnoreCase(value)) return phase;
    }
    return null;
}
```

Add imports:

```java
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;
```

- [ ] **Step 7: Run IDEA inspection**

Use IDEA MCP `get_file_problems` on `BeyondCommand.java`.

Expected: no Java errors.

## Task 7: Update Translations

**Files:**
- Modify: `Beyond/src/main/resources/assets/beyond/lang/en_us.json`
- Modify: `Beyond/src/main/resources/assets/beyond/lang/zh_cn.json`

- [ ] **Step 1: Remove old list command keys**

Remove keys starting with:

```text
commands.beyond.config.playerList.
commands.beyond.config.safeZoneList.
```

- [ ] **Step 2: Add new English keys**

Add:

```json
"commands.beyond.playerPhase.not_found": "Player %s not found",
"commands.beyond.playerPhase.invalid": "Invalid player phase: %s",
"commands.beyond.playerPhase.get": "%s player phase: %s",
"commands.beyond.playerPhase.set": "Set %s player phase to %s",
"commands.beyond.unlockCurrentNode.not_in_node": "You are not inside a node",
"commands.beyond.unlockCurrentNode.already_unlocked": "Current node is already unlocked",
"commands.beyond.unlockCurrentNode.success": "Unlocked current node"
```

- [ ] **Step 3: Add new Chinese keys**

Add:

```json
"commands.beyond.playerPhase.not_found": "找不到玩家 %s",
"commands.beyond.playerPhase.invalid": "无效的玩家阶段：%s",
"commands.beyond.playerPhase.get": "%s 当前玩家阶段：%s",
"commands.beyond.playerPhase.set": "已将 %s 的玩家阶段设置为 %s",
"commands.beyond.unlockCurrentNode.not_in_node": "你不在节点范围内",
"commands.beyond.unlockCurrentNode.already_unlocked": "当前节点已解锁",
"commands.beyond.unlockCurrentNode.success": "已解锁当前节点"
```

- [ ] **Step 4: Validate JSON by build**

No separate JSON parser is required. The Gradle build in Task 9 must pass.

## Task 8: Global Reference Cleanup

**Files:**
- Whole project search

- [ ] **Step 1: Search removed APIs**

Run IDEA MCP search or `rg` for:

```text
getRoguePlayerIds
addRoguePlayer
removeRoguePlayer
getSafeZonePlayerIds
addSafeZonePlayer
removeSafeZonePlayer
roguePlayerIdStrings
safeZonePlayerIdStrings
```

Expected: no source references remain.

- [ ] **Step 2: Search old command translation keys**

Search:

```text
commands.beyond.config.playerList
commands.beyond.config.safeZoneList
```

Expected: no source or lang references remain.

## Task 9: Verification

**Files:**
- All touched Java files

- [ ] **Step 1: Run IDEA inspections**

Use IDEA MCP `get_file_problems` on each touched Java file:

```text
PlayerPhase.java
RogueData.java
RogueContext.java
PlayerInGameCap.java
ProgressStartCap.java
NodeCap.java
PlayerProgressFinishCap.java
RogueEncounterRunner.java
RewardEventType.java
BeyondCommand.java
```

Expected: no Java errors.

- [ ] **Step 2: Build project**

Run Gradle build for the relevant project:

```bash
./gradlew :Beyond:build
```

Expected: build succeeds.

- [ ] **Step 3: Report residual risks**

Mention that manual Minecraft testing should cover:

- leaving safe zone gives loot bag and sets `PREPARE_ROGUE`
- opening loot bag moves to `PRE_ROGUE`
- all ready starts progress
- node interactions still require active player phase
- `/beyond playerPhase get/set`
- `/beyond unlockCurrentNode` inside and outside node chunks
