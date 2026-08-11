package com.elitemobs.skills

import org.bukkit.Bukkit
import org.bukkit.entity.LivingEntity
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class HighJumpSkill(private val plugin: com.elitemobs.EliteMobsPlugin) {

    private val activeTasks = ConcurrentHashMap<UUID, Int>()

    fun start(entityUUID: UUID, config: Map<String, Any>) {
        val jumpMultiplier = (config["jump-multiplier"] as? Double) ?: 2.5
        val interval = (config["jump-interval-ticks"] as? Int) ?: 100

        val taskId = Bukkit.getScheduler().runTaskTimer(plugin, Runnable {
            val entity = Bukkit.getEntity(entityUUID) as? LivingEntity ?: run {
                stop(entityUUID)
                return@Runnable
            }

            if (!entity.isValid || entity.isDead) {
                stop(entityUUID)
                return@Runnable
            }

            applyHighJump(entity, jumpMultiplier)
        }, interval.toLong(), interval.toLong()).taskId

        activeTasks[entityUUID] = taskId
    }

    fun stop(entityUUID: UUID) {
        activeTasks.remove(entityUUID)?.let { taskId ->
            Bukkit.getScheduler().cancelTask(taskId)
        }
    }

    private fun applyHighJump(entity: LivingEntity, jumpMultiplier: Double) {
        // 计算跳跃提升等级
        // 每级跳跃提升提供0.5格额外高度
        val boostLevel = ((jumpMultiplier - 1.0) * 2).toInt().coerceIn(1, 10)

        // 添加跳跃提升效果
        entity.addPotionEffect(
            PotionEffect(
                PotionEffectType.JUMP_BOOST,
                40,  // 2秒持续时间
                boostLevel,
                true,
                false,
                true
            )
        )

        // 发送粒子效果表示技能激活
        entity.world.spawnParticle(
            org.bukkit.Particle.HAPPY_VILLAGER,
            entity.location.add(0.0, 1.0, 0.0),
            15,
            0.5,
            0.5,
            0.5,
            0.0
        )

        // 添加速度提升效果
        entity.addPotionEffect(
            PotionEffect(
                PotionEffectType.SPEED,
                20,  // 1秒
                1,
                true,
                false,
                true
            )
        )
    }
}
