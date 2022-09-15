package com.telegrambot.entity.custom;


import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table
//        ( schema = Const.TABLE_NAME)
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = false)
    private long     id;

    @Column(length = 50)
    private String  name;

    private boolean language;

    private boolean isHide;
}
