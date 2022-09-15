package com.telegrambot.command.impl;

import com.telegrambot.command.Command;
import com.telegrambot.entity.User;
import com.telegrambot.entity.custom.Complaint;
import com.telegrambot.entity.custom.Suggestion;
import com.telegrambot.entity.enums.WaitingType;
import com.telegrambot.util.Const;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Date;

public class id023_Complaint extends Command {

//    @Autowired
//    private SuggestionRepository suggestionRepository;
//    @Autowired
//    private ComplaintRepository complaintRepository;
    private Suggestion  suggestion;
    private int         deleteMessageId;

    @Override
    public boolean  execute()       throws TelegramApiException {
        switch (waitingType) {
            case START:
                if(userRepository.findByChatId(chatId) == null){
                    sendMessageWithKeyboard("Вы не прошли регистрацию" , 57);
                    return EXIT;
                }
                deleteMessage(updateMessageId);
                User user = userRepository.findByChatId(chatId);
                suggestion      = new Suggestion();
                suggestion      .setFullName(user.getFullName());
                suggestion      .setPostDate(new Date());
                suggestion      .setPhoneNumber(user.getPhone());
                deleteMessageId = getComplaint();
                waitingType     = WaitingType.SET_COMPLAINT;
                return COMEBACK;
            case SET_COMPLAINT:
                deleteMessage(updateMessageId);
                deleteMessage(deleteMessageId);
                if (hasMessageText()) {
                    suggestion      .setText(updateMessageText);
                    Complaint complaint = new Complaint();
                    complaint.setText(suggestion.getText());
                    complaint.setFullName(suggestion.getFullName());
                    complaint.setPhoneNumber(suggestion.getFullName());
                    complaint.setPostDate(suggestion.getPostDate());
                    complaintRepository.save(complaint);
                    sendMessage(Const.COMPLAINT_DONE_MESSAGE);
                    return EXIT;
                } else {
                    wrongData();
                    getComplaint();
                }
                return COMEBACK;
        }
        return EXIT;
    }

    private int     getComplaint()  throws TelegramApiException {
        return botUtils.sendMessage(Const.COMPLAINT_SEND_MESSAGE, chatId);
    }

    private void wrongData()     throws TelegramApiException {
        botUtils.sendMessage(Const.WRONG_DATA_TEXT, chatId);
    }
}
