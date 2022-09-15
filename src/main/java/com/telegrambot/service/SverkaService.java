package com.telegrambot.service;

import com.telegrambot.config.Bot;
import com.telegrambot.entity.custom.Recipient;
import com.telegrambot.entity.custom.Status;
import com.telegrambot.enums.Language;
import com.telegrambot.repository.*;
import com.telegrambot.util.Const;
import com.telegrambot.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
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
public class SverkaService {

    long chatId;
    DefaultAbsSender bot;

    private UserRepository userRepository = TelegramBotRepositoryProvider.getUserRepository();
    private XSSFWorkbook workbook = new XSSFWorkbook();
    private XSSFWorkbook workbookClear = new XSSFWorkbook();
    //    private XSSFCellStyle style           = workbook.createCellStyle();
    private Language currentLanguage = Language.ru;
    private Sheet sheet;
    private int count;

    private RecipientRepository recipientRepository = TelegramBotRepositoryProvider.getRecipientRepository();
    private MessageRepository messageRepository = TelegramBotRepositoryProvider.getMessageRepository();
    private PropertiesRepository propertiesRepository = TelegramBotRepositoryProvider.getPropertiesRepository();

    private List<Recipient> docRecipients;
    //    @Value(value = "${path_value}")
    private String path = "C:\\test\\";

    public void sendServiceReport(long chatId, DefaultAbsSender bot, File file) {
        try {
            this.chatId = chatId;
            this.bot = bot;
            importExel(file);
            sendExcess();
        } catch (Exception e) {
            log.error("Can't create/send report", e);
            try {
                bot.execute(new SendMessage(chatId, "Ошибка при импортировании"));
            } catch (TelegramApiException ex) {
                log.error("Can't send message", ex);
            }
        }
    }

    private void sendExcess() {
        try {
            sendReport();
        } catch (Exception e) {
            log.error("Can't create/send report", e);
            try {
                bot.execute(new SendMessage(chatId, "Ошибка при создании отчета"));
            } catch (TelegramApiException ex) {
                log.error("Can't send message", ex);
            }
        }
    }


