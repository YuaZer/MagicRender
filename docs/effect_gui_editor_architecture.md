# MagicRender 游戏内特效 GUI 编辑器实现设计

本文档设计 MagicRender 的游戏内 Unity 风格特效编辑器。目标是在游戏内安全编辑特效配置、实时预览世界空间特效、导出 JSON 到本地配置目录，并遵守单人/多人权限边界。

## 1. 设计目标

编辑器需要面向不会写代码的用户，同时保留开发者需要的精确控制。

核心能力：

- 编辑特效基础信息：id、分组、启用状态、持续时间、重要级别、可见距离。
- 编辑特效类型：Trail、Beam、Magic Circle，后续扩展 Particle、Aura、Shockwave、Screen Effect、Offscreen。
- 编辑运动轨迹：follow、orbit、helix，以及半径、角速度、垂直振幅、相位。
- 编辑材质表现：颜色、宽度、半径、透明/加色混合、朝向、分段数量。
- 实时预览：不写入文件也能临时播放当前草稿效果。
- 导出配置：写入 `config/magicrender/effects/*.json`。
- 同名检查：导出前检测本地是否已有同名配置文件和同名 effect id。
- 热加载：导出成功后可选择立即 reload，使新配置进入运行时。
- 权限控制：单人游戏可直接使用；多人服务器仅权限等级 >= 2 的玩家可使用。

非目标：

- 第一版不实现完整 Unity VFX Graph 节点编辑器。
- 第一版不实现屏幕空间 Bloom/FBO 编辑。
- 第一版不让配置直接暴露 OpenGL、Framebuffer、BufferBuilder 等底层细节。
- 第一版不在多人服务器上静默写服务端配置；编辑器导出的是本地客户端配置。

## 2. 与现有文档的关系

编辑器基于以下已有设计：

- `docs/independent_world_effect_rendering_architecture.md`
  - 编辑器只生成独立世界空间特效配置，不修改 Minecraft 世界渲染器。
  - 默认聚焦第一层世界空间特效。
- `docs/unity_effect_layer_1_world_space.md`
  - 编辑对象围绕 Billboard、Trail、Beam、Magic Circle、Mesh、Area Indicator 扩展。
- `docs/unity_effect_motion_trajectory_design.md`
  - Trail/Beam 采用采样点、ribbon mesh、宽度/颜色曲线、实体锚点。
- `docs/motion_trajectory_config_structure.md`
  - 第一版 GUI 直接对应当前 JSON 字段。
- `docs/config_hot_reload_implementation_prompt.md`
  - 导出必须保守、安全、可热加载，单个配置错误不影响其它配置。

## 3. 权限模型

### 3.1 单人游戏

判断条件：

```kotlin
Minecraft.getInstance().isSingleplayer
```

或当前连接为本地集成服务器。

行为：

- 允许直接打开编辑器。
- 允许实时预览。
- 允许导出到本地 `config/magicrender/effects/`。
- 导出后允许直接调用客户端 reload。

### 3.2 多人游戏

多人游戏不能只靠客户端判断权限。必须向服务器请求权限确认。

权限要求：

```text
permission level >= 2
```

建议复用 `server.json`：

```json
{
  "permissions": {
    "reloadRequiresLevel": 2,
    "spawnTestEffectRequiresLevel": 2,
    "openEditorRequiresLevel": 2
  }
}
```

如果暂时不新增字段，第一版可复用：

```text
server.permissions.spawnTestEffectRequiresLevel
```

多人行为：

- 客户端执行 `/mrender editor` 或按键打开编辑器。
- 客户端发送 `EditorPermissionRequestPayload`。
- 服务端检查 `source.player.hasPermissions(2)`。
- 服务端返回 `EditorPermissionResponsePayload(allowed, reason)`。
- allowed 为 true 才打开编辑器。
- denied 时显示系统消息，不打开编辑器。

### 3.3 为什么不能只做客户端判断

多人环境下，客户端无法可信判断自己是否 OP。即使编辑器只导出本地配置，实时预览也可能造成服务器玩法测试混乱，因此打开入口需要服务器授权。

## 4. 入口设计

### 4.1 命令

客户端命令：

```text
/mrender editor
/mrender editor new
/mrender editor edit <effectId>
/mrender editor preview <effectId>
```

服务端命令提示：

```text
/magicrender editor
```

如果该命令被服务端解析，返回提示：

