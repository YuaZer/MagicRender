package io.github.yuazer.magicrender.client.effect.trajectory

import net.minecraft.world.phys.Vec3

data class TrajectoryRenderContext(
    val cameraPosition: Vec3,
    val tickDelta: Float,
    val renderTimeTicks: Double,
    val nowNanos: Long
)
