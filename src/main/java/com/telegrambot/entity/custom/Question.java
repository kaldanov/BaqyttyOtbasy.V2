package com.telegrambot.entity.custom;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table
//        (schema = Const.TABLE_NAME)
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long     id;
    @Column
    private long     id2;
    @Column(length = 4096)
    private String  name;
    @Column(length = 4096)
    private String description;
    @Column
    private int     languageId;
    @Column
    private boolean isHide;
}
