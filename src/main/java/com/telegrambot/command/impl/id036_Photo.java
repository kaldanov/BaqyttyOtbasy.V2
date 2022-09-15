package com.telegrambot.command.impl;

import com.telegrambot.command.Command;
import com.telegrambot.entity.enums.WaitingType;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class id036_Photo extends Command {

    @Override
    public boolean execute() throws TelegramApiException {
        switch (waitingType) {
            case START:
                deleteMessage(updateMessageId);
                sendMessage("Отправь фото");
                waitingType = WaitingType.SET_PHOTO;
                return COMEBACK;
            case SET_PHOTO:
                deleteMessage(updateMessageId);
                if (hasPhoto()) {
                    sendMessage(updateMessagePhoto);
                }
//                return EXIT;
        }
        return EXIT;
    }
}
