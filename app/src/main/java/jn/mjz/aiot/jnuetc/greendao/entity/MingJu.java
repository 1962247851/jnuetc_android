package jn.mjz.aiot.jnuetc.greendao.entity;

import androidx.annotation.NonNull;

import jn.mjz.aiot.jnuetc.util.GsonUtil;

/**
 * @author qq1962247851
 * @date 2020/1/15 13:31
 */
public class MingJu {
    private Long id;
    private String author;
    private String shiName;
    private String content;
    private String topic;
    private String type;

    @NonNull
    @Override
    public String toString() {
        return GsonUtil.getInstance().toJson(this);
    }

    public MingJu() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getShiName() {
        return shiName;
    }

    public void setShiName(String shiName) {
        this.shiName = shiName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
