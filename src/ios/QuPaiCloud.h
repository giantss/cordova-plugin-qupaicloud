#import <Cordova/CDVPlugin.h>
#import <QPSDK/QPSDK.h>
#import "M13ProgressViewRing.h"
#import "M13ProgressHUD.h"




@interface QuPaiCloud : CDVPlugin

@property (nonatomic, assign) BOOL      enableBeauty;                       /* 是否开启美颜切换 */
@property (nonatomic, assign) BOOL      enableImport;                       /* 是否开启导入 */
@property (nonatomic, assign) BOOL      enableMoreMusic;                    /* 是否添加更多音乐按钮 */
@property (nonatomic, assign) BOOL      enableVideoEffect;                  /* 是否开启视频编辑页面 */
@property (nonatomic, assign) BOOL      enableWatermark;                    /* 是否开启水印图片 */
@property (nonatomic, assign) CGFloat   thumbnailCompressionQuality;        /* 首帧图图片质量:压缩质量 0-1 */
@property (nonatomic, strong) UIColor   *tintColor;                         /* 颜色 */
@property (nonatomic, strong) UIImage   *watermarkImage;                    /* 水印图片 */
@property (nonatomic, assign) QupaiSDKWatermarkPosition watermarkPosition;  /* 水印图片位置 */
@property (nonatomic, assign) QupaiSDKCameraPosition   cameraPosition;     /* 默认摄像头位置，only Back or Front */


@property (nonatomic, assign) CGFloat   minDuration;                         /* 允许拍摄的最小时长 */

@property (nonatomic, assign) CGFloat   maxDuration;                         /* 允许拍摄的最大时长 */

@property (nonatomic, assign) CGFloat   bitRate; /* 视频码率，建议600*1000-2000*1000,码率越大，视频越清析，视频文件也会越大。参考：8秒的视频以2000*1000的码率压缩，文件大小1.5M-2M。请开发者根据自己的业务场景设置时长和码率 */


@property (weak, nonatomic) IBOutlet UISlider *colorR;
@property (weak, nonatomic) IBOutlet UISlider *colorG;
@property (weak, nonatomic) IBOutlet UISlider *colorB;


@property(nonatomic, copy) NSString *localVideoPath;
@property(nonatomic, copy) NSString *localCoverPath;
@property(nonatomic, copy) NSString *domain;  //拼接获取视频和封图的必须参数
@property(nonatomic, copy) NSString *accessToken;
@property(nonatomic, copy) NSString *callback;
@property(nonatomic, copy) NSString *uid;

@property (nonatomic,strong) M13ProgressHUD *HUD;

- (void)initAuth:(CDVInvokedUrlCommand *)command;

- (void)recordVideo:(CDVInvokedUrlCommand *)command;

- (void)upLoadVideo:(CDVInvokedUrlCommand *)command;



@end
