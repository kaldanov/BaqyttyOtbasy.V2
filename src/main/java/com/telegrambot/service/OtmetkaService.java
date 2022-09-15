package com.telegrambot.service;

import com.telegrambot.config.Bot;
import com.telegrambot.entity.custom.Recipient;
import com.telegrambot.entity.custom.Registration_Service;
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
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class OtmetkaService {

    long chatId;
    DefaultAbsSender bot;

    private UserRepository userRepository = TelegramBotRepositoryProvider.getUserRepository();
    private XSSFWorkbook  workbook        = new XSSFWorkbook();
    private XSSFWorkbook  workbookClear        = new XSSFWorkbook();
    //    private XSSFCellStyle style           = workbook.createCellStyle();
    private Language currentLanguage = Language.ru;
    private Sheet         sheet;
    private int           count;

    private RecipientRepository recipientRepository = TelegramBotRepositoryProvider.getRecipientRepository();
    private MessageRepository messageRepository = TelegramBotRepositoryProvider.getMessageRepository();
    private PropertiesRepository propertiesRepository = TelegramBotRepositoryProvider.getPropertiesRepository();

    private List<Registration_Service> docRecipients = new ArrayList<>();
    private List<Registration_Service> notZalito = new ArrayList<>();
    //    @Value(value = "${path_value}")
    private String path = "C:\\test\\";

    public List<Registration_Service > sendServiceReport(long chatId, DefaultAbsSender bot,File file) {
        try {
            this.chatId = chatId;
            this.bot = bot;
            List<Registration_Service > regss = importExel(file);
            sendExcess();
            return regss;
        } catch (Exception e) {
            log.error("Can't create/send report", e);
            try {
                bot.execute(new SendMessage(chatId, "Ошибка при импортировании"));
            } catch (TelegramApiException ex) {
                log.error("Can't send message", ex);
            }
            return new ArrayList<>();
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


    private List<Registration_Service >  importExel(File file1) throws IOException {
        RecipientRepository recipientRepository = TelegramBotRepositoryProvider.getRecipientRepository();

        docRecipients = new ArrayList<>();
        FileInputStream file = new FileInputStream(file1);
        workbook = new XSSFWorkbook(file);
        XSSFSheet sheet = workbook.getSheetAt(0);

        String iin= "";
        String dateStr= "";
        Date dateReg;


        for (int i = 1; i < sheet.getLastRowNum() + 1; i++) {

            try {
                Row row = sheet.getRow(i);
                iin = getStringValue(row, 0);
                dateReg = getDateValue(row, 1);
                dateReg.setHours(9);

                Registration_Service reg = new Registration_Service();
                reg.setIin(iin);
                reg.setDateReg(dateReg);

                if (iin.length() != 12 || dateReg == null || recipientRepository.findByIin(iin) == null) {
                    notZalito.add(reg);
                    continue;
                }

                docRecipients.add(reg);
            }
            catch (Exception e){
                e.printStackTrace();
            }

        }

        file.close();
        return docRecipients;
    }

    private Date getDateValue(Row row, int i) {
        try {
            return row.getCell(i).getDateCellValue();
        }catch (Exception e){
            return getDateStr(row, i);
        }
    }

    private Date getDateStr(Row row, int i) {
        try {
            String date123 =  getStringValue(row, i);
            DateFormat format = new SimpleDateFormat("dd.MM.yyyy");
            format.setLenient(true);
            Date date741 = format.parse(date123);
            return date741;
        }catch (Exception e){
            return null;
        }
    }

    private String getStringValue(Row row, int i) {
        try {
            return row.getCell(i).getStringCellValue();
        }catch (Exception e){
            return getNumericValue(row , i);
        }
    }

    private String getNumericValue(Row row, int i) {
        Double phoneDouble;
        try {
            phoneDouble = row.getCell(i).getNumericCellValue();
            return  String.valueOf(phoneDouble.longValue());
        } catch (Exception e) {
            return "";
        }
    }

    protected Language getLanguage() {
        if (chatId == 0) return Language.ru;
        return LanguageService.getLanguage(chatId);
    }

    private void            sendReport() throws TelegramApiException, IOException {
        sheet                      = workbookClear.createSheet("Не залитые");

        if (notZalito.size() == 0) {
            return;
        }

        XSSFCellStyle styleTitle    = setStyle();
        int rowIndex                = 0;
        createTitle(styleTitle, rowIndex, Arrays.asList("ИИН ; ДАТА".split(Const.SPLIT)));
        DateFormat format = new SimpleDateFormat("dd.MM.yyyy");


        List<List<String>> info     = notZalito.stream().map(x -> {

            List<String> list       = new ArrayList<>();
            try {

                list.add(String.valueOf(x.getIin()));
                list.add(format.format(x.getDateReg()));
            }catch (Exception e){}
            count = list.size();
            return list;
        }).collect(Collectors.toList());
        addInfo(info, rowIndex);
        sendFile(chatId, bot);
    }


    private void            addInfo(List<List<String>> reports, int rowIndex) {
        int cellIndex;
        for (List<String> report: reports) {
            sheet.createRow(++rowIndex);
            insertToRow(rowIndex, report);
        }
        for (cellIndex = 0; cellIndex <= count; cellIndex++) {
            sheet.autoSizeColumn(cellIndex);
        }
    }

    private void            createTitle(XSSFCellStyle styleTitle, int rowIndex, List<String> title) {
        sheet.createRow(rowIndex);
        insertToRow(rowIndex, title);
    }

    private void            insertToRow(int row, List<String> cellValues) {
        int cellIndex = 0;
        for (String cellValue : cellValues) {
            addCellValue(row, cellIndex++, cellValue);
        }
    }

    private void            addCellValue(int rowIndex, int cellIndex, String cellValue) {
        sheet.getRow(rowIndex).createCell(cellIndex).setCellValue(getString(cellValue));
        sheet.getRow(rowIndex).getCell(cellIndex).setCellStyle(setStyle());
    }

    private String          getString(String nullable) {
        if (nullable == null) return "";
        return nullable;
    }

    private XSSFCellStyle   setStyle() {
        BorderStyle tittle          = BorderStyle.MEDIUM;
        XSSFCellStyle styleTitle    = workbookClear.createCellStyle();
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

    private void            sendFile(long chatId, DefaultAbsSender bot) throws IOException, TelegramApiException {
        String fileName = "Не залитые.xlsx";
        path+=fileName;
        File dir = new File("C:\\test\\");
        if (!dir.exists()){
            dir.mkdir();
        }
        try (FileOutputStream stream = new FileOutputStream(path)) {
            workbookClear.write(stream);
        } catch (IOException e) {
            log.error("Can't send file error: ", e);
        }
        sendFile(chatId, bot, fileName, path);
    }
    private void                sendFile(long chatId, DefaultAbsSender bot, String fileName, String path) throws IOException, TelegramApiException {
        File file = new File(path);
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            bot.execute(new SendDocument().setChatId(chatId).setDocument(fileName, fileInputStream));
        }
        file.delete();
    }



}