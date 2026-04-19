# API参考文档

<cite>
**本文档引用的文件**
- [BiotechAPI.java](file://Biotech/src/main/java/org/biotech/api/BiotechAPI.java)
- [BorderRenderUtil.java](file://Beyond/src/main/java/com/pz/beyond/api/util/BorderRenderUtil.java)
- [TeleportUtil.java](file://Beyond/src/main/java/com/pz/beyond/api/util/TeleportUtil.java)
- [BeyondAPI.java](file://Beyond/src/main/java/com/pz/beyond/api/BeyondAPI.java)
- [BeyondLevelData.java](file://Beyond/src/main/java/com/pz/beyond/api/system/BeyondLevelData.java)
- [BeyondPlayerData.java](file://Beyond/src/main/java/com/pz/beyond/api/system/BeyondPlayerData.java)
- [ProgressDefinition.java](file://Beyond/src/main/java/com/pz/beyond/api/system/definition/ProgressDefinition.java)
- [LevelZoneData.java](file://Beyond/src/main/java/com/pz/beyond/api/system/zone/LevelZoneData.java)
- [PlayerZoneData.java](file://Beyond/src/main/java/com/pz/beyond/api/system/zone/zones/PlayerZoneData.java)
</cite>

## 更新摘要
**变更内容**
- BeyondAPI接口大幅简化，移除了对旧ProgressCatalog、LevelZoneData、ProgressManager的访问方法
- 现在专注于新的数据类访问，提供更简洁的API接口
- 新增BorderRenderUtil工具类API，提供安全边界渲染功能
- 新增TeleportUtil工具类API，提供安全传送位置查找和结构定位功能
- 增强BeyondAPI接口，增加维度数据访问能力

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
- **简化后的BeyondAPI**：专注于新的数据类访问，提供维度数据和玩家数据的获取能力。
- **新增** BorderRenderUtil：提供安全边界渲染工具，支持动态纹理效果和透明度渐变。
- **新增** TeleportUtil：提供安全传送位置查找和结构定位功能。
- **新增** 数据类体系：BeyondLevelData、BeyondPlayerData及其相关定义类构成新的数据访问层。

同时，文档覆盖Biotech模块中的基因系统核心接口族（如IGene、IGeneItem、IUnidentifiedGeneItem、IXenoItem、IGeneLike），以及与之配套的管理器接口（IChoiceManager、IGeneInventoryManager、ILootTableManager、IMergeManager）。每个API均给出参数说明、返回值描述、使用场景、注意事项与最佳实践，并提供与实际代码实现一致的路径引用与图示。

## 项目结构
本仓库包含多个子模块，其中与API相关的关键位置如下：
- Biotech 模块：提供基因系统API、管理器接口、数据模型与客户端渲染等。
- Beyond 模块：提供简化后的API接口、工具类和新的数据类体系。
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
BASimplified["BeyondAPI.java"]
BGLD["BeyondLevelData.java"]
BGPD["BeyondPlayerData.java"]
PROGDEF["ProgressDefinition.java"]
LZD["LevelZoneData.java"]
PZD["PlayerZoneData.java"]
BORDERUTIL["BorderRenderUtil.java"]
TPUTIL["TeleportUtil.java"]
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
BASimplified --> BGLD
BASimplified --> BGPD
BGLD --> PROGDEF
BGLD --> LZD
BGPD --> PZD
BORDERUTIL --> BASimplified
TPUTIL --> BASimplified
```

**图表来源**
- [BiotechAPI.java:1-92](file://Biotech/src/main/java/org/biotech/api/BiotechAPI.java#L1-L92)
- [BeyondAPI.java:1-27](file://Beyond/src/main/java/com/pz/beyond/api/BeyondAPI.java#L1-L27)
- [BeyondLevelData.java:1-69](file://Beyond/src/main/java/com/pz/beyond/api/system/BeyondLevelData.java#L1-L69)
- [BeyondPlayerData.java:1-97](file://Beyond/src/main/java/com/pz/beyond/api/system/BeyondPlayerData.java#L1-L97)
- [ProgressDefinition.java:1-85](file://Beyond/src/main/java/com/pz/beyond/api/system/definition/ProgressDefinition.java#L1-L85)
- [LevelZoneData.java:1-83](file://Beyond/src/main/java/com/pz/beyond/api/system/zone/LevelZoneData.java#L1-L83)
- [PlayerZoneData.java:1-64](file://Beyond/src/main/java/com/pz/beyond/api/system/zone/zones/PlayerZoneData.java#L1-L64)
- [BorderRenderUtil.java:1-110](file://Beyond/src/main/java/com/pz/beyond/api/util/BorderRenderUtil.java#L1-L110)
- [TeleportUtil.java:1-94](file://Beyond/src/main/java/com/pz/beyond/api/util/TeleportUtil.java#L1-L94)

## 核心组件
本节概述简化后的BeyondAPI与其他关键接口，帮助快速定位所需能力。

- **简化后的BeyondAPI**
  - 功能：提供维度数据和玩家数据的获取能力，移除了对旧ProgressCatalog、LevelZoneData、ProgressManager的访问方法。
  - 关键方法：getBeyondLevelData(Level)、getBeyondPlayerData(Player)。
  - 使用场景：获取主世界维度数据、玩家Beyond模块数据。
  - 注意事项：仅支持主世界维度，其他维度返回默认数据。

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

- **新增** 数据类体系
  - BeyondLevelData：主世界维度数据容器，包含进度定义和区域数据。
  - BeyondPlayerData：玩家Beyond模块数据，聚合所有Beyond相关数据。
  - ProgressDefinition：关卡数据定义，包含场景和遭遇映射。
  - LevelZoneData：世界区域数据，挂载在Level上。
  - PlayerZoneData：玩家当前所在区域数据。

**章节来源**
- [BeyondAPI.java:15-26](file://Beyond/src/main/java/com/pz/beyond/api/BeyondAPI.java#L15-L26)
- [BorderRenderUtil.java:13-110](file://Beyond/src/main/java/com/pz/beyond/api/util/BorderRenderUtil.java#L13-L110)
- [TeleportUtil.java:14-94](file://Beyond/src/main/java/com/pz/beyond/api/util/TeleportUtil.java#L14-L94)
- [BeyondLevelData.java:24-68](file://Beyond/src/main/java/com/pz/beyond/api/system/BeyondLevelData.java#L24-L68)
- [BeyondPlayerData.java:27-96](file://Beyond/src/main/java/com/pz/beyond/api/system/BeyondPlayerData.java#L27-L96)
- [ProgressDefinition.java:23-84](file://Beyond/src/main/java/com/pz/beyond/api/system/definition/ProgressDefinition.java#L23-L84)
- [LevelZoneData.java:22-83](file://Beyond/src/main/java/com/pz/beyond/api/system/zone/LevelZoneData.java#L22-L83)
- [PlayerZoneData.java:28-63](file://Beyond/src/main/java/com/pz/beyond/api/system/zone/zones/PlayerZoneData.java#L28-L63)

## 架构总览
下图展示了简化后的BeyondAPI与新的数据类体系之间的关系，以及与BiotechAPI的协作关系。**新增**的BorderRenderUtil和TeleportUtil工具类现在作为独立的实用工具类存在，而BeyondAPI现在提供了更简洁的维度数据访问能力。

```mermaid
classDiagram
class BeyondAPI {
+getBeyondLevelData(level) BeyondLevelData
+getBeyondPlayerData(player) BeyondPlayerData
}
class BeyondLevelData {
+dimension ResourceKey~Level~
+progressDefinitions Map~ResourceLocation,ProgressDefinition~
+levelZoneData LevelZoneData
+CODEC Codec~BeyondLevelData~
+STREAM_CODEC StreamCodec~RegistryFriendlyByteBuf,BeyondLevelData~
}
class BeyondPlayerData {
+playerUUID UUID
+playerZoneData PlayerZoneData
+getPlayer() ServerPlayer
+getPlayerZoneData() PlayerZoneData
+CODEC Codec~BeyondPlayerData~
+STREAM_CODEC StreamCodec~RegistryFriendlyByteBuf,BeyondPlayerData~
}
class ProgressDefinition {
+identifier ResourceLocation
+scenes SceneDefinition[]
+encounters EncounterMapping[]
+CODEC Codec~ProgressDefinition~
+STREAM_CODEC StreamCodec~RegistryFriendlyByteBuf,ProgressDefinition~
}
class LevelZoneData {
+zonePos Map~Long,ZoneType~
+zoneData Map~ZoneType,ZoneData~
+getZoneData(chunkPos) ZoneData
+getZoneData(pos) ZoneData
+CODEC Codec~LevelZoneData~
+STREAM_CODEC StreamCodec~RegistryFriendlyByteBuf,LevelZoneData~
}
class PlayerZoneData {
+currentZone ZoneType
+player ServerPlayer
+CODEC Codec~PlayerZoneData~
+STREAM_CODEC StreamCodec~RegistryFriendlyByteBuf,PlayerZoneData~
}
class BorderRenderUtil {
+render(centerX, centerZ, radius, r, g, b, bottomAlpha, topAlpha, tex, camera, poseStack) void
+render(minX, minZ, maxX, maxZ, r, g, b, bottomAlpha, topAlpha, tex, camera, poseStack) void
}
class TeleportUtil {
+findSafeTeleportPos(level, target) BlockPos
+isSafeStandingPosition(level, pos) boolean
+findNearestStructure(level, structureTag, searchRadius) BlockPos
+findNearestStructure(level, structureTag, searchRadius, defaultPos) BlockPos
}
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
BeyondAPI --> BeyondLevelData : "获取"
BeyondAPI --> BeyondPlayerData : "获取"
BeyondLevelData --> ProgressDefinition : "包含"
BeyondLevelData --> LevelZoneData : "包含"
BeyondPlayerData --> PlayerZoneData : "包含"
BorderRenderUtil --> BeyondAPI : "使用"
TeleportUtil --> BeyondAPI : "使用"
BiotechAPI -.-> BeyondAPI : "协作"
```

**图表来源**
- [BeyondAPI.java:15-26](file://Beyond/src/main/java/com/pz/beyond/api/BeyondAPI.java#L15-L26)
- [BeyondLevelData.java:24-68](file://Beyond/src/main/java/com/pz/beyond/api/system/BeyondLevelData.java#L24-L68)
- [BeyondPlayerData.java:27-96](file://Beyond/src/main/java/com/pz/beyond/api/system/BeyondPlayerData.java#L27-L96)
- [ProgressDefinition.java:23-84](file://Beyond/src/main/java/com/pz/beyond/api/system/definition/ProgressDefinition.java#L23-L84)
- [LevelZoneData.java:22-83](file://Beyond/src/main/java/com/pz/beyond/api/system/zone/LevelZoneData.java#L22-L83)
- [PlayerZoneData.java:28-63](file://Beyond/src/main/java/com/pz/beyond/api/system/zone/zones/PlayerZoneData.java#L28-L63)
- [BorderRenderUtil.java:13-110](file://Beyond/src/main/java/com/pz/beyond/api/util/BorderRenderUtil.java#L13-L110)
- [TeleportUtil.java:14-94](file://Beyond/src/main/java/com/pz/beyond/api/util/TeleportUtil.java#L14-L94)
- [BiotechAPI.java:18-92](file://Biotech/src/main/java/org/biotech/api/BiotechAPI.java#L18-L92)

## 详细组件分析

### 简化后的BeyondAPI 接口详解
- 设计原则
  - 简化单一职责：专注于数据类访问，移除复杂的进度管理方法。
  - 类型安全：提供明确的数据类返回类型，避免类型转换。
  - 委托模式：将具体实现委托给数据类和相关管理器。
- 公共方法
  - getBeyondLevelData(Level): 获取维度级别的Beyond数据。
  - getBeyondPlayerData(Player): 获取玩家级别的Beyond数据。
- 参数与返回
  - Level：世界对象；Player：玩家对象。
  - 返回值：对应的Beyond数据类实例。
- 使用示例（路径）
  - 获取维度数据：[BeyondAPI.java:16-18](file://BeyondAPI.java#L16-L18)
  - 获取玩家数据：[BeyondAPI.java:22-25](file://BeyondAPI.java#L22-L25)
- 最佳实践
  - 在调用前检查返回值的有效性。
  - 使用数据类的属性访问而非直接依赖具体实现。
  - 注意维度限制，仅在主世界使用相关功能。

**章节来源**
- [BeyondAPI.java:15-26](file://Beyond/src/main/java/com/pz/beyond/api/BeyondAPI.java#L15-L26)

### BeyondLevelData 数据类详解
- 设计原则
  - 数据聚合：将所有维度相关数据聚合在一个类中。
  - 序列化支持：提供Codec和StreamCodec支持网络传输和持久化。
  - 主世界限定：明确只存储到主世界的数据。
- 核心属性
  - dimension：维度标识，默认Level.OVERWORLD。
  - progressDefinitions：进度定义映射。
  - levelZoneData：区域数据容器。
- 编解码支持
  - CODEC：基于RecordCodecBuilder的序列化支持。
  - STREAM_CODEC：基于StreamCodec的网络传输支持。
- 使用场景
  - 存储和传输维度级别的Beyond数据。
  - 作为BeyondAPI的主要数据载体。

**章节来源**
- [BeyondLevelData.java:24-68](file://Beyond/src/main/java/com/pz/beyond/api/system/BeyondLevelData.java#L24-L68)

### BeyondPlayerData 数据类详解
- 设计原则
  - 延迟初始化：玩家区域数据按需创建。
  - 双端支持：同时支持客户端和服务器端使用。
  - 服务器解析：提供服务器端玩家解析功能。
- 核心属性
  - playerUUID：玩家唯一标识。
  - playerZoneData：玩家区域数据（延迟初始化）。
  - serverPlayer：服务器端玩家引用。
- 功能方法
  - getPlayer()：获取服务器端玩家实例。
  - getPlayerZoneData()：获取或创建玩家区域数据。
- 编解码支持
  - CODEC和STREAM_CODEC：完整的序列化和网络传输支持。
- 使用场景
  - 存储和传输玩家级别的Beyond数据。
  - 作为玩家状态管理的核心数据容器。

**章节来源**
- [BeyondPlayerData.java:27-96](file://Beyond/src/main/java/com/pz/beyond/api/system/BeyondPlayerData.java#L27-L96)

### ProgressDefinition 数据定义详解
- 设计原则
  - 结构化定义：将关卡数据分解为场景和遭遇两个层面。
  - 映射关系：通过EncounterMapping建立遭遇类型与定义的关系。
  - 序列化支持：提供完整的编解码支持。
- 核心结构
  - identifier：关卡标识符。
  - scenes：场景定义列表。
  - encounters：遭遇映射列表。
- EncounterMapping 内部类
  - encounterType：遭遇类型枚举。
  - encounterDefinition：遭遇定义对象。
- 使用场景
  - 定义关卡的场景结构。
  - 配置不同类型的遭遇及其定义。

**章节来源**
- [ProgressDefinition.java:23-84](file://Beyond/src/main/java/com/pz/beyond/api/system/definition/ProgressDefinition.java#L23-L84)

### LevelZoneData 区域数据详解
- 设计原则
  - 区块级映射：每个区块只能有一种区域类型。
  - 快速查询：通过区块坐标快速定位区域数据。
  - 序列化支持：提供完整的编解码支持。
- 核心结构
  - zonePos：区块坐标到区域类型的映射。
  - zoneData：区域类型到区域数据的映射。
- 查询方法
  - getZoneData(ChunkPos)：通过区块坐标获取区域数据。
  - getZoneData(BlockPos)：通过方块坐标获取区域数据。
- 使用场景
  - 区域系统的核心数据结构。
  - 快速判断玩家所在区域类型。

**章节来源**
- [LevelZoneData.java:22-83](file://Beyond/src/main/java/com/pz/beyond/api/system/zone/LevelZoneData.java#L22-L83)

### PlayerZoneData 玩家区域数据详解
- 设计原则
  - 简化设计：只存储玩家当前所在区域的基本信息。
  - 序列化支持：提供完整的编解码支持。
  - 延迟初始化：支持客户端和服务器端的不同初始化方式。
- 核心属性
  - currentZone：当前区域类型（可为空）。
  - player：关联的服务器端玩家（可为空）。
- 使用场景
  - 跟踪玩家当前所在区域。
  - 区域事件触发的基础数据。

**章节来源**
- [PlayerZoneData.java:28-63](file://Beyond/src/main/java/com/pz/beyond/api/system/zone/zones/PlayerZoneData.java#L28-L63)

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

## 依赖分析
- 外部依赖
  - NeoForge事件系统：LivingDamageEvent、MobSpawnEvent等。
  - Curios API：IDynamicStackHandler、ICurioItem、SlotContext等。
  - Minecraft网络编解码：StreamCodec、CustomPacketPayload。
  - **新增** Minecraft客户端渲染系统：Blaze3D、PoseStack、BufferBuilder等。
  - **新增** Minecraft结构系统：Structure、TagKey等。
- 内部依赖
  - BeyondAPI依赖数据类进行数据访问。
  - **新增** BorderRenderUtil依赖BeyondAPI的渲染系统。
  - **新增** TeleportUtil依赖BeyondAPI的维度数据访问。
  - BiotechAPI作为独立模块，不依赖BeyondAPI。
- 耦合与内聚
  - BeyondAPI作为门面，降低上层对底层实现的耦合。
  - 各数据类职责清晰，便于替换与扩展。
  - **新增** 工具类采用独立设计，降低模块间耦合。

**图表来源**
- [BeyondAPI.java:3-7](file://Beyond/src/main/java/com/pz/beyond/api/BeyondAPI.java#L3-L7)
- [BorderRenderUtil.java:3-11](file://Beyond/src/main/java/com/pz/beyond/api/util/BorderRenderUtil.java#L3-L11)
- [TeleportUtil.java:3-7](file://Beyond/src/main/java/com/pz/beyond/api/util/TeleportUtil.java#L3-L7)
- [BeyondLevelData.java:3-14](file://Beyond/src/main/java/com/pz/beyond/api/system/BeyondLevelData.java#L3-L14)
- [BeyondPlayerData.java:3-19](file://Beyond/src/main/java/com/pz/beyond/api/system/BeyondPlayerData.java#L3-L19)

**章节来源**
- [BeyondAPI.java:3-7](file://Beyond/src/main/java/com/pz/beyond/api/BeyondAPI.java#L3-L7)
- [BorderRenderUtil.java:3-11](file://Beyond/src/main/java/com/pz/beyond/api/util/BorderRenderUtil.java#L3-L11)
- [TeleportUtil.java:3-7](file://Beyond/src/main/java/com/pz/beyond/api/util/TeleportUtil.java#L3-L7)
- [BeyondLevelData.java:3-14](file://Beyond/src/main/java/com/pz/beyond/api/system/BeyondLevelData.java#L3-L14)
- [BeyondPlayerData.java:3-19](file://Beyond/src/main/java/com/pz/beyond/api/system/BeyondPlayerData.java#L3-L19)

## 性能考虑
- 简化后的API调用
  - BeyondAPI方法调用简单直接，性能开销极小。
  - 数据类访问通过Level和Player的getData方法实现，性能稳定。
- 客户端渲染
  - BorderRenderUtil渲染涉及大量顶点计算，建议在必要时才调用。
  - 合理设置透明度参数，避免过度影响渲染性能。
- 传送位置查找
  - TeleportUtil的位置查找可能需要遍历多个Y坐标，建议设置合理的搜索范围。
  - 结构查找使用Minecraft内置方法，注意搜索半径对性能的影响。
- 数据序列化
  - 数据类的Codec和StreamCodec经过优化，适合频繁的网络传输。
  - 建议在批量操作时合并序列化调用以提高效率。

## 故障排除指南
- BeyondAPI调用失败
  - 检查传入的Level或Player对象是否有效。
  - 确认数据类是否正确注册和初始化。
  - 参考：[BeyondAPI.java:16-25](file://Beyond/src/main/java/com/pz/beyond/api/BeyondAPI.java#L16-L25)
- 数据类访问异常
  - 检查Level和Player的getData方法是否正常工作。
  - 确认数据类的注册是否正确完成。
  - 验证维度限制，确保在主世界使用。
- BorderRenderUtil渲染异常
  - 检查相机和PoseStack参数是否正确传递。
  - 确认纹理资源是否存在且格式正确。
  - 验证透明度参数范围（0-255）。
- TeleportUtil传送位置查找失败
  - 检查服务器世界对象是否有效。
  - 验证目标位置坐标是否在世界范围内。
  - 确认结构标签是否正确注册。
- BiotechAPI调用失败
  - 检查目标玩家是否在线、是否具备对应能力；查看日志中的info/error信息。
  - 参考：[BiotechAPI.java:47-90](file://Biotech/src/main/java/org/biotech/api/BiotechAPI.java#L47-L90)

**章节来源**
- [BeyondAPI.java:16-25](file://Beyond/src/main/java/com/pz/beyond/api/BeyondAPI.java#L16-L25)
- [BorderRenderUtil.java:14-76](file://Beyond/src/main/java/com/pz/beyond/api/util/BorderRenderUtil.java#L14-L76)
- [TeleportUtil.java:28-92](file://Beyond/src/main/java/com/pz/beyond/api/util/TeleportUtil.java#L28-L92)
- [BiotechAPI.java:47-90](file://Biotech/src/main/java/org/biotech/api/BiotechAPI.java#L47-L90)

## 结论
本文档梳理了简化后的BeyondAPI与新的数据类体系，明确了其设计原则、使用场景与最佳实践。**简化后的BeyondAPI**移除了对旧ProgressCatalog、LevelZoneData、ProgressManager的访问方法，专注于新的数据类访问，提供了更简洁和类型安全的API接口。**新增**的BorderRenderUtil和TeleportUtil工具类为Beyond模块提供了强大的渲染和传送功能，而新的数据类体系（BeyondLevelData、BeyondPlayerData等）构成了完整的数据访问层。通过统一的门面API与清晰的数据类接口，开发者可以便捷地扩展Beyond模块功能。建议在实际开发中遵循接口契约、关注性能与错误处理，并结合日志与调试工具进行问题定位。

## 附录
- 版本兼容性与迁移指南
  - NeoForge版本：本API基于NeoForge事件与自定义包协议，请确保运行环境版本兼容。
  - Curios版本：依赖Curios能力与槽位系统，请确保Curios版本与API一致。
  - **新增** Minecraft版本：BorderRenderUtil依赖Minecraft客户端渲染系统，TeleportUtil依赖Minecraft结构系统。
  - 迁移建议：从旧的BeyondAPI调用迁移到新的数据类访问；当Curios或NeoForge升级时，优先检查StreamCodec与事件回调签名变化。
  - **新增** 对于新增的工具类，确保导入正确的包路径和依赖。
- 常用路径速查
  - 简化后的BeyondAPI：[BeyondAPI.java:15-26](file://Beyond/src/main/java/com/pz/beyond/api/BeyondAPI.java#L15-L26)
  - BeyondLevelData：[BeyondLevelData.java:24-68](file://Beyond/src/main/java/com/pz/beyond/api/system/BeyondLevelData.java#L24-L68)
  - BeyondPlayerData：[BeyondPlayerData.java:27-96](file://Beyond/src/main/java/com/pz/beyond/api/system/BeyondPlayerData.java#L27-L96)
  - ProgressDefinition：[ProgressDefinition.java:23-84](file://Beyond/src/main/java/com/pz/beyond/api/system/definition/ProgressDefinition.java#L23-L84)
  - LevelZoneData：[LevelZoneData.java:22-83](file://Beyond/src/main/java/com/pz/beyond/api/system/zone/LevelZoneData.java#L22-L83)
  - PlayerZoneData：[PlayerZoneData.java:28-63](file://Beyond/src/main/java/com/pz/beyond/api/system/zone/zones/PlayerZoneData.java#L28-L63)
  - **新增** BorderRenderUtil：[BorderRenderUtil.java:13-110](file://Beyond/src/main/java/com/pz/beyond/api/util/BorderRenderUtil.java#L13-L110)
  - **新增** TeleportUtil：[TeleportUtil.java:14-94](file://Beyond/src/main/java/com/pz/beyond/api/util/TeleportUtil.java#L14-L94)
  - BiotechAPI：[BiotechAPI.java:18-92](file://Biotech/src/main/java/org/biotech/api/BiotechAPI.java#L18-L92)