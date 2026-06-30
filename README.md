# MagicRender

MagicRender is a Fabric mod for Minecraft 1.21.1 that provides configurable client-side magic visual effects, a local web editor, and server-to-client effect playback APIs.

The project focuses on world-space visual effects such as trails, beams, magic circles, particle-like glow points, layered flowing ribbons, radial burst rays, and soft glow composition.

## Requirements

- Minecraft `1.21.1`
- Java `21`
- Fabric Loader `0.16.10+`
- Fabric API
- Fabric Language Kotlin `1.13.4+kotlin.2.2.0+`

## Main Features

- Config-driven visual effects loaded from `config/magicrender`.
- Client-side render layers for:
  - ribbon trails
  - beams
  - magic circles
  - advanced glow/particle/ribbon/circle/burst layers
- Local browser editor with Chinese/English UI.
- 3D editor preview based on local Three.js assets.
- Public client API for playing and stopping effects.
- Server API plus S2C payloads for requesting target clients to play or stop effects.
- Commands for reload, validation, test playback, and client-side entity binding.

## Configuration

Default configuration files are generated under:

```text
config/magicrender/
```

Important files:

```text
common.json
server.json
client.json
effect_groups.json
effects/*.json
```

Effect ids use the standard `namespace:path` format, for example:

```json
"id": "magicrender:entity_arcane_stream"
```

### Basic Effect Shape

```json
{
  "version": 1,
  "id": "magicrender:example",
  "enabled": true,
  "group": "combat",
  "durationTicks": 80,
  "importance": "normal",
  "visibility": {
    "drawDistance": 96,
    "hideWhenShadersConflict": false
  },
  "components": {}
}
```

## Components

### Trail

`components.trail` draws a ribbon following a source anchor. It supports width/color curves, texture, blend mode, sampling settings, and motion offsets.

Motion modes:

- `follow`
- `orbit`
- `helix`
- `spiral`
- `formula`
- `js`
- `javascript`

Formula mode supports JavaScript-style mathematical expressions, not arbitrary script execution.

Available variables:

```text
tick, time, radius, angularSpeed, verticalAmplitude, verticalSpeed, phase, angle, angleDegrees, verticalAngle
```

Example:

```json
"motion": {
  "mode": "formula",
  "radius": 1.2,
  "angularSpeed": 8.0,
  "verticalAmplitude": 0.4,
  "verticalSpeed": 12.0,
  "formula": {
    "x": "Math.cos(angle) * radius",
    "y": "Math.sin(verticalAngle) * verticalAmplitude",
    "z": "Math.sin(angle) * radius"
  }
}
```

### Beam

`components.beam` draws a ribbon between source and target anchors.

It supports:

- width
- start/end color
- segment count
- noise/wobble
- texture
- alpha/additive blend mode

### Magic Circle

`components.magicCircle` draws a camera-facing or horizontal circle with inner ring and glyph tick marks.

It supports:

- radius
- thickness
- color
- segment count
- rotation speed
- facing mode
- glyph count
- blend mode

### Advanced

`components.advanced` is the newer layered visual system. It is intended for effects like glowing point clouds, magic cores, layered runic circles, flowing light streams, and radial energy bursts.

It contains:

- `bloom`: pseudo-bloom/soft glow layering.
- `core`: central glow sprite.
- `particleEmitters[]`: billboard point-cloud emitters.
- `ribbonBundles[]`: multiple flowing ribbon curves.
- `circleLayers[]`: multiple independent circle layers.
- `radialBursts[]`: ray-like burst strokes from the source.

Example:

```json
"advanced": {
  "enabled": true,
  "bloom": {
    "enabled": true,
    "layers": 3,
    "scaleStep": 1.8,
    "alphaFalloff": 0.45
  },
  "core": {
    "enabled": true,
    "color": "#FFFF66FF",
    "radius": 0.65,
    "pulseAmplitude": 0.2,
    "pulseSpeed": 0.12,
    "texture": "minecraft:textures/particle/flash.png",
    "blendMode": "additive"
  },
  "particleEmitters": [
    {
      "enabled": true,
      "shape": "sphere",
      "count": 160,
      "color": {
        "start": "#FF445CFF",
        "end": "#AAFF44FF"
      },
      "size": {
        "start": 0.08,
        "end": 0.018
      },
      "radius": 2.2,
      "height": 2.0,
      "noise": 0.35,
      "texture": "minecraft:textures/particle/flash.png",
      "blendMode": "additive"
    }
  ],
  "ribbonBundles": [
    {
      "enabled": true,
      "count": 10,
      "width": {
        "start": 0.12,
        "end": 0.018
      },
      "color": {
        "start": "#FFFFEEAA",
        "end": "#FF66FF99"
      },
      "length": 8.0,
      "samples": 120,
      "phaseStep": 24.0,
      "amplitude": 0.75,
      "frequency": 1.5,
      "twist": 0.6,
      "flowSpeed": 0.12,
      "texture": "minecraft:textures/particle/flame.png",
      "blendMode": "additive"
    }
  ]
}
```

