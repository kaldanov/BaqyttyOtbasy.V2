package com.telegrambot.command.impl;

import com.telegrambot.entity.enums.WaitingType;
import com.telegrambot.service.ComplaintReportService;
import com.telegrambot.util.Const;
import com.telegrambot.util.DateKeyboard;
import com.telegrambot.command.Command;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Date;

public class id024_ReportComplaint extends Command {

    private DateKeyboard dateKeyboard;
    private Date            start;
    private Date            end;

    @Override
    public boolean  execute()       throws TelegramApiException {
        if (!isAdmin() && !isMainAdmin()) {
            sendMessage(Const.NO_ACCESS);
            return EXIT;
        }
        switch (waitingType) {
            case START:
                if(userRepository.findByChatId(chatId) == null){
                    sendMessageWithKeyboard("Вы не прошли регистрацию" , 57);
                    return EXIT;
                }
                deleteMessage(updateMessageId);
                dateKeyboard    = new DateKeyboard();
                sendStartDate();
                waitingType     = WaitingType.START_DATE;
                return COMEBACK;
            case START_DATE:
                deleteMessage(updateMessageId);
                if (hasCallbackQuery()) {
                    if (dateKeyboard.isNext(updateMessageText)) {
                        sendStartDate();
                    } else {
                        start           = dateKeyboard.getDateDate(updateMessageText);
//                        start           .setHours(0);
//                        start           .setMinutes(0);
//                        start           .setSeconds(0);
                        sendEndDate();
                        waitingType     = WaitingType.END_DATE;
                    }
                }
                return COMEBACK;
            case END_DATE:
                deleteMessage(updateMessageId);
                if (hasCallbackQuery()) {
                    if (dateKeyboard.isNext(updateMessageText)) {
                        sendStartDate();
                    } else {
                        end     = dateKeyboard.getDateDate(updateMessageText);
//                        end     .setHours(23);
//                        end     .setMinutes(59);
//                        end     .setSeconds(59);
                        sendReport();
                        waitingType = WaitingType.END_DATE;
                    }
                    return COMEBACK;
                }
                sendLightReport();
                return COMEBACK;
        }
        return EXIT;
    }

    private void sendStartDate() throws TelegramApiException {
        toDeleteKeyboard(sendMessageWithKeyboard(sendLightReport() + getText(Const.SELECT_START_DATE_MESSAGE), dateKeyboard.getCalendarKeyboard()));
    }

    private void sendEndDate()   throws TelegramApiException {
        toDeleteKeyboard(sendMessageWithKeyboard(Const.SELECT_END_DATE_MESSAGE, dateKeyboard.getCalendarKeyboard()));
    }

    private void    sendReport()    throws TelegramApiException {
        int preview                             = sendMessage(getText(Const.REPORT_DOING_MESSAGE));
        ComplaintReportService reportService    = new ComplaintReportService(messageRepository, complaintRepository);
        reportService.sendSuggestionReport(chatId, bot, start, end, preview);
    }

    private String  sendLightReport() {
        return String.format(getText(Const.COMPLAINT_COUNT_MESSAGE), suggestionRepository.findAll().size());
    }
}
