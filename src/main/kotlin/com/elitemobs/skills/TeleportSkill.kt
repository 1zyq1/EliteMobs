package com.elitemobs.skills

import org.bukkit.Bukkit
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class TeleportSkill(private val plugin: com.elitemobs.EliteMobsPlugin) {

    private val activeTasks = ConcurrentHashMap<UUID, Int>()

    fun start(entityUUID: UUID, config: Map<String, Any>) {
        val interval = (config["interval-ticks"] as? Int) ?: 80
        val range = (config["range"] as? Double) ?: 15.0

        val taskId = Bukkit.getScheduler().runTaskTimer(plugin, Runnable {
            val entity = Bukkit.getEntity(entityUUID) as? LivingEntity ?: run {
                stop(entityUUID)
                return@Runnable
            }

            if (!entity.isValid || entity.isDead) {
                stop(entityUUID)
                return@Runnable
            }

            teleportToPlayer(entity, range)
        }, interval.toLong(), interval.toLong()).taskId

        activeTasks[entityUUID] = taskId
    }

    fun stop(entityUUID: UUID) {
        activeTasks.remove(entityUUID)?.let { taskId ->
            Bukkit.getScheduler().cancelTask(taskId)
        }
    }

    private fun teleportToPlayer(entity: LivingEntity, range: Double) {
        // 找到范围内最近的玩家
        val target = entity.world.players
            .filter {
                it.location.distance(entity.location) <= range &&
                it.gameMode != org.bukkit.GameMode.SPECTATOR &&
                !it.isDead
            }
            .minByOrNull { it.location.distance(entity.location) }
            ?: return

        // 传送前粒子效果
        entity.world.spawnParticle(
            Particle.PORTAL,
            entity.location,
            50,
            0.5,
            0.5,
            0.5,
            0.1
        )
        entity.world.playSound(entity.location, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f)

        // 传送到目标玩家附近（背后）
        val direction = target.location.direction.normalize().multiply(-2.0)
        val teleportLoc = target.location.add(direction)
        teleportLoc.y = target.location.y

        entity.teleport(teleportLoc)

        // 传送后粒子效果
        entity.world.spawnParticle(
            Particle.PORTAL,
            entity.location,
            50,
            0.5,
            0.5,
            0.5,
            0.1
        )
        entity.world.playSound(entity.location, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f)
    }
}
