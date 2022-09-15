package com.telegrambot.entity.custom;

import lombok.Data;

import javax.persistence.*;


@Entity
@Data
@Table
//        (schema = Const.TABLE_NAME)
public class Consultation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long    id;
    private int second;
    private String  photo;
    private String  text;
    private int     consultationNameId;
    private String  fullName;
    private Long    consultationTeacherId;
    private int     langId;
}
