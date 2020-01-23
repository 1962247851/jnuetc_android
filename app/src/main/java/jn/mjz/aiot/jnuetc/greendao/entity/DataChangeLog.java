package jn.mjz.aiot.jnuetc.greendao.entity;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Generated;

/**
 * @author 19622
 */
@Entity
public class DataChangeLog {
    @Id
    private Long id;
    @NotNull
    private String changeInfo;
    @NotNull
    private long date;
    @NotNull
    private String name;
    @NotNull
    private Long dataId;

    @Keep
    public DataChangeLog() {
        this.date = System.currentTimeMillis();
    }

    @Generated(hash = 1722556467)
    public DataChangeLog(Long id, @NotNull String changeInfo, long date, @NotNull String name,
            @NotNull Long dataId) {
        this.id = id;
        this.changeInfo = changeInfo;
        this.date = date;
        this.name = name;
        this.dataId = dataId;
    }

    @Override
    public String toString() {
        Gson gson = new GsonBuilder().addSerializationExclusionStrategy(new ExclusionStrategy() {
            @Override
            public boolean shouldSkipField(FieldAttributes f) {
                return "data".equals(f.getName());
            }

            @Override
            public boolean shouldSkipClass(Class<?> clazz) {
                return false;
            }
        }).create();
        return gson.toJson(this);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getChangeInfo() {
        return changeInfo;
    }

    public void setChangeInfo(String changeInfo) {
        this.changeInfo = changeInfo;
    }

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getDataId() {
        return dataId;
    }

    public void setDataId(Long dataId) {
        this.dataId = dataId;
    }

    public void setDate(long date) {
        this.date = date;
    }
}
