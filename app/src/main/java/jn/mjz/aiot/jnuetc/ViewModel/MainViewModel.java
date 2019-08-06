package jn.mjz.aiot.jnuetc.ViewModel;

import android.content.SharedPreferences;
import android.util.Log;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.greenrobot.greendao.query.WhereCondition;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jn.mjz.aiot.jnuetc.Application.MyApplication;
import jn.mjz.aiot.jnuetc.Greendao.Dao.DataDao;
import jn.mjz.aiot.jnuetc.Greendao.Entity.Data;
import jn.mjz.aiot.jnuetc.R;
import jn.mjz.aiot.jnuetc.Util.GlobalUtil;
import jn.mjz.aiot.jnuetc.Util.GsonUtil;
import jn.mjz.aiot.jnuetc.Util.HttpUtil;
import jn.mjz.aiot.jnuetc.Util.SharedPreferencesUtil;
import jn.mjz.aiot.jnuetc.View.Activity.MainActivity;
import okhttp3.Response;

public class MainViewModel extends ViewModel {

    private static final String TAG = "MainViewModel";
    public DataDao dataDao = MyApplication.getDaoSession().getDataDao();
    private SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(GlobalUtil.KEYS.NEW.FILE_NAME_DRAWER);


    //保存当前所在的界面，变化时会刷新sharePreferences
    private MutableLiveData<Integer> currentState;
    //暂时存放筛选配置的变量，用于从本地数据库筛选数据,会跟随currentState变化
    private MutableLiveData<Map<String, Boolean>> selectedLocalsN;
    private MutableLiveData<Map<String, Boolean>> selectedLocalsS;
    private MutableLiveData<Boolean> timeOrder;


    //暂时存放筛选结果的Data列表，并在变化时通知adapter刷新数据
    private MutableLiveData<List<Data>> dataListAll;
    private MutableLiveData<List<Data>> dataList1;
    private MutableLiveData<List<Data>> dataList2;
    private MutableLiveData<List<Data>> dataList3;
    private MutableLiveData<List<Data>> dataList4;
    private MutableLiveData<List<Data>> dataList5;


    /**
     * 从服务器获取最新保修单数据，并且赋值给{@link MainViewModel#dataListAll}
     */
    public void queryAll(HttpUtil.HttpUtilCallBack<List<Data>> callBack) {
        HttpUtil.post.haveResponse(GlobalUtil.URLS.QUERY.ALL, null, new HttpUtil.HttpUtilCallBack<String>() {

            @Override
            public void onResponse(Response response, String result) {
                if (result != null) {
                    List<Data> resultList = GsonUtil.parseJsonArray2ObejctList(result, Data.class);
                    getDataListAll().setValue(resultList);
                    dataDao.deleteAll();
                    dataDao.insertInTx(resultList);
                    callBack.onResponse(response, resultList);
                } else {
                    callBack.onResponse(response, null);
                }
            }

            @Override
            public void onFailure(IOException e) {
                callBack.onFailure(e);
            }

        });
    }

    /**
     * 通过id查询报修单
     *
     * @param id       id值
     * @param callBack 回调
     */
    public void queryById(String id, HttpUtil.HttpUtilCallBack<Data> callBack) {
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        HttpUtil.post.haveResponse(GlobalUtil.URLS.QUERY.BY_ID, params, new HttpUtil.HttpUtilCallBack<String>() {
            @Override
            public void onResponse(Response response, @Nullable String result) {
                String s = response.headers().get("state");
                if (s != null && s.equals("OK") && result != null && !result.equals("{}")) {
                    callBack.onResponse(response, GsonUtil.getInstance().fromJson(result, Data.class));
                } else {
                    callBack.onFailure(null);
                }
            }

            @Override
            public void onFailure(IOException e) {
                callBack.onFailure(e);
            }

        });
    }

    /**
     * 反馈或者接单或者转让
     *
     * @param data     报修单
     * @param callBack 回调,会返回更新后的data
     */
    public void feedback(Data data, HttpUtil.HttpUtilCallBack<Data> callBack) {
        Map<String, Object> params = new HashMap<>();
        params.put("data", data);
        HttpUtil.post.haveResponse(GlobalUtil.URLS.UPDATE.FEEDBACK, params, new HttpUtil.HttpUtilCallBack<String>() {
            @Override
            public void onResponse(Response response, String result) {
                String s = response.header("state");
                if (s != null && s.equals("OK") && result != null && !result.equals("{}")) {
                    callBack.onResponse(response, GsonUtil.getInstance().fromJson(result, Data.class));
                } else {
                    callBack.onFailure(null);
                }
            }

            @Override
            public void onFailure(IOException e) {
                callBack.onFailure(e);
            }
        });

    }

