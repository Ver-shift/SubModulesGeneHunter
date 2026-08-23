# Better Combat 动作序列整理

本文档用于记录 GeneHunter 当前采用的 Better Combat 武器动作序列模型，方便后续设计武器、基因词条和动作变体。

## 动作符号

| 符号 | 动作 | Better Combat 判定 / 形态 | 设计用途 |
| --- | --- | --- | --- |
| A | 横扫 | `HORIZONTAL_PLANE`，普通角度 | 范围命中、叠层、流血扩散、清理小怪 |
| B | 前刺 | `FORWARD_BOX` | 单体、穿透、攻击距离、弱点、直线效果 |
| C | 竖劈 / 下砸 | `VERTICAL_PLANE` 或 slam 动画 | 重击、破甲、处决、震地、眩晕 |
| S | 旋转横扫 | 360 度 `HORIZONTAL_PLANE` | 大回环清场、应对包围、大量触发命中效果 |
| T | 投掷 / 特殊动作 | 特殊武器行为 | 环刃或其他非标准近战循环 |

从代码层面看，Better Combat 主要有 `HORIZONTAL_PLANE`、`FORWARD_BOX`、`VERTICAL_PLANE` 三类核心 hitbox。
`S` 是策划层面的拆分：它本质上仍然是横扫，但 360 度角度会让它在战斗中表现得明显不同。

## 当前预设动作序列

以下整理基于 Better Combat 的 weapon attribute presets，也就是 Simply Swords 这类武器通常使用的动作配置。

| 武器预设 | 动作序列 | 说明 |
| --- | --- | --- |
| `sword` | `AAB` | 两段横扫，然后一段前刺。适合前两击叠层，第三击结算。 |
| `cutlass` | `AA` | 更短的剑类双横扫。 |
| `rapier` | `AB` | 小角度横扫，然后前刺。适合弱点和穿透逻辑。 |
| `katana` | `ACCA` | 横扫、竖劈、竖劈、横扫。适合持续流血或多次重击窗口。 |
| `claymore` | `ABC` | 横扫、前刺、下砸。适合剑类和重击混合构筑。 |
| `twin_blade` | `AA` | 两段大角度横扫。适合持续范围触发。 |
| `spear` | `B` | 单段长距离前刺。适合距离、穿透和直线伤害。 |
| `scythe` | `AA` | 两段长柄横扫。适合收割和流血扩散。 |
| `halberd` | `CB` | 竖劈，然后前刺。适合长柄破甲后接距离结算。 |
| `glaive` | `CCA` | 两段竖劈，然后一段高倍率大横扫。适合终段范围爆发。 |
| `double_axe` | `AS` | 大横扫，然后 360 度旋转。适合群体破甲或被包围时清场。 |
| `hammer` | `C` | 单段下砸式重击。适合震地、眩晕和处决测试。 |
| `mace` | `AAC` | 两段横扫，然后一段下砸。适合铺垫后终结。 |
| `axe` | `AA` | 两段窄角度横扫。适合短循环破甲。 |
| `dagger` | `AA`，双持时可追加 `B` | 短距离横扫，双持条件下可触发刺击。 |

## 当前测试覆盖

GeneHunter 当前覆盖了 Better Combat 的 `sword` 预设，文件位置：

`src/main/resources/data/bettercombat/weapon_attributes/sword.json`

同时为了直接测试原版铁剑，当前还覆盖了铁剑自己的 Better Combat 条目：

`src/main/resources/data/minecraft/weapon_attributes/iron_sword.json`

测试序列如下：

| 武器预设 | 原始序列 | 测试覆盖 | 目的 |
| --- | --- | --- | --- |
| `sword` | `AAB` | `SSS` | 将剑的每一段攻击都改成 360 度旋转横扫，用来测试大范围命中、清包围能力和高频范围触发效果。 |
| `minecraft:iron_sword` | 继承 `bettercombat:sword` | `SSS` | 直接覆盖铁剑，验证原版铁剑是否读取物品自己的 `weapon_attributes`。 |

这个覆盖仍然保留 `category: "sword"`，所以分类检查上它仍然是剑。
本次只改变攻击序列、攻击角度和动画表现。

## 设计含义

- `AAB` 适合叠层系统：前两击施加标记或流血，第三击消耗或转化层数。
- `AAC` 适合铺垫后终结：前两击准备敌人，最后一击破甲、眩晕或处决。
- `CB` 适合长柄破甲后穿刺：先制造破绽，再用距离优势结算。
- `CCA` 适合终段范围爆发：慢速铺垫后，用大横扫收割。
- `SSS` 是故意做得很极端的测试序列。它适合观察旋转型武器是否会产生过多命中事件、过高安全性，或者和命中触发类基因产生过强联动。
