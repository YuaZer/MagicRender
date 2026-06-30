# MagicRender 独立世界特效渲染架构设计

本文档面向 Minecraft 1.21.1、Fabric、Kotlin、官方 Mojang mappings。目标是让 MagicRender 的特效系统像“叠加在世界上的独立渲染层”，尽量不修改 Minecraft 的世界渲染器，从而降低与 Iris、Sodium、以及其它渲染优化/光影模组冲突的概率。

## 1. 需求拆解

你的核心诉求不是“替换世界渲染”，而是“在世界渲染完成的合适阶段，把自己的特效稳定画上去”。这意味着架构上要避免以下做法：

- 不 mixin `LevelRenderer.renderLevel` 的主体逻辑。
- 不替换 chunk renderer、terrain layer、entity renderer 的核心路径。
- 不依赖 Sodium/Iris 的内部类或内部渲染管线。
- 不长期污染 OpenGL/RenderSystem 状态。
- 不假设当前帧缓冲、深度缓冲、透明合成顺序永远等于原版。

建议采用的边界是：

- Minecraft/Iris/Sodium 负责世界、实体、天气、云、光影主流程。
- MagicRender 只维护自己的特效数据、资源、模拟、批处理和绘制。
- MagicRender 通过 Fabric 官方渲染事件接入帧生命周期。
- 如必须 mixin，只做窄范围生命周期补充或兼容探测，不改渲染主路径。

## 2. 方案审核结论

你提出的技术组合是合理的：

```text
Fabric WorldRenderEvents
+ 自研 EffectManager
+ 自定义粒子/Trail/Beam/Mesh 渲染器
+ RenderLayer/BufferBuilder 批处理
+ 可选 Framebuffer 离屏渲染
+ Iris/Sodium 检测兼容模式
```

但这些能力不应该一次性作为同一风险等级实现。更合理的做法是分三层：

| 层级 | 能力 | 推荐状态 | 兼容风险 | 说明 |
| --- | --- | --- | --- | --- |
| 第一层 | 世界空间特效 | 核心能力，默认开启 | 低 | 光环、魔法阵、轨迹、粒子、Beam、Trail、Mesh |
| 第二层 | 屏幕空间特效 | 可选能力，按配置开启 | 中到高 | 扭曲、辉光、冲击波、色散、模糊 |
| 第三层 | 离屏渲染/FBO 合成 | 高级能力，实验开关 | 高 | 先画入自己的 framebuffer，再合成回主画面 |

架构上应确保：

- 第一层不依赖第二层和第三层。
- 第二层可以复用第一层的 EffectManager、资源系统和材质系统。
- 第三层只作为增强路径，不作为普通粒子、Trail、Beam 的必要条件。
- 检测到 Iris/Sodium 时只记录状态，不自动关闭第二层和第三层；是否禁用由配置手动决定。

## 3. 总体架构

建议把项目分为五层：

```text
magicrender
├─ api                  对外特效 API，不依赖具体渲染实现
├─ effect               特效实例、发射器、时间轴、空间绑定、生命周期
├─ render               客户端渲染后端、批处理、材质、shader、mesh
├─ resource             贴图、shader、材质定义、热重载
└─ compat               Iris/Sodium/原版差异探测与降级策略
```

核心运行流程：

```text
服务端/客户端事件
    -> EffectSystem 创建或更新特效实例
    -> EffectSimulation 在 client tick 或 render frame 中推进状态
    -> EffectRenderRegistry 按 RenderPhase 分组
    -> Fabric WorldRenderEvents 在合适时机调用 MagicRenderWorldLayer
    -> RenderBackend 生成顶点/提交 draw call
    -> 恢复 RenderSystem 状态
```

按三层特效能力扩展后的运行流程：

```text
Fabric WorldRenderEvents
    -> MagicRenderFrame.begin
    -> EffectManager.tick/update
    -> Layer 1: WorldSpaceEffectRenderer
        -> ParticleRenderer
        -> TrailRenderer
        -> BeamRenderer
        -> MeshRenderer
        -> Ring/AreaRenderer
    -> Layer 2: ScreenSpaceEffectRenderer 可选
        -> distortion
        -> glow
        -> shockwave
        -> chromatic aberration
        -> blur
    -> Layer 3: OffscreenEffectPipeline 可选
        -> render effects to own framebuffer
        -> composite to main target
    -> MagicRenderFrame.end
```

