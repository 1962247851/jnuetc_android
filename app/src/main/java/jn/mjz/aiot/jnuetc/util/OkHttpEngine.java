package jn.mjz.aiot.jnuetc.util;

import androidx.annotation.NonNull;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.youth.xframe.XFrame;
import com.youth.xframe.utils.http.HttpCallBack;
import com.youth.xframe.utils.http.IHttpEngine;
import com.youth.xframe.utils.http.XHttp;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static okhttp3.internal.Util.EMPTY_REQUEST;

/**
 * @author 19622
 */
public class OkHttpEngine implements IHttpEngine {

    private OkHttpClient client;
    private static final int CACHE_SIZE = 10 * 1024 * 1024;

    public OkHttpEngine() {
        client = new OkHttpClient().newBuilder()
                .retryOnConnectionFailure(true)
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .cache(new Cache(XFrame.getContext().getCacheDir(), CACHE_SIZE))
                .build();
    }

    @Override
    public void get(String url, Map<String, Object> params, HttpCallBack callBack) {
        Request request = new Request.Builder().url(url + getUrlParamsByMap(params)).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                XHttp.handler.post(() -> callBack.onFailed(e.toString()));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                ResponseBody body = response.body();
                if (response.isSuccessful() && body != null) {
                    final String result = body.string();
                    XHttp.handler.post(() -> callBack.onSuccess(GsonUtil.getInstance().fromJson(result, JsonObject.class)));
                } else {
                    XHttp.handler.post(() -> callBack.onFailed(response.message()));
                }
            }
        });
    }

    @Override
    public void post(String url, Map<String, Object> params, HttpCallBack callBack) {
        RequestBody body = EMPTY_REQUEST;
        if (null != params && !params.isEmpty()) {
            FormBody.Builder builder = new FormBody.Builder();
            for (String key : params.keySet()) {
                Object o = params.get(key);
                if (o != null) {
                    builder.add(key, o.toString());
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
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                XHttp.handler.post(() -> callBack.onFailed(e.toString()));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                ResponseBody body = response.body();
                if (response.isSuccessful() && body != null) {
                    final String result = body.string();
                    XHttp.handler.post(() -> {
                        try {
                            callBack.onSuccess(GsonUtil.getInstance().fromJson(result, JsonObject.class));
                        } catch (JsonSyntaxException e) {
                            XHttp.handler.post(() -> callBack.onFailed("服务端响应格式错误"));
                        }
                    });
                } else {
                    XHttp.handler.post(() -> callBack.onFailed(response.message()));
                }
            }
        });
    }

    private String getUrlParamsByMap(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return "";
        }
        StringBuilder params = new StringBuilder("?");
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            params.append(entry.getKey());
            params.append("=");
            params.append(entry.getValue());
            params.append("&");
        }
        String str = params.toString();
        return str.substring(0, str.length() - 1);
    }
}
