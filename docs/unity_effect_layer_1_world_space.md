# Unity 风格特效设计：第一层 - 世界空间特效

本文档细化 MagicRender 的第一层能力：世界空间特效。它对应 Unity VFX 中最常见、也最应该优先落地的一类效果：粒子、拖尾、光束、法阵、范围圈、冲击波、能量罩、实体绑定特效等。

这一层的原则是：特效存在于 Minecraft 世界坐标中，参与相机透视和深度测试，但不修改 Minecraft 的世界渲染器。

## 1. 定义

世界空间特效具备以下特征：

- 有明确世界坐标或实体绑定目标。
- 随相机移动产生正确透视关系。
- 可选择被方块/实体遮挡，或作为 overlay 永远显示。
- 通常由 billboard、mesh、trail、beam、ring、decal-like 几何组成。
- 通过 Fabric `WorldRenderEvents` 注入绘制，不接管原版 renderer。

不属于第一层的内容：

- 纯屏幕空间后处理，例如全屏 bloom、扭曲、径向模糊。
- GUI/HUD 特效。
- 修改方块材质、chunk mesh、实体 renderer 的特效。
- Iris shader pack 内部 pass 级别的渲染扩展。

## 2. 第一层目标

第一阶段要优先支持这些 Unity 风格效果：

| 能力 | Unity 对应概念 | Minecraft 实现 |
| --- | --- | --- |
| Billboard 粒子 | Particle System billboard | 相机朝向 quad 批处理 |
| Trail 拖尾 | Trail Renderer | 世界点环形缓冲 + ribbon mesh |
| Beam 光束 | Line/Beam Renderer | 两点或多点 ribbon |
| Mesh 特效 | Mesh Renderer | 自定义小型 mesh |
| Ring/法阵 | Mesh/Particle decal | 水平面或朝向面 quad/ring |
| Sphere/能量罩 | Mesh + additive/alpha material | 球体/半球 mesh |
| Entity attach | Transform parent | 每帧从实体插值位置更新 |
| Area indicator | Projected indicator | 平面环、扇形、矩形范围 |

第一层不追求一次做完整 Unity VFX Graph，而是先形成稳定的运行时抽象：坐标、生命周期、材质、批处理、事件接入。

## 3. 渲染阶段策略

世界空间特效按材质选择渲染阶段：

```text
AFTER_ENTITIES
    -> opaque mesh
    -> depth-aware solid effect

AFTER_TRANSLUCENT
    -> alpha particle
    -> additive particle
    -> beam/trail/ring
    -> most world-space VFX

LAST
    -> always-visible outline
    -> debug shape
    -> forced overlay range indicator
```

默认选择：

- `ADDITIVE_WORLD`：`AFTER_TRANSLUCENT`
- `TRANSLUCENT_WORLD`：`AFTER_TRANSLUCENT`
- `OPAQUE_WORLD`：`AFTER_ENTITIES`
- `OVERLAY_ALWAYS`：`LAST`

Iris 存在时：

- 默认仍用 `AFTER_TRANSLUCENT`。
- 如果光影包导致透明层表现异常，允许配置切换到 `LAST`。
- 不在第一层做自定义 framebuffer 后处理。

## 4. 坐标模型

特效逻辑层保存世界坐标：

```kotlin
data class EffectTransform(
    val position: Vec3,
    val rotation: Quaternionf,
    val scale: Vector3f
)
```

渲染层只接受相机相对坐标：

```text
localToCamera = worldPosition - cameraPosition
```

原因：

- Minecraft 世界坐标可能很大，直接用 float 容易抖动。
- Fabric `WorldRenderContext.consumers()` 要求顶点相对 camera。
- Sodium/Iris 场景下也更接近当前世界渲染习惯。

实体绑定特效必须使用插值位置，而不是 tick 整数位置：

```text
renderPos = lerp(entity.oldPos, entity.currentPos, tickDelta) + attachOffset
```

## 5. 特效实例模型

建议第一层使用统一实例模型：

```kotlin
interface WorldEffectInstance {
    val id: ResourceLocation
    val transform: EffectTransform
    val ageTicks: Int
    val lifetimeTicks: Int
    val visible: Boolean
    val renderers: List<WorldEffectRenderer>

    fun tick()
    fun isAlive(): Boolean
}
```

实体绑定使用单独的 anchor：

```kotlin
sealed interface EffectAnchor {
    data class World(val position: Vec3) : EffectAnchor
    data class Entity(val entityId: Int, val offset: Vec3) : EffectAnchor
    data class Block(val pos: BlockPos, val offset: Vec3) : EffectAnchor
}
```

渲染前把 anchor 解析为当前帧世界坐标：

