package com.telegrambot.command.impl;

import com.telegrambot.command.Command;
import com.telegrambot.entity.User;
import com.telegrambot.entity.custom.Event;
import com.telegrambot.entity.custom.Recipient;
import com.telegrambot.entity.enums.WaitingType;
import com.telegrambot.util.Const;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

public class id019_EditEvent extends Command {

    private List<Event> events;
    private Event       event;
    private int         deleteMessageId;
    private long         eventId;

    @Override
    public boolean  execute()               throws TelegramApiException {
        if(userRepository.findByChatId(chatId) == null){
            sendMessageWithKeyboard("Вы не прошли регистрацию" , 57);
            return EXIT;
        }
//        if (chatId == 766856789){
//            List<Recipient> all = recipientRepository.findAllByOrderById();
//            for (Recipient recipient : all){
//                if (recipient.getIin().length() != 12){
//                    recipientRepository.delete(recipient);
//                }
//
//            }
//
//            List<User> users = userRepository.findAll();
//            for (User user : users){
//                if (user.getIin() != null && user.getEmail() != null){
//                    Recipient recipient = recipientRepository.findByIin(user.getIin());
//                    if (recipient != null){
//                        if (recipient.getDistrict() == null || recipient.getDistrict().equals("")){
//                            recipient.setDistrict(user.getEmail());
//                            recipientRepository.save(recipient);
//                        }
//                    }
//                }
//            }
//
//
//        }
        if (!isAdmin() && !isMainAdmin()) {
            sendMessage(Const.NO_ACCESS);
            return EXIT;
        }
        switch (waitingType) {
            case START:
                deleteMessage(updateMessageId);
                sendEvent();
                waitingType = WaitingType.CHOOSE_EVENT;
                return COMEBACK;
            case CHOOSE_EVENT:
                deleteMessage(deleteMessageId);
                deleteMessage(updateMessageId);
                if (hasMessageText()) {
                    if (isCommand(getText(Const.SLASH_DELETE_MESSAGE))) {
                        eventId     = events.get(getInt()).getId();
                        eventRepository.delete(eventRepository.findById(eventId));
                        sendEvent();
                    } else if (isCommand(getText(Const.SLASH_NEW_MESSAGE))) {
                        deleteMessageId = sendMessage(Const.NEW_NAME_FOR_EVENT_MESSAGE);
                        waitingType     = WaitingType.NEW_EVENT;
                    } else if (isCommand(getText(Const.SLASH_SETTING_MESSAGE))) {
                        event       = events.get(getInt());
                        event       .setHide(!event.isHide());
                        eventRepository.save(event);
                        sendEvent();
                    }
                }
                return COMEBACK;
            case NEW_EVENT:
                deleteMessage(updateMessageId);
                deleteMessage(deleteMessageId);
                if (hasMessageText()) {
                    event           = new Event();
                    event           .setName(updateMessageText);
                    deleteMessageId = sendMessage(Const.SEND_PHOTO_OR_IMG_EVENT_MESSAGE);
                    waitingType     = WaitingType.SET_PHOTO;
                }
                return COMEBACK;
            case SET_PHOTO:
                deleteMessage(updateMessageId);
                deleteMessage(deleteMessageId);
                if (hasPhoto()) {
                    event           .setPhoto(updateMessagePhoto);
                    deleteMessageId = sendMessage(Const.SEND_INFO_EVENT_MESSAGE);
                    waitingType     = WaitingType.SET_TEXT_EVENT;
                }
                return COMEBACK;
            case SET_TEXT_EVENT:
                deleteMessage(updateMessageId);
                deleteMessage(deleteMessageId);
                if (hasMessageText()) {
                    event   .setText(updateMessageText);
                    event   .setHide(false);
                    eventRepository.save(event);
                    toDeleteKeyboard(sendMessageWithKeyboard(getText(Const.PRESS_SHARE_TO_GROUP_MESSAGE), Const.EVENT_KEYBOARD));
                    waitingType = WaitingType.CHOOSE_OPTION;
                }
                return COMEBACK;
            case CHOOSE_OPTION:
                deleteMessage(updateMessageId);
                deleteMessage(deleteMessageId);
                if (hasCallbackQuery()) {
                    if (isButton(Const.SHARE_BUTTON)) {
                        sendMessageToGroup();
                        sendEvent();
                        waitingType = WaitingType.CHOOSE_EVENT;
                    } else if (isButton(Const.DONE_BUTTON)) {
                        sendEvent();
                        waitingType = WaitingType.CHOOSE_EVENT;
                    }
                }
                return COMEBACK;
        }
        return EXIT;
    }

    private int     getInt() {
        return Integer.parseInt(updateMessageText.replaceAll("[^0-9]", ""));
    }

    private void    sendEvent()             throws TelegramApiException {
        String formatMessage        = getText(Const.EVENT_INFO_MESSAGE);
        StringBuilder infoByEvent   = new StringBuilder();
        events                      = eventRepository.findAll();
        String format               = getText(Const.EVENT_EDIT_MESSAGE);
        for (int i = 0; i < events.size(); i++) {
            event       = events.get(i);
            infoByEvent.append(String.format(format,getText(Const.SLASH_DELETE_MESSAGE) + i, getText(Const.SLASH_SETTING_MESSAGE) + i, event.isHide() ? "❌" : "✅" , event.getName())).append(next);
        }
        deleteMessageId             = sendMessage(String.format(formatMessage, infoByEvent.toString(), getText(Const.SLASH_NEW_MESSAGE)));
    }

    private boolean isCommand(String command) { return updateMessageText.startsWith(command); }

    private void    sendMessageToGroup()    throws TelegramApiException {
        StringBuilder messageToGroup = new StringBuilder();
        messageToGroup.append("Мероприятие : ").append(event.getName()).append(next);
        messageToGroup.append("Информация : ").append(next);
        messageToGroup.append(event.getText());
        if (event.getPhoto() != null) bot.execute(new SendPhoto().setChatId(groupRepository.findById(Integer.parseInt(propertiesRepository.findById(Const.GROUP_ID_FROM_PROPERTIES).getValue())).getChatId()).setPhoto(event.getPhoto()));
        bot.execute(new SendMessage().setChatId(groupRepository.findById(Integer.parseInt(propertiesRepository.findById(Const.GROUP_ID_FROM_PROPERTIES).getValue())).getChatId()).setText(messageToGroup.toString()));
    }
}
