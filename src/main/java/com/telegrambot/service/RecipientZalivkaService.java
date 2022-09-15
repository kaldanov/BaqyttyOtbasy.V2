package com.telegrambot.service;

import com.telegrambot.config.Bot;
import com.telegrambot.entity.custom.Recipient;
import com.telegrambot.enums.Language;
import com.telegrambot.repository.PropertiesRepository;
import com.telegrambot.repository.RecipientRepository;
import com.telegrambot.repository.TelegramBotRepositoryProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.*;
import java.util.*;

@Slf4j
public class RecipientZalivkaService {

    long chatId;
    File file;
    List<String> neZalito = new ArrayList<>();
    private RecipientRepository recipientRepository = TelegramBotRepositoryProvider.getRecipientRepository();
    private XSSFWorkbook workbook;
    private PropertiesRepository propertiesRepository = TelegramBotRepositoryProvider.getPropertiesRepository();


    public List<String> sendServiceReport(long chatId, DefaultAbsSender bot,File file) throws TelegramApiException {
        try {
            this.chatId = chatId;
            this.file = file;
            return importExel();
        } catch (Exception e) {
            log.error("Can't create/send report", e);
            try {
                bot.execute(new SendMessage(chatId, "Ошибка при импортировании"));
            } catch (TelegramApiException ex) {
                log.error("Can't send message", ex);
            }
        }
        return neZalito;
    }


    private List<String> importExel() throws IOException, InvalidFormatException {

        workbook = new XSSFWorkbook(file);
        XSSFSheet sheet = workbook.getSheetAt(0);

        String fullName = "";
        String iin= "";
        String phone_number= "";
        String address= "";
        String aliments= "";
        String apartment= "";
        String children= "";
        String credit= "";
        String credit_history= "";
        String education= "";
        String employment= "";
        String employment_type= "";
        String status= "";
        String maritalStatus= "";
        String visa= "";
        String social_benefits= "";
        String speciality= "";
        String disability= "";
        String disability_type= "";


        for (int i = 1; i < sheet.getLastRowNum() + 1; i++) {

            try {
                Row row = sheet.getRow(i);

                fullName = getStringValue(row , 0);
                iin = getStringValue(row, 1);

                if (iin.length() != 12) {
                    System.out.println("Error IIN -> " + iin);
                    neZalito.add(iin);
                    continue;
                }

                phone_number =    getStringValue(row, 2);
                address =         getStringValue(row, 3);
                visa =            getStringValue(row, 4);
                apartment =       getStringValue(row, 5);
                children =        getStringValue(row, 6);
                social_benefits = getStringValue(row, 7);
                maritalStatus =   getStringValue(row, 8);
                status =          getStringValue(row, 9);
                aliments =        getStringValue(row, 10);
                employment_type = getStringValue(row, 11);
                employment =      getStringValue(row, 12);
                education =       getStringValue(row, 13);
                speciality =      getStringValue(row, 14);
                disability =      getStringValue(row, 15);
                disability_type = getStringValue(row, 16);
                credit =          getStringValue(row, 17);
                credit_history =  getStringValue(row, 18);



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
                //recipient1.setMaritalStatus(maritalStatus);
                //recipient1.setStatus(status);
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
                recipientRepository.save(recipient1);
            }
            catch (Exception e){
                e.printStackTrace();
            }

        }

        return neZalito;
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

}