package com.telegrambot.entity;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table
public class Admin {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = false)

    private long   id;

    private long   userId;

    @Column(length = 4096)
    private String comment;
}
