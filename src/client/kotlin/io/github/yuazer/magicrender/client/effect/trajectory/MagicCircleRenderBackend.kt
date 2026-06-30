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
        if (meshes.isEmpty()) return

        RenderSystem.enableBlend()
        RenderSystem.enableDepthTest()
        RenderSystem.depthMask(false)
        RenderSystem.disableCull()

        try {
            RenderSystem.setShader { GameRenderer.getPositionColorShader() }
            for ((blendMode, meshGroup) in meshes.groupBy { it.blendMode }) {
                applyBlendMode(blendMode)
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
}
