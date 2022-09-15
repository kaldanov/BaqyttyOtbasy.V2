package com.telegrambot.command.impl;

import com.telegrambot.command.Command;
import com.telegrambot.service.SurveyReportService;
import com.telegrambot.util.Const;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class id017_ReportSurvey extends Command {

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
        deleteMessage(updateMessageId);
        sendReport();
        return EXIT;
    }

    private void    sendReport()    throws TelegramApiException {
        int preview                         = sendMessage("Список подготавливается...");
        SurveyReportService reportService   = new SurveyReportService(messageRepository, questionRepository, surveyAnswerRepository, questMessageRepository);
        reportService.sendSurveyReport(chatId, bot, preview);
    }
}
