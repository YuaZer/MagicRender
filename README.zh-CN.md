# MagicRender

[English](README.md) | 简体中文

MagicRender 是一个面向 Minecraft `1.21.1` 的 Fabric Mod，提供可配置的客户端魔法视觉特效、本地网页编辑器，以及服务端到客户端的特效播放 API。

项目重点是世界空间中的视觉特效：运动轨迹、光束、法阵、点云粒子、流动能量丝带、放射光刺、特效组，以及高级层的真实屏幕空间辉光。

## 环境要求

- Minecraft `1.21.1`
- Java `21`
- Fabric Loader `0.16.10+`
- Fabric API
- Fabric Language Kotlin `1.13.4+kotlin.2.2.0+`

## 主要功能

- 从 `config/magicrender` 加载 JSON 配置驱动的特效。
- 客户端渲染能力：
  - 丝带轨迹
  - 光束
  - 法阵
  - 高级核心、粒子、丝带、法阵层、放射光刺
  - 高级几何的真实后处理辉光
- 特效组：一个 group key 可以同时播放多个 effect id。
- 客户端帧级跟随：绑定实体的特效使用渲染帧插值，不再只依赖 tick 位置。
- 本地浏览器编辑器：中英文 UI、悬浮帮助、可拖拽布局、特效组项目树和 3D 预览。
- 客户端 API 与服务端 API：支持播放/停止单个特效和特效组。
- 命令：重载、校验、测试播放、客户端实体绑定。

## 配置目录

默认配置生成在：

```text
config/magicrender/
```

当前目录结构：

```text
config/magicrender/
  common.json
  server.json
  client.json
  effects/
    *.json
  effects_group/
    *.json
```

`effects/*.json` 保存单个特效定义。

`effects_group/*.json` 保存特效组项目定义。`effect_groups.json` 已不再使用；特效组只从 `effects_group` 目录加载。

effect id 和 group key 都使用标准 `namespace:path` 格式：

```json
"magicrender:arcane_burst"
```

## 单特效定义

最小结构：

```json
{
  "version": 1,
  "id": "magicrender:example",
  "enabled": true,
  "group": "magicrender:example_group",
  "durationTicks": 80,
  "importance": "normal",
  "visibility": {
    "drawDistance": 96,
    "hideWhenShadersConflict": false
  },
  "components": {}
}
```

颜色格式为 `#RRGGBB` 或 `#RRGGBBAA`。

## 特效组

特效组把一个 group key 映射到多个子特效 id。播放特效组时，会在同一个 source/target 锚点上同时播放所有引用的子特效。

示例文件：

```text
config/magicrender/effects_group/arcane_combo.json
```

```json
{
  "version": 1,
  "groups": {
    "magicrender:arcane_combo": {
      "enabled": true,
      "description": "Arcane combo group",
      "priority": 100,
      "effects": [
        "magicrender:arcane_burst",
        "magicrender:entity_arcane_stream"
      ],
      "limits": {
        "maxActiveEffects": 128,
        "drawDistance": 96
      }
    }
  }
}
```

也支持数组简写：

```json
{
  "version": 1,
  "groups": {
    "magicrender:arcane_combo": [
      "magicrender:arcane_burst",
      "magicrender:entity_arcane_stream"
    ]
  }
}
```

## 组件

### Trail

`components.trail` 绘制跟随 source 锚点的丝带轨迹。支持宽度/颜色曲线、贴图、混合模式、采样设置、分段插值和运动偏移。

运动模式：

- `follow`
- `orbit`
- `helix`
- `spiral`
- `formula`
- `js`
- `javascript`

公式模式使用 JavaScript 风格数学表达式，不执行任意脚本。

可用变量：

```text
tick, time, radius, angularSpeed, verticalAmplitude, verticalSpeed, phase, angle, angleDegrees, verticalAngle
```

### Beam

`components.beam` 在 source 和 target 锚点之间绘制光束丝带。

支持：

- 宽度
- 起点/终点颜色
- 分段数量
- 噪声/摆动
- 贴图
- alpha/additive 混合模式

