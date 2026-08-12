package com.elitemobs.skills

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.attribute.Attribute
import org.bukkit.entity.LivingEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class SummonMinionsSkill(private val plugin: com.elitemobs.EliteMobsPlugin) : Listener {

    private val activeTasks = ConcurrentHashMap<UUID, Int>()
    private val minionCount = ConcurrentHashMap<UUID, Int>()
    private val ownerKey = NamespacedKey(plugin, "minion_owner")

    fun start(entityUUID: UUID, config: Map<String, Any>) {
        val interval = (config["interval-ticks"] as? Int) ?: 200
        val maxMinions = (config["max-minions"] as? Int) ?: 3
        val healthMultiplier = (config["minion-health-multiplier"] as? Double) ?: 1.5
        val damageMultiplier = (config["minion-damage-multiplier"] as? Double) ?: 1.2

        minionCount[entityUUID] = 0

        val taskId = Bukkit.getScheduler().runTaskTimer(plugin, Runnable {
            val entity = Bukkit.getEntity(entityUUID) as? LivingEntity ?: run {
                stop(entityUUID)
                return@Runnable
            }

            if (!entity.isValid || entity.isDead) {
                stop(entityUUID)
                return@Runnable
            }

            val currentCount = minionCount[entityUUID] ?: 0
            if (currentCount < maxMinions) {
                spawnMinion(entity, healthMultiplier, damageMultiplier)
            }
        }, interval.toLong(), interval.toLong()).taskId

        activeTasks[entityUUID] = taskId
    }

    fun stop(entityUUID: UUID) {
        activeTasks.remove(entityUUID)?.let { taskId ->
            Bukkit.getScheduler().cancelTask(taskId)
        }
        minionCount.remove(entityUUID)
    }

    @EventHandler
    fun onMinionDeath(event: EntityDeathEvent) {
        val entity = event.entity
        val ownerUUID = entity.persistentDataContainer.get(ownerKey, PersistentDataType.STRING) ?: return

        try {
            val owner = UUID.fromString(ownerUUID)
            val currentCount = minionCount[owner] ?: return
            if (currentCount > 0) {
                minionCount[owner] = currentCount - 1
            }
        } catch (_: IllegalArgumentException) { }
    }

    private fun spawnMinion(entity: LivingEntity, healthMultiplier: Double, damageMultiplier: Double) {
        val entityType = entity.type

        // 在精英怪周围随机位置寻找安全出生点
        val baseLoc = entity.location
        for (attempt in 1..5) {
            val angle = Math.random() * 2 * Math.PI
            val distance = 2.0 + Math.random() * 2.0
            val spawnLoc = Location(
                entity.world,
                baseLoc.x + Math.cos(angle) * distance,
                0.0,
                baseLoc.z + Math.sin(angle) * distance
            )

            val safeLoc = findSafeLocation(spawnLoc, 3) ?: continue

            val minion = entity.world.spawnEntity(safeLoc, entityType) as? LivingEntity ?: return

            // 标记小怪归属
            minion.persistentDataContainer.set(ownerKey, PersistentDataType.STRING, entity.uniqueId.toString())

            // 设置小怪属性（较弱）
            val baseHealth = minion.getAttribute(Attribute.MAX_HEALTH)?.value ?: 20.0
            minion.getAttribute(Attribute.MAX_HEALTH)?.baseValue = baseHealth * healthMultiplier
            minion.health = baseHealth * healthMultiplier

            minion.getAttribute(Attribute.ATTACK_DAMAGE)?.baseValue =
                (minion.getAttribute(Attribute.ATTACK_DAMAGE)?.value ?: 2.0) * damageMultiplier

            // 设置小怪名字
            minion.customName = "§c§l小型 ${entityType.name.lowercase()}"
            minion.isCustomNameVisible = true

            // 添加弱化效果
            minion.addPotionEffect(
                PotionEffect(PotionEffectType.WEAKNESS, Int.MAX_VALUE, 0, true, false, true)
            )

            // 粒子效果
            entity.world.spawnParticle(
                org.bukkit.Particle.SOUL,
                safeLoc,
                20,
                0.5,
                0.5,
                0.5,
                0.1
            )

            // 更新计数
            minionCount[entity.uniqueId] = (minionCount[entity.uniqueId] ?: 0) + 1
            return
        }
    }

    private fun findSafeLocation(loc: Location, maxSearch: Int): Location? {
        val world = loc.world ?: return null
        // 从上方往下搜索，找到安全位置（下方有实体方块，头顶有2格空气）
        for (y in loc.blockY + maxSearch downTo loc.blockY - maxSearch) {
            val checkLoc = Location(world, loc.x, y.toDouble(), loc.z)
            val block = checkLoc.block
            val above = checkLoc.clone().add(0.0, 1.0, 0.0).block
            val above2 = checkLoc.clone().add(0.0, 2.0, 0.0).block

            if (block.type.isSolid && !above.type.isSolid && !above2.type.isSolid) {
                return Location(world, loc.x, y.toDouble() + 1.0, loc.z)
            }
        }
        return null
    }
}
