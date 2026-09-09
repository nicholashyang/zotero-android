# 首轮 UI 改版验证结果

记录日期：2026-09-10。基线：`b5b71acd02f8771218321099b84c3c56dc00b190`。
分支：`codex/ios-inspired-ui`。本轮界面实现和可安装开发包已完成；账号业务回归
尚未完成，因此本记录不把整个验收计划标为通过。

## 开发 APK

- 文件：[Zotero-iOS-inspired-dev-debug.apk](../../artifacts/Zotero-iOS-inspired-dev-debug.apk)
- 包名：`org.zotero.android.debug`；版本：`1.0.0-280`，versionCode 280。
- 大小：190,795,550 字节（约 182 MiB）。沿用开发签名、应用名称与图标。
- SHA-256：`29c4a490fe5d1dfa0c4938aae56d78d0b0d6172b62be38ab645440438bde7054`
- [机器可读校验信息](apk-manifest.json)。APK 为本地交付文件，没有提交到 Git 或发布到远端。

## 构建与检查

JDK 17.0.20.1、Gradle 9.6.1、SDK Platform 37.0 / Build Tools 36.0.0。
完整命令与资源打包步骤见 [开发说明](../DEVELOPMENT.md)。

| 检查 | 实际结果 |
| --- | --- |
| 原版 `:app:assembleDevDebug` | 通过；用于 UI 对照的独立副本只增加测试依赖和 runner。 |
| 改版 `:app:assembleDevDebug` | 通过；输出上面的开发 APK。 |
| 改版 `:app:assembleDevDebugAndroidTest` | 通过。 |
| 改版 `:app:testDevDebugUnitTest` | 57 项通过，0 失败、0 错误、0 跳过。 |
| 改版 `:app:lintDevDebug` | 报告生成成功；逐条与原版比较，无新增问题。 |
| 缺少 Firebase 配置 | 在隔离副本应用当前构建脚本，移走 JSON，清除构建输出后重新 assemble 成功；Google Services 和两个 Crashlytics 任务跳过。随后恢复原文件。该 APK 也通过正常入口冷启动。 |
| 正常应用入口 | 最终交付 APK 安装成功，正常 Application 冷启动到引导页成功；不是只运行测试 Application。 |
| 子模块、Git diff 检查 | 子模块版本未改变；`git diff --check` 通过。 |
| 开发 CI | 工作流已配置，尚未推送触发远端执行。 |

Lint 原版为 **571 Error + 1 Fatal + 695 Warning**；改版为
**571 Error + 1 Fatal + 690 Warning**。新增 0 项，因复用原有字符串减少 5 项
`UnusedResources` 警告。既有错误主要为翻译复数、格式字符串和缺少翻译，另有
Locale、API 和 Manifest 问题。保留上游 `abortOnError = false`；任务成功不表示
这些既有问题已修复。比较按规则 ID、严重程度、仓库相对路径和消息进行，忽略
改版造成的行号变化。详见 [Lint 差异](lint-comparison.json) 与
[JVM 测试汇总](unit-test-summary.json)。

## 设备测试与视觉检查

隔离模拟器为 API 35 / Google APIs / ARM64，手机尺寸 1080×2340、440 dpi。
测试替换为普通 Application，不连接数据库、账号或同步。最后一次主套件
**21 项全部通过**（32.534 秒）：

- 8 项交互：搜索清除与取消、搜索时返回、详情与文献打开动作分离、保存与取消
  回调分离、独立多选、全部设置入口、200 条列表的查询/滚动保存恢复、键盘展开
  时最后一行仍能滚到工具栏上方。
- 12 项截图：五个核心页面、文献/设置深色、文献 200% 字号、空列表、下载中、
  加载中和加载失败。
- 1 项 PDF SDK 冒烟：主线程调用 `PSPDFKit.initialize(context, null)`，打开仓库
  自制的单页 PDF，确认页数与可提取文字。

此外，五页统一深色、五页统一 200% 字号和五页横屏分别通过 **5 项截图测试**。
横屏使用同一隔离模拟器覆盖为 2560×1600、240 dpi，结束后已恢复尺寸；这里只
验证公共组件在宽屏的显示，**不代表完整平板分栏导航通过**。200% 截图采用
Compose LocalDensity 的字号覆盖，不代表所有系统版本的字号缩放行为。

