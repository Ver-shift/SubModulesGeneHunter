# 肉鸽系统状态机设计

## 1. 三层状态机架构

系统采用三层独立但协同的状态机：

```
┌─────────────────────────────────────────────────────┐
│ RogueState (全局)    │ 控制整局游戏的宏观阶段          │
├─────────────────────────────────────────────────────┤
│ PlayerRogueState (玩家) │ 控制单个玩家的微观状态       │
├─────────────────────────────────────────────────────┤
│ NodeState (节点)        │ 控制单个节点的生命周期       │
└─────────────────────────────────────────────────────┘
```

三者**不直接互相修改**，而是通过 `tick` 总线检测条件后自动推进，业务逻辑通过 **NeoForge Event** 发布供外部扩展。

---

## 2. PlayerRogueState —— 玩家状态机（核心）

### 2.1 状态定义

| 状态 | 含义 | 玩家所处位置 |
|------|------|-------------|
| `IN_SAFE_ZONE` | 玩家在安全区内 | 安全区 |
| `READY_ROGUE` | 跨出边界，等待所有玩家ready | 大世界 |
| `ON_ROGUE` | 在大世界自由活动，未进入节点 | 大世界 |
| `IN_NODE` | 进入节点区域，未触发事件 | 节点区域 |
| `READY_NODE` | 右键节点方块，等待所有玩家ready启动节点 | 节点区域 |
| `IN_NODE_EVENT` | 节点事件执行中 | 节点区域 |
| `READY_NEXT` | 当前事件完成，等待所有玩家ready触发下一个事件 | 节点区域 |
| `DEAD` | 死亡，消耗复活次数 | 任意 |
| `SPECTATING` | 复活次数归零，观战等待结算 | 任意 |
| `EMPTY` | 过渡态，由tick自动确定下一状态 | 任意 |

### 2.2 不需要 IN_NODE_FINISH 的原因

`EventTask` 内部是一个有序的 `List<RogueEventType>`，最后一个事件设计为"领取奖励事件"。当 `eventIndex` 走到末尾，整个 `EventTask` 完成，玩家从 `IN_NODE_EVENT` 直接回到 `ON_ROGUE`。不需要额外的中间状态。

### 2.3 状态迁移图

```
                    ┌─ 跨出安全区边界 ──→ READY_ROGUE
                    │      (changeZone)        │
                    │                   所有玩家都READY
                    │                          ↓
                    │                      ON_ROGUE ←──────────────────────────────┐
                    │                          │                                   │
                    │                   踏入节点区域                                │
                    │                   (changeZone)                               │
                    │                          ↓                                   │
                    │                       IN_NODE                                │
                    │                          │                                   │
                    │                  右键节点方块                                 │
                    │                          ↓                                   │
                    │                      READY_NODE                              │
                    │                          │                                   │
                    │                   所有玩家都READY                             │
                    │                          ↓                                   │
                    │                    IN_NODE_EVENT ──────┐                     │
                    │                          │             │                     │
                    │                   单个事件完成          │                     │
                    │                   (eventIndex++)       │                     │
                    │                          ↓             │                     │
                    │                      READY_NEXT ────→  │                     │
                    │                          │             │ 还有下一事件         │
                    │                   所有玩家都READY       │ (eventIndex < size)  │
                    │                          │             │                     │
                    │                   触发下一个事件 ───────┘                     │
                    │                          │                                   │
                    │                 EventTask全部完成(eventIndex==size)           │
                    │                          └───────────────────────────────────┘
                    │
    IN_SAFE_ZONE ←─┘

    死亡线路：
    ON_ROGUE / IN_NODE_EVENT ── 死亡 ──→ DEAD
                                             │
                                     有复活次数 → ON_ROGUE
                                     无复活次数 → SPECTATING → (结算) → IN_SAFE_ZONE
```

### 2.4 状态分组方法

