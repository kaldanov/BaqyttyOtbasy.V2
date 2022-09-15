package com.telegrambot.command.impl;

import com.telegrambot.entity.Message;
import com.telegrambot.util.Const;
import com.telegrambot.command.Command;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class id033_SpecShowInfo extends Command {

    @Override
    public boolean execute() throws TelegramApiException {
        if(userRepository.findByChatId(chatId) == null){
            sendMessageWithKeyboard("Вы не прошли регистрацию" , 57);
            return EXIT;
        }
        if (specialistRepository.findAllByChatIdOrderById(chatId).size() < 1) {
            sendMessage(Const.NO_ACCESS);
            return EXIT;
        }
        deleteMessage(updateMessageId);
        Message message = messageRepository.findByIdAndLangId(messageId, getLanguage().getId());
        sendMessage(messageId, chatId, null, message.getPhoto());
//        if (message.getFile() != null) {
//            switch (message.getTypeFile()) {
//                case "audio":
//                    bot.execute(new SendAudio().setAudio(message.getFile()).setChatId(chatId));
//                case "video":
//                    bot.execute(new SendVideo().setVideo(message.getFile()).setChatId(chatId));
//                case "document":
//                    bot.execute(new SendDocument().setDocument(message.getFile()).setChatId(chatId));
//            }
//        }
        return EXIT;
    }
}
