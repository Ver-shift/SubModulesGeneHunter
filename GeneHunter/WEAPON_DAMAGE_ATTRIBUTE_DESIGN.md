# 武器伤害属性设计

## 目标

武器伤害的固定值与百分比必须使用不同属性存储，避免把原版属性修饰器的乘算语义混入业务伤害计算。

## 属性模型

每种武器形制（刀、剑、斧、锤）都有一对属性：

| 属性 | 含义 | 配置 operation | amount 单位 |
| --- | --- | --- | --- |
| `gene_hunter:<type>_weapon_damage` | 固定伤害 | `add_value` | 伤害点数 |
| `gene_hunter:<type>_weapon_damage_rate` | 伤害倍率 | `add_value` | 百分点 |

其中 `<type>` 为 `blade`、`sword`、`axe` 或 `hammer`。

## 配置示例

剑类伤害增加 25%：

```json
{
  "type": "gene_hunter:sword_weapon_damage_rate",
  "id": "gene_hunter:example_sword_damage_rate",
  "amount": 25,
  "operation": "add_value",
  "slot": "any"
}
```

剑类固定伤害增加 3 点：

```json
{
  "type": "gene_hunter:sword_weapon_damage",
  "id": "gene_hunter:example_sword_damage",
  "amount": 3,
  "operation": "add_value",
  "slot": "any"
}
```

禁止把 `*_weapon_damage` 配成 `add_multiplied_total` 来表示百分比。

## 计算规则

手持匹配形制武器时：

```text
最终伤害 = （原始伤害 + 形制固定伤害）
         × （1 + 形制伤害倍率 / 100）
         + 非匹配形制固定伤害转换值
```

倍率属性用 `add_value` 相加：`+25` 与 `-12` 得到 `+13%`。该规则由 `WeaponDamageHandler` 统一执行；数据包只负责表达数值，不直接依赖原版 `add_multiplied_total` 的计算顺序。

## 兼容性与迁移

旧数据包中 `*_weapon_damage` 的 `add_multiplied_total` 仍由运行时代码暂时兼容，避免旧内容立即失效。新内容和后续迁移必须改用对应的 `*_weapon_damage_rate`，并将小数比例换成百分点：`0.25` 改为 `25`，`-0.16` 改为 `-16`。
