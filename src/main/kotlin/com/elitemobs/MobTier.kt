package com.elitemobs

import org.bukkit.ChatColor

enum class MobTier(
    val displayName: String,
    val color: ChatColor,
    val healthMultiplier: Double,
    val damageMultiplier: Double,
    val speedMultiplier: Double,
    val spawnChance: Double,
    val prefix: String,
    val suffix: String
) {
    COMMON(
        displayName = "普通",
        color = ChatColor.WHITE,
        healthMultiplier = 1.0,
        damageMultiplier = 1.0,
        speedMultiplier = 1.0,
        spawnChance = 0.0,  // 原版怪物不转换为普通，普通只是显示名称
        prefix = "§7",
        suffix = "§7"
    ),
    ADVANCED(
        displayName = "高级",
        color = ChatColor.AQUA,
        healthMultiplier = 3.0,
        damageMultiplier = 2.5,
        speedMultiplier = 1.2,
        spawnChance = 0.50,  // 转换时50%概率为高级
        prefix = "§b§l◆ ",
        suffix = " §b§l◆"
    ),
    ELITE(
        displayName = "精英",
        color = ChatColor.GOLD,
        healthMultiplier = 6.0,
        damageMultiplier = 5.0,
        speedMultiplier = 1.5,
        spawnChance = 0.30,  // 转换时30%概率为精英
        prefix = "§6§l★ ",
        suffix = " §6§l★"
    ),
    KING(
        displayName = "王者",
        color = ChatColor.RED,
        healthMultiplier = 15.0,
        damageMultiplier = 10.0,
        speedMultiplier = 2.0,
        spawnChance = 0.15,  // 转换时15%概率为王者
        prefix = "§c§l👑 ",
        suffix = " §c§l👑"
    ),
    BOSS(
        displayName = "BOSS",
        color = ChatColor.LIGHT_PURPLE,
        healthMultiplier = 25.0,
        damageMultiplier = 15.0,
        speedMultiplier = 2.5,
        spawnChance = 0.05,  // 转换时5%概率为BOSS
        prefix = "§d§l⚔ ",
        suffix = " §d§l⚔"
    );

    fun getName(): String {
        return "${color}$displayName"
    }

    companion object {
        /**
         * 随机决定怪物等级 (只在 ADVANCED/ELITE/KING/BOSS 中选择)
         */
        fun rollTier(): MobTier {
            val roll = Math.random()
            var cumulative = 0.0

            // 只在可转换的等级中选择 (跳过COMMON)
            val convertibleTiers = listOf(ADVANCED, ELITE, KING, BOSS)
            for (tier in convertibleTiers) {
                cumulative += tier.spawnChance
                if (roll <= cumulative) {
                    return tier
                }
            }
            return ADVANCED
        }
    }
}
