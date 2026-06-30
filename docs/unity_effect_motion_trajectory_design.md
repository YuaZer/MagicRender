# MagicRender Unity 风格运动轨迹特效设计

本文档设计 MagicRender 的运动轨迹系统。目标是实现类似 Unity `Trail Renderer`、`Line Renderer`、粒子拖尾、Beam/Lightning 的世界空间轨迹效果，用于武器挥砍残影、飞行弹道、魔法连线、能量拖尾、冲刺轨迹等。

设计前提：

- 基于 `docs/independent_world_effect_rendering_architecture.md` 的第一层世界空间特效。
- 不修改 Minecraft 世界渲染器。
- 默认使用 Fabric `WorldRenderEvents.AFTER_TRANSLUCENT` 或 `LAST`。
- 使用相机相对坐标。
- 不依赖 Iris/Sodium 内部 API。

## 1. Unity 思路映射

Unity 常见轨迹能力可以拆成三类：

| Unity 概念 | MagicRender 类型 | 典型用途 |
| --- | --- | --- |
| Trail Renderer | `TrailEffect` | 实体移动拖尾、武器挥砍残影 |
| Line Renderer | `BeamEffect` | 两点连线、法术链、锁定线 |
| Particle trails | `ParticleTrailEffect` | 粒子飞行后留下短尾迹 |

第一版建议优先实现：

1. `TrailEffect`：基于采样点的 ribbon mesh。
2. `BeamEffect`：两点或多点线段生成 billboard ribbon。
3. `ParticleTrailEffect`：复用 `TrailEffect` 的短生命周期点列。

## 2. 核心概念

运动轨迹不是“每帧画一条线”，而是：

```text
目标运动
    -> 按时间/距离采样世界坐标点
    -> 保存有限长度的 TrailPoint 列表
    -> 每帧按相机方向展开成 ribbon mesh
    -> 使用宽度/颜色/透明度曲线衰减
    -> 批处理提交顶点
```

关键数据：

- 采样点位置。
- 采样点年龄。
- 点间距离。
- 宽度曲线。
- 颜色/透明度曲线。
- 材质和混合方式。

## 3. 数据模型

### 3.1 TrailPoint

```kotlin
data class TrailPoint(
    val position: Vec3,
    val ageTicks: Int,
    val spawnTimeNanos: Long,
    val width: Float,
    val color: Int
)
```

说明：

- `position` 保存世界坐标。
- `ageTicks` 用于生命周期裁剪。
- `spawnTimeNanos` 可用于更平滑的 render delta 插值。
- `width` 和 `color` 可以在采样时固定，也可以渲染时根据曲线计算。

### 3.2 TrailState

```kotlin
class TrailState(
    val points: ArrayDeque<TrailPoint>,
    var lastSamplePosition: Vec3?,
    var enabled: Boolean
)
```

建议使用环形缓冲或 `ArrayDeque`。不要每帧创建大量临时 List。

### 3.3 TrailDefinition

```kotlin
data class TrailDefinition(
    val id: ResourceLocation,
    val material: ResourceLocation,
    val maxPoints: Int,
    val lifetimeTicks: Int,
    val minSampleDistance: Double,
    val sampleEveryTick: Boolean,
    val width: Curve,
    val alpha: Curve,
    val color: ColorGradient,
    val renderMode: TrailRenderMode
)
```

保守默认值：

```text
maxPoints = 32
lifetimeTicks = 20
minSampleDistance = 0.05
width.start = 0.25
width.end = 0.0
alpha.start = 0.85
alpha.end = 0.0
```

## 4. Anchor 设计

轨迹必须绑定到一个运动源：

```kotlin
sealed interface TrailAnchor {
    data class Entity(val entityId: Int, val offset: Vec3) : TrailAnchor
    data class WorldPoint(val position: Vec3) : TrailAnchor
    data class BoneLikeSlot(val entityId: Int, val slot: String, val offset: Vec3) : TrailAnchor
}
```

第一版只实现：

- `Entity`
- `WorldPoint`

`BoneLikeSlot` 留给后续实体模型骨骼/插槽系统。

## 5. 采样策略

### 5.1 时间采样

每 tick 采样一次：

```text
ClientTickEvents.END_CLIENT_TICK
    -> resolve anchor world position
    -> append point
```

