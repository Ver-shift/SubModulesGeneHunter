# SubmodulesGeneHunter

## 项目简介

这是一个 NeoForge 多模块工程，按功能拆分为多个子模块，方便 2-3 人并行开发与独立维护。

## 模块说明

- `GalaxyLib`：公共基础库，提供通用能力与工具类。
- `Beyond`：关卡与安全区相关玩法模块。
- `Biotech`：基因与特性系统模块。
- `ModFix`：兼容性修复与 Mixin 补丁模块。
- `GeneHunter`：主整合模块，聚合并对外提供核心功能。
- `GameText`：联调与集成测试模块。

## 当前分工

- `member/daxiazhanshenzhao`：负责除 `Beyond` 外的模块开发。
- `member/pumpkin1zz`：负责 `Beyond` 模块开发。

## Git 分支规则（极简）

- `main`：主开发分支。
- `member/<name>`：个人开发分支。
- `version/neoforge-1.21.1/1.0.0`：版本冻结分支，长期保留。

## 开发约定

1. 每个人优先在自己的 `member/*` 分支开发。
2. 功能完成后合并到 `main`。
3. 发版从 `main` 切 `version/*` 分支。
4. 线上修复优先在 `version/*` 处理，再同步回 `main`。

## 依赖管理约定

- 各模块依赖由各模块自行维护（例如 `Beyond` 的依赖写在 `Beyond/build.gradle`，`Biotech` 的依赖写在 `Biotech/build.gradle`）。
- 暂不做全局统一依赖收敛，冲突按实际问题再处理。
