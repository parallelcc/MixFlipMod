# MixFlipMod

小米 MIX Flip / MIX Flip 2 外屏增强模块

支持 HyperOS 3.0 及以上版本，需要配合支持 libxposed API 101 的 Xposed 框架使用

## 功能

隐藏外屏状态：

- 恢复部分系统应用默认样式

系统框架：

- 解除外屏应用限制
- 所有应用合盖继续使用
- 自定义外屏输入法（设置为 Gboard 后，可使用 Magisk 或 KernelSU + mountify 刷入 MixFlipGboard 模块启用外屏优化适配）
- 自定义外屏应用缩放模式

系统界面：

- 恢复外屏通知长按菜单
- 恢复外屏控制中心样式
- 隐藏外屏状态栏时钟
- 调整外屏通知图标最大数量

外屏桌面：

- 屏蔽特殊应用外屏启动页
- 外屏最近任务样式（可以切换外屏最近任务显示布局）

## 权限说明

项目会读取已安装应用列表，用于显示相关应用的名称、图标，并为需要按应用选择的功能提供应用列表

## 致谢

项目实现参考了以下开源项目的一些思路：

- [KernelSU](https://github.com/tiann/KernelSU)
- [HyperCeiler](https://github.com/ReChronoRain/HyperCeiler)

## 许可

本项目使用 AGPL-3.0 许可证，详见 [LICENSE](LICENSE)
