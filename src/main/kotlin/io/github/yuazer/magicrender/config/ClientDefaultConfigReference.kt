package io.github.yuazer.magicrender.config

fun clientDefaultJson(): String {
    return """{
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
  },
  "editor": {
    "enabled": true,
    "host": "127.0.0.1",
    "port": 3566
  }
}
"""
}