```text
EffectAnchor -> EffectTransform -> camera-relative vertices
```

这样 API 层不用关心渲染细节，渲染层也不用理解业务事件。

## 6. Renderer 类型

第一层建议实现 6 个 renderer 类型。

### 6.1 BillboardParticleRenderer

用途：

- 火花、烟雾、魔法光点、浮动符文、小爆炸。

核心数据：

```kotlin
data class ParticleState(
    var position: Vec3,
    var velocity: Vec3,
    var age: Int,
    var lifetime: Int,
    var size: Float,
    var color: Int,
    var rotation: Float,
    var frame: Int
)
```

实现要点：

- 每个粒子生成一个朝向相机的 quad。
- quad 顶点使用相机 right/up 向量计算。
- 同材质粒子合并到一个 batch。
- 支持 alpha over lifetime、size over lifetime、velocity over lifetime。

第一版只需要 CPU 模拟 + CPU 生成顶点，不做 GPU particle。

### 6.2 TrailRenderer

用途：

- 武器挥砍残影、飞行弹道、实体移动拖尾、魔法线条。

核心数据：

```kotlin
data class TrailPoint(
    val position: Vec3,
    val age: Int,
    val width: Float,
    val color: Int
)
```

实现要点：

- 每条 trail 用环形缓冲保存点。
- 渲染时把点列展开成 ribbon mesh。
- 宽度可随生命周期衰减。
- 旧点按时间删除。
- 不要每帧分配新 List，可复用数组或对象池。

### 6.3 BeamRenderer

用途：

- 两点之间的激光、锁链、引导线、闪电段。

核心数据：

```kotlin
data class BeamState(
    val from: EffectAnchor,
    val to: EffectAnchor,
    val width: Float,
    val segments: Int,
    val noise: Float
)
```

实现要点：

- 两点 beam 可以按相机朝向生成 billboard ribbon。
- 闪电 beam 可以生成中间扰动点。
- 多段 beam 仍然进入同一 ribbon batch。
- endpoint 每帧从 anchor 解析，支持实体到实体、实体到方块。

### 6.4 MeshEffectRenderer

用途：

- 能量罩、简单模型、旋转符文、爆炸碎片。

实现要点：

- 支持小型静态 mesh，不进入 chunk。
- mesh 顶点在渲染时乘 effect transform。
- 材质仍使用 `EffectMaterial`。
- 不要复用 Minecraft block model pipeline 作为第一版核心。

第一版 mesh 可以只支持内置 primitive：

- quad
- ring
- disk
- sphere low-poly
- cylinder
- cone

### 6.5 AreaIndicatorRenderer

用途：

- 地面法阵、技能范围圈、扇形攻击提示、矩形区域。

实现要点：

- 本质是世界空间平面 mesh。
- 默认 `depthTest = true`，`depthWrite = false`。
- 可提供 `OVERLAY_ALWAYS` 模式用于强提示。
- 高级贴地投影不放入第一版，因为它容易变成 shader/depth 兼容问题。

第一版形状：

- circle ring
- filled disk
- sector
- rectangle
- line strip

### 6.6 DebugShapeRenderer

用途：

- 开发期可视化 anchor、bounding box、emit volume、裁剪范围。

实现要点：

- 固定走 `LAST`。
- 可开关。
- 不作为正式特效 API 的主要能力。

## 7. 材质设计

第一层材质必须足够少，但语义清楚：

```kotlin
enum class WorldEffectMaterialType {
    OPAQUE_WORLD,
    TRANSLUCENT_WORLD,
    ADDITIVE_WORLD,
    SOFT_ADDITIVE_WORLD,
    OVERLAY_ALWAYS
}
```

材质字段：

```kotlin
data class WorldEffectMaterial(
    val id: ResourceLocation,
    val texture: ResourceLocation,
    val type: WorldEffectMaterialType,
    val depthTest: Boolean,
    val depthWrite: Boolean,
    val cull: Boolean,
    val lightMode: LightMode
)
```

`LightMode` 建议：

```text
FULL_BRIGHT     魔法光、UI-like 世界提示
WORLD_LIGHT     烟雾、实体式物体
CUSTOM          后续扩展
```

第一版优先支持 `FULL_BRIGHT`。光照正确性可以后续补。

## 8. 批处理设计

第一层不要每个 effect 单独 draw call。渲染队列按以下 key 分组：

```text
phase
material type
shader
texture
vertex format
```

建议流程：

```text
WorldRenderEvent
    -> collect visible instances
    -> renderer emits vertices into EffectBatcher
    -> batcher sorts groups
    -> RenderBackend flush groups
```

Billboard、trail、beam 都可以先输出到同一种 `POSITION_COLOR_TEX_LIGHT` 或简化顶点格式。

