package jn.mjz.aiot.jnuetc.greendao.entity;

import jn.mjz.aiot.jnuetc.util.GsonUtil;

/**
 * @author 19622
 */
public class User {
    private Long id;
    private String openId;
    private String formId;
    private String userName;
    private Integer sex;
    private String sno;
    private String password;
    private Integer rootLevel;
    private Integer whichGroup;
    private long regDate;

    @Override
    public String toString() {
        return GsonUtil.getInstance().toJson(this);
    }

    public User() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public String getFormId() {
        return formId;
    }

    public void setFormId(String formId) {
        this.formId = formId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Integer getSex() {
        return sex;
    }

    public void setSex(Integer sex) {
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

    public Integer getRootLevel() {
        return rootLevel;
    }

    public void setRootLevel(Integer rootLevel) {
        this.rootLevel = rootLevel;
    }

    /**
     * 根据{@link #getRole()} 返回园区
     * @return 所有园区/北区/南区
     */
    public String getGroupStringIfNotAll() {
        return haveWholeSchoolAccess() ? "所有园区" : getWhichGroup() == 0 ? "北区" : "南区";
    }

    public Integer getWhichGroup() {
        return whichGroup;
    }

    public void setWhichGroup(Integer whichGroup) {
        this.whichGroup = whichGroup;
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
        switch (getRootLevel()) {
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
     * rootLevel == 2 || rootLevel == 3
     *
     * @return 判断是否有删单权限，即管理员才有删单权限
     */
    public boolean haveDeleteAccess() {
        return getRole() != UserRoles.NORMAL && getRole() != UserRoles.WHOLE_SCHOOL;
    }

    /**
     * rootLevel == 2 || rootLevel == 3
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
        return data.getRepairer().contains(getUserName());
    }

}
