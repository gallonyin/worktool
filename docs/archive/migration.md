# 文档迁移说明

以下文档已迁移到archive目录作为历史参考：

## 1. 架构文档
- 原位置：`/docs/technical/architecture.md`
- 原因：
  * 包含已过时的企业微信SDK集成方案
  * 使用了混合Java/Kotlin的实现
  * 架构设计不符合现代化要求

## 2. API文档
- 原位置：`/docs/technical/api-spec.md`
- 原因：
  * API设计基于旧版本企业微信API
  * 缺乏响应式设计
  * WebSocket实现需要现代化改造

## 3. 开发指南
- 原位置：`/docs/development/workflow.md`
- 原因：
  * 开发流程需要适应新的技术栈
  * CI/CD流程需要更新
  * 测试策略需要重新设计

## 4. 技术债务分析
- 原位置：`/docs/tech-debt/analysis.md`
- 原因：
  * 作为历史技术债务参考
  * 新项目采用全新技术栈，避免原有问题

## 5. 重构路线图
- 原位置：`/docs/refactoring/roadmap.md`
- 原因：
  * 已替换为新的实施计划
  * 原计划基于旧架构改造
  * 现采用重新开发策略

## 文档位置映射

| 原位置 | 新位置 | 备注 |
|--------|--------|------|
| /docs/technical/architecture.md | /docs/archive/old-architecture.md | 仅作参考 |
| /docs/technical/api-spec.md | /docs/archive/old-api-spec.md | 仅作参考 |
| /docs/development/workflow.md | /docs/archive/old-workflow.md | 仅作参考 |
| /docs/tech-debt/analysis.md | /docs/archive/tech-debt.md | 历史问题记录 |
| /docs/refactoring/roadmap.md | /docs/archive/old-roadmap.md | 原重构计划 |

## 注意事项

1. 新项目开发应以新文档为准
2. 历史文档仅供参考，不应直接用于新项目
3. 如需迁移功能，应重新设计实现方案
4. 文档更新请遵循新的规范和流程