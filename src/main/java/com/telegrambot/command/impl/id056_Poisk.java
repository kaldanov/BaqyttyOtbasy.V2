package com.telegrambot.command.impl;

import com.telegrambot.command.Command;
import com.telegrambot.config.Bot;
import com.telegrambot.entity.custom.Recipient;
import com.telegrambot.entity.enums.WaitingType;
import com.telegrambot.service.RecipientZalivkaService;
import com.telegrambot.service.SverkaService;
import com.telegrambot.util.ButtonsLeaf;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class id056_Poisk extends Command {
    private int delMess;
    private int typeSearch;
    private ButtonsLeaf recipientsLeaf;

    @Override
    public boolean execute() throws SQLException, TelegramApiException {
        if(userRepository.findByChatId(chatId) == null){
            sendMessageWithKeyboard("Вы не прошли регистрацию" , 57);
            return EXIT;
        }
        switch (waitingType){
            case START:
                if (hasCallbackQuery()){
                    deleteMessage(updateMessageId);
                    if (isButton(1040)){
                        delMess = sendMessage("Введите ИИН для поиска:");
                        typeSearch = 1;
                        waitingType = WaitingType.CHOOSE_SEARCH;
                    }
                    else if (isButton(1041)){
                        delMess = sendMessage("Введите Номер телефона для поиска:");
                        typeSearch = 2;
                        waitingType = WaitingType.CHOOSE_SEARCH;
                    }
                    else if (isButton(1042)){
                        delMess = sendMessage("Введите ФИО для поиска:");
                        typeSearch = 3;
                        waitingType = WaitingType.CHOOSE_SEARCH;
                    }
                }
                else {
                    deleteMessage(updateMessageId);
                    sendMessageWithKeyboard("Выберите, что хотите сделать?", 56);
                }
                return COMEBACK;
            case CHOOSE_SEARCH:
                deleteMessage(updateMessageId);
                deleteMessage(delMess);
                List<Recipient> recipients = new ArrayList<>();
                if (hasMessageText()){
                    switch (typeSearch){
                        case 1: recipients = recipientsByIIN(updateMessageText); break;
                        case 2: recipients = recipientsByPhone(updateMessageText); break;
                        default: recipients = recipientsByFIO(updateMessageText); break;
                    }
                }
                recipientsLeaf = new ButtonsLeaf(getNames(recipients), getIds(recipients) , 10);
                if (recipients.size() > 0)
                    sendMessageWithKeyboard("Выберите:", recipientsLeaf.getListButtonWhereIdIsData());
                else {
                    sendMessage("Список пуст!");
                    return EXIT;
                }
                waitingType = WaitingType.CHOOSE_RECIPIENT;
                return COMEBACK;
            case CHOOSE_RECIPIENT:
                if (hasCallbackQuery() && recipientsLeaf.isNext(updateMessageText)){
                    editMessageWithKeyboard("Выберите:",updateMessageId,(InlineKeyboardMarkup) recipientsLeaf.getListButtonWhereIdIsData());
                }
                else if (hasCallbackQuery() && recipientRepository.findById(getLong(updateMessageText)) != null){
                    deleteMessage(updateMessageId);
                    Recipient recipient = recipientRepository.findById(getLong(updateMessageText));
                    sendMessage(getRecipientInfo(recipient));
                    return EXIT;
                }
                return COMEBACK;
        }

        return EXIT;
    }

    private String getRecipientInfo(Recipient recipient) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getText(89)).append(recipient.getFullName()).append(next);
        stringBuilder.append(getText(97)).append(recipient.getIin()).append(next);
        stringBuilder.append(getText(90)).append(recipient.getPhoneNumber()).append(next);
        stringBuilder.append(getText(1088)).append(recipient.getStatus() != null?recipient.getStatus():"").append(next);
        stringBuilder.append(getText(91)).append(recipient.getAddress()).append(next);

        return stringBuilder.toString();
    }

    private long getLong(String updateMessageText) {
        try {
            Long lol = Long.parseLong(updateMessageText);
            return lol;
        }catch (Exception e){
            e.printStackTrace();
            return -1L;
        }
    }

    private List<String> getIds(List<Recipient> recipients) {
        List<String> ids = new ArrayList<>();
        for (Recipient recipient : recipients){
            ids.add(String.valueOf(recipient.getId()));
        }
        return ids;
    }

    private List<String> getNames(List<Recipient> recipients) {
        List<String> ids = new ArrayList<>();
        for (Recipient recipient : recipients){
            ids.add(recipient.getFullName());
        }
        return ids;
    }

    private List<Recipient> recipientsByFIO(String updateMessageText) {
        List<Recipient> recipients = recipientRepository.findAllByDistrict(new Bot().getTableSchema());
        List<Recipient> searchRecipients = new ArrayList<>();
        for (Recipient recipient : recipients){
            if(recipient.getFullName().toLowerCase().contains(updateMessageText.toLowerCase())){
                searchRecipients.add(recipient);
            }
        }
        return searchRecipients;
    }

    private List<Recipient> recipientsByPhone(String updateMessageText) {
        List<Recipient> recipients = recipientRepository.findAllByOrderById();
        List<Recipient> searchRecipients = new ArrayList<>();
        for (Recipient recipient : recipients){
            if(recipient.getPhoneNumber() != null &&
                    recipient.getPhoneNumber().toLowerCase().contains(updateMessageText.toLowerCase())){
                searchRecipients.add(recipient);
            }
        }
        return searchRecipients;
    }

    private List<Recipient> recipientsByIIN(String updateMessageText) {
        List<Recipient> recipients = recipientRepository.findAllByOrderById();
        List<Recipient> searchRecipients = new ArrayList<>();
        for (Recipient recipient : recipients){
            if(recipient.getIin().equals(updateMessageText)){
                searchRecipients.add(recipient);
            }
        }
        return searchRecipients;
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

    protected String uploadFile(String fileId) {
        Objects.requireNonNull(fileId);
        GetFile getFile = new GetFile();
        getFile.setFileId(fileId);
        try {
            org.telegram.telegrambots.meta.api.objects.File file = bot.execute(getFile);
            return file.getFilePath();
        } catch (TelegramApiException e) {
            throw new IllegalStateException(e);
        }
    }


}