## 4. 三层特效模型

### 4.1 第一层：世界空间特效

这是 MagicRender 的基础层，应优先实现并默认开启。

能力范围：

- 光环
- 魔法阵
- 轨迹
- 粒子
- Beam
- Trail
- 小型 Mesh 特效
- 世界范围提示
- 实体绑定特效

技术路线：

- 使用 `WorldRenderEvents.AFTER_ENTITIES`、`AFTER_TRANSLUCENT`、`LAST`。
- 使用相机相对坐标生成顶点。
- 使用自研 `EffectManager` 管理生命周期。
- 使用自定义 `ParticleRenderer`、`TrailRenderer`、`BeamRenderer`、`MeshRenderer`。
- 使用 `BufferBuilder` 或 Fabric/原版兼容的 vertex consumer 批处理。
- 不需要单独 framebuffer。
- 不读取或修改 Sodium/Iris 内部渲染器。

第一层是最符合“叠加在世界上的独立渲染层”的部分。它可以做到低侵入、可批处理、可被世界深度遮挡，也最容易在 Iris/Sodium 下保持可用。

详细设计见 `docs/unity_effect_layer_1_world_space.md`。

### 4.2 第二层：屏幕空间特效

第二层处理已经渲染好的画面或深度信息，适合 Unity 后处理风格效果。

能力范围：

- 扭曲
- 辉光
- 冲击波
- 色散
- 模糊

技术路线：

- 作为可选模块挂在 `WorldRenderEvents.END` 或后续自定义合成点。
- 使用全屏 quad 或局部屏幕空间 quad。
- 需要读取颜色纹理、深度纹理或第一层输出结果时，必须通过受控 framebuffer 管线。
- 默认在 Iris/Sodium 下仍按配置开启；shader pack 可能改变 framebuffer 和合成顺序，因此必须允许用户手动关闭。

第二层的关键问题不是“能不能做”，而是“在 Iris/Sodium 下是否能稳定拿到想要的画面输入”。因此第二层必须支持用户手动降级：

| 效果 | 原版 | Sodium | Iris |
| --- | --- | --- | --- |
| 局部扭曲 | 默认开启 | 默认开启 | 默认开启，可手动关闭 |
| 辉光 | 默认开启 | 默认开启 | 默认开启，可手动用第一层 additive 替代 |
| 冲击波 | 默认开启 | 默认开启 | 默认开启，可手动使用世界空间 ring 降级 |
| 色散 | 默认开启 | 默认开启 | 默认开启，可手动关闭 |
| 模糊 | 默认开启 | 默认开启 | 默认开启，可手动关闭 |

### 4.3 第三层：离屏渲染

第三层是高级渲染管线：MagicRender 先把特效画入自己的 framebuffer，再合成回主画面。

能力范围：

- 单独 framebuffer。
- 特效 mask。
- 特效颜色缓冲。
- 特效深度或软粒子辅助缓冲。
- bloom/blur/downsample/upsample。
- 最终 composite。

推荐流程：

```text
WorldRenderEvents.AFTER_TRANSLUCENT or LAST
    -> bind MagicRender FBO
    -> clear effect color/depth as needed
    -> render selected effects into FBO
    -> unbind MagicRender FBO
    -> bind previous/main target
    -> composite MagicRender FBO to current framebuffer
```

第三层必须满足：

- 可完全关闭。
- 不影响第一层直接渲染。
- resize 时重建 framebuffer。
- resource reload 时重建 shader。
- 每次 bind framebuffer 后恢复原来的目标。
- 检测 Iris/Sodium 时不自动关闭；用户可通过配置手动关闭。

第三层能提供最接近 Unity 后处理/VFX Graph 的画面效果，但也是最容易和 shader pack、fabulous graphics、其它后处理模组冲突的部分。

## 5. 接入点选择