Note: the current glow implementation is pseudo-bloom using layered additive billboards. It is not a full screen-space post-processing bloom pipeline.

## Local Web Editor

The client starts a local editor web server when enabled by client config.

Default editor assets are under:

```text
src/main/resources/assets/magicrender/editor/
```

The editor supports:

- Chinese/English switching.
- Form editing for common effect fields.
- Full JSON editing/export.
- Hover help for parameters.
- 3D preview with orbit, pan, zoom, reset camera, and expanded preview.
- Advanced configuration preview for glow, particles, ribbon bundles, circle layers, and radial bursts.

The editor uses local Three.js files:

```text
assets/magicrender/editor/vendor/three.module.js
assets/magicrender/editor/vendor/OrbitControls.js
```

## Commands

Server-side command root:

```text
/magicrender
```

Useful server commands:

```text
/magicrender reload
/magicrender reload server
/magicrender config status
/magicrender config validate
/magicrender play self <effectId>
/magicrender play nearby <effectId>
/magicrender stop all
```

Client-side command roots:

```text
/magicrender
/mrender
/magicrenderclient
```

Useful client commands:

```text
/mrender reload
/mrender reload client
/mrender config status
/mrender config validate
/mrender bind trail <effectId>
/mrender bind circle <effectId>
/mrender bind stream <effectId>
```

Client binding commands are mainly for single-player/testing and bind visuals to the entity under the crosshair.

## API Overview

### Client API

Client-side API:

```kotlin
io.github.yuazer.magicrender.client.api.MagicRenderClientApi
```

Common methods:

```kotlin
MagicRenderClientApi.playEffect(effectId, source)
MagicRenderClientApi.playEffect(effectId, source, target)
MagicRenderClientApi.playTrail(effectId, source)
MagicRenderClientApi.playMagicCircle(effectId, source)
MagicRenderClientApi.playBeam(effectId, source, target)
MagicRenderClientApi.stop(handle)
MagicRenderClientApi.stopEffect(effectId)
MagicRenderClientApi.stopBoundToEntity(entityId)
MagicRenderClientApi.stopAllApiEffects()
MagicRenderClientApi.clearAllRenderedEffects()
```

`playEffect` automatically includes enabled trail, magic circle, beam, and advanced components.

### Server API

Server-side API:

```kotlin
io.github.yuazer.magicrender.api.MagicRenderServerApi
```

Common methods:

```kotlin
MagicRenderServerApi.play(player, effectId, sourceEntity)
MagicRenderServerApi.play(player, effectId, sourceEntity, targetEntity)
MagicRenderServerApi.play(player, effectId, sourcePosition)
MagicRenderServerApi.playForTracking(entity, effectId)
MagicRenderServerApi.broadcast(server, effectId, sourcePosition)
MagicRenderServerApi.stop(player, requestId)
MagicRenderServerApi.stopEffect(player, effectId)
MagicRenderServerApi.stopBoundToEntity(player, entity)
MagicRenderServerApi.stopAll(player)
```

The server API sends Fabric S2C payloads to the client. The client receives the payload and calls `MagicRenderClientApi`.

## Networking

Payloads are registered in:

```text
io.github.yuazer.magicrender.network.MagicRenderPayloads
```

Current S2C payloads:

- `magicrender:play_effect`
- `magicrender:stop_effect`

Payloads support entity anchors and world-point anchors.

## Build

Build with Gradle:

```powershell
.\gradlew.bat build
```

Important Gradle settings:

- Kotlin JVM `2.2.0`
- Java toolchain `21`
- Fabric Loom `1.15.4`
- Shadowed NanoHTTPD for the local editor server

## Project Layout

```text
src/main/kotlin/io/github/yuazer/magicrender/
  api/          Server-side public API
  command/      Server commands
  config/       Shared config parsing/defaults
  i18n/         Translation helper
  network/      Custom payloads

src/client/kotlin/io/github/yuazer/magicrender/client/
  api/          Client-side public API
  command/      Client commands
  config/       Client config and compatibility gates
  editor/       Editor draft/export/preview logic
  effect/       Runtime render managers/builders/backends
  network/      Client payload receivers

src/main/resources/assets/magicrender/
  editor/       Local web editor frontend
  lang/         Minecraft language files
  i18n/         Runtime translation resources
```

## Notes

- Resource ids must use `namespace:path`.
- Colors use `#RRGGBB` or `#AARRGGBB`.
- Additive blending is best for energy/glow effects.
- Alpha blending is better for soft smoke, water, or non-glowing transparent visuals.
- Advanced pseudo-bloom increases draw calls because it renders extra translucent layers.
- Large particle counts and many ribbon bundles should be used with group/client limits in mind.
