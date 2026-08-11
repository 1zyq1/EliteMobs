package com.elitemobs

import org.bukkit.Material
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Zombie
import org.bukkit.entity.Skeleton
import org.bukkit.entity.Piglin
import org.bukkit.inventory.ItemStack

object EquipmentManager {

    fun equipMob(entity: LivingEntity, mobType: String, tier: MobTier, bowChance: Double = 0.0) {
        when (tier) {
            MobTier.COMMON -> equipCommon(entity, mobType)
            MobTier.ADVANCED -> equipAdvanced(entity, mobType, bowChance)
            MobTier.ELITE -> equipElite(entity, mobType, bowChance)
            MobTier.KING -> equipKing(entity, mobType, bowChance)
            MobTier.BOSS -> equipBoss(entity, mobType, bowChance)
        }
    }

    private fun equipCommon(entity: LivingEntity, mobType: String) {
        when (mobType) {
            "zombie" -> {
                entity.equipment?.helmet = ItemStack(Material.LEATHER_HELMET)
                entity.equipment?.chestplate = ItemStack(Material.LEATHER_CHESTPLATE)
                entity.equipment?.leggings = ItemStack(Material.LEATHER_LEGGINGS)
                entity.equipment?.boots = ItemStack(Material.LEATHER_BOOTS)
                entity.equipment?.setItemInMainHand(ItemStack(Material.STONE_SWORD))
            }
            "skeleton" -> {
                entity.equipment?.setItemInMainHand(ItemStack(Material.STONE_SWORD))
                entity.equipment?.helmet = ItemStack(Material.LEATHER_HELMET)
            }
            "piglin" -> {
                entity.equipment?.setItemInMainHand(ItemStack(Material.GOLDEN_SWORD))
                entity.equipment?.helmet = ItemStack(Material.GOLDEN_HELMET)
                entity.equipment?.chestplate = ItemStack(Material.GOLDEN_CHESTPLATE)
            }
            else -> {}
        }
    }

    private fun equipAdvanced(entity: LivingEntity, mobType: String, bowChance: Double = 0.0) {
        when (mobType) {
            "zombie" -> {
                entity.equipment?.helmet = ItemStack(Material.CHAINMAIL_HELMET)
                entity.equipment?.chestplate = ItemStack(Material.CHAINMAIL_CHESTPLATE)
                entity.equipment?.leggings = ItemStack(Material.CHAINMAIL_LEGGINGS)
                entity.equipment?.boots = ItemStack(Material.CHAINMAIL_BOOTS)
                entity.equipment?.setItemInMainHand(ItemStack(Material.STONE_SWORD))
            }
            "skeleton" -> {
                entity.equipment?.helmet = ItemStack(Material.CHAINMAIL_HELMET)
                if (Math.random() < bowChance) {
                    entity.equipment?.setItemInMainHand(ItemStack(Material.BOW))
                } else {
                    entity.equipment?.setItemInMainHand(ItemStack(Material.STONE_SWORD))
                }
            }
            "piglin" -> {
                entity.equipment?.setItemInMainHand(ItemStack(Material.GOLDEN_SWORD))
                entity.equipment?.helmet = ItemStack(Material.CHAINMAIL_HELMET)
                entity.equipment?.chestplate = ItemStack(Material.CHAINMAIL_CHESTPLATE)
            }
            else -> {}
        }
    }

    private fun equipElite(entity: LivingEntity, mobType: String, bowChance: Double = 0.0) {
        when (mobType) {
            "zombie" -> {
                entity.equipment?.helmet = ItemStack(Material.CHAINMAIL_HELMET)
                entity.equipment?.chestplate = ItemStack(Material.IRON_CHESTPLATE)
                entity.equipment?.leggings = ItemStack(Material.IRON_LEGGINGS)
                entity.equipment?.boots = ItemStack(Material.IRON_BOOTS)
                entity.equipment?.setItemInMainHand(ItemStack(Material.IRON_SWORD))
            }
            "skeleton" -> {
                entity.equipment?.helmet = ItemStack(Material.IRON_HELMET)
                entity.equipment?.chestplate = ItemStack(Material.IRON_CHESTPLATE)
                entity.equipment?.leggings = ItemStack(Material.IRON_LEGGINGS)
                entity.equipment?.boots = ItemStack(Material.IRON_BOOTS)
                if (Math.random() < bowChance) {
                    entity.equipment?.setItemInMainHand(ItemStack(Material.BOW))
                } else {
                    entity.equipment?.setItemInMainHand(ItemStack(Material.IRON_SWORD))
                }
            }
            "spider" -> {
                entity.equipment?.setItemInMainHand(ItemStack(Material.IRON_SWORD))
            }
            "piglin" -> {
                entity.equipment?.setItemInMainHand(ItemStack(Material.GOLDEN_SWORD))
                entity.equipment?.helmet = ItemStack(Material.CHAINMAIL_HELMET)
                entity.equipment?.chestplate = ItemStack(Material.CHAINMAIL_CHESTPLATE)
                entity.equipment?.leggings = ItemStack(Material.CHAINMAIL_LEGGINGS)
                entity.equipment?.boots = ItemStack(Material.CHAINMAIL_BOOTS)
            }
            "creeper" -> {
                // 苦力怕没有装备
            }
            "enderman" -> {
                // 末影人没有装备
            }
        }
    }

