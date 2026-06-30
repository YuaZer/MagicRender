package io.github.yuazer.magicrender.client.effect.trajectory

import net.minecraft.world.phys.Vec3

data class RibbonVertex(
    val position: Vec3,
    val colorArgb: Int,
    val u: Float,
    val v: Float
)

data class RibbonMesh(
    val texture: String,
    val blendMode: EffectBlendMode,
    val vertices: List<RibbonVertex>
)

object RibbonMeshBuilder {
    fun buildTrail(instance: TrailEffectInstance, cameraPosition: Vec3): RibbonMesh {
        val pointData = instance.state.points.toList()
        return build(
            points = pointData.map { it.position },
            texture = instance.definition.texture,
            blendMode = instance.definition.blendMode,
            cameraPosition = cameraPosition,
            renderMode = instance.definition.renderMode,
            widthAt = { index ->
                val point = pointData[index]
                val t = ageT(point.ageTicks, instance.definition.lifetimeTicks)
                instance.definition.width.evaluate(t)
            },
            colorAt = { index ->
                val point = pointData[index]
                val t = ageT(point.ageTicks, instance.definition.lifetimeTicks)
                multiplyAlpha(instance.definition.color.evaluate(t), instance.definition.alpha.evaluate(t))
            }
        )
    }

    fun buildBeam(instance: BeamEffectInstance, points: List<Vec3>, cameraPosition: Vec3): RibbonMesh {
        return build(
            points = points,
            texture = instance.definition.texture,
            blendMode = instance.definition.blendMode,
            cameraPosition = cameraPosition,
            renderMode = instance.definition.renderMode,
            widthAt = { index ->
                val t = normalizedIndex(index, points.size)
                instance.definition.width.evaluate(t)
            },
            colorAt = { index ->
                val t = normalizedIndex(index, points.size)
                instance.definition.color.evaluate(t)
            }
        )
    }

    private fun build(
        points: List<Vec3>,
        texture: String,
        blendMode: EffectBlendMode,
        cameraPosition: Vec3,
        renderMode: TrailRenderMode,
        widthAt: (Int) -> Double,
        colorAt: (Int) -> Int
    ): RibbonMesh {
        if (points.size < 2) return RibbonMesh(texture, blendMode, emptyList())

        val vertices = ArrayList<RibbonVertex>((points.size - 1) * 6)
        val sideVectors = points.indices.map { index ->
            computeSide(points, index, cameraPosition, renderMode)
        }

        var accumulatedDistance = 0.0
        for (index in 0 until points.lastIndex) {
            val current = points[index]
            val next = points[index + 1]
            val segmentLength = current.distanceTo(next)
            val nextDistance = accumulatedDistance + segmentLength

            val currentHalfWidth = widthAt(index) * 0.5
            val nextHalfWidth = widthAt(index + 1) * 0.5
            val currentSide = sideVectors[index]
            val nextSide = sideVectors[index + 1]

            val currentLeft = current.subtract(currentSide.scale(currentHalfWidth))
            val currentRight = current.add(currentSide.scale(currentHalfWidth))
            val nextLeft = next.subtract(nextSide.scale(nextHalfWidth))
            val nextRight = next.add(nextSide.scale(nextHalfWidth))

            val currentColor = colorAt(index)
            val nextColor = colorAt(index + 1)
            val v0 = accumulatedDistance.toFloat()
            val v1 = nextDistance.toFloat()

            vertices += RibbonVertex(currentLeft, currentColor, 0.0f, v0)
            vertices += RibbonVertex(currentRight, currentColor, 1.0f, v0)
            vertices += RibbonVertex(nextRight, nextColor, 1.0f, v1)

            vertices += RibbonVertex(currentLeft, currentColor, 0.0f, v0)
            vertices += RibbonVertex(nextRight, nextColor, 1.0f, v1)
            vertices += RibbonVertex(nextLeft, nextColor, 0.0f, v1)

            accumulatedDistance = nextDistance
        }

        return RibbonMesh(texture, blendMode, vertices)
    }

    private fun computeSide(points: List<Vec3>, index: Int, cameraPosition: Vec3, renderMode: TrailRenderMode): Vec3 {
        val previous = points[(index - 1).coerceAtLeast(0)]
        val next = points[(index + 1).coerceAtMost(points.lastIndex)]
        val direction = safeNormalize(next.subtract(previous), Vec3(0.0, 0.0, 1.0))
        val reference = when (renderMode) {
            TrailRenderMode.FACE_CAMERA -> safeNormalize(cameraPosition.subtract(points[index]), Vec3(0.0, 1.0, 0.0))
            TrailRenderMode.WORLD_UP -> Vec3(0.0, 1.0, 0.0)
        }
        return safeNormalize(direction.cross(reference), Vec3(1.0, 0.0, 0.0))
    }

    private fun safeNormalize(vector: Vec3, fallback: Vec3): Vec3 {
        return if (vector.length() < 1.0E-5) fallback else vector.normalize()
    }

    private fun ageT(ageTicks: Int, lifetimeTicks: Int): Double {
        return if (lifetimeTicks <= 0) 1.0 else ageTicks.toDouble() / lifetimeTicks.toDouble()
    }

    private fun normalizedIndex(index: Int, size: Int): Double {
        return if (size <= 1) 0.0 else index.toDouble() / (size - 1).toDouble()
    }

    private fun multiplyAlpha(argb: Int, alphaMultiplier: Double): Int {
        val alpha = ((argb ushr 24 and 0xFF) * alphaMultiplier.coerceIn(0.0, 1.0)).toInt().coerceIn(0, 255)
        return alpha shl 24 or (argb and 0x00FFFFFF)
    }
}
