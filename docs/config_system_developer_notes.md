# MagicRender 配置系统开发说明

当前实现提供了配置系统的基础骨架，覆盖：

- 默认配置文件生成。
- `common.json`、`server.json`、`client.json`、`effect_groups.json`、`effects/*.json` 解析。
- 客户端和服务端热加载入口。
- 服务端命令和客户端命令。
- Iris/Sodium 探测与兼容模式解析。检测只记录状态，不自动禁用屏幕空间或离屏能力。
- 单个 effect 文件失败时跳过，不影响其它 effect。
- 数值 clamp 和 warning/error 结果收集。

## 配置目录

运行后会生成：

```text
config/magicrender/
├─ common.json
├─ client.json
├─ server.json
├─ effect_groups.json
└─ effects/
   └─ arcane_burst.json
```

## 命令

服务端命令：

```text
/magicrender reload
/magicrender reload server
/magicrender config status
/magicrender config validate
```

客户端命令：

```text
/magicrender reload
/magicrender reload client
/magicrender config status
/magicrender config validate
```

## 新增 effect 配置

在 `config/magicrender/effects/` 下新增一个 JSON 文件：

```json
{
  "version": 1,
  "id": "magicrender:healing_aura",
  "enabled": true,
  "group": "ambient",
  "durationTicks": 80,
  "importance": "normal",
  "visibility": {
    "drawDistance": 64,
    "hideWhenShadersConflict": false
  },
  "components": {
    "particles": {
      "enabled": true,
      "style": "spark",
      "amount": 24,
      "color": "#88FFAAFF",
      "size": 0.25
    },
    "aura": {
      "enabled": true,
      "style": "soft",
      "radius": 1.8,
      "color": "#66FF99AA"
    }
  }
}
```

然后执行热加载命令。解析失败时旧配置会保留。

## 后续接入点

后续实现 `EffectManager` 时应只读取 `MagicRenderConfigManager.current` 和 `ClientConfigReloader.current` 的快照，不要在渲染帧内直接读文件。

推荐启用判断：

```text
global enabled
&& group enabled
&& effect enabled
&& common visual type enabled
&& client visual type enabled
&& compatibility state allows it; this is driven by manual config, not automatic Iris/Sodium disablement
&& limits not exceeded
```

客户端侧可以通过 `ClientEffectGate.canUseEffect(...)` 统一判断特效类型是否允许播放。

第一层世界空间特效只依赖配置快照，不依赖第二层和第三层。

## 运动轨迹层

运动轨迹层位于客户端包：

```text
src/client/kotlin/io/github/yuazer/magicrender/client/effect/trajectory/
```

当前实现包括：

- `TrailDefinition`
- `BeamDefinition`
- `TrailAnchor`
- `TrailSampler`
- `BeamPointGenerator`
- `RibbonMeshBuilder`
- `MotionEffectManager`
- `MotionTrajectoryLayer`

创建实体拖尾：

```kotlin
MotionEffectManager.spawnTrail(
    effectId = "magicrender:dash_trail",
    anchor = TrailAnchor.Entity(entity.id)
)
```

创建两点 Beam：

```kotlin
MotionEffectManager.spawnBeam(
    effectId = "magicrender:mana_link",
    from = TrailAnchor.Entity(caster.id),
    to = TrailAnchor.Entity(target.id)
)
```

`MotionEffectManager.buildMeshes(...)` 会生成 `RibbonMesh` 列表。后续渲染批处理器应读取这些 mesh，按 texture/material 分组后写入 `BufferBuilder` 或项目自己的 vertex sink。
