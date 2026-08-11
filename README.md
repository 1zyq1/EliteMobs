# EliteMobs

Minecraft Paper 插件 - 概率刷新带技能的强化怪物

## 支持版本

- Minecraft 1.21+
- Paper 服务器

## 玩法介绍

原版怪物自然刷新时，有 **35%** 概率升级为精英怪，分为 5 个等级：

| 等级 | 血量倍率 | 伤害倍率 | 速度倍率 | 技能数量 | 装备 |
|------|---------|---------|---------|---------|------|
| 普通 | x1 | x1 | x1 | 0 | 无 |
| 高级 | x3 | x2.5 | x1.2 | 1 | 锁链 |
| 精英 | x6 | x5 | x1.5 | 2 | 铁 |
| 王者 | x15 | x10 | x2.0 | 3 | 钻石 |
| BOSS | x25 | x15 | x2.5 | 全部 | 下界合金 |

## 支持怪物

- 骷髅 (40% 概率拿弓 / 60% 概率拿剑)
- 僵尸
- 蜘蛛
- 苦力怕
- 猪灵
- 末影人

## 技能系统

### 远程技能
- **火焰弹发射** - 发射火焰弹攻击玩家
- **高跳** - 跳跃高度大幅提升 + 移动速度加成

### 近战技能
- **火焰附加** - 攻击时点燃玩家
- **反伤** - 被攻击时反弹 40% 伤害并回血
- **眩晕** - 使玩家失明 + 缓慢 + 跳跃抑制
- **失明** - 使玩家失明
- **挖掘疲劳** - 大幅降低玩家挖掘速度

### 特殊技能
- **传送** - 传送到最近玩家背后
- **召唤小怪** - 周期性召唤同类弱化版本
- **苦力怕分裂** - 爆炸前分裂成多个小苦力怕

### 特殊机制
- **秒杀** - 极小概率一击必杀玩家 (基础 0.5%，BOSS 最高 4%)

## 安装

1. 下载 `EliteMobs.jar`
2. 放入服务器 `plugins` 文件夹
3. 重启服务器
4. 编辑 `plugins/EliteMobs/config.yml` 自定义配置

## 命令

| 命令 | 说明 | 权限 |
|------|------|------|
| `/elitemobs reload` | 重载配置 | elitemobs.admin |
| `/elitemobs info` | 查看插件信息 | elitemobs.admin |
| `/elitemobs clear` | 清理所有精英怪 | elitemobs.admin |
| `/emreload` | 快捷重载配置 | elitemobs.admin |

## 配置说明

### 基础配置
```yaml
worlds:
  - "world"
  - "world_nether"
  - "world_the_end"

spawn-chance: 0.35          # 精英怪生成概率
max-health-cap: 150         # 血量上限
skeleton-bow-chance: 0.4    # 骷髅拿弓概率
```

### 清理配置
```yaml
spawning:
  despawn-radius: 48        # 超过此距离自动清理
  despawn-check-interval: 60 # 检查间隔(tick)
```

### 怪物配置
每种怪物可独立配置：
- 血量/伤害/速度倍率
- 技能列表
- 装备等级

### 掉落配置
击杀精英怪可获得：
- 经验值
- 物品掉落 (按等级配置)

## 依赖

- **无** - 纯 Paper API，无需额外插件

## 构建

```bash
# 需要 JDK 21
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot"
& "C:\gradle\gradle-8.14.3\bin\gradle.bat" clean shadowJar
```

输出: `build/libs/EliteMobs.jar`

## 许可证

MIT License

## 作者

1zyq1
