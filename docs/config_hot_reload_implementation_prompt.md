# MagicRender 配置系统与热加载实现提示词

下面这段提示词用于指导后续开发 MagicRender 的配置系统。目标是让不会写代码的人也能安全调整特效行为，同时保证客户端和服务端都支持热加载配置，无需重启。

## 实现提示词

你正在开发 Minecraft Fabric 模组 `MagicRender`，项目使用 Kotlin、Minecraft 1.21.1、Fabric、官方 Mojang mappings。请根据以下文档实现配置系统和热加载能力：

- `docs/independent_world_effect_rendering_architecture.md`
- `docs/unity_effect_layer_1_world_space.md`

### 核心目标

为 MagicRender 实现一套安全、分层、可热加载的特效配置系统。配置文件应面向非程序员，允许服主、整合包作者和普通玩家调整特效行为，但不能因为配置错误导致崩溃、严重卡顿、破坏原版渲染状态，或与 Iris/Sodium 冲突。

### 总体原则

必须遵循：

- 不崩溃：配置解析失败时使用默认值或跳过单个错误节点，不让游戏崩溃。
- 不卡顿：所有数量、距离、粒子、Trail、Beam、FBO 都必须有保守上限。
- 不冲突：默认不启用高风险屏幕空间和离屏渲染能力。
- 不破坏原版渲染状态：所有渲染开关只影响 MagicRender 自己的渲染层。
- 面向效果命名：配置名表达用户看到的效果，不表达底层实现细节。
- 分层配置：全局、兼容性、性能、特效组、单类特效、单个特效定义分开。
- 独立开关：每类特效都必须有独立启用/禁用开关。
- 保守默认值：默认配置优先稳定和兼容，不追求最强视觉效果。

### 配置文件设计

请设计并实现以下配置文件：

```text
config/magicrender/
├─ common.json              客户端和服务端都读取的通用行为配置
├─ client.json              仅客户端读取的渲染、显示、兼容配置
├─ server.json              仅服务端读取的权限、同步、限制配置
├─ effect_groups.json       特效组配置
└─ effects/
   ├─ arcane_burst.json
   ├─ healing_aura.json
   └─ ...
```

不要把所有配置塞进一个大文件。配置节点必须按职责分层。

### 热加载要求

客户端和服务端都必须支持热加载：

- 客户端热加载：重新读取 `client.json`、`common.json`、`effect_groups.json`、`effects/*.json`。
- 服务端热加载：重新读取 `server.json`、`common.json`、`effect_groups.json`、`effects/*.json`。
- 热加载不需要重启游戏或服务器。
- 热加载失败时保留上一份可用配置。
- 热加载成功后新配置只影响之后创建的特效；已存在特效可以自然结束，除非配置显式要求停止。
- 热加载命令应至少提供：
  - `/magicrender reload`
  - `/magicrender reload client`，仅客户端可用或通过客户端命令实现
  - `/magicrender reload server`，仅服务端/管理员可用
  - `/magicrender config status`

如果 Fabric API 提供合适的生命周期/命令 API，应优先使用 Fabric API。不要依赖 Iris/Sodium 内部 API。

### 配置解析要求

实现一个配置加载层，建议包结构：

```text
src/main/kotlin/io/github/yuazer/magicrender/config/
├─ MagicRenderConfigManager.kt
├─ ConfigLoadResult.kt
├─ ConfigError.kt
├─ CommonConfig.kt
├─ ServerConfig.kt
├─ EffectGroupConfig.kt
└─ EffectDefinition.kt

src/client/kotlin/io/github/yuazer/magicrender/client/config/
├─ ClientConfig.kt
└─ ClientConfigReloader.kt
```

要求：

- 使用结构化 JSON 解析，不要手写字符串解析。
- 每个配置类都有默认值。
- 每个数值字段都有 clamp。
- 未知字段允许存在，但需要在 debug 日志中提示。
- 错误字段不能导致整个配置系统失败。
- 单个 effect 文件失败时，只禁用该 effect，不影响其它 effect。
- 配置加载结果要包含 warnings 和 errors，供 `/magicrender config status` 展示。

### 配置命名原则

配置名必须描述效果，不描述实现细节。

推荐：

```json
{
  "visuals": {
    "magicCircles": true,
    "auras": true,
    "trails": true,
    "beams": true,
    "particles": true,
    "screenShockwaves": true
  }
}
```

避免：

