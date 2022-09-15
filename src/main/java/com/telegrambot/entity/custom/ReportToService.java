package com.telegrambot.entity.custom;


import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table
//        ( schema = Const.TABLE_NAME)
public class ReportToService {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long     id;
//
//    @Column(length = 50)
//    private String  text;

    private String file;

    private long serviceId;
    private long senderChatId;
    private Date sendDate;


    public ReportToService(String file) {
        this.file = file;
    }

    public ReportToService() {
    }
}