### Magic Circle

`components.magicCircle` 绘制面向相机或水平铺设的法阵，包含内环和符文刻度。

支持：

- 半径
- 厚度
- 颜色
- 分段数
- 旋转速度
- 朝向模式
- 符文数量
- alpha/additive 混合模式

### Advanced

`components.advanced` 是高级分层视觉系统，用于魔法核心、点云粒子、流动光带、多层符文法阵和放射能量光刺。

包含：

- `bloom`：兼容用的旧式伪 Bloom 叠层。
- `glow`：真实屏幕空间辉光设置。
- `core`：中心发光精灵。
- `particleEmitters[]`：点云/公告板粒子发射器。
- `ribbonBundles[]`：多条流动丝带曲线。
- `circleLayers[]`：多个独立法阵层。
- `radialBursts[]`：从源点放射的光刺。

真实辉光会把明亮的高级几何先渲染到离屏缓冲，经过模糊后再加法叠加回画面。

## 本地网页编辑器

客户端在 `client.json` 启用后会启动本地编辑器网页服务。

编辑器资源位置：

```text
src/main/resources/assets/magicrender/editor/
```

编辑器支持：

- 中英文切换。
- 特效组项目编辑。
- 子特效树状管理。
- 在组下新增子特效。
- 从已加载配置导入子特效。
- 将整个组导出到 `config/magicrender/effects_group`。
- 组导出时可同时导出所有子特效到 `config/magicrender/effects`。
- 单独导出选中的子特效。
- 表单化编辑常见特效字段。
- 参数悬浮帮助。
- 3D 预览：旋转、平移、缩放、重置视角、展开预览、运动轨迹、丝带束和近似预览辉光。
- 可拖拽布局：特效组区域、特效类型区域、参数区域、预览/状态/JSON 区域。

编辑器使用本地 Three.js 文件：

```text
assets/magicrender/editor/vendor/three.module.js
assets/magicrender/editor/vendor/OrbitControls.js
```

## 命令

服务端命令根：

```text
/magicrender
```

常用服务端命令：

```text
/magicrender reload
/magicrender reload server
/magicrender config status
/magicrender config validate
/magicrender play self <effectId>
/magicrender play nearby <effectId>
/magicrender play group <groupKey>
/magicrender stop all
```

客户端命令根：

```text
/magicrender
/mrender
/magicrenderclient
```

常用客户端命令：

```text
/mrender reload
/mrender reload client
/mrender config status
/mrender config validate
/mrender bind trail <effectId>
/mrender bind circle <effectId>
/mrender bind stream <effectId>
/mrender bind group <groupKey>
/mrender bindGroup <groupKey>
```

客户端绑定命令主要用于单人测试，会把视觉效果绑定到准星指向的实体。

## API 概览

### 客户端 API

客户端 API：

```kotlin
io.github.yuazer.magicrender.client.api.MagicRenderClientApi
```

常用方法：

```kotlin
MagicRenderClientApi.playEffect(effectId, source)
MagicRenderClientApi.playEffect(effectId, source, target)
MagicRenderClientApi.playGroup(groupKey, source)
MagicRenderClientApi.playGroup(groupKey, source, target)
MagicRenderClientApi.playTrail(effectId, source)
MagicRenderClientApi.playMagicCircle(effectId, source)
MagicRenderClientApi.playBeam(effectId, source, target)
MagicRenderClientApi.bindGroup(groupKey, entity)
MagicRenderClientApi.bindEntityGroup(groupKey, entity)
MagicRenderClientApi.stop(handle)
MagicRenderClientApi.stopEffect(effectId)
MagicRenderClientApi.stopGroup(groupKey)
MagicRenderClientApi.stopBoundToEntity(entityId)
MagicRenderClientApi.stopAllApiEffects()
MagicRenderClientApi.clearAllRenderedEffects()
MagicRenderClientApi.loadedGroupEffectIds(groupKey)
MagicRenderClientApi.loadedGroupKeys()
```

