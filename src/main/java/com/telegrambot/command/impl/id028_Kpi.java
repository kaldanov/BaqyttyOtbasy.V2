package com.telegrambot.command.impl;

import com.telegrambot.entity.custom.Kpi;
import com.telegrambot.entity.custom.Registration_Service;
import com.telegrambot.entity.enums.WaitingType;
import com.telegrambot.util.ButtonsLeaf;
import com.telegrambot.util.Const;
import com.telegrambot.util.DateKeyboard;
import com.telegrambot.command.Command;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class id028_Kpi extends Command {

    private DateKeyboard dateKeyboard;
    private List<String>            list = new ArrayList<>();
    private List<String>            handlingList;
    private String                  handling;
    private Kpi kpi = new Kpi();
    private Registration_Service registration_service;
    private int                     deleteMessageId;

    @Override
    public boolean execute() throws TelegramApiException {
        if (!isAdmin() && !isMainAdmin()) {
            sendMessage(Const.NO_ACCESS);
            return EXIT;
        }
        switch (waitingType) {
            case START:
                deleteMessage(updateMessageId);
                handlingList = Arrays.asList(getText(Const.COUNT_HANDLING_TYPE_MESSAGE).split(Const.SPLIT));
                list.addAll(handlingList);
                ButtonsLeaf buttonsLeaf = new ButtonsLeaf(list);
                toDeleteKeyboard(sendMessageWithKeyboard(getText(Const.KPI_MESSAGE), buttonsLeaf.getListButton()));
                waitingType  = WaitingType.SET_HANDLING;
                return COMEBACK;
            case SET_HANDLING:
                deleteMessage(updateMessageId);
                if (hasCallbackQuery()) {
                    handling = handlingList.get(Integer.parseInt(updateMessageText));
                    kpi.setKpiType(handling);
                    deleteMessageId      = sendMessage(Const.SEND_IIN_FROM_ADMIN_MESSAGE);
                    waitingType          = WaitingType.SET_IIN;
                }
                return COMEBACK;
            case SET_IIN:
                deleteMessage(updateMessageId);
                if (hasMessageText() && update.getMessage().getText().length() == 12) {
                    deleteMessage(updateMessageId);
                    dateKeyboard    = new DateKeyboard();
                    sendDate();
                    kpi.setIIN(update.getMessage().getText());
                    waitingType = WaitingType.SET_DATE;
                }
                else {
                    sendMessage(1018);
                }
                return COMEBACK;
            case SET_DATE:
                if (hasCallbackQuery()) {
                    if (dateKeyboard.isNext(updateMessageText)) {
                        sendDate();
                        return COMEBACK;
                    }
                    Date date = dateKeyboard.getDateDate(updateMessageText);
                    date.setHours(0);
                    date.setMinutes(0);
                    date.setMinutes(0);
                    kpi.setDate(date);
                    kpiRepository.save(kpi);
                    sendMessage(1017);
                    return EXIT;
                }

                return COMEBACK;
        }
        return EXIT;
    }

    private void sendDate() throws TelegramApiException {
        toDeleteKeyboard(sendMessageWithKeyboard(getText(1167), dateKeyboard.getCalendarKeyboard()));
    }

    private void switchType() throws TelegramApiException {
        if ("Гранты".equals(handling)) {
//            registrationHandling = new RegistrationHandling();
            deleteMessageId = sendMessage(Const.SEND_IIN_FROM_ADMIN_MESSAGE);
            waitingType = WaitingType.SET_IIN;
        }
    }

}
