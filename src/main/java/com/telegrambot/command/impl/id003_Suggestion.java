package com.telegrambot.command.impl;

import com.telegrambot.command.Command;
import com.telegrambot.entity.User;
import com.telegrambot.entity.custom.Suggestion;
import com.telegrambot.entity.enums.WaitingType;
import com.telegrambot.util.Const;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Date;

public class id003_Suggestion extends Command {
    private Suggestion suggestion;
    private int         deleteMessageId;

    @Override
    public boolean  execute()       throws TelegramApiException {
        switch (waitingType) {
            case START:
                deleteMessage(updateMessageId);
                User user = userRepository.findByChatId(chatId);
                suggestion      = new Suggestion();
                suggestion.setFullName(user.getFullName());
                suggestion.setPostDate(new Date());
                suggestion.setPhoneNumber(user.getPhone());
                deleteMessageId = getSuggestion();
                waitingType     = WaitingType.SET_SUGGESTION;
                return COMEBACK;
            case SET_SUGGESTION:
                deleteMessage(updateMessageId);
                deleteMessage(deleteMessageId);
                if (hasMessageText()) {
                    suggestion.setText(updateMessageText);
                    suggestionRepository.save(suggestion);
                    sendMessage(Const.SUGGESTION_DONE);
                    return EXIT;
                } else {
                    wrongData();
                    getSuggestion();
                    return COMEBACK;
                }
        }
        return EXIT;
    }

    private void wrongData()     throws TelegramApiException {
        botUtils.sendMessage(Const.WRONG_DATA_TEXT, chatId);
    }

    private int getSuggestion() throws TelegramApiException {
        return botUtils.sendMessage(Const.SET_SUGGESTION_MESSAGE, chatId);
    }
}
