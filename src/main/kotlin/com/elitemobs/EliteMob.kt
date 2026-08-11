package com.elitemobs

import org.bukkit.Bukkit
import org.bukkit.entity.LivingEntity
import java.util.*

class EliteMob(
    private val plugin: EliteMobsPlugin,
    val entity: LivingEntity,
    val mobType: String,
    val tier: MobTier = MobTier.COMMON
) {
    val uuid: UUID = entity.uniqueId
    private var tickCount = 0

    fun tick() {
        if (!entity.isValid || entity.isDead) {
            plugin.removeEliteMob(uuid)
            return
        }
        tickCount++
    }

    fun getTickCount(): Int = tickCount

    fun isAlive(): Boolean = entity.isValid && !entity.isDead
}