本项目当前依赖 `fabric-rendering-v1`，在 1.21.1 可使用：

- `WorldRenderEvents.START`
- `WorldRenderEvents.AFTER_SETUP`
- `WorldRenderEvents.BEFORE_ENTITIES`
- `WorldRenderEvents.AFTER_ENTITIES`
- `WorldRenderEvents.BEFORE_DEBUG_RENDER`
- `WorldRenderEvents.AFTER_TRANSLUCENT`
- `WorldRenderEvents.LAST`
- `WorldRenderEvents.END`

推荐分层使用：

| 特效类型 | 推荐事件 | 深度关系 | 说明 |
| --- | --- | --- | --- |
| 非透明实体式特效、可被方块遮挡的实体光效 | `AFTER_ENTITIES` | 开深度测试，写入/不写入深度按材质决定 | 可使用 `WorldRenderContext.consumers()`，和实体/方块实体阶段更接近 |
| 半透明粒子、能量带、冲击波 | `AFTER_TRANSLUCENT` | 通常开深度测试，关闭深度写入 | 在原版透明层之后，适合直接写 framebuffer |
| 永远压在世界上的轮廓、选区、调试线框 | `LAST` | 可开深度测试或强制 overlay | 矩阵已匹配相机视图，适合直接绘制 |
| 纯屏幕空间后处理/全屏光晕 | `END` 或独立 framebuffer 后合成 | 自己管理状态 | 不依赖世界矩阵，注意不要影响手部和 GUI |

默认策略：

- 主世界 3D 特效优先注册到 `WorldRenderEvents.AFTER_TRANSLUCENT`。
- 需要更自然遮挡的非透明几何体，可注册到 `AFTER_ENTITIES`。
- 纯叠加线框、辅助图形、可视化工具使用 `LAST`。
- `END` 只用于自管理 framebuffer 或屏幕空间合成，不做普通世界几何绘制。

## 6. 坐标与相机

MagicRender 的所有世界空间特效都应保存世界坐标，渲染时转换为相机相对坐标：

```text
renderX = worldX - camera.position.x
renderY = worldY - camera.position.y
renderZ = worldZ - camera.position.z
```

原因：

- 避免大坐标下浮点精度抖动。
- 与 Fabric `WorldRenderContext.consumers()` 的要求一致。
- 与原版、Sodium、Iris 的世界渲染坐标习惯更接近。

建议封装：

```kotlin
data class RenderCamera(
    val x: Double,
    val y: Double,
    val z: Double,
    val projection: Matrix4f,
    val position: Matrix4f
)
```

渲染器只接受 `RenderCamera` 和相机相对顶点，不直接到处读取 `MinecraftClient`/`Minecraft` 单例。

## 7. 深度、透明和混合策略

特效材质应显式声明渲染状态，不依赖外部状态：

```text
EffectMaterial
├─ texture
├─ shader
├─ blendMode
├─ depthTest
├─ depthWrite
├─ cull
├─ lightMode
└─ phase
```

建议内置材质模式：

| 模式 | depthTest | depthWrite | blend | 用途 |
| --- | --- | --- | --- | --- |
| `OPAQUE_WORLD` | true | true | off | 实体式实心特效 |
| `TRANSLUCENT_WORLD` | true | false | alpha | 烟雾、半透明屏障 |
| `ADDITIVE_WORLD` | true | false | additive | 魔法光、火花、能量 |
| `OVERLAY_ALWAYS` | false | false | alpha/additive | 总在最上层的提示、调试效果 |
| `SCREEN_COMPOSITE` | false | false | custom | 屏幕空间后处理 |

每个 draw batch 必须：

1. 设置自己的 shader、texture、blend、depth、cull 状态。
2. 绘制。
3. 恢复或重置影响范围内的状态。

不要假设 Fabric 事件进入时的 GL 状态固定。Iris/Sodium 可能改变渲染路径，即便事件仍被调用，具体 framebuffer 和状态也可能不同。

## 8. 与 Iris/Sodium 的兼容边界

### 8.1 Sodium

Sodium 主要替换 chunk/terrain 渲染和批处理路径。MagicRender 应避免：

