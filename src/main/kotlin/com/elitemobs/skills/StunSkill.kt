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

class StunSkill(private val plugin: EliteMobsPlugin) : Listener {

    private val activeMobs = ConcurrentHashMap.newKeySet<UUID>()
    private val lastUsed = ConcurrentHashMap<UUID, Long>()
    private val durationMap = ConcurrentHashMap<UUID, Int>()
    private val cooldownMap = ConcurrentHashMap<UUID, Long>()

    fun start(entityUUID: UUID, config: Map<String, Any>) {
        activeMobs.add(entityUUID)
        durationMap[entityUUID] = (config["duration-ticks"] as? Number)?.toInt() ?: 80
        cooldownMap[entityUUID] = (config["cooldown-ticks"] as? Number)?.toLong() ?: 160L
    }

    fun stop(entityUUID: UUID) {
        activeMobs.remove(entityUUID)
        lastUsed.remove(entityUUID)
        durationMap.remove(entityUUID)
        cooldownMap.remove(entityUUID)
    }

    @EventHandler
    fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
        val damaged = event.entity as? Player ?: return
        val damager = event.damager as? LivingEntity ?: return

        if (!activeMobs.contains(damager.uniqueId)) return

        val duration = durationMap[damager.uniqueId] ?: 80
        val cooldown = cooldownMap[damager.uniqueId] ?: 160L

        val now = System.currentTimeMillis()
        val last = lastUsed[damager.uniqueId] ?: 0L
        if (now - last < cooldown * 50) return
        lastUsed[damager.uniqueId] = now

        // 眩晕 = 失明 + 缓慢 + 跳跃抑制
        damaged.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, duration, 0, true, false))
        damaged.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS, duration, 2, true, false))
        damaged.addPotionEffect(PotionEffect(PotionEffectType.JUMP_BOOST, duration, 128, true, false))

        damaged.sendMessage("§c§l被眩晕了！")

        damaged.world.spawnParticle(
            org.bukkit.Particle.DUST,
            damaged.location.add(0.0, 1.0, 0.0),
            30, 0.3, 0.5, 0.3,
            org.bukkit.Particle.DustOptions(org.bukkit.Color.RED, 1.0f)
        )
    }
}
