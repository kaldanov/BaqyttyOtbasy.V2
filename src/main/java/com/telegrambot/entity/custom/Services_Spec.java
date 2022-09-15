package com.telegrambot.entity.custom;


import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table
public class Services_Spec {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column
    private long     id;

    @Column
    private long     serviceId;

    @Column
    private long     specId;

    public Services_Spec(long serviceId, long specId) {
        this.serviceId = serviceId;
        this.specId = specId;
    }

    public Services_Spec() {
    }

    //    @Column
//    private int     status_id;
    //1 spec
    //2 person

}
