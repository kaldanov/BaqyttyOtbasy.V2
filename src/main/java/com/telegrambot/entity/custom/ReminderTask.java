package com.telegrambot.entity.custom;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table
//        (schema = Const.TABLE_NAME)
public class ReminderTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long     id;
    private String  text;
    private Date dateBegin;
}
