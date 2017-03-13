package com.sen5.ocup.yili;

import android.content.Context;
import android.util.Log;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSStsTokenCredentialProvider;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;
import com.orhanobut.logger.Logger;
import com.sen5.ocup.callback.RequestCallback;

/**
 * Created by chenqianghua on 2016/11/7.
 */
public class OOSClientUtils {
    private String endpoint = "http://oss-cn-hangzhou.aliyuncs.com";
    private String TAG = OOSClientUtils.class.getSimpleName();
    private String accessKeyId;
    private String accessKeySecret;
    private Context mContext;
    private AvatarOOS mAvatarOOS;
    private RequestCallback.IUploadAvatarCallBack mCallBack;
    public static  int uploadSeccess = 2;

    // 明文设置secret的方式建议只在测试时使用，更多鉴权模式请参考后面的`访问控制`章节
    OSSCredentialProvider credentialProvider = null;
    OSSStsTokenCredentialProvider provider = null;

    private OSS oss = null;
    private String mPath;

    // 构造上传请求
    PutObjectRequest put = null;

    public OOSClientUtils(Context context, AvatarOOS avatarOOS, String path, RequestCallback.IUploadAvatarCallBack callback) {
        mContext = context;
        this.mAvatarOOS = avatarOOS;
        this.mPath = path;
        this.mCallBack = callback;
        initOOS();
    }

    public void initOOS(){
//        credentialProvider = new OSSPlainTextAKSKCredentialProvider(mAvatarOOS.getAccessKeyId(), mAvatarOOS.getAccessKeySecret());
        Logger.e(TAG,"SecurityToken = "+mAvatarOOS.getSecurityToken());
        Logger.e(TAG,"AccesskeyId = "+mAvatarOOS.getAccessKeyId());
        Logger.e(TAG,"AccesskeykeySecret = "+mAvatarOOS.getAccessKeySecret());
        Logger.e(TAG,"filePath = " + mAvatarOOS.getPath()+"/"+mAvatarOOS.getFilename());
        Logger.e(TAG,"mPath = "+mPath);
        provider = new OSSStsTokenCredentialProvider(mAvatarOOS.getAccessKeyId(),mAvatarOOS.getAccessKeySecret(),mAvatarOOS.getSecurityToken());
        put = new PutObjectRequest(mAvatarOOS.getBucket(),mAvatarOOS.getPath()+"/"+mAvatarOOS.getFilename(), mPath);

        oss = new OSSClient(mContext, endpoint, provider);
    }

    public int uploadFile(){
        // 异步上传时可以设置进度回调
        Logger.e("OOSClientUtils","uploadFile上传开始");
        final int flag = 0;
        put.setProgressCallback(new OSSProgressCallback<PutObjectRequest>() {
            @Override
            public void onProgress(PutObjectRequest request, long currentSize, long totalSize) {
                Logger.e(TAG,"当前进度为"+(currentSize/totalSize)*100);
                mCallBack.uploadProgress((int) (currentSize/totalSize)*100);
            }
        });
        OSSAsyncTask task = oss.asyncPutObject(put, new OSSCompletedCallback<PutObjectRequest, PutObjectResult>() {
            @Override
            public void onSuccess(PutObjectRequest request, PutObjectResult result) {
                Logger.e("PutObject", "UploadSuccess");
                mCallBack.uploadSuccess();
            }

            @Override
            public void onFailure(PutObjectRequest request, ClientException clientExcepion, ServiceException serviceException) {
                // 请求异常
                Logger.e(TAG,"clientException = "+clientExcepion.getMessage());
                if (clientExcepion != null) {
                    // 本地异常如网络异常等
                    clientExcepion.printStackTrace();
                    mCallBack.uploadFail();
                }
                if (serviceException != null) {
                    mCallBack.uploadFail();
                    // 服务异常
                    Log.e("ErrorCode", serviceException.getErrorCode());
                    Log.e("RequestId", serviceException.getRequestId());
                    Log.e("HostId", serviceException.getHostId());
                    Log.e("RawMessage", serviceException.getRawMessage());
                }
            }
        });
        return 0;
    }
// task.cancel(); // 可以取消任务

// task.waitUntilFinished(); // 可以等待直到任务完成
}
