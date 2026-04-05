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

## 依赖管理约定（详细去重方案）

### 1) 仓库去重（根项目统一）

- 所有仓库地址统一在根 `build.gradle` 的 `allprojects.repositories` 声明。
- 子模块默认不重复写 `repositories`，防止遗漏导致传递依赖解析失败。

### 2) 版本去重（根配置集中，直接引用）

- 在根 `gradle.properties` 里维护共享依赖版本（可直接复制）：

```ini
# 共享依赖版本（模块可直接引用，避免重复维护）
dep_ldlib2_version=2.2.5
dep_curios_version=9.5.1+1.21.1
dep_jetbrains_annotations_version=24.1.0
```

- 子模块里直接引用，不使用额外公式变量，例如 `Biotech/build.gradle`：

```groovy
implementation("com.lowdragmc.ldlib2:ldlib2-neoforge-${minecraft_version}:${dep_ldlib2_version}:all")
implementation("top.theillusivec4.curios:curios-neoforge:${dep_curios_version}")
compileOnly("org.jetbrains:annotations:${dep_jetbrains_annotations_version}")
```

### 3) 坐标归属（模块独立）

- 依赖坐标仍写在各模块 `build.gradle`（谁用谁声明）。
- 示例：`Biotech` 依赖仍在 `Biotech/build.gradle`，只是版本由根共享配置提供。

### 4) 覆盖策略（尽量少用）

- 默认：模块直接使用根共享版本。
- 仅在临时验证新版本时，才在模块内单独改版本；验证后尽量回收为共享版本。
