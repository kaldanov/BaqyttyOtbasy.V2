package com.telegrambot.command.impl;

import com.telegrambot.command.Command;
import com.telegrambot.entity.enums.WaitingType;
import com.telegrambot.service.SuggestionReportService;
import com.telegrambot.util.Const;
import com.telegrambot.util.DateKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Date;

public class
id016_ReportSuggestion extends Command {

    private DateKeyboard dateKeyboard;
    private Date start;
    private Date end;

    @Override
    public boolean  execute()       throws TelegramApiException {
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
                dateKeyboard = new DateKeyboard();
                sendStartDate();
                waitingType = WaitingType.START_DATE;
                return COMEBACK;
            case START_DATE:
                deleteMessage(updateMessageId);
                if (hasCallbackQuery()) {
                    if (dateKeyboard.isNext(updateMessageText)) {
                        sendStartDate();
                        return COMEBACK;
                    }
                    start = dateKeyboard.getDateDate(updateMessageText);
                    start.setHours(0);
                    start.setMinutes(0);
                    start.setSeconds(0);
                    sendEndDate();
                    waitingType = WaitingType.END_DATE;
                }
                return COMEBACK;
            case END_DATE:
                deleteMessage(updateMessageId);
                if (hasCallbackQuery()) {
                    if (dateKeyboard.isNext(updateMessageText)) {
                        sendStartDate();
                        return COMEBACK;
                    }
                    end = dateKeyboard.getDateDate(updateMessageText);
                    end.setHours(23);
                    end.setMinutes(59);
                    end.setSeconds(59);
                    sendReport();
                    waitingType = WaitingType.END_DATE;
                    return COMEBACK;
                }
                sendLightReport();
                return COMEBACK;
        }
        return EXIT;
    }

    private int     sendStartDate() throws TelegramApiException { return toDeleteKeyboard(sendMessageWithKeyboard(sendLightReport() + getText(1055), dateKeyboard.getCalendarKeyboard())); }

    private int     sendEndDate()   throws TelegramApiException { return toDeleteKeyboard(sendMessageWithKeyboard(1056, dateKeyboard.getCalendarKeyboard())); }

    private String  sendLightReport() { return String.format("Кол-во жалоб: %s ", suggestionRepository.findAll().size()); }

    private void    sendReport()    throws TelegramApiException {
        int preview                             = sendMessage("Отчет подготавливается...");
        SuggestionReportService reportService   = new SuggestionReportService(messageRepository, suggestionRepository);
        reportService.sendSuggestionReport(chatId, bot, start, end, preview);
    }
}
