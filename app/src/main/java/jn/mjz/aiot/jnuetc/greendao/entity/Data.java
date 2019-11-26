package jn.mjz.aiot.jnuetc.greendao.entity;

import com.youth.xframe.utils.XEmptyUtils;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.NotNull;

import java.util.UUID;

import jn.mjz.aiot.jnuetc.util.GlobalUtil;
import jn.mjz.aiot.jnuetc.util.GsonUtil;

/**
 * @author 19622
 */
@Entity
public class Data {
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

    @Keep
    public Data() {
        uuid = UUID.randomUUID().toString().replace("-", "");
        this.mark = GlobalUtil.MARKS[0];
        this.service = GlobalUtil.SERVICES[0];
        this.repairMessage = "";
    }


    @Generated(hash = 813129418)
    public Data(Long id, @NotNull String uuid, long date, short state,
                @NotNull String name, @NotNull String college, @NotNull String grade,
                String tel, String qq, @NotNull String local, short district,
                @NotNull String model, @NotNull String message, String repairer,
                long orderDate, long repairDate, String mark, String service,
                String repairMessage, String photo) {
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


    public long getDate() {
        return this.date;
    }


    public void setDate(long date) {
        this.date = date;
    }


    public short getState() {
        return this.state;
    }


    public void setState(short state) {
        this.state = state;
    }


    public String getName() {
        return this.name;
    }


    public void setName(String name) {
        this.name = name;
    }


    public String getCollege() {
        return this.college;
    }


    public void setCollege(String college) {
        this.college = college;
    }


    public String getGrade() {
        return this.grade;
    }


    public void setGrade(String grade) {
        this.grade = grade;
    }


    public String getTel() {
        return this.tel;
    }


    public void setTel(String tel) {
        this.tel = tel;
    }


    public String getQq() {
        return this.qq;
    }


    public void setQq(String qq) {
        this.qq = qq;
    }


    public String getLocal() {
        return this.local;
    }


    public void setLocal(String local) {
        this.district = "杏桃桔桂梅榴李竹".indexOf(local.charAt(0)) != -1 ? (short) 0 : (short) 1;
        this.local = local;
    }


    public String getModel() {
        return this.model;
    }


    public void setModel(String model) {
        this.model = model;
    }


    public String getMessage() {
        return this.message;
    }


    public void setMessage(String message) {
        this.message = message;
    }


    public String getRepairer() {
        return this.repairer;
    }


    public void setRepairer(String repairer) {
        this.repairer = repairer;
    }


    public long getRepairDate() {
        return this.repairDate;
    }


    public void setRepairDate(long repairDate) {
        this.repairDate = repairDate;
    }


    public String getMark() {
        return this.mark;
    }


    public void setMark(String mark) {
        this.mark = mark;
    }


    public String getService() {
        return this.service;
    }


    public void setService(String service) {
        this.service = service;
    }


    public String getRepairMessage() {
        return this.repairMessage;
    }


    public void setRepairMessage(String repairMessage) {
        this.repairMessage = repairMessage;
    }


    public short getDistrict() {
        return this.district;
    }


    public void setDistrict(short district) {
        this.district = district;
    }

    public long getOrderDate() {
        return this.orderDate;
    }


    public void setOrderDate(long orderDate) {
        this.orderDate = orderDate;
    }


    public String getPhoto() {
        return this.photo;
    }


    public void setPhoto(String photo) {
        this.photo = photo;
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

}