`playEffect` 会自动播放已启用的 trail、magic circle、beam 和 advanced 组件。

`playGroup` 会解析已加载的 group key，并同时播放该组引用的所有 effect id。

### 服务端 API

服务端 API：

```kotlin
io.github.yuazer.magicrender.api.MagicRenderServerApi
```

常用方法：

```kotlin
MagicRenderServerApi.play(player, effectId, sourceEntity)
MagicRenderServerApi.play(player, effectId, sourceEntity, targetEntity)
MagicRenderServerApi.play(player, effectId, sourcePosition)
MagicRenderServerApi.playForTracking(entity, effectId)
MagicRenderServerApi.broadcast(server, effectId, sourcePosition)
MagicRenderServerApi.playGroup(player, groupKey, sourceEntity)
MagicRenderServerApi.playGroup(player, groupKey, sourceEntity, targetEntity)
MagicRenderServerApi.playGroupForTracking(entity, groupKey)
MagicRenderServerApi.broadcastGroup(server, groupKey, sourcePosition)
MagicRenderServerApi.stop(player, requestId)
MagicRenderServerApi.stopEffect(player, effectId)
MagicRenderServerApi.stopGroup(player, groupKey)
MagicRenderServerApi.stopBoundToEntity(player, entity)
MagicRenderServerApi.stopAll(player)
```

服务端 API 通过 Fabric S2C payload 通知客户端，客户端收到后调用 `MagicRenderClientApi` 播放或停止特效。

## 网络同步

Payload 注册位置：

```text
io.github.yuazer.magicrender.network.MagicRenderPayloads
```

当前 S2C payload：

- `magicrender:play_effect`
- `magicrender:stop_effect`

播放 payload 支持：

- 单特效播放
- 特效组播放
- 实体锚点
- 世界坐标锚点
- 可选目标锚点

停止 payload 支持：

- request id
- effect id
- group key
- entity id
- 全部停止

## 渲染说明

- 绑定实体的特效使用渲染帧插值位置，降低实体移动时的视觉割裂。
- 轨迹采样可以按渲染帧更新，并使用帧时间计算采样点年龄。
- Advanced、trail、beam、magic circle 管理器会在绘制前准备渲染帧数据。
- 真实辉光通过 `components.advanced.glow` 启用。
- 网页编辑器预览只近似模拟真实辉光；游戏内渲染器使用实际的离屏辉光处理器。

## 构建

使用 Gradle 构建：

```powershell
.\gradlew.bat build
```

重要 Gradle 设置：

- Kotlin JVM `2.2.0`
- Java toolchain `21`
- Fabric Loom `1.15.4`
- 本地编辑器服务使用 shadow 后的 NanoHTTPD

## 项目结构

```text
src/main/kotlin/io/github/yuazer/magicrender/
  api/          服务端公共 API
  command/      服务端命令
  config/       共享配置解析和默认配置
  i18n/         翻译辅助
  network/      自定义 payload

src/client/kotlin/io/github/yuazer/magicrender/client/
  api/          客户端公共 API
  command/      客户端命令
  config/       客户端配置和兼容性开关
  editor/       编辑器草稿、导出、预览和网页服务逻辑
  effect/       运行时渲染管理器、构建器和后端
  network/      客户端 payload 接收器

src/main/resources/assets/magicrender/
  editor/       本地网页编辑器前端
  lang/         Minecraft 语言文件
  i18n/         运行时翻译资源
```

## 注意事项

- 资源 id 必须使用 `namespace:path`。
- 特效组 key 也使用 `namespace:path`。
- 颜色使用 `#RRGGBB` 或 `#RRGGBBAA`。
- `additive` 混合适合能量和发光效果。
- `alpha` 混合更适合烟雾、水滴或非发光透明效果。
- 旧式伪 Bloom 会增加绘制调用，因为它会额外渲染半透明叠层。
- 真实辉光会增加离屏渲染和模糊开销，可通过 `downsample`、`iterations`、`radius` 调整性能。
- 大量粒子、长轨迹和多条丝带束需要结合 group/client 限制使用。