```java
public boolean isInSafeZone()   { return this == IN_SAFE_ZONE; }

public boolean isInGame()       { return this == READY_ROGUE || this == ON_ROGUE || isInNode(); }

public boolean isInNode()       { return this == IN_NODE || this == READY_NODE
                                       || this == IN_NODE_EVENT || this == READY_NEXT; }

public boolean isDead()         { return this == DEAD || this == SPECTATING; }
```

### 2.5 状态转换合法性

```java
public boolean canTransitionTo(PlayerRogueState target) {
    return switch (this) {
        case IN_SAFE_ZONE  -> target == READY_ROGUE || target == ON_ROGUE;
        case READY_ROGUE   -> target == ON_ROGUE || target == IN_SAFE_ZONE;
        case ON_ROGUE      -> target == IN_NODE || target == DEAD || target == IN_SAFE_ZONE;
        case IN_NODE       -> target == READY_NODE || target == ON_ROGUE || target == DEAD;
        case READY_NODE    -> target == IN_NODE_EVENT || target == ON_ROGUE;
        case IN_NODE_EVENT -> target == READY_NEXT   // 事件完成，等待下一事件
                           || target == ON_ROGUE     // EventTask全部完成
                           || target == DEAD;        // 事件中死亡
        case READY_NEXT    -> target == IN_NODE_EVENT // 全部ready，触发下一事件
                           || target == ON_ROGUE;     // 玩家放弃节点
        case DEAD          -> target == ON_ROGUE || target == SPECTATING;
        case SPECTATING    -> target == IN_SAFE_ZONE;
        case EMPTY         -> true; // 过渡态，任何目标都允许
    };
}
```

---

## 3. RogueState —— 全局状态机

### 3.1 状态定义

| 状态 | 含义 | 触发条件 |
|------|------|----------|
| `LOBBY` | 局外养成，所有玩家在安全区 | 游戏初始 / 所有玩家返回安全区 |
| `READY` | 至少一个玩家 ready | 任一玩家变为 READY_ROGUE |
| `IN_PROGRESS` | 游戏运行中，所有玩家在大世界 | 所有玩家不再是 READY_ROGUE |
| `IN_NODE` | 至少一个玩家在节点内 | 任一玩家进入 IN_NODE 系状态 |
| `POST_GAME` | Boss被击败，进入结算 | Boss事件完成 |

### 3.2 与 PlayerRogueState 的协同关系

```
RogueState.LOBBY       ← 所有玩家 IN_SAFE_ZONE
RogueState.READY       ← 任一玩家 READY_ROGUE
RogueState.IN_PROGRESS ← 所有玩家 ON_ROGUE（没有READY_ROGUE了）
RogueState.IN_NODE     ← 任一玩家 isInNode() == true
RogueState.POST_GAME   ← Boss死亡，结算开始
```

> **关键模式**："所有玩家都ready才推进"。`READY_ROGUE`、`READY_NODE`、`READY_NEXT` 都需要等待 `isPlayerAllReady()` 返回 true，由 `tick` 自动检测。
>
> **节点内多事件循环**：`IN_NODE_EVENT → READY_NEXT → IN_NODE_EVENT → ...` 直到 `eventIndex == events.size()`，最后的事件是领取奖励事件，完成后直接回到 `ON_ROGUE`。

---

## 4. 响应式状态推进：tick 总线

### 4.1 核心原则

`tick` **只做条件检测和状态迁移，不执行具体业务逻辑**。业务逻辑由 NeoForge Event 承载。

```
tick(level) {
    1. 遍历所有在线玩家 → playerTick(player)
       ├─ 检测坐标 → 判断 ZoneType 变化 → 自动更新 PlayerRogueState
       └─ 检测死亡/复活 → 自动更新 PlayerRogueState
    2. autoAdvanceRogueState(level)
       └─ 根据所有玩家状态 → 自动推进 RogueState
    3. 发布 RogueTickEvent（可选，供外部监听）
}
```

