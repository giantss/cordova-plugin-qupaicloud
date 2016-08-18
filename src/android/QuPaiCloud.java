package com.giants.qupaicloud;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.duanqu.qupai.bean.QupaiUploadTask;
import com.duanqu.qupai.engine.session.MovieExportOptions;
import com.duanqu.qupai.engine.session.ProjectOptions;
import com.duanqu.qupai.engine.session.ThumbnailExportOptions;
import com.duanqu.qupai.engine.session.UISettings;
import com.duanqu.qupai.engine.session.VideoSessionCreateInfo;
import com.duanqu.qupai.sdk.android.QupaiManager;
import com.duanqu.qupai.sdk.android.QupaiService;
import com.duanqu.qupai.upload.QupaiUploadListener;
import com.duanqu.qupai.upload.UploadService;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.UUID;

public class QuPaiCloud extends CordovaPlugin{

    private float mDurationLimit;
    private float mMinDurationLimit;
    private int mVideoBitrate;
    private int mWaterMark = -1;  //是否需要水印
    private String waterMarkPath;
    private int beautySkinProgress = 80;  //美颜参数:1-100.这里不设指定为80,这个值只在第一次设置，之后在录制界面滑动美颜参数之后系统会记住上一次滑动的状态
    private static final int RESULT_OK  = -1;
    private static final int RESULT_FIRST_USER   = 1;
    private static final int RESULT_CANCELED    = 0;
    public static final String ERROR_JSON_EXCEPTION = "JSON格式错误";
    public static final String ERROR_INVALID_PARAMETERS = "参数格式错误";
    private static final String TAG = "giants";
    private Handler mMainHandler = new Handler();
    public String localVideoPath = "";
    public String localCoverPath = "";
    private String serverVideoPath = "";
    private String serverCoverPath = "";
    private  CallbackContext callbackContext;
    //public String [] thum;
    private ProgressDialog m_pDialog = null;

    @Override
    protected void pluginInitialize() {
        super.pluginInitialize();
        m_pDialog = new ProgressDialog(this.cordova.getActivity());
        m_pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        m_pDialog.setIndeterminate(false);
        m_pDialog.setCancelable(false);
    }

    @Override
    public boolean execute(String action, CordovaArgs args,
                           final CallbackContext callbackContext) throws JSONException {

        this.callbackContext = callbackContext;
        if (action.equals("initAuth")) {

            return  initAuth(args,callbackContext);

        }else if(action.equals("recordVideo")){

            recordVideo(callbackContext);

        }else if(action.equals("upLoadVideo")){
            return  startUpload(args, callbackContext);
        }
        return super.execute(action, args, callbackContext);
    }

