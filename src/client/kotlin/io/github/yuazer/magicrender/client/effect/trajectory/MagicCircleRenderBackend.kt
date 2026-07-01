package io.github.yuazer.magicrender.client.effect.trajectory

import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.BufferUploader
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.Tesselator
import com.mojang.blaze3d.vertex.VertexFormat
import net.minecraft.client.renderer.GameRenderer
import net.minecraft.world.phys.Vec3

object MagicCircleRenderBackend {
    fun render(meshes: List<MagicCircleMesh>, cameraPosition: Vec3) {
        render(meshes, cameraPosition, glowOnly = false)
    }

    fun renderGlow(meshes: List<MagicCircleMesh>, cameraPosition: Vec3, intensity: Double) {
        render(meshes.map { it.glow(intensity) }, cameraPosition, glowOnly = true)
    }

    private fun render(meshes: List<MagicCircleMesh>, cameraPosition: Vec3, glowOnly: Boolean) {
        if (meshes.isEmpty()) return

        RenderSystem.enableBlend()
        RenderSystem.enableDepthTest()
        RenderSystem.depthMask(false)
        RenderSystem.disableCull()

        try {
            RenderSystem.setShader { GameRenderer.getPositionColorShader() }
            for ((blendMode, meshGroup) in meshes.groupBy { it.blendMode }) {
                if (glowOnly) {
                    RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE)
                } else {
                    applyBlendMode(blendMode)
                }
                renderGroup(meshGroup, cameraPosition)
            }
        } finally {
            RenderSystem.depthMask(true)
            RenderSystem.enableCull()
            RenderSystem.disableBlend()
        }
    }

    private fun renderGroup(meshes: List<MagicCircleMesh>, cameraPosition: Vec3) {
        val vertexCount = meshes.sumOf { it.vertices.size }
        if (vertexCount == 0) return

        val builder = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR)
        for (mesh in meshes) {
            for (vertex in mesh.vertices) {
                val relative = vertex.position.subtract(cameraPosition)
                builder
                    .addVertex(relative.x.toFloat(), relative.y.toFloat(), relative.z.toFloat())
                    .setColor(vertex.colorArgb)
            }
        }
        BufferUploader.drawWithShader(builder.buildOrThrow())
    }

    private fun applyBlendMode(blendMode: EffectBlendMode) {
        when (blendMode) {
            EffectBlendMode.ALPHA -> RenderSystem.defaultBlendFunc()
            EffectBlendMode.ADDITIVE -> RenderSystem.blendFunc(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE
            )
        }
    }

    private fun MagicCircleMesh.glow(intensity: Double): MagicCircleMesh {
        return copy(vertices = vertices.map { vertex ->
            vertex.copy(colorArgb = scaleForGlow(vertex.colorArgb, intensity))
        })
    }

    private fun scaleForGlow(color: Int, intensity: Double): Int {
        val scale = intensity.coerceAtLeast(0.0)
        val alpha = ((color ushr 24 and 0xFF) * scale).toInt().coerceIn(0, 255)
        val red = ((color ushr 16 and 0xFF) * scale).toInt().coerceIn(0, 255)
        val green = ((color ushr 8 and 0xFF) * scale).toInt().coerceIn(0, 255)
        val blue = ((color and 0xFF) * scale).toInt().coerceIn(0, 255)
        return alpha shl 24 or (red shl 16) or (green shl 8) or blue
    }
}
