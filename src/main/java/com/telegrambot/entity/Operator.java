package com.telegrambot.entity;

import javax.persistence.*;


@Entity
@Table
//        (schema = Const.TABLE_NAME)
public class Operator {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long     id;

    private long    userId;

    @Column(length = 4096)
    private String  comment;

    public long getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
