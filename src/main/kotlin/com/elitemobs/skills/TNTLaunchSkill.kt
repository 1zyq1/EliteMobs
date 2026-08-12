package com.elitemobs.skills

import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.SmallFireball
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.Vector
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class TNTLaunchSkill(private val plugin: com.elitemobs.EliteMobsPlugin) {

    private val activeTasks = ConcurrentHashMap<UUID, Int>()

    fun start(entityUUID: UUID, config: Map<String, Any>) {
        val interval = (config["interval-ticks"] as? Int) ?: 40
        val launchHeight = (config["launch-height"] as? Double) ?: 2.0
        val speed = (config["speed"] as? Double) ?: 1.5

        val taskId = Bukkit.getScheduler().runTaskTimer(plugin, Runnable {
            val entity = Bukkit.getEntity(entityUUID) as? LivingEntity ?: run {
                stop(entityUUID)
                return@Runnable
            }

            if (!entity.isValid || entity.isDead) {
                stop(entityUUID)
                return@Runnable
            }

            // 只有附近有玩家时才发射
            val nearestPlayer = entity.world.players
                .filter { it.location.distanceSquared(entity.location) < 400.0 }  // 20^2 = 400
                .minByOrNull { it.location.distanceSquared(entity.location) }

            if (nearestPlayer == null) return@Runnable

            launchFireball(entity, nearestPlayer, launchHeight, speed)
        }, interval.toLong(), interval.toLong()).taskId

        activeTasks[entityUUID] = taskId
    }

    fun stop(entityUUID: UUID) {
        activeTasks.remove(entityUUID)?.let { taskId ->
            Bukkit.getScheduler().cancelTask(taskId)
        }
    }

    private fun launchFireball(entity: LivingEntity, target: org.bukkit.entity.Player, launchHeight: Double, speed: Double) {
        val location = entity.location.add(0.0, launchHeight, 0.0)

        // 生成火焰弹
        val fireball = entity.world.spawn(location, SmallFireball::class.java) { fb ->
            fb.shooter = entity

            // 朝向目标玩家发射
            val direction = target.location.toVector().subtract(location.toVector()).normalize()
            fb.velocity = direction.multiply(speed)
        }

        // 粒子效果
        entity.world.spawnParticle(
            org.bukkit.Particle.FLAME,
            location,
            15,
            0.3, 0.3, 0.3,
            0.05
        )
    }
}
