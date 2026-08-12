package com.elitemobs.skills

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Creeper
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class CreeperSplitSkill(private val plugin: com.elitemobs.EliteMobsPlugin) : Listener {

    private val activeMobs = ConcurrentHashMap.newKeySet<UUID>()
    private val splitConfig = ConcurrentHashMap<UUID, Map<String, Any>>()
    private val parentKey = NamespacedKey(plugin, "split_parent")

    fun start(entityUUID: UUID, config: Map<String, Any>) {
        activeMobs.add(entityUUID)
        splitConfig[entityUUID] = config
    }

    fun stop(entityUUID: UUID) {
        activeMobs.remove(entityUUID)
        splitConfig.remove(entityUUID)
    }

    @EventHandler
    fun onEntityExplode(event: EntityExplodeEvent) {
        val entity = event.entity as? Creeper ?: return
        if (!activeMobs.contains(entity.uniqueId)) return

        val config = splitConfig[entity.uniqueId] ?: return
        val splitCount = (config["split-count"] as? Int) ?: 3
        val splitHealthMultiplier = (config["split-health-multiplier"] as? Double) ?: 0.5
        val splitDamageMultiplier = (config["split-damage-multiplier"] as? Double) ?: 0.7
        val canSplitAgain = (config["recursive"] as? Boolean) ?: false

        // 取消爆炸
        event.isCancelled = true

        // 生成分裂小苦力怕
        for (i in 0 until splitCount) {
            val angle = Math.random() * 2 * Math.PI
            val distance = 1.5 + Math.random() * 1.5
            val spawnLoc = Location(
                entity.world,
                entity.location.x + Math.cos(angle) * distance,
                0.0,
                entity.location.z + Math.sin(angle) * distance
            )

            val safeLoc = findSafeLocation(spawnLoc, 3) ?: continue

            val miniCreeper = entity.world.spawnEntity(safeLoc, org.bukkit.entity.EntityType.CREEPER) as? Creeper ?: continue

            // 标记分裂体归属
            miniCreeper.persistentDataContainer.set(parentKey, PersistentDataType.STRING, entity.uniqueId.toString())

            // 设置小苦力怕属性
            val baseHealth = miniCreeper.getAttribute(Attribute.MAX_HEALTH)?.value ?: 20.0
            miniCreeper.getAttribute(Attribute.MAX_HEALTH)?.baseValue = baseHealth * splitHealthMultiplier
            miniCreeper.health = baseHealth * splitHealthMultiplier

            miniCreeper.getAttribute(Attribute.ATTACK_DAMAGE)?.baseValue =
                (miniCreeper.getAttribute(Attribute.ATTACK_DAMAGE)?.value ?: 2.0) * splitDamageMultiplier

            // 设置名称
            miniCreeper.customName = "§c§l分裂苦力怕"
            miniCreeper.isCustomNameVisible = true

            // 添加弱化效果
            miniCreeper.addPotionEffect(
                PotionEffect(PotionEffectType.WEAKNESS, Int.MAX_VALUE, 0, true, false, true)
            )

            // 粒子效果
            entity.world.spawnParticle(
                Particle.EXPLOSION_EMITTER,
                safeLoc,
                1,
                0.0, 0.0, 0.0, 0.0
            )

            // 如果允许递归分裂，注册分裂技能
            if (canSplitAgain) {
                val miniUUID = miniCreeper.uniqueId
                activeMobs.add(miniUUID)
                splitConfig[miniUUID] = mapOf(
                    "split-count" to maxOf(1, splitCount - 1),
                    "split-health-multiplier" to (splitHealthMultiplier * 0.7),
                    "split-damage-multiplier" to (splitDamageMultiplier * 0.8),
                    "recursive" to false
                )
            }
        }

        // 粒子效果 - 分裂
        entity.world.spawnParticle(
            Particle.CAMPFIRE_COSY_SMOKE,
            entity.location,
            30,
            0.5, 0.5, 0.5, 0.1
        )
    }

    @EventHandler
    fun onSplitCreeperDeath(event: EntityDeathEvent) {
        val uuid = event.entity.uniqueId
        if (activeMobs.contains(uuid)) {
            stop(uuid)
        }
    }

    private fun findSafeLocation(loc: Location, maxSearch: Int): Location? {
        val world = loc.world ?: return null
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
