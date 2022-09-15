package com.telegrambot.entity.custom;

import javax.persistence.*;

@Entity
@Table
//        (schema = Const.TABLE_NAME)
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long     id;

    @Column(length = 50)
    private String  name;
    private String  photo;
    private String  text;
    @Column(name = "is_hide")
    private boolean hider;

    public long getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isHide() {
        return hider;
    }

    public void setHide(boolean hide) {
        hider = hide;
    }
}
