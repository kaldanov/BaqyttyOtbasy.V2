package com.telegrambot.entity;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table
//        (schema = Const.TABLE_NAME)
public class Button {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = false)
    private long     id;

    @Column(length = 300)
    private String  name;

    @Column(columnDefinition = "int default 0")
    private Integer commandId;

    @Column(length = 4096)
    private String  url;

    private boolean requestContact;

    private Integer messageId;

    private int     langId;

    public long getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getCommandId() {
        return commandId;
    }

    public void setCommandId(Integer commandId) {
        this.commandId = commandId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isRequestContact() {
        return requestContact;
    }

    public void setRequestContact(boolean requestContact) {
        this.requestContact = requestContact;
    }

    public Integer getMessageId() {
        return messageId;
    }

    public void setMessageId(Integer messageId) {
        this.messageId = messageId;
    }

    public int getLangId() {
        return langId;
    }

    public void setLangId(int langId) {
        this.langId = langId;
    }
}
