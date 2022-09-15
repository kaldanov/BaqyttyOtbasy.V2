package com.telegrambot.entity.custom;


import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table
//        ( schema = Const.TABLE_NAME)
public class DirectionRegistration {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long     id;

    @Column
    private long     registrationId;

    @Column
    private long     directionId;

    public DirectionRegistration(long registrationId, long directionId) {
        this.registrationId = registrationId;
        this.directionId = directionId;
    }

    public DirectionRegistration() {
    }
}
