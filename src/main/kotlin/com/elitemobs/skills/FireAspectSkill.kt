package com.elitemobs.skills

import org.bukkit.Bukkit
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class FireAspectSkill(private val plugin: com.elitemobs.EliteMobsPlugin) : Listener {

    private val activeMobs = ConcurrentHashMap.newKeySet<UUID>()
    private val fireDuration = ConcurrentHashMap<UUID, Int>()

    fun start(entityUUID: UUID, config: Map<String, Any>) {
        activeMobs.add(entityUUID)
        fireDuration[entityUUID] = (config["fire-duration-ticks"] as? Int) ?: 60
    }

    fun stop(entityUUID: UUID) {
        activeMobs.remove(entityUUID)
        fireDuration.remove(entityUUID)
    }

    @EventHandler
    fun onEntityDamage(event: EntityDamageByEntityEvent) {
        val damager = event.damager as? LivingEntity ?: return
        val victim = event.entity as? Player ?: return

        if (!activeMobs.contains(damager.uniqueId)) return

        val duration = fireDuration[damager.uniqueId] ?: 60
        victim.fireTicks = duration

        // 火焰粒子效果
        victim.world.spawnParticle(
            org.bukkit.Particle.FLAME,
            victim.location.add(0.0, 1.0, 0.0),
            20,
            0.3,
            0.3,
            0.3,
            0.05
        )
    }
}
