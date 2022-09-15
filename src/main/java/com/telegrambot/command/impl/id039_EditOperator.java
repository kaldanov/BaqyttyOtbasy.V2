package com.telegrambot.command.impl;

import com.telegrambot.entity.Operator;
import com.telegrambot.entity.User;
import com.telegrambot.util.Const;
import com.telegrambot.util.DateUtil;
import com.telegrambot.command.Command;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
public class id039_EditOperator extends Command {

    private StringBuilder text;
    private List<Long> allOperators = new ArrayList<>();
    private int           message;
    private static String delete;
    private static String deleteIcon;
    private static String showIcon;
    public  int thirdDeleteMessId;



    @Override
    public boolean execute() throws TelegramApiException {
        if(userRepository.findByChatId(chatId) == null){
            sendMessageWithKeyboard("Вы не прошли регистрацию" , 57);
            return EXIT;
        }

        if (!isAdmin()) {
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
            return registerNewOperator();
//            return COMEBACK;
        }
        if(updateMessageText.contains(delete)) {
            try {
                if (allOperators.size() > 1) {
                    int numberAdminList = Integer.parseInt(updateMessageText.replaceAll("[^0-9]",""));
                    operatorRepository.delete(operatorRepository.findByUserId(allOperators.get(numberAdminList)));
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        sendEditorAdmin();

        return COMEBACK;
    }
    private boolean registerNewOperator()  throws TelegramApiException {
        deleteMessage(updateMessageId);
        String phone = update.getMessage().getContact().getPhoneNumber();
        if(phone.charAt(0) == '8')
            phone = phone.replaceFirst("8" , "+7");
        else if(phone.charAt(0) == '7')
            phone = phone.replaceFirst("7", "+7");


        try {
            if (userRepository.findByPhone(phone) == null) {
                deleteMessage(thirdDeleteMessId);
                thirdDeleteMessId = sendMessage("Пользователь не зарегестрирован в данном боте!\nОтправьте зарегистрированного пользователя!");
            }
            else {
                User newOper = userRepository.findByPhone(phone);
                if (operatorRepository.findByUserId(newOper.getChatId()) != null) {
                    sendMessage("Пользователь уже оператор!\nОтправьте другого пользователя!");
                }

                else {
                    Operator operator = new Operator();
                    operator.setUserId(newOper.getChatId());
                    operator.setComment(String.format("%s %s %s", newOper.getUserName(), newOper.getPhone(), DateUtil.getDbMmYyyyHhMmSs(new Date())));
                    operatorRepository.save(operator);
                    //operatorDao.addAssistant(newOperChatId, String.format("%s %s %s", user.getUserName(), user.getPhone(), DateUtil.getDbMmYyyyHhMmSs(new Date())));
                    User userOper = userRepository.findByChatId(chatId);
                    log.info("{} added new admin - {} ", getInfoByUser(userOper), getInfoByUser(newOper));
                    sendEditorAdmin();
                }

            }
        }
        catch (Exception e){
            e.printStackTrace();
            sendMessage("Пользователь ранее удалил аккаунт, из-за этого в базе 2 экземпляра этого аккаунта!");
        }

        return COMEBACK;
    }
    private String  getInfoByUser(User user) { return String.format("%s %s %s", user.getFullName(), user.getPhone(), user.getChatId()); }

    private void    sendEditorAdmin()   throws TelegramApiException {
        deleteMessage(updateMessageId);
        try {
            getText(EXIT);
            message = sendMessage(String.format(getText(1175), text.toString()));
        } catch (TelegramApiException e) {
            getText(COMEBACK);
            message = sendMessage(String.format(getText(1175), text.toString()));
        }
        toDeleteMessage(message);
    }

    private void    getText(boolean withLink) {
        allOperators = new ArrayList<>();
        text        = new StringBuilder();
        operatorRepository.findAll().forEach(operator -> {
            allOperators.add(operator.getUserId());
        });
        //allOperators   = operatorRepository.findAll();
        int count   = 0;
        for (Long admin : allOperators) {
            try {
                User user = userRepository.findByChatId(admin);
                if (allOperators.size() == 1) {
                    if (withLink) {
                        text.append(getLinkForUser(user.getChatId(), user.getUserName())).append(space).append(next);
                    } else {
                        text.append(getInfoByUser(user)).append(space).append(next);
                    }
                    text.append("Должен быть минимум еще 1 оператор.").append(next);
                } else {
                    if (withLink) {
                        text.append(delete).append(count).append(deleteIcon).append(" - ").append(showIcon).append(getLinkForUser(user.getChatId(), user.getUserName())).append(space).append(next);
                    } else {
                        text.append(delete).append(count).append(deleteIcon).append(" - ").append(getInfoByUser(user)).append(space).append(next);
                    }
                }
                count++;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
