# DiPlay 通用架构版

基于 [shihabal3amri/DiPlay](https://github.com/shihabal3amri/DiPlay) 及本项目的欧拉好猫哈曼适配版本，
为 Android 8.1 及以上系统增加 ARM 架构支持。保留原作者及贡献者署名。

本分支 `android81-universal` 对应 **v15 通用版源码**；v10、v11 保持各自功能，
完整对应源码以发布页单独标注版本的 `*-source.zip` 提供。

## v15 更新

- 修复 Android 8.1 / 9 缺少 Wi-Fi Direct 选项，补齐旧版创建接口与系统生成凭据的读取。
- 不再暗中切换为 LocalOnlyHotspot；未知信道不再阻止启动，不误报 5 GHz。
- 保留 v14 的所有连接与音频适配。旧系统频段由车机固件选择，真实车机连接仍需验证。

使用方法和限制见 [v15 更新说明](docs/V15_ANDROID81_WIFI_DIRECT.md)。

## v14 更新

- 新增“同一 Wi-Fi／局域网”连接模式：车机与 iPhone 同连路由器、随身 Wi-Fi 或另一台手机的热点。
- 同时提供 IPv4 / IPv6 服务发现和监听，保留适合哈曼车机的 IPv4 优先策略。
- 保留最长 120 秒的开机热点等待，并等待网络地址稳定后继续连接。
- 发出启动指令后 30 秒仍未收到 CarPlay TCP 连接时重试；网络未就绪采用 2、4、8、16、30 秒退避，最多自动重试 5 次。配置不匹配时提示检查设置，支持手动重新连接。
- 旧连接的超时回调不会关闭新连接；播放稳定 60 秒后重置重试预算。
- 保留 v12 的音频焦点、导航与 Siri 音道、方向盘切歌、画中画和四架构支持。

使用方法与验证范围见 [v14 更新说明](docs/V14_LAN_RETRY.md)。

## 下载与安装

[下载 v15 Android 8.1 Wi-Fi Direct 兼容版 APK、源码和校验文件](https://github.com/hiscatwang/DiPlay/releases/tag/v0.2.10-universal-v15-wifi-direct)。

[下载 v14 通用版 APK、源码和校验文件](https://github.com/hiscatwang/DiPlay/releases/tag/v0.2.10-universal-v14-lan)。


[下载 v10 / v11 / v12 通用 APK、对应源码、安装说明和校验文件](https://github.com/hiscatwang/DiPlay/releases/tag/v0.2.10-universal-v10-v12)。
每个 APK 都同时包含 `armeabi-v7a`、`arm64-v8a`、`x86`、`x86_64`，最低 Android 8.1（API 27）。

| 版本 | 保留的版本功能 | versionCode |
| --- | --- | --- |
| v10 | 方向盘切歌、既有音频与显示适配 | 39 |
| v11 | v10 功能，加开机热点等待修复 | 40 |
| v12 | v11 功能，加 Siri 跟随导航音道 | 41 |
| v14 | v12 通用版功能，加同一局域网与连接超时重试 | 43 |
| v15 | v14 功能，加 Android 8.1 / 9 Wi-Fi Direct 兼容 | 44 |

CarPlay 返回车机入口统一使用现有绿色 DiPlay 图标，默认文字为“返回车机”。
覆盖安装时，旧的欧拉/比亚迪默认名称自动迁移，其他自定义名称和手动上传的图标保留。
包名继续使用 `com.shihab.diplay.ora81`，以便使用原签名覆盖对应版本并保留设置。
这些 APK 是同一应用的不同版本；Android 通常不允许降级安装。

## 验证范围

- 历史 v10、v11、v12 通用版分别通过 771、788、797 项测试，三模块 lint 零错误（仍有非错误警告）。
- 四种架构的原生库均完成构建、ELF、API 27 链接依赖及签名校验。
- Android 8.1 模拟器验证对应原版覆盖安装、名称迁移、设置保留、进程重启，
  并实际加载 x86 / x86_64 的原生库。
- **本次通用构建尚未在 ARM 实机或真实 CarPlay 连接中验证。**
  “通用”表示包含四种 CPU 架构，不代表所有车机的音频、蓝牙或热点实现均已适配。
- 原有欧拉 Intel 版本的车主实车反馈属于历史验证，不等同于本次 ARM 构建已经验证。

## 源码与许可

构建说明见 [docs/MULTI_ABI.md](docs/MULTI_ABI.md)，历史欧拉适配见
[docs/ORA_ANDROID81.md](docs/ORA_ANDROID81.md)。发布页自动生成的 Source code 是本分支的 v15 源码；
v10、v11 请下载各自的 `*-source.zip`。

本项目面向个人研究、车友交流和非商业维护；不改变原有 GPL/AGPL 代码许可赋予的权利。
保留上游及第三方素材限制，见 [许可说明](docs/LICENSING.zh-CN.md) 和
[第三方说明](docs/THIRD_PARTY_NOTICES.md)。随包实验性运行资源沿用上游公开安装包，
不代表 Apple 官方认证。认证资产由外部目录提供；源码不包含 Android 签名私钥。