```text
MagicRender editor is a client-side screen. Use /mrender editor.
```

### 4.2 按键绑定

建议新增客户端按键：

```text
key.magicrender.open_editor
默认未绑定
```

按键行为：

- 单人：直接打开。
- 多人：发起权限请求。

## 5. 包结构建议

```text
src/client/kotlin/io/github/yuazer/magicrender/client/editor/
├─ EffectEditorScreen.kt
├─ EffectEditorState.kt
├─ EffectEditorDraft.kt
├─ EffectEditorAccess.kt
├─ EffectEditorExporter.kt
├─ EffectEditorPreview.kt
├─ EffectEditorValidation.kt
├─ widgets/
│  ├─ LabeledEditBox.kt
│  ├─ NumericField.kt
│  ├─ ToggleRow.kt
│  ├─ ColorField.kt
│  ├─ EffectTypeTabs.kt
│  └─ SectionPanel.kt
└─ network/
   ├─ EditorPermissionPayload.kt
   ├─ EditorPermissionClient.kt
   └─ EditorPermissionServer.kt
```

如果后续编辑器变复杂，可拆成：

```text
client/editor/model/
client/editor/screen/
client/editor/widget/
client/editor/export/
client/editor/preview/
```

## 6. 编辑器数据模型

编辑器不应直接修改 `EffectDefinition` 运行时对象。需要使用草稿模型：

```kotlin
data class EffectEditorDraft(
    var version: Int = 1,
    var id: String = "magicrender:new_effect",
    var enabled: Boolean = true,
    var group: String = "combat",
    var durationTicks: Int = 80,
    var importance: String = "normal",
    var visibility: VisibilityDraft = VisibilityDraft(),
    var components: ComponentDrafts = ComponentDrafts()
)
```

组件草稿：

```kotlin
data class ComponentDrafts(
    var trail: TrailDraft = TrailDraft(enabled = false),
    var beam: BeamDraft = BeamDraft(enabled = false),
    var magicCircle: MagicCircleDraft = MagicCircleDraft(enabled = false)
)
```

Trail 草稿字段对应当前配置：

```kotlin
data class TrailDraft(
    var enabled: Boolean,
    var style: String = "ribbon",
    var texture: String = "minecraft:textures/misc/white.png",
    var renderMode: String = "face_camera",
    var blendMode: String = "additive",
    var lifetimeTicks: Int = 42,
    var maxPoints: Int = 96,
    var minSampleDistance: Double = 0.03,
    var maxSegmentLength: Double = 0.28,
    var maxInsertedPointsPerTick: Int = 4,
    var sampleEveryTick: Boolean = true,
    var widthStart: Double = 0.24,
    var widthEnd: Double = 0.02,
    var colorStart: String = "#FFF6B640",
    var colorEnd: String = "#22FF4FB8",
    var motion: TrailMotionDraft = TrailMotionDraft()
)
```

Motion 草稿：

```kotlin
data class TrailMotionDraft(
    var mode: String = "helix",
    var radius: Double = 0.9,
    var angularSpeed: Double = 16.0,
    var verticalAmplitude: Double = 0.42,
    var verticalSpeed: Double = 10.0,
    var phase: Double = 0.0
)
```

Magic Circle 草稿：

```kotlin
data class MagicCircleDraft(
    var enabled: Boolean,
    var style: String = "arcane_gate",
    var radius: Double = 1.65,
    var color: String = "#CCFFD24A",
    var thickness: Double = 0.055,
    var segments: Int = 128,
    var facing: String = "face_camera",
    var rotationSpeed: Double = 1.2,
    var innerRadiusScale: Double = 0.68,
    var glyphs: Int = 18,
    var blendMode: String = "additive"
)
```

Beam 草稿：

```kotlin
data class BeamDraft(
    var enabled: Boolean,
    var style: String = "mana",
    var texture: String = "minecraft:textures/misc/white.png",
    var width: Double = 0.18,
    var segments: Int = 8,
    var noise: Double = 0.08,
    var colorStart: String = "#AAFFFFFF",
    var colorEnd: String = "#4488FFFF",
    var blendMode: String = "additive"
)
```

## 7. GUI 页面结构

第一版建议做一个单屏编辑器，而不是复杂多窗口。

