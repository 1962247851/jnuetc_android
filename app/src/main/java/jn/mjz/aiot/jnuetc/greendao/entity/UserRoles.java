package jn.mjz.aiot.jnuetc.greendao.entity;

/**
 * @author 19622
 */

public enum UserRoles {
    /**
     * 按分组接单
     */
    NORMAL,
    //可以看到全校的报修单
    WHOLE_SCHOOL,

    //有删单权限
    DELETE,
    //有控制报修开关的权限
    ADMINISTRATOR

}
