package jn.mjz.aiot.jnuetc.greendao.entity;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.library.baseAdapters.BR;

import com.google.gson.JsonObject;
import com.youth.xframe.XFrame;
import com.youth.xframe.entity.DateDifference;
import com.youth.xframe.utils.XAppUtils;
import com.youth.xframe.utils.XDateUtils;
import com.youth.xframe.utils.XEmptyUtils;
import com.youth.xframe.utils.http.HttpCallBack;
import com.youth.xframe.utils.http.XHttp;
import com.youth.xframe.widget.XToast;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.ToMany;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import jn.mjz.aiot.jnuetc.R;
import jn.mjz.aiot.jnuetc.application.App;
import jn.mjz.aiot.jnuetc.greendao.Dao.DaoSession;
import jn.mjz.aiot.jnuetc.greendao.Dao.DataChangeLogDao;
import jn.mjz.aiot.jnuetc.greendao.Dao.DataDao;
import jn.mjz.aiot.jnuetc.util.DateUtil;
import jn.mjz.aiot.jnuetc.util.GlobalUtil;
import jn.mjz.aiot.jnuetc.util.GsonUtil;
import jn.mjz.aiot.jnuetc.util.HttpUtil;
import jn.mjz.aiot.jnuetc.view.fragment.DataChangeLogFragment;
import jn.mjz.aiot.jnuetc.viewmodel.MainViewModel;

/**
 * @author 19622
 */
@Entity
public class Data extends BaseObservable {

    @Id
    private Long id;

    @NotNull
    @Index(unique = true)
    private String uuid;

    @NotNull
    private long date;

    @NotNull
    private short state;

    @NotNull
    private String name;

    @NotNull
    private String college;

    @NotNull
    private String grade;

    private String tel;

    private String qq;

    @NotNull
    private String local;

    @NotNull
    private short district;

    @NotNull
    private String model;

    @NotNull
    private String message;

    private String repairer;

    private long orderDate;

    private long repairDate;

    private String mark;

    private String service;

    private String repairMessage;

    private String photo;

    @ToMany(referencedJoinProperty = "dataId")
    private List<DataChangeLog> dataChangeLogs;

    /**
     * Used to resolve relations
     */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    /**
     * Used for active entity operations.
     */
    @Generated(hash = 1702140558)
    private transient DataDao myDao;

    @Keep
    public Data() {
        uuid = UUID.randomUUID().toString();
        this.mark = GlobalUtil.MARKS[0];
        this.service = GlobalUtil.SERVICES[0];
        this.repairMessage = "";
    }


    @Generated(hash = 813129418)
    public Data(Long id, @NotNull String uuid, long date, short state, @NotNull String name, @NotNull String college, @NotNull String grade, String tel, String qq, @NotNull String local, short district, @NotNull String model, @NotNull String message, String repairer, long orderDate,
                long repairDate, String mark, String service, String repairMessage, String photo) {
        this.id = id;
        this.uuid = uuid;
        this.date = date;
        this.state = state;
        this.name = name;
        this.college = college;
        this.grade = grade;
        this.tel = tel;
        this.qq = qq;
        this.local = local;
        this.district = district;
        this.model = model;
        this.message = message;
        this.repairer = repairer;
        this.orderDate = orderDate;
        this.repairDate = repairDate;
        this.mark = mark;
        this.service = service;
        this.repairMessage = repairMessage;
        this.photo = photo;
    }


    @Override
    public String toString() {
        return GsonUtil.getInstance().toJson(this);
    }


    public Long getId() {
        return this.id;
    }


    public void setId(Long id) {
        this.id = id;
    }


    public String getUuid() {
        return this.uuid;
    }


    public void setUuid(String uuid) {
        this.uuid = uuid;
    }


    public short getState() {
        return this.state;
    }


    public void setState(short state) {
        this.state = state;
    }


    @Bindable
    public String getName() {
        return this.name;
    }


    public void setName(String name) {
        this.name = name;
        notifyPropertyChanged(BR.name);
    }


    public static List<String> getColleges() {
        return Arrays.asList(XFrame.getResources().getStringArray(R.array.spinner_college_entries));
    }

    @Bindable
    public String getCollege() {
        return this.college;
    }

    public void setCollege(String college) {
        this.college = college;
        notifyPropertyChanged(BR.college);
    }

    public static List<String> getMarks() {
        return Arrays.asList(XFrame.getResources().getStringArray(R.array.spinner_mark_entries));
    }

    public static List<String> getGrades() {
        return Arrays.asList(XFrame.getResources().getStringArray(R.array.spinner_grade_entries));
    }

    public static List<String> getServices() {
        return Arrays.asList(XFrame.getResources().getStringArray(R.array.spinner_service_entries));
    }


    @Bindable
    public String getGrade() {
        return this.grade;
    }


