package jn.mjz.aiot.jnuetc.greendao.entity;

/**
 * @author qq1962247851
 * @date 2020/1/19 19:54
 */
public class DataModified {
    private Data oldData;
    private Data newData;


    public DataModified(Data oldData, Data newData) {
        this.oldData = oldData;
        this.newData = newData;
    }

    public Data getOldData() {
        return oldData;
    }

    public void setOldData(Data oldData) {
        this.oldData = oldData;
    }

    public Data getNewData() {
        return newData;
    }

    public void setNewData(Data newData) {
        this.newData = newData;
    }
}