```text
EffectEditorScreen
├─ 顶部栏
│  ├─ effect id
│  ├─ loaded/new 标记
│  ├─ validation 状态
│  ├─ Preview
│  ├─ Export
│  └─ Close
├─ 左侧导航
│  ├─ 基础
│  ├─ Trail
│  ├─ Beam
│  ├─ Magic Circle
│  ├─ 运动轨迹
│  ├─ 可见性
│  └─ 导出
├─ 中央属性面板
│  ├─ 当前分类字段
│  ├─ 数字输入
│  ├─ 开关
│  ├─ 下拉选项
│  ├─ 颜色输入
│  └─ clamp 提示
└─ 底部状态栏
   ├─ 保存路径
   ├─ 同名文件提示
   ├─ warnings/errors
   └─ 当前预览 handle
```

### 7.1 基础页

字段：

- `id`
- `enabled`
- `group`
- `durationTicks`
- `importance`
- `drawDistance`
- `hideWhenShadersConflict`

### 7.2 Trail 页

字段：

- `trail.enabled`
- `style`
- `texture`
- `renderMode`
- `blendMode`
- `lifetimeTicks`
- `maxPoints`
- `sampleEveryTick`
- `minSampleDistance`
- `maxSegmentLength`
- `maxInsertedPointsPerTick`
- `width.start`
- `width.end`
- `color.start`
- `color.end`

### 7.3 运动轨迹页

字段：

- `motion.mode`: follow / orbit / helix
- `radius`
- `angularSpeed`
- `verticalAmplitude`
- `verticalSpeed`
- `phase`

交互：

- `follow` 时隐藏或禁用 radius/角速度字段。
- `orbit` 时启用 radius/angularSpeed/phase。
- `helix` 时额外启用 verticalAmplitude/verticalSpeed。

### 7.4 Magic Circle 页

字段：

- `magicCircle.enabled`
- `style`
- `radius`
- `color`
- `thickness`
- `segments`
- `facing`
- `rotationSpeed`
- `innerRadiusScale`
- `glyphs`
- `blendMode`

### 7.5 Beam 页

字段：

- `beam.enabled`
- `style`
- `texture`
- `width`
- `segments`
- `noise`
- `color.start`
- `color.end`
- `blendMode`

## 8. 控件设计

使用原版 GUI 控件即可：

- `EditBox`: 文本、资源 ID、颜色。
- `Button`: 导出、预览、关闭、重置。
- 自定义 `NumericField`: 包装 `EditBox`，输入时解析数字并 clamp。
- 自定义 `ToggleRow`: 开关字段。
- 自定义 `OptionCycleButton`: mode、blendMode、renderMode、facing。
- 自定义 `ColorField`: 支持 `#AARRGGBB`，错误时红色边框或状态栏 warning。

不要在编辑器内暴露底层实现名，例如 BufferBuilder、FBO、GL state。

## 9. 实时预览设计

### 9.1 预览目标

实时预览不写文件，不污染正式配置。它从 `EffectEditorDraft` 生成临时 `EffectDefinition`，注册到一个临时预览表，或直接调用预览专用 spawn。

第一版建议：

```text
EffectEditorDraft
    -> 临时 EffectDefinition
    -> EffectEditorPreview.previewDefinition
    -> MagicCircleManager / MotionEffectManager 预览 spawn
```

### 9.2 预览锚点

支持三种预览锚点：

- 玩家自身：默认，适合单人快速测试。
- 准星实体：如果当前瞄准实体，则绑定实体。
- 固定世界点：玩家前方 3 格位置。

GUI 可提供切换：

```text
Preview Target: Self | Crosshair Entity | In Front Of Camera
```

### 9.3 预览生命周期

要求：

- 每次点击 Preview 前，停止上一次预览 handle。
- 关闭编辑器时停止预览。
- 切换世界或断开连接时清理预览。
- 预览时使用短生命周期，不生成无限持续效果。

接口建议：

```kotlin
object EffectEditorPreview {
    fun preview(draft: EffectEditorDraft, target: PreviewTarget): PreviewHandles
    fun stopCurrent()
}
```

### 9.4 预览配置来源

当前 `MotionEffectManager.spawnTrail(effectId, anchor)` 只能从 `MagicRenderConfigManager.current.effects` 查配置。为了预览草稿，需要补一个运行时重载：

