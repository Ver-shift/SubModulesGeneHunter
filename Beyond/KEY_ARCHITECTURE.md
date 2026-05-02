# BlockOffensive — 关键架构提取

下面是从代码库中提取出的关键实现点（面向快速理解与二次开发）。将帮助你定位“回合/对局生命周期、赛点/胜利/加时、商店/购买时段、以及事件驱动”的核心代码并给出修改建议。

---

## 快速检查清单
- 阅读：`src/main/java/com/phasetranscrystal/blockoffensive/map/CSGameMap.java`（主实现）
- 阅读：`src/main/java/com/phasetranscrystal/blockoffensive/map/CSMap.java`（抽象父类，tick/同步逻辑）
- 阅读：`FPSMatch/src/main/java/com/phasetranscrystal/fpsmatch/core/shop/FPSMShop.java`（商店核心）
- 阅读：`FPSMatch/src/main/java/com/phasetranscrystal/fpsmatch/common/capability/team/ShopCapability.java`（队伍商店能力）
- 阅读：`FPSMatch/src/main/java/com/phasetranscrystal/fpsmatch/core/shop/ShopData.java`（玩家商店数据与买/退逻辑）
- 阅读：`FPSMatch/src/main/java/com/phasetranscrystal/fpsmatch/common/packet/shop/ShopActionC2SPacket.java`（客户端->服务端购买包）
- 阅读：`src/main/java/com/phasetranscrystal/blockoffensive/map/CSGameEvents.java`（Forge 事件订阅）
- 阅读：`src/main/java/com/phasetranscrystal/blockoffensive/client/screen/CSGameShopScreen.java`（客户端商店 UI）

---

## 项目总体架构（一句话）
采用“状态机 + tick 驱动”为主轴（在 `CSGameMap.tick()` 中），在关键节点通过 Forge 事件和自定义事件广播（例如 `CSGameRoundEndEvent`、`FPSMShopEvent.DataInit`、`ShopSlotChangeEvent` 等）进行解耦处理。

---

## 对局 / 回合生命周期（关键点）
- 对局对象：`CSGameMap`（继承 `CSMap` -> `BaseMap`），每张地图/实例管理一场对局。
- 启动比赛：`CSGameMap.start()`（初始化队伍、重置状态、发放套装、分配 C4、调用 `mapTeams.startNewRound()`）。
- 主循环：`CSGameMap.tick()`：处理暂停/热身/等待/回合进行逻辑，按 tick 累加 `currentRoundTime` / `currentPauseTime`。
- 回合结束处理：`roundVictory(winnerTeam, reason)` 执行 MVP、发布 `CSGameRoundEndEvent`、更新比分、发放经济奖励并设置 `isWaitingWinner`。
- 新回合：`startNewRound()` 清理地图、重置玩家/状态、分发 C4、同步商店信息并再次 `mapTeams.startNewRound()`。

---

## 赛点 / 胜利 / 加时（关键逻辑）
- 胜利分数配置：`winnerRound`（默认 13，见 `CSGameMap.setup()`）。
- 赛点提示：`CSGameMap.checkMatchPoint()` 在比分接近胜利阈值（或加时下的阈值）时发送标题与音效。
- 加时触发：在 `processRoundScoreAndOvertimeVote()` 中，当双方达到 `winnerRound - 1` 时启动加时投票（`isWaitingOverTimeVote` + `startOvertimeVote()`）。
- 加时执行：`startOvertime()` 将 `isOvertime=true`，重置队伍能力、把商店起始金设为大值并 `startNewRound()`。
- 胜利判定：`victoryGoal()` 使用 `calculateRequiredScore()`（考虑 `isOvertime` 与 `overCount`）判断是否有队赢得整场比赛。

---