### 4.2 playerTick 内部检测链

```
playerTick(player) {
    zoneManager.playerTick(player)   // ZoneCapType 链自动调用 changeZone

    → changeZone(player, from, to) 被触发时：
      from=Safe_Zone, to≠Safe_Zone → IN_SAFE_ZONE → READY_ROGUE
      from≠Safe_Zone, to=Safe_Zone → 任意状态 → IN_SAFE_ZONE (触发回城逻辑)
      from≠Node_Zone, to=Node_Zone → ON_ROGUE → IN_NODE
      from=Node_Zone, to≠Node_Zone → IN_NODE系 → ON_ROGUE

    → 死亡检测：
      玩家死亡 + 复活次数>0  → DEAD → (倒计时结束) → ON_ROGUE
      玩家死亡 + 复活次数=0  → DEAD → SPECTATING → (结算) → IN_SAFE_ZONE

    → Ready检测：
      isPlayerAllReady(READY_ROGUE) → READY_ROGUE → ON_ROGUE (全部玩家)
      isPlayerAllReady(READY_NODE)  → READY_NODE → IN_NODE_EVENT (全部玩家，触发第一个事件)
      isPlayerAllReady(READY_NEXT)  → READY_NEXT → IN_NODE_EVENT (全部玩家，触发下一个事件)
}
```

### 4.3 边界跨越检测：IZoneCapEvent.changeZone

安全区边界跨越不由 `PlayerRogueState` 自己检测，而是复用已有的 `ZoneCapType` 体系：

```java
// ZoneCapType 已经实现了 IZoneCapEvent
// ZoneManager.playerTick 会检测玩家坐标 → 判断 ZoneType 变化 → 调用 changeZone

// 在 RogueManager 或专门的 ZoneCap 实现中：
@Override
public void changeZone(ServerPlayer player, ZoneType from, Context context) {
    PlayerRogueState currentState = BeyondAPI.getBeyondPlayerData(player)
        .getPlayerRogueData().getState();

    // 离开安全区 → 进入大世界
    if (from == ZoneType.Safe_Zone && context.zoneType() != ZoneType.Safe_Zone) {
        // 发布事件 → 发放宝藏袋 → 改变状态为 READY_ROGUE
        NeoForge.EVENT_BUS.post(new PlayerRogueStateEvent(player, currentState, PlayerRogueState.READY_ROGUE));
    }

    // 进入安全区 → 结算并回城
    if (from != ZoneType.Safe_Zone && context.zoneType() == ZoneType.Safe_Zone) {
        NeoForge.EVENT_BUS.post(new PlayerRogueStateEvent(player, currentState, PlayerRogueState.IN_SAFE_ZONE));
    }
}
```

---

## 5. 扩展机制：NeoForge Event

### 5.1 状态变更事件

替代已删除的 `IRogueListener`，使用 NeoForge Event 通知状态变更：

```java
/**
 * 玩家肉鸽状态变更事件。在 PlayerRogueState 发生改变时发布到 EVENT_BUS。
 * 外部模组通过 @SubscribeEvent 监听，实现逻辑扩展。
 */
public class PlayerRogueStateChangeEvent extends Event {
    private final ServerPlayer player;
    private final PlayerRogueState oldState;
    private final PlayerRogueState newState;

    // getters...
}
```

### 5.2 需要新增的 Event

| 事件 | 发布时机 | 用途 |
|------|----------|------|
| `PlayerRogueStateChangeEvent` | 玩家状态变更时 | 通用扩展点 |
| `RogueStateChangeEvent` | 全局状态变更时 | 全局逻辑扩展 |
| `RogueStartEvent`（已有） | 游戏正式启动 | 外部监听 |
| `ResolveEvent`（已有） | 定义→运行时数据转化 | 注入随机性 |

### 5.3 外部扩展方式

