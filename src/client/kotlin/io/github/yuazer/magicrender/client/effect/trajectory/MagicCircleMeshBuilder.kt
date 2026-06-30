package io.github.yuazer.magicrender.client.effect.trajectory

import net.minecraft.world.phys.Vec3
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object MagicCircleMeshBuilder {
    fun build(instance: MagicCircleInstance, center: Vec3, cameraPosition: Vec3): MagicCircleMesh {
        val definition = instance.definition
        val basis = computeBasis(definition.facing, center, cameraPosition)
        val rotation = Math.toRadians(instance.ageTicks * definition.rotationSpeedDegreesPerTick)
        val vertices = ArrayList<MagicCircleVertex>(definition.segments * 18)
        val color = fadeColor(definition.colorArgb, instance.ageTicks, instance.lifetimeTicks)

        addRing(
            vertices = vertices,
            center = center,
            basis = basis,
            radius = definition.radius,
            thickness = definition.thickness,
            segments = definition.segments,
            rotation = rotation,
            color = color
        )

        addRing(
            vertices = vertices,
            center = center,
            basis = basis,
            radius = definition.radius * definition.innerRadiusScale,
            thickness = definition.thickness * 0.55,
            segments = definition.segments,
            rotation = -rotation * 0.65,
            color = scaleAlpha(color, 0.72)
        )

        addGlyphTicks(
            vertices = vertices,
            center = center,
            basis = basis,
            radius = definition.radius * 0.86,
            length = definition.radius * 0.22,
            thickness = definition.thickness * 0.75,
            count = definition.glyphs,
            rotation = rotation,
            color = scaleAlpha(color, 0.9)
        )

        return MagicCircleMesh(definition.blendMode, vertices)
    }

    private fun addRing(
        vertices: MutableList<MagicCircleVertex>,
        center: Vec3,
        basis: PlaneBasis,
        radius: Double,
        thickness: Double,
        segments: Int,
        rotation: Double,
        color: Int
    ) {
        val inner = (radius - thickness * 0.5).coerceAtLeast(0.01)
        val outer = radius + thickness * 0.5
        for (index in 0 until segments) {
            val a0 = rotation + index.toDouble() / segments.toDouble() * PI * 2.0
            val a1 = rotation + (index + 1).toDouble() / segments.toDouble() * PI * 2.0
            val inner0 = pointOnPlane(center, basis, inner, a0)
            val outer0 = pointOnPlane(center, basis, outer, a0)
            val inner1 = pointOnPlane(center, basis, inner, a1)
            val outer1 = pointOnPlane(center, basis, outer, a1)

            addTriangle(vertices, inner0, outer0, outer1, color)
            addTriangle(vertices, inner0, outer1, inner1, color)
        }
    }

    private fun addGlyphTicks(
        vertices: MutableList<MagicCircleVertex>,
        center: Vec3,
        basis: PlaneBasis,
        radius: Double,
        length: Double,
        thickness: Double,
        count: Int,
        rotation: Double,
        color: Int
    ) {
        if (count <= 0) return
        for (index in 0 until count) {
            val angle = rotation + index.toDouble() / count.toDouble() * PI * 2.0
            val tangentAngle = angle + PI * 0.5
            val radial = basis.right.scale(cos(angle)).add(basis.up.scale(sin(angle)))
            val tangent = basis.right.scale(cos(tangentAngle)).add(basis.up.scale(sin(tangentAngle)))
            val tickCenter = center.add(radial.scale(radius))
            val halfLength = length * 0.5
            val halfThickness = thickness * 0.5

            val p0 = tickCenter.subtract(tangent.scale(halfLength)).subtract(radial.scale(halfThickness))
            val p1 = tickCenter.add(tangent.scale(halfLength)).subtract(radial.scale(halfThickness))
            val p2 = tickCenter.add(tangent.scale(halfLength)).add(radial.scale(halfThickness))
            val p3 = tickCenter.subtract(tangent.scale(halfLength)).add(radial.scale(halfThickness))
            addQuad(vertices, p0, p1, p2, p3, color)
        }
    }

    private fun pointOnPlane(center: Vec3, basis: PlaneBasis, radius: Double, angle: Double): Vec3 {
        return center
            .add(basis.right.scale(cos(angle) * radius))
            .add(basis.up.scale(sin(angle) * radius))
    }

    private fun addQuad(vertices: MutableList<MagicCircleVertex>, p0: Vec3, p1: Vec3, p2: Vec3, p3: Vec3, color: Int) {
        addTriangle(vertices, p0, p1, p2, color)
        addTriangle(vertices, p0, p2, p3, color)
    }

    private fun addTriangle(vertices: MutableList<MagicCircleVertex>, p0: Vec3, p1: Vec3, p2: Vec3, color: Int) {
        vertices += MagicCircleVertex(p0, color)
        vertices += MagicCircleVertex(p1, color)
        vertices += MagicCircleVertex(p2, color)
    }

    private fun computeBasis(facing: MagicCircleFacing, center: Vec3, cameraPosition: Vec3): PlaneBasis {
        return when (facing) {
            MagicCircleFacing.HORIZONTAL -> PlaneBasis(Vec3(1.0, 0.0, 0.0), Vec3(0.0, 0.0, 1.0))
            MagicCircleFacing.FACE_CAMERA -> {
                val normal = safeNormalize(cameraPosition.subtract(center), Vec3(0.0, 0.0, 1.0))
                val right = safeNormalize(Vec3(0.0, 1.0, 0.0).cross(normal), Vec3(1.0, 0.0, 0.0))
                val up = safeNormalize(normal.cross(right), Vec3(0.0, 1.0, 0.0))
                PlaneBasis(right, up)
            }
        }
    }

    private fun safeNormalize(vector: Vec3, fallback: Vec3): Vec3 {
        return if (vector.length() < 1.0E-5) fallback else vector.normalize()
    }

    private fun fadeColor(argb: Int, ageTicks: Int, lifetimeTicks: Int): Int {
        if (lifetimeTicks <= 0) return argb
        val age = ageTicks.toDouble() / lifetimeTicks.toDouble()
        val fade = when {
            age < 0.12 -> age / 0.12
            age > 0.82 -> (1.0 - age) / 0.18
            else -> 1.0
        }.coerceIn(0.0, 1.0)
        return scaleAlpha(argb, fade)
    }

    private fun scaleAlpha(argb: Int, scale: Double): Int {
        val alpha = ((argb ushr 24 and 0xFF) * scale.coerceIn(0.0, 1.0)).toInt().coerceIn(0, 255)
        return alpha shl 24 or (argb and 0x00FFFFFF)
    }

    private data class PlaneBasis(
        val right: Vec3,
        val up: Vec3
    )
}
