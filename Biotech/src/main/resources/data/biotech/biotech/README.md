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
- `add_multiplied_total` 是最终值百分比：`0.05` 即 +5%，`-0.05` 即 -5%。攻击速度和移速的百分比增减通常使用该操作。
- `slot` 当前统一为 `any`，使定义的属性在对应 Gene/Xene Curios 栏位装备时生效。

内置四项测试定义均包含至少一项增益和一项减益，便于验证正负属性、叠加与卸下后的属性回滚。
