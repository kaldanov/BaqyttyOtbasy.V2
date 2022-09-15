package com.telegrambot.command.impl;

import com.telegrambot.command.Command;
import com.telegrambot.entity.custom.ReportToService;
import com.telegrambot.entity.enums.WaitingType;
import com.telegrambot.enums.Language;
import com.telegrambot.util.Const;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;


public class id049_ReRegistration extends Command {


    public  int deleteMessId;
    public  int secondDeleteMessId;

    private XSSFWorkbook workbook                = new XSSFWorkbook();
    private XSSFCellStyle style                   = workbook.createCellStyle();
    private Sheet sheets;
    private Sheet                   sheet;
    private Language currentLanguage         = Language.ru;
    private List<ReportToService> reportToServices;


    @Override
    public boolean execute() throws TelegramApiException {
       try{
           if(userRepository.findByChatId(chatId) == null){
               sendMessageWithKeyboard("Вы не прошли регистрацию" , 57);
               return EXIT;
           }

           if (!isUpravlenie()){
               sendMessage(10);
               return EXIT;
           }
           switch (waitingType){
               case START:
                   if (isUpravlenie()){
                       deleteMess();
                       deleteMessId =  sendMessageWithKeyboard(getText(24), 50);
                       waitingType = WaitingType.SEND_REPORT;
                   }
                   else{
                       sendMessage(10);
                       return EXIT;
                   }

                   return COMEBACK;
               case SEND_REPORT:
                    sendMessage(1123);
                   sendCitizenReport();

                   //todo send report SANZHAR!!!!
//
//                   int preview                         = sendMessage("Отчет подготавливается...");
//                   ServiceReportService reportService  = new ServiceReportService();
//                   reportService.sendServiceReport(chatId, bot, start, end, preview);

                   return COMEBACK;

               default: return EXIT;
           }
       }
       catch (Exception e){
           e.printStackTrace();
           sendMessageWithKeyboard("Добро пожаловать!", 1);
           return EXIT;
       }
    }



    private void deleteMess() {

            deleteMessage(updateMessageId);
            deleteMessage(deleteMessId );
            deleteMessage(secondDeleteMessId);
    }

    private void            sendCitizenReport() throws TelegramApiException, IOException {
        sheets                              = workbook.createSheet("Зарегистрированых");
        sheet                               = workbook.getSheetAt(0);

        reportToServices = reportServiceRepository.findAll();

        if (reportToServices.size() == 0) {
//            bot.execute(new DeleteMessage(chatId, messagePrevReport));
            bot.execute(new SendMessage(chatId, "Отчеты отсутствуют"));
            return;
        }
        BorderStyle thin                    = BorderStyle.THIN;
        short black                         = IndexedColors.BLACK.getIndex();
        XSSFCellStyle styleTitle            = setStyle(workbook, thin, black, style);
        int rowIndex                        = 0;
        createTitle(styleTitle, rowIndex, Arrays.asList("№;ФИО;Услуга;Файл;Дата отправления".split(Const.SPLIT)));
        List<List<String>> info             = reportToServices.stream().map(x -> {
            List<String> list               = new ArrayList<>();
            list.add(String.valueOf(x.getId()));
            list.add(userRepository.findByChatId(x.getSenderChatId()).getFullName());
            list.add(serviceRepository.findById2AndLangId(x.getServiceId(), getLanguage().getId()).getName());
            list.add("https://api.telegram.org/file/bot" + propertiesRepository.findById(Const.BOT_TOKEN).getValue() + "/" + uploadFile(x.getFile()));
            list.add(x.getSendDate().toString());

            return list;
        }).collect(Collectors.toList());
        addInfo(info, rowIndex);
        sendFile();
    }

//    private String uploadFile(String fileId){
//        Objects.requireNonNull(fileId);
//        GetFile getFile = new GetFile().setFileId(fileId);
//        try{
//            org.telegram.telegrambots.meta.api.objects.File file = bot.execute(getFile);
//            return file.getFilePath();
//        } catch (TelegramApiException e){
//            throw new IllegalMonitorStateException();
//        }
//    }
    private void            addInfo(List<List<String>> reports, int rowIndex) {
        for (List<String> report : reports) {
            sheets.createRow(++rowIndex);
            insertToRow(rowIndex, report, style);
        }
        for (int index = 0; index < 7; index++) {
            sheets.autoSizeColumn(index);
        }
    }

    private void            createTitle(XSSFCellStyle styleTitle, int rowIndex, List<String> title) {
        sheets.createRow(rowIndex);
        insertToRow(rowIndex, title, styleTitle);
    }

    private void            insertToRow(int row, List<String> cellValues, CellStyle cellStyle) {
        int cellIndex = 0;
        for (String cellValue : cellValues) {
            addCellValue(row, cellIndex++, cellValue, cellStyle);
        }
    }

    private void            addCellValue(int rowIndex, int cellIndex, String cellValue, CellStyle cellStyle) {
        sheets.getRow(rowIndex).createCell(cellIndex).setCellValue(getString(cellValue));
        sheet.getRow(rowIndex).getCell(cellIndex).setCellStyle(cellStyle);
    }

    private String          getString(String nullable) {
        if (nullable == null) return "";
        return nullable;
    }

    private XSSFCellStyle   setStyle(XSSFWorkbook workbook, BorderStyle thin, short black, XSSFCellStyle style) {
        style.setWrapText(true);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillBackgroundColor(IndexedColors.BLUE.getIndex());
        style.setBorderTop(thin);
        style.setBorderBottom(thin);
        style.setBorderRight(thin);
        style.setBorderLeft(thin);
        style.setTopBorderColor(black);
        style.setRightBorderColor(black);
        style.setBottomBorderColor(black);
        style.setLeftBorderColor(black);
        BorderStyle tittle          = BorderStyle.MEDIUM;

        XSSFFont titleFont = workbook.createFont();
        titleFont.setFontHeight(10);
        titleFont.setBold(true);
        titleFont.setColor(black);

        XSSFCellStyle styleTitle    = workbook.createCellStyle();
        styleTitle.setWrapText(true);
        styleTitle.setAlignment(HorizontalAlignment.CENTER);
        styleTitle.setVerticalAlignment(VerticalAlignment.CENTER);
        styleTitle.setBorderTop(tittle);
        styleTitle.setBorderBottom(tittle);
        styleTitle.setBorderRight(tittle);
        styleTitle.setBorderLeft(tittle);
        styleTitle.setTopBorderColor(black);
        styleTitle.setRightBorderColor(black);
        styleTitle.setBottomBorderColor(black);
        styleTitle.setFont(titleFont);
        styleTitle.setLeftBorderColor(black);
        style.setFillForegroundColor(new XSSFColor(new Color(0, 52, 94)));
        return styleTitle;
    }

    private void            sendFile() throws IOException, TelegramApiException {
        String fileName = "Отчет по специалистам(person).xlsx";
        String path     = "C:\\botApps\\BakyttyOtbasy\\" + fileName;
        path            += new Date().getTime();
        try (FileOutputStream stream = new FileOutputStream(path)) {
            workbook.write(stream);
        } catch (IOException e) {
            System.out.println("Can't send File error: ");
            e.printStackTrace();
        }
        sendFile(chatId, bot, fileName, path);
    }

    private void            sendFile(long chatId, DefaultAbsSender bot, String fileName, String path) throws IOException, TelegramApiException {
        File file = new File(path);
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            bot.execute(new SendDocument().setChatId(chatId).setDocument(fileName, fileInputStream));
        }
        file.delete();
    }
//


}
