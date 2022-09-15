package com.telegrambot.entity;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table
//        (schema = Const.TABLE_NAME)
public class Keyboard {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long    id;

    @Column(length = 4096)
    private String  buttonIds;

    private boolean inline;

    @Column(length = 4096)
    private String  comment;
}