    private fun equipKing(entity: LivingEntity, mobType: String, bowChance: Double = 0.0) {
        when (mobType) {
            "zombie" -> {
                entity.equipment?.helmet = ItemStack(Material.DIAMOND_HELMET)
                entity.equipment?.chestplate = ItemStack(Material.DIAMOND_CHESTPLATE)
                entity.equipment?.leggings = ItemStack(Material.DIAMOND_LEGGINGS)
                entity.equipment?.boots = ItemStack(Material.DIAMOND_BOOTS)
                entity.equipment?.setItemInMainHand(ItemStack(Material.DIAMOND_SWORD))
            }
            "skeleton" -> {
                entity.equipment?.helmet = ItemStack(Material.DIAMOND_HELMET)
                entity.equipment?.chestplate = ItemStack(Material.DIAMOND_CHESTPLATE)
                entity.equipment?.leggings = ItemStack(Material.DIAMOND_LEGGINGS)
                entity.equipment?.boots = ItemStack(Material.DIAMOND_BOOTS)
                if (Math.random() < bowChance) {
                    entity.equipment?.setItemInMainHand(ItemStack(Material.BOW))
                } else {
                    entity.equipment?.setItemInMainHand(ItemStack(Material.DIAMOND_SWORD))
                }
            }
            "spider" -> {
                entity.equipment?.setItemInMainHand(ItemStack(Material.DIAMOND_SWORD))
                entity.equipment?.helmet = ItemStack(Material.DIAMOND_HELMET)
            }
            "piglin" -> {
                entity.equipment?.setItemInMainHand(ItemStack(Material.GOLDEN_SWORD))
                entity.equipment?.helmet = ItemStack(Material.NETHERITE_HELMET)
                entity.equipment?.chestplate = ItemStack(Material.NETHERITE_CHESTPLATE)
                entity.equipment?.leggings = ItemStack(Material.NETHERITE_LEGGINGS)
                entity.equipment?.boots = ItemStack(Material.NETHERITE_BOOTS)
            }
            "creeper" -> {
                // 苦力怕没有装备，但可以给它一个爆炸增强的视觉效果
            }
            "enderman" -> {
                // 末影人没有装备
            }
        }
    }

    private fun equipBoss(entity: LivingEntity, mobType: String, bowChance: Double = 0.0) {
        when (mobType) {
            "zombie" -> {
                entity.equipment?.helmet = ItemStack(Material.NETHERITE_HELMET)
                entity.equipment?.chestplate = ItemStack(Material.NETHERITE_CHESTPLATE)
                entity.equipment?.leggings = ItemStack(Material.NETHERITE_LEGGINGS)
                entity.equipment?.boots = ItemStack(Material.NETHERITE_BOOTS)
                entity.equipment?.setItemInMainHand(ItemStack(Material.NETHERITE_SWORD))
            }
            "skeleton" -> {
                entity.equipment?.helmet = ItemStack(Material.NETHERITE_HELMET)
                entity.equipment?.chestplate = ItemStack(Material.NETHERITE_CHESTPLATE)
                entity.equipment?.leggings = ItemStack(Material.NETHERITE_LEGGINGS)
                entity.equipment?.boots = ItemStack(Material.NETHERITE_BOOTS)
                if (Math.random() < bowChance) {
                    entity.equipment?.setItemInMainHand(ItemStack(Material.BOW))
                } else {
                    entity.equipment?.setItemInMainHand(ItemStack(Material.NETHERITE_SWORD))
                }
            }
            "spider" -> {
                entity.equipment?.setItemInMainHand(ItemStack(Material.NETHERITE_SWORD))
                entity.equipment?.helmet = ItemStack(Material.NETHERITE_HELMET)
            }
            "piglin" -> {
                entity.equipment?.setItemInMainHand(ItemStack(Material.NETHERITE_SWORD))
                entity.equipment?.helmet = ItemStack(Material.NETHERITE_HELMET)
                entity.equipment?.chestplate = ItemStack(Material.NETHERITE_CHESTPLATE)
                entity.equipment?.leggings = ItemStack(Material.NETHERITE_LEGGINGS)
                entity.equipment?.boots = ItemStack(Material.NETHERITE_BOOTS)
            }
            "creeper" -> {
                // 苦力怕没有装备
            }
            "enderman" -> {
                // 末影人没有装备
            }
        }
    }
}
