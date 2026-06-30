package io.github.yuazer.magicrender.client.effect.trajectory

import net.minecraft.world.phys.Vec3
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

object AdvancedMeshBuilder {
    fun buildBillboards(instance: AdvancedEffectInstance, source: Vec3, target: Vec3?, cameraPosition: Vec3): List<BillboardMesh> {
        val meshes = ArrayList<BillboardMesh>()
        val basis = billboardBasis(source, cameraPosition)
        val ageT = normalizedAge(instance)

        if (instance.definition.core.enabled) {
            meshes += coreMeshes(instance.definition.core, instance.definition.bloom, source, basis, instance.ageTicks, ageT)
        }

        for ((emitterIndex, emitter) in instance.definition.particleEmitters.withIndex()) {
            val vertices = ArrayList<BillboardVertex>(emitter.count * 6)
            for (index in 0 until emitter.count) {
                val seedValue = seed(instance, emitterIndex * 4099 + index)
                val position = particlePosition(emitter, source, seedValue, index, instance.ageTicks)
                val pointT = ((index.toDouble() / emitter.count.coerceAtLeast(1)) + ageT).mod1()
                val size = lerp(emitter.sizeStart, emitter.sizeEnd, pointT)
                val color = lerpColor(emitter.colorStart, emitter.colorEnd, pointT)
                addBillboard(vertices, position, basis, size, color)
                if (instance.definition.bloom.enabled) addBloom(vertices, position, basis, size, color, instance.definition.bloom)
            }
            if (vertices.isNotEmpty()) meshes += BillboardMesh(emitter.texture, emitter.blendMode, vertices)
        }

        return meshes
    }

    fun buildRibbons(instance: AdvancedEffectInstance, source: Vec3, target: Vec3?, cameraPosition: Vec3): List<RibbonMesh> {
        val meshes = ArrayList<RibbonMesh>()
        val destination = target ?: source.add(Vec3(6.0, 0.0, 0.0))
        for (bundle in instance.definition.ribbonBundles) {
            for (line in 0 until bundle.count) {
                val points = ribbonPoints(bundle, source, destination, line, instance.ageTicks)
                val mesh = RibbonMeshBuilder.buildCustom(
                    points = points,
                    texture = bundle.texture,
                    blendMode = bundle.blendMode,
                    cameraPosition = cameraPosition,
                    renderMode = TrailRenderMode.FACE_CAMERA,
                    widthAt = { index ->
                        val t = normalizedIndex(index, points.size)
                        lerp(bundle.widthStart, bundle.widthEnd, t)
                    },
                    colorAt = { index ->
                        val t = (normalizedIndex(index, points.size) + instance.ageTicks * bundle.flowSpeed * 0.02).mod1()
                        lerpColor(bundle.colorStart, bundle.colorEnd, t)
                    },
                    vOffset = (instance.ageTicks * bundle.flowSpeed).toFloat()
                )
                if (mesh.vertices.isNotEmpty()) meshes += mesh
            }
        }

        for (burst in instance.definition.radialBursts) {
            val rotation = Math.toRadians(instance.ageTicks * burst.rotationSpeed)
            for (ray in 0 until burst.rays) {
                val seed = seed(instance, 9000 + ray)
                val jitter = (seed - 0.5) * burst.randomJitter
                val angle = rotation + ray.toDouble() / burst.rays.coerceAtLeast(1) * PI * 2.0 + jitter
                val end = source.add(Vec3(cos(angle) * burst.length, sin(angle * 1.7) * burst.length * 0.12, sin(angle) * burst.length))
                val points = listOf(source, end)
                val mesh = RibbonMeshBuilder.buildCustom(
                    points = points,
                    texture = burst.texture,
                    blendMode = burst.blendMode,
                    cameraPosition = cameraPosition,
                    renderMode = TrailRenderMode.FACE_CAMERA,
                    widthAt = { index -> if (index == 0) burst.widthStart else burst.widthEnd },
                    colorAt = { index -> if (index == 0) burst.colorStart else burst.colorEnd }
                )
                if (mesh.vertices.isNotEmpty()) meshes += mesh
            }
        }
        return meshes
    }

