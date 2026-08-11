package com.elitemobs

import org.bukkit.configuration.file.FileConfiguration

class ConfigManager(private val plugin: EliteMobsPlugin) {

    private var config: FileConfiguration = plugin.config

    fun load() {
        plugin.reloadConfig()
        config = plugin.config
    }

    fun isWorldEnabled(worldName: String): Boolean {
        return config.getStringList("worlds").contains(worldName)
    }

    fun getEnabledWorlds(): List<String> {
        return config.getStringList("worlds")
    }

    fun getSpawnChance(): Double {
        return config.getDouble("spawn-chance", 0.1)
    }

    fun isMobEnabled(mobType: String): Boolean {
        return config.getBoolean("mobs.$mobType.enabled", false)
    }

    fun getEnabledMobs(): List<String> {
        val mobsSection = config.getConfigurationSection("mobs") ?: return emptyList()
        return mobsSection.getKeys(false).filter { config.getBoolean("mobs.$it.enabled", false) }
    }

    fun getMobHealthMultiplier(mobType: String): Double {
        return config.getDouble("mobs.$mobType.health-multiplier", 3.0)
    }

    fun getMobDamageMultiplier(mobType: String): Double {
        return config.getDouble("mobs.$mobType.damage-multiplier", 2.5)
    }

    fun getMobSpeedMultiplier(mobType: String): Double {
        return config.getDouble("mobs.$mobType.speed-multiplier", 1.2)
    }

    fun getMobSkills(mobType: String): List<String> {
        return config.getStringList("mobs.$mobType.skills")
    }

    fun isSkillEnabled(skillName: String): Boolean {
        return config.getBoolean("skills.$skillName.enabled", false)
    }

    fun getSkillConfig(skillName: String): Map<String, Any> {
        val section = config.getConfigurationSection("skills.$skillName") ?: return emptyMap()
        val result = mutableMapOf<String, Any>()
        section.getKeys(false).forEach { key ->
            section.get(key)?.let { result[key] = it }
        }
        return result
    }

    fun isInstakillEnabled(): Boolean {
        return config.getBoolean("instakill.enabled", true)
    }

    fun getInstakillChance(): Double {
        return config.getDouble("instakill.chance", 0.005)
    }

    // ===== 清理配置 =====

    fun getDespawnRadius(): Double {
        return config.getDouble("spawning.despawn-radius", 48.0)
    }

    fun getDespawnCheckInterval(): Long {
        return config.getLong("spawning.despawn-check-interval", 60L)
    }

    // ===== 骷髅弓箭概率 =====

    fun getSkeletonBowChance(): Double {
        return config.getDouble("skeleton-bow-chance", 0.4)
    }

    // ===== 血量上限 =====

    fun getMaxHealthCap(): Double {
        return config.getDouble("max-health-cap", 150.0)
    }
}