    // 鉴权
    public  boolean initAuth( CordovaArgs args,final CallbackContext callbackContext)throws JSONException {

 final JSONObject params;
        try {
            params = args.getJSONObject(0);
        } catch (JSONException e) {
            callbackContext.error(ERROR_INVALID_PARAMETERS);
            return true;
        }
        Contant.space  = params.getString("uid");

        Auth.getInstance().initAuth(cordova.getActivity().getApplicationContext(),callbackContext, Contant.APP_KEY, Contant.APP_SECRET,Contant.space);

        return  true;
    }
    // 录制
    public void recordVideo(final CallbackContext callbackContext)throws JSONException {


        QupaiService qupaiService = QupaiManager
                .getQupaiService(cordova.getActivity().getApplicationContext());

        if (qupaiService == null) {
            Toast.makeText(cordova.getActivity(), "插件没有初始化，无法获取 QupaiService",
                    Toast.LENGTH_LONG).show();
            return;
        }

        //视频时长

        mDurationLimit = Contant.DEFAULT_DURATION_LIMIT;


        //默认最小时长

        mMinDurationLimit = Contant.DEFAULT_MIN_DURATION_LIMIT;


        //视频码率

        mVideoBitrate = Contant.DEFAULT_BITRATE;


        //水印存储的目录
        waterMarkPath = Contant.WATER_MARK_PATH;

               //添加音乐功能
        qupaiService.addMusic(0, "Athena", "assets://Qupai/music/Athena");
        qupaiService.addMusic(1, "Box Clever", "assets://Qupai/music/Box Clever");
		
        UISettings _UISettings = new UISettings() {

            @Override
            public boolean hasEditor() {
                return true;
            }

            @Override
            public boolean hasImporter() {
                return true;
            }

            @Override
            public boolean hasGuide() {
                return true;
            }

            @Override
            public boolean hasSkinBeautifer() {
                return true;
            }
        };

        MovieExportOptions movie_options = new MovieExportOptions.Builder()
                .setVideoBitrate(mVideoBitrate)
                .configureMuxer("movflags", "+faststart")
                .build();

        ProjectOptions projectOptions = new ProjectOptions.Builder()
                .setVideoSize(480, 480)
                .setVideoFrameRate(30)
                .setDurationRange(mMinDurationLimit, mDurationLimit)
                .get();

        ThumbnailExportOptions thumbnailExportOptions = new ThumbnailExportOptions.Builder()
                .setCount(1).get();

        VideoSessionCreateInfo info = new VideoSessionCreateInfo.Builder()
                .setWaterMarkPath(waterMarkPath)
                .setWaterMarkPosition(mWaterMark)
                .setCameraFacing(0)
                .setBeautyProgress(beautySkinProgress)
                .setBeautySkinOn(true)
                .setMovieExportOptions(movie_options)
                .setThumbnailExportOptions(thumbnailExportOptions)
                .build();

        //初始化，建议在application里面做初始化，这里做是为了方便开发者认识参数的意义
        qupaiService.initRecord(info, projectOptions, _UISettings);

        //是否需要更多音乐页面--如果不需要更多音乐可以干掉
        //Intent moreMusic = new Intent();
//                if (st_more_music.isChecked()) {
//                    moreMusic.setClass(MainActivity.this, MoreMusicActivity.class);
//                } else {
//                    moreMusic = null;
//                }
        //qupaiService.hasMroeMusic(moreMusic);

        //引导，只显示一次，这里用SharedPreferences记录
        final AppGlobalSetting sp = new AppGlobalSetting(cordova.getActivity().getApplicationContext());
        Boolean isGuideShow = sp.getBooleanGlobalItem(
                AppConfig.PREF_VIDEO_EXIST_USER, true);

        /**
         * 建议上面的initRecord只在application里面调用一次。这里为了能够开发者直观看到改变所以可以调用多次
         */

        qupaiService.showRecordPage(cordova.getActivity(), RequestCode.RECORDE_SHOW, isGuideShow);
        sp.saveGlobalConfigItem(
                AppConfig.PREF_VIDEO_EXIST_USER, false);

            //getLocalVideoInfo(localVideoPath, localCoverPath, callbackContext);

            Intent intent = new Intent();
            cordova.startActivityForResult((CordovaPlugin)this, intent, RequestCode.RECORDE_SHOW);

    }

    /**
     * 开始上传
     */
    public boolean startUpload(CordovaArgs args, final CallbackContext callbackContext)throws JSONException {

        final JSONObject params;
        try {
            params = args.getJSONObject(0);
        } catch (JSONException e) {
            callbackContext.error(ERROR_INVALID_PARAMETERS);
            return true;
        }
        Contant.space  = params.getString("uid");
        this.localVideoPath  = params.getString("localVideoPath");
        this.localCoverPath  = params.getString("localCoverPath");
        //progress.setVisibility(View.VISIBLE);
        UploadService uploadService = UploadService.getInstance();
        m_pDialog.setMessage("上传进度: " + 0 + "%");
        m_pDialog.show();
        uploadService.setQupaiUploadListener(new QupaiUploadListener() {
            @Override
            public void onUploadProgress(final String uuid,final long uploadedBytes,final long totalBytes) {
                mMainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        int percentsProgress = (int) (uploadedBytes * 100 / totalBytes);
                        Log.e(TAG, "uuid:" + uuid + "data:onUploadProgress" + percentsProgress);
                        m_pDialog.setMessage("上传进度: " + percentsProgress + "%");
                    }
                });

                //progress.setProgress(percentsProgress);
            }

