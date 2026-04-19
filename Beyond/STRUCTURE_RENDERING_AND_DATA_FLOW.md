# MiniHUD 结构数据获取与渲染流程说明

> 目标：只说明“结构相关的数据如何获得、如何渲染到屏幕”，不展开 Mod 其他模块结构。

## 1. 关注范围

本文只覆盖以下两块：

1. 结构数据获取（单机集成服 + Servux 联机同步）
2. 结构渲染（更新判定、缓冲构建、最终绘制）

---

## 2. 端到端主链路（从 Tick 到画框）

1. 每个客户端 tick 调用 `ClientTickHandler.onClientTick()`
2. 进入 `RenderHandler.updateData()`，若结构总开关开启，则调用 `DataStorage.updateStructureData()`
3. `DataStorage` 根据环境选择数据来源：
   - 单机/集成服：扫描附近区块结构起点
   - 联机：处理 Servux 下发的结构包并更新缓存
4. 世界渲染阶段调用 `RenderHandler.onRenderWorldLast()` -> `OverlayRenderer.renderOverlays()` -> `RenderContainer.render()`
5. `RenderContainer.update()` 调用 `OverlayRendererStructures.needsUpdate()` / `update()`
6. `OverlayRendererStructures.update()` 生成结构盒顶点数据并上传到 GPU
7. `RenderContainer.draw()` 调用各渲染器 `draw()`，结构框最终显示

---

## 3. 结构数据如何获得

### 3.1 入口与更新节流

关键方法：`DataStorage.updateStructureData()`

- 仅在 `world/player` 存在时执行
- 以 `20 tick` 为周期处理结构更新
- 单机时通过玩家位置 + hysteresis（16）判断是否需要重扫
- 联机时处理服务端同步数据过期和频道注册逻辑

关键状态字段（`DataStorage`）：

- `structuresNeedUpdating`：是否强制重算
- `lastStructureUpdatePos`：上次更新位置
- `structureRendererNeedsUpdate`：通知渲染器重建缓冲
- `hasStructureDataFromServer`：当前是否使用服务端结构数据

### 3.2 单机/集成服路径

关键方法：

- `updateStructureDataFromIntegratedServer()`
- `addStructureDataFromGenerator()`

核心流程：

1. 获取当前维度 `ServerWorld`
2. 在服务端任务中遍历玩家附近 chunk 范围
3. 用 `world.getChunk(cx, cz, ChunkStatus.STRUCTURE_REFERENCES, false)` 获取区块（尽量不触发额外加载）
4. 遍历 `chunk.getStructureStarts()`
5. 结构 ID -> `StructureType.fromStructureId()`
6. 过滤：
   - `type.isEnabled()`（该结构类型开关启用）
   - `start.hasChildren()`
   - `MiscUtils.isStructureWithinRange(...)`
7. 转为 `StructureData.fromStructureStart(...)` 后存入 `structures`
8. 设置 `structureRendererNeedsUpdate = true`

### 3.3 联机（Servux）路径

关键方法：

- `ServuxStructuresHandler.decodeStructuresPacket()`
- `DataStorage.addOrUpdateStructuresFromServer()`

核心流程：

1. 接收 `PACKET_S2C_STRUCTURE_DATA`
2. 通过 `PacketSplitter` 拼完整数据
3. 读取 NBT 的 `Structures` 列表
4. 每条结构转为 `StructureData.fromStructureStartTag(...)`
5. 使用“同结构替换”策略刷新缓存并更新时间戳
6. 清理过期结构 `removeExpiredStructures(...)`
7. 设置 `structureRendererNeedsUpdate = true`

补充：

- 服务端 metadata 在 `receiveServuxStrucutresMetadata()` 处理
- 结构总开关切换时会触发 `registerStructureChannel()` / `unregisterStructureChannel()`（联机场景）

### 3.4 数据模型

`StructureData` 包含：

- `type`：结构类型
- `mainBox`：整体包围盒（由组件包围盒合并）
- `componentBoxes`：各结构片段包围盒
- `refreshTime`：用于联机数据过期管理

`StructureType` 负责：

- 结构 ID 到枚举类型映射
- 每种结构的开关状态读取（`isEnabled()`）

