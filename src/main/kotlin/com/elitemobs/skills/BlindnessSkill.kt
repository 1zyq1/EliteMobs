package com.elitemobs.skills

import com.elitemobs.EliteMobsPlugin
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class BlindnessSkill(private val plugin: EliteMobsPlugin) : Listener {

    private val activeMobs = ConcurrentHashMap.newKeySet<UUID>()
    private val lastUsed = ConcurrentHashMap<UUID, Long>()
    private var durationTicks = 100
    private var cooldownTicks = 200L

    fun start(entityUUID: UUID, config: Map<String, Any>) {
        activeMobs.add(entityUUID)
        durationTicks = (config["duration-ticks"] as? Number)?.toInt() ?: 100
        cooldownTicks = (config["cooldown-ticks"] as? Number)?.toLong() ?: 200L
    }

    fun stop(entityUUID: UUID) {
        activeMobs.remove(entityUUID)
        lastUsed.remove(entityUUID)
    }

    @EventHandler
    fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
        val damaged = event.entity as? Player ?: return
        val damager = event.damager as? LivingEntity ?: return

        if (!activeMobs.contains(damager.uniqueId)) return

        val now = System.currentTimeMillis()
        val last = lastUsed[damager.uniqueId] ?: 0L
        if (now - last < cooldownTicks * 50) return
        lastUsed[damager.uniqueId] = now

        damaged.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, durationTicks, 0, true, false))
        damaged.sendMessage("§8§l你失明了！")

        damaged.world.spawnParticle(
            org.bukkit.Particle.SMOKE,
            damaged.location.add(0.0, 1.5, 0.0),
            40, 0.4, 0.4, 0.4, 0.02
        )
    }
}