    /**
     * 修改报修单的所有信息
     *
     * @param data     报修单
     * @param callBack 回调,会返回更新后的data
     */
    public void modify(Data data, HttpUtil.HttpUtilCallBack<Data> callBack) {
        Map<String, Object> params = new HashMap<>();
        params.put("data", data);
        HttpUtil.post.haveResponse(GlobalUtil.URLS.UPDATE.MODIFY, params, new HttpUtil.HttpUtilCallBack<String>() {
            @Override
            public void onResponse(Response response, @Nullable String result) {
                String s = response.header("state");
                if (s != null && s.equals("OK") && result != null && !result.equals("{}")) {
                    callBack.onResponse(response, GsonUtil.getInstance().fromJson(result, Data.class));
                } else {
                    callBack.onFailure(null);
                }
            }

            @Override
            public void onFailure(IOException e) {
                callBack.onFailure(e);
            }
        });

    }

    /**
     * 手动创建一个新的报修单
     *
     * @param data     报修单
     * @param callBack 回调,返回插入后的data
     */
    public void insert(Data data, HttpUtil.HttpUtilCallBack<Data> callBack) {
        Map<String, Object> params = new HashMap<>();
        params.put("data", data);
        HttpUtil.post.haveResponse(GlobalUtil.URLS.INSERT.INSERT, params, new HttpUtil.HttpUtilCallBack<String>() {
            @Override
            public void onResponse(Response response, @Nullable String result) {
                String s = response.header("state");
                if (s != null && s.equals("OK") && result != null && !result.equals("{}")) {
                    callBack.onResponse(response, GsonUtil.getInstance().fromJson(result, Data.class));
                } else {
                    callBack.onFailure(null);
                }
            }

            @Override
            public void onFailure(IOException e) {
                callBack.onFailure(e);
            }
        });

    }

    /**
     * 删除报修单
     *
     * @param id       id
     * @param callBack 回调,返回操作是否成功
     */
    public void delete(String id, HttpUtil.HttpUtilCallBack<Boolean> callBack) {
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        HttpUtil.post.haveResponse(GlobalUtil.URLS.DELETE.DELETE, params, new HttpUtil.HttpUtilCallBack<String>() {
            @Override
            public void onResponse(Response response, @Nullable String result) {
                String s = response.header("state");
                if (s != null && s.equals("OK")) {
                    callBack.onResponse(response, true);
                } else {
                    callBack.onResponse(response, false);
                }
            }

            @Override
            public void onFailure(IOException e) {
                callBack.onFailure(e);
            }
        });

    }

    public void deleteMany(List<Integer> ids, HttpUtil.HttpUtilCallBack<Boolean> callBack) {
        Map<String, Object> params = new HashMap<>();
        params.put("ids", GsonUtil.getInstance().toJson(ids));
        HttpUtil.post.haveResponse(GlobalUtil.URLS.DELETE.DELETE_MANY, params, new HttpUtil.HttpUtilCallBack<String>() {
            @Override
            public void onResponse(Response response, @Nullable String result) {
                String s = response.header("state");
                if (s != null && s.equals("OK")) {
                    callBack.onResponse(response, true);
                } else {
                    callBack.onResponse(response, false);
                }
            }

            @Override
            public void onFailure(IOException e) {
                callBack.onFailure(e);
            }
        });

    }


    /**
     * 通过学号判断是或否为管理员
     *
     * @param callBack 回调
     */
    public void haveRoot(HttpUtil.HttpUtilCallBack<Boolean> callBack) {
        Map<String, Object> params = new HashMap<>();
        params.put("sno", GlobalUtil.user.getSno());
        HttpUtil.post.haveResponse(GlobalUtil.URLS.QUERY.HAVE_ROOT, params, new HttpUtil.HttpUtilCallBack<String>() {
            @Override
            public void onResponse(Response response, @Nullable String result) {
                String s = response.header("state");
                if (s != null && s.equals("OK")) {
                    callBack.onResponse(response, true);
                } else if (s != null && s.equals("FAILURE")) {
                    callBack.onResponse(response, false);
                }
            }

            @Override
            public void onFailure(IOException e) {
                callBack.onFailure(e);
            }
        });

    }

