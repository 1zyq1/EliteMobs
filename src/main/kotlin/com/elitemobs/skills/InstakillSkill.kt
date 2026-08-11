package com.elitemobs.skills

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class InstakillSkill(private val plugin: com.elitemobs.EliteMobsPlugin) : Listener {

    private val activeMobs = ConcurrentHashMap.newKeySet<UUID>()
    private val instakillChance = ConcurrentHashMap<UUID, Double>()

    fun start(entityUUID: UUID, chance: Double) {
        activeMobs.add(entityUUID)
        instakillChance[entityUUID] = chance
    }

    fun stop(entityUUID: UUID) {
        activeMobs.remove(entityUUID)
        instakillChance.remove(entityUUID)
    }

    @EventHandler
    fun onEntityDamage(event: EntityDamageByEntityEvent) {
        val damager = event.damager as? LivingEntity ?: return
        val victim = event.entity as? Player ?: return

        if (!activeMobs.contains(damager.uniqueId)) return
        val chance = instakillChance[damager.uniqueId] ?: return

        // 掷骰判定秒杀
        if (Math.random() >= chance) return

        // 取消原始伤害，直接击杀
        event.isCancelled = true

        // 获取怪物显示名称
        val mobName = damager.customName ?: damager.name ?: "精英怪"
        val playerName = victim.name

        // 秒杀效果 - 粒子爆炸
        victim.world.spawnParticle(
            Particle.EXPLOSION_EMITTER,
            victim.location,
            2,
            0.5, 0.5, 0.5, 0.0
        )
        victim.world.spawnParticle(
            Particle.LARGE_SMOKE,
            victim.location,
            50,
            0.5, 1.0, 0.5, 0.15
        )

        // 秒杀音效
        victim.world.playSound(victim.location, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.5f, 0.8f)
        victim.world.playSound(victim.location, Sound.ENTITY_WITHER_DEATH, 1.0f, 1.2f)

        // 对玩家造成致命伤害
        val maxHealth = victim.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH)?.value ?: 20.0
        victim.damage(maxHealth * 10)

        // 全服广播秒杀消息
        val message = "${ChatColor.DARK_RED}${ChatColor.BOLD}[精英怪] ${ChatColor.RED}$playerName ${ChatColor.GRAY}被 ${ChatColor.GOLD}$mobName ${ChatColor.DARK_RED}${ChatColor.BOLD}秒杀了!"
        Bukkit.broadcastMessage(message)

        plugin.logger.info("$playerName 被 $mobName 秒杀!")
    }
}
