package com.telegrambot.entity.custom;


import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table
//        ( schema = Const.TABLE_NAME)
public class Service {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column
    private long     id;

    @Column
    private long     id2;

    @Column
    private long     categoryId;

    @Column
    private long     statusId;
    //1 пришел/приграсить                 | разовое посещение
    //2 пришел/пригласить/закончить курс  | курс
    //3 без кнопок                        | персон

    @Column(length = 4096)
    private String  name;

    @Column(length = 4096)
    private String  description;

    @Column(length = 4096)
    private String  photo;


    private long langId;

    @Column(columnDefinition = "boolean default true")
    private boolean active;

}
