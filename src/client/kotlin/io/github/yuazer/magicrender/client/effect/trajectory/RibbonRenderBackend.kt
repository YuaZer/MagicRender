package io.github.yuazer.magicrender.client.effect.trajectory

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.vertex.BufferUploader
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.Tesselator
import com.mojang.blaze3d.vertex.VertexFormat
import net.minecraft.client.renderer.GameRenderer
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.phys.Vec3

object RibbonRenderBackend {
    private val fallbackTexture = ResourceLocation.withDefaultNamespace("textures/misc/white.png")

    fun render(meshes: List<RibbonMesh>, cameraPosition: Vec3) {
        render(meshes, cameraPosition, glowOnly = false)
    }

    fun renderGlow(meshes: List<RibbonMesh>, cameraPosition: Vec3, intensity: Double) {
        render(meshes.map { it.glow(intensity) }, cameraPosition, glowOnly = true)
    }

    private fun render(meshes: List<RibbonMesh>, cameraPosition: Vec3, glowOnly: Boolean) {
        if (meshes.isEmpty()) return

        RenderSystem.enableBlend()
        RenderSystem.defaultBlendFunc()
        RenderSystem.enableDepthTest()
        RenderSystem.depthMask(false)
        RenderSystem.disableCull()

        try {
            RenderSystem.setShader { GameRenderer.getPositionTexColorShader() }
            for ((key, meshGroup) in meshes.groupBy { MeshRenderKey(it.texture, it.blendMode) }) {
                if (glowOnly) {
                    RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE)
                } else {
                    applyBlendMode(key.blendMode)
                }
                renderGroup(key.texture, meshGroup, cameraPosition)
            }
        } finally {
            RenderSystem.depthMask(true)
            RenderSystem.enableCull()
            RenderSystem.disableBlend()
        }
    }

    private fun renderGroup(texture: String, meshes: List<RibbonMesh>, cameraPosition: Vec3) {
        val vertexCount = meshes.sumOf { it.vertices.size }
        if (vertexCount == 0) return

        RenderSystem.setShaderTexture(0, textureLocation(texture))
        val builder = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_TEX_COLOR)

        for (mesh in meshes) {
            for (vertex in mesh.vertices) {
                val relative = vertex.position.subtract(cameraPosition)
                builder
                    .addVertex(relative.x.toFloat(), relative.y.toFloat(), relative.z.toFloat())
                    .setUv(vertex.u, vertex.v)
                    .setColor(vertex.colorArgb)
            }
        }

        BufferUploader.drawWithShader(builder.buildOrThrow())
    }

    private fun textureLocation(value: String): ResourceLocation {
        return runCatching {
            if (value.isBlank()) fallbackTexture else ResourceLocation.parse(value)
        }.getOrDefault(fallbackTexture)
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

    private data class MeshRenderKey(
        val texture: String,
        val blendMode: EffectBlendMode
    )

    private fun RibbonMesh.glow(intensity: Double): RibbonMesh {
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
