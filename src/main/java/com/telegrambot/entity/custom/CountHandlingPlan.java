package com.telegrambot.entity.custom;


import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table
//        (schema = Const.TABLE_NAME)
public class CountHandlingPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long    id;

    @Column(length = 50)
    private String handlingType;
    private int    courseTypeId;
    private int    courseNameId;
    private int    countPeople;
}
