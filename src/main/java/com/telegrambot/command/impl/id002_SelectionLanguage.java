package com.telegrambot.command.impl;

import com.telegrambot.command.Command;
import com.telegrambot.enums.Language;
import com.telegrambot.service.LanguageService;
import com.telegrambot.util.Const;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class id002_SelectionLanguage extends Command {

    @Override
    public  boolean execute() throws TelegramApiException {
        deleteMessage(updateMessageId);
        if (!isUser()) {
            sendMessage(Const.NO_ACCESS);
            return EXIT;
        }
        chosenLanguage();
        sendMessage(Const.WELCOME_TEXT_WHEN_START);
        return EXIT;
    }

    private void    chosenLanguage() {
        if (isButton(Const.RU_LANGUAGE)) LanguageService.setLanguage(chatId, Language.ru);
        if (isButton(Const.KZ_LANGUAGE)) LanguageService.setLanguage(chatId, Language.kz);
    }
}