    fun buildCircles(instance: AdvancedEffectInstance, source: Vec3, cameraPosition: Vec3): List<MagicCircleMesh> {
        val meshes = ArrayList<MagicCircleMesh>()
        for (layer in instance.definition.circleLayers) {
            val definition = MagicCircleDefinition(
                effectId = instance.definition.effectId,
                style = layer.glyphMode,
                radius = layer.radius,
                colorArgb = fadeColor(layer.colorArgb, instance.ageTicks, instance.lifetimeTicks),
                thickness = layer.thickness,
                segments = layer.segments,
                facing = layer.facing,
                rotationSpeedDegreesPerTick = layer.rotationSpeed,
                innerRadiusScale = 0.68,
                glyphs = layer.glyphs,
                blendMode = layer.blendMode
            )
            val circle = MagicCircleInstance(instance.handle, definition, instance.source, instance.lifetimeTicks, instance.ageTicks)
            val mesh = MagicCircleMeshBuilder.build(circle, source, cameraPosition)
            if (mesh.vertices.isNotEmpty()) meshes += mesh
        }
        return meshes
    }

    private fun coreMeshes(core: CoreGlowDefinition, bloom: BloomApproximationDefinition, source: Vec3, basis: BillboardBasis, ageTicks: Int, ageT: Double): List<BillboardMesh> {
        val vertices = ArrayList<BillboardVertex>()
        val pulse = 1.0 + sin(ageTicks * core.pulseSpeed) * core.pulseAmplitude
        val size = core.radius * pulse
        val color = fadeColor(core.colorArgb, ageT)
        addBillboard(vertices, source, basis, size, color)
        if (bloom.enabled) addBloom(vertices, source, basis, size, color, bloom)
        return listOf(BillboardMesh(core.texture, core.blendMode, vertices))
    }

    private fun particlePosition(emitter: ParticleEmitterDefinition, source: Vec3, seedValue: Double, index: Int, ageTicks: Int): Vec3 {
        val a = seedValue * PI * 2.0 + ageTicks * emitter.swirlSpeed * 0.02
        val b = seed(index * 17.0 + seedValue * 31.0) * PI * 2.0
        val drift = ageTicks * emitter.speed
        val noise = sin(ageTicks * 0.07 + index * 12.9898) * emitter.noise
        return when (emitter.shape.lowercase()) {
            "column", "pillar" -> {
                val y = ((seedValue + drift * 0.04).mod1() - 0.5) * emitter.height
                source.add(Vec3(cos(a) * emitter.radius + noise * 0.1, y, sin(a) * emitter.radius + noise * 0.1))
            }
            "disc", "ring" -> source.add(Vec3(cos(a) * emitter.radius, noise * 0.08, sin(a) * emitter.radius))
            "box", "cube" -> source.add(Vec3((seedValue - 0.5) * emitter.radius * 2.0, (seed(index + 9.0) - 0.5) * emitter.height, (seed(index + 19.0) - 0.5) * emitter.radius * 2.0))
            else -> {
                val r = emitter.radius * (0.35 + seed(index + 3.0) * 0.65)
                source.add(Vec3(cos(a) * sin(b) * r, cos(b) * emitter.height * 0.5 + noise * 0.12, sin(a) * sin(b) * r))
            }
        }
    }

    private fun ribbonPoints(bundle: RibbonBundleDefinition, source: Vec3, target: Vec3, line: Int, ageTicks: Int): List<Vec3> {
        val direction = safeNormalize(target.subtract(source), Vec3(1.0, 0.0, 0.0))
        val side = safeNormalize(direction.cross(Vec3(0.0, 1.0, 0.0)), Vec3(0.0, 0.0, 1.0))
        val up = safeNormalize(side.cross(direction), Vec3(0.0, 1.0, 0.0))
        val points = ArrayList<Vec3>(bundle.samples)
        val phase = Math.toRadians(line * bundle.phaseStep + ageTicks * bundle.flowSpeed * 8.0)
        for (index in 0 until bundle.samples) {
            val t = normalizedIndex(index, bundle.samples)
            val along = direction.scale(bundle.length * t)
            val wave = sin(t * PI * 2.0 * bundle.frequency + phase) * bundle.amplitude
            val twist = cos(t * PI * 2.0 * bundle.frequency + phase) * bundle.amplitude * bundle.twist
            points += source.add(along).add(side.scale(wave)).add(up.scale(twist))
        }
        return points
    }

