package com.telegrambot.entity.custom;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table
public class RegistrationEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long     id;
    private long    chatId;
    private long    eventId;
    private Date registrationDate;
    private boolean isCome;
}
