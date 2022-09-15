package com.telegrambot.entity.custom;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table
//        (schema = Const.TABLE_NAME)
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long     id;
    private String  names;
    private long    chatId;
    private String  userName;
    private boolean isRegistered;
    private String  message;
    private boolean isCanWithoutTag;
    private boolean isCanPhoto;
    private boolean isCanVideo;
    private boolean isCanAudio;
    private boolean isCanFile;
    private boolean isCanLink;
    private boolean isCanSticker;
}
