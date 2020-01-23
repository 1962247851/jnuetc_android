package jn.mjz.aiot.jnuetc.greendao.entity;

import java.util.List;

/**
 * @author qq1962247851
 * @date 2020/1/14 16:52
 */
public class RankingInfo {
    private String userName;
    private Integer count;
    private List<Data> dataList;

    public RankingInfo(String userName, List<Data> dataList) {
        this.userName = userName;
        this.dataList = dataList;
        this.count = dataList.size();
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public List<Data> getDataList() {
        return dataList;
    }

    public void setDataList(List<Data> dataList) {
        this.dataList = dataList;
    }
}
