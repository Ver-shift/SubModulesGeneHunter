# API参考文档

<cite>
**本文档引用的文件**
- [BiotechAPI.java](file://Biotech/src/main/java/org/biotech/api/BiotechAPI.java)
- [ISafeZoneRuleListener.java](file://Beyond/src/main/java/com/pz/beyond/safeZoneRule/ISafeZoneRuleListener.java)
- [SafeZonePayload.java](file://Beyond/src/main/java/com/pz/beyond/network/SafeZonePayload.java)
- [IGene.java](file://Biotech/src/main/java/org/biotech/api/system/gene/core/IGene.java)
- [IGeneItem.java](file://Biotech/src/main/java/org/biotech/api/system/gene/core/IGeneItem.java)
- [IChoiceManager.java](file://Biotech/src/main/java/org/biotech/api/system/choice/core/IChoiceManager.java)
- [IGeneInventoryManager.java](file://Biotech/src/main/java/org/biotech/api/system/gene/core/manager/IGeneInventoryManager.java)
- [ILootTableManager.java](file://Biotech/src/main/java/org/biotech/api/system/loot/core/ILootTableManager.java)
- [IMergeManager.java](file://Biotech/src/main/java/org/biotech/api/system/merge/core/IMergeManager.java)
- [IGeneLike.java](file://Biotech/src/main/java/org/biotech/api/system/gene/core/IGeneLike.java)
- [IUnidentifiedGeneItem.java](file://Biotech/src/main/java/org/biotech/api/system/gene/core/IUnidentifiedGeneItem.java)
- [IXenoItem.java](file://Biotech/src/main/java/org/biotech/api/system/gene/core/IXenoItem.java)
- [BorderRenderUtil.java](file://Beyond/src/main/java/com/pz/beyond/api/util/BorderRenderUtil.java)
- [TeleportUtil.java](file://Beyond/src/main/java/com/pz/beyond/api/util/TeleportUtil.java)
- [BeyondAPI.java](file://Beyond/src/main/java/com/pz/beyond/api/BeyondAPI.java)
- [BeyondAttachInit.java](file://Beyond/src/main/java/com/pz/beyond/api/init/BeyondAttachInit.java)
</cite>

## 更新摘要
**变更内容**
- 新增BorderRenderUtil工具类API，提供安全边界渲染功能
- 新增TeleportUtil工具类API，提供安全传送位置查找和结构定位功能
- 增强BeyondAPI接口，增加维度数据访问能力
- 更新架构总览图，反映新增工具类和增强的API

## 目录
1. [简介](#简介)
2. [项目结构](#项目结构)
3. [核心组件](#核心组件)
4. [架构总览](#架构总览)
5. [详细组件分析](#详细组件分析)
6. [依赖分析](#依赖分析)
7. [性能考虑](#性能考虑)
8. [故障排除指南](#故障排除指南)
9. [结论](#结论)
10. [附录](#附录)

## 简介
本文件为"基因猎人"子模块的API参考文档，聚焦于以下核心接口与能力：
- BiotechAPI：提供对玩家基因数据、界面打开、Curios槽位访问等的统一入口。
- ISafeZoneRuleListener：安全区规则监听器接口，用于定义安全区内/外的每tick行为以及伤害、怪物生成等事件处理。
- 自定义网络包 SafeZonePayload：基于NeoForge自定义包协议的数据载体，用于传输安全区中心与半径信息。
- **新增** BorderRenderUtil：提供安全边界渲染工具，支持动态纹理效果和透明度渐变。
- **新增** TeleportUtil：提供安全传送位置查找和结构定位功能。
- **增强** BeyondAPI：新增维度数据访问能力，支持进度目录、区域数据和进度管理器的获取。

同时，文档覆盖Biotech模块中的基因系统核心接口族（如IGene、IGeneItem、IUnidentifiedGeneItem、IXenoItem、IGeneLike），以及与之配套的管理器接口（IChoiceManager、IGeneInventoryManager、ILootTableManager、IMergeManager）。每个API均给出参数说明、返回值描述、使用场景、注意事项与最佳实践，并提供与实际代码实现一致的路径引用与图示。

## 项目结构
本仓库包含多个子模块，其中与API相关的关键位置如下：
- Biotech 模块：提供基因系统API、管理器接口、数据模型与客户端渲染等。
- Beyond 模块：提供安全区规则、网络包、工具类和维度数据访问等扩展功能。
- GalaxyLib、GameText、GeneHunter、ModFix：通用库与示例模块，不作为本API文档重点。

```mermaid
graph TB
subgraph "Biotech 模块"
BA["BiotechAPI.java"]
GENECORE["IGene.java"]
GENELIKE["IGeneLike.java"]
GENELIKEITEM["IGeneItem.java"]
UNIDENTGENE["IUnidentifiedGeneItem.java"]
XENEOBJ["IXenoItem.java"]
CHOICE["IChoiceManager.java"]
INV["IGeneInventoryManager.java"]
LOOT["ILootTableManager.java"]
MERGE["IMergeManager.java"]
end
subgraph "Beyond 模块"
SZLIST["ISafeZoneRuleListener.java"]
SZPAY["SafeZonePayload.java"]
BORDERUTIL["BorderRenderUtil.java"]
TPUTIL["TeleportUtil.java"]
BEYOND["BeyondAPI.java"]
BEYONDATTACH["BeyondAttachInit.java"]
end
BA --> INV
BA --> LOOT
BA --> MERGE
BA --> CHOICE
BA --> GENECORE
GENECORE --> GENELIKE
GENELIKE --> GENELIKEITEM
UNIDENTGENE --> LOOT
UNIDENTGENE --> INV
SZLIST --> SZPAY
BORDERUTIL --> TPUTIL
BEYOND --> BEYONDATTACH
```

**图表来源**
- [BiotechAPI.java:1-92](file://Biotech/src/main/java/org/biotech/api/BiotechAPI.java#L1-L92)
- [ISafeZoneRuleListener.java:1-42](file://Beyond/src/main/java/com/pz/beyond/safeZoneRule/ISafeZoneRuleListener.java#L1-L42)
- [SafeZonePayload.java:1-31](file://Beyond/src/main/java/com/pz/beyond/network/SafeZonePayload.java#L1-L31)
- [BorderRenderUtil.java:1-110](file://Beyond/src/main/java/com/pz/beyond/api/util/BorderRenderUtil.java#L1-L110)
- [TeleportUtil.java:1-94](file://Beyond/src/main/java/com/pz/beyond/api/util/TeleportUtil.java#L1-L94)
- [BeyondAPI.java:1-62](file://Beyond/src/main/java/com/pz/beyond/api/BeyondAPI.java#L1-L62)
- [BeyondAttachInit.java:1-186](file://Beyond/src/main/java/com/pz/beyond/api/init/BeyondAttachInit.java#L1-L186)

**章节来源**
- [BiotechAPI.java:1-92](file://Biotech/src/main/java/org/biotech/api/BiotechAPI.java#L1-L92)
- [ISafeZoneRuleListener.java:1-42](file://Beyond/src/main/java/com/pz/beyond/safeZoneRule/ISafeZoneRuleListener.java#L1-L42)
- [SafeZonePayload.java:1-31](file://Beyond/src/main/java/com/pz/beyond/network/SafeZonePayload.java#L1-L31)
- [BorderRenderUtil.java:1-110](file://Beyond/src/main/java/com/pz/beyond/api/util/BorderRenderUtil.java#L1-L110)
- [TeleportUtil.java:1-94](file://Beyond/src/main/java/com/pz/beyond/api/util/TeleportUtil.java#L1-L94)
- [BeyondAPI.java:1-62](file://Beyond/src/main/java/com/pz/beyond/api/BeyondAPI.java#L1-L62)
- [BeyondAttachInit.java:1-186](file://Beyond/src/main/java/com/pz/beyond/api/init/BeyondAttachInit.java#L1-L186)

## 核心组件
本节概述BiotechAPI与Beyond模块中的关键接口，帮助快速定位所需能力。

- BiotechAPI
  - 功能：提供获取玩家基因数据、打开基因界面、访问Curios槽位等统一入口。
  - 关键方法：获取基因数据、获取基因库存管理器、获取战利品表管理器、获取合并管理器、打开他人基因界面、打开基因选择界面、获取Curios基因/异种槽位。
  - 使用场景：服务端对玩家执行UI交互、跨模块读取/写入玩家基因状态。
  - 注意事项：Curios槽位访问依赖Curios API；打开界面时需确保目标玩家在线且具备对应能力。

- ISafeZoneRuleListener
  - 功能：定义安全区内/外每tick行为及伤害、怪物生成等事件回调。
  - 关键方法：onSafeZoneTick、outSideSafeZoneTick、invincible、onMobSpawn。
  - 使用场景：实现安全区内的重生、无敌、禁止刷怪等规则。
  - 注意事项：默认空实现，按需覆写需要的方法。

- SafeZonePayload
  - 功能：自定义网络包，承载安全区中心坐标与半径。
  - 字段：centerX、centerZ、radius。
  - 使用场景：服务端向客户端广播安全区配置或动态更新。
  - 注意事项：遵循NeoForge自定义包协议规范，使用StreamCodec进行编解码。

- **新增** BorderRenderUtil
  - 功能：提供安全边界渲染工具，支持动态纹理效果和透明度渐变。
  - 关键方法：render(centerX, centerZ, radius, r, g, b, bottomAlpha, topAlpha, tex, camera, poseStack)、render(minX, minZ, maxX, maxZ, r, g, b, bottomAlpha, topAlpha, tex, camera, poseStack)。
  - 使用场景：客户端渲染安全区边界效果，支持动态纹理和透明度控制。
  - 注意事项：依赖Minecraft客户端渲染系统，需要正确的相机和PoseStack参数。

- **新增** TeleportUtil
  - 功能：提供安全传送位置查找和结构定位功能。
  - 关键方法：findSafeTeleportPos、isSafeStandingPosition、findNearestStructure。
  - 使用场景：安全传送系统、结构定位、传送目的地验证。
  - 注意事项：需要服务器世界权限，安全位置检查包含多种条件。

- **增强** BeyondAPI
  - 功能：新增维度数据访问能力，提供进度目录、区域数据和进度管理器的获取。
  - 关键方法：getProgressCatalog、getZoneData、getProgressManager、getProgressManagerImpl、getBeyondData。
  - 使用场景：跨维度数据访问、进度管理系统集成、区域数据管理。
  - 注意事项：仅支持主世界维度，其他维度返回null。

**章节来源**
- [BiotechAPI.java:18-92](file://Biotech/src/main/java/org/biotech/api/BiotechAPI.java#L18-L92)
- [ISafeZoneRuleListener.java:8-42](file://Beyond/src/main/java/com/pz/beyond/safeZoneRule/ISafeZoneRuleListener.java#L8-L42)
- [SafeZonePayload.java:10-30](file://Beyond/src/main/java/com/pz/beyond/network/SafeZonePayload.java#L10-L30)
- [BorderRenderUtil.java:13-110](file://Beyond/src/main/java/com/pz/beyond/api/util/BorderRenderUtil.java#L13-L110)
- [TeleportUtil.java:14-94](file://Beyond/src/main/java/com/pz/beyond/api/util/TeleportUtil.java#L14-L94)
- [BeyondAPI.java:15-62](file://Beyond/src/main/java/com/pz/beyond/api/BeyondAPI.java#L15-L62)

## 架构总览
下图展示了BiotechAPI与各管理器、接口之间的关系，以及与Curios、NeoForge事件系统的集成点。**新增**的BorderRenderUtil和TeleportUtil工具类现在作为独立的实用工具类存在，而BeyondAPI现在提供了更强大的维度数据访问能力。

```mermaid
classDiagram
class BiotechAPI {
+getGeneData(player)
+getGeneInventoryManager(player)
+getLootTableManager(player)
+getMergeManager(player)
+openGeneInventoryFor(targetPlayer)
+getChoiceManager(player)
+openGeneChoiceFor(targetPlayer)
+getGeneEquipSlots(targetPlayer)
+getXeneEquipSlots(targetPlayer)
}
class BeyondAPI {
+getProgressCatalog(level)
+getZoneData(level)
+getProgressManager(level)
+getProgressManagerImpl(level)
+getBeyondData(player)
}
class BorderRenderUtil {
+render(centerX, centerZ, radius, r, g, b, bottomAlpha, topAlpha, tex, camera, poseStack)
+render(minX, minZ, maxX, maxZ, r, g, b, bottomAlpha, topAlpha, tex, camera, poseStack)
}
class TeleportUtil {
+findSafeTeleportPos(level, target)
+isSafeStandingPosition(level, pos)
+findNearestStructure(level, structureTag, searchRadius)
+findNearestStructure(level, structureTag, searchRadius, defaultPos)
}
class IChoiceManager {
+openChoiceMenu() boolean
+getChoiceCount() int
+setChoiceCount(choice) void
+doRoll() void
}
class IGeneInventoryManager {
+openGeneMenu() boolean
+add(item/GeneItem/ItemStack) AddResult
+removeItem(slotIndex) boolean
+addToFavorite(item) AddResult
+getFirstEmptySlot() int
+getItemStack(slotIndex) ItemStack
}
class ILootTableManager {
+modify(lootType, builder) ILootTableManager
+rollWithReplacement(lootType) LootResult
+roolWithoutReplacement(lootType) LootResult
+setWeightByName(name, weight) void
+merge(targetLootType, ids) void
+claimResults(results) void
+init() void
}
class IMergeManager {
+updateSlotData() void
+traitCount() int
+traitCountPerGene() float
+outputXeneCount(traitCount) int
+merge() void
}
class IGene {
+getID() ResourceLocation
+getConfigBuilder() GeneConfigBuilder
+getDisplayName() Component
+tick(slotContext, stack) void
+geneOnEquip(slotContext, prevStack, stack) void
+onUnequip(slotContext, stack) void
+geneCanEquip(slotContext, stack) boolean
+geneCanUnequip(slotContext, stack) boolean
}
class IGeneLike {
+asGene() IGene
}
class IGeneItem {
}
class IUnidentifiedGeneItem {
+use(player, rarity) void
+use(level, player, hand, rarity) InteractionResultHolder
}
class IXenoItem {
}
class BeyondAttachInit {
+getBeyondData(player)
+getProgressCatalog(level)
+getLevelZoneData(level)
+getProgressManager(level)
+isAllowedDimension(level)
}
BiotechAPI --> IChoiceManager : "获取/打开"
BiotechAPI --> IGeneInventoryManager : "获取/打开"
BiotechAPI --> ILootTableManager : "获取"
BiotechAPI --> IMergeManager : "获取"
IGeneItem <|.. IGeneLike : "实现"
IUnidentifiedGeneItem ..> ILootTableManager : "使用"
IUnidentifiedGeneItem ..> IGeneInventoryManager : "使用"
IGene <|-- IGeneItem : "继承"
IGene <|-- IXenoItem : "继承"
BeyondAPI --> BeyondAttachInit : "委托"
BorderRenderUtil --> BeyondAPI : "辅助"
TeleportUtil --> BeyondAPI : "辅助"
```

**图表来源**
- [BiotechAPI.java:21-92](file://Biotech/src/main/java/org/biotech/api/BiotechAPI.java#L21-L92)
- [BeyondAPI.java:15-62](file://Beyond/src/main/java/com/pz/beyond/api/BeyondAPI.java#L15-L62)
- [BorderRenderUtil.java:13-110](file://Beyond/src/main/java/com/pz/beyond/api/util/BorderRenderUtil.java#L13-L110)
- [TeleportUtil.java:14-94](file://Beyond/src/main/java/com/pz/beyond/api/util/TeleportUtil.java#L14-L94)
- [BeyondAttachInit.java:23-186](file://Beyond/src/main/java/com/pz/beyond/api/init/BeyondAttachInit.java#L23-L186)
- [IChoiceManager.java:5-22](file://Biotech/src/main/java/org/biotech/api/system/choice/core/IChoiceManager.java#L5-L22)
- [IGeneInventoryManager.java:12-133](file://Biotech/src/main/java/org/biotech/api/system/gene/core/manager/IGeneInventoryManager.java#L12-L133)
- [ILootTableManager.java:14-77](file://Biotech/src/main/java/org/biotech/api/system/loot/core/ILootTableManager.java#L14-L77)
- [IMergeManager.java:13-52](file://Biotech/src/main/java/org/biotech/api/system/merge/core/IMergeManager.java#L13-L52)
- [IGene.java:15-91](file://Biotech/src/main/java/org/biotech/api/system/gene/core/IGene.java#L15-L91)
- [IGeneLike.java:3-5](file://Biotech/src/main/java/org/biotech/api/system/gene/core/IGeneLike.java#L3-L5)
- [IGeneItem.java:5-7](file://Biotech/src/main/java/org/biotech/api/system/gene/core/IGeneItem.java#L5-L7)
- [IUnidentifiedGeneItem.java:23-138](file://Biotech/src/main/java/org/biotech/api/system/gene/core/IUnidentifiedGeneItem.java#L23-L138)
- [IXenoItem.java:9-12](file://Biotech/src/main/java/org/biotech/api/system/gene/core/IXenoItem.java#L9-L12)

## 详细组件分析

### BiotechAPI 接口详解
- 设计原则
  - 单一职责：集中暴露玩家基因相关能力与UI入口。
  - 松耦合：通过能力与数据容器访问，避免直接依赖具体实现。
  - 可扩展：新增能力可通过注册新能力/管理器扩展。
- 公共方法
  - getGeneData(Player): 获取玩家的基因数据容器。
  - getGeneInventoryManager(Player)/getLootTableManager(Player)/getMergeManager(Player): 获取对应管理器能力。
  - openGeneInventoryFor(ServerPlayer): 服务端为目标玩家打开基因库存界面。
  - getChoiceManager(Player)/openGeneChoiceFor(ServerPlayer): 基因选择系统入口。
  - getGeneEquipSlots/getXeneEquipSlots(Player): 通过Curios API获取基因/异种装备槽位。
- 参数与返回
  - Player/targetPlayer：玩家对象；ServerPlayer：仅服务端可用。
  - 返回值：数据容器、管理器实例或Curios的IDynamicStackHandler；若无能力则可能为空。
- 使用示例（路径）
  - 服务端打开他人基因界面：[BiotechAPI.java:47-56](file://Biotech/src/main/java/org/biotech/api/BiotechAPI.java#L47-L56)
  - 获取Curios槽位：[BiotechAPI.java:75-90](file://Biotech/src/main/java/org/biotech/api/BiotechAPI.java#L75-L90)
- 最佳实践
  - 在调用open*方法前检查管理器是否可用，避免空指针。
  - Curios槽位访问需确保Curios已初始化且玩家拥有对应槽位。
  - 服务端操作应通过BiotechAPI提供的静态方法，避免直接访问能力。

**章节来源**
- [BiotechAPI.java:18-92](file://Biotech/src/main/java/org/biotech/api/BiotechAPI.java#L18-L92)

### ISafeZoneRuleListener 规则监听器接口
- 设计原则
  - 默认空实现：允许按需覆写特定事件回调，减少样板代码。
  - 上下文传递：通过SafeZoneContext携带当前玩家信息。
- 方法说明
  - onSafeZoneTick(SafeZoneContext): 安全区内每tick回调。
  - outSideSafeZoneTick(SafeZoneContext): 安全区外每tick回调。
  - invincible(LivingDamageEvent.Pre): 无敌判定前置事件。
  - onMobSpawn(MobSpawnEvent.PositionCheck): 怪物生成前置事件。
- 使用场景
  - 实现安全区内自动回血、免伤、禁止刷怪等规则。
- 示例（路径）
  - 定义监听器并覆写方法：[ISafeZoneRuleListener.java:13-41](file://Beyond/src/main/java/com/pz/beyond/safeZoneRule/ISafeZoneRuleListener.java#L13-L41)

**章节来源**
- [ISafeZoneRuleListener.java:8-42](file://Beyond/src/main/java/com/pz/beyond/safeZoneRule/ISafeZoneRuleListener.java#L8-L42)

### 自定义网络包 SafeZonePayload
- 设计原则
  - 轻量数据载体：仅包含安全区几何信息。
  - 符合NeoForge规范：实现CustomPacketPayload并提供StreamCodec。
- 字段
  - centerX、centerZ：安全区中心坐标（整数）。
  - radius：安全区半径（整数）。
- 编解码
  - 类型标识：使用ResourceLocation作为包类型。
  - 流编解码：使用StreamCodec.composite进行组合编解码。
- 使用场景
  - 服务端向客户端推送安全区配置或动态变更。
- 示例（路径）
  - 定义包类型与编解码器：[SafeZonePayload.java:10-30](file://Beyond/src/main/java/com/pz/beyond/network/SafeZonePayload.java#L10-L30)

**章节来源**
- [SafeZonePayload.java:10-30](file://Beyond/src/main/java/com/pz/beyond/network/SafeZonePayload.java#L10-L30)

### BorderRenderUtil 工具类详解
- 设计原则
  - 独立工具类：提供专门的渲染功能，不依赖其他模块。
  - 客户端渲染：专注于Minecraft客户端渲染系统。
  - 参数化配置：支持颜色、透明度、纹理等参数配置。
- 公共方法
  - render(int centerX, int centerZ, int radius, int r, int g, int b, int bottomAlpha, int topAlpha, ResourceLocation tex, Camera camera, PoseStack poseStack)
  - render(double minX, double minZ, double maxX, double maxZ, int r, int g, int b, int bottomAlpha, int topAlpha, ResourceLocation tex, Camera camera, PoseStack poseStack)
- 参数与返回
  - centerX/centerZ/radius：安全区几何参数。
  - r/g/b：颜色RGB分量（0-255）。
  - bottomAlpha/topAlpha：底部和顶部透明度（0-255）。
  - tex：纹理资源位置。
  - camera/poseStack：Minecraft渲染系统必需参数。
  - 返回值：void。
- 使用场景
  - 客户端渲染安全区边界效果。
  - 实现动态纹理和透明度渐变。
- 最佳实践
  - 确保相机和PoseStack参数正确传递。
  - 合理设置透明度参数以获得最佳视觉效果。
  - 注意渲染性能，避免过度频繁的调用。

**章节来源**
- [BorderRenderUtil.java:13-110](file://Beyond/src/main/java/com/pz/beyond/api/util/BorderRenderUtil.java#L13-L110)

### TeleportUtil 工具类详解
- 设计原则
  - 安全优先：确保传送位置的安全性和可见性。
  - 结构导向：提供结构定位功能，便于导航。
  - 灵活配置：支持默认返回位置和搜索半径配置。
- 公共方法
  - findSafeTeleportPos(ServerLevel level, BlockPos target): 查找安全的传送位置。
  - isSafeStandingPosition(ServerLevel level, BlockPos pos): 检查位置是否可以安全站立。
  - findNearestStructure(ServerLevel level, TagKey<Structure> structureTag, int searchRadius): 查找最近的结构位置。
  - findNearestStructure(ServerLevel level, TagKey<Structure> structureTag, int searchRadius, BlockPos defaultPos): 查找最近的结构位置（带默认返回）。
- 参数与返回
  - level：服务器世界对象。
  - target/pos：目标位置坐标。
  - structureTag：结构标签。
  - searchRadius：搜索半径（区块）。
  - defaultPos：默认返回位置。
  - 返回值：安全位置坐标或结构位置。
- 使用场景
  - 安全传送系统实现。
  - 结构定位和导航功能。
  - 传送目的地验证。
- 最佳实践
  - 在查找安全位置时考虑世界边界。
  - 合理设置搜索半径以平衡性能和准确性。
  - 处理未找到结构的情况，提供合理的默认位置。

**章节来源**
- [TeleportUtil.java:14-94](file://Beyond/src/main/java/com/pz/beyond/api/util/TeleportUtil.java#L14-L94)

### BeyondAPI 增强接口详解
- 设计原则
  - 维度隔离：仅支持主世界维度，其他维度返回null。
  - 委托模式：将实际数据访问委托给BeyondAttachInit。
  - 类型安全：提供泛型和具体类型的双重访问方式。
- 公共方法
  - getProgressCatalog(Level level): 获取进度目录数据。
  - getZoneData(Level level): 获取区域数据。
  - getProgressManager(Level level): 获取进度管理器（接口类型）。
  - getProgressManagerImpl(Level level): 获取进度管理器（具体类型）。
  - getBeyondData(Player player): 获取玩家Beyond数据。
- 参数与返回
  - level：世界对象；player：玩家对象。
  - 返回值：对应的数据容器或管理器实例。
- 使用场景
  - 跨维度数据访问。
  - 进度管理系统集成。
  - 区域数据管理。
- 最佳实践
  - 检查返回值是否为null，特别是在非主世界使用时。
  - 使用getProgressManagerImpl获取具体实现类型以访问更多功能。
  - 注意维度限制，仅在主世界使用相关功能。

**章节来源**
- [BeyondAPI.java:15-62](file://Beyond/src/main/java/com/pz/beyond/api/BeyondAPI.java#L15-L62)

### 基因系统接口族
- IGene
  - 能力：提供基因ID、配置构建器、显示名，默认tick/穿戴/脱下/可穿戴判定。
  - 网络编解码：通过ResourceLocation与StreamCodec支持序列化。
  - 扩展：默认方法封装Curios接口，保证与Curios生态兼容。
- IGeneLike
  - 能力：将对象转换为IGene实例。
- IGeneItem / IXenoItem
  - 能力：分别代表基因物品与异种物品，继承ICurioItem以接入Curios。
- IUnidentifiedGeneItem
  - 能力：未鉴定基因的使用流程，包括根据稀有度设置抽取次数、发放词条、消息提示与音效。
  - 依赖：ILootTableManager、IGeneInventoryManager、ServerConfig、AttributeInit等。
- 使用场景
  - 开发者实现自定义基因/异种物品，或扩展未鉴定基因的使用逻辑。

**章节来源**
- [IGene.java:15-91](file://Biotech/src/main/java/org/biotech/api/system/gene/core/IGene.java#L15-L91)
- [IGeneLike.java:3-5](file://Biotech/src/main/java/org/biotech/api/system/gene/core/IGeneLike.java#L3-L5)
- [IGeneItem.java:5-7](file://Biotech/src/main/java/org/biotech/api/system/gene/core/IGeneItem.java#L5-L7)
- [IXenoItem.java:9-12](file://Biotech/src/main/java/org/biotech/api/system/gene/core/IXenoItem.java#L9-L12)
- [IUnidentifiedGeneItem.java:23-138](file://Biotech/src/main/java/org/biotech/api/system/gene/core/IUnidentifiedGeneItem.java#L23-L138)

### 管理器接口族
- IChoiceManager
  - 能力：打开选择界面、设置/获取选择次数、执行抽取。
- IGeneInventoryManager
  - 能力：打开界面、向背包/收藏槽添加物品、删除、查询空位、获取物品栈。
  - 结果枚举AddResult：SUCCESS/FULL/EMPTY_ITEM/NO_SPACE/INVALID_TYPE，并提供崩溃报告工具。
- ILootTableManager
  - 能力：修改战利品表、不放回/放回抽取、按名称批量设置权重、合并多个战利品表、领取结果。
  - 结果记录LootResult：包含抽取结果与对应战利品类型。
- IMergeManager
  - 能力：槽位数据更新、统计词条数、计算平均词条数、输出异种数量、执行合并。

**章节来源**
- [IChoiceManager.java:5-22](file://Biotech/src/main/java/org/biotech/api/system/choice/core/IChoiceManager.java#L5-L22)
- [IGeneInventoryManager.java:12-133](file://Biotech/src/main/java/org/biotech/api/system/gene/core/manager/IGeneInventoryManager.java#L12-L133)
- [ILootTableManager.java:14-77](file://Biotech/src/main/java/org/biotech/api/system/loot/core/ILootTableManager.java#L14-L77)
- [IMergeManager.java:13-52](file://Biotech/src/main/java/org/biotech/api/system/merge/core/IMergeManager.java#L13-L52)

### API调用时序示例

#### 服务端打开他人基因界面
```mermaid
sequenceDiagram
participant Server as "服务端"
participant API as "BiotechAPI"
participant Manager as "IGeneInventoryManager"
participant Logger as "日志"
Server->>API : "openGeneInventoryFor(targetPlayer)"
API->>API : "getGeneInventoryManager(targetPlayer)"
API->>Manager : "openGeneMenu()"
alt "打开成功"
API->>Logger : "info(打开成功)"
else "打开失败"
API->>Logger : "error(打开失败)"
end
```

**图表来源**
- [BiotechAPI.java:47-56](file://Biotech/src/main/java/org/biotech/api/BiotechAPI.java#L47-L56)
- [IGeneInventoryManager.java:17-17](file://Biotech/src/main/java/org/biotech/api/system/gene/core/manager/IGeneInventoryManager.java#L17-L17)

#### 未鉴定基因使用流程
```mermaid
flowchart TD
Start(["开始使用"]) --> CheckPlayer["检查是否为服务器玩家"]
CheckPlayer --> |是| GetMgr["获取ILootTableManager"]
CheckPlayer --> |否| End(["结束"])
GetMgr --> SetAttr["根据稀有度设置抽取次数"]
SetAttr --> Roll["执行放回抽取"]
Roll --> Claim["发放结果到玩家或背包"]
Claim --> Msg["发送获取消息/失败原因"]
Msg --> End
```

**图表来源**
- [IUnidentifiedGeneItem.java:30-94](file://Biotech/src/main/java/org/biotech/api/system/gene/core/IUnidentifiedGeneItem.java#L30-L94)
- [ILootTableManager.java:22-68](file://Biotech/src/main/java/org/biotech/api/system/loot/core/ILootTableManager.java#L22-L68)

#### 安全边界渲染流程
```mermaid
sequenceDiagram
participant Client as "客户端"
participant BRU as "BorderRenderUtil"
participant RS as "渲染系统"
participant PS as "PoseStack"
Client->>BRU : "render(centerX, centerZ, radius, r, g, b, alpha, tex, camera, poseStack)"
BRU->>PS : "pushPose()"
BRU->>RS : "配置渲染状态"
BRU->>RS : "设置着色器和纹理"
BRU->>BRU : "计算UV偏移"
BRU->>RS : "绘制四面墙"
BRU->>RS : "恢复渲染状态"
BRU->>PS : "popPose()"
```

**图表来源**
- [BorderRenderUtil.java:14-76](file://Beyond/src/main/java/com/pz/beyond/api/util/BorderRenderUtil.java#L14-L76)

#### 安全传送位置查找流程
```mermaid
flowchart TD
Start(["开始查找"]) --> CheckWorld["检查服务器世界"]
CheckWorld --> Loop["向上遍历Y坐标"]
Loop --> CheckPos["检查当前位置"]
CheckPos --> |安全| Return["返回安全位置"]
CheckPos --> |不安全| Next["检查下一个位置"]
Next --> Loop
Loop --> |未找到| Default["返回默认位置"]
Default --> End(["结束"])
Return --> End
```

**图表来源**
- [TeleportUtil.java:28-38](file://Beyond/src/main/java/com/pz/beyond/api/util/TeleportUtil.java#L28-L38)
- [TeleportUtil.java:52-65](file://Beyond/src/main/java/com/pz/beyond/api/util/TeleportUtil.java#L52-L65)

## 依赖分析
- 外部依赖
  - NeoForge事件系统：LivingDamageEvent、MobSpawnEvent等。
  - Curios API：IDynamicStackHandler、ICurioItem、SlotContext等。
  - Minecraft网络编解码：StreamCodec、CustomPacketPayload。
  - **新增** Minecraft客户端渲染系统：Blaze3D、PoseStack、BufferBuilder等。
  - **新增** Minecraft结构系统：Structure、TagKey等。
- 内部依赖
  - BiotechAPI依赖能力注册与数据容器（CapInit、AttachInit）。
  - IUnidentifiedGeneItem依赖ServerConfig、LootTypeInit、AttributeInit等。
  - **新增** BorderRenderUtil依赖BeyondAPI的渲染系统。
  - **新增** TeleportUtil依赖BeyondAPI的维度数据访问。
  - **增强** BeyondAPI依赖BeyondAttachInit进行数据访问。
- 耦合与内聚
  - BiotechAPI作为门面，降低上层对底层实现的耦合。
  - 各管理器接口职责清晰，便于替换与扩展。
  - **新增** 工具类采用独立设计，降低模块间耦合。

**图表来源**
- [BiotechAPI.java:3-15](file://Biotech/src/main/java/org/biotech/api/BiotechAPI.java#L3-L15)
- [IUnidentifiedGeneItem.java:15-21](file://Biotech/src/main/java/org/biotech/api/system/gene/core/IUnidentifiedGeneItem.java#L15-L21)
- [SafeZonePayload.java:3-8](file://Beyond/src/main/java/com/pz/beyond/network/SafeZonePayload.java#L3-L8)
- [BorderRenderUtil.java:3-11](file://Beyond/src/main/java/com/pz/beyond/api/util/BorderRenderUtil.java#L3-L11)
- [TeleportUtil.java:3-7](file://Beyond/src/main/java/com/pz/beyond/api/util/TeleportUtil.java#L3-L7)
- [BeyondAPI.java:3-10](file://Beyond/src/main/java/com/pz/beyond/api/BeyondAPI.java#L3-L10)

**章节来源**
- [BiotechAPI.java:3-15](file://Biotech/src/main/java/org/biotech/api/BiotechAPI.java#L3-L15)
- [IUnidentifiedGeneItem.java:15-21](file://Biotech/src/main/java/org/biotech/api/system/gene/core/IUnidentifiedGeneItem.java#L15-L21)
- [SafeZonePayload.java:3-8](file://Beyond/src/main/java/com/pz/beyond/network/SafeZonePayload.java#L3-L8)
- [BorderRenderUtil.java:3-11](file://Beyond/src/main/java/com/pz/beyond/api/util/BorderRenderUtil.java#L3-L11)
- [TeleportUtil.java:3-7](file://Beyond/src/main/java/com/pz/beyond/api/util/TeleportUtil.java#L3-L7)
- [BeyondAPI.java:3-10](file://Beyond/src/main/java/com/pz/beyond/api/BeyondAPI.java#L3-L10)

## 性能考虑
- 界面打开与能力访问
  - 建议在服务端调用前先检查管理器是否可用，避免无效调用导致日志噪音。
- Curios槽位访问
  - 槽位访问涉及Optional链式调用，建议缓存结果并在必要时重新获取。
- 战利品抽取
  - 放回抽取可能产生多次随机，建议在服务端批处理并一次性发放结果。
- 合并系统
  - 合并前建议预估输出数量与槽位占用，避免频繁IO与UI刷新。
- **新增** 客户端渲染
  - BorderRenderUtil渲染涉及大量顶点计算，建议在必要时才调用。
  - 合理设置透明度参数，避免过度影响渲染性能。
- **新增** 传送位置查找
  - TeleportUtil的位置查找可能需要遍历多个Y坐标，建议设置合理的搜索范围。
  - 结构查找使用Minecraft内置方法，注意搜索半径对性能的影响。

## 故障排除指南
- 打开界面失败
  - 检查目标玩家是否在线、是否具备对应能力；查看日志中的info/error信息。
  - 参考：[BiotechAPI.java:47-56](file://Biotech/src/main/java/org/biotech/api/BiotechAPI.java#L47-L56)
- Curios槽位为空
  - 确认Curios已正确初始化、玩家拥有对应槽位ID；检查槽位名称常量。
  - 参考：[BiotechAPI.java:72-81](file://Biotech/src/main/java/org/biotech/api/BiotechAPI.java#L72-L81)
- 未鉴定基因无法使用
  - 检查ILootTableManager与IGeneInventoryManager是否可用；确认ServerConfig中的抽取次数配置。
  - 参考：[IUnidentifiedGeneItem.java:30-46](file://Biotech/src/main/java/org/biotech/api/system/gene/core/IUnidentifiedGeneItem.java#L30-L46)
- 网络包未收到
  - 确认包类型标识与StreamCodec一致；检查客户端是否注册了对应处理器。
  - 参考：[SafeZonePayload.java:15-29](file://Beyond/src/main/java/com/pz/beyond/network/SafeZonePayload.java#L15-L29)
- **新增** 边界渲染异常
  - 检查相机和PoseStack参数是否正确传递。
  - 确认纹理资源是否存在且格式正确。
  - 验证透明度参数范围（0-255）。
- **新增** 传送位置查找失败
  - 检查服务器世界对象是否有效。
  - 验证目标位置坐标是否在世界范围内。
  - 确认结构标签是否正确注册。
- **新增** BeyondAPI维度访问失败
  - 确认使用的维度是否为主世界。
  - 检查BeyondAttachInit是否正确初始化。
  - 验证返回值是否为null。

**章节来源**
- [BiotechAPI.java:47-90](file://Biotech/src/main/java/org/biotech/api/BiotechAPI.java#L47-L90)
- [IUnidentifiedGeneItem.java:30-94](file://Biotech/src/main/java/org/biotech/api/system/gene/core/IUnidentifiedGeneItem.java#L30-L94)
- [SafeZonePayload.java:15-30](file://Beyond/src/main/java/com/pz/beyond/network/SafeZonePayload.java#L15-L30)
- [BorderRenderUtil.java:14-76](file://Beyond/src/main/java/com/pz/beyond/api/util/BorderRenderUtil.java#L14-L76)
- [TeleportUtil.java:28-92](file://Beyond/src/main/java/com/pz/beyond/api/util/TeleportUtil.java#L28-L92)
- [BeyondAPI.java:23-59](file://Beyond/src/main/java/com/pz/beyond/api/BeyondAPI.java#L23-59)

## 结论
本文档梳理了BiotechAPI与Beyond模块中的关键接口，明确了其设计原则、使用场景与最佳实践。**新增**的BorderRenderUtil和TeleportUtil工具类为Beyond模块提供了强大的渲染和传送功能，而**增强**的BeyondAPI则扩展了维度数据访问能力。通过统一的门面API与清晰的管理器接口，开发者可以便捷地扩展基因系统、实现安全区规则、自定义网络包以及新的工具类功能。建议在实际开发中遵循接口契约、关注性能与错误处理，并结合日志与崩溃报告进行问题定位。

## 附录
- 版本兼容性与迁移指南
  - NeoForge版本：本API基于NeoForge事件与自定义包协议，请确保运行环境版本兼容。
  - Curios版本：依赖Curios能力与槽位系统，请确保Curios版本与API一致。
  - **新增** Minecraft版本：BorderRenderUtil依赖Minecraft客户端渲染系统，TeleportUtil依赖Minecraft结构系统。
  - 迁移建议：当Curios或NeoForge升级时，优先检查StreamCodec与事件回调签名变化；对BiotechAPI的调用保持不变，但需验证能力注册与槽位ID一致性。
  - **新增** 对于新增的工具类，确保导入正确的包路径和依赖。
- 常用路径速查
  - BiotechAPI：[BiotechAPI.java:21-92](file://Biotech/src/main/java/org/biotech/api/BiotechAPI.java#L21-L92)
  - ISafeZoneRuleListener：[ISafeZoneRuleListener.java:13-41](file://Beyond/src/main/java/com/pz/beyond/safeZoneRule/ISafeZoneRuleListener.java#L13-L41)
  - SafeZonePayload：[SafeZonePayload.java:10-30](file://Beyond/src/main/java/com/pz/beyond/network/SafeZonePayload.java#L10-L30)
  - **新增** BorderRenderUtil：[BorderRenderUtil.java:13-110](file://Beyond/src/main/java/com/pz/beyond/api/util/BorderRenderUtil.java#L13-L110)
  - **新增** TeleportUtil：[TeleportUtil.java:14-94](file://Beyond/src/main/java/com/pz/beyond/api/util/TeleportUtil.java#L14-L94)
  - **增强** BeyondAPI：[BeyondAPI.java:15-62](file://Beyond/src/main/java/com/pz/beyond/api/BeyondAPI.java#L15-L62)
  - 基因接口族：[IGene.java:15-91](file://Biotech/src/main/java/org/biotech/api/system/gene/core/IGene.java#L15-L91)、[IGeneLike.java:3-5](file://Biotech/src/main/java/org/biotech/api/system/gene/core/IGeneLike.java#L3-L5)、[IGeneItem.java:5-7](file://Biotech/src/main/java/org/biotech/api/system/gene/core/IGeneItem.java#L5-L7)、[IUnidentifiedGeneItem.java:23-138](file://Biotech/src/main/java/org/biotech/api/system/gene/core/IUnidentifiedGeneItem.java#L23-L138)、[IXenoItem.java:9-12](file://Biotech/src/main/java/org/biotech/api/system/gene/core/IXenoItem.java#L9-L12)
  - 管理器接口族：[IChoiceManager.java:5-22](file://Biotech/src/main/java/org/biotech/api/system/choice/core/IChoiceManager.java#L5-L22)、[IGeneInventoryManager.java:12-133](file://Biotech/src/main/java/org/biotech/api/system/gene/core/manager/IGeneInventoryManager.java#L12-L133)、[ILootTableManager.java:14-77](file://Biotech/src/main/java/org/biotech/api/system/loot/core/ILootTableManager.java#L14-L77)、[IMergeManager.java:13-52](file://Biotech/src/main/java/org/biotech/api/system/merge/core/IMergeManager.java#L13-L52)
  - **增强** BeyondAPI相关：[BeyondAttachInit.java:23-186](file://Beyond/src/main/java/com/pz/beyond/api/init/BeyondAttachInit.java#L23-L186)