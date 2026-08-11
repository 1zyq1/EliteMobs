package com.elitemobs

import com.elitemobs.skills.*
import org.bukkit.Bukkit
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.TNTPrimed
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class SkillManager(private val plugin: EliteMobsPlugin) : Listener {

    private val activeSkills = ConcurrentHashMap<UUID, MutableMap<String, Any>>()
    private val tntSkill = TNTLaunchSkill(plugin)
    private val highJumpSkill = HighJumpSkill(plugin)
    val fireAspectSkill = FireAspectSkill(plugin)
    val teleportSkill = TeleportSkill(plugin)
    val summonMinionsSkill = SummonMinionsSkill(plugin)
    val thornsSkill = ThornsSkill(plugin)
    val creeperSplitSkill = CreeperSplitSkill(plugin)
    val instakillSkill = InstakillSkill(plugin)
    val stunSkill = StunSkill(plugin)
    val blindnessSkill = BlindnessSkill(plugin)
    val miningFatigueSkill = MiningFatigueSkill(plugin)

    private val ownerKey = org.bukkit.NamespacedKey(plugin, "tnt_owner")

    fun initialize() {
        // 注册事件监听器类技能
        Bukkit.getPluginManager().registerEvents(fireAspectSkill, plugin)
        Bukkit.getPluginManager().registerEvents(thornsSkill, plugin)
        Bukkit.getPluginManager().registerEvents(creeperSplitSkill, plugin)
        Bukkit.getPluginManager().registerEvents(instakillSkill, plugin)
        Bukkit.getPluginManager().registerEvents(stunSkill, plugin)
        Bukkit.getPluginManager().registerEvents(blindnessSkill, plugin)
        Bukkit.getPluginManager().registerEvents(miningFatigueSkill, plugin)
    }

    fun registerSkill(entityUUID: UUID, skillName: String) {
        val skills = activeSkills.getOrPut(entityUUID) { mutableMapOf() }
        val skillConfig = plugin.configManager.getSkillConfig(skillName)
        skills[skillName] = skillConfig

        when (skillName) {
            "fireball-launcher" -> tntSkill.start(entityUUID, skillConfig)
            "high-jump" -> highJumpSkill.start(entityUUID, skillConfig)
            "fire-aspect" -> fireAspectSkill.start(entityUUID, skillConfig)
            "teleport" -> teleportSkill.start(entityUUID, skillConfig)
            "summon-minions" -> summonMinionsSkill.start(entityUUID, skillConfig)
            "thorns" -> thornsSkill.start(entityUUID, skillConfig)
            "creeper-split" -> creeperSplitSkill.start(entityUUID, skillConfig)
            "stun" -> stunSkill.start(entityUUID, skillConfig)
            "blindness" -> blindnessSkill.start(entityUUID, skillConfig)
            "mining-fatigue" -> miningFatigueSkill.start(entityUUID, skillConfig)
        }
    }

    fun registerInstakill(entityUUID: UUID, chance: Double) {
        instakillSkill.start(entityUUID, chance)
    }

    fun unregisterSkill(entityUUID: UUID, skillName: String) {
        activeSkills[entityUUID]?.remove(skillName)
        when (skillName) {
            "fireball-launcher" -> tntSkill.stop(entityUUID)
            "high-jump" -> highJumpSkill.stop(entityUUID)
            "fire-aspect" -> fireAspectSkill.stop(entityUUID)
            "teleport" -> teleportSkill.stop(entityUUID)
            "summon-minions" -> summonMinionsSkill.stop(entityUUID)
            "thorns" -> thornsSkill.stop(entityUUID)
            "creeper-split" -> creeperSplitSkill.stop(entityUUID)
            "stun" -> stunSkill.stop(entityUUID)
            "blindness" -> blindnessSkill.stop(entityUUID)
            "mining-fatigue" -> miningFatigueSkill.stop(entityUUID)
        }
    }

    fun unregisterAll(entityUUID: UUID) {
        val skills = activeSkills.remove(entityUUID) ?: return
        skills.keys.forEach { skillName -> unregisterSkill(entityUUID, skillName) }
        instakillSkill.stop(entityUUID)
    }

    @EventHandler
    fun onEntityDeath(event: EntityDeathEvent) {
        val entity = event.entity
        val uuid = entity.uniqueId

        if (activeSkills.containsKey(uuid)) {
            unregisterAll(uuid)
            plugin.removeEliteMob(uuid)
        }
    }

    @EventHandler
    fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
        val damaged = event.entity as? LivingEntity ?: return
        val damager = event.damager

        // 爆炸伤害保护：TNT不能伤害发射它的怪物
        if (damager is TNTPrimed) {
            val ownerUUID = damager.persistentDataContainer.get(ownerKey, org.bukkit.persistence.PersistentDataType.STRING)
            if (ownerUUID != null) {
                try {
                    val owner = Bukkit.getEntity(UUID.fromString(ownerUUID))
                    // 如果受伤者就是TNT的主人，取消伤害
                    if (owner != null && owner.uniqueId == damaged.uniqueId) {
                        event.isCancelled = true
                        return
                    }
                } catch (_: IllegalArgumentException) { }
            }
        }
    }

    fun getActiveSkills(uuid: UUID): Map<String, Any> {
        return activeSkills[uuid] ?: emptyMap()
    }
}