优点：

- 简单稳定。
- 不依赖帧率。

缺点：

- 高速运动时点间距可能较大。

### 5.2 距离采样

当当前位置与上一个采样点距离超过阈值时采样：

```text
if distance(current, lastSample) >= minSampleDistance:
    append point
```

建议第一版同时使用：

```text
sample if:
    sampleEveryTick == true
    OR distance >= minSampleDistance
```

### 5.3 高速补点

高速弹道会出现断裂，可以在两点之间插值补点：

```text
segments = floor(distance / maxSegmentLength)
for i in 1..segments:
    append lerp(last, current, i / segments)
```

保守默认：

```text
maxSegmentLength = 0.5 block
maxInsertedPointsPerTick = 4
```

## 6. 生命周期

每 tick 更新：

```text
for point in points:
    point.ageTicks += 1

remove points where:
    point.ageTicks > lifetimeTicks
    OR points.size > maxPoints
```

如果 anchor 消失：

- 不立即清空 trail。
- 停止采样。
- 让已有点自然衰减消失。

这样可以避免实体死亡/卸载时轨迹突然断掉。

## 7. Ribbon Mesh 生成

Trail 渲染时把点列展开成连续四边形带：

```text
p0, p1, p2, p3 ...
    -> 每个点计算 left/right 顶点
    -> 相邻点组成 quad
```

### 7.1 面向相机的 Ribbon

适合大多数魔法轨迹：

```text
direction = normalize(nextPoint - previousPoint)
toCamera = normalize(cameraPosition - pointPosition)
side = normalize(cross(direction, toCamera))
left = point - side * width * 0.5
right = point + side * width * 0.5
```

优点：

- 总是面向玩家。
- 类似 Unity trail 的常见表现。

缺点：

- 相机角度极端时可能翻转，需要做 side 平滑。

### 7.2 固定朝上 Ribbon

适合地面残影、法阵轨迹：

```text
side = normalize(cross(direction, worldUp))
```

优点：

- 地面效果稳定。

缺点：

- 竖直运动时效果差。

### 7.3 RenderMode

```kotlin
enum class TrailRenderMode {
    FACE_CAMERA,
    WORLD_UP,
    FIXED_NORMAL
}
```

第一版实现：

- `FACE_CAMERA`
- `WORLD_UP`

## 8. 曲线与渐变

Unity Trail Renderer 的核心体验来自曲线。

### 8.1 Width over Trail

按归一化年龄计算：

```text
t = point.ageTicks / lifetimeTicks
width = widthCurve.evaluate(t)
```

常见曲线：

```text
constant      维持宽度
fade_out      逐渐变窄
head_wide     头部宽，尾部细
tip_sharp     两头尖，中间宽
```

### 8.2 Alpha over Trail

```text
alpha = alphaCurve.evaluate(t)
```

默认：

```text
alpha(t) = 1.0 - t
```

### 8.3 Color over Trail

```text
color = gradient.evaluate(t)
```

例如：

```text
head: #88FFFFFF
tail: #2266FFFF
```

第一版可以先支持 `start` / `end` 两段线性插值。

## 9. UV 设计

Trail 纹理通常沿长度方向滚动：

```text
u = 0 or 1 across width
v = accumulatedDistance * textureScale + time * scrollSpeed
```

字段：

```kotlin
data class TrailTextureSettings(
    val tileLength: Double,
    val scrollSpeed: Double
)
```

默认：

```text
tileLength = 1.0
scrollSpeed = 0.0
```

## 10. Beam 与 Trail 的区别

Trail 是历史轨迹：

```text
过去的位置点 -> 衰减消失
```

Beam 是当前连接：

```text
from anchor -> to anchor -> 每帧重建
```

Beam 可以复用 Ribbon 生成器，但点列来源不同：

```text
Trail: sampled points
Beam: generated points between from/to
```

Beam 支持：

- 直线 beam。
- 多段 beam。
- 噪声扰动 lightning。
- 宽度/颜色随长度变化。

## 11. JSON 配置设计

面向非程序员，字段表达效果，不表达底层实现。

### 11.1 Trail 示例

