# GeneHunter 与 Beyond 长期解耦整理计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 分阶段降低 `GeneHunter` 与 `Beyond` 当前肉鸽刷怪、Gateway、奖励选择链路的耦合度，让后续事件、奖励和生成载体扩展时不再把逻辑堆进大类。

**Architecture:** `Beyond` 保持框架层，只描述节点事件、刷怪定义、刷怪计划和通用流程；`GeneHunter` 保持玩法层，负责 Gateway 载体、基因/武器奖励和具体数据定义。整理以“先拆 GeneHunter 高风险堆叠类，再抽 Beyond 编排服务”为顺序，每一步都保持行为不变并提交一个安全点。

**Tech Stack:** Minecraft NeoForge 1.21.1、Java、Gradle、LowDragLib2、Gateways to Eternity、现有 `Beyond`/`GeneHunter` 数据与 attachment 同步系统。

---

## 当前读码结论

核心链路：

1. `Beyond` 的 `RogueEncounterRunner` 负责节点状态推进。
2. Runner 根据节点颜色和场景构造 `SpawnContext`。
3. `EncounterSpawnPlanner` 使用 `SpawnDefinition`、`SpawnPack`、`Character` 生成 `SpawnPlan`。
4. `GeneHunter` 的 `SpawnEvent` 取出 `SpawnSessionData` 并调用 `Character.placeSpawn`。
5. `GeneHunterSpawnCharacter` 将 `SpawnPlan` 转为动态 Gateway，并生成 Gateway 实体。
6. Gateway 完成后触发 `ChoiceReward`，进入基因/武器 choice 奖励。
7. `ChoiceManager` 负责抽取、刷新、扣经验、领取和阶段推进。
8. `ChoiceContainer` 构建 UI，并目前还持有普通/首领奖励编排入口。

主要耦合点：

1. `RogueEncounterRunner` 职责过宽：事件推进、广播、同步、刷怪计划缓存、boss 判定集中在一个类。
2. `Character` 抽象混合了“难度/预算计算”和“具体生成执行”两类职责。
3. `GeneHunterSpawnCharacter` 同时做 Gateway 构建、动态注册、实体放置和活动实体检测。
4. `ChoiceManager` 同时处理 UI 打开、RPC、roll request 构建、阶段推进、刷新费用和领取。
5. `ChoiceContainer` 同时处理 UI 构建和奖励方案选择。
6. `ZombieExampleSpawnDefinition` 内联了普通包、Boss 包和怪物血量模板，后续会变成巨大配置类。
7. `Beyond` 的 `SpawnDefinition.gateway` 字段仍带有具体 Gateway 概念，应改成通用语义或删除。

## 整理原则

- 每次只移动一个职责，不同时改行为和平衡数值。
- 先新增小服务/工厂，再切换调用方，最后删除旧入口。
- `Beyond` 不引用 `GeneHunter`，只暴露通用接口和数据模型。
- `GeneHunter` 可以依赖 `Beyond`，但具体玩法规则不要写进 UI 类。
- 数据定义类只描述组合，不承载复杂计算规则。
- 每个阶段完成后运行 IDEA MCP 错误检查和 Gradle 编译；游戏内验证由你执行。

## 阶段 0：保存当前可编译状态

**Files:**
- Add: `docs/superpowers/plans/2026-06-13-genehunter-beyond-refactor-plan.md`
- Include existing worktree changes under `Beyond/` and `GeneHunter/`

- [ ] **Step 1: 检查未跟踪和被忽略文件**

Run:

```bash
git status --short
git ls-files --others --exclude-standard
git status --ignored --short
```

Expected:

```text
列出所有当前已修改、未跟踪、被忽略文件；确认 docs/ 被 .git/info/exclude 忽略，新文档需要 git add -f。
```

- [ ] **Step 2: 编译当前安全点**

Run:

```bash
./gradlew compileJava
```

或使用 IDEA MCP `build_project`。

Expected:

```text
BUILD SUCCESSFUL 或 IDEA MCP 返回 isSuccess=true。
```

- [ ] **Step 3: 提交当前安全点**

Run:

```bash
git add Beyond GeneHunter
git add -f docs/superpowers/plans/2026-06-13-genehunter-beyond-refactor-plan.md
git status --short
git commit -m "保存：整理前的刷怪与奖励安全点"
```

Expected:

```text
提交包含当前 Beyond/GeneHunter 代码、生成资源、mixins、动态 Gateway、choice 奖励和本计划文档。
```

## 阶段 1：拆分 GeneHunter Choice 奖励链路

**Files:**
- Create: `GeneHunter/src/main/java/org/galaxy/gene_hunter/api/system/choice/ChoiceRollFactory.java`
- Create: `GeneHunter/src/main/java/org/galaxy/gene_hunter/api/system/choice/ChoiceExperienceCost.java`
- Create: `GeneHunter/src/main/java/org/galaxy/gene_hunter/api/system/choice/ChoiceRewardScheme.java`
- Create: `GeneHunter/src/main/java/org/galaxy/gene_hunter/api/system/choice/GeneHunterChoiceRewards.java`
- Modify: `GeneHunter/src/main/java/org/galaxy/gene_hunter/api/system/choice/ChoiceManager.java`
- Modify: `GeneHunter/src/main/java/org/galaxy/gene_hunter/container/ChoiceContainer.java`
- Modify: `GeneHunter/src/main/java/org/galaxy/gene_hunter/gateway/ChoiceReward.java`

- [ ] **Step 1: 抽出 roll request 构建**

Move `ChoiceManager.createChoiceRequest(...)` into `ChoiceRollFactory`.

Target API:

```java
public final class ChoiceRollFactory {

    public LootManager.Request create(ServerPlayer player, ChoiceStage stage) {
        // 保留当前 weapon/xene 表选择、NodeColor 权重和 fixedRolls 逻辑。
    }
}
```

Expected:

```text
ChoiceManager 不再知道 weapon/xene 具体表列表，只调用 ChoiceRollFactory。
```

- [ ] **Step 2: 抽出经验费用策略**

Rename or replace `ChoiceRefreshCost` with `ChoiceExperienceCost`.

Target API:

```java
public final class ChoiceExperienceCost {

    public int nextCost(LootManager.Request request, int refreshTimes) {
        // 当前规则：表基础成本最大值 * (refreshTimes + 1)。
    }

    public boolean consume(ServerPlayer player, int points) {
        // 扣原版经验点数，不扣等级。
    }
}
```

Expected:

```text
ChoiceManager 不直接访问 player.totalExperience，也不直接维护表成本表。
```

- [ ] **Step 3: 抽出奖励方案**

Move `ChoiceContainer.rogueRewardEvent` 和 `ChoiceContainer.bossRewardEvent` to `GeneHunterChoiceRewards`.

Target API:

```java
public final class GeneHunterChoiceRewards {

    public static void normal(ServerPlayer player, NodeColor nodeColor) {
        GeneHunterAPI.getChoiceManager(player).startStages(List.of(
                ChoiceStage.of(BiotechLootTypeInit.XENE_TRAIT_LOOT_TYPE.get(), nodeColor)
        ));
    }

    public static void boss(ServerPlayer player, NodeColor nodeColor) {
        GeneHunterAPI.getChoiceManager(player).startStages(List.of(
                ChoiceStage.fixed(BiotechLootTypeInit.XENE_TRAIT_LOOT_TYPE.get(), nodeColor, 3),
                ChoiceStage.of(GeneHunterLootInit.WEAPON_LOOT_TYPE.get(), nodeColor)
        ));
    }
}
```

Expected:

```text
ChoiceContainer 只负责 UI；ChoiceReward 只调用 GeneHunterChoiceRewards。
```

- [ ] **Step 4: 清理 ChoiceManager**

Keep only these responsibilities in `ChoiceManager`:

```text
打开 UI、开始阶段、执行当前阶段 roll、刷新当前阶段、领取当前槽位、进入下一阶段。
```

Expected:

```text
ChoiceManager 中没有具体 loot table id、没有经验成本表、没有普通/首领奖励编排。
```

- [ ] **Step 5: 验证并提交**

Run:

```bash
./gradlew compileJava
```

Commit:

