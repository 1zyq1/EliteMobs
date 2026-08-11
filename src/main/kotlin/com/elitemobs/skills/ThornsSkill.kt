package com.elitemobs.skills

import org.bukkit.Bukkit
import org.bukkit.Particle
import org.bukkit.entity.LivingEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class ThornsSkill(private val plugin: com.elitemobs.EliteMobsPlugin) : Listener {

    private val activeMobs = ConcurrentHashMap.newKeySet<UUID>()
    private val reflectPercentage = ConcurrentHashMap<UUID, Double>()

    fun start(entityUUID: UUID, config: Map<String, Any>) {
        activeMobs.add(entityUUID)
        reflectPercentage[entityUUID] = (config["reflect-percentage"] as? Double) ?: 0.3
    }

    fun stop(entityUUID: UUID) {
        activeMobs.remove(entityUUID)
        reflectPercentage.remove(entityUUID)
    }

    @EventHandler
    fun onEntityDamage(event: EntityDamageByEntityEvent) {
        val victim = event.entity as? LivingEntity ?: return
        val damager = event.damager as? LivingEntity ?: return

        if (!activeMobs.contains(victim.uniqueId)) return

        val reflect = reflectPercentage[victim.uniqueId] ?: 0.3
        val damage = event.finalDamage
        val reflectDamage = damage * reflect

        // 反伤
        damager.damage(reflectDamage, victim)

        // 反伤粒子效果
        damager.world.spawnParticle(
            Particle.CRIT,
            damager.location.add(0.0, 1.0, 0.0),
            15,
            0.3,
            0.3,
            0.3,
            0.1
        )

        // 精英怪回复少量生命
        val healAmount = damage * 0.1
        victim.health = (victim.health + healAmount).coerceAtMost(
            victim.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH)?.value ?: 20.0
        )
    }
}
