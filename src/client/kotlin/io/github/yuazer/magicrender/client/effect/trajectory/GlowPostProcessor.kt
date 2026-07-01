package io.github.yuazer.magicrender.client.effect.trajectory

import com.mojang.blaze3d.pipeline.RenderTarget
import com.mojang.blaze3d.pipeline.TextureTarget
import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import io.github.yuazer.magicrender.client.config.ClientConfigReloader
import net.minecraft.client.Minecraft
import net.minecraft.world.phys.Vec3
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL12
import org.lwjgl.opengl.GL13
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30

object GlowPostProcessor {
    private var glowTarget: RenderTarget? = null
    private var blurA: RenderTarget? = null
    private var blurB: RenderTarget? = null
    private var extractProgram = 0
    private var blurProgram = 0
    private var compositeProgram = 0
    private var quadVao = 0
    private var quadVbo = 0

    fun render(
        glow: GlowPostDefinition?,
        cameraPosition: Vec3,
        ribbons: List<RibbonMesh>,
        circles: List<MagicCircleMesh>,
        billboards: List<BillboardMesh>
    ) {
        val definition = glow ?: return
        if (!definition.enabled || definition.intensity <= 0.0) return
        if (!ClientConfigReloader.current.visuals.screenGlow) return
        if (!ClientConfigReloader.compatibility.offscreenCompositionAllowed) return
        if (ribbons.isEmpty() && circles.isEmpty() && billboards.isEmpty()) return

        ensurePrograms()
        val minecraft = Minecraft.getInstance()
        val mainTarget = minecraft.mainRenderTarget
        val targets = ensureTargets(mainTarget.width, mainTarget.height, definition.downsample)

        targets.glow.clear(Minecraft.ON_OSX)
        targets.glow.bindWrite(true)
        GL11.glViewport(0, 0, targets.glow.width, targets.glow.height)
        RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT, Minecraft.ON_OSX)
        RibbonRenderBackend.renderGlow(ribbons, cameraPosition, definition.intensity)
        MagicCircleRenderBackend.renderGlow(circles, cameraPosition, definition.intensity)
        BillboardRenderBackend.renderGlow(billboards, cameraPosition, definition.intensity)

        extract(targets.glow, targets.a, definition)
        var source = targets.a
        var destination = targets.b
        repeat(definition.iterations.coerceIn(1, 12)) {
            blur(source, destination, 0.0f, 1.0f, definition)
            val swap = source
            source = destination
            destination = swap
            blur(source, destination, 1.0f, 0.0f, definition)
            val swapBack = source
            source = destination
            destination = swapBack
        }

