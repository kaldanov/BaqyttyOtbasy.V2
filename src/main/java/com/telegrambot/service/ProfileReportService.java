package com.telegrambot.service;

import com.telegrambot.config.Bot;
import com.telegrambot.entity.custom.Recipient;
import com.telegrambot.entity.custom.Status;
import com.telegrambot.enums.Language;
import com.telegrambot.repository.MessageRepository;
import com.telegrambot.repository.RecipientRepository;
import com.telegrambot.repository.UserRepository;
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
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProfileReportService {

    private final MessageRepository messageRepository;
    private final RecipientRepository recipientRepository;
    private UserRepository userRepository;
    private XSSFWorkbook workbook = new XSSFWorkbook();
    private XSSFCellStyle style = workbook.createCellStyle();
    private Language currentLanguage = Language.ru;
    private Sheet sheets;
    private Sheet sheet;
    private int count;
    @Value(value = "${path_value}")
    private String path;
    private Map<Integer, String> statusType = new HashMap();

    public ProfileReportService(MessageRepository messageRepository, RecipientRepository recipientRepository, UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.recipientRepository = recipientRepository;
        this.userRepository = userRepository;
    }

    public void sendProfileReport(long chatId, DefaultAbsSender bot, Date dateBegin, Date dateEnd, int messagePrevReport) {
        statusType.put(7, "полная семья");
        statusType.put(8, "полная семья");
        statusType.put(9, "неполная семья");
        statusType.put(10, "неполная семья");
        statusType.put(11, "неполная семья");
        statusType.put(12, "неполная семья");
        currentLanguage = LanguageService.getLanguage(chatId);
        try {
            sendReport(chatId, bot, dateBegin, dateEnd, messagePrevReport);
        } catch (Exception e) {
            log.error("Can't create/send report", e);
            try {
                bot.execute(new SendMessage(chatId, "Ошибка при создании отчета"));
            } catch (TelegramApiException ex) {
                log.error("Can't send message", ex);
            }
        }
    }

    private void sendReport(long chatId, DefaultAbsSender bot, Date dateBegin, Date dateEnd, int messagePrevReport) throws TelegramApiException, IOException {
        sheets = workbook.createSheet("Анкета");
        sheet = sheets;
        List<Recipient> recipients = recipientRepository.findAllByRegistrationDateBetweenOrderByRegistrationDateDesc(dateBegin, dateEnd);
        recipients = trimRecipient(recipients);
        if (recipients.size() == 0) {
            bot.execute(new DeleteMessage(chatId, messagePrevReport));
            bot.execute(new SendMessage(chatId, "За выбранный период анкетирование отсутствует"));
            return;
        }
        BorderStyle thin = BorderStyle.THIN;
        short black = IndexedColors.BLACK.getIndex();
        XSSFCellStyle styleTitle = setStyle(workbook, thin, black, style);
        int rowIndex = 0;
        createTitle(styleTitle, rowIndex, Arrays.asList(messageRepository.getMessageText(1057, currentLanguage.getId()).split(Const.SPLIT)));
        //№;ФИО;ИИН;Контакты;Место жительство;Прописка;Наличие жилья;
        // ИИН ребенка;Сведения о получении социальных пособий;Статус;
        // Семейное положение;Алименты;Занятость;Место работы;Образование;
        // Специальность по образованию;Наличие инвалидности;Тип инвалидности;
        // Наличие кредита;Информация о кредите;Дата регистраций

        List<List<String>> info = recipients.stream().map(x -> {
            List<String> list = new ArrayList<>();
            list.add(String.valueOf(x.getId()));
            list.add(x.getFullName());
            list.add(x.getIin());
            list.add(x.getPhoneNumber());
            list.add(x.getAddress());
            list.add(x.getVisa());
            list.add(x.getApartment());
            list.add(x.getChildren());
            list.add(x.getSocialBenefits());
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("");
            if (x.getStatus() != null && x.getStatus().size() != 0) {
                for (Status status1 : x.getStatus()) {
                    if (status1.getStatusId() == 3) {
                        stringBuilder.append(statusType.get(status1.getId())).append("(").append(status1.getNameRus()).append(")").append("\n");
                    } else
                        stringBuilder.append(status1.getNameRus()).append("\n");
                }
            }
            list.add(stringBuilder.toString());
            list.add(x.getAliments());
            list.add(x.getEmploymentType() != null ? x.getEmploymentType() : "");
            list.add(x.getEmployment() != null ? x.getEmployment() : "");
            list.add(x.getEducation() != null ? x.getEducation() : "");
            list.add(x.getEducationName() != null ? x.getEducationName() : "");
            list.add(x.getDisability() != null ? x.getDisability() : " ");
            list.add(x.getDisabilityType() != null ? x.getDisabilityType() : " ");
            list.add(x.getCreditHistory() != null ? x.getCreditHistory() : "");
            list.add(x.getCreditInfo() != null ? x.getCreditInfo() : "");
            list.add(com.telegrambot.util.DateUtil.getDateAndTime(x.getRegistrationDate()));
            count = list.size();
            return list;
        }).collect(Collectors.toList());
        addInfo(info, rowIndex);
        sendFile(chatId, bot, dateBegin, dateEnd);
    }

    private List<Recipient> trimRecipient(List<Recipient> recipients) {
        List<Recipient> trimmed = new ArrayList<>();
        for (Recipient recipient : recipients) {
//            if (userRepository.findByIin(recipient.getIin()) != null &&
//                    userRepository.findByIin(recipient.getIin()).getEmail() != null &&
//                    userRepository.findByIin(recipient.getIin()).getEmail().equals(new Bot().getTableSchema())) {
//                    trimmed.add(recipient);
//            }
//            else
            if (recipient.getDistrict() != null && recipient.getDistrict().equals(new Bot().getTableSchema())) {
                trimmed.add(recipient);
            }

        }
        return trimmed;
    }

    private void addInfo(List<List<String>> reports, int rowIndex) {
        int cellIndex;
        for (List<String> report : reports) {
            sheets.createRow(++rowIndex);
            insertToRow(rowIndex, report, style);
        }
        for (cellIndex = 0; cellIndex <= count; cellIndex++) {
            sheets.autoSizeColumn(cellIndex);
        }
    }

    private void createTitle(XSSFCellStyle styleTitle, int rowIndex, List<String> title) {
        sheets.createRow(rowIndex);
        insertToRow(rowIndex, title, styleTitle);
    }

    private void insertToRow(int row, List<String> cellValues, CellStyle cellStyle) {
        int cellIndex = 0;
        for (String cellValue : cellValues) {
            addCellValue(row, cellIndex++, cellValue, cellStyle);
        }
    }

    private void addCellValue(int rowIndex, int cellIndex, String cellValue, CellStyle cellStyle) {
        sheets.getRow(rowIndex).createCell(cellIndex).setCellValue(getString(cellValue));
        sheet.getRow(rowIndex).getCell(cellIndex).setCellStyle(cellStyle);
    }

    private String getString(String nullable) {
        if (nullable == null) return "";
        return nullable;
    }

    private XSSFCellStyle setStyle(XSSFWorkbook workbook, BorderStyle thin, short black, XSSFCellStyle style) {
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

    private void sendFile(long chatId, DefaultAbsSender bot, Date dateBegin, Date dateEnd) throws IOException, TelegramApiException {
        String fileName = "Анкета за: " + com.telegrambot.util.DateUtil.getDayDate(dateBegin) + " - " + DateUtil.getDayDate(dateEnd) + ".xlsx";
        path += fileName;
        File dir = new File("C:\\test\\");
        if (!dir.exists()) {
            dir.mkdir();
        }
        try (FileOutputStream stream = new FileOutputStream(path)) {
            workbook.write(stream);
        } catch (IOException e) {
            log.error("Can't send file error: ", e);
        }
        sendFile(chatId, bot, fileName, path);
    }

    private void sendFile(long chatId, DefaultAbsSender bot, String fileName, String path) throws IOException, TelegramApiException {
        File file = new File(path);
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            bot.execute(new SendDocument().setChatId(chatId).setDocument(fileName, fileInputStream));
        }
        file.delete();
    }


}
