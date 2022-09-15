package com.telegrambot.entity.custom;


import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table
//        (schema = Const.TABLE_NAME)
public class ServiceSurveyAnswer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long    id;
    private int    surveyId;
    private long   chatId;
    private String button;
    private String handlingType;
}
