package com.telegrambot.command.impl;

import com.telegrambot.entity.enums.WaitingType;
import com.telegrambot.util.ButtonsLeaf;
import com.telegrambot.util.Const;
import com.telegrambot.util.DateKeyboard;
import com.telegrambot.command.Command;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class id038_ReportCourses extends Command {
    private DateKeyboard dateKeyboard;
    private Date start;
    private Date end;
    private ArrayList<String> list               = new ArrayList<>();
    private ButtonsLeaf buttonsLeaf;
    private long iin = 0;


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
                Arrays.asList("По ИИН;Все".split(Const.SPLIT)).forEach((e) -> list.add(e));
                buttonsLeaf = new ButtonsLeaf(list);
                toDeleteKeyboard(sendMessageWithKeyboard(getText(Const.CHOOSE_VARIANT_MESSAGE), buttonsLeaf.getListButton()));

                waitingType = WaitingType.CHOOSE_COURSES_VARIANT;
                return COMEBACK;

            case CHOOSE_COURSES_VARIANT:
                deleteMessage(updateMessageId);
                if(hasCallbackQuery()){
                    if (updateMessageText.equals("0")) {
                        sendMessage(getText(Const.SET_IIN_MESSAGE));
                        waitingType = WaitingType.SEND_IIN;
                    }
                    else if(updateMessageText.equals("1")){
                        deleteMessage(updateMessageId);
                        dateKeyboard = new DateKeyboard();
                        sendStartDate();
                        waitingType = WaitingType.START_DATE;
                    }
                    else{
                        toDeleteKeyboard(sendMessageWithKeyboard(getText(Const.COMMAND_NOT_FOUND), buttonsLeaf.getListButton()));
                    }
                }

                return COMEBACK;

            case SEND_IIN:
                deleteMessage(updateMessageId);
                try {
                    if (hasMessageText()) {
                        if(updateMessageText.length()==12) {
                            iin = Long.parseLong(updateMessageText);
                            dateKeyboard = new DateKeyboard();
                            sendStartDate();
                            waitingType = WaitingType.START_DATE;
                        }
                        else{
                            sendMessage(getText(Const.WRONG_DATA_TEXT));
                        }
                    }
                }
                catch (Exception e){
                    sendMessage(getText(Const.WRONG_DATA_TEXT));
                }
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
//                sendLightReport();
                return COMEBACK;
        }
        return EXIT;
    }

    private void sendStartDate() throws TelegramApiException {
//        toDeleteKeyboard(sendMessageWithKeyboard(sendLightReport() + getText(1055), dateKeyboard.getCalendarKeyboard()));
    }

    private void sendEndDate()   throws TelegramApiException {
        toDeleteKeyboard(sendMessageWithKeyboard(1056, dateKeyboard.getCalendarKeyboard()));
    }

//    private String  sendLightReport() { return String.format("Кол-во регистраций: %s ", registrationHandlingRepository.findAll().size()); }

    private void    sendReport()    throws TelegramApiException {
        int preview                             = sendMessage("Отчет подготавливается...");
//        CoursesReportService coursesService = new CoursesReportService(registrationCoursesRepository, userRepository, coursesNameRepository);
//        coursesService.sendCoursesReport(chatId, bot, start, end, preview, iin);
    }
}