```json
{
  "render": {
    "useFramebufferObject": true,
    "useBufferBuilderPath": true,
    "bindMainTargetAfterPass": true
  }
}
```

实现细节可以存在于内部代码或高级实验配置，但不能成为普通用户主要配置项。

### common.json 设计

`common.json` 控制客户端和服务端共同认可的行为：

```json
{
  "version": 1,
  "enabled": true,
  "visuals": {
    "particles": true,
    "trails": true,
    "beams": true,
    "auras": true,
    "magicCircles": true,
    "worldIndicators": true,
    "screenEffects": true,
    "offscreenComposition": true
  },
  "limits": {
    "maxActiveEffects": 256,
    "maxParticlesTotal": 8000,
    "maxParticlesPerEffect": 256,
    "maxTrails": 128,
    "maxBeams": 128,
    "defaultDrawDistance": 64,
    "importantDrawDistance": 128
  },
  "groups": {
    "defaultEnabled": true,
    "disabledGroups": []
  }
}
```

默认值必须保守。特别是：

- `screenEffects` 默认 `true`，但必须可由配置手动关闭。
- `offscreenComposition` 默认 `true`，但必须可由配置手动关闭。
- 粒子数量默认低于压力测试上限。
- 检测到 Iris/Sodium 只作为状态提示，不应自动禁用屏幕空间或离屏能力。

### client.json 设计

`client.json` 控制客户端渲染偏好和兼容模式：

```json
{
  "version": 1,
  "quality": {
    "preset": "balanced",
    "particleMultiplier": 1.0,
    "effectDistanceMultiplier": 1.0
  },
  "compatibility": {
    "mode": "auto",
    "whenIrisLoaded": "normal",
    "whenSodiumLoaded": "normal",
    "disableScreenEffectsWithShaders": false,
    "disableOffscreenCompositionWithShaders": false
  },
  "visuals": {
    "particles": true,
    "trails": true,
    "beams": true,
    "auras": true,
    "magicCircles": true,
    "screenDistortion": true,
    "screenGlow": true,
    "screenShockwaves": true,
    "chromaticShift": true,
    "blur": true
  },
  "debug": {
    "showStats": false,
    "logReloads": true,
    "logSkippedEffects": true
  }
}
```

兼容模式语义：

- `auto`：默认模式，检测 Iris/Sodium 但不自动禁用屏幕空间或离屏能力。
- `safe`：用户手动选择的保守模式，只启用第一层世界空间特效。
- `balanced`：启用第一层和第二层。
- `experimental`：允许第一层、第二层和第三层，但仍必须有上限和状态恢复。

### server.json 设计

`server.json` 控制服务器侧权限和同步限制：

```json
{
  "version": 1,
  "enabled": true,
  "sync": {
    "sendEffectEventsToClients": true,
    "sendEffectDefinitions": false,
    "allowClientOverrides": true
  },
  "permissions": {
    "reloadRequiresLevel": 2,
    "spawnTestEffectRequiresLevel": 2
  },
  "limits": {
    "maxEffectsPerPlayer": 64,
    "maxBroadcastDistance": 96,
    "rateLimitPerSecond": 20
  }
}
```

服务端不应强制客户端启用高风险画面效果。服务端只负责“是否允许播放某类效果”和“同步哪些效果事件”。

### effect_groups.json 设计

特效组用于让非程序员批量控制特效：

```json
{
  "version": 1,
  "groups": {
    "combat": {
      "enabled": true,
      "description": "战斗技能特效",
      "priority": 100,
      "limits": {
        "maxActiveEffects": 128,
        "drawDistance": 96
      }
    },
    "ambient": {
      "enabled": true,
      "description": "环境氛围特效",
      "priority": 20,
      "limits": {
        "maxActiveEffects": 64,
        "drawDistance": 48
      }
    },
    "debug": {
      "enabled": false,
      "description": "调试可视化",
      "priority": 0
    }
  }
}
```

要求：

- 每个 effect 必须属于一个 group。
- 未知 group 使用 `default`。
- group 可统一开关、限流、设置优先级和渲染距离。

### 单个 effect 配置设计

单个 effect 文件表达“效果是什么”，不要表达底层渲染实现细节。

示例：

