package com.telegrambot.command.impl;

import com.telegrambot.command.Command;
import com.telegrambot.config.Bot;
import com.telegrambot.entity.User;
import com.telegrambot.entity.custom.Recipient;
import com.telegrambot.util.Const;
import com.telegrambot.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
public class id025_ShowUsers extends Command {

    private List<User>  allUsers;
    private int         count;
    private int         messagePreviewReport;

    @Override
    public  boolean     execute()    throws TelegramApiException {
        if(userRepository.findByChatId(chatId) == null){
            sendMessageWithKeyboard("Вы не прошли регистрацию" , 57);
            return EXIT;
        }
        if (!isAdmin() && !isMainAdmin()) {
            sendMessage(Const.NO_ACCESS);
            return EXIT;
        }
        if (hasMessageText()) {
//            count       = userRepository.findAllByEmailOrderById(new Bot().getTableSchema()).size();
//            allUsers    = userRepository.findAllByEmailOrderById(new Bot().getTableSchema());

            count = getCount();

            if (new Bot().getTableSchema().equals("PUBLIC")){
                count       = userRepository.findAll().size();
                allUsers    = userRepository.findAll();
            }
            if (count == 0) {
                sendMessage(Const.REGISTRATION_USERS_NOT_FOUND_MESSAGE);
                return EXIT;
            }
            messagePreviewReport = sendMessage(String.format(getText(Const.USERS_REPORT_DOING_MESSAGE), count));
            new Thread(() -> {
                try {
                    sendReport();
                } catch (TelegramApiException e) {
                    log.error("Can't send report", e);
                    try {
                        sendMessage("Ошибка отправки списка");
                    } catch (TelegramApiException ex) {
                        log.error("Can't send message", ex);
                    }
                }
            }).start();
        }
        return COMEBACK;
    }

    private int getCount() {
        List<User> users = userRepository.findAll();
        allUsers = new ArrayList<>();
        for (User user : users) {
            if (user.getIin() != null) {
                Recipient recipient = recipientRepository.findByIin(user.getIin());
                if (recipient != null &&
                        recipient.getDistrict() != null &&
                        recipient.getDistrict().equals(new Bot().getTableSchema())){
                    allUsers.add(user);
                }
            }
        }
        return allUsers.size();
    }


    private void        sendReport() throws TelegramApiException {
        int total           = count;
        XSSFWorkbook wb     = new XSSFWorkbook();
        Sheet sheets        = wb.createSheet("Пользователи");
        // -------------------------Стиль ячеек-------------------------
        BorderStyle thin            = BorderStyle.THIN;
        short black                 = IndexedColors.BLACK.getIndex();
        XSSFCellStyle style         = wb.createCellStyle();
        style.setWrapText           (true);
        style.setAlignment          (HorizontalAlignment.CENTER);
        style.setVerticalAlignment  (VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.BLUE.getIndex());
        style.setBorderTop          (thin);
        style.setBorderBottom       (thin);
        style.setBorderRight        (thin);
        style.setBorderLeft         (thin);
        style.setTopBorderColor     (black);
        style.setRightBorderColor   (black);
        style.setBottomBorderColor  (black);
        style.setLeftBorderColor    (black);
        BorderStyle tittle              = BorderStyle.MEDIUM;
        XSSFCellStyle styleTitle        = wb.createCellStyle();
        styleTitle.setWrapText          (true);
        styleTitle.setAlignment         (HorizontalAlignment.CENTER);
        styleTitle.setVerticalAlignment (VerticalAlignment.CENTER);
        styleTitle.setBorderTop         (tittle);
        styleTitle.setBorderBottom      (tittle);
        styleTitle.setBorderRight       (tittle);
        styleTitle.setBorderLeft        (tittle);
        styleTitle.setTopBorderColor    (black);
        styleTitle.setRightBorderColor  (black);
        styleTitle.setBottomBorderColor (black);
        styleTitle.setLeftBorderColor   (black);
        style.setFillForegroundColor    (new XSSFColor(new java.awt.Color(0, 52, 94)));
        Sheet sheet                     = wb.getSheetAt(0);
        //--------------------------------------------------------------------
        int rowIndex    = 0;
        int CellIndex   = 0;
        sheets  .createRow(rowIndex) .createCell(CellIndex)  .setCellValue("Регистрационные данные");
        sheet   .getRow(rowIndex)    .getCell(CellIndex)     .setCellStyle(styleTitle);
        sheets  .getRow(rowIndex)    .createCell(++CellIndex).setCellValue("Телефон");
        sheet   .getRow(rowIndex)    .getCell(CellIndex)     .setCellStyle(styleTitle);
        sheets  .getRow(rowIndex)    .createCell(++CellIndex).setCellValue("Данные telegram");
        sheet   .getRow(rowIndex)    .getCell(CellIndex)     .setCellStyle(styleTitle);
        sheets  .getRow(rowIndex)    .createCell(++CellIndex).setCellValue("Статус");
        sheet   .getRow(rowIndex)    .getCell(CellIndex)     .setCellStyle(styleTitle);
        for (User entity : allUsers) {
            CellIndex = 0;
            sheets  .createRow(++rowIndex).createCell(CellIndex)     .setCellValue(entity.getFullName());
            sheet   .getRow(rowIndex)     .getCell(CellIndex)        .setCellStyle(style);
            sheets  .getRow(rowIndex)     .createCell(++CellIndex)   .setCellValue(entity.getPhone());
            sheet   .getRow(rowIndex)     .getCell(CellIndex)        .setCellStyle(style);
            sheets  .getRow(rowIndex)     .createCell(++CellIndex)   .setCellValue(entity.getUserName());
            sheet   .getRow(rowIndex)     .getCell(CellIndex)        .setCellStyle(style);
            sheets  .getRow(rowIndex)     .createCell(++CellIndex)   .setCellValue(entity.getStatus());
            sheet   .getRow(rowIndex)     .getCell(CellIndex)        .setCellStyle(style);
        }
        String[] splitWidth = "13200;13200;13200;13200".split(";");
        for (int i = 0; i < splitWidth.length; i++) {
            if (splitWidth[i].equalsIgnoreCase("auto")) {
                sheets.autoSizeColumn(i);
            } else {
                int size = 0;
                try {
                    size = Integer.parseInt(splitWidth[i].replaceAll("[^0-9]", ""));
                } catch (NumberFormatException e) {
                    log.warn("Error in message № 309 - {}", splitWidth[i]);
                }
                if (size > 0) sheets.setColumnWidth(i, size);
            }
        }
        String filename = String.format("List users %s.xlsx", DateUtil.getDayDate(new Date()));
        deleteMessage(messagePreviewReport);
        bot.execute(new SendDocument().setChatId(chatId).setDocument(filename, getInputStream(wb)));
    }

    private InputStream getInputStream(XSSFWorkbook workbook) {
        ByteArrayOutputStream tables = new ByteArrayOutputStream();
        try {
            workbook.write(tables);
        } catch (IOException e) {
            log.error("Can't write table to wb, case: {}", e);
        }
        return new ByteArrayInputStream(tables.toByteArray());
    }
}
