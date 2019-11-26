package jn.mjz.aiot.jnuetc.greendao.entity;

import jn.mjz.aiot.jnuetc.util.GsonUtil;

public class User {
    private int id;
    private String openId;
    private String name;
    private short sex;
    private String sno;
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

    public String getSno() {
        return sno;
    }

    public void setSno(String sno) {
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

    /**
     * 判断用户角色
     *
     * @return 用户角色
     */
    private UserRoles getRole() {
        switch (getRoot()) {
            case 1:
                return UserRoles.WHOLE_SCHOOL;
            case 2:
                return UserRoles.DELETE;
            case 3:
                return UserRoles.ADMINISTRATOR;
            case 0:
            default:
                return UserRoles.NORMAL;
        }
    }

    /**
     * 判断是否可以收到整个学校的报修单
     *
     * @return 是否可以收到整个学校的报修单
     */
    public boolean haveWholeSchoolAccess() {
        return getRole() != UserRoles.NORMAL;
    }

    /**
     * 判断是否为最高管理员
     *
     * @return 是否为最高管理员
     */
    public boolean haveAdministratorAccess() {
        return getRole() == UserRoles.ADMINISTRATOR;
    }

    /**
     * root == 2 || root == 3
     *
     * @return 判断是否有删单权限，即管理员才有删单权限
     */
    public boolean haveDeleteAccess() {
        return getRole() != UserRoles.NORMAL && getRole() != UserRoles.WHOLE_SCHOOL;
    }

    /**
     * root == 2 || root == 3
     *
     * @return 判断是否有管理员权限
     */
    public boolean haveModifyAccess() {
        return getRole() != UserRoles.NORMAL && getRole() != UserRoles.WHOLE_SCHOOL;
    }

    /**
     * 判断编辑权限
     *
     * @param data 要处理的报修单
     * @return 是否有编辑权限
     */
    private boolean haveEditAccessWithData(Data data) {
        boolean b = false;
        switch (getRole()) {
            case DELETE:
            case ADMINISTRATOR:
                b = true;
                break;
            case NORMAL:
            case WHOLE_SCHOOL:
                b = haveRelationWithData(data);
                break;
            default:
        }
        return b;
    }

    /**
     * 判断与报修单是否有关系
     *
     * @param data 要判断的报修单
     * @return 是否有关系
     */
    public boolean haveRelationWithData(Data data) {
        return data.getRepairer().contains(getName());
    }
}