## 商店 / 购买时段（关键流程）
- 配置项：`closeShopTime`（tick，为 `CSGameMap.setup()` 的设置；默认 200 ticks = 10s）、`roundTimeLimit`、`waitingTime` 等。
- 商店关闭判定：`CSGameMap.isClosedShop()` 与 `isRoundTimeEnd()`。当 `currentRoundTime >= closeShopTime` 时会触发 `isShopLocked = true` 并 `syncShopInfo(false, 0)`。
- 玩家能否打开商店：`CSGameMap.getPlayerCanOpenShop(ShopCapability cap, ServerPlayer player)` 返回 `!isShopLocked && shop.isInArea(player)`。
- 客户端显示：服务端通过 `ShopStatesS2CPacket`（`CSMap.syncShopInfo`）发送是否可开、下一轮最低钱和剩余购买时间（秒）。客户端 UI (`CSGameShopScreen`) 根据 `CSClientData` 显示倒计时与按钮。
- 购买调用链（click -> 发包 -> 执行）：
  - 客户端 UI：`CSGameShopScreen.GunButtonLayout` 发送 `ShopActionC2SPacket`（BUY/RETURN）。
  - 服务端包处理：`ShopActionC2SPacket.handle()` 通过 `BaseMap -> Team -> ShopCapability -> FPSMShop.handleButton()` 调用。
  - 商店处理：`FPSMShop.handleButton()` -> `ShopData.handleButton()` -> `ShopData.handleBuy()` -> `ShopSlot.buy()`（具体物品发放在 `ShopSlot` 内）。
  - 同步：`FPSMShop.syncShopData(player)` 与 `syncShopMoneyData(player)` 将变更同步回客户端。

---

## 事件系统与扩展点
- Forge 事件：模块里大量使用 `@SubscribeEvent`（例如 `CSGameEvents`、`ShopCapability` 的拾取/丢弃处理）用于跨模块联动。
- 自定义事件：`CSGameRoundEndEvent`（回合结束）、`FPSMShopEvent.DataInit`（商店数据初始化）、`ShopSlotChangeEvent` / `CheckCostEvent`（商店内广播）。
- 可插入点：
  - 在购买前添加校验/费用修改：订阅或拦截 `CheckCostEvent`。
  - 购买后逻辑：监听 `ShopSlotChangeEvent` 或在 `ShopSlot.buy()` 中添加逻辑。
  - 回合/胜利事件扩展：订阅 `CSGameRoundEndEvent`。

---

## 常用配置项 & 修改入口（一览）
- 更改购买倒计时（示例）：编辑 `CSGameMap.setup()` 中 `closeShopTime`，值为 tick（秒 = 值 / 20）。
  - 文件：`src/main/java/com/phasetranscrystal/blockoffensive/map/CSGameMap.java`
  - 示例：把 `this.addSetting("closeShopTime", 200)` 改为 `300` -> 15 秒。
- 更改回合时长/胜利分数：同文件的 `roundTimeLimit`、`winnerRound`。
- 修改加时规则：`handleOvertimeAndTeamSwitch` / `startOvertime` / `calculateRequiredScore`。

---

## 快速调用图（购买链路，简洁版）
Client Click -> CSGameShopScreen -> send ShopActionC2SPacket -> ShopActionC2SPacket.handle()
-> BaseMap (by name) -> Team -> ShopCapability.getShop().handleButton()
-> FPSMShop.handleButton() -> ShopData.handleButton() -> ShopData.handleBuy() -> ShopSlot.buy()
-> FPSMShop.syncShopData(player) + syncShopMoneyData(player) -> Client Update

---

## 建议与注意事项
- 大量计时值使用 tick（20 tick = 1s），修改时注意单位换算。
- 商店区域判断 `FPSMShop.isInArea(entity)`：若 areas 为空表示全域可买，地图自定义时注意覆盖。
- 事件订阅优先级与 `EventPriority` 有关（`ShopCapability` 中对丢弃/拾取使用了 `LOWEST` 做解耦），添加订阅时注意冲突。

---

如需我把这份 Markdown 插入到项目文档（已保存为 `DOCS/KEY_ARCHITECTURE.md`），或把其中某些配置改成以秒为单位的友好 API 并做代码修改与快速编译验证，请告诉我下一步要做的事项。
