package com.telegrambot.entity;

import com.telegrambot.enums.FileType;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table
//        (schema = Const.TABLE_NAME)
public class Message {

    @Id
    @Column(unique = false)
    private long     id;

    @Column(length = 8096)
    private String  name;

    @Column(length = 8096)
    private String  photo;

    private Integer keyboardId;

    @Column(length = 8096)
    private String  file;

    @Column(length = 100)
    private String  typeFile;

    private int     langId;

    public void setFile(String file, FileType fileType) {
        this.file       = file;
        this.typeFile   = fileType.name();
    }
}
