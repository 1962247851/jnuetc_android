package jn.mjz.aiot.jnuetc.viewmodel;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.gson.JsonObject;
import com.xiaomi.mipush.sdk.MiPushClient;
import com.youth.xframe.XFrame;
import com.youth.xframe.utils.http.HttpCallBack;
import com.youth.xframe.utils.http.XHttp;
import com.youth.xframe.utils.log.XLog;
import com.youth.xframe.widget.XToast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jn.mjz.aiot.jnuetc.application.App;
import jn.mjz.aiot.jnuetc.greendao.Dao.DataDao;
import jn.mjz.aiot.jnuetc.greendao.entity.Data;
import jn.mjz.aiot.jnuetc.greendao.entity.MingJu;
import jn.mjz.aiot.jnuetc.greendao.entity.User;
import jn.mjz.aiot.jnuetc.util.GlobalUtil;
import jn.mjz.aiot.jnuetc.util.GsonUtil;
import jn.mjz.aiot.jnuetc.util.HttpUtil;
import jn.mjz.aiot.jnuetc.util.SharedPreferencesUtil;

/**
 * @author 19622
 */
public class MainViewModel extends ViewModel {

    private static final String TAG = "MainViewModel";
    public static DataDao dataDao = App.getDaoSession().getDataDao();
    public static User user = null;

    public static User getUser() {
        if (user == null) {
            String userJson = SharedPreferencesUtil.getSharedPreferences(GlobalUtil.Keys.LoginActivity.FILE_NAME).getString(GlobalUtil.Keys.LoginActivity.USER_JSON_STRING, "needLogin");
            if (!"needLogin".equals(userJson)) {
                user = GsonUtil.getInstance().fromJson(userJson, User.class);
            }
        }
        return user;
    }

    private SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(GlobalUtil.Keys.New.FILE_NAME_DRAWER);

    private MutableLiveData<Boolean> drawerOpen;
    /**
     * 保存当前所在的界面，变化时会刷新sharePreferences
     */
    private MutableLiveData<Integer> currentState;
    /**
     * 暂时存放筛选配置的变量，用于从本地数据库筛选数据,会跟随currentState变化
     */
    private MutableLiveData<Map<String, Boolean>> selectedLocalsN;
    private MutableLiveData<Map<String, Boolean>> selectedLocalsS;
    private MutableLiveData<Boolean> timeOrder;

    /**
     * 暂时存放筛选结果的Data列表，并在变化时通知adapter刷新数据
     */
    private MutableLiveData<List<Data>> dataList1;
    private MutableLiveData<List<Data>> dataList2;
    private MutableLiveData<List<Data>> dataList3;
    private MutableLiveData<List<Data>> dataList4;
    private MutableLiveData<List<Data>> dataList5;

