package com.telegrambot.entity.custom;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table
//        (schema = Const.TABLE_NAME)
public class Complaint {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long    id;
    private String fullName;
    private String phoneNumber;
    private String text;
    private Date postDate;
}