---

## 4. 渲染部分（重点）

## 4.1 渲染器挂接

结构渲染器是 `OverlayRendererStructures.INSTANCE`，由 `RenderContainer` 管理。

在每帧世界渲染中：

1. `RenderContainer.update(...)`：判定和更新
2. `RenderContainer.draw(...)`：真正绘制

## 4.2 何时重建结构渲染数据

关键方法：`OverlayRendererStructures.needsUpdate()`

满足任一条件即更新：

- `DataStorage.structureRendererNeedsUpdate()` 为 true
- 相机/玩家位置相对上次更新位置移动超过 hysteresis（16）

这能避免每帧都重建结构顶点，降低开销。

## 4.3 update 阶段：筛选、建缓冲、上传

关键方法：`OverlayRendererStructures.update(...)`

流程：

1. 计算最大渲染范围 `maxRange = (viewDistance + 4) * 16`
2. `getStructuresToRender(...)` 获取“范围内 + 类型开关开启”的结构列表
3. 首次从空到非空时 `allocateGlResources()`
4. 创建两类缓冲：
   - QUADS（面）
   - DEBUG_LINES（线框）
5. `renderStructureBoxes(...)` 遍历结构并写入缓冲
6. `uploadData(...)` 把 CPU 缓冲上传到 GPU
7. 若本次无结构，释放资源并标记 `wasEmpty = true`

## 4.4 单个结构如何绘制

关键方法：`OverlayRendererStructures.renderStructure(...)`

对每个结构执行：

1. 先画 `mainBox`（主颜色）
2. 再画 `componentBoxes`（组件颜色）
3. 若组件只有 1 个且与主框完全一致，则跳过重复绘制

底层调用：`fi.dy.masa.malilib.render.RenderUtils.drawBox(...)`

即每个包围盒都会生成“半透明面 + 边线”的渲染数据。

## 4.5 draw 阶段：统一状态 + 提交

关键流程：

- `RenderContainer.draw(...)` 统一设置 GL 状态（深度、混合、polygon offset 等）
- 为每个 renderer 应用相机平移矩阵
- 调用 renderer 的 `draw(...)`
- `OverlayRendererBase.draw(...)` 内部逐个 `RenderObject` 提交绘制

因此，结构渲染的性能关键在 update 阶段（是否频繁重建），而不是 draw 阶段（直接消费已上传缓冲）。

## 4.6 穿墙渲染

- 配置项：`Configs.Generic.STRUCTURES_RENDER_THROUGH`
- 作用：切换 `OverlayRendererStructures.setRenderThrough(...)`
- 实现点：`OverlayRendererBase.preRender()` 中关闭深度测试（开启透视效果）

---

## 5. 结构渲染相关开关（只列和本文相关）

1. `RendererToggle.OVERLAY_STRUCTURE_MAIN_TOGGLE`：结构系统总开关
2. `StructureToggle` 各子类型开关：控制具体结构种类是否参与数据筛选和渲染
3. `Configs.Generic.STRUCTURES_RENDER_THROUGH`：结构框是否穿墙显示

---

## 6. 可快速定位的核心文件

- `src/main/java/fi/dy/masa/minihud/event/ClientTickHandler.java`
- `src/main/java/fi/dy/masa/minihud/event/RenderHandler.java`
- `src/main/java/fi/dy/masa/minihud/util/DataStorage.java`
- `src/main/java/fi/dy/masa/minihud/network/ServuxStructuresHandler.java`
- `src/main/java/fi/dy/masa/minihud/util/StructureData.java`
- `src/main/java/fi/dy/masa/minihud/util/StructureType.java`
- `src/main/java/fi/dy/masa/minihud/renderer/OverlayRendererStructures.java`
- `src/main/java/fi/dy/masa/minihud/renderer/RenderContainer.java`
- `src/main/java/fi/dy/masa/minihud/renderer/OverlayRendererBase.java`

---

## 7. 一句话总结

结构功能可以理解为：**`DataStorage` 负责“找结构和维护缓存”，`OverlayRendererStructures` 负责“按范围/开关筛选后构建盒子缓冲并绘制”**。