    /**
     * 更新用户信息（更新管理员权限），更新数据库里的报修单数据，更新推送订阅
     *
     * @param callBack 回调
     */
    public void updateUserInfo(HttpUtil.HttpUtilCallBack<User> callBack) {
        Map<String, Object> params = new HashMap<>(2);
        params.put("sno", user.getSno());
        params.put("password", user.getPassword());
        XHttp.obtain().post(GlobalUtil.Urls.User.LOGIN, params, new HttpCallBack<JsonObject>() {
            @Override
            public void onSuccess(JsonObject jsonObject) {
                int error = jsonObject.get("error").getAsInt();
                if (error == 1) {
                    SharedPreferences.Editor editor = SharedPreferencesUtil.getSharedPreferences(GlobalUtil.Keys.LoginActivity.FILE_NAME).edit();
                    editor.putString(GlobalUtil.Keys.LoginActivity.USER_JSON_STRING, jsonObject.get("body").getAsString());
                    editor.apply();
                    User user = GsonUtil.getInstance().fromJson(jsonObject.get("body").getAsString(), User.class);
                    editor = SharedPreferencesUtil.getSharedPreferences(GlobalUtil.Keys.New.FILE_NAME_DRAWER).edit();
                    if (!MainViewModel.user.haveWholeSchoolAccess()) {
                        //升级为管理员
                        if (user.haveWholeSchoolAccess()) {
                            //原来是北区，把所有南区全选
                            if (MainViewModel.user.getWhichGroup() == 0) {
                                MiPushClient.subscribe(XFrame.getContext(), "1", null);
                                MiPushClient.subscribe(XFrame.getContext(), "1", null);
                                MiPushClient.subscribe(XFrame.getContext(), "1", null);
                                for (String local : GlobalUtil.TITLES_S) {
                                    editor.putBoolean(local, true);
                                }
                                //原来是南区，把所有北区全选
                            } else {
                                MiPushClient.subscribe(XFrame.getContext(), "0", null);
                                MiPushClient.subscribe(XFrame.getContext(), "0", null);
                                MiPushClient.subscribe(XFrame.getContext(), "0", null);
                                for (String local : GlobalUtil.TITLES_N) {
                                    editor.putBoolean(local, true);
                                }
                            }
                            editor.apply();
                            if (getCurrentState().getValue() != 0) {

                                queryAll(new HttpUtil.HttpUtilCallBack<List<Data>>() {
                                    @Override
                                    public void onResponse(List<Data> result) {
                                        loadAllSettings(0);
                                        queryDataListBySetting(0);
                                        loadAllSettings(null);
                                    }

                                    @Override
                                    public void onFailure(String message) {

                                    }
                                });
                            }
                        }
                    } else {
                        //降级
                        if (!user.haveWholeSchoolAccess()) {
                            //原来是北区，把所有南区取消选中
                            if (MainViewModel.user.getWhichGroup() == 0) {
                                MiPushClient.unsubscribe(XFrame.getContext(), "1", null);
                                MiPushClient.unsubscribe(XFrame.getContext(), "1", null);
                                MiPushClient.unsubscribe(XFrame.getContext(), "1", null);
                                for (String local : GlobalUtil.TITLES_S) {
                                    editor.putBoolean(local, false);
                                }
                                //原来是南区，把所有北区取消选中
                            } else {
                                MiPushClient.unsubscribe(XFrame.getContext(), "0", null);
                                MiPushClient.unsubscribe(XFrame.getContext(), "0", null);
                                MiPushClient.unsubscribe(XFrame.getContext(), "0", null);
                                for (String local : GlobalUtil.TITLES_N) {
                                    editor.putBoolean(local, false);
                                }
                            }
                            //依旧是管理员
                        } else {
                            MiPushClient.subscribe(XFrame.getContext(), "0", null);
                            MiPushClient.subscribe(XFrame.getContext(), "0", null);
                            MiPushClient.subscribe(XFrame.getContext(), "0", null);
                            MiPushClient.subscribe(XFrame.getContext(), "1", null);
                            MiPushClient.subscribe(XFrame.getContext(), "1", null);
                            MiPushClient.subscribe(XFrame.getContext(), "1", null);
                        }
                        editor.apply();
                        if (getCurrentState().getValue() != 0) {
                            loadAllSettings(0);
                            queryDataListBySetting(0);
                            loadAllSettings(null);
                        }
                    }
                    MainViewModel.user = user;
                    callBack.onResponse(user);
                } else {
                    SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(GlobalUtil.Keys.LoginActivity.FILE_NAME);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(GlobalUtil.Keys.LoginActivity.USER_JSON_STRING, "needLogin");
                    editor.putBoolean(GlobalUtil.Keys.LoginActivity.REMEMBER_PASSWORD, false);
                    editor.apply();
                    callBack.onFailure(null);
                }
            }

            @Override
            public void onFailed(String error) {
                callBack.onFailure(error);
            }
        });
    }