- 注入 chunk rebuild。
- 替换 terrain `RenderType` / `RenderLayer`。
- 读取 Sodium 内部 chunk renderer。

建议：

- 使用 Fabric `WorldRenderEvents`。
- 世界特效自己提交顶点。
- 需要遮挡时依赖当前 depth buffer，而不是读取 chunk 数据。

### 8.2 Iris

Iris 会改变 framebuffer、shader pack、透明合成和阴影流程。MagicRender 应避免：

- 强行绑定原版 main framebuffer 后假设它就是最终输出。
- 修改 Iris shader pipeline。
- 在光影包内部 pass 中插入自己的渲染，除非以后单独做 Iris 专用扩展。

建议：

- 默认把 MagicRender 当作普通世界 overlay。
- 在 `AFTER_TRANSLUCENT` 和 `LAST` 两个阶段提供配置切换。
- 检测到 Iris/Sodium 时，默认仍按配置开启屏幕空间和离屏能力。
- 对全屏 bloom、distortion、grab pass 这类效果提供手动降级配置。

### 8.3 兼容探测

建立 `compat` 层：

```kotlin
object RenderCompat {
    val isSodiumLoaded: Boolean
    val isIrisLoaded: Boolean
    val supportsAdvancedPost: Boolean
    val preferredWorldPhase: EffectRenderPhase
}
```

只用 Fabric Loader 检测 mod id：

- `sodium`
- `iris`

不要编译期依赖这些 mod 的内部 API。

## 9. 模块设计

### 9.1 API 层

对外暴露稳定、简单的特效创建接口：

```kotlin
interface MagicEffectApi {
    fun spawn(effectId: ResourceLocation, position: Vec3, params: EffectParams = EffectParams.EMPTY): EffectHandle
    fun attach(effectId: ResourceLocation, target: Entity, params: EffectParams = EffectParams.EMPTY): EffectHandle
    fun stop(handle: EffectHandle)
}
```

API 层不暴露 OpenGL、RenderSystem、Fabric event。

### 9.2 Effect 层

职责：

- 管理特效实例生命周期。
- 支持世界坐标、实体绑定、骨骼/插槽绑定的扩展。
- 支持 tick 时间和 render partial tick 插值。
- 支持 LOD、距离裁剪、视锥裁剪。

核心对象：

```text
EffectInstance
├─ id
├─ transform
├─ lifetime
├─ emitter list
├─ renderer list
├─ visibility policy
└─ state variables
```

### 9.3 Render 层

职责：

- 把 effect renderer 输出为 batch。
- 按 `EffectRenderPhase`、材质、贴图、shader 排序。
- 尽量减少 draw call。
- 封装原版 `BufferBuilder` / `Tesselator` / `RenderSystem` 使用。

推荐接口：

```kotlin
interface EffectRenderer<T : EffectInstance> {
    val phase: EffectRenderPhase
    fun render(instance: T, context: EffectRenderContext, sink: EffectVertexSink)
}
```

`EffectRenderContext` 包含：

- 当前世界
- camera
- tick delta
- projection matrix
- position matrix
- light texture
- compat flags

### 9.4 Resource 层

资源类型：

- `assets/magicrender/effects/*.json`
- `assets/magicrender/textures/effect/*.png`
- `assets/magicrender/shaders/core/*.json`
- `assets/magicrender/shaders/core/*.vsh`
- `assets/magicrender/shaders/core/*.fsh`

建议先实现 JSON 驱动的轻量特效定义：

```json
{
  "duration": 40,
  "emitters": [
    {
      "type": "billboard_particles",
      "texture": "magicrender:textures/effect/spark.png",
      "count": 24,
      "material": "additive_world"
    }
  ]
}
```

后续再扩展 timeline、曲线、噪声、mesh、trail。

## 10. 客户端入口设计

`MagicrenderClient.onInitializeClient()` 应只做注册：

```text
MagicRenderClient
├─ MagicRenderResources.registerReloadListeners()
├─ MagicRenderEvents.registerClientTick()
├─ MagicRenderWorldLayer.register()
└─ RenderCompat.detect()
```

`MagicRenderWorldLayer.register()`：