            @Override
            public void onUploadError(final String uuid,final int errorCode,final String message) {
                mMainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        m_pDialog.hide();
                        Log.e(TAG, "uuid:" + uuid + "onUploadError" + errorCode + message);
                    }
                });

            }

            @Override
            public void onUploadComplte(final String uuid,final int responseCode,final String responseMessage) {
                mMainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        m_pDialog.hide();
                    }
                });

                //http://{DOMAIN}/v/{UUID}.mp4?token={ACCESS-TOKEN}
                //progress.setVisibility(View.GONE);
                // btn_open_video.setVisibility(View.VISIBLE);

                //这里返回的uuid是你创建上传任务时生成的uuid.开发者可以使用其他作为标识
                //videoUrl返回的是上传成功的视频地址,imageUrl是上传成功的图片地址
                serverVideoPath = Contant.domain + "/v/" + responseMessage + ".mp4";
                serverCoverPath = Contant.domain + "/v/" + responseMessage + ".jpg";
                Log.e(TAG, "data:onUploadComplte" + "uuid:" + uuid + Contant.domain + "/v/" + responseMessage + ".jpg" + "?token=" + Contant.accessToken);
                Log.e(TAG, "data:onUploadComplte" + "uuid:" + uuid + Contant.domain + "/v/" + responseMessage + ".mp4" + "?token=" + Contant.accessToken);
                callbackContext.success(makeJson(serverVideoPath, serverCoverPath, callbackContext));
            }
        });
        String uuid = UUID.randomUUID().toString();
        Log.e("QupaiAuth", "accessToken ::" + Contant.accessToken + ",space::" + Contant.space);
        startUpload(createUploadTask(cordova.getActivity().getApplicationContext(), uuid, new File(localVideoPath), new File(localCoverPath),
                Contant.accessToken, Contant.space, Contant.shareType, Contant.tags, Contant.description));

        return  true;
    }

    /**
     * 开始上传
     * @param data 上传任务的task
     */
    private void startUpload(QupaiUploadTask data) {
        try {
            UploadService uploadService = UploadService.getInstance();
            uploadService.startUpload(data);
        } catch (IllegalArgumentException exc) {
            Log.e(TAG, "upload Missing some arguments. " + exc.getMessage());
        }
    }

    /**
     * 创建一个上传任务
     * @param context
     * @param uuid        随机生成的UUID
     * @param _VideoFile  完整视频文件
     * @param _Thumbnail  缩略图
     * @param accessToken 通过调用鉴权得到token
     * @param space        开发者生成的Quid，必须要和token保持一致
     * @param share       是否公开 0公开分享 1私有(default) 公开类视频不需要AccessToken授权
     * @param tags        标签 多个标签用 "," 分隔符
     * @param description 视频描述
     * @return
     */
    private QupaiUploadTask createUploadTask(Context context, String uuid, File _VideoFile, File _Thumbnail, String accessToken,
                                             String space, int share, String tags, String description) {
        UploadService uploadService = UploadService.getInstance();
        return uploadService.createTask(context, uuid, _VideoFile, _Thumbnail,
                accessToken, space, share, tags, description);
    }

    /**
     * 组装JSON
     *
     * @param serverVideoPath
     * @param serverCoverPath
     * @return
     */
    private JSONObject makeJson(String serverVideoPath, String serverCoverPath, final  CallbackContext callbackContext) {
        String json = "{\"serverVideoPath\": \"" + serverVideoPath + "\", " + " \"serverCoverPath\": \"" + serverCoverPath + "\"}";
        JSONObject jo = null;
        try {
            jo = new JSONObject(json);
        } catch (JSONException e) {
            e.printStackTrace();
            callbackContext.error(ERROR_JSON_EXCEPTION);
        }
        return jo;
    }


}
