# MagicRender

English | [简体中文](README.zh-CN.md)

MagicRender is a Fabric mod for Minecraft `1.21.1` that provides configurable client-side magic visual effects, a local web editor, and server-to-client playback APIs.

The project focuses on world-space visual effects: motion trails, beams, magic circles, particle-like point clouds, flowing ribbon bundles, radial burst rays, grouped effects, and true screen-space glow for advanced layers.

## Requirements

- Minecraft `1.21.1`
- Java `21`
- Fabric Loader `0.16.10+`
- Fabric API
- Fabric Language Kotlin `1.13.4+kotlin.2.2.0+`

## Main Features

- Config-driven effects loaded from `config/magicrender`.
- Client-side rendering for:
  - ribbon trails
  - beams
  - magic circles
  - advanced core/particle/ribbon/circle/burst layers
  - true post-process glow for advanced geometry
- Effect groups: one group key can play multiple effect ids at the same time.
- Frame-level client following: entity-attached effects interpolate on render frames instead of depending only on ticks.
- Local browser editor with Chinese/English UI, hover help, draggable panel layout, effect-group project tree, and 3D preview.
- Public client API and server API for playing/stopping single effects and effect groups.
- Commands for reload, validation, test playback, and client-side binding.

## Configuration Layout

Default configuration files are generated under:

```text
config/magicrender/
```

Current layout:

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

`effects/*.json` contains individual effect definitions.

`effects_group/*.json` contains effect-group project definitions. `effect_groups.json` is no longer used; group configuration is loaded from the `effects_group` directory only.

Effect ids and group keys use the standard `namespace:path` format:

```json
"magicrender:arcane_burst"
```

## Effect Definition

Minimal shape:

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

Colors use `#RRGGBB` or `#RRGGBBAA`.

## Effect Groups

An effect group maps a group key to one or more child effect ids. Playing the group plays every referenced effect at the same source/target anchor.

Example file:

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

Array shorthand is also supported:

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

## Components

### Trail

`components.trail` draws a ribbon following a source anchor. It supports width/color curves, texture, blend mode, sampling settings, segment interpolation, and motion offsets.

Motion modes:

- `follow`
- `orbit`
- `helix`
- `spiral`
- `formula`
- `js`
- `javascript`

Formula mode uses JavaScript-style mathematical expressions, not arbitrary script execution.

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

`components.magicCircle` draws a camera-facing or horizontal circle with an inner ring and glyph tick marks.

It supports:

- radius
- thickness
- color
- segment count
- rotation speed
- facing mode
- glyph count
- alpha/additive blend mode

### Advanced

`components.advanced` is the layered visual system for magic cores, point clouds, flowing light streams, layered runic circles, and radial energy bursts.

It contains:

- `bloom`: legacy pseudo-bloom layering for compatibility.
- `glow`: true screen-space glow settings.
- `core`: central glow sprite.
- `particleEmitters[]`: billboard/point-cloud emitters.
- `ribbonBundles[]`: multiple flowing ribbon curves.
- `circleLayers[]`: multiple independent circle layers.
- `radialBursts[]`: ray-like burst strokes from the source.

True glow renders bright advanced geometry to an offscreen buffer, blurs it, and additively composites it back into the scene.

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
  "glow": {
    "enabled": true,
    "intensity": 1.35,
    "radius": 1.0,
    "iterations": 4,
    "downsample": 2,
    "threshold": 0.0
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

## Local Web Editor

The client starts a local editor web server when enabled in `client.json`.

Editor assets:

```text
src/main/resources/assets/magicrender/editor/
```

The editor supports:

- Chinese/English switching.
- Effect-group project editing.
- Tree management for child effects.
- Adding a new child effect under a group.
- Importing loaded effects as child effects.
- Exporting a whole group to `config/magicrender/effects_group`.
- Exporting all child effects to `config/magicrender/effects` when group export includes children.
- Exporting a selected child effect separately.
- Form editing for common effect fields.
- Hover help for parameters.
- 3D preview with orbit/pan/zoom, reset camera, expanded preview, motion trails, ribbon bundles, and approximate preview glow.
- Draggable layout columns for effect groups, effect type tabs, parameter fields, and preview/status/JSON panels.

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
/magicrender play group <groupKey>
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
/mrender bind group <groupKey>
/mrender bindGroup <groupKey>
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

`playEffect` automatically includes enabled trail, magic circle, beam, and advanced components.

`playGroup` resolves `groupKey` through the loaded group bindings and plays all referenced effect ids.

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

The server API sends Fabric S2C payloads to the client. The client receives the payload and calls `MagicRenderClientApi`.

## Networking

Payloads are registered in:

```text
io.github.yuazer.magicrender.network.MagicRenderPayloads
```

Current S2C payloads:

- `magicrender:play_effect`
- `magicrender:stop_effect`

Play payloads support:

- single effect playback
- effect-group playback
- entity anchors
- world-point anchors
- optional target anchors

Stop payloads support:

- request id
- effect id
- group key
- entity id
- all effects

## Rendering Notes

- Entity-bound effects are resolved against interpolated render-frame positions, reducing visual separation from moving entities.
- Trail sampling can update per render frame and ages samples using frame time.
- Advanced, trail, beam, and magic-circle managers prepare render-frame data before drawing.
- True glow is available for advanced components through `components.advanced.glow`.
- The web editor preview approximates true glow; the in-game renderer uses the actual offscreen glow processor.

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
  editor/       Editor draft/export/preview/web-server logic
  effect/       Runtime render managers/builders/backends
  network/      Client payload receivers

src/main/resources/assets/magicrender/
  editor/       Local web editor frontend
  lang/         Minecraft language files
  i18n/         Runtime translation resources
```

## Notes

- Resource ids must use `namespace:path`.
- Effect-group keys also use `namespace:path`.
- Colors use `#RRGGBB` or `#RRGGBBAA`.
- Additive blending is best for energy/glow effects.
- Alpha blending is better for smoke, water, or non-glowing transparent visuals.
- Legacy pseudo-bloom increases draw calls because it renders extra translucent layers.
- True glow costs extra offscreen rendering and blur passes; tune `downsample`, `iterations`, and `radius` for performance.
- Large particle counts, long trails, and many ribbon bundles should be used with group/client limits in mind.
