package com.telegrambot.command.impl;

import com.telegrambot.command.Command;
import com.telegrambot.entity.Message;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendAudio;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
public class id031_StructureShowInfo extends Command {

    @Override
    public boolean execute() throws TelegramApiException {
        if(userRepository.findByChatId(chatId) == null){
            sendMessageWithKeyboard("Вы не прошли регистрацию" , 57);
            return EXIT;
        }
        deleteMessage(updateMessageId);
        Message message = messageRepository.findByIdAndLangId(messageId, getLanguage().getId());
        try {
            if (message.getFile() != null) {
                switch (message.getTypeFile()) {
                    case "audio":
                        bot.execute(new SendAudio().setAudio(message.getFile()).setChatId(chatId));
                    case "video":
                        bot.execute(new SendVideo().setVideo(message.getFile()).setChatId(chatId));
                    case "document":
                        bot.execute(new SendDocument().setChatId(chatId).setDocument(message.getFile()));
                    case "photo":
                        bot.execute(new SendPhoto().setChatId(chatId).setPhoto(message.getFile()));
                }
            }
        } catch (TelegramApiException e) {
            log.error("Exception by send file for message " + messageId, e);
        }
        sendMessage(messageId, chatId, null, message.getPhoto());
        return EXIT;
    }
}
