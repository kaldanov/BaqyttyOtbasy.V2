package com.telegrambot.command.impl;

import com.telegrambot.command.Command;
import org.telegram.telegrambots.meta.api.methods.send.SendLocation;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class id034_MapLocationSend extends Command {

    @Override
    public boolean execute() throws TelegramApiException {
        if(userRepository.findByChatId(chatId) == null){
            sendMessageWithKeyboard("Вы не прошли регистрацию" , 57);
            return EXIT;
        }
        SendLocation sendLocation = new SendLocation();
        sendLocation.setLatitude(Float.parseFloat(propertiesRepository.findById(5).getValue()));
        sendLocation.setLongitude(Float.parseFloat(propertiesRepository.findById(5).getValue()));
        bot.execute(sendLocation.setChatId(chatId)).getMessageId();
        return EXIT;
    }
}
