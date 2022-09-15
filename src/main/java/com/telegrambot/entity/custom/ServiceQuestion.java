package com.telegrambot.entity.custom;


import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table
//        (schema = Const.TABLE_NAME)
public class ServiceQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long     id;
    private String  name;
    private String  question;
    private int     languageId;
    private int     serviceId;
    @Column(name = "is_hide")
    private boolean hider;
    private String  handlingType;
}