已人工查看截图中的长标题、中英文内容、分组、操作栏、大字号换行、空状态和
错误状态。截图测试确认能渲染并捕获，没有设置像素级 golden 断言。文献长标题
按设计最多两行后省略；大字号详情标签允许两行，内容随高度增长，页面可滚动。
详情可点击字段至少 48dp，条目类型行不再用固定高度限制大字号。

| 展示模式 | 文库 | 分类 | 文献 | 详情 | 设置 |
| --- | --- | --- | --- | --- | --- |
| 浅色及原版对照 | [五页前后对照](README.md#截图与对照) | 同左 | 同左 | 同左 | 同左 |
| 深色 | [查看](phone-dark/libraries-light.png) | [查看](phone-dark/collections-light.png) | [查看](phone-dark/items-light.png) | [查看](phone-dark/details-light.png) | [查看](phone-dark/settings-light.png) |
| 200% 字号 | [查看](phone-large-text/libraries-light.png) | [查看](phone-large-text/collections-light.png) | [查看](phone-large-text/items-light.png) | [查看](phone-large-text/details-light.png) | [查看](phone-large-text/settings-light.png) |
| 宽屏横向 | [查看](tablet-landscape/libraries-light.png) | [查看](tablet-landscape/collections-light.png) | [查看](tablet-landscape/items-light.png) | [查看](tablet-landscape/details-light.png) | [查看](tablet-landscape/settings-light.png) |

五页原版 fixture 截图测试通过，使用同样的手机配置和样例数据。详情 fixture 仅
包含标题、元数据和摘要，文库/分类为简化样例；所有截图均不包含真实账号资料。

主题六组主要文字/背景颜色对比度计算值为 5.01–17.01，见
[颜色对比记录](color-contrast.json)。返回、详情、选择等按钮的语义标签被交互
测试使用；文献类型、笔记与分类展开/收起增加或复用原有朗读文案。完整 TalkBack
朗读顺序尚未做人工回归，不能据此声明整应用无障碍合规。

## 仍需完成的验收

用户已确认有可增删测试文献的专用账号，并选择手动登录。目前可见模拟器停留
在登录入口，尚未确认登录及首次同步完成。后续仅使用单独的测试分类与带
`Codex UI regression` 前缀的测试条目，避免操作其他文献。

| 待验证项目 | 当前边界 |
| --- | --- |
| 登录、首次/后续同步、分类切换 | 未完成真实账号回归。 |
| 导入、笔记、附件下载、引用导出 | 原有业务逻辑未改；尚未验证真实服务与持久化。 |
| 详情保存/取消、作者重排 | 回调分离已测试，真实数据库保存/取消及作者重排仍待验证。 |
| 完整阅读器入口、批注与导出 | 仅无密钥初始化及本地 PDF 解析通过，未验证完整阅读器 UI，也未确认正式授权。 |
| 平板分栏与导航返回 | 公共组件宽屏截图通过；真实导航图和分栏操作未验证。 |
| 长列表性能 | 200 条列表状态恢复通过；没有原版/改版滚动帧时间基准，不作性能提升结论。 |

SDK 依赖和生产初始化路径保持原状。通过 PDF 解析冒烟不保证批注、导出等功能
或正式发布的授权完整性。首轮改版不改变应用包名、导航参数、数据库结构、同步
协议及导入、引用业务 API。

## 可复核记录

- [最终构建任务结果](test-results/final-validation.txt)
- [原版构建任务结果](test-results/baseline-validation.txt)
- [无 Firebase 配置构建结果](test-results/no-firebase-validation.txt)
- [21 项设备测试](test-results/instrumentation.txt)
- [原版五页截图测试](test-results/baseline-instrumentation.txt)
- [深色](test-results/phone-dark.txt)、[大字号](test-results/phone-large-text.txt)、[横屏](test-results/tablet-landscape.txt)

完整构建日志与本地工具链保存在忽略的 `.local/` 下；早期临时目录在中断后丢失，
本报告采用恢复持久化工具链后的实际结果。完整 Lint HTML/XML 位于
`app/build/reports/`。以上提交记录只保存测试样例、摘要与截图，不包含账号凭据。
