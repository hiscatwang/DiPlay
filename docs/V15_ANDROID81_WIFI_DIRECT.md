# v15：Android 8.1 / 9 Wi-Fi Direct 兼容适配

基于 v14 通用版。最低 Android 8.1（API 27），包含 ARMv7、ARM64、x86、x86_64。
包名 `com.shihab.diplay.ora81`，versionCode **44**，
versionName `0.2.10-android81-test15-wifi-direct-universal`；沿用原签名，支持覆盖安装并保留设置。

## 修复内容

- Android 8.1 / 9 显示并保存 Wi-Fi Direct 连接选项，不再因系统版本隐藏或重置选择。
- 连接控制器不再把旧系统的 Wi-Fi Direct 请求悄悄改成 LocalOnlyHotspot。
- API 27 / 28 使用公开的 `createGroup(Channel, ActionListener)` 接口，读取系统实际生成的 SSID 和密码。
- 避免调用 API 29 才提供的配置 Builder、三参数 createGroup、requestP2pState 和 getFrequency。
- 旧系统频率不可读时保持未知（信道 0），不误用普通 Wi-Fi 的频率，不因未知频率一直等待或在确认 CarPlay 时出错。
- 系统忙时有界重试一次；不支持、权限不足或不确定的创建超时不会反复创建。保留取消等待和已有组所有权检查，不移除其他应用的已识别组。
- Android 10 及以上保留原有频率选择、回退和成功配置记忆。界面不再把 Wi-Fi Direct 固定标成 5 GHz。
- 保留 v14 同一局域网、连接重试、开机热点等待、音频焦点、导航与 Siri 音道、方向盘切歌和画中画适配。

## 使用方法和系统限制

打开车机 Wi-Fi，在 DiPlay → 设置 → 连接设置中选择 **Wi-Fi Direct**，允许位置权限。
部分车机还需开启系统位置服务。保持车机与 iPhone 蓝牙配对，再重新连接。

Android 8.1 / 9 的公开接口不能指定网络名称、密码或频段；由车机固件决定，不能保证 5 GHz。
旧接口的活动连接会在关闭时移除，但系统可能保留其 Wi-Fi Direct 组配置文件。
清除应用数据或重装后无法确认已有组归属时，会提示重置连接，不会擅自删除未知组。
如果车机固件没有实现 Wi-Fi Direct，仍可使用车载热点或同一 Wi-Fi／局域网。

## 验证范围

发布附件 `VALIDATION-v15.zip` 包含 API 27 / 28 的旧接口、系统凭据、未知信道、控制器实际模式、
权限、忙状态、取消、超时及组清理回归测试，以及构建、lint、四架构原生库与签名校验。
Android 8.1 模拟器用于检查 v14 覆盖升级、原设置保留、Wi-Fi Direct 选项显示与重启后保存。
模拟器不能代表车机无线硬件：**车机与 iPhone 的实际 Wi-Fi Direct 连接和 ARM 实机尚待验证，本版为预发布。**

构建配置见 [MULTI_ABI.md](MULTI_ABI.md)；保留原代码许可与素材限制，见 [LICENSING.zh-CN.md](LICENSING.zh-CN.md)。
源码不包含认证资产、签名私钥、构建缓存或本机设置。
