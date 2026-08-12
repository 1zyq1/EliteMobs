package com.elitemobs

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.plugin.java.JavaPlugin
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class EliteMobsPlugin : JavaPlugin(), Listener, CommandExecutor {

    private val managedMobs = ConcurrentHashMap<UUID, EliteMob>()
    val configManager = ConfigManager(this)
    private val skillManager = SkillManager(this)
    private lateinit var lootManager: LootManager

    override fun onEnable() {
        saveDefaultConfig()
        configManager.load()
        skillManager.initialize()
        lootManager = LootManager(this)

        Bukkit.getPluginManager().registerEvents(this, this)
        Bukkit.getPluginManager().registerEvents(skillManager, this)

        // 注册命令
        getCommand("elitemobs")?.setExecutor(this)
        getCommand("emreload")?.setExecutor(this)

        // 启动清理循环 - 玩家远离时移除怪物
        Bukkit.getScheduler().runTaskTimer(this, Runnable {
            despawnDistantMobs()
        }, configManager.getDespawnCheckInterval(), configManager.getDespawnCheckInterval())

        logger.info("EliteMobs 插件已启用!")
        logger.info("  启用怪物种类: ${configManager.getEnabledMobs().size}")
        logger.info("  启用世界: ${configManager.getEnabledWorlds().joinToString()}")
        logger.info("  刷新概率: ${configManager.getSpawnChance() * 100}%")
        logger.info("  清理半径: ${configManager.getDespawnRadius()} 格")
    }

    override fun onDisable() {
        managedMobs.clear()
        logger.info("EliteMobs 插件已禁用!")
    }

    /**
     * 原版刷怪接口：所有自然刷新的怪物显示"普通"名称
     * 其中有概率的怪物会被转换为高级/精英/王者/BOSS
     */
    @EventHandler
    fun onCreatureSpawn(event: CreatureSpawnEvent) {
        val entity = event.entity as? LivingEntity ?: return

        // 只处理自然刷怪
        if (event.spawnReason != CreatureSpawnEvent.SpawnReason.NATURAL) return

        // 检查是否为允许的怪物类型
        val mobType = getMobType(entity) ?: return
        if (!configManager.isMobEnabled(mobType)) return

        // 只在启用的世界中处理
        if (!configManager.isWorldEnabled(entity.world.name)) return

        // 所有原版怪物显示"普通"名称
        val chineseName = getChineseMobName(mobType)
        entity.customName = "§7$chineseName"
        entity.isCustomNameVisible = true

        // 概率决定是否升级为高级/精英/王者/BOSS
        if (Math.random() > configManager.getSpawnChance()) return

        // 决定等级
        val tier = MobTier.rollTier()

        // 在原位将怪物转换
        spawnManagedMob(entity, mobType, tier)
    }

    /**
     * 清理远离玩家的怪物
     */
    private fun despawnDistantMobs() {
        val despawnRadius = configManager.getDespawnRadius()

        val iterator = managedMobs.entries.iterator()
        while (iterator.hasNext()) {
            val (uuid, mob) = iterator.next()
            val entity = mob.entity

            if (!entity.isValid || entity.isDead) {
                iterator.remove()
                continue
            }

            // 检查是否在任何在线玩家的清理范围内
            val nearbyPlayer = Bukkit.getOnlinePlayers().any { player ->
                player.world == entity.world && player.location.distance(entity.location) < despawnRadius
            }

            if (!nearbyPlayer) {
                // 没有玩家在附近，移除怪物
                skillManager.unregisterAll(uuid)
                entity.remove()
                iterator.remove()
            }
        }
    }

    /**
     * 玩家击杀怪物时，给予对应奖励
     */
    @EventHandler
    fun onEntityDeath(event: EntityDeathEvent) {
        val entity = event.entity as? LivingEntity ?: return
        val uuid = entity.uniqueId

        val eliteMob = managedMobs[uuid] ?: return
        val killer = entity.killer ?: return

        // 给予击杀奖励
        lootManager.giveLoot(killer, eliteMob.mobType, eliteMob.tier)

        // 清理技能和数据
        skillManager.unregisterAll(uuid)
        managedMobs.remove(uuid)
    }

    private fun getMobType(entity: Entity): String? {
        return when (entity) {
            is Skeleton -> "skeleton"
            is Zombie -> "zombie"
            is Spider -> "spider"
            is Creeper -> "creeper"
            is Piglin -> "piglin"
            is Enderman -> "enderman"
            else -> null
        }
    }

    private fun spawnManagedMob(entity: LivingEntity, mobType: String, tier: MobTier) {
        val mob = EliteMob(this, entity, mobType, tier)
        managedMobs[entity.uniqueId] = mob

        // 应用属性
        applyAttributes(entity, mobType, tier)

        // 穿戴装备
        EquipmentManager.equipMob(entity, mobType, tier, configManager.getSkeletonBowChance())

        // 禁止怪物捡起物品
        entity.setCanPickupItems(false)

        // 构建显示名称
        val chineseName = getChineseMobName(mobType)
        val tierName = tier.getName()
        val skills = getSkillsForTier(mobType, tier)
        val skillIcons = getSkillIcons(skills)

        entity.customName = "${tier.prefix}$tierName $chineseName$skillIcons${tier.suffix}"
        entity.isCustomNameVisible = true

        // 确保名称持久显示
        entity.persistentDataContainer.set(
            org.bukkit.NamespacedKey(this, "managed_mob"),
            org.bukkit.persistence.PersistentDataType.STRING,
            "$mobType:${tier.name}"
        )

        // 注册技能（普通怪1个，精英2个，王者全部）
        if (skills.isNotEmpty()) {
            skills.forEach { skillName ->
                skillManager.registerSkill(entity.uniqueId, skillName)
            }
        }

        // 注册秒杀技能（高级和以上有概率秒杀）
        if (configManager.isInstakillEnabled() && tier != MobTier.COMMON) {
            val instakillBaseChance = configManager.getInstakillChance()
            val instakillChance = when (tier) {
                MobTier.ADVANCED -> instakillBaseChance * 1.5
                MobTier.ELITE -> instakillBaseChance * 2
                MobTier.KING -> instakillBaseChance * 4
                MobTier.BOSS -> instakillBaseChance * 8
                else -> 0.0
            }
            if (instakillChance > 0) {
                skillManager.registerInstakill(entity.uniqueId, instakillChance)
            }
        }
    }

    private fun getSkillsForTier(mobType: String, tier: MobTier): List<String> {
        val allSkills = configManager.getMobSkills(mobType)
        return when (tier) {
            MobTier.COMMON -> emptyList()
            MobTier.ADVANCED -> allSkills.take(1)
            MobTier.ELITE -> allSkills.take(2)
            MobTier.KING -> allSkills.take(3)
            MobTier.BOSS -> allSkills  // BOSS拥有全部技能
        }
    }

    private fun getChineseMobName(mobType: String): String {
        return when (mobType) {
            "skeleton" -> "骷髅"
            "zombie" -> "僵尸"
            "spider" -> "蜘蛛"
            "creeper" -> "苦力怕"
            "piglin" -> "猪灵"
            "enderman" -> "末影人"
            else -> mobType
        }
    }

    private fun getSkillIcons(skills: List<String>): String {
        if (skills.isEmpty()) return ""

        val icons = skills.map { skillName ->
            when (skillName) {
                "fireball-launcher" -> "火球"
                "high-jump" -> "跳"
                "fire-aspect" -> "火"
                "teleport" -> "传"
                "summon-minions" -> "召"
                "thorns" -> "甲"
                "creeper-split" -> "裂"
                "stun" -> "晕"
                "blindness" -> "盲"
                "mining-fatigue" -> "挖"
                else -> ""
            }
        }.filter { it.isNotEmpty() }

        return if (icons.isNotEmpty()) " §7[${icons.joinToString(" ")}§7]" else ""
    }

    private fun applyAttributes(entity: LivingEntity, mobType: String, tier: MobTier) {
        val configHealth = configManager.getMobHealthMultiplier(mobType)
        val configDamage = configManager.getMobDamageMultiplier(mobType)
        val configSpeed = configManager.getMobSpeedMultiplier(mobType)

        val healthMultiplier = configHealth * tier.healthMultiplier
        val damageMultiplier = configDamage * tier.damageMultiplier
        val speedMultiplier = configSpeed * tier.speedMultiplier

        // 智能缩放：如果计算出的血量超过上限，按比例缩放倍率
        val baseHealth = entity.getAttribute(Attribute.MAX_HEALTH)?.value ?: 20.0
        val maxHealthCap = configManager.getMaxHealthCap()
        val calculatedHealth = baseHealth * healthMultiplier
        val effectiveHealth = if (calculatedHealth > maxHealthCap) {
            maxHealthCap
        } else {
            calculatedHealth
        }
        entity.getAttribute(Attribute.MAX_HEALTH)?.baseValue = effectiveHealth
        entity.health = effectiveHealth

        entity.getAttribute(Attribute.ATTACK_DAMAGE)?.baseValue =
            (entity.getAttribute(Attribute.ATTACK_DAMAGE)?.value ?: 2.0) * damageMultiplier

        entity.getAttribute(Attribute.MOVEMENT_SPEED)?.baseValue =
            (entity.getAttribute(Attribute.MOVEMENT_SPEED)?.value ?: 0.2) * speedMultiplier
    }

    fun getEliteMob(uuid: UUID): EliteMob? = managedMobs[uuid]
    fun removeEliteMob(uuid: UUID) {
        managedMobs.remove(uuid)
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("elitemobs.admin")) {
            sender.sendMessage("${ChatColor.RED}你没有权限执行此命令!")
            return true
        }

        when (command.name.lowercase()) {
            "elitemobs" -> {
                if (args.isEmpty()) {
                    sendHelp(sender)
                    return true
                }
                when (args[0].lowercase()) {
                    "reload" -> reloadConfig(sender)
                    "info" -> sendInfo(sender)
                    "clear" -> clearAllMobs(sender)
                    else -> sendHelp(sender)
                }
            }
            "emreload" -> reloadConfig(sender)
        }
        return true
    }

    private fun reloadConfig(sender: CommandSender) {
        configManager.load()
        skillManager.initialize()
        sender.sendMessage("${ChatColor.GREEN}[EliteMobs] 配置已重载!")
        logger.info("配置已被 ${sender.name} 重载")
    }

    private fun sendHelp(sender: CommandSender) {
        sender.sendMessage("${ChatColor.GOLD}===== EliteMobs 命令帮助 =====")
        sender.sendMessage("${ChatColor.YELLOW}/elitemobs reload ${ChatColor.WHITE}- 重载插件配置")
        sender.sendMessage("${ChatColor.YELLOW}/elitemobs info ${ChatColor.WHITE}- 查看插件信息")
        sender.sendMessage("${ChatColor.YELLOW}/elitemobs clear ${ChatColor.WHITE}- 清理所有精英怪")
        sender.sendMessage("${ChatColor.YELLOW}/emreload ${ChatColor.WHITE}- 快捷重载配置")
    }

    private fun sendInfo(sender: CommandSender) {
        sender.sendMessage("${ChatColor.GOLD}===== EliteMobs 插件信息 =====")
        sender.sendMessage("${ChatColor.YELLOW}版本: ${description.version}")
        sender.sendMessage("${ChatColor.YELLOW}管理怪物数: ${managedMobs.size}")
        sender.sendMessage("${ChatColor.YELLOW}启用怪物种类: ${configManager.getEnabledMobs().size}")
        sender.sendMessage("${ChatColor.YELLOW}启用世界: ${configManager.getEnabledWorlds().joinToString()}")
        sender.sendMessage("${ChatColor.YELLOW}刷新概率: ${configManager.getSpawnChance() * 100}%")
        sender.sendMessage("${ChatColor.YELLOW}血量上限: ${configManager.getMaxHealthCap()}")
        sender.sendMessage("${ChatColor.YELLOW}清理半径: ${configManager.getDespawnRadius()} 格")
    }

    private fun clearAllMobs(sender: CommandSender) {
        val count = managedMobs.size
        if (count == 0) {
            sender.sendMessage("${ChatColor.YELLOW}[EliteMobs] 没有精英怪需要清理")
            return
        }

        val toRemove = ArrayList(managedMobs.keys)
        for (uuid in toRemove) {
            val entity = server.getEntity(uuid)
            if (entity != null) {
                skillManager.unregisterAll(uuid)
                entity.remove()
            }
        }
        managedMobs.clear()
        sender.sendMessage("${ChatColor.GREEN}[EliteMobs] 已清理 ${ChatColor.WHITE}$count${ChatColor.GREEN} 只精英怪")
    }
}