    private fun addBillboard(vertices: MutableList<BillboardVertex>, center: Vec3, basis: BillboardBasis, size: Double, color: Int) {
        val half = size * 0.5
        val right = basis.right.scale(half)
        val up = basis.up.scale(half)
        val p0 = center.subtract(right).subtract(up)
        val p1 = center.add(right).subtract(up)
        val p2 = center.add(right).add(up)
        val p3 = center.subtract(right).add(up)
        vertices += BillboardVertex(p0, color, 0.0f, 1.0f)
        vertices += BillboardVertex(p1, color, 1.0f, 1.0f)
        vertices += BillboardVertex(p2, color, 1.0f, 0.0f)
        vertices += BillboardVertex(p0, color, 0.0f, 1.0f)
        vertices += BillboardVertex(p2, color, 1.0f, 0.0f)
        vertices += BillboardVertex(p3, color, 0.0f, 0.0f)
    }

    private fun addBloom(vertices: MutableList<BillboardVertex>, center: Vec3, basis: BillboardBasis, size: Double, color: Int, bloom: BloomApproximationDefinition) {
        var layerSize = size
        var alphaScale = bloom.alphaFalloff
        repeat(bloom.layers) {
            layerSize *= bloom.scaleStep
            addBillboard(vertices, center, basis, layerSize, scaleAlpha(color, alphaScale))
            alphaScale *= bloom.alphaFalloff
        }
    }

    private fun billboardBasis(center: Vec3, cameraPosition: Vec3): BillboardBasis {
        val normal = safeNormalize(cameraPosition.subtract(center), Vec3(0.0, 0.0, 1.0))
        val right = safeNormalize(Vec3(0.0, 1.0, 0.0).cross(normal), Vec3(1.0, 0.0, 0.0))
        val up = safeNormalize(normal.cross(right), Vec3(0.0, 1.0, 0.0))
        return BillboardBasis(right, up)
    }

    private fun normalizedAge(instance: AdvancedEffectInstance): Double {
        return if (instance.lifetimeTicks <= 0) 1.0 else (instance.ageTicks.toDouble() / instance.lifetimeTicks).coerceIn(0.0, 1.0)
    }

    private fun normalizedIndex(index: Int, size: Int): Double {
        return if (size <= 1) 0.0 else index.toDouble() / (size - 1).toDouble()
    }

    private fun seed(instance: AdvancedEffectInstance, offset: Int): Double {
        return instance.seeds.getOrElse(offset % instance.seeds.size.coerceAtLeast(1)) { 0.5 }
    }

    private fun seed(value: Double): Double {
        val x = sin(value * 12.9898) * 43758.5453
        return x - floor(x)
    }

    private fun lerp(a: Double, b: Double, t: Double): Double = a + (b - a) * t.coerceIn(0.0, 1.0)

    private fun lerpColor(a: Int, b: Int, t: Double): Int {
        val clamped = t.coerceIn(0.0, 1.0)
        fun channel(shift: Int): Int {
            val av = a ushr shift and 0xFF
            val bv = b ushr shift and 0xFF
            return (av + (bv - av) * clamped).toInt().coerceIn(0, 255)
        }
        return channel(24) shl 24 or (channel(16) shl 16) or (channel(8) shl 8) or channel(0)
    }

    private fun fadeColor(argb: Int, ageT: Double): Int {
        val fade = when {
            ageT < 0.08 -> ageT / 0.08
            ageT > 0.88 -> (1.0 - ageT) / 0.12
            else -> 1.0
        }.coerceIn(0.0, 1.0)
        return scaleAlpha(argb, fade)
    }

    private fun fadeColor(argb: Int, ageTicks: Int, lifetimeTicks: Int): Int {
        val t = if (lifetimeTicks <= 0) 1.0 else ageTicks.toDouble() / lifetimeTicks
        return fadeColor(argb, t)
    }

    private fun scaleAlpha(argb: Int, scale: Double): Int {
        val alpha = ((argb ushr 24 and 0xFF) * scale.coerceIn(0.0, 1.0)).toInt().coerceIn(0, 255)
        return alpha shl 24 or (argb and 0x00FFFFFF)
    }

    private fun Double.mod1(): Double {
        val value = this - floor(this)
        return if (value < 0.0) value + 1.0 else value
    }

    private fun safeNormalize(vector: Vec3, fallback: Vec3): Vec3 {
        return if (vector.length() < 1.0E-5) fallback else vector.normalize()
    }

    private data class BillboardBasis(
        val right: Vec3,
        val up: Vec3
    )
}
