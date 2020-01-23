package jn.mjz.aiot.jnuetc.greendao.entity;

import jn.mjz.aiot.jnuetc.util.GsonUtil;

/**
 * @author 19622
 */
public class Version {
    private Long id;
    private String message;
    private long date;
    private String version;
    private String url;

    @Override
    public String toString() {
        return GsonUtil.getInstance().toJson(this);
    }

    public Version() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
