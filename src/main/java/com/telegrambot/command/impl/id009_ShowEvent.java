package com.telegrambot.command.impl;

import com.telegrambot.command.Command;
import com.telegrambot.entity.custom.Event;
import com.telegrambot.entity.custom.RegistrationEvent;
import com.telegrambot.entity.enums.WaitingType;
import com.telegrambot.util.ButtonsLeaf;
import com.telegrambot.util.Const;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class id009_ShowEvent extends Command {

    private List<Event>         events;
    private Event               event;
    private ButtonsLeaf         buttonsLeaf;
    private int                 deleteMessageId;
    private int                 secondDeleteMessageId;
    private RegistrationEvent registrationEvent;

    @Override
    public boolean  execute()   throws TelegramApiException {

        switch (waitingType) {
            case START:
                if(userRepository.findByChatId(chatId) == null){
                    sendMessageWithKeyboard("Вы не прошли регистрацию" , 57);
                    return EXIT;
                }

                deleteMessage(updateMessageId);
                events              = eventRepository.findAllByHiderIsFalseOrderById();
                if (events == null || events.size() == 0) {
                    deleteMessageId = sendMessage(Const.ACTION_EVENT_EMPTY);
                    return EXIT;
                }
                List<String>  list  = new ArrayList<>();
                events.forEach((e)  -> list.add(e.getName()));
                buttonsLeaf         = new ButtonsLeaf(list);
                toDeleteKeyboard(sendMessageWithKeyboard(getText(Const.ACTIVE_EVENT_MESSAGE), buttonsLeaf.getListButton()));
                waitingType         = WaitingType.EVENT_SELECTION;
                return COMEBACK;
            case EVENT_SELECTION:
                delete();
                if (hasCallbackQuery()) {
                    event                       = events.get(Integer.parseInt(updateMessageText));
                    String formatMessage        = getText(Const.EVENT_INFORMATION_MESSAGE);
                    String result               = String.format(formatMessage, event.getName(), event.getText());
                    if (event.getPhoto() != null) {
                        secondDeleteMessageId   = bot.execute(new SendPhoto().setChatId(chatId).setPhoto(event.getPhoto())).getMessageId();
                    }
                    deleteMessageId             = sendMessageWithKeyboard(result, Const.JOIN_EVENT_KEYBOARD);
                    waitingType                 = WaitingType.EVENT;
                } else {
                    secondDeleteMessageId       = wrongData();
                    toDeleteKeyboard(sendMessageWithKeyboard(getText(Const.ACTIVE_EVENT_MESSAGE), buttonsLeaf.getListButton()));
                }
                return COMEBACK;
            case EVENT:
                delete();
                if (hasCallbackQuery()) {
                    if (isButton(Const.JOIN_EVENT_BUTTON)) {
                        if (!registrationEventRepository.findByChatIdAndEventIdOrderById(chatId, event.getId())) {
                            registrationEvent   = new RegistrationEvent();
                            registrationEvent   .setChatId(chatId);
                            registrationEvent   .setEventId(event.getId());
                            registrationEvent   .setRegistrationDate(new Date());
                            registrationEvent   .setCome(false);
                            registrationEventRepository.save(registrationEvent);
                            deleteMessageId     = done();
                            toDeleteKeyboard(sendMessageWithKeyboard(getText(Const.ACTIVE_EVENT_MESSAGE), buttonsLeaf.getListButton()));
                        } else {
                            deleteMessageId     = sendMessage(Const.YOU_REGISTERED_MESSAGE);
                            events              = eventRepository.findAllByHiderIsFalseOrderById();
                            if (events == null || events.size() == 0) {
                                deleteMessageId = sendMessage(Const.ACTION_EVENT_EMPTY);
                                return EXIT;
                            }
                            List<String> backList   = new ArrayList<>();
                            events.forEach((e)      -> backList.add(e.getName()));
                            buttonsLeaf             = new ButtonsLeaf(backList);
                            toDeleteKeyboard(sendMessageWithKeyboard(getText(Const.ACTIVE_EVENT_MESSAGE), buttonsLeaf.getListButton()));
                            waitingType             = WaitingType.EVENT_SELECTION;
                        }
                    } else if (isButton(Const.BACK_BUTTON)) {
                        events              = eventRepository.findAllByHiderIsFalseOrderById();
                        if (events == null || events.size() == 0) {
                            deleteMessageId = sendMessage(Const.ACTION_EVENT_EMPTY);
                            return EXIT;
                        }
                        List<String> backList   = new ArrayList<>();
                        events.forEach((e)      -> backList.add(e.getName()));
                        buttonsLeaf             = new ButtonsLeaf(backList);
                        toDeleteKeyboard(sendMessageWithKeyboard(getText(Const.ACTIVE_EVENT_MESSAGE), buttonsLeaf.getListButton()));
                        waitingType             = WaitingType.EVENT_SELECTION;
                    }
                } else {
                    String formatMessage        = getText(Const.EVENT_INFORMATION_MESSAGE);
                    String result               = String.format(formatMessage, event.getName(), event.getText());
                    if (event.getPhoto()       != null) {
                        secondDeleteMessageId   = bot.execute(new SendPhoto().setChatId(chatId).setPhoto(event.getPhoto())).getMessageId();
                    }
                    deleteMessageId             = sendMessageWithKeyboard(result, Const.JOIN_EVENT_KEYBOARD);
                }
                return COMEBACK;
        }
        return EXIT;
    }

    private int     wrongData() throws TelegramApiException { return botUtils.sendMessage(Const.WRONG_DATA_TEXT, chatId); }

    private int     done()      throws TelegramApiException { return botUtils.sendMessage(Const.DONE_JOIN_MESSAGE, chatId); }

    private void    delete() {
        deleteMessage(updateMessageId);
        deleteMessage(deleteMessageId);
        deleteMessage(secondDeleteMessageId);
    }
}
