package com.telegrambot.command.impl;

import com.telegrambot.command.Command;
import com.telegrambot.entity.Admin;
import com.telegrambot.entity.User;
import com.telegrambot.util.Const;
import com.telegrambot.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
public class id015_EditAdmin extends Command {

    private StringBuilder text;
    private List<Long>    allAdmins = new ArrayList<>();
    private int           message;
    private static String delete;
    private static String deleteIcon;
    private static String showIcon;
    public  int thirdDeleteMessId;


    @Override
    public  boolean execute()           throws TelegramApiException {
        if(userRepository.findByChatId(chatId) == null){
            sendMessageWithKeyboard("Вы не прошли регистрацию" , 57);
            return EXIT;
        }
        if (!isAdmin() && !isMainAdmin()) {
            sendMessage(Const.NO_ACCESS);
            return EXIT;
        }
        if (deleteIcon == null) {
            deleteIcon  = getText(1051);
            showIcon    = getText(1052);
            delete      = getText(1053);
        }
        if (message != 0) deleteMessage(message);
        if (hasContact()) {
            registerNewAdmin();
            return COMEBACK;
        }
        if(updateMessageText.contains(delete)) {
            try {
                if (allAdmins.size() > 1) {
                    int numberAdminList = Integer.parseInt(updateMessageText.replaceAll("[^0-9]",""));
                    adminRepository.delete(adminRepository.findByUserId(allAdmins.get(numberAdminList)));
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        sendEditorAdmin();
        return COMEBACK;
    }

    private boolean registerNewAdmin()  throws TelegramApiException {
        deleteUpdateMess();
        String phone = update.getMessage().getContact().getPhoneNumber();
        if(phone.charAt(0) == '8')
            phone = phone.replaceFirst("8" , "+7");
        else if(phone.charAt(0) == '7')
            phone = phone.replaceFirst("7" , "+7");



        if (userRepository.findByPhone(phone) == null){
            deleteMessage(thirdDeleteMessId);
            thirdDeleteMessId = sendMessage("Пользователь не зарегестрирован в данном боте!\nОтправьте зарегистрированного пользователя!");
        }


        else {
            long newAdminChatId = userRepository.findByPhone(phone).getChatId();
            if (adminRepository.countByUserId(newAdminChatId) > 0) {
                sendMessage("Пользователь уже администратор");
                return EXIT;
            }
            else {
                User user = userRepository.findByChatId(newAdminChatId);
                Admin admin = new Admin();
                admin.setUserId(newAdminChatId);
                admin.setComment(String.format("%s %s %s", user.getUserName(), user.getPhone(), DateUtil.getDbMmYyyyHhMmSs(new Date())));
                adminRepository.save(admin);
                User userAdmin = userRepository.findByChatId(chatId);
                log.info("{} added new admin - {} ", getInfoByUser(userAdmin), getInfoByUser(user));
                sendEditorAdmin();
            }

        }
        return COMEBACK;
    }

    private String  getInfoByUser(User user) { return String.format("%s %s %s", user.getFullName(), user.getPhone(), user.getChatId()); }

    private void    sendEditorAdmin()   throws TelegramApiException {
        deleteMessage(updateMessageId);
        try {
            getText(EXIT);
            message = sendMessage(String.format(getText(1054), text.toString()));
        } catch (TelegramApiException e) {
            getText(COMEBACK);
            message = sendMessage(String.format(getText(1054), text.toString()));
        }
        toDeleteMessage(message);
    }

    private void    getText(boolean withLink) {
        text        = new StringBuilder();
        allAdmins = new ArrayList<>();
        //allAdmins   = adminDao.getAll();
        adminRepository.findAllByOrderById().forEach(admin -> {
            allAdmins.add(admin.getUserId());
        });
        int count   = 0;
        for (Long admin : allAdmins) {
            try {
                User user = userRepository.findByChatId(admin);
                if (allAdmins.size() == 1) {
                    if (withLink) {
                        text.append(getLinkForUser(user.getChatId(), user.getUserName())).append(space).append(next);
                    }
                    else {
                        text.append(getInfoByUser(user)).append(space).append(next);
                    }
                    text.append("Должен быть минимум 1 администратор.").append(next);
                }
                else {
                    if (withLink) {
                        text.append(delete).append(count).append(deleteIcon).append(" - ").append(showIcon).append(getLinkForUser(user.getChatId(), user.getUserName())).append(space).append(next);
                    }
                    else {
                        text.append(delete).append(count).append(deleteIcon).append(" - ").append(getInfoByUser(user)).append(space).append(next);
                    }
                }
                count++;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private void deleteMess(){
        deleteMessage(thirdDeleteMessId);
    }
    private void deleteUpdateMess(){
        deleteMessage(updateMessageId);
    }
}
