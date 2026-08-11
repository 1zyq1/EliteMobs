package com.elitemobs

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class LootManager(private val plugin: EliteMobsPlugin) {

    fun giveLoot(player: Player, mobType: String, tier: MobTier) {
        val config = plugin.config
        val tierKey = when (tier) {
            MobTier.COMMON -> "common"
            MobTier.ADVANCED -> "advanced"
            MobTier.ELITE -> "elite"
            MobTier.KING -> "king"
            MobTier.BOSS -> "boss"
        }

        // 给经验
        val exp = config.getInt("loot.$tierKey.exp", 10)
        if (exp > 0) {
            player.giveExp(exp)
        }

        // 给物品 (格式: "minecraft:iron_nugget:5:10")
        val items = config.getStringList("loot.$tierKey.items")
        for (itemStr in items) {
            val parts = itemStr.split(":")
            if (parts.size < 4) continue

            val materialName = "${parts[0]}:${parts[1]}"  // "minecraft:iron_nugget"
            val minCount = parts[2].toIntOrNull() ?: 1
            val maxCount = parts[3].toIntOrNull() ?: 1

            val material = Material.matchMaterial(materialName) ?: continue
            val count = if (minCount == maxCount) minCount
                       else minCount + (Math.random() * (maxCount - minCount + 1)).toInt()

            if (count <= 0) continue

            val item = ItemStack(material, count)
            player.world.dropItemNaturally(player.location, item)
        }

        // 发送提示
        val chineseName = getChineseMobName(mobType)
        val tierName = tier.getName()
        player.sendMessage("§6§l击杀 $tierName $chineseName §r获得奖励!")
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
}
