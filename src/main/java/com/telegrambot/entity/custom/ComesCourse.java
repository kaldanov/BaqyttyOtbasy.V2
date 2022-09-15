package com.telegrambot.entity.custom;


import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table
//        ( schema = Const.TABLE_NAME)
public class ComesCourse {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = false)
    private long     id;

    @Column
    private long  registrationServiceId;


    @Column
    private Date actionDate;

    public ComesCourse(long registrationServiceId, Date actionDate) {
        this.registrationServiceId = registrationServiceId;
        this.actionDate = actionDate;
    }

    public ComesCourse() {
    }
}
