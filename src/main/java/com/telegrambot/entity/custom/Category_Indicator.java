package com.telegrambot.entity.custom;


import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table
//        ( schema = Const.TABLE_NAME)
public class Category_Indicator {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long     id;

    @Column
    private Long      second;

    @Column(length = 4096)
    private String  name;

    private int langId;

}
