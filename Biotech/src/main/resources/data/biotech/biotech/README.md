# Biotech 数据包属性说明

`gene_registry` 与 `xene_registry` 下的每个 JSON 都使用相同的定义格式：

```json
{
  "rarity": "rare",
  "description": {"translate": "gene.example.description"},
  "attributes": [
    {
      "type": "minecraft:generic.max_health",
      "id": "your_namespace:unique_modifier_id",
      "amount": 1.0,
      "operation": "add_value",
      "slot": "any"
    }
  ]
}
```

- `type` 是 Minecraft 属性 ID，例如 `minecraft:generic.max_health`、`minecraft:generic.attack_speed`、`minecraft:generic.movement_speed` 与 `minecraft:generic.armor`。
- `id` 必须在该定义内唯一；它用于让 Minecraft 正确叠加和移除属性修饰。
- `amount` 支持负数。`add_value` 是固定数值：`1.0` 最大生命即 +1 点生命，`-1.0` 即 -1 点生命。
- `add_multiplied_total` 是最终值百分比：`0.05` 即 +5%，`-0.05` 即 -5%。攻击速度和移速的百分比增减通常使用该操作。对于默认值为 `0` 的 GeneHunter 属性，必须使用 `add_value`，否则乘算不会产生效果。
- `slot` 当前统一为 `any`，使定义的属性在对应 Gene/Xene Curios 栏位装备时生效。

GeneHunter 提供的可用战斗属性也可以直接填写在 `type` 中，例如：

- `gene_hunter:sword_weapon_damage`：剑类武器额外伤害（固定值，建议单个天赋不超过 `1.0`）。
- `gene_hunter:blade_weapon_damage`、`gene_hunter:sword_weapon_damage`、`gene_hunter:axe_weapon_damage`、`gene_hunter:hammer_weapon_damage`：对应武器形态的额外伤害（固定值，建议单个天赋不超过 `1.0`）。
- `gene_hunter:weapon_damage_conversion_rate`：未匹配武器形制伤害的转化率（`0.08` 即 8%）。
- `gene_hunter:attack_temperature_burn_damage`：高温状态下每 20 tick 的燃烧伤害（建议保持在 `0.5` 以下）。

测试定义以铁剑自带的 9 点剑类伤害为基线，将直接武器伤害控制在 1 点以内（约 11%），并为每个定义配套负面属性。

内置 Xene 定义均包含至少一项增益和一项减益，便于验证正负属性、叠加与卸下后的属性回滚。`xene_trait_base` 将它们按基础、普通和高级池投放到关卡奖励中。
