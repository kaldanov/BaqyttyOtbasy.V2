package com.telegrambot.service;

import com.telegrambot.entity.custom.Complaint;
import com.telegrambot.enums.Language;
import com.telegrambot.repository.ComplaintRepository;
import com.telegrambot.repository.MessageRepository;
import com.telegrambot.util.Const;
import com.telegrambot.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ComplaintReportService {

//    private DaoFactory      factory         = DaoFactory.getInstance();
//    private MessageDao      messageDao      = factory.getMessageDao();
//    private SuggestionDao   suggestionDao   = factory.getSuggestionDao();
    private final MessageRepository messageRepository;
    private final ComplaintRepository complaintRepository;
    private XSSFWorkbook    workbook        = new XSSFWorkbook();
    private XSSFCellStyle   style           = workbook.createCellStyle();
    private Language currentLanguage = Language.ru;
    private Sheet           sheets;
    private Sheet           sheet;
    @Value(value = "${path_value}")
    private String path;
    public ComplaintReportService(MessageRepository messageRepository, ComplaintRepository complaintRepository) {
        this.messageRepository = messageRepository;
        this.complaintRepository = complaintRepository;
    }

    public void sendSuggestionReport(long chatId, DefaultAbsSender bot, Date dateBegin, Date dateEnd, int messagePrevReport) {
        currentLanguage = LanguageService.getLanguage(chatId);
        try {
            sendCompReport(chatId, bot, dateBegin, dateEnd, messagePrevReport);
        } catch (Exception e) {
            log.error("Can't create/send report", e);
            try {
                //messageRepository.getMessageText(Const.ERROR_SEND_REPORT_MESSAGE, currentLanguage.getId());
                int langId = currentLanguage.getId();
                String name = messageRepository.findByIdAndLangId(Const.ERROR_SEND_REPORT_MESSAGE, langId).getName();
                bot.execute(new SendMessage(chatId,name));
            } catch (TelegramApiException ex) {
                log.error("Can't send message", ex);
            }
        }
    }

    private void sendCompReport(long chatId, DefaultAbsSender bot, Date dateBegin, Date dateEnd, int messagePrevReport) throws TelegramApiException, IOException {
        sheets                      = workbook.createSheet("Жалобы");
        sheet                       = workbook.getSheetAt(0);
        List<Complaint> reports    = complaintRepository.findAllByPostDateBetweenOrderById(dateBegin, dateEnd);

        if (reports == null || reports.size() == 0) {
            bot.execute(new DeleteMessage(chatId, messagePrevReport));
            bot.execute(new SendMessage(chatId, messageRepository.getMessageText(Const.ERROR_SEND_REPORT_MESSAGE, currentLanguage.getId())));
            return;
        }
        BorderStyle thin            = BorderStyle.THIN;
        short black                 = IndexedColors.BLACK.getIndex();
        XSSFCellStyle styleTitle    = setStyle(workbook, thin, black, style);
        int rowIndex                = 0;
        createTitle(styleTitle, rowIndex, Arrays.asList("№;ФИО;Номер телефона;Текст;Дата".split(Const.SPLIT)));
        List<List<String>> info = reports.stream().map(x -> {
            List<String> list   = new ArrayList<>();
            list.add(String.valueOf(x.getId()));
            list.add(x.getFullName());
            list.add(x.getPhoneNumber());
            list.add(x.getText());
            list.add(com.telegrambot.util.DateUtil.getDayDate(x.getPostDate()));
            return list;
        }).collect(Collectors.toList());
        addInfo(info, rowIndex);
        sendFile(chatId, bot, dateBegin, dateEnd);
    }

    private void            addInfo(List<List<String>> reports, int rowIndex) {
        int cellIndex;
        for (List<String> report : reports) {
            sheets.createRow(++rowIndex);
            insertToRow(rowIndex, report, style);
        }
        cellIndex = 0;
        sheets.autoSizeColumn(cellIndex++);
        sheets.setColumnWidth(cellIndex++, 4000);
        sheets.setColumnWidth(cellIndex++, 4000);
        sheets.setColumnWidth(cellIndex++, 4000);
        sheets.autoSizeColumn(cellIndex++);
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
        BorderStyle tittle = BorderStyle.MEDIUM;

        XSSFCellStyle styleTitle = workbook.createCellStyle();
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
        styleTitle.setLeftBorderColor(black);
        style.setFillForegroundColor(new XSSFColor(new Color(0, 52, 94)));
        return styleTitle;
    }

    private void            sendFile(long chatId, DefaultAbsSender bot, Date dateBegin, Date dateEnd) throws IOException, TelegramApiException {
        String fileName = "Предложения за: " + com.telegrambot.util.DateUtil.getDayDate(dateBegin) + " - " + DateUtil.getDayDate(dateEnd) + ".xlsx";
//        String path = "C:\\test\\" + fileName;
        path+=fileName;
        //path += new Date().getTime();
        File dir = new File("C:\\test\\");
        if (!dir.exists()){
            dir.mkdir();
        }
        try (FileOutputStream stream = new FileOutputStream(path)) {
            workbook.write(stream);
        } catch (IOException e) {
            log.error("Can't send File error: ", e);
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
}