```bash
git add GeneHunter/src/main/java/org/galaxy/gene_hunter/api/system/choice GeneHunter/src/main/java/org/galaxy/gene_hunter/container/ChoiceContainer.java GeneHunter/src/main/java/org/galaxy/gene_hunter/gateway/ChoiceReward.java
git commit -m "整理：拆分选择奖励职责"
```

## 阶段 2：拆分 GeneHunter Gateway 生成载体

**Files:**
- Create: `GeneHunter/src/main/java/org/galaxy/gene_hunter/gateway/GatewaySpawnPlacer.java`
- Create: `GeneHunter/src/main/java/org/galaxy/gene_hunter/gateway/GatewaySpawnTokenFactory.java`
- Modify: `GeneHunter/src/main/java/org/galaxy/gene_hunter/character/GeneHunterSpawnCharacter.java`
- Modify: `GeneHunter/src/main/java/org/galaxy/gene_hunter/gateway/GeneHunterGatewayFactory.java`
- Modify: `GeneHunter/src/main/java/org/galaxy/gene_hunter/gateway/GeneHunterGatewayBinder.java`

- [ ] **Step 1: 抽出 Gateway token 创建**

Target API:

```java
public final class GatewaySpawnTokenFactory {

    public ItemStack create(SpawnDefinition definition, SpawnPlan plan) {
        Gateway gateway = gatewayFactory.create(definition, plan);
        ResourceLocation id = GeneHunterGatewayBinder.runtimeId(definition.getId(), gateway);
        DynamicHolder<Gateway> holder = GeneHunterGatewayBinder.bind(id, gateway, null);
        ItemStack stack = new ItemStack(GatewayObjects.GATE_PEARL);
        GatePearlItem.setGate(stack, holder);
        return stack;
    }
}
```

Expected:

```text
GeneHunterSpawnCharacter.createSpawnToken 只委托 GatewaySpawnTokenFactory。
```

- [ ] **Step 2: 抽出 Gateway 实体放置**

Target API:

```java
public final class GatewaySpawnPlacer {

    public GatewayEntity place(SpawnDefinition definition, SpawnPlan plan, ServerLevel level, BlockPos nodePos, List<ServerPlayer> players) {
        // 保留 NormalGatewayEntity、SPAWN_TAG、BOSS_TAG、RogueSpawnHelper.gatewayPos 逻辑。
    }

    public boolean hasActive(ServerLevel level, BlockPos nodePos) {
        // 保留 96 格检测范围和 SPAWN_TAG。
    }
}
```

Expected:

```text
GeneHunterSpawnCharacter 只作为 Beyond Character 适配器，不直接处理 GatewayEntity 细节。
```

- [ ] **Step 3: 验证并提交**

Run:

```bash
./gradlew compileJava
```

Commit:

```bash
git add GeneHunter/src/main/java/org/galaxy/gene_hunter/character GeneHunter/src/main/java/org/galaxy/gene_hunter/gateway
git commit -m "整理：拆分网关生成载体职责"
```

## 阶段 3：整理 GeneHunter 刷怪数据定义

**Files:**
- Create: `GeneHunter/src/main/java/org/galaxy/gene_hunter/data/spawn/GeneHunterSpawnEntries.java`
- Create: `GeneHunter/src/main/java/org/galaxy/gene_hunter/data/spawn/GeneHunterBossEntries.java`
- Modify: `GeneHunter/src/main/java/org/galaxy/gene_hunter/data/spawn/ZombieExampleSpawnDefinition.java`

- [ ] **Step 1: 抽出普通怪物条目模板**

Target API:

```java
public final class GeneHunterSpawnEntries {

    public static SpawnEntry entry(EntityType<?> entity, int count) {
        return SpawnEntry.builder()
                .entity(EntityType.getKey(entity))
                .count(count)
                .health(health(entity))
                .build();
    }

    private static float health(EntityType<?> entity) {
        // 移动当前 ZombieExampleSpawnDefinition.health 逻辑。
    }
}
```

Expected:

```text
ZombieExampleSpawnDefinition 不再包含长串 health if。
```

- [ ] **Step 2: 抽出 Boss 条目模板**

Target API:

