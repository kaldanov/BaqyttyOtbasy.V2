package com.telegrambot.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import javax.persistence.Table;

@AllArgsConstructor
@Getter
@ToString
@Table(schema = "public")
public enum Language {

    ru(1),
    kz(2),
    en(3);

    private int id;

    public static Language getById(int id) {
        for (Language language : values())
            if (language.id == (id)) return language;
        return kz;
    }
}
