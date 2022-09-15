package com.telegrambot.command.impl;

import com.telegrambot.command.Command;
import com.telegrambot.entity.enums.WaitingType;
import com.telegrambot.service.RecipientZalivkaService;
import com.telegrambot.service.SverkaService;
import com.telegrambot.util.ButtonsLeaf;
import com.telegrambot.util.Const;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class id055_Sverka extends Command {
    private int delMess;
    private ButtonsLeaf fileOrDB;
    int fileDB = -1;

    @Override
    public boolean execute() throws SQLException, TelegramApiException {
        if (!isAdmin()) {
            sendMessage(Const.NO_ACCESS);
            return EXIT;
        }

        switch (waitingType){
            case START:
                deleteMessage(updateMessageId);
                List<String> names = new ArrayList<>();
                names.add("Сверка");
                names.add("Заполнить базу");
                fileOrDB = new ButtonsLeaf(names);

                sendMessageWithKeyboard("Выберите, что хотите сделать?", fileOrDB.getListButton());
                waitingType = WaitingType.SET_FILE_DB;
                return COMEBACK;
            case SET_FILE_DB:
                deleteMessage(updateMessageId);
                FileInputStream shablon = null;
                try {
                    shablon = new FileInputStream(new File(propertiesRepository.findById(8).getValue()));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                if (updateMessageText.equals("0")){ // сверка
                    fileDB = 0;
                    delMess = bot.execute(new SendDocument().setChatId(chatId).setCaption("Отправьте файл в формате Excel...(вам предоставлен шаблон базы)").setDocument("Шаблон базы.xlsx", shablon)).getMessageId();
//                    delMess =  sendMessage("Отправьте файл в формате Excel...");
                    waitingType = WaitingType.SET_FILE;
                }
                else if (updateMessageText.equals("1")){
                    fileDB = 1;
                    delMess = bot.execute(new SendDocument().setChatId(chatId).setCaption("Отправьте файл в формате Excel...(вам предоставлен шаблон базы)").setDocument("Шаблон базы.xlsx", shablon)).getMessageId();

//                    delMess = sendMessage("Отправьте файл в формате Excel...");
                    waitingType = WaitingType.SET_FILE;

                }
                return COMEBACK;
            case SET_FILE:
                deleteMessage(updateMessageId);
                deleteMessage(delMess);
                if (hasDocument()){
                    if (fileDB == 0){
                        int delid = sendMessage("Excel обрабатывается...");
                        File file = bot.downloadFile(uploadFile(update.getMessage().getDocument().getFileId()));
                        SverkaService sverkaService = new SverkaService();
                        sverkaService.sendServiceReport(chatId, bot, file);
                        deleteMessage(delid);
                        return EXIT;
                    }
                    else if (fileDB == 1){
                        int delid = sendMessage("Excel обрабатывается...");
                        File file = bot.downloadFile(uploadFile(update.getMessage().getDocument().getFileId()));
                        RecipientZalivkaService recipientZalivkaService = new RecipientZalivkaService();
                        List<String> neZalino = recipientZalivkaService.sendServiceReport(chatId, bot, file);
                        String str = "База обновилась!\n"+getNeZalitos(neZalino);
                        List<String > strs = getBolu(str);
                        for (String s : strs){
                            sendMessage(s);
                        }
                        deleteMessage(delid);
                        return EXIT;
                    }
                }
                return COMEBACK;
        }

        return EXIT;
    }

    private List<String> getBolu(String str) {
        List<String> asd = new ArrayList<>();
        while (str.length() > 4000) {
            try {
                asd.add(str.substring(0, 4000));
                str = str.substring(4000);
            } catch (Exception e) {
                System.out.println("Bol!");
            }
        }
        asd.add(str);
        return asd;
    }

    private String getNeZalitos (List<String> neZalino){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(neZalino.size()).append(" человек с неправильным ИИН-ом\n");
        for (String iin : neZalino){
            stringBuilder.append("ИИН->").append(iin).append("\n");
        }
        if (neZalino.size() != 0){
            return stringBuilder.toString();
        }
        else return "";
    }



}