```kotlin
fun MotionEffectManager.spawnTrail(definition: EffectDefinition, anchor: TrailAnchor): Long?
fun MagicCircleManager.spawn(definition: EffectDefinition, anchor: TrailAnchor): Long?
fun MotionEffectManager.spawnBeam(definition: EffectDefinition, from: TrailAnchor, to: TrailAnchor): Long?
```

这样预览不用写文件再 reload。

## 10. 导出设计

### 10.1 导出路径

导出目录：

```text
config/magicrender/effects/
```

文件名从 effect id path 生成：

```text
magicrender:entity_arcane_stream -> entity_arcane_stream.json
other_namespace:my_effect -> other_namespace__my_effect.json
```

建议避免跨 namespace 覆盖：

```text
<namespace>__<path>.json
```

但为了兼容当前默认文件，也允许默认 namespace `magicrender` 使用：

```text
entity_arcane_stream.json
```

### 10.2 同名检查

导出前检查：

1. 文件是否存在。
2. 当前已加载配置中是否存在同 id。
3. 目标文件里的 `id` 是否和草稿 id 不一致。

结果分级：

```text
OK: 不存在同名文件和同 id。
WARNING: 文件存在，但 id 相同，可覆盖。
ERROR: 文件存在且 id 不同，默认禁止覆盖。
ERROR: id 无效。
ERROR: JSON 无法生成。
```

GUI 行为：

- 首次点击 Export，如果存在同名文件，显示确认状态，不立即覆盖。
- 用户再次点击 Confirm Overwrite 才覆盖。
- 不允许覆盖 id 不同的文件，除非提供高级选项并明确确认。

### 10.3 JSON 生成

必须使用结构化 JSON API，例如 Gson `JsonObject`，不要字符串拼接。

导出后格式：

- UTF-8。
- 缩进 2 空格。
- 字段顺序固定，便于用户阅读。

### 10.4 导出后操作

导出成功后提供：

- `Reload Now`
- `Open Folder`
- `Continue Editing`

第一版不建议调用系统文件管理器，避免跨平台/沙箱问题。可以先只提供 reload。

## 11. 校验规则

编辑器校验必须与配置解析 clamp 一致。

基础：

```text
id: [a-z0-9_.-]+:[a-z0-9_./-]+
durationTicks: 1..1200
drawDistance: 0..importantDrawDistance
```

Trail：

```text
lifetimeTicks: 1..200
maxPoints: 2..128
minSampleDistance: 0.01..4.0
maxSegmentLength: 0.05..8.0
maxInsertedPointsPerTick: 0..8
width.start/end: 0.0..8.0
```

Motion：

```text
radius: 0.0..16.0
angularSpeed: -72.0..72.0
verticalAmplitude: 0.0..16.0
verticalSpeed: -72.0..72.0
phase: -360.0..360.0
```

Magic Circle：

```text
radius: 0.1..32.0
thickness: 0.01..2.0
segments: 16..256
rotationSpeed: -16.0..16.0
innerRadiusScale: 0.1..0.95
glyphs: 0..64
```

Beam：

```text
width: 0.01..4.0
segments: 1..64
noise: 0.0..4.0
```

颜色：

```text
#RRGGBB
#AARRGGBB
```

内部统一导出为 `#AARRGGBB`。

## 12. 多人权限网络设计

### 12.1 Payload

```kotlin
data class EditorPermissionRequestPayload(
    val requestId: UUID
)

data class EditorPermissionResponsePayload(
    val requestId: UUID,
    val allowed: Boolean,
    val requiredLevel: Int,
    val actualAllowed: Boolean,
    val message: String
)
```

### 12.2 服务端处理

```text
收到 request
    -> 读取 MagicRenderConfigManager.current.server.permissions
    -> required = spawnTestEffectRequiresLevel 或 openEditorRequiresLevel
    -> allowed = player.hasPermissions(required)
    -> 回包
```

### 12.3 客户端处理

```text
/mrender editor
    -> if singleplayer: open screen
    -> else send request and show pending message

收到 response
    -> allowed: open screen
    -> denied: chat message
```

### 12.4 超时

多人请求超过 3 秒无响应：

```text
Unable to verify MagicRender editor permission from server.
```

不打开编辑器。

## 13. 与当前运行时的集成点

当前已有：

- `MagicRenderConfigManager.current`
- `ClientConfigReloader.reloadClient()`
- `MotionEffectManager.spawnTrail(effectId, anchor)`
- `MotionEffectManager.spawnBeam(effectId, from, to)`
- `MagicCircleManager.spawn(effectId, anchor)`
- `/mrender bind ...`

