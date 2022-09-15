package com.telegrambot.entity.custom;


import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table
//        ( schema = Const.TABLE_NAME)
public class Direction {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long     id;


    @Column(length = 4096)
    private String  nameKaz;

    @Column(length = 4096)
    private String  nameRus;

    @Column
    private long     serviceId;

    public String getName(int langId){
        if (langId ==1)
            return getNameRus();
        return getNameKaz();
    }

}
