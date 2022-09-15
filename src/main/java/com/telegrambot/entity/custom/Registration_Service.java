package com.telegrambot.entity.custom;


import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table
//        ( schema = Const.TABLE_NAME)
public class Registration_Service {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column
    private long     id;

    @Column
    private long     userChatId;

    @Column
    private Date dateReg;

    @Column
    private long     serviceId;

    @Column
    private long     specId;

    @Column
    private String iin;

    public String getIin() {
        return iin;
    }

    @Column
    private Date inviteDate;

    @Column
    private String inviteTime;


    @Column
    private boolean isFinish;

    @Column
    private String parentIIN;

    public Registration_Service(long userChatId, Date dateReg, long serviceId, long specId, String iin, boolean isFinish) {
        this.userChatId = userChatId;
        this.dateReg = dateReg;
        this.serviceId = serviceId;
        this.specId = specId;
        this.iin = iin;
        this.isFinish = isFinish;
    }

    public Registration_Service() {
    }

}
