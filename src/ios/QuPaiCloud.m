//
//  QuPaiCloud.m
//  ChinaBike
//
//  Created by pengzhong on 16/8/11.
//
//

#import "QuPaiCloud.h"

#define kQPAppKey @"20a5b0e24c3b450"
#define kQPAppSecret @"b06f9fe4b3ba4ae5af7026d14cf0eddb"
NSString *INITAUTH_SUCCESS = @"鉴权成功";



@implementation QuPaiCloud

/**
 *  鉴权
 */
- (void)initAuth:(CDVInvokedUrlCommand *)command
{
    [[QPAuth shared] registerAppWithKey:kQPAppKey secret:kQPAppSecret space:@"space" success:^(NSString *accessToken){
        [[NSUserDefaults standardUserDefaults] setObject:accessToken forKey:@"accessToken"];
        
        _accessToken = accessToken;
        
        NSLog(@"auth  success accessToken %@",_accessToken);
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        
    } failure:^(NSError *error) {
        
        NSLog(@"auth  failure %@",error);
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];

        
    }];

}

/**
 *  插件初始化主要用于appkey的注册
 */
- (void)pluginInitialize
{
    
    
    _enableImport = true;
    _enableBeauty = true;
    _enableMoreMusic = true;
    _enableVideoEffect = true;
    _enableWatermark = true;
    _thumbnailCompressionQuality = 1;
    _cameraPosition = QupaiSDKCameraPositionBack;
    _minDuration = 2;
    _maxDuration = 8;
    _bitRate = 2000*1000;
    _domain = @"http://chinabike.s.qupai.me";

}

/**
 *  视频录制
 */

-(void) recordVideo:(CDVInvokedUrlCommand *)command
{
    NSLog(@"执行了");
    
    _callback = command.callbackId;
    
    QupaiSDK *sdk = [QupaiSDK shared];
    [sdk setDelegte:(id<QupaiSDKDelegate>)self];
    
    /* 可选设置 */
    [sdk setEnableImport:_enableImport];
    [sdk setEnableMoreMusic:_enableMoreMusic];
    [sdk setEnableBeauty:_enableBeauty];
    [sdk setEnableVideoEffect:_enableVideoEffect];
    [sdk setEnableWatermark:_enableWatermark];
    [sdk setTintColor:[UIColor colorWithRed:_colorR.value/255.0 green:_colorG.value/255.0 blue:_colorB.value/255.0 alpha:1]];
    [sdk setThumbnailCompressionQuality:_thumbnailCompressionQuality];
    [sdk setWatermarkImage:_enableWatermark ? [UIImage imageNamed:@"watermask"] : nil];
    [sdk setWatermarkPosition:QupaiSDKWatermarkPositionTopRight];
    [sdk setCameraPosition:_cameraPosition];
    
    /* 基本设置 */
    UIViewController *recordController = [sdk createRecordViewControllerWithMinDuration:_minDuration
                                                                            maxDuration:_maxDuration
                                                                                bitRate:_bitRate];
    
    UINavigationController *navigation = [[UINavigationController alloc] initWithRootViewController:recordController];
    navigation.navigationBarHidden = YES;
    
    [self.viewController presentViewController:navigation animated:YES completion:nil];
    
}
/**
 *  视频上传
 */

-(void) upLoadVideo:(CDVInvokedUrlCommand *)command
{
    
    _callback = command.callbackId;
    
    NSDictionary* path = [command argumentAtIndex:0];
    if(path){
        
        _localVideoPath = [path objectForKey:@"localVideoPath"];
        _localCoverPath = [path objectForKey:@"localCoverPath"];
        
        
    }
    _HUD = [[M13ProgressHUD alloc] initWithProgressView:[[M13ProgressViewRing alloc] init]];
    _HUD.progressViewSize = CGSizeMake(60.0, 60.0);
    _HUD.animationPoint = CGPointMake([UIScreen mainScreen].bounds.size.width / 2, [UIScreen mainScreen].bounds.size.height / 2);
    [self.webView addSubview:_HUD];
    _HUD.status = @"视频上传中";
    [_HUD show:YES];

    
    QPUploadTask *uploadTask = [[QPUploadTaskManager shared] createUploadTaskWithVideoPath:_localVideoPath
                                                                             thumbnailPath:_localCoverPath];
    
    [[QPUploadTaskManager shared] startUploadTask:uploadTask progress:^(CGFloat progress) {
        dispatch_async(dispatch_get_main_queue(), ^{
            
           
            NSLog(@"progress: %f",progress);


            [_HUD setProgress:progress animated:YES];

            
        });
    } success:^(QPUploadTask *uploadTask, NSString *remoteId) {
        dispatch_async(dispatch_get_main_queue(), ^{
            
            [_HUD hide:YES];
            
            NSString *serverVideoPath = [NSString stringWithFormat:@"%@/v/%@.mp4", _domain,remoteId];
            
            NSString *serverCoverPath = [NSString stringWithFormat:@"%@/v/%@.jpg", _domain,remoteId];
            
            
             NSLog(@"serverVideoPath = %@",serverVideoPath);
            
             NSLog(@"serverCoverPath = %@",serverCoverPath);
            
            NSMutableDictionary *Dic = [NSMutableDictionary dictionaryWithCapacity:2];
            [Dic setObject:serverVideoPath forKey:@"serverVideoPath"];
            [Dic setObject:serverCoverPath forKey:@"serverCoverPath"];
            CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:Dic];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:self.callback];
            
            
        });
        
        
    } failure:^(NSError *error) {
        dispatch_async(dispatch_get_main_queue(), ^{
            _HUD.status = @"视频失败";

            [_HUD hide:YES];
             NSLog(@"upload failed");
            
            
        });
    }];

}

/**
 *  拍视频结束回调
 */

- (void)qupaiSDK:(id<QupaiSDKDelegate>)sdk compeleteVideoPath:(NSString *)videoPath thumbnailPath:(NSString *)thumbnailPath
{
    
    //    NSData *data = [NSData dataWithContentsOfFile:thumbnailPath];
    //    NSAssert(data.length != 0, @"failed");
    __weak QuPaiCloud* weakSelf = self;

    
    NSLog(@"Qupai SDK compelete %@",videoPath);
    [self.viewController dismissViewControllerAnimated:YES completion:nil];
    if (videoPath && thumbnailPath) {
        NSLog(@"videoPath: %@",videoPath);
        UISaveVideoAtPathToSavedPhotosAlbum(videoPath, nil, nil, nil);
        NSLog(@"thumbnailPath: %@",thumbnailPath);
        UIImageWriteToSavedPhotosAlbum([UIImage imageWithContentsOfFile:thumbnailPath], nil, nil, nil);

        //OC调用JS方法
        [weakSelf.commandDelegate evalJs: [NSString stringWithFormat:@"Tux.Global.getLocalVideoInfo('%@', '%@')", videoPath,thumbnailPath]];
        
    }
    
}








@end
