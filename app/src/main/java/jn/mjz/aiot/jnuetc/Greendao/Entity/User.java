package jn.mjz.aiot.jnuetc.Greendao.Entity;

import jn.mjz.aiot.jnuetc.Util.GsonUtil;

public class User {
    private int id;
    private String openId;
    private String name;
    private short sex;
    private int sno;
    private String password;
    private short root;
    private int group;
    private int count;
    private long regDate;

    @Override
    public String toString() {
        return GsonUtil.getInstance().toJson(this);
    }

    public User() {
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public short getSex() {
        return sex;
    }

    public void setSex(short sex) {
        this.sex = sex;
    }

    public int getSno() {
        return sno;
    }

    public void setSno(int sno) {
        this.sno = sno;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public short getRoot() {
        return root;
    }

    public void setRoot(short root) {
        this.root = root;
    }

    public int getGroup() {
        return group;
    }

    public void setGroup(int group) {
        this.group = group;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public long getRegDate() {
        return regDate;
    }

    public void setRegDate(long regDate) {
        this.regDate = regDate;
    }

}