    public void setGrade(String grade) {
        this.grade = grade;
        notifyPropertyChanged(BR.grade);
    }

    @Bindable
    public String getTel() {
        return this.tel;
    }


    public void setTel(String tel) {
        this.tel = tel;
        notifyPropertyChanged(BR.tel);
    }


    @Bindable
    public String getQq() {
        return this.qq;
    }

    public void setQq(String qq) {
        this.qq = qq;
        notifyPropertyChanged(BR.qq);
    }

    public static List<String> getLocals() {
        return Arrays.asList(GlobalUtil.LOCATIONS);
    }

    @Bindable
    public String getLocal() {
        return this.local;
    }


    public void setLocal(String local) {
        this.district = "杏桃桔桂梅榴李竹".indexOf(local.charAt(0)) != -1 ? (short) 0 : (short) 1;
        this.local = local;
        notifyPropertyChanged(BR.local);
    }


    @Bindable
    public String getModel() {
        return this.model;
    }


    public void setModel(String model) {
        this.model = model;
        notifyPropertyChanged(BR.model);
    }

    @Bindable
    public String getMessage() {
        return this.message;
    }


    public void setMessage(String message) {
        this.message = message;
        notifyPropertyChanged(BR.message);
    }

    @Bindable
    public String getRepairer() {
        return this.repairer;
    }


    public void setRepairer(String repairer) {
        this.repairer = repairer;
        notifyPropertyChanged(BR.repairer);
    }

    @Bindable
    public String getMark() {
        return this.mark;
    }


    public void setMark(String mark) {
        this.mark = mark;
        notifyPropertyChanged(BR.mark);
    }

    @Bindable
    public String getService() {
        return this.service;
    }


    public void setService(String service) {
        this.service = service;
        notifyPropertyChanged(BR.service);
    }


    @Bindable
    public String getRepairMessage() {
        return this.repairMessage;
    }


    public void setRepairMessage(String repairMessage) {
        this.repairMessage = repairMessage;
        notifyPropertyChanged(BR.repairMessage);
    }


    public short getDistrict() {
        return this.district;
    }


    public void setDistrict(short district) {
        this.district = district;
    }


    public String getPhoto() {
        return this.photo;
    }


    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public List<String> getPhotoUrlList() {
        List<String> urlList = null;
        if (photo != null && !photo.isEmpty()) {
            urlList = new LinkedList<>();
            List<String> photoList = GsonUtil.parseJsonArray2ObjectList(photo, String.class);
            for (String s : photoList) {
                urlList.add(String.format("%s?path=/opt/dataDP/&fileName=%s%s", GlobalUtil.Urls.File.DOWNLOAD, uuid, s));
            }
        }
        return urlList;
    }


    /**
     * 判断是否全为非空
     *
     * @return 全为非空
     */
    public boolean isAllNotEmpty() {
        boolean b = true;
        //判断报修的信息
        if (XEmptyUtils.isEmpty(getLocal()) || XEmptyUtils.isEmpty(getName()) || XEmptyUtils.isEmpty(getTel()) || XEmptyUtils.isEmpty(getQq()) || XEmptyUtils.isEmpty(getCollege()) || XEmptyUtils.isEmpty(getGrade()) || XEmptyUtils.isEmpty(getModel()) || XEmptyUtils.isEmpty(getMessage())) {
            b = false;
        }
        //还要判断反馈的
        if (getState() == 2) {
            if (XEmptyUtils.isEmpty(getRepairer()) || XEmptyUtils.isEmpty(getMark()) || XEmptyUtils.isEmpty(getService()) || XEmptyUtils.isEmpty(getRepairMessage())) {
                b = false;
            }
        }
        return b;
    }

    public String getDateString() {
        return DateUtil.getDateAndTime(date, " ");
    }

    public long getDate() {
        return this.date;
    }


    public void setDate(long date) {
        this.date = date;
    }

    public String getProcessingTitle() {
        switch (state) {
            case 0:
                return "等待处理已耗时";
            case 1:
                return "处理耗时";
            default:
                return "维修总用时";
        }
    }

    public String getProcessingTime() {
        String s = "";
        long startTimeMill;
        long endTimeMill;
        switch (state) {
            case 1:
                startTimeMill = orderDate;
                endTimeMill = System.currentTimeMillis();
                break;
            case 2:
                startTimeMill = orderDate;
                endTimeMill = repairDate;
                break;
            default:
                startTimeMill = date;
                endTimeMill = System.currentTimeMillis();
        }
        DateDifference twoDataDifference = XDateUtils.getTwoDataDifference(new Date(endTimeMill), new Date(startTimeMill));
        int day = (int) twoDataDifference.getDay();
        int hour = (int) twoDataDifference.getHour() % 24;
        int minute = (int) twoDataDifference.getMinute() % 60;
        int second = (int) twoDataDifference.getSecond() % 60;
        if (day != 0) {
            s = (s + day + "天");
        }
        if (hour != 0) {
            s = (s + hour + "小时");
        }
        if (minute != 0) {
            s = (s + minute + "分钟");
        }
        if (s.isEmpty()) {
            s = "不足一分钟";
        }
        return s;
    }