```json
{
  "version": 1,
  "id": "magicrender:dash_trail",
  "enabled": true,
  "group": "combat",
  "durationTicks": 30,
  "components": {
    "trail": {
      "enabled": true,
      "style": "ribbon",
      "anchor": "entity",
      "lifetimeTicks": 16,
      "maxPoints": 32,
      "minSampleDistance": 0.08,
      "width": {
        "start": 0.45,
        "end": 0.0
      },
      "color": {
        "start": "#88E6FFFF",
        "end": "#2266FFFF"
      },
      "texture": "magicrender:textures/effect/trail_soft.png"
    }
  }
}
```

### 11.2 Beam 示例

```json
{
  "version": 1,
  "id": "magicrender:mana_link",
  "enabled": true,
  "group": "combat",
  "durationTicks": 40,
  "components": {
    "beam": {
      "enabled": true,
      "style": "mana",
      "width": 0.18,
      "segments": 8,
      "noise": 0.08,
      "color": {
        "start": "#AAFFFFFF",
        "end": "#4488FFFF"
      },
      "texture": "magicrender:textures/effect/beam_mana.png"
    }
  }
}
```

## 12. 配置安全上限

必须 clamp：

```text
maxPoints: 2..128
lifetimeTicks: 1..200
minSampleDistance: 0.01..4.0
width.start/end: 0.01..8.0
segments: 1..64
noise: 0.0..4.0
maxInsertedPointsPerTick: 0..8
```

全局限制：

```text
common.limits.maxTrails
common.limits.maxBeams
common.limits.maxActiveEffects
```

超过限制时：

- 跳过低优先级轨迹。
- 缩短 lifetime。
- 降低 maxPoints。
- 记录 warning，但不崩溃。

## 13. 批处理

Trail 和 Beam 应进入同一个 ribbon batch：

```text
batch key:
    phase
    material
    texture
    blend mode
    render mode
```

顶点格式建议：

```text
position
color
uv
light
```

第一版可以 full bright。

## 14. 渲染阶段

默认：

```text
Trail -> AFTER_TRANSLUCENT
Beam -> AFTER_TRANSLUCENT
Debug trail -> LAST
```

材质：

```text
ADDITIVE_WORLD       魔法/能量轨迹
TRANSLUCENT_WORLD    烟雾/残影
OVERLAY_ALWAYS       调试或强提示
```

深度：

```text
depthTest = true
depthWrite = false
blend = additive or alpha
```

## 15. 与 EffectManager 的关系

`EffectManager` 负责：

- 创建 trail/beam 实例。
- tick 更新采样。
- 清理死亡实例。
- 根据配置和上限决定是否允许创建。

`TrailRenderer` 负责：

- 读取 `TrailState.points`。
- 生成相机相对 ribbon 顶点。
- 提交到 batch。

不要在 renderer 中解析配置或查文件。

## 16. 实现顺序

建议按以下顺序实现：

1. `TrailPoint`
2. `TrailState`
3. `TrailDefinition`
4. `TrailSampler`
5. `RibbonMeshBuilder`
6. `TrailRenderer`
7. `BeamState`
8. `BeamPointGenerator`
9. `BeamRenderer`
10. JSON 配置字段解析
11. 与 `ClientEffectGate` 接入
12. Debug 统计和上限控制

## 17. 最小可用版本

最小版本只需要：

- 实体 anchor。
- 每 tick 采样。
- `FACE_CAMERA` ribbon。
- 宽度从 start 到 end 线性衰减。
- 颜色从 start 到 end 线性插值。
- `AFTER_TRANSLUCENT` 渲染。
- `maxPoints` 和 `lifetimeTicks` 上限。

做到这些后，就能支持：

- 玩家冲刺残影。
- 投射物尾迹。
- 武器挥砍光带。
- 两点魔法 Beam。

## 18. 关键结论

运动轨迹系统应当作为第一层世界空间特效的一部分实现。它不是屏幕空间后处理，也不需要 FBO。正确路线是：

- 逻辑层采样世界点。
- 渲染层生成 camera-facing ribbon。
- 材质层控制透明、加色、深度测试。
- 配置层控制宽度、颜色、生命周期和上限。
- 兼容层只检测环境，不依赖 Iris/Sodium 内部实现。

这样既能获得 Unity 风格的 Trail/Beam 表现，又能保持 MagicRender 的低侵入和可兼容目标。