```java
// 示例：外部模组监听状态变更，实现策划 1.3.1.1（发放宝藏袋）
@SubscribeEvent
public void onPlayerLeaveSafeZone(PlayerRogueStateChangeEvent event) {
    if (event.getOldState() == PlayerRogueState.IN_SAFE_ZONE
        && event.getNewState() == PlayerRogueState.READY_ROGUE) {
        // 发放宝藏袋
        giveTreasureBag(event.getPlayer());
    }
}
```

---

## 6. 需要补充的接口和方法

### 6.1 IRogueManager 补充

```java
public interface IRogueManager {
    void setRogueLevel(ResourceKey<Level> level);
    void tick(ServerLevel level);

    // 状态自动推进
    void autoAdvanceRogueState(ServerLevel level);
    void autoAdvancePlayerState(ServerPlayer player);

    // Ready检查
    void tryStartRogue(ServerLevel level);
    void tryStartNode(ServerLevel level);
    boolean isPlayerAllReady(ServerLevel level, PlayerRogueState state);

    // 死亡/复活
    void onPlayerDeath(ServerPlayer player);
    void onPlayerRespawn(ServerPlayer player);

    // 结算
    void tryFinishRogue(ServerLevel level);
}
```

### 6.2 IPlayerRougeManager 补充

```java
public interface IPlayerRougeManager {
    void tick(ServerPlayer player);
    void intoRogue(ServerPlayer player);
    void leaveRogue(ServerPlayer player);
    void intoProgress(ServerPlayer player);

    // 新增
    void setState(ServerPlayer player, PlayerRogueState newState);
    PlayerRogueState getState(ServerPlayer player);
}
```

### 6.3 PlayerRogueData 补充

```java
@Data
public class PlayerRogueData {
    private PlayerRogueState state = PlayerRogueState.IN_SAFE_ZONE;
    private int lifeCount;      // 当前剩余复活次数
    private int maxLifeCount;   // 最大复活次数（属性驱动）
    private int deathCount;     // 累计死亡次数
    private long readyTimestamp; // ready的时间戳（用于超时检测等）
}
```

### 6.4 RogueData 补充

`ProgressType` 就是一个副本，玩家一次只在一个副本里冒险。进度游标 `scenesIndex` 已经在 `ProgressType` 内部，`RogueData` 不需要重复维护索引。

```java
@Data
public class RogueData {
    private List<ServerPlayer> inGamePlayers;
    private RogueState rogueState = RogueState.LOBBY;
    private RogueNodeData rogueNodeData;
    private ProgressType progressType;  // 当前副本（一次冒险只有一个）
    private long gameSeed;              // 本局种子（策划 2.3.1.4.a）
}
```

---

## 7. 待实现清单

### P0 —— 状态枚举完善

- [ ] `PlayerRogueState` 补充 `IN_NODE_EVENT`、`READY_NEXT`、`SPECTATING`
- [ ] `PlayerRogueState` 添加 `isInSafeZone()`、`isInGame()`、`isInNode()`、`isDead()`
- [ ] `PlayerRogueState` 添加 `canTransitionTo(PlayerRogueState)`

### P1 —— tick 响应式检测

- [ ] `RogueManager.tick(ServerLevel)` 实现状态检测链
- [ ] `RogueManager.autoAdvanceRogueState()` 根据所有玩家状态推进全局状态
- [ ] 实现 `changeZone` 回调驱动玩家状态变更
- [ ] 实现死亡→观战→结算→回城的完整链路

### P2 —— Event 扩展体系

- [ ] 新增 `PlayerRogueStateChangeEvent`
- [ ] 新增 `RogueStateChangeEvent`
- [ ] 在状态变更点发布 Event

### P3 —— 数据完善

- [ ] `PlayerRogueData` 补充 `maxLifeCount`、`readyTimestamp`
- [ ] `RogueData` 补充 `gameSeed`
- [ ] `RogueManager` 实现 `onPlayerDeath`、`onPlayerRespawn`
