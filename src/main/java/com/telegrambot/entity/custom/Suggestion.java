package com.telegrambot.entity.custom;


import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table
//        (schema = Const.TABLE_NAME)
public class Suggestion {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long    id;
    private String fullName;
    private String phoneNumber;
    private String text;

    @Temporal(TemporalType.DATE)
    private Date postDate;

    public long getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Date getPostDate() {
        return postDate;
    }

    public void setPostDate(Date postDate) {
        this.postDate = postDate;
    }
}
