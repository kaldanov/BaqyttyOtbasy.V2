package com.telegrambot.command.impl;

import com.telegrambot.command.Command;
import com.telegrambot.entity.custom.Category_Indicator;
import com.telegrambot.util.Const;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static org.apache.http.client.methods.RequestBuilder.delete;


public class id043_Add_Category_Indicator extends Command {

    public int deleteMessId;
    public int secondDeleteMessId;



    @Override
    public boolean execute() throws TelegramApiException {
        if (!isAdmin()) {
            sendMessage(Const.NO_ACCESS);
            return EXIT;
        }
        if (isAdmin()) {
            switch (waitingType) {
                case START:

                    return COMEBACK;



            }

            return true;
        }

        return EXIT;
    }

    private void deleteMess() {

        deleteMessage(updateMessageId);
        deleteMessage(deleteMessId);
        deleteMessage(secondDeleteMessId);
    }


    public String getText(int messageIdFromDb) {
        return messageRepository.getMessageText(messageIdFromDb, getLanguage().getId());
    }


}