需要新增：

- `EffectEditorDraft` 与 JSON 导出。
- `EffectEditorScreen`。
- `EffectEditorExporter`。
- `EffectEditorPreview`。
- `spawn(definition, anchor)` 预览重载。
- 编辑器权限 payload。
- `/mrender editor` 命令。

## 14. 实现顺序

第一阶段：离线编辑与导出

1. 新增 `EffectEditorDraft`。
2. 新增 `EffectEditorValidation`。
3. 新增 `EffectEditorExporter`。
4. 新增 `/mrender editor` 客户端命令，单人直接打开。
5. 实现基础 GUI：基础页、Trail 页、Magic Circle 页。
6. 导出 JSON 到 `config/magicrender/effects/`。
7. 导出后调用 `ClientConfigReloader.reloadClient()`。

第二阶段：实时预览

1. 给 `MotionEffectManager`、`MagicCircleManager` 增加 definition 重载。
2. 新增 `EffectEditorPreview`。
3. 支持 self/crosshair/front 三种预览目标。
4. 关闭 screen 时停止预览。

第三阶段：多人权限

1. 新增 editor permission payload。
2. 注册 client/server networking。
3. 多人打开编辑器前请求权限。
4. 权限不足时只显示提示。

第四阶段：完整字段编辑

1. Beam 页。
2. Visibility 页。
3. Group 选择。
4. 从已有 effect 配置加载到草稿。
5. 同名文件差异提示。

第五阶段：体验增强

1. 颜色预览块。
2. Slider + numeric 双输入。
3. 最近导出列表。
4. 一键复制 effect id。
5. Debug 预览统计。

## 15. 文件写入安全

导出时必须保证：

- 只写入 `config/magicrender/effects/`。
- 文件名从 effect id 规范化，不允许 `..`、绝对路径、反斜杠穿越。
- 写入前先生成完整 JSON 字符串。
- 使用临时文件写入，再原子替换，避免写一半损坏。
- 写入失败时显示错误，不修改运行时配置。

建议：

```text
target = effectsRoot.resolve(safeFileName).normalize()
require(target.startsWith(effectsRoot))
```

## 16. GUI 状态与热加载边界

编辑器维护的是草稿，不直接修改 `MagicRenderConfigManager.current`。

```text
草稿编辑
    -> Preview: 临时 definition
    -> Export: 写 JSON
    -> Reload: 重新加载配置快照
```

这样可以避免用户拖动滑块时不断读写文件，也避免半成品配置污染运行时。

## 17. 首版推荐默认模板

打开新建编辑器时使用：

```text
Template: Entity Arcane Stream
```

默认启用：

- Trail
- Magic Circle
- motion = helix
- additive blend
- 保守 maxPoints 和 duration

该模板对应现有 `magicrender:entity_arcane_stream`。

## 18. 风险与约束

风险：

- Minecraft 原版 Screen API 控件较基础，复杂布局需要自定义滚动面板。
- 多人权限需要网络 payload，不能只做客户端判断。
- 实时预览如果每次字段变化都 spawn，容易堆积实例。
- 同名配置覆盖必须谨慎，避免覆盖整合包作者配置。

约束：

- 不编译时无法确认所有 1.21.1 GUI API 名称，实际实现前需查本地映射。
- 当前渲染系统仅支持部分组件，GUI 不应展示尚未实现的高级项，或应标记为 disabled。
- 第一版只编辑当前已实现的 Trail、Beam、Magic Circle。

## 19. 验收标准

单人：

- `/mrender editor` 打开编辑器。
- 修改 Trail/Magic Circle 字段。
- 点击 Preview 能在玩家或准星实体上看到效果。
- 点击 Export 生成 JSON。
- 同名文件时出现覆盖确认。
- Reload 后 `/mrender bind stream <id>` 可使用新配置。

多人：

- 非 OP 执行 `/mrender editor` 不打开，显示权限不足。
- OP 或权限等级 >= 2 玩家可打开。
- 导出仍只写本地客户端配置。
- 实时预览只在本地客户端显示，不广播给其它玩家。

安全：

- 无效数字会被 clamp 或阻止导出。
- 无效 id 阻止导出。
- 无效颜色阻止导出或回退默认颜色。
- 导出路径不能逃出 `config/magicrender/effects/`。