    /**
     * 查询所有用户名字，不排除自己的名字
     *
     * @param callBack 回调
     */
    public static void queryAllUser(HttpUtil.HttpUtilCallBack<List<String>> callBack) {
        XHttp.obtain().post(GlobalUtil.Urls.User.QUERY_ALL, null, new HttpCallBack<JsonObject>() {
            @Override
            public void onSuccess(JsonObject jsonObject) {
                List<User> resultList = GsonUtil.parseJsonArray2ObjectList(jsonObject.get("body").getAsString(), User.class);
                List<String> userNames = new ArrayList<>();
                for (int i = 0; i < resultList.size(); i++) {
                    userNames.add(resultList.get(i).getUserName());
                }
                callBack.onResponse(userNames);
            }

            @Override
            public void onFailed(String error) {
                callBack.onFailure(error);
            }
        });
    }

    /**
     * 从服务器获取最新保修单数据，并且保存到本地数据库（先删除所有再插入所有）
     *
     * @param callBack 回调
     */
    public static void queryAll(HttpUtil.HttpUtilCallBack<List<Data>> callBack) {
        XHttp.obtain().post(GlobalUtil.Urls.Data.QUERY_ALL, null, new HttpCallBack<JsonObject>() {
            @Override
            public void onSuccess(JsonObject jsonObject) {
                if (jsonObject.get("error").getAsInt() == 1) {

                    XLog.json(jsonObject.get("body").getAsString());

                    List<Data> resultList = GsonUtil.parseJsonArray2ObjectList(jsonObject.get("body").getAsString(), Data.class);
                    dataDao.deleteAll();
                    dataDao.insertInTx(resultList);
                    if (user.getRootLevel() == 0) {
                        List<Data> needToDelete = dataDao.queryBuilder().where(
                                DataDao.Properties.District.notEq(user.getWhichGroup()),
                                DataDao.Properties.State.eq(0)
                        ).build().list();
                        dataDao.deleteInTx(needToDelete);
                    }
                    callBack.onResponse(resultList);
                } else {
                    callBack.onFailure(null);
                }
            }

            @Override
            public void onFailed(String error) {
                callBack.onFailure(error);
            }
        });
    }