    public long getOrderDate() {
        return this.orderDate;
    }


    public void setOrderDate(long orderDate) {
        this.orderDate = orderDate;
    }

    public String getRepairDateString() {
        return DateUtil.getDateAndTime(repairDate, " ");
    }


    public long getRepairDate() {
        return this.repairDate;
    }


    public void setRepairDate(long repairDate) {
        this.repairDate = repairDate;
    }


    public String getStateString() {
        switch (state) {
            case 0:
                return "待处理";
            case 1:
                return "处理中";
            default:
                return "已维修";
        }
    }

    /**
     * 根据Id从服务器获取最新信息，并排序更新日志
     *
     * @param callBack 回调
     */
    public static void update(Long id, HttpUtil.HttpUtilCallBack<Data> callBack) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        XHttp.obtain().post(GlobalUtil.Urls.Data.QUERY_BY_ID, params, new HttpCallBack<JsonObject>() {
            @Override
            public void onSuccess(JsonObject jsonObject) {
                int error = jsonObject.get("error").getAsInt();
                if (error == 1) {
                    Data data = GsonUtil.getInstance().fromJson(jsonObject.get("body").getAsString(), Data.class);
                    DataChangeLogFragment.sortLogByTimeDesc(data.getDataChangeLogs());
                    App.getDaoSession().getDataDao().update(data);
                    callBack.onResponse(data);
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
     * 修改报修单的所有信息，修改成功后再查询一次报修单，因为dataChangeLogs是另一张表，有延迟
     *
     * @param oldDataJson 修改前的报修单
     * @param callBack    回调,会返回更新后的data
     */
    public void modify(String oldDataJson, HttpUtil.HttpUtilCallBack<Data> callBack) {
        Map<String, Object> params = new HashMap<>(3);
        params.put("dataJson", toString());
        params.put("oldDataJson", oldDataJson);
        params.put("name", MainViewModel.getUser().getUserName());
        XHttp.obtain().post(GlobalUtil.Urls.Data.UPDATE, params, new HttpCallBack<JsonObject>() {
            @Override
            public void onSuccess(JsonObject jsonObject) {
                int error = jsonObject.get("error").getAsInt();
                if (error == 1) {
                    Data data1 = GsonUtil.getInstance().fromJson(jsonObject.get("body").getAsString(), Data.class);
                    Data.update(data1.getId(), new HttpUtil.HttpUtilCallBack<Data>() {
                        @Override
                        public void onResponse(Data result) {
                            App.getDaoSession().getDataDao().update(result);
                            callBack.onResponse(result);
                        }

                        @Override
                        public void onFailure(String error) {
                            callBack.onFailure(error);
                        }
                    });

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
     * 打开qq或Tim并复制qq号
     *
     * @param qq QQ号
     */
    public static void openQq(String qq) {
        if (XAppUtils.isInstallApp("com.tencent.mobileqq")) {
            XAppUtils.startApp("com.tencent.mobileqq");
            MainViewModel.copyToClipboard(XFrame.getContext(), qq);
            XToast.success(String.format("QQ：%s已复制到剪切板", qq));
        } else if (XAppUtils.isInstallApp("com.tencent.tim")) {
            XAppUtils.startApp("com.tencent.tim");
            MainViewModel.copyToClipboard(XFrame.getContext(), qq);
            XToast.success(String.format("QQ：%s已复制到剪切板", qq));
        } else {
            XToast.error("未安装QQ和Tim或安装的版本不支持");
        }
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 978765309)
    public List<DataChangeLog> getDataChangeLogs() {
        if (dataChangeLogs == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            DataChangeLogDao targetDao = daoSession.getDataChangeLogDao();
            List<DataChangeLog> dataChangeLogsNew = targetDao._queryData_DataChangeLogs(id);
            synchronized (this) {
                if (dataChangeLogs == null) {
                    dataChangeLogs = dataChangeLogsNew;
                }
            }
        }
        return dataChangeLogs;
    }


    /**
     * Resets a to-many relationship, making the next get call to query for a fresh result.
     */
    @Generated(hash = 496895173)
    public synchronized void resetDataChangeLogs() {
        dataChangeLogs = null;
    }


    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 128553479)
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.delete(this);
    }


    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 1942392019)
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.refresh(this);
    }


    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 713229351)
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.update(this);
    }


    /**
     * called by internal mechanisms, do not call yourself.
     */
    @Generated(hash = 966473446)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getDataDao() : null;
    }


}
