package com.telegrambot.entity.custom;


import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table
//        (schema = Const.TABLE_NAME)
public class CategoryGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = false)
    private long  id;

    private long groupChatId;
}