```json
{
  "version": 1,
  "id": "magicrender:arcane_burst",
  "enabled": true,
  "group": "combat",
  "durationTicks": 40,
  "importance": "normal",
  "visibility": {
    "drawDistance": 96,
    "hideWhenShadersConflict": false
  },
  "components": {
    "particles": {
      "enabled": true,
      "style": "spark",
      "amount": 48,
      "color": "#66E6FFFF",
      "size": 0.35
    },
    "magicCircle": {
      "enabled": true,
      "style": "arcane",
      "radius": 2.0,
      "color": "#44AAFFFF"
    },
    "shockwave": {
      "enabled": false,
      "strength": 0.35
    },
    "sound": {
      "enabled": true,
      "id": "minecraft:block.amethyst_block.chime",
      "volume": 0.6,
      "pitch": 1.0
    }
  }
}
```

字段约束：

- `durationTicks` clamp 到 `1..20*60`。
- `amount` clamp 到 `0..maxParticlesPerEffect`。
- `radius` clamp 到 `0.1..32.0`。
- `drawDistance` clamp 到 `0..importantDrawDistance`。
- 颜色解析失败时使用白色或组件默认色。
- 未知 `style` 使用安全默认样式。

### 特效类型独立开关

至少实现这些开关路径：

```text
common.visuals.particles
common.visuals.trails
common.visuals.beams
common.visuals.auras
common.visuals.magicCircles
common.visuals.worldIndicators
common.visuals.screenEffects
common.visuals.offscreenComposition

client.visuals.particles
client.visuals.trails
client.visuals.beams
client.visuals.auras
client.visuals.magicCircles
client.visuals.screenDistortion
client.visuals.screenGlow
client.visuals.screenShockwaves
client.visuals.chromaticShift
client.visuals.blur
```

最终启用状态应由多层共同决定：

```text
enabled = global enabled
    && group enabled
    && effect enabled
    && common type enabled
    && client type enabled
    && compatibility allows it
    && limits not exceeded
```

### 安全降级规则

必须实现以下降级：

- Iris/Sodium 已加载：只记录检测状态，不自动禁用屏幕空间或离屏能力。
- `disableScreenEffectsWithShaders = true`：用户手动禁用第二层屏幕空间特效。
- `disableOffscreenCompositionWithShaders = true`：用户手动禁用第三层 FBO 合成。
- Sodium 已加载：所有层按配置决定是否启用，不读取 Sodium 内部类。
- 配置超过上限：clamp 并记录 warning。
- 单个 effect 无效：禁用该 effect，记录 error，不影响其它 effect。
- 资源缺失：使用默认贴图或跳过该组件。
- 热加载失败：保留上一份可用配置。

### 命令设计

实现命令：

```text
/magicrender reload
/magicrender reload client
/magicrender reload server
/magicrender config status
/magicrender config validate
```

命令输出应面向用户：

- 成功加载了多少配置文件。
- 跳过了多少无效特效。
- 有哪些 warning/error。
- 当前兼容模式。
- Iris/Sodium 是否检测到。
- 屏幕空间和离屏渲染是否被配置手动禁用。

### 日志要求

日志要清楚，但不能刷屏：

- 热加载成功：info。
- 配置被 clamp：warn，但同类 warning 做限频。
- 单个 effect 文件无效：warn。
- 兼容策略关闭高风险能力：info。
- 渲染帧内不要持续输出同一个错误。

### 测试要求

至少验证：

- 缺少所有配置文件时能生成或使用默认配置。
- JSON 语法错误时不崩溃。
- 单个 effect 配置错误时不影响其它 effect。
- 热加载成功后新特效使用新配置。
- 热加载失败后旧配置仍可用。
- Iris/Sodium 未安装时使用默认兼容策略。
- Iris/Sodium 安装时第二层/第三层默认仍开启，可通过配置手动禁用。
- 各类特效开关能独立禁用对应效果。

### 禁止事项

不要做：

- 不要让配置文件直接暴露 OpenGL/FBO/BufferBuilder 细节给普通用户。
- 不要让服务端强制客户端开启屏幕空间或离屏渲染。
- 不要因为单个配置文件错误崩溃客户端或服务端。
- 不要在热加载时销毁正在渲染的 buffer 而不做安全切换。
- 不要依赖 Iris/Sodium 内部 API。
- 不要把检测到 Iris/Sodium 当成自动禁用条件。

### 最终交付

完成后请提供：

- 配置类和默认值。
- 配置加载/校验/热加载管理器。
- 客户端与服务端命令。
- 示例默认配置文件。
- 与 `EffectManager`、特效组、特效 definition 的接入点。
- 简短开发文档，说明如何新增一个 effect 配置。