## 9. 生命周期和模拟

世界空间特效模拟分两部分：

```text
client tick
    -> 生命周期 age
    -> 发射器 spawn
    -> 粒子物理
    -> trail 点采样

render frame
    -> tickDelta 插值
    -> billboard 朝向
    -> batch 构建
```

不要在 render frame 中改变游戏逻辑生命周期。render frame 只做插值和绘制。

## 10. 裁剪策略

第一层必须内置裁剪，否则 Unity 风格粒子很容易拖垮帧率：

```text
distance culling
frustum culling
max instances
max particles per effect
max total particles
max draw distance by material
```

建议默认值：

```text
maxWorldEffects = 512
maxParticles = 20000
defaultDrawDistance = 96 blocks
importantDrawDistance = 160 blocks
```

每个 effect definition 可覆盖：

```json
{
  "culling": {
    "max_distance": 96,
    "frustum": true,
    "priority": 100
  }
}
```

## 11. JSON 定义草案

第一版可以用 JSON 描述组合特效：

```json
{
  "duration": 40,
  "space": "world",
  "renderers": [
    {
      "type": "billboard_particles",
      "material": "magicrender:additive_spark",
      "texture": "magicrender:textures/effect/spark.png",
      "emission": {
        "mode": "burst",
        "count": 32
      },
      "lifetime": [12, 24],
      "size": {
        "start": 0.35,
        "end": 0.0
      },
      "velocity": {
        "shape": "sphere",
        "speed": [0.05, 0.18]
      },
      "color": {
        "start": "#7DFFF2FF",
        "end": "#2F6BFFFF"
      }
    },
    {
      "type": "ring",
      "material": "magicrender:translucent_rune",
      "texture": "magicrender:textures/effect/rune_circle.png",
      "radius": {
        "start": 0.3,
        "end": 2.5
      },
      "alpha": {
        "start": 0.85,
        "end": 0.0
      }
    }
  ]
}
```

解析后生成运行时 definition：

```text
EffectDefinition
    -> RendererDefinition list
    -> EmitterDefinition list
    -> Material references
```

## 12. API 调用示例

世界坐标播放：

```kotlin
MagicEffects.spawn(
    id = ResourceLocation.fromNamespaceAndPath("magicrender", "arcane_burst"),
    position = Vec3(x, y, z)
)
```

绑定实体播放：

```kotlin
MagicEffects.attach(
    id = ResourceLocation.fromNamespaceAndPath("magicrender", "charging_aura"),
    entity = player,
    offset = Vec3(0.0, 1.0, 0.0)
)
```

两点光束：

```kotlin
MagicEffects.beam(
    id = ResourceLocation.fromNamespaceAndPath("magicrender", "mana_link"),
    from = EffectAnchor.Entity(caster.id, Vec3(0.0, 1.4, 0.0)),
    to = EffectAnchor.Entity(target.id, Vec3(0.0, 1.0, 0.0)),
    durationTicks = 20
)
```

## 13. 与 Minecraft 原生粒子的关系

Minecraft 原生粒子适合：

- 小型、简单、低成本粒子。
- 不要求自定义材质/批处理/复杂行为。
- 与原版粒子系统一致的效果。

MagicRender 世界空间特效适合：

- Unity 风格组合特效。
- 多 renderer 组合。
- 自定义材质、beam、trail、法阵、mesh。
- 需要统一生命周期和资源定义的技能/魔法效果。

不要把 MagicRender 第一层建立在原版 ParticleEngine 上。原版粒子可以作为兼容 fallback，但不应作为核心架构。

## 14. 第一阶段最小实现清单

最小可用版本建议只实现：

1. `EffectAnchor`
2. `WorldEffectInstance`
3. `EffectManager`
4. `EffectRenderPhase`
5. `WorldEffectMaterial`
6. `MagicRenderWorldLayer`
7. `BillboardParticleRenderer`
8. `AreaIndicatorRenderer` 的 circle ring
9. 距离裁剪
10. Iris/Sodium mod id 探测

完成后即可验证核心目标：

- 原版可显示世界粒子和法阵。
- Sodium 下不接管 chunk renderer。
- Iris 下不修改 shader pipeline。
- 特效像独立层一样叠加在世界中。

## 15. 后续扩展顺序

建议扩展顺序：

1. TrailRenderer
2. BeamRenderer
3. Mesh primitive
4. JSON effect definition
5. Resource reload
6. Material registry
7. Debug stats
8. 配置化 Iris/Sodium 降级策略

第一层做好后，再设计第二层：屏幕空间后处理和 framebuffer 合成。第二层的兼容风险明显更高，应当建立在第一层稳定之后。
