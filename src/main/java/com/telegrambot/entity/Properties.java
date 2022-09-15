package com.telegrambot.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table
//        (schema = Const.TABLE_NAME)
public class Properties {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long     id;

    @Column(length = 4096)
    private String  name;

    @Column(length = 4096)
    private String value1;

    @Column(length = 4096)
    private String  value;
}
