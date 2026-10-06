# DiPlay 通用架构版

基于 [shihabal3amri/DiPlay](https://github.com/shihabal3amri/DiPlay) 及本项目的欧拉好猫哈曼适配版本，
为 Android 8.1 及以上系统增加 ARM 架构支持。保留原作者及贡献者署名。

本分支 `android81-universal` 对应 **v12 通用版源码**；v10、v11 保持各自功能，
完整对应源码以发布页单独标注版本的 `*-source.zip` 提供。

## 下载与安装

[下载 v10 / v11 / v12 通用 APK、对应源码、安装说明和校验文件](https://github.com/hiscatwang/DiPlay/releases/tag/v0.2.10-universal-v10-v12)。
每个 APK 都同时包含 `armeabi-v7a`、`arm64-v8a`、`x86`、`x86_64`，最低 Android 8.1（API 27）。

| 版本 | 保留的版本功能 | versionCode |
| --- | --- | --- |
| v10 | 方向盘切歌、既有音频与显示适配 | 39 |
| v11 | v10 功能，加开机热点等待修复 | 40 |
| v12 | v11 功能，加 Siri 跟随导航音道 | 41 |

CarPlay 返回车机入口统一使用现有绿色 DiPlay 图标，默认文字为“返回车机”。
覆盖安装时，旧的欧拉/比亚迪默认名称自动迁移，其他自定义名称和手动上传的图标保留。
包名继续使用 `com.shihab.diplay.ora81`，以便使用原签名覆盖对应版本并保留设置。
三个 APK 是同一应用的不同版本；Android 通常不允许降级安装。

## 验证范围

- 三个版本分别通过 771、788、797 项测试，三模块 lint 零错误（仍有非错误警告）。
- 四种架构的原生库均完成构建、ELF、API 27 链接依赖及签名校验。
- Android 8.1 模拟器验证对应原版覆盖安装、名称迁移、设置保留、进程重启，
  并实际加载 x86 / x86_64 的原生库。
- **本次通用构建尚未在 ARM 实机或真实 CarPlay 连接中验证。**
  “通用”表示包含四种 CPU 架构，不代表所有车机的音频、蓝牙或热点实现均已适配。
- 原有欧拉 Intel 版本的车主实车反馈属于历史验证，不等同于本次 ARM 构建已经验证。

## 源码与许可

构建说明见 [docs/MULTI_ABI.md](docs/MULTI_ABI.md)，历史欧拉适配见
[docs/ORA_ANDROID81.md](docs/ORA_ANDROID81.md)。发布页自动生成的 Source code 是本分支的 v12 源码；
v10、v11 请下载各自的 `*-source.zip`。

本项目面向个人研究、车友交流和非商业维护；不改变原有 GPL/AGPL 代码许可赋予的权利。
保留上游及第三方素材限制，见 [许可说明](docs/LICENSING.zh-CN.md) 和
[第三方说明](docs/THIRD_PARTY_NOTICES.md)。随包实验性运行资源沿用上游公开安装包，
不代表 Apple 官方认证。认证资产由外部目录提供；源码不包含 Android 签名私钥。