```kotlin
WorldRenderEvents.AFTER_TRANSLUCENT.register { context ->
    MagicRenderEngine.render(EffectRenderPhase.AFTER_TRANSLUCENT, context)
}

WorldRenderEvents.LAST.register { context ->
    MagicRenderEngine.render(EffectRenderPhase.LAST, context)
}

WorldRenderEvents.END.register { context ->
    MagicRenderEngine.endFrame(context)
}
```

`AFTER_ENTITIES` 是否启用建议做成配置项或由材质 phase 决定。

## 11. 帧生命周期

建议每帧分为：

```text
ClientTickEvents.END_CLIENT_TICK
    -> 固定 tick 模拟：生命周期、发射器、逻辑状态

WorldRenderEvents.START
    -> beginFrame：清空临时统计、记录 world/camera 基础信息

WorldRenderEvents.AFTER_TRANSLUCENT / LAST
    -> render phase：筛选实例、生成 batch、绘制

WorldRenderEvents.END
    -> endFrame：释放临时 buffer、校验状态、统计性能
```

不要在 render event 中做大量资源加载、JSON 解析、贴图创建。资源必须预加载或懒加载后缓存，并能响应资源包 reload。

## 12. 性能策略

第一版就应加入这些约束：

- 距离裁剪：超出配置距离不渲染。
- 生命周期上限：防止无限粒子堆积。
- 每帧最大实例数/粒子数。
- 材质排序：按 phase -> shader -> texture -> blend 分组。
- Billboard 粒子批处理。
- Trail/beam 使用环形缓冲，避免每帧大量分配。
- Debug 统计：实例数、粒子数、batch 数、draw call 数。

## 13. 降级策略

检测到 Iris/Sodium 时不要直接禁用特效。检测结果只用于状态显示和可选手动降级：

| 能力 | 原版 | Sodium | Iris |
| --- | --- | --- | --- |
| 世界 billboard 粒子 | 开启 | 开启 | 开启 |
| additive 世界光效 | 开启 | 开启 | 开启，必要时用户手动切 `LAST` |
| depth-aware 半透明 | 开启 | 开启 | 开启，异常时用户手动降级 |
| 全屏 distortion | 开启 | 开启 | 开启，异常时用户手动关闭 |
| 自定义 framebuffer 后处理 | 开启 | 开启 | 开启，异常时用户手动关闭 |

配置建议：

```text
magicrender.compat.mode = AUTO | SAFE | BALANCED | EXPERIMENTAL
magicrender.render.phase.default = AFTER_TRANSLUCENT | LAST
magicrender.post.enabled = true
```

建议兼容模式语义：

| 模式 | 第一层 | 第二层 | 第三层 | 用途 |
| --- | --- | --- | --- | --- |
| `AUTO` | 开启 | 开启 | 开启 | 默认，只检测环境不自动禁用 |
| `SAFE` | 开启 | 关闭 | 关闭 | 用户手动保守模式 |
| `BALANCED` | 开启 | 开启 | 开启 | 用户手动平衡模式 |
| `EXPERIMENTAL` | 开启 | 开启 | 开启 | 用户主动测试高级效果 |

## 14. 不推荐路线

这些路线短期看起来强，但长期冲突风险高：

- 大范围 mixin `LevelRenderer`。
- 接管原版 chunk/entity/particle renderer。
- 直接修改 Iris/Sodium 内部类。
- 把特效伪装成原版 block/entity 来借用整个渲染管线。
- 用全局 GL 状态堆叠实现效果，但不做状态恢复。
- 每个特效单独 draw call，不做批处理。

## 15. 推荐落地顺序

第一阶段：第一层最小可用世界空间特效

1. 新建 `client.render` 包。
2. 注册 `WorldRenderEvents.AFTER_TRANSLUCENT`、`LAST`、`END`。
3. 实现 `EffectRenderContext`、`EffectRenderPhase`。
4. 实现 `EffectManager`。
5. 实现固定材质的 billboard 粒子 demo。
6. 实现 circle ring 魔法阵。
7. 加入 RenderSystem 状态保存/恢复约定。

第二阶段：第一层完整化

