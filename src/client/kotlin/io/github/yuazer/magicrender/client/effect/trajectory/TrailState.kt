package io.github.yuazer.magicrender.client.effect.trajectory

import net.minecraft.world.phys.Vec3
import java.util.ArrayDeque

data class TrailPoint(
    val position: Vec3,
    val ageTicks: Int,
    val spawnTimeNanos: Long,
    val width: Double,
    val colorArgb: Int
)

class TrailState {
    val points: ArrayDeque<TrailPoint> = ArrayDeque()
    var lastSamplePosition: Vec3? = null
    var enabled: Boolean = true

    fun isEmpty(): Boolean = points.isEmpty()
}

data class TrailEffectInstance(
    val handle: Long,
    val definition: TrailDefinition,
    val anchor: TrailAnchor,
    val state: TrailState = TrailState()
) {
    fun isAlive(): Boolean {
        return state.enabled || !state.isEmpty()
    }
}

data class BeamEffectInstance(
    val handle: Long,
    val definition: BeamDefinition,
    val from: TrailAnchor,
    val to: TrailAnchor,
    var ageTicks: Int = 0,
    val lifetimeTicks: Int = 20
) {
    fun isAlive(): Boolean = ageTicks <= lifetimeTicks
}