        mainTarget.bindWrite(false)
        GL11.glViewport(0, 0, mainTarget.width, mainTarget.height)
        composite(source, definition.intensity.toFloat())
    }

    fun close() {
        glowTarget?.destroyBuffers()
        blurA?.destroyBuffers()
        blurB?.destroyBuffers()
        glowTarget = null
        blurA = null
        blurB = null
        if (extractProgram != 0) GL20.glDeleteProgram(extractProgram)
        if (blurProgram != 0) GL20.glDeleteProgram(blurProgram)
        if (compositeProgram != 0) GL20.glDeleteProgram(compositeProgram)
        if (quadVbo != 0) GL30.glDeleteBuffers(quadVbo)
        if (quadVao != 0) GL30.glDeleteVertexArrays(quadVao)
        extractProgram = 0
        blurProgram = 0
        compositeProgram = 0
        quadVbo = 0
        quadVao = 0
    }

    private fun ensureTargets(mainWidth: Int, mainHeight: Int, downsample: Int): Targets {
        val divisor = downsample.coerceIn(1, 8)
        val width = (mainWidth / divisor).coerceAtLeast(1)
        val height = (mainHeight / divisor).coerceAtLeast(1)
        val currentGlow = glowTarget
        val currentA = blurA
        val currentB = blurB
        if (currentGlow != null && currentA != null && currentB != null &&
            currentGlow.width == width && currentGlow.height == height &&
            currentA.width == width && currentA.height == height &&
            currentB.width == width && currentB.height == height
        ) {
            return Targets(currentGlow, currentA, currentB)
        }

        currentGlow?.destroyBuffers()
        currentA?.destroyBuffers()
        currentB?.destroyBuffers()
        glowTarget = TextureTarget(width, height, false, Minecraft.ON_OSX)
        blurA = TextureTarget(width, height, false, Minecraft.ON_OSX)
        blurB = TextureTarget(width, height, false, Minecraft.ON_OSX)
        return Targets(glowTarget!!, blurA!!, blurB!!)
    }

    private fun blur(source: RenderTarget, destination: RenderTarget, directionX: Float, directionY: Float, glow: GlowPostDefinition) {
        destination.clear(Minecraft.ON_OSX)
        destination.bindWrite(true)
        GL11.glViewport(0, 0, destination.width, destination.height)
        RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT, Minecraft.ON_OSX)
        RenderSystem.disableDepthTest()
        RenderSystem.depthMask(false)
        RenderSystem.disableCull()
        RenderSystem.disableBlend()
        GL20.glUseProgram(blurProgram)
        GL20.glUniform1i(GL20.glGetUniformLocation(blurProgram, "uTexture"), 0)
        GL20.glUniform2f(GL20.glGetUniformLocation(blurProgram, "uTexelSize"), 1.0f / source.width.toFloat(), 1.0f / source.height.toFloat())
        GL20.glUniform2f(GL20.glGetUniformLocation(blurProgram, "uDirection"), directionX, directionY)
        GL20.glUniform1f(GL20.glGetUniformLocation(blurProgram, "uRadius"), glow.radius.toFloat().coerceAtLeast(0.25f))
        bindTexture(source.colorTextureId)
        drawQuad()
        GL20.glUseProgram(0)
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0)
        RenderSystem.depthMask(true)
        RenderSystem.enableDepthTest()
        RenderSystem.enableCull()
    }

    private fun extract(source: RenderTarget, destination: RenderTarget, glow: GlowPostDefinition) {
        destination.clear(Minecraft.ON_OSX)
        destination.bindWrite(true)
        GL11.glViewport(0, 0, destination.width, destination.height)
        RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT, Minecraft.ON_OSX)
        RenderSystem.disableDepthTest()
        RenderSystem.depthMask(false)
        RenderSystem.disableCull()
        RenderSystem.disableBlend()
        GL20.glUseProgram(extractProgram)
        GL20.glUniform1i(GL20.glGetUniformLocation(extractProgram, "uTexture"), 0)
        GL20.glUniform1f(GL20.glGetUniformLocation(extractProgram, "uThreshold"), glow.threshold.toFloat().coerceIn(0.0f, 1.0f))
        bindTexture(source.colorTextureId)
        drawQuad()
        GL20.glUseProgram(0)
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0)
        RenderSystem.depthMask(true)
        RenderSystem.enableDepthTest()
        RenderSystem.enableCull()
    }

    private fun composite(source: RenderTarget, intensity: Float) {
        RenderSystem.disableDepthTest()
        RenderSystem.depthMask(false)
        RenderSystem.disableCull()
        RenderSystem.enableBlend()
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE)
        GL20.glUseProgram(compositeProgram)
        GL20.glUniform1i(GL20.glGetUniformLocation(compositeProgram, "uTexture"), 0)
        GL20.glUniform1f(GL20.glGetUniformLocation(compositeProgram, "uIntensity"), intensity.coerceAtLeast(0.0f))
        bindTexture(source.colorTextureId)
        drawQuad()
        GL20.glUseProgram(0)
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0)
        RenderSystem.depthMask(true)
        RenderSystem.disableBlend()
        RenderSystem.enableDepthTest()
        RenderSystem.enableCull()
    }

    private fun bindTexture(textureId: Int) {
        GL13.glActiveTexture(GL13.GL_TEXTURE0)
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE)
    }

    private fun drawQuad() {
        if (quadVao == 0) createQuad()
        GL30.glBindVertexArray(quadVao)
        GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, 4)
        GL30.glBindVertexArray(0)
    }

    private fun createQuad() {
        val vertices = floatArrayOf(
            -1.0f, -1.0f, 0.0f, 0.0f,
            1.0f, -1.0f, 1.0f, 0.0f,
            -1.0f, 1.0f, 0.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f
        )
        quadVao = GL30.glGenVertexArrays()
        quadVbo = GL30.glGenBuffers()
        GL30.glBindVertexArray(quadVao)
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, quadVbo)
        GL30.glBufferData(GL30.GL_ARRAY_BUFFER, vertices, GL30.GL_STATIC_DRAW)
        GL20.glEnableVertexAttribArray(0)
        GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, 4 * Float.SIZE_BYTES, 0L)
        GL20.glEnableVertexAttribArray(1)
        GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 4 * Float.SIZE_BYTES, (2 * Float.SIZE_BYTES).toLong())
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, 0)
        GL30.glBindVertexArray(0)
    }

    private fun ensurePrograms() {
        if (extractProgram == 0) extractProgram = createProgram(VERTEX_SHADER, EXTRACT_FRAGMENT_SHADER)
        if (blurProgram == 0) blurProgram = createProgram(VERTEX_SHADER, BLUR_FRAGMENT_SHADER)
        if (compositeProgram == 0) compositeProgram = createProgram(VERTEX_SHADER, COMPOSITE_FRAGMENT_SHADER)
    }

    private fun createProgram(vertexSource: String, fragmentSource: String): Int {
        val vertex = compileShader(GL20.GL_VERTEX_SHADER, vertexSource)
        val fragment = compileShader(GL20.GL_FRAGMENT_SHADER, fragmentSource)
        val program = GL20.glCreateProgram()
        GL20.glAttachShader(program, vertex)
        GL20.glAttachShader(program, fragment)
        GL20.glBindAttribLocation(program, 0, "aPosition")
        GL20.glBindAttribLocation(program, 1, "aUv")
        GL20.glLinkProgram(program)
        if (GL20.glGetProgrami(program, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
            val log = GL20.glGetProgramInfoLog(program)
            GL20.glDeleteProgram(program)
            error("MagicRender glow shader link failed: $log")
        }
        GL20.glDeleteShader(vertex)
        GL20.glDeleteShader(fragment)
        return program
    }

    private fun compileShader(type: Int, source: String): Int {
        val shader = GL20.glCreateShader(type)
        GL20.glShaderSource(shader, source)
        GL20.glCompileShader(shader)
        if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            val log = GL20.glGetShaderInfoLog(shader)
            GL20.glDeleteShader(shader)
            error("MagicRender glow shader compile failed: $log")
        }
        return shader
    }

    private data class Targets(
        val glow: RenderTarget,
        val a: RenderTarget,
        val b: RenderTarget
    )

    private const val VERTEX_SHADER = """
        #version 150
        in vec2 aPosition;
        in vec2 aUv;
        out vec2 vUv;
        void main() {
            vUv = aUv;
            gl_Position = vec4(aPosition, 0.0, 1.0);
        }
    """

    private const val BLUR_FRAGMENT_SHADER = """
        #version 150
        uniform sampler2D uTexture;
        uniform vec2 uTexelSize;
        uniform vec2 uDirection;
        uniform float uRadius;
        in vec2 vUv;
        out vec4 fragColor;
        void main() {
            vec2 stepUv = uDirection * uTexelSize * uRadius;
            vec4 color = texture(uTexture, vUv) * 0.227027;
            color += texture(uTexture, vUv + stepUv * 1.384615) * 0.316216;
            color += texture(uTexture, vUv - stepUv * 1.384615) * 0.316216;
            color += texture(uTexture, vUv + stepUv * 3.230769) * 0.070270;
            color += texture(uTexture, vUv - stepUv * 3.230769) * 0.070270;
            fragColor = color;
        }
    """

    private const val EXTRACT_FRAGMENT_SHADER = """
        #version 150
        uniform sampler2D uTexture;
        uniform float uThreshold;
        in vec2 vUv;
        out vec4 fragColor;
        void main() {
            vec4 color = texture(uTexture, vUv);
            float brightness = max(max(color.r, color.g), color.b) * color.a;
            float keep = smoothstep(uThreshold, min(1.0, uThreshold + 0.08), brightness);
            fragColor = vec4(color.rgb * keep, color.a * keep);
        }
    """

    private const val COMPOSITE_FRAGMENT_SHADER = """
        #version 150
        uniform sampler2D uTexture;
        uniform float uIntensity;
        in vec2 vUv;
        out vec4 fragColor;
        void main() {
            vec4 glow = texture(uTexture, vUv);
            fragColor = vec4(glow.rgb * uIntensity, glow.a);
        }
    """
}