    private void importExel(File file1) throws IOException {
        docRecipients = new ArrayList<>();
        FileInputStream file = new FileInputStream(file1);
        workbook = new XSSFWorkbook(file);
        XSSFSheet sheet = workbook.getSheetAt(0);

        String fullName = "";
        String iin = "";
        String phone_number = "";
        String address = "";
        String aliments = "";
        String apartment = "";
        String children = "";
        String credit = "";
        String credit_history = "";
        String education = "";
        String employment = "";
        String employment_type = "";
        String status = "";
        String maritalStatus = "";
        String visa = "";
        String social_benefits = "";
        String speciality = "";
        String disability = "";
        String disability_type = "";


        for (int i = 1; i < sheet.getLastRowNum() + 1; i++) {

            try {
                Row row = sheet.getRow(i);

                fullName = getStringValue(row, 0);
                iin = getStringValue(row, 1);

                if (iin.length() != 12) {
                    System.out.println("Error IIN -> " + iin);
                    continue;
                }

                phone_number = getStringValue(row, 2);
                address = getStringValue(row, 3);
                visa = getStringValue(row, 4);
                apartment = getStringValue(row, 5);
                children = getStringValue(row, 6);
                social_benefits = getStringValue(row, 7);
                maritalStatus = getStringValue(row, 8);
                status = getStringValue(row, 9);
                aliments = getStringValue(row, 10);
                employment_type = getStringValue(row, 11);
                employment = getStringValue(row, 12);
                education = getStringValue(row, 13);
                speciality = getStringValue(row, 14);
                disability = getStringValue(row, 15);
                disability_type = getStringValue(row, 16);
                credit = getStringValue(row, 17);
                credit_history = getStringValue(row, 18);


                Recipient recipient1 = new Recipient();
                recipient1.setFullName(fullName);
                recipient1.setIin(iin);
                recipient1.setPhoneNumber(phone_number);
                recipient1.setAddress(address);
                recipient1.setAliments(aliments);
                recipient1.setApartment(apartment);
                recipient1.setChildren(children);
                recipient1.setCreditHistory(credit);
                recipient1.setCreditInfo(credit_history);
                recipient1.setEducation(education);
                recipient1.setEmployment(employment);
                recipient1.setEmploymentType(employment_type);
                recipient1.setVisa(visa);
                recipient1.setSocialBenefits(social_benefits);
                recipient1.setEducationName(education);
                recipient1.setDisability(disability);
                recipient1.setDisabilityType(disability_type);
                recipient1.setEducationName(speciality);
                recipient1.setRegistrationDate(new Date());

                recipient1.setDistrict(new Bot().getTableSchema());

                Recipient recipient = recipientRepository.findByIin(iin);
                if (recipient != null) {
                    recipient1.setId(recipient.getId());
                    recipient1.setDistrict(recipient.getDistrict());
                }
                docRecipients.add(recipient1);
//                recipientRepository.save(recipient1);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        file.close();
    }

    private String getStringValue(Row row, int i) {
        try {
            return row.getCell(i).getStringCellValue();
        } catch (Exception e) {
            return getNumericValue(row, i);
        }
    }

    private String getNumericValue(Row row, int i) {
        Double phoneDouble;
        try {
            phoneDouble = row.getCell(i).getNumericCellValue();
            return String.valueOf(phoneDouble.longValue());
        } catch (Exception e) {
            return "";
        }
    }

    protected Language getLanguage() {
        if (chatId == 0) return Language.ru;
        return LanguageService.getLanguage(chatId);
    }

    private List<Recipient> trimRecipient1(List<Recipient> recipients) {
        List<Recipient> trimmed = new ArrayList<>();
        for (Recipient recipient : recipients) {
//            if (recipient.getIin() != null &&
//                    recipient.getIin().length() == 12 &&
//                    userRepository.findByIin(recipient.getIin()) != null &&
//                    userRepository.findByIin(recipient.getIin()).getEmail() != null &&
//                    userRepository.findByIin(recipient.getIin()).getEmail().equals(new Bot().getTableSchema())) {
//                trimmed.add(recipient);
//            }
//            else
            if (recipient.getIin() != null &&
                    recipient.getIin().length() == 12 &&
                    recipient.getDistrict() != null &&
                    recipient.getDistrict().equals(new Bot().getTableSchema())) {
                trimmed.add(recipient);
            }

        }
        return trimmed;
    }

    private void sendReport() throws TelegramApiException, IOException {
        sheet = workbookClear.createSheet("Анкета");
        List<Recipient> recipients = recipientRepository.findAllByOrderById();

        recipients = trimRecipient1(recipients);

        recipients = trimRecipient(recipients);

        if (recipients.size() == 0) {
            bot.execute(new SendMessage(chatId, "Анкетирование отсутствует"));
            return;
        }

        XSSFCellStyle styleTitle = setStyle();
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
        sendFile(chatId, bot);
    }

    private List<Recipient> trimRecipient(List<Recipient> baseRecipients) {
        List<Recipient> trimmed = new ArrayList<>();

        for (Recipient recipient : baseRecipients) {
            boolean has = false;
            for (Recipient docRecipient : docRecipients) {
                if (recipient.getIin().equals(docRecipient.getIin())) {
                    has = true;
                    break;
                }
            }
            if (!has)
                trimmed.add(recipient);
        }
        return trimmed;
    }

    private void addInfo(List<List<String>> reports, int rowIndex) {
        int cellIndex;
        for (List<String> report : reports) {
            sheet.createRow(++rowIndex);
            insertToRow(rowIndex, report);
        }
        for (cellIndex = 0; cellIndex <= count; cellIndex++) {
            sheet.autoSizeColumn(cellIndex);
        }
    }

    private void createTitle(XSSFCellStyle styleTitle, int rowIndex, List<String> title) {
        sheet.createRow(rowIndex);
        insertToRow(rowIndex, title);
    }

    private void insertToRow(int row, List<String> cellValues) {
        int cellIndex = 0;
        for (String cellValue : cellValues) {
            addCellValue(row, cellIndex++, cellValue);
        }
    }

    private void addCellValue(int rowIndex, int cellIndex, String cellValue) {
        sheet.getRow(rowIndex).createCell(cellIndex).setCellValue(getString(cellValue));
        sheet.getRow(rowIndex).getCell(cellIndex).setCellStyle(setStyle());
    }

    private String getString(String nullable) {
        if (nullable == null) return "";
        return nullable;
    }

    private XSSFCellStyle setStyle() {
        BorderStyle tittle = BorderStyle.MEDIUM;
        XSSFCellStyle styleTitle = workbookClear.createCellStyle();
        styleTitle.setWrapText(true);
        styleTitle.setAlignment(HorizontalAlignment.CENTER);
        styleTitle.setVerticalAlignment(VerticalAlignment.CENTER);
        styleTitle.setBorderTop(tittle);
        styleTitle.setBorderBottom(tittle);
        styleTitle.setBorderRight(tittle);
        styleTitle.setBorderLeft(tittle);
        styleTitle.setTopBorderColor(IndexedColors.BLACK.getIndex());
        styleTitle.setRightBorderColor(IndexedColors.BLACK.getIndex());
        styleTitle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        styleTitle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        styleTitle.setFillForegroundColor(new XSSFColor(new Color(0, 52, 94)));
        return styleTitle;
    }

    private void sendFile(long chatId, DefaultAbsSender bot) throws IOException, TelegramApiException {
        String fileName = "Анкета.xlsx";
        path += fileName;
        File dir = new File("C:\\test\\");
        if (!dir.exists()) {
            dir.mkdir();
        }
        try (FileOutputStream stream = new FileOutputStream(path)) {
            workbookClear.write(stream);
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