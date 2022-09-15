package com.telegrambot.command.impl;

import com.telegrambot.entity.custom.ReminderTask;
import com.telegrambot.entity.enums.WaitingType;
import com.telegrambot.util.Const;
import com.telegrambot.util.DateKeyboard;
import com.telegrambot.command.Command;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Date;
import java.util.List;

public class id029_Reminder extends Command {

    private List<ReminderTask>  reminderTaskList;
    private int                 deleteMessageId;
    private long                 reminderTaskId;
    private DateKeyboard dateKeyboard;
    private boolean             isUpdate = false;
    private ReminderTask        reminderTask;

    @Override
    public boolean  execute()               throws TelegramApiException {
        if(userRepository.findByChatId(chatId) == null){
            sendMessageWithKeyboard("Вы не прошли регистрацию" , 57);
            return EXIT;
        }
        if (!isAdmin() && !isMainAdmin()) {
            sendMessage(Const.NO_ACCESS);
            return EXIT;
        }
        switch (waitingType) {
            case START:
                deleteMessage(updateMessageId);
                sendListReminder();
                waitingType = WaitingType.CHOOSE_OPTION;
                return COMEBACK;
            case CHOOSE_OPTION:
                deleteMessage(deleteMessageId);
                deleteMessage(updateMessageId);
                if (hasMessageText()) {
                    if (isCommand("/new")) {
                        dateKeyboard = new DateKeyboard();
                        sendStartDate();
                        waitingType  = WaitingType.START_DATE;
                    } else if (isCommand("/del")) {
                        reminderTaskId   = reminderTaskList.get(Integer.parseInt(updateMessageText.replaceAll("[^0-9]",""))).getId();
                        reminderTaskRepository.delete(reminderTaskRepository.findById(reminderTaskId));
                        sendListReminder();
                        waitingType = WaitingType.CHOOSE_OPTION;
                    } else if (isCommand("/st")) {
                        reminderTaskId   = reminderTaskList.get(Integer.parseInt(updateMessageText.replaceAll("[^0-9]",""))).getId();
                        dateKeyboard = new DateKeyboard();
                        sendStartDate();
                        isUpdate = true;
                        waitingType = WaitingType.START_DATE;
                    }
                }
                return COMEBACK;
            case START_DATE:
                deleteMessage(updateMessageId);
                if (hasCallbackQuery()) {
                    if (dateKeyboard.isNext(updateMessageText)) {
                        sendStartDate();
                        return COMEBACK;
                    }
                    Date dateStart = dateKeyboard.getDateDate(updateMessageText);
                    dateStart.setHours(0);
                    dateStart.setMinutes(0);
                    dateStart.setSeconds(0);
                    if (isUpdate) {
                        reminderTask = reminderTaskRepository.findById(reminderTaskId);
                    } else {
                        reminderTask = new ReminderTask();
                    }
                    reminderTask.setDateBegin(dateStart);
                    sendMessage(Const.SEND_MESSAGE_TEXT_MESSAGE);
                    waitingType = WaitingType.SET_TEXT;
                }
                return COMEBACK;
            case SET_TEXT:
                deleteMessage(updateMessageId);
                if (hasMessageText()) {
                    reminderTask.setText(updateMessageText);
                    if (isUpdate) {
                        reminderTaskRepository.save(reminderTask);
                        sendMessageToGroup();
                    } else {
                        reminderTaskRepository.save(reminderTask);
                        sendMessageToGroup();
                    }
                    sendListReminder();
                    waitingType = WaitingType.CHOOSE_OPTION;
                }
                return COMEBACK;
        }
        return EXIT;
    }

    private void    sendListReminder()      throws TelegramApiException {
        String formatMessage            = getText(Const.REMINDER_EDIT_MESSAGE);
        StringBuilder stringBuilder     = new StringBuilder();
        reminderTaskList                = reminderTaskRepository.findAll();
        String format                   = getText(Const.REMINDER_EDIT_SINGLE_MESSAGE);
        for (int i = 0; i < reminderTaskList.size(); i++) {
            ReminderTask reminderTask   = reminderTaskList.get(i);
            stringBuilder.append(String.format(format, "/del" + i, "/st" + i, reminderTask.getText())).append(next);
        }
        deleteMessageId                 = sendMessage(String.format(formatMessage, stringBuilder.toString(), "/new"));
    }

    private void sendStartDate()         throws TelegramApiException {
        toDeleteKeyboard(sendMessageWithKeyboard(getText(Const.SELECT_START_DATE_MESSAGE), dateKeyboard.getCalendarKeyboard()));
    }

    private void    sendMessageToGroup()    throws TelegramApiException {
            long groupChatId = groupRepository.findById(Integer.parseInt(propertiesRepository.findById(3).getValue())).getChatId();
            sendMessage(reminderTask.getText(), groupChatId);
    }

    private boolean isCommand(String command) { return updateMessageText.startsWith(command); }
}
