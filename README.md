# SubmodulesGeneHunter

## 模块分级

- `Beyond`：肉鸽关卡系统 lib。
- `Biotech`：局内基因、局外基因 lib。
- `GalaxyLib`：核心库，公共依赖与 util。
- `ModFix`：修复 mod 冲突或魔改本体，主要用于 mixin。
- `GeneHunter`：整合包核心 mod，提供主功能并整合前置能力。
- `GameText`：纯测试模块，不写业务代码，默认将所有 mod 作为依赖。

## 小团队 Git 协作（2-3 人极简版）

### 分支规则

- `main`：主开发分支，保持可运行。
- `member/<name>`：个人长期开发分支，例如 `member/alice`。
- `version/neoforge-1.21.1-1.0.0`：版本冻结分支，永久保留。

### 日常流程

1. 每人只在自己的 `member/<name>` 开发。
2. 功能完成后，从 `member/<name>` 提 PR 到 `main`。
3. 发版时，从 `main` 切 `version/*` 分支并打版本标签。
4. 线上修复先改 `version/*`，再同步回 `main`。

### 同一 lib 多人维护

- 一个 lib 可以多人开发，但指定 1 人做最终合并。
- 合并前只改自己负责目录，避免跨模块顺手改动。
- 出现冲突时，最后合并的人负责解决并通知相关作者确认。

### 提交信息（尽量统一）

- `feat(module): ...` 新功能
- `fix(module): ...` 问题修复
- `chore(module): ...` 杂项/构建调整

示例：`feat(GeneHunter): add gene scanner ui`
