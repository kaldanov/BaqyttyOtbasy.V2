package com.telegrambot.entity.custom;


import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table
//        (schema = Const.TABLE_NAME)
public class QuestMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long    id;
    private long    languageId;
    private String message;
    private String range;
    private long    idQuest;

    public QuestMessage setId(long id) {
        this.id = id;
        return this;
    }

    public QuestMessage setIdQuest(long idQuest) {
        this.idQuest = idQuest;
        return this;
    }

    public QuestMessage setRange(String range) {
        this.range = range;
        return this;
    }

    public QuestMessage setIdLanguage(long languageId) {
        this.languageId = languageId;
        return this;
    }


}
