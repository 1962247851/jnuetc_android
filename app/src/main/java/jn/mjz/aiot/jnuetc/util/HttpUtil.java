package jn.mjz.aiot.jnuetc.util;


import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.youth.xframe.XFrame;
import com.youth.xframe.utils.http.XHttp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static okhttp3.internal.Util.EMPTY_REQUEST;

public class HttpUtil {
    private static final int cacheSize = 10 * 1024 * 1024;
    private static OkHttpClient client;

    public static class post {

        private static final String TAG = "HttpUtil";

        public static void haveResponse(String url, Map<String, Object> params, HttpUtilCallBack<String> callback) {
            initClient();

            RequestBody body = EMPTY_REQUEST;
            if (null != params && !params.isEmpty()) {
                FormBody.Builder builder = new FormBody.Builder();
                for (String key : params.keySet()) {
                    Object value = params.get(key);
                    builder.add(key, value.toString());
                }
                body = builder.build();
            }

            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    XHttp.handler.post(() -> callback.onFailure(e));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String result = response.body().string();
                        XHttp.handler.post(() -> callback.onResponse(response, result));
                    } else {
                        XHttp.handler.post(() -> callback.onResponse(response, null));
                    }
                }

            });
        }

        public static void uploadHaveResponse(String url, Map<String, Object> params, HttpUtilCallBack callBack) {
            initClient();
            RequestBody body = EMPTY_REQUEST;
            if (null != params && !params.isEmpty()) {
                MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
                for (String key : params.keySet()) {
                    Object value = params.get(key);
                    if (value instanceof File) {
                        File file = (File) value;
                        Log.e(TAG, "uploadHaveResponse: "+file.length() );
                        builder.addFormDataPart(key, file.getName(),
                                RequestBody.create(MediaType.parse("application/octet-stream"),file));//"application/octet-stream"
                    } else {
                        builder.addFormDataPart(key, value.toString());
                    }
                }
                body = builder.build();
            }

            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    XHttp.handler.post(() -> callBack.onFailure(e));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String result = response.body().string();
                        XHttp.handler.post(() -> callBack.onResponse(response, result));
                    }
                }
            });
        }
    }

    public static class get {
        public static void haveResponse(String url, Map<String, Object> params, HttpUtilCallBack<String> callback) {
            initClient();

            Request request = new Request.Builder().url(url + getUrlParamsByMap(params)).build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onFailure(e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String result = response.body().string();
                        XHttp.handler.post(() -> callback.onResponse(response, result));
                    }

                }
            });
        }

        public static void downloadFile(AppCompatActivity activity, Uri uri, String url, Map<String, Object> params, IFileDownloadListener fileDownloadListener) {
            initClient();
            Request request = new Request.Builder().url(url + getUrlParamsByMap(params)).build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    fileDownloadListener.onError(e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) {
                    if (response.isSuccessful()) {
                        fileDownloadListener.onStart();
                        InputStream is = null;
                        byte[] buf = new byte[2048];
                        int len;
                        FileOutputStream fos = null;
                        //储存下载文件
                        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                            Log.e("onResponse: ", "Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)");
                        }
//                                File file = new File(activity.getExternalFilesDir(null), fileName);
//                                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);//Q用不了
                        try {
                            ParcelFileDescriptor pfd = activity.getContentResolver().
                                    openFileDescriptor(uri, "w");
                            fos = new FileOutputStream(pfd.getFileDescriptor());
//                            if (file.exists()) {
//                                file.delete();
//                            } else {
//                                file.createNewFile();
//                            }
                            is = response.body().byteStream();
                            long total = response.body().contentLength();
                            Log.e("onResponse:", " contentLength = " + total);
//                            fos = new FileOutputStream(file);
                            long sum = 0;
                            while ((len = is.read(buf)) != -1) {
                                fos.write(buf, 0, len);
                                sum += len;
                                int progress = (int) (sum * 1.0 / total);
                                fileDownloadListener.onDownloading(progress);
                            }
                            fos.flush();
                            //下载完成
                            XHttp.handler.post(() -> fileDownloadListener.onFinish());
                        } catch (Exception e) {
                            fileDownloadListener.onError(e.getMessage());
                        } finally {
                            try {
                                if (is != null) {
                                    is.close();
                                }
                                if (fos != null) {
                                    fos.close();
                                }
                            } catch (IOException e) {
                                fileDownloadListener.onError(e.getMessage());
                            }
                        }
                    }
                }
            });


        }

    }

    private static void initClient() {
        if (client == null) {
            client = new OkHttpClient().newBuilder()
                    .retryOnConnectionFailure(true)
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .cache(new Cache(XFrame.getContext().getCacheDir(), cacheSize))
                    .build();
        }
    }

    private static String getUrlParamsByMap(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return "";
        }
        StringBuffer params = new StringBuffer("?");
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            params.append(entry.getKey());
            params.append("=");
            params.append(entry.getValue());
            params.append("&");
        }
        String str = params.toString();
        return str.substring(0, str.length() - 1);
    }

    public interface HttpUtilCallBack<T> {
        void onResponse(Response response, T result);

        void onFailure(IOException e);
    }

    public interface IFileDownloadListener {
        void onStart();

        void onDownloading(int progress);

        void onFinish();

        void onError(String errorMessage);
    }
}