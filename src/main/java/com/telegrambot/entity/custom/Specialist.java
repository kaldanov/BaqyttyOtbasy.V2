package com.telegrambot.entity.custom;


import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table
//        (schema = Const.TABLE_NAME)
public class Specialist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long     id;
    private long    chatId;
    private String  fullName;

    @Column(length = 4096)
    private String photo;
//    private int status_id;
    @Column(length = 4096)
    private String descriptionRus;

    @Column(length = 4096)
    private String descriptionKaz;

    @Column(columnDefinition = "boolean default true")
    private boolean active;

    public Specialist setChatId(long chatId) {
        this.chatId = chatId;
        return this;
    }

    public Specialist setFullName(String fullName) {
        this.fullName = fullName;
        return this;
    }
}
