# Android 应用内悬浮窗
## 架构
- FloatingManager（监听 Activity 生命周期，调用添加和移除方法）
- FloatingViewImpl（实际执行添加和移除逻辑）
- FloatingMagnetView（自定义内容的外壳，包含具体的滑动&吸附逻辑）
- 自定义内容 View