    /**
     * 查询当前报修服务状态
     *
     * @param callBack 回调,返回状态
     */
    public void checkState(HttpUtil.HttpUtilCallBack<Boolean> callBack) {
        Map<String, Object> params = new HashMap<>();
        params.put("type", "service");
        HttpUtil.post.haveResponse(GlobalUtil.URLS.QUERY.CHECK_STATE, params, new HttpUtil.HttpUtilCallBack<String>() {
            @Override
            public void onResponse(Response response, @Nullable String result) {
                String s = response.header("state");
                if (s != null && s.equals("1")) {
                    callBack.onResponse(response, true);
                } else {
                    callBack.onResponse(response, false);
                }
            }

            @Override
            public void onFailure(IOException e) {
                callBack.onFailure(e);
            }
        });

    }

    /**
     * 开启报修服务
     *
     * @param callBack 回调,返回操作是否成功
     */
    public void openService(HttpUtil.HttpUtilCallBack<Boolean> callBack) {
        Map<String, Object> params = new HashMap<>();
        params.put("type", "service");
        HttpUtil.post.haveResponse(GlobalUtil.URLS.UPDATE.START_SERVICE, params, new HttpUtil.HttpUtilCallBack<String>() {
            @Override
            public void onResponse(Response response, @Nullable String result) {
                String s = response.header("state");
                if (s != null && s.equals("OK")) {
                    callBack.onResponse(response, true);
                } else {
                    callBack.onResponse(response, false);
                }
            }

            @Override
            public void onFailure(IOException e) {
                callBack.onFailure(e);
            }
        });

    }