```java
public final class GeneHunterBossEntries {

    public static BossSpawnEntry ironGolem(float health, double attackDamage, double armor) {
        return BossSpawnEntry.builder()
                .entity(EntityType.getKey(EntityType.IRON_GOLEM))
                .count(1)
                .health(health)
                .attackDamage(attackDamage)
                .armor(armor)
                .build();
    }
}
```

Expected:

```text
Boss 条目构建规则与普通 pack 组合分离。
```

- [ ] **Step 3: 验证并提交**

Run:

```bash
./gradlew compileJava
```

Commit:

```bash
git add GeneHunter/src/main/java/org/galaxy/gene_hunter/data/spawn
git commit -m "整理：拆分刷怪数据模板"
```

## 阶段 4：降低 Beyond Runner 职责

**Files:**
- Create: `Beyond/src/main/java/org/galaxy/beyond/api/system/rogue/core/EncounterBroadcaster.java`
- Create: `Beyond/src/main/java/org/galaxy/beyond/api/system/rogue/core/SpawnSessionResolver.java`
- Modify: `Beyond/src/main/java/org/galaxy/beyond/api/system/rogue/core/RogueEncounterRunner.java`

- [ ] **Step 1: 抽出遭遇广播**

Target API:

```java
public final class EncounterBroadcaster {

    public void encounterStart(ServerLevel level, EncounterType type) {}

    public void currentEvent(ServerLevel level, List<ResourceLocation> eventIds, int index, int total) {}

    public void nextEvent(ServerLevel level) {}

    public void nodeUnlocked(ServerLevel level, int current, int totalScenes) {}
}
```

Expected:

```text
RogueEncounterRunner 不直接拼接大部分 Component.translatable 广播。
```

- [ ] **Step 2: 抽出 SpawnSession 解析和缓存**

Target API:

```java
public final class SpawnSessionResolver {

    public SpawnSessionData resolve(ServerLevel level, IRogueContext context, RogueNodeData nodeData, EncounterType encType, RogueEventType currentEvent) {
        // 移动 spawnData、isBossEvent 相关逻辑。
    }
}
```

Expected:

```text
RogueEncounterRunner 不直接知道 EncounterSpawnPlanner、SpawnDefinitionManager、Character 细节。
```

- [ ] **Step 3: 验证并提交**

Run:

```bash
./gradlew compileJava
```

Commit:

```bash
git add Beyond/src/main/java/org/galaxy/beyond/api/system/rogue/core
git commit -m "整理：拆分遭遇运行器职责"
```

## 阶段 5：清理 Beyond SpawnDefinition 旧字段

**Files:**
- Modify: `Beyond/src/main/java/org/galaxy/beyond/api/system/definition/SpawnDefinition.java`
- Modify generated or source spawn definition JSON if necessary

- [ ] **Step 1: 检查 gateway 字段使用情况**

Run:

```bash
rg "getGateway|\\.gateway\\(|\"gateway\"|gateway;" Beyond GeneHunter
```

Expected:

```text
如果只有旧数据或注释使用，则可以删除或迁移字段；如果仍有运行时使用，先改调用方。
```

- [ ] **Step 2: 删除或改名旧字段**

Preferred:

```java
// 如果没有运行时使用，删除 SpawnDefinition.gateway 字段。
```

Fallback:

```java
// 如果还需要外部载体信息，改名为 carrier/payload，并删除 GeneHunter/Gateway 专属注释。
```

Expected:

```text
Beyond 框架层不再出现 GeneHunter 或 Gateway 专属概念。
```

- [ ] **Step 3: 验证并提交**

Run:

```bash
./gradlew compileJava
```

Commit:

```bash
git add Beyond/src/main/java/org/galaxy/beyond/api/system/definition/SpawnDefinition.java
git commit -m "整理：清理刷怪定义旧载体字段"
```

## 每阶段完成标准

- IDEA MCP `get_file_problems(errorsOnly=true)` 对修改文件无错误。
- IDEA MCP `build_project` 或 Gradle 编译通过。
- 不做复杂测试，游戏内行为由你验证。
- 每阶段只做一个方向的整理，不顺手改数值。
- 提交信息使用中文。
- 提交前运行：

```bash
git status --short
git ls-files --others --exclude-standard
git status --ignored --short
```

确认没有该提交相关文件被忽略或漏加。
