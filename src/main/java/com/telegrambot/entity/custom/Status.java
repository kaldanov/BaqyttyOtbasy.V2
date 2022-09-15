package com.telegrambot.entity.custom;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(schema = "public")
public class Status {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    String nameKaz;
    String nameRus;

    int statusId;

}
