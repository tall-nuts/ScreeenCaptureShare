# ScreeenCaptureShare
安卓截屏涂鸦分享（仿金大师），底部拼接公司logo或微信公众号二维码。
- 原理：使用view.getDrawingCache()方法进行屏幕截屏，之后进行canvas涂鸦绘制，在点击分享后在canvas底部绘制logo保存图片。
- 使用：
调用ScreenCaptureShareActivity.screenCapture( view,  offset)即可。
        1. 参数1为截屏页面的任意控件（通过其获取RootView）
        2. 参数2为偏移量（默认传0就行）
        
*代码暂不完善。诸君根据情况自己修改合成拼接的logo代码。*

金大师截屏分享：

![image](https://github.com/pengfeigao/ScreeenCaptureShare/blob/master/screencapture/ScreenCapture-JDS.gif =300*534)

我们的截屏分享：

![image](https://github.com/pengfeigao/ScreeenCaptureShare/blob/master/screencapture/ScreenCapture.gif =300*534)
