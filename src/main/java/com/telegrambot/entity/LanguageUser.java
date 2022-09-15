package com.telegrambot.entity;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table
//        (schema = Const.TABLE_NAME)
public class LanguageUser {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long     id;

    private long    chatId;

    private int     languageId;

    public LanguageUser setChatId(long chatId) {
        this.chatId = chatId;
        return this;
    }

    public LanguageUser setLanguageId(int languageId) {
        this.languageId = languageId;
        return this;
    }
}
