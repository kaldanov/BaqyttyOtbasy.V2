package com.telegrambot.entity.custom;


import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table
//        (schema = Const.TABLE_NAME)
public class SurveyAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long    id;
    private long    surveyId;
    private long   chatId;
    private String button;
    private String text;
}