    /**
     * 通过id查询报修单
     *
     * @param id       id值
     * @param callBack 回调
     */
    public static void queryById(String id, HttpUtil.HttpUtilCallBack<Data> callBack) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        XHttp.obtain().post(GlobalUtil.Urls.Data.QUERY_BY_ID, params, new HttpCallBack<JsonObject>() {
            @Override
            public void onSuccess(JsonObject jsonObject) {
                int error = jsonObject.get("error").getAsInt();
                if (error == 1) {
                    callBack.onResponse(GsonUtil.getInstance().fromJson(jsonObject.get("body").getAsString(), Data.class));
                } else if (error == 0) {
                    callBack.onFailure(jsonObject.get("body").getAsString());
                }
            }

            @Override
            public void onFailed(String error) {
                callBack.onFailure(error);
            }
        });
    }

    /**
     * 修改报修单的所有信息
     *
     * @param newData  修改后的报修单
     * @param oldData  修改前的报修单
     * @param callBack 回调,会返回更新后的data
     */
    public void modify(Data newData, String oldData, HttpUtil.HttpUtilCallBack<Data> callBack) {
        Map<String, Object> params = new HashMap<>(3);
        params.put("dataJson", newData);
        params.put("oldDataJson", oldData);
        params.put("name", user.getUserName());
        XHttp.obtain().post(GlobalUtil.Urls.Data.UPDATE, params, new HttpCallBack<JsonObject>() {
            @Override
            public void onSuccess(JsonObject jsonObject) {
                int error = jsonObject.get("error").getAsInt();
                if (error == 1) {
                    Data data1 = GsonUtil.getInstance().fromJson(jsonObject.get("body").getAsString(), Data.class);
                    dataDao.update(data1);
                    callBack.onResponse(data1);
                } else if (error == 0) {
                    callBack.onFailure(jsonObject.get("msg").getAsString());
                }
            }

            @Override
            public void onFailed(String error) {
                callBack.onFailure(error);
            }
        });
    }

    /**
     * 手动创建一个新的报修单
     * todo 修改requestParam
     *
     * @param data     报修单
     * @param callBack 回调,返回插入后的data
     */
    @Deprecated
    public void insert(Data data, HttpUtil.HttpUtilCallBack<Data> callBack) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("data", data);
        XHttp.obtain().post(GlobalUtil.Urls.Data.INSERT, params, new HttpCallBack<JsonObject>() {
            @Override
            public void onSuccess(JsonObject jsonObject) {

            }

            @Override
            public void onFailed(String error) {

            }
        });
    }

    /**
     * 批量删除报修单
     *
     * @param ids      要删除的id列表
     * @param callBack 回调
     */
    public void deleteMany(List<Integer> ids, HttpUtil.HttpUtilCallBack<Boolean> callBack) {
        Map<String, Object> params = new HashMap<>(2);
        params.put("idListJson", GsonUtil.getInstance().toJson(ids));
        params.put("userJson", MainViewModel.user.toString());
        XHttp.obtain().post(GlobalUtil.Urls.Data.DELETE_BY_ID_LIST, params, new HttpCallBack<JsonObject>() {
            @Override
            public void onSuccess(JsonObject jsonObject) {
                int error = jsonObject.get("error").getAsInt();
                if (error == 1) {
                    List<Long> longs = new ArrayList<>();
                    for (Integer integer : ids) {
                        longs.add(Long.valueOf(integer));
                    }
                    dataDao.deleteByKeyInTx(longs);
                    callBack.onResponse(true);
                } else {
                    callBack.onResponse(false);
                    XToast.error(jsonObject.get("msg").getAsString());
                }
            }

            @Override
            public void onFailed(String error) {
                callBack.onFailure(error);
            }
        });
    }

    /**
     * 查询当前报修服务状态
     *
     * @param callBack 回调,返回状态
     */
    public void checkServerState(HttpUtil.HttpUtilCallBack<Boolean> callBack) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("type", "repair");
        XHttp.obtain().post(GlobalUtil.Urls.State.CHECK_SERVICE, params, new HttpCallBack<JsonObject>() {
            @Override
            public void onSuccess(JsonObject jsonObject) {
                int error = jsonObject.get("error").getAsInt();
                if (error == 1) {
                    callBack.onResponse(jsonObject.get("body").getAsBoolean());
                }
            }

            @Override
            public void onFailed(String error) {
                callBack.onFailure(error);
            }
        });
    }

    /**
     * 查询每日图片服务状态
     *
     * @param callBack 回调,返回状态
     */
    public void checkDayDayPhotoState(HttpUtil.HttpUtilCallBack<Boolean> callBack) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("type", "dayDP");
        XHttp.obtain().post(GlobalUtil.Urls.State.CHECK_SERVICE, params, new HttpCallBack<JsonObject>() {
            @Override
            public void onSuccess(JsonObject jsonObject) {
                int error = jsonObject.get("error").getAsInt();
                if (error == 1) {
                    callBack.onResponse(jsonObject.get("body").getAsBoolean());
                }
            }

            @Override
            public void onFailed(String error) {
                callBack.onFailure(error);
            }
        });
    }

    /**
     * 查询注册服务状态
     *
     * @param callBack 回调,返回状态
     */
    public void checkRegisterState(HttpUtil.HttpUtilCallBack<Boolean> callBack) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("type", "register");
        XHttp.obtain().post(GlobalUtil.Urls.State.CHECK_SERVICE, params, new HttpCallBack<JsonObject>() {
            @Override
            public void onSuccess(JsonObject jsonObject) {
                int error = jsonObject.get("error").getAsInt();
                if (error == 1) {
                    callBack.onResponse(jsonObject.get("body").getAsBoolean());
                }
            }

            @Override
            public void onFailed(String error) {
                callBack.onFailure(error);
            }
        });
    }

    /**
     * 开启报修服务
     *
     * @param callBack 回调,返回操作是否成功
     */
    public void openService(HttpUtil.HttpUtilCallBack<Boolean> callBack) {
        Map<String, Object> params = new HashMap<>(2);
        params.put("type", "repair");
        params.put("available", true);
        XHttp.obtain().post(GlobalUtil.Urls.State.CHANGE_SERVICE, params, new HttpCallBack<JsonObject>() {
            @Override
            public void onSuccess(JsonObject jsonObject) {
                int error = jsonObject.get("error").getAsInt();
                if (error == 1) {
                    callBack.onResponse(true);
                } else {
                    callBack.onResponse(false);
                }
            }

            @Override
            public void onFailed(String error) {
                callBack.onFailure(error);
            }
        });
    }

    /**
     * 关闭报修服务
     *
     * @param callBack 回调,返回操作是否成功
     */
    public void closeService(HttpUtil.HttpUtilCallBack<Boolean> callBack) {
        Map<String, Object> params = new HashMap<>(2);
        params.put("type", "repair");
        params.put("available", false);
        XHttp.obtain().post(GlobalUtil.Urls.State.CHANGE_SERVICE, params, new HttpCallBack<JsonObject>() {
            @Override
            public void onSuccess(JsonObject jsonObject) {
                int error = jsonObject.get("error").getAsInt();
                if (error == 1) {
                    callBack.onResponse(true);
                } else {
                    callBack.onResponse(false);
                }
            }

            @Override
            public void onFailed(String error) {
                callBack.onFailure(error);
            }
        });
    }

    /**
     * 开启每日一图服务
     *
     * @param callBack 回调,返回操作是否成功
     */
    public void openDayDayPhotoService(HttpUtil.HttpUtilCallBack<Boolean> callBack) {
        Map<String, Object> params = new HashMap<>(2);
        params.put("type", "dayDP");
        params.put("available", true);
        XHttp.obtain().post(GlobalUtil.Urls.State.CHANGE_SERVICE, params, new HttpCallBack<JsonObject>() {
            @Override
            public void onSuccess(JsonObject jsonObject) {
                int error = jsonObject.get("error").getAsInt();
                if (error == 1) {
                    callBack.onResponse(true);
                } else {
                    callBack.onResponse(false);
                }
            }

            @Override
            public void onFailed(String error) {
                callBack.onFailure(error);
            }
        });
    }

    /**
     * 关闭每日一图服务
     *
     * @param callBack 回调,返回操作是否成功
     */
    public void closeDayDayPhotoService(HttpUtil.HttpUtilCallBack<Boolean> callBack) {
        Map<String, Object> params = new HashMap<>(2);
        params.put("type", "dayDP");
        params.put("available", false);
        XHttp.obtain().post(GlobalUtil.Urls.State.CHANGE_SERVICE, params, new HttpCallBack<JsonObject>() {
            @Override
            public void onSuccess(JsonObject jsonObject) {
                int error = jsonObject.get("error").getAsInt();
                if (error == 1) {
                    callBack.onResponse(true);
                } else {
                    callBack.onResponse(false);
                }
            }

            @Override
            public void onFailed(String error) {
                callBack.onFailure(error);
            }
        });
    }

    /**
     * 开启注册服务
     *
     * @param callBack 回调,返回操作是否成功
     */
    public void openRegisterService(HttpUtil.HttpUtilCallBack<Boolean> callBack) {
        Map<String, Object> params = new HashMap<>(2);
        params.put("type", "register");
        params.put("available", true);
        XHttp.obtain().post(GlobalUtil.Urls.State.CHANGE_SERVICE, params, new HttpCallBack<JsonObject>() {
            @Override
            public void onSuccess(JsonObject jsonObject) {
                int error = jsonObject.get("error").getAsInt();
                if (error == 1) {
                    callBack.onResponse(true);
                } else {
                    callBack.onResponse(false);
                }
            }

            @Override
            public void onFailed(String error) {
                callBack.onFailure(error);
            }
        });
    }

    /**
     * 关闭注册服务
     *
     * @param callBack 回调,返回操作是否成功
     */
    public void closeRegisterService(HttpUtil.HttpUtilCallBack<Boolean> callBack) {
        Map<String, Object> params = new HashMap<>(2);
        params.put("type", "register");
        params.put("available", false);
        XHttp.obtain().post(GlobalUtil.Urls.State.CHANGE_SERVICE, params, new HttpCallBack<JsonObject>() {
            @Override
            public void onSuccess(JsonObject jsonObject) {
                int error = jsonObject.get("error").getAsInt();
                if (error == 1) {
                    callBack.onResponse(true);
                } else {
                    callBack.onResponse(false);
                }
            }

            @Override
            public void onFailed(String error) {
                callBack.onFailure(error);
            }
        });
    }

    /**
     * 根据当前所在的界面拿对应的SharedPreference
     *
     * @return 返回SharedPreference
     */
    private SharedPreferences getSharedPreferences() {
        int current = getCurrentState().getValue() == null ? 0 : getCurrentState().getValue();
        switch (current) {
            case 0:
                sharedPreferences = SharedPreferencesUtil.getSharedPreferences(GlobalUtil.Keys.New.FILE_NAME_DRAWER);
                break;
            case 1:
                sharedPreferences = SharedPreferencesUtil.getSharedPreferences(GlobalUtil.Keys.Processing.FILE_NAME_DRAWER);
                break;
            case 2:
                sharedPreferences = SharedPreferencesUtil.getSharedPreferences(GlobalUtil.Keys.Done.FILE_NAME_DRAWER);
                break;
        }
        return sharedPreferences;
    }

    /**
     * 当drawer关闭的时候调用保存所有设置
     */
    public void saveAllSettings() {
        Map<String, Boolean> map;
        Boolean b = getTimeOrder().getValue();
        SharedPreferences.Editor editor = getSharedPreferences().edit();

        if (b != null) {
            editor.putBoolean("time", b);
        }

        map = getSelectedLocalsN().getValue();
        if (map != null && editor != null) {
            for (int i = 0; i < map.size(); i++) {
                String local = GlobalUtil.TITLES_N[i];
                editor.putBoolean(local, map.get(local));
            }
            editor.apply();
        }

        map = getSelectedLocalsS().getValue();
        if (map != null && editor != null) {
            for (int i = 0; i < map.size(); i++) {
                String local = GlobalUtil.TITLES_S[i];
                editor.putBoolean(local, map.get(local));
            }
            editor.apply();
        }
    }

    /**
     * 加载当前drawer的所有设置
     */
    public void loadAllSettings(@Nullable Integer state) {

        Map<String, Boolean> mapN = getSelectedLocalsN().getValue();
        Map<String, Boolean> mapS = getSelectedLocalsS().getValue();
        SharedPreferences sharedPreferences = null;
        state = state == null ? getCurrentState().getValue() : state;
        switch (state) {
            case 0:
                sharedPreferences = SharedPreferencesUtil.getSharedPreferences(GlobalUtil.Keys.New.FILE_NAME_DRAWER);
                break;
            case 1:
                sharedPreferences = SharedPreferencesUtil.getSharedPreferences(GlobalUtil.Keys.Processing.FILE_NAME_DRAWER);
                break;
            case 2:
                sharedPreferences = SharedPreferencesUtil.getSharedPreferences(GlobalUtil.Keys.Done.FILE_NAME_DRAWER);
                break;
        }

        if (mapN != null && mapS != null && sharedPreferences != null) {

            getTimeOrder().setValue(sharedPreferences.getBoolean("time", false));

            for (int i = 0; i < 8; i++) {
                String localN = GlobalUtil.TITLES_N[i];
                mapN.put(localN, sharedPreferences.getBoolean(localN, state != 0 || user.getRootLevel() != 0 || user.getWhichGroup() == 0));
                String localS = GlobalUtil.TITLES_S[i];
                mapS.put(localS, sharedPreferences.getBoolean(localS, state != 0 || user.getRootLevel() != 0 || user.getWhichGroup() == 1));
            }

        }
    }

    /**
     * 全选或取消全选某个园区
     *
     * @param select 是否选中
     * @param map    要修改的map
     */
    public void selectOrCancelAll(boolean select, Map<String, Boolean> map) {

        for (String key : map.keySet()) {
            map.put(key, select);
        }

    }

    /**
     * 判断是否全选了北区或南区
     *
     * @param map 要判断的map
     * @return 布尔类型
     */
    public boolean isSelectAll(Map<String, Boolean> map) {
        boolean selectAll = true;
        if (map != null) {
            for (String key : map.keySet()) {
                if (!map.get(key)) {
                    selectAll = false;
                    break;
                }
            }
        }
        return selectAll;
    }

    /**
     * 根据设置和state更新显示的dataList
     *
     * @param state 要查询哪个界面的
     */
    public void queryDataListBySetting(@Nullable Integer state) {
        state = state == null ? getCurrentState().getValue() : state;
        Map<String, Boolean> mapN = getSelectedLocalsN().getValue();
        Map<String, Boolean> mapS = getSelectedLocalsS().getValue();

        List<String> selectedLocals = new ArrayList<>();
        if (mapN != null && mapS != null) {
            for (int i = 0; i < 8; i++) {
                String localN = GlobalUtil.TITLES_N[i];
                if (mapN.get(localN)) {
                    selectedLocals.add(localN);
                }
                String localS = GlobalUtil.TITLES_S[i];
                if (mapS.get(localS)) {
                    selectedLocals.add(localS);
                }
            }
        }

//            Log.d(TAG, "queryDataListBySetting: getCurrentState() = " + getCurrentState().getValue());
//            Log.d(TAG, "queryDataListBySetting: " + selectedLocals);
//            Log.d(TAG, "queryDataListBySetting: " + getTimeOrder().getValue());
        List<Data> selectedDatas;
        if (state == 2) {
            selectedDatas = dataDao.queryBuilder()
                    .where(DataDao.Properties.Local.in(selectedLocals)
                            , DataDao.Properties.State.eq(2))
                    .orderCustom(DataDao.Properties.RepairDate, getTimeOrder().getValue() != null && getTimeOrder().getValue() ? "asc" : "desc")
                    .build()
                    .list();
            List<Data> dataList = new ArrayList<>(selectedDatas);
            for (Data d : selectedDatas) {
                //不需要的
                if (d.getRepairer().contains(user.getUserName())) {
                    dataList.remove(d);
                }
            }
            selectedDatas.clear();
            selectedDatas.addAll(dataList);
        } else if (state == 1) {
            selectedDatas = dataDao.queryBuilder()
                    .where(DataDao.Properties.Local.in(selectedLocals)
                            , DataDao.Properties.State.eq(1)
                            , DataDao.Properties.Repairer.notEq(user.getUserName()))
                    .orderCustom(DataDao.Properties.Date, getTimeOrder().getValue() != null && getTimeOrder().getValue() ? "asc" : "desc")
                    .build()
                    .list();
        } else {
            selectedDatas = dataDao.queryBuilder()
                    .where(DataDao.Properties.Local.in(selectedLocals)
                            , DataDao.Properties.State.eq(0))
                    .orderCustom(DataDao.Properties.Date, getTimeOrder().getValue() != null && getTimeOrder().getValue() ? "asc" : "desc")
                    .build()
                    .list();
        }

//            Log.d(TAG, "queryDataListBySetting: " + selectedDatas);
        switch (state) {
            case 0:
                getDataList1().setValue(selectedDatas);
                break;
            case 1:
                getDataList2().setValue(selectedDatas);
                break;
            case 2:
                getDataList3().setValue(selectedDatas);
                break;
        }
    }

    /**
     * 查询与我有关的报修单（注意GreenDao like的用法）
     *
     * @param state 要查询的状态
     */
    public void queryDataListAboutMyself(@NonNull Integer state) {
        List<Data> dataList;
        if (state == 1) {
            dataList = dataDao.queryBuilder().where(
                    DataDao.Properties.State.eq(1)
                    , DataDao.Properties.Repairer.like("%" + user.getUserName() + "%"))
                    .orderAsc(DataDao.Properties.Date)
                    .build().list();
            getDataList4().setValue(dataList);
        } else if (state == 2) {
            dataList = dataDao.queryBuilder().where(
                    DataDao.Properties.State.eq(2)
                    , DataDao.Properties.Repairer.like("%" + user.getUserName() + "%"))
                    .orderDesc(DataDao.Properties.RepairDate)
                    .build().list();
            getDataList5().setValue(dataList);
        }
    }

    /**
     * 获取名句
     *
     * @param callBack 回调
     */
    public static void getMingJu(HttpUtil.HttpUtilCallBack<MingJu> callBack) {
        HashMap<String, Object> params = new HashMap<>(2);
        String topic = SharedPreferencesUtil.getSettingPreferences().getString("ming_ju_topic", "人生");
        params.put("topic", topic);
        params.put("type", "");
        XHttp.obtain().post(GlobalUtil.Urls.MingJu.GET_MING_JU, params, new HttpCallBack<JsonObject>() {
            @Override
            public void onSuccess(JsonObject jsonObject) {
                int error = jsonObject.get("error").getAsInt();
                if (error == 1) {
                    callBack.onResponse(GsonUtil.getInstance().fromJson(jsonObject.get("body").getAsString(), MingJu.class));
                } else {
                    callBack.onFailure(jsonObject.get("msg").getAsString());
                }
            }

            @Override
            public void onFailed(String error) {
                callBack.onFailure(error);
            }
        });
    }

    public static void copyToClipboard(Context context, String text) {
        ClipboardManager systemService = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        systemService.setPrimaryClip(ClipData.newPlainText("text", text));
    }

    public MutableLiveData<Boolean> getTimeOrder() {
        if (timeOrder == null) {
            timeOrder = new MutableLiveData<>();
            timeOrder.setValue(true);
        }
        return timeOrder;
    }

    public MutableLiveData<Map<String, Boolean>> getSelectedLocalsN() {
        if (selectedLocalsN == null) {
            selectedLocalsN = new MutableLiveData<>();
            Map<String, Boolean> map = new HashMap<>();
            for (String s : GlobalUtil.TITLES_N) {
                map.put(s, user.getRootLevel() != 0 || user.getWhichGroup() == 0);
            }
            selectedLocalsN.setValue(map);
        }
        return selectedLocalsN;
    }

    public MutableLiveData<Map<String, Boolean>> getSelectedLocalsS() {
        if (selectedLocalsS == null) {
            selectedLocalsS = new MutableLiveData<>();
            Map<String, Boolean> map = new HashMap<>();
            for (String s : GlobalUtil.TITLES_S) {
                map.put(s, user.getRootLevel() != 0 || user.getWhichGroup() == 1);
            }
            selectedLocalsS.setValue(map);

        }
        return selectedLocalsS;
    }

    public MutableLiveData<Integer> getCurrentState() {
        if (currentState == null) {
            currentState = new MutableLiveData<>();
            currentState.setValue(0);
        }
        return currentState;
    }

    public MutableLiveData<Boolean> getDrawerOpen() {
        if (drawerOpen == null) {
            drawerOpen = new MutableLiveData<>();
            drawerOpen.setValue(false);
        }
        return drawerOpen;
    }

    public MutableLiveData<List<Data>> getDataList1() {
        if (dataList1 == null) {
            dataList1 = new MutableLiveData<>();
            dataList1.setValue(new ArrayList<>());
        }
        return dataList1;
    }

    public MutableLiveData<List<Data>> getDataList2() {
        if (dataList2 == null) {
            dataList2 = new MutableLiveData<>();
            dataList2.setValue(new ArrayList<>());
        }
        return dataList2;
    }

    public MutableLiveData<List<Data>> getDataList3() {
        if (dataList3 == null) {
            dataList3 = new MutableLiveData<>();
            dataList3.setValue(new ArrayList<>());
        }
        return dataList3;
    }

    public MutableLiveData<List<Data>> getDataList4() {
        if (dataList4 == null) {
            dataList4 = new MutableLiveData<>();
            dataList4.setValue(new ArrayList<>());
        }
        return dataList4;
    }

    public MutableLiveData<List<Data>> getDataList5() {
        if (dataList5 == null) {
            dataList5 = new MutableLiveData<>();
            dataList5.setValue(new ArrayList<>());
        }
        return dataList5;
    }

}
