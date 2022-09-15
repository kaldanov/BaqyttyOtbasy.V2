package com.telegrambot.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "users", schema = "public")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long     id;

    private long    chatId;

    @Column(length = 50)
    private String  phone;

    @Column(length = 200)
    private String  fullName;
//
//    @Column(length = 200)
//    private String  district;

    @Column(length = 500)
    private String  userName;

    @Column(length = 4096)
    private String  status;

    @Column(length = 30)
    private String iin;
//    @Column(length = 4096)
//    private String email;

    public User() {
    }
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getChatId() {
        return chatId;
    }

    public void setChatId(long chatId) {
        this.chatId = chatId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getIin() {
        return iin;
    }

    public void setIin(String iin) {
        this.iin = iin;
    }
}
