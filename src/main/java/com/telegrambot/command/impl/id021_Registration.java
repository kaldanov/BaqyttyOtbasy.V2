package com.telegrambot.command.impl;

import com.telegrambot.command.Command;
import com.telegrambot.config.Bot;
import com.telegrambot.entity.User;
import com.telegrambot.entity.custom.Recipient;
import com.telegrambot.entity.enums.WaitingType;
import com.telegrambot.util.ButtonsLeaf;
import com.telegrambot.util.Const;
import com.telegrambot.util.UpdateUtil;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class id021_Registration extends Command {

    private User            user;
    private int             deleteMessageId;
    private int             secDeleteMessageId;
    private boolean once = false;
//    private RegistrationService registrationService = new RegistrationService(chatId);
    private List<String> list;
    private ButtonsLeaf buttonsLeaf;


    @Override
    public  boolean execute()   throws TelegramApiException {
        switch (waitingType) {
            case START:
                user = userRepository.findByChatId(chatId);
//                delete();
                if(isButton(96)){ // 96- cancel
                    sendMessage(35);
                    return EXIT;
                }
                if (isButton(107)){
                    once = true;
                }
//                if(isButton(1036)){ // re registr
//                    if (user != null && user.getIin() != null){
//                        Recipient recipient = recipientRepository.findByIin(user.getIin());
//                        if (recipient != null){
//                            recipient.setDistrict(new Bot().getTableSchema());
//                        }
//                    }
////                    if (user!= null){
////                        user.setEmail(new Bot().getTableSchema());
////                        userRepository.save(user);
////                        sendMessage(85);
////                    }
//                }
//                if (user != null && user.getIin() != null){
//                    Recipient recipient = recipientRepository.findByIin(user.getIin());
//
//                    if(recipient != null && recipient.getDistrict() != null && !recipient.getDistrict().equals(new Bot().getTableSchema()) && !once){
//                        deleteMessageId = sendMessageWithKeyboard(getText(Const.YOU_ALREADY_REGISTERED_MESSAGE), 53);
//                        return COMEBACK;
//                    }
////                    if (userRepository.findByChatId(chatId).getEmail() != null && !userRepository.findByChatId(chatId).getEmail().equals(new Bot().getTableSchema()) && !once){
////                        deleteMessageId = sendMessageWithKeyboard(getText(Const.YOU_ALREADY_REGISTERED_MESSAGE), 53);
////                        return COMEBACK;
////                    }
//                }
                if (!isRegistered()) {
                    delete();
                    user = new User();
                    user.setChatId(chatId);

                    deleteMessageId = sendMessage(Const.SET_FULL_NAME_MESSAGE, chatId);
                    waitingType = WaitingType.SET_FULL_NAME;
                    return COMEBACK;
                }
                else {
                    sendMessageWithAddition();
                    return EXIT;
                }
            case SET_FULL_NAME:

                delete();
                if (update.hasMessage() && update.getMessage().hasText() && update.getMessage().getText().length() <= 50) {
                    user.setFullName(update.getMessage().getText());
                    secDeleteMessageId = sendMessage(Const.SEND_CONTACT_MESSAGE, chatId);
                    waitingType = WaitingType.SET_PHONE_NUMBER;
                } else {
                    secDeleteMessageId = sendMessage(Const.WRONG_DATA_TEXT, chatId);
                    deleteMessageId = sendMessage(Const.SET_FULL_NAME_MESSAGE, chatId);
                }
                return COMEBACK;
            case SET_PHONE_NUMBER:
                delete();
                if (update.getMessage().hasContact()) {
                    String phone = update.getMessage().getContact().getPhoneNumber();

                    if (phone.charAt(0) == '8') {
                        phone = phone.replaceFirst("8", "+7");
                    } else if (phone.charAt(0) == '7') {
                        phone = phone.replaceFirst("7", "+7");
                    }

                    if (userRepository.findByPhone(phone) != null) {
                        user.setId(userRepository.findByPhone(phone).getId());
                    }

                    user.setPhone(phone);
                    user.setUserName(UpdateUtil.getFrom(update));
                    getIin();
                    waitingType = WaitingType.SET_IIN;

                    return COMEBACK;
                } else {
                    secDeleteMessageId = sendMessage(Const.WRONG_DATA_TEXT, chatId);
                    deleteMessageId = sendMessage(Const.SEND_CONTACT_MESSAGE, chatId);
                    return COMEBACK;
                }

            case SET_IIN:
                delete();
                if (update.getMessage().hasText() && isIin(update.getMessage().getText())) {
                    List<User> userList = userRepository.findAllByIin(update.getMessage().getText());
                    if (userList.size() != 0) {
                        sendMessage(getText(93), chatId);
                        getIin();
                        waitingType = WaitingType.SET_IIN;
                    } else {
                        user.setIin(update.getMessage().getText());
                        deleteMessageId = sendMessageWithKeyboard(String.format(getText(94) ,update.getMessage().getText()), 55);
                        waitingType = WaitingType.CONFIRM;
                    }
                } else {
                    wrongData();
                    getIin();
                    waitingType = WaitingType.SET_IIN;

                }
                return COMEBACK;
            case CONFIRM:
                if(update.hasCallbackQuery()){
                    if(isButton(89)){//confirm
                        userRepository.save(user);
                        getStatus();
                        waitingType = WaitingType.SET_STATUS;
                    }
                    else if(isButton(1005)){ //back
                        sendMessage(Const.SET_IIN_MESSAGE, chatId);
                        waitingType = WaitingType.SET_IIN;
                    }
                    else{
                        sendMessage(Const.WRONG_DATA_TEXT, chatId);
                        sendMessageWithKeyboard(getText(94), 55);
                    }

                }
                else{
                    deleteMessageId=  sendMessage(Const.WRONG_DATA_TEXT, chatId);
                    secDeleteMessageId = sendMessageWithKeyboard(getText(94), 55);
                }
                return COMEBACK;
            case SET_STATUS:
                delete();
                if (update.hasCallbackQuery()) {
                    if (list.get(Integer.parseInt(update.getCallbackQuery().getData())).equals(getText(Const.OTHERS_MESSAGE))) {
                        getOther();
                        waitingType = WaitingType.OTHER_STATUS;
                    } else {

                        user.setStatus(list.get(Integer.parseInt(update.getCallbackQuery().getData())));
//                        user.setEmail(new Bot().getTableSchema());
                        userRepository.save(user);
                        sendMessage(4, chatId);
                        return EXIT;
                    }
                } else {
                    wrongData();
                    getStatus();
                }
                return COMEBACK;
            case OTHER_STATUS:
                delete();
                if (update.hasMessage() && update.getMessage().hasText()) {
                    user.setStatus(update.getMessage().getText());
//                    user.setEmail(new Bot().getTableSchema());
                    userRepository.save(user);
                    sendMessage(4, chatId);
                    return EXIT;
                } else {
                    wrongData();
                    getOther();
                }
                return COMEBACK;
        }
        return EXIT;
    }

    private void getStatus() throws TelegramApiException {
        list = new ArrayList<>();
        Arrays.asList(getText(Const.STATUS_TYPE_MESSAGE).split(Const.SPLIT)).forEach((e) -> list.add(e));
        list.add(getText(Const.OTHERS_MESSAGE));
        buttonsLeaf = new ButtonsLeaf(list);
        sendMessageWithKeyboard(getText(Const.STATUS_MESSAGE), buttonsLeaf.getListButton(), chatId);
    }

    private void wrongData() throws TelegramApiException {
        sendMessage(Const.WRONG_DATA_TEXT, chatId);
    }

    private boolean isIin(String text) {
        try {
            Long.parseLong(text);
            return text.length() == 12;
        } catch (Exception e) {
            return false;
        }
    }

    private int getIin() throws TelegramApiException {
        return sendMessage(Const.SET_IIN_MESSAGE, chatId);
    }

    private void    delete() {
        deleteMessage(updateMessageId);
        deleteMessage(deleteMessageId);
        deleteMessage(secDeleteMessageId);
    }
    private int getOther() throws TelegramApiException {
        return sendMessage(Const.SET_YOUR_OPTION_MESSAGE, chatId);
    }
}
