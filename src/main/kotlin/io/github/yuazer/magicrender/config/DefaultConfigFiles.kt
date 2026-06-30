package io.github.yuazer.magicrender.config

internal object DefaultConfigFiles {
    const val COMMON = """{
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
"""

    const val SERVER = """{
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
"""

    const val EFFECT_GROUPS = """{
  "version": 1,
  "groups": {
    "default": {
      "enabled": true,
      "description": "Default effect group",
      "priority": 50,
      "limits": {
        "maxActiveEffects": 64,
        "drawDistance": 64
      }
    },
    "combat": {
      "enabled": true,
      "description": "Combat skill effects",
      "priority": 100,
      "limits": {
        "maxActiveEffects": 128,
        "drawDistance": 96
      }
    },
    "ambient": {
      "enabled": true,
      "description": "Ambient effects",
      "priority": 20,
      "limits": {
        "maxActiveEffects": 64,
        "drawDistance": 48
      }
    },
    "debug": {
      "enabled": false,
      "description": "Debug visualization",
      "priority": 0
    }
  }
}
"""

    const val ARCANE_BURST = """{
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
"""

    const val DASH_TRAIL = """{
  "version": 1,
  "id": "magicrender:dash_trail",
  "enabled": true,
  "group": "combat",
  "durationTicks": 30,
  "importance": "normal",
  "visibility": {
    "drawDistance": 96,
    "hideWhenShadersConflict": false
  },
  "components": {
    "trail": {
      "enabled": true,
      "style": "ribbon",
      "lifetimeTicks": 16,
      "maxPoints": 32,
      "minSampleDistance": 0.08,
      "maxSegmentLength": 0.5,
      "maxInsertedPointsPerTick": 4,
      "sampleEveryTick": true,
      "renderMode": "face_camera",
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
"""

    const val MANA_LINK = """{
  "version": 1,
  "id": "magicrender:mana_link",
  "enabled": true,
  "group": "combat",
  "durationTicks": 40,
  "importance": "normal",
  "visibility": {
    "drawDistance": 96,
    "hideWhenShadersConflict": false
  },
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
"""

    const val ENTITY_ARCANE_STREAM = """{
  "version": 1,
  "id": "magicrender:entity_arcane_stream",
  "enabled": true,
  "group": "combat",
  "durationTicks": 80,
  "importance": "high",
  "visibility": {
    "drawDistance": 96,
    "hideWhenShadersConflict": false
  },
  "components": {
    "trail": {
      "enabled": true,
      "style": "helix_stream",
      "lifetimeTicks": 42,
      "maxPoints": 96,
      "minSampleDistance": 0.03,
      "maxSegmentLength": 0.28,
      "maxInsertedPointsPerTick": 4,
      "sampleEveryTick": true,
      "renderMode": "face_camera",
      "blendMode": "additive",
      "motion": {
        "mode": "helix",
        "radius": 0.9,
        "angularSpeed": 16.0,
        "verticalAmplitude": 0.42,
        "verticalSpeed": 10.0,
        "phase": 0.0
      },
      "width": {
        "start": 0.24,
        "end": 0.02
      },
      "color": {
        "start": "#FFF6B640",
        "end": "#22FF4FB8"
      },
      "texture": "minecraft:textures/misc/white.png"
    },
    "magicCircle": {
      "enabled": true,
      "style": "arcane_gate",
      "radius": 1.65,
      "color": "#CCFFD24A",
      "thickness": 0.055,
      "segments": 128,
      "facing": "face_camera",
      "rotationSpeed": 1.2,
      "innerRadiusScale": 0.68,
      "glyphs": 18,
      "blendMode": "additive"
    },
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
      ],
      "circleLayers": [
        {
          "enabled": true,
          "radius": 2.2,
          "thickness": 0.045,
          "color": "#FFFFFF22",
          "segments": 160,
          "rotationSpeed": 1.0,
          "glyphs": 18,
          "glyphMode": "ticks",
          "facing": "face_camera",
          "blendMode": "additive"
        }
      ],
      "radialBursts": [
        {
          "enabled": true,
          "rays": 22,
          "length": 2.8,
          "width": {
            "start": 0.09,
            "end": 0.0
          },
          "color": {
            "start": "#FFFF66FF",
            "end": "#00FF66FF"
          },
          "rotationSpeed": 0.45,
          "texture": "minecraft:textures/particle/flash.png",
          "blendMode": "additive"
        }
      ]
    }
  }
}
"""
}