    /**
     * 关闭报修服务
     *
     * @param callBack 回调,返回操作是否成功
     */
    public void closeService(HttpUtil.HttpUtilCallBack<Boolean> callBack) {
        Map<String, Object> params = new HashMap<>();
        params.put("type", "service");
        HttpUtil.post.haveResponse(GlobalUtil.URLS.UPDATE.CLOSE_SERVICE, params, new HttpUtil.HttpUtilCallBack<String>() {
            @Override
            public void onResponse(Response response, @Nullable String result) {
                String s = response.header("state");
                if (s != null && s.equals("OK")) {
                    callBack.onResponse(response, true);
                } else {
                    callBack.onResponse(response, false);
                }
            }

            @Override
            public void onFailure(IOException e) {
                callBack.onFailure(e);
            }
        });

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
                String local = GlobalUtil.titlesN[i];
                editor.putBoolean(local, map.get(local));
            }
            editor.apply();
        }

        map = getSelectedLocalsS().getValue();
        if (map != null && editor != null) {
            for (int i = 0; i < map.size(); i++) {
                String local = GlobalUtil.titlesS[i];
                editor.putBoolean(local, map.get(local));
            }
            editor.apply();
        }
    }

    /**
     * 加载当前drawer的所有设置
     */
    public void loadAllSettings(int state) {

        Map<String, Boolean> mapN = getSelectedLocalsN().getValue();
        Map<String, Boolean> mapS = getSelectedLocalsS().getValue();
        SharedPreferences sharedPreferences = null;
        switch (state) {
            case 0:
                sharedPreferences = SharedPreferencesUtil.getSharedPreferences(GlobalUtil.KEYS.NEW.FILE_NAME_DRAWER);
                break;
            case 1:
                sharedPreferences = SharedPreferencesUtil.getSharedPreferences(GlobalUtil.KEYS.PROCESSING.FILE_NAME_DRAWER);
                break;
            case 2:
                sharedPreferences = SharedPreferencesUtil.getSharedPreferences(GlobalUtil.KEYS.DONE.FILE_NAME_DRAWER);
                break;
        }

        if (mapN != null && mapS != null && sharedPreferences != null) {
            getTimeOrder().setValue(sharedPreferences.getBoolean("time", false));

            for (int i = 0; i < 8; i++) {
                String localN = GlobalUtil.titlesN[i];
                mapN.put(localN, sharedPreferences.getBoolean(localN, state != 0 || GlobalUtil.user.getRoot() == 1 || GlobalUtil.user.getGroup() == 0));
                String localS = GlobalUtil.titlesS[i];
                mapS.put(localS, sharedPreferences.getBoolean(localS, state != 0 || GlobalUtil.user.getRoot() == 1 || GlobalUtil.user.getGroup() == 1));
            }

        }

//        Log.d(TAG, "loadAllSettings: state = " + state);
//        Log.d(TAG, "loadAllSettings: N " + getSelectedLocalsN().getValue());
//        Log.d(TAG, "loadAllSettings: S " + getSelectedLocalsS().getValue());
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
     * @return 查询得到的报修单
     */
    public void queryDataListBySetting(@Nullable Integer state) {
        state = state == null ? getCurrentState().getValue() : state;
        Map<String, Boolean> mapN = getSelectedLocalsN().getValue();
        Map<String, Boolean> mapS = getSelectedLocalsS().getValue();

        List<String> selectedLocals = new ArrayList<>();
        if (mapN != null && mapS != null) {
            for (int i = 0; i < 8; i++) {
                String localN = GlobalUtil.titlesN[i];
                if (mapN.get(localN)) {
                    selectedLocals.add(localN);
                }
                String localS = GlobalUtil.titlesS[i];
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
                if (d.getRepairer().contains(GlobalUtil.user.getName())) {//不需要的
                    dataList.remove(d);
                }
            }
            selectedDatas.clear();
            selectedDatas.addAll(dataList);
        } else if (state == 1) {
            selectedDatas = dataDao.queryBuilder()
                    .where(DataDao.Properties.Local.in(selectedLocals)
                            , DataDao.Properties.State.eq(1)
                            , DataDao.Properties.Repairer.notEq(GlobalUtil.user.getName()))
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
            for (String s : GlobalUtil.titlesN) {
                map.put(s, GlobalUtil.user.getRoot() == 1 || GlobalUtil.user.getGroup() == 0);
            }
            selectedLocalsN.setValue(map);
        }
        return selectedLocalsN;
    }

    public MutableLiveData<Map<String, Boolean>> getSelectedLocalsS() {
        if (selectedLocalsS == null) {
            selectedLocalsS = new MutableLiveData<>();
            Map<String, Boolean> map = new HashMap<>();
            for (String s : GlobalUtil.titlesS) {
                map.put(s, GlobalUtil.user.getRoot() == 1 || GlobalUtil.user.getGroup() == 1);
            }
            selectedLocalsS.setValue(map);

        }
        return selectedLocalsS;
    }

    public SharedPreferences getSharedPreferences() {
        int current = getCurrentState().getValue() == null ? 0 : getCurrentState().getValue();
        switch (current) {
            case 0:
                sharedPreferences = SharedPreferencesUtil.getSharedPreferences(GlobalUtil.KEYS.NEW.FILE_NAME_DRAWER);
                break;
            case 1:
                sharedPreferences = SharedPreferencesUtil.getSharedPreferences(GlobalUtil.KEYS.PROCESSING.FILE_NAME_DRAWER);
                break;
            case 2:
                sharedPreferences = SharedPreferencesUtil.getSharedPreferences(GlobalUtil.KEYS.DONE.FILE_NAME_DRAWER);
                break;
        }
        return sharedPreferences;
    }

    public MutableLiveData<Integer> getCurrentState() {
        if (currentState == null) {
            currentState = new MutableLiveData<>();
            currentState.setValue(0);
        }
        return currentState;
    }

    public MutableLiveData<List<Data>> getDataListAll() {
        if (dataListAll == null) {
            dataListAll = new MutableLiveData<>();
            dataListAll.setValue(dataDao.loadAll());
        }
//        if (dataListAll.getValue() == null) {
//            dataListAll.setValue(dataDao.loadAll());
//        }
        return dataListAll;
    }

    public MutableLiveData<List<Data>> getDataList1() {
        if (dataList1 == null) {
            dataList1 = new MutableLiveData<>();
            dataList1.setValue(new ArrayList<>());
        }
//        if (dataList1.getValue() == null) {
//            dataList1.setValue(new ArrayList<>());
//        }
        return dataList1;
    }

    public MutableLiveData<List<Data>> getDataList2() {
        if (dataList2 == null) {
            dataList2 = new MutableLiveData<>();
            dataList2.setValue(new ArrayList<>());
        }
//        if (dataList2.getValue() == null) {
//            dataList2.setValue(new ArrayList<>());
//        }
        return dataList2;
    }

    public MutableLiveData<List<Data>> getDataList3() {
        if (dataList3 == null) {
            dataList3 = new MutableLiveData<>();
            dataList3.setValue(new ArrayList<>());
        }
//        if (dataList3.getValue() == null) {
//            dataList3.setValue(new ArrayList<>());
//        }
        return dataList3;
    }

    public MutableLiveData<List<Data>> getDataList4() {
        if (dataList4 == null) {
            dataList4 = new MutableLiveData<>();
            dataList4.setValue(new ArrayList<>());
        }
//        if (dataList4.getValue() == null) {
//            dataList4.setValue(new ArrayList<>());
//        }
        return dataList4;
    }

    public MutableLiveData<List<Data>> getDataList5() {
        if (dataList5 == null) {
            dataList5 = new MutableLiveData<>();
            dataList5.setValue(new ArrayList<>());
        }
//        if (dataList5.getValue() == null) {
//            dataList5.setValue(new ArrayList<>());
//        }
        return dataList5;
    }

}