1. 支持 spawn/stop/lifetime。
2. 支持世界坐标与实体绑定。
3. 实现 TrailRenderer。
4. 实现 BeamRenderer。
5. 实现小型 Mesh/primitive renderer。
6. 支持距离裁剪和最大粒子数。

第三阶段：资源驱动

1. 添加 JSON effect definition。
2. 添加资源 reload listener。
3. 添加贴图和材质注册表。
4. 添加基础曲线和 timeline。

第四阶段：第二层屏幕空间特效

1. 添加全屏 quad pass。
2. 添加局部冲击波/扭曲接口。
3. 添加辉光降级策略：优先 additive 世界空间效果。
4. 检测 Iris/Sodium 时默认仍开启，提供手动关闭配置。

第五阶段：第三层离屏渲染

1. 添加 MagicRender framebuffer。
2. 支持 resize/reload 重建。
3. 支持 selected effects render to FBO。
4. 支持 composite pass。
5. 默认开启，但必须支持明确配置关闭。

第六阶段：兼容与质量

1. 检测 Iris/Sodium。
2. 添加 phase 自动选择和配置。
3. 添加 debug overlay 或日志统计。
4. 分别在原版、Sodium、Iris、Iris+Sodium 下测试。

## 16. 建议包结构

```text
src/client/kotlin/io/github/yuazer/magicrender/client/
├─ MagicrenderClient.kt
├─ compat/
│  └─ RenderCompat.kt
├─ effect/
│  ├─ EffectHandle.kt
│  ├─ EffectInstance.kt
│  ├─ EffectManager.kt
│  └─ EffectParams.kt
├─ render/
│  ├─ EffectRenderContext.kt
│  ├─ EffectRenderPhase.kt
│  ├─ MagicRenderEngine.kt
│  ├─ MagicRenderWorldLayer.kt
│  ├─ screen/
│  │  ├─ ScreenEffectPipeline.kt
│  │  └─ FullscreenPass.kt
│  ├─ offscreen/
│  │  ├─ EffectFramebuffer.kt
│  │  └─ EffectCompositePass.kt
│  ├─ material/
│  │  ├─ BlendMode.kt
│  │  └─ EffectMaterial.kt
│  └─ batch/
│     ├─ EffectBatcher.kt
│     └─ BillboardParticleBatch.kt
└─ resource/
   ├─ EffectDefinition.kt
   └─ MagicRenderResourceManager.kt
```

## 17. 关键设计结论

MagicRender 应该是“世界渲染事件上的独立特效引擎”，不是“Minecraft 世界渲染器补丁”。最稳的实现方式是：

- 用 Fabric `WorldRenderEvents` 接入，不直接改 `LevelRenderer` 主流程。
- 以 `AFTER_TRANSLUCENT` 和 `LAST` 作为主要绘制阶段。
- 所有顶点使用相机相对坐标。
- 每个材质显式设置并恢复渲染状态。
- 与 Iris/Sodium 只做 mod id 探测和策略降级，不依赖内部 API。
- 复杂后处理默认按配置启用，检测到 Iris/Sodium 不自动关闭。

这个架构能覆盖大多数 Unity 风格的世界特效：billboard 粒子、trail、beam、冲击波、能量罩、范围提示、法阵、屏幕空间闪光等，同时把与渲染优化/光影模组的冲突面控制在最低。

最终建议：

- 第一层世界空间特效作为 MagicRender 的核心产品能力，默认开启。
- 第二层屏幕空间特效作为视觉增强，默认开启，可手动关闭。
- 第三层离屏渲染作为高级能力，默认开启，可手动关闭，但不能成为普通特效的硬依赖。
- `Fabric WorldRenderEvents + EffectManager + 自定义 Renderer + 批处理 + 可选 FBO + Iris/Sodium 兼容模式` 是正确方向，但必须按层解耦。

## 18. Unity 风格特效分层文档

- 第一层：世界空间特效，见 `docs/unity_effect_layer_1_world_space.md`。
- 第一层扩展：运动轨迹、Trail、Beam，见 `docs/unity_effect_motion_trajectory_design.md`。
