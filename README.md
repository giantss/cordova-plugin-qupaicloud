# Cordova_QuPaiCloud_插件

	
这个一个QuPaiCloud SDK的Cordova 插件。 		

##主要功能
- 鉴权
- 拍视频
- 上传视频

##安装要求
- Cordova Version >=3.5
- Cordova-Android >=4.0
- Cordova-iOS >=4.0

##安装
1. 命令行运行      ```cordova plugin add https://github.com/giantss/cordova-plugin-qupaicloud```  
2. 命令行运行 cordova build --device     
 		
##注意事项					        	
1. 这个插件要求cordova-android 的版本 >=4.0,推荐使用 cordova  5.0.0 或更高的版本，因为从cordova 5.0 开始cordova-android 4.0 是默认使用的android版本
2.  请在cordova的deviceready事件触发以后再调用本插件！！！		
3. <del>在低于5.1.1的cordova版本中存在一个Bug，如果你有多个插件要修改iOS工程中的 “*-Info.plist” CFBundleURLTypes, 只有第一个安装的插件才会生效.所以安装完插件请务必在你的Xcode工程里面检查一下URLTypes。 关于这个bug的详情你可以在 [这里](https://issues.apache.org/jira/browse/CB-8007)找到</del> 建议安装使用5.1.1及以上的cordova版本 	
4. Android版本请确保你的签名是正确的			
				

##使用方式   
1. 鉴权              								
```Javascript
        QuPaiCloud.initAuth(function () {
            //鉴权成功这里可以调用视频录制的方法
        }, function (msg) {
            alert(msg);
        },{
            uid: 'xxx'   //xxx代表上传到趣拍云上存储的相对路径  我是已uid（用户id）作为作为上传的目录 方便管理。 
        });
					
```	


2. 视频录制              								
```Javascript
 QuPaiCloud.recordVideo();  
					
```	

3. 上传视频              								
```Javascript
 QuPaiCloud.upLoadVideo(function(param){
                        Tux.Global.serverVideoPath = param.serverVideoPath; //服务端视频地址
                        Tux.Global.serverCoverPath = param.serverCoverPath;//封图视频地址
                        //alert(Tux.Global.serverVideoPath);
                        //alert(Tux.Global.serverCoverPath);
                        Tux.Util.videoUpLoad(cmp);
                    },function(msg){
                        alert(msg);
                    },{
                        localVideoPath: Tux.Global.localVideoPath,
                        localCoverPath: Tux.Global.localCoverPath，
						 uid: 'xxx' 
                    });
					
```	
			     
### Android Quirks
需要在android 的MainActivity里面重写 onActivityResult方法
```Javascript
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 10001){
            if (resultCode == RESULT_OK) {
                QuPaiCloud q = new QuPaiCloud();
                RecordResult result = new RecordResult(data);
                //得到视频地址，和缩略图地址的数组，返回十张缩略图
                q.localVideoPath = result.getPath();
                q.localCoverPath = result.getThumbnail()[0];
                result.getDuration();
                Log.e(TAG, "视频路径:" + q.localVideoPath + "图片路径:" + q.localCoverPath);

                appView.loadUrl("javascript:Tux.Global.getLocalVideoInfo('"+q.localVideoPath+"','"+q.localCoverPath+"');");
                //q.getLocalVideoInfo(result.getPath(),result.getThumbnail()[0]);
                //q.startUpload(getApplicationContext(),m_pDialog);
                /**Restart ADB integration and try again

                 * 清除草稿,草稿文件将会删除。所以在这之前我们执行拷贝move操作。
                 * 上面的拷贝操作请自行实现，第一版本的copyVideoFile接口不再使用
                 */
//            QupaiService qupaiService = QupaiManager
//                    .getQupaiService(MainActivity.this);
//            qupaiService.deleteDraft(getApplicationContext(),data);

            } else {
                if (resultCode == RESULT_CANCELED) {
                    //  Toast.makeText(cordova.getActivity(), "RESULT_CANCELED", Toast.LENGTH_LONG).show();
                }
            }
        }

    }

```

