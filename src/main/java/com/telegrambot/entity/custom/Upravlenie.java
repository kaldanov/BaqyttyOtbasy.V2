package com.telegrambot.entity.custom;


import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table
public class Upravlenie {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = false)
    private long     id;

    @Column
    private long chatId;

    @Column(length = 4096)
    private String comment;

}
