package com.telegrambot.entity.custom;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;


@Data
@Entity
@Table
//        (schema = Const.TABLE_NAME)
public class Kpi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long ID;
    private String IIN;
    private Date date;
    private String kpiType;
}
