package com.telegrambot.command.impl;

import com.telegrambot.command.Command;
import com.telegrambot.config.Bot;
import com.telegrambot.entity.custom.*;
import com.telegrambot.entity.enums.WaitingType;
import com.telegrambot.repository.StatusRepository;
import com.telegrambot.repository.TelegramBotRepositoryProvider;
import com.telegrambot.util.BotUtil;
import com.telegrambot.util.ButtonsLeaf;
import com.telegrambot.util.Const;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


public class id050_RecipientBySpecialist extends Command {
//    private User user;
    private Recipient recipient;
//    private Long chatId;
    private BotUtil botUtil;
    private List<String> list;
    private ButtonsLeaf buttonsLeaf;
    private WaitingType waitingType = WaitingType.START;
    private boolean COMEBACK = false;
    private boolean EXIT = true;

    private int secondDeleteMessageId;
    private ArrayList<String> socialBenefitsList = new ArrayList<>();
    private int deleteMessageId;
    private boolean isReg;
    // for request service
    private List<Category_Indicator> allCategories;

    private List<Service> servicesOfCategory;


    private List<Status> statuses = new ArrayList<>();

    private StatusRepository statusRepository = TelegramBotRepositoryProvider.getStatusRepository();



    @Override
    public boolean execute() throws TelegramApiException {
        if (specialistRepository.findAllByChatIdOrderById(chatId) == null || specialistRepository.findAllByChatIdOrderById(chatId).size() == 0) {
            sendMessage(Const.NO_ACCESS);
            return EXIT;
        }
            switch (waitingType) {
                case START:
                    if(userRepository.findByChatId(chatId) == null){
                        sendMessageWithKeyboard("Вы не прошли регистрацию" , 57);
                        return EXIT;
                    }
                    deleteMess();
                        deleteMessageId = sendMessage(Const.SET_IIN_MESSAGE);
                        recipient = new Recipient();
                        recipient.setRegistrationDate(new Date());
                        recipient.setDistrict(new Bot().getTableSchema());

//                        recipient.setChatId(chatId);

                        waitingType = WaitingType.SET_IIN;


                    return COMEBACK;

                case SET_IIN:
                    deleteUpdateMess();
                    try {
                        Long.parseLong(update.getMessage().getText());
                    }
                    catch (NumberFormatException e) {
                        wrongIinNotNumber();
                        deleteMessageId = getIin();
                        return COMEBACK;
                    }
                    if (update.hasMessage() && update.getMessage().hasText() && update.getMessage().getText().length() == 12) {

                        //todo check recipient
                        deleteMess();
                        recipient.setIin(update.getMessage().getText());

                        if (recipientRepository.findByIin(update.getMessage().getText()) == null){
                            deleteMessageId = getName();
                            waitingType = WaitingType.SET_FULL_NAME;
                        }
                        else{
                            sendMessageWithKeyboard(getText(81), 54);
                            waitingType = WaitingType.YES_NO;
                            return COMEBACK;
                        }
                    }
                    else {
                        wrongData();
                        deleteMessageId = getIin();
                    }
                    if (userRepository.findByIin(updateMessageText) != null) {
                        isReg = true;
                    }

                    return COMEBACK;

                case YES_NO:
                    deleteUpdateMess();
                    if (hasCallbackQuery()){
                        if (isButton(108)){ // re registr
                            deleteMess();
                            deleteMessageId = getName();

                            recipient.setId(recipientRepository.findByIin(recipient.getIin()).getId());
                            if (recipientRepository.findByIin(recipient.getIin()).getDistrict() != null)
                                recipient.setDistrict(recipientRepository.findByIin(recipient.getIin()).getDistrict());
//                            recipient = recipientRepository.findByIin(recipient.getIin());
                            waitingType = WaitingType.SET_FULL_NAME;
                        }
                        else if (isButton(96)){ // cancel
                            deleteMess();
                            sendMessageWithKeyboard(getText(35), 28);
                            return EXIT;
                        }
                        else{
                            wrongData();
                            sendMessageWithKeyboard(getText(81), 54);

                        }
                    }
                    else{
                        wrongData();
                        sendMessageWithKeyboard(getText(81), 54);
                    }
                    return COMEBACK;

                case SET_FULL_NAME:
                    deleteUpdateMess();
                    if (update.hasMessage() && update.getMessage().hasText() && update.getMessage().getText().length() <= 50) {
                        deleteMess();
                        recipient.setFullName(update.getMessage().getText());
                        deleteMessageId = getPhone();
                        waitingType = WaitingType.SET_PHONE_NUMBER;
                    } else {
                        wrongData();
                        deleteMessageId = getName();

                    }
                    return COMEBACK;

                case SET_PHONE_NUMBER:
                    deleteUpdateMess();
                    if(hasMessageText()){
                        deleteMess();
                        recipient.setPhoneNumber(update.getMessage().getText());
                        deleteMessageId = getStatus();
                        waitingType = WaitingType.SET_STATUS;
                    }
                    else{
                        deleteMessageId = wrongData();
                    }
                    return COMEBACK;
                case SET_STATUS:
                    delete();
                    if (hasCallbackQuery()) {
                        if (!update.getCallbackQuery().getData().equals(getText(Const.SKIP_MESSAGE))) {
                            Status status = statusRepository.findById(Integer.parseInt(update.getCallbackQuery().getData()));
                            statuses.add(status);
                            if (status.getStatusId() == 2) {
                                deleteMessageId = getInStatus(1);
                                waitingType = WaitingType.SET_STATUS;
                            } else {
                                deleteMessageId = getInStatus(3);
                                waitingType = WaitingType.SET_IN_STATUS;
                            }
                        } else {
                            deleteMessageId = getAddress();
                            waitingType = WaitingType.SET_ADDRESS;
                        }
                    } else {
                        wrongData();
                        deleteMessageId = getStatus();
                    }
                    return COMEBACK;
                case SET_IN_STATUS:
                    delete();
                    if (hasCallbackQuery()) {
                        if (!update.getCallbackQuery().getData().equals(getText(Const.SKIP_MESSAGE))) {
                            statuses.add(statusRepository.getOne(Integer.parseInt(update.getCallbackQuery().getData())));
                            recipient.setStatus(statuses);
                            deleteMessageId = getAddress();
                            waitingType = WaitingType.SET_ADDRESS;
                        }

                    } else {
                        wrongData();
                        deleteMessageId = getInStatus(3);
                    }
                    return COMEBACK;
                case SET_ADDRESS:
                    deleteMess();
                   if (hasMessageText()) {
                        recipient.setAddress(updateMessageText);
                    }
                   else {
                        secondDeleteMessageId = wrongData();
                        deleteMessageId = getAddress();
                        return COMEBACK;
                    }
                    deleteMessageId = getVisa();
                    waitingType = WaitingType.SET_VISA;
                    return COMEBACK;
                case SET_VISA:
                    deleteMess();
                    if (hasCallbackQuery()) {
                        if (!list.get(Integer.parseInt(updateMessageText)).equals(getText(Const.SKIP_MESSAGE))) {
                            recipient.setVisa(list.get(Integer.parseInt(updateMessageText)));
                        }
                        deleteMessageId = getApartment();
                        waitingType = WaitingType.SET_APARTMENT;
                    } else {
                        secondDeleteMessageId = wrongData();
                        deleteMessageId = getVisa();
                    }
                    return COMEBACK;
                case SET_APARTMENT:
                    deleteMess();
                    if (hasCallbackQuery()) {
                        if (!list.get(Integer.parseInt(updateMessageText)).equals(getText(Const.SKIP_MESSAGE))) {
                            if (list.get(Integer.parseInt(updateMessageText)).equals(getText(Const.OTHERS_MESSAGE))) {
                                deleteMessageId = getOther();
                                waitingType = WaitingType.OTHER_APARTMENT;
                                return COMEBACK;
                            } else {
                                recipient.setApartment(list.get(Integer.parseInt(updateMessageText)));
                            }
                        }
                        deleteMessageId = getChildren();
                        waitingType = WaitingType.ACTION_MENU;
                    } else {
                        secondDeleteMessageId = wrongData();
                        deleteMessageId = getApartment();
                    }
                    return COMEBACK;
                case OTHER_APARTMENT:
                    deleteMess();
                    if (hasMessageText()) {
                        recipient.setApartment(updateMessageText);
                        deleteMessageId = getChildren();
                        waitingType = WaitingType.ACTION_MENU;
                    } else {
                        secondDeleteMessageId = wrongData();
                        deleteMessageId = getOther();
                    }
                    return COMEBACK;
                case ACTION_MENU:
                    deleteMess();
                    if (isButton(Const.NEXT_BUTTON)) {
                        StringBuilder stringBuilder = new StringBuilder();

                        for (String status : socialBenefitsList) {
                            stringBuilder.append(status).append(Const.SPLIT).append(space);
                        }
                        if (!stringBuilder.toString().equals(""))
                            recipient.setChildren(stringBuilder.toString().substring(0, stringBuilder.length() - 2));
                        deleteMessageId = getSocialBenefits();
                        waitingType = WaitingType.SOCIAL_BENEFITS;
                        socialBenefitsList.clear();
                    } else if (isButton(Const.ADD_KIDS_BUTTON)) {
                        deleteMessageId = getChildrenIin();
                        waitingType = WaitingType.SET_CHILDREN;
                    } else {
                        secondDeleteMessageId = wrongData();
                        deleteMessageId = getChildren();
                    }
                    return COMEBACK;
                case SET_CHILDREN:
                    deleteMess();
                    if (hasMessageText()) {
                        socialBenefitsList.add(updateMessageText);
                        deleteMessageId = getChildren();
                        waitingType = WaitingType.ACTION_MENU;
                    } else {
                        secondDeleteMessageId = wrongData();
                        deleteMessageId = getChildrenIin();
                    }
                    return COMEBACK;
                case SOCIAL_BENEFITS:
                    deleteMess();
                    if (hasCallbackQuery()) {
                        if (!list.get(Integer.parseInt(updateMessageText)).equals(getText(Const.SKIP_MESSAGE))) {
                            if (list.get(Integer.parseInt(updateMessageText)).equals(getText(Const.NEXT_MESSAGE))) {
                                StringBuilder stringBuilder = new StringBuilder();
//                                if (isUpdate) {
//                                    if (recipient.getSocialBenefits() != null)
//                                        socialBenefitsList.add(0, recipient.getSocialBenefits());
//                                }
                                for (String socialBenefits : socialBenefitsList) {
                                    stringBuilder.append(socialBenefits).append(Const.SPLIT).append(space);
                                }
                                if (!stringBuilder.toString().equals(""))
                                    recipient.setSocialBenefits(stringBuilder.toString().substring(0, stringBuilder.length() - 2));
//                                if (isUpdate) recipientRepository.save(recipient);
                                deleteMessageId = getAliments();
                                waitingType = WaitingType.SET_ALIMENTS;
                                socialBenefitsList.clear();
                            } else if (list.get(Integer.parseInt(updateMessageText)).equals(getText(Const.OTHERS_MESSAGE))) {
                                deleteMessageId = getOther();
                                waitingType = WaitingType.OTHER_SOCIAL_BENEFITS;
                                return COMEBACK;
                            } else {
                                socialBenefitsList.add(list.get(Integer.parseInt(updateMessageText)));
                                deleteMessageId = getSocialBenefits();
                                waitingType = WaitingType.SOCIAL_BENEFITS;
                            }
                        } else {
                            deleteMessageId = getAliments();
                            waitingType = WaitingType.SET_ALIMENTS;
                        }
                    } else {
                        secondDeleteMessageId = wrongData();
                        deleteMessageId = getSocialBenefits();
                    }
                    return COMEBACK;
                case OTHER_SOCIAL_BENEFITS:
                    deleteMess();
                    if (hasMessageText()) {
                        socialBenefitsList.add(updateMessageText);
                        deleteMessageId = getSocialBenefits();
                        waitingType = WaitingType.SOCIAL_BENEFITS;
                    } else {
                        secondDeleteMessageId = wrongData();
                        deleteMessageId = getOther();
                    }
                    return COMEBACK;
//                case SET_MARITAL_STATUS:
//                    deleteMess();
//                    if (hasCallbackQuery()) {
//                        if (!list.get(Integer.parseInt(updateMessageText)).equals(getText(Const.SKIP_MESSAGE))) {
//                            recipient.setMaritalStatus(list.get(Integer.parseInt(updateMessageText)));
////                            if (isUpdate) recipientRepository.save(recipient);
//                            if (!list.get(Integer.parseInt(updateMessageText)).equals("Замужем/Женат") &&
//                                    !list.get(Integer.parseInt(updateMessageText)).equals("Сожительство") &&
//                                    !list.get(Integer.parseInt(updateMessageText)).equals("Не замужем/не женат")) {
//                                deleteMessageId = getAliments();
//                                waitingType = WaitingType.SET_ALIMENTS;
//                            } else {
//                                recipient.setAliments("Не получаю");
////                                if (isUpdate) recipientRepository.save(recipient);
//                                deleteMessageId = getEmploymentType();
//                                waitingType = WaitingType.EMPLOYMENT_TYPE;
//                            }
//                        } else {
//                            deleteMessageId = getEmploymentType();
//                            waitingType = WaitingType.EMPLOYMENT_TYPE;
//                        }
//                    } else {
//                        secondDeleteMessageId = wrongData();
//                        deleteMessageId = getMaritalStatus();
//                    }
//                    return COMEBACK;
                case SET_ALIMENTS:
                    deleteMess();
                    if (hasCallbackQuery()) {
                        if (!list.get(Integer.parseInt(updateMessageText)).equals(getText(Const.SKIP_MESSAGE))) {
                            recipient.setAliments(list.get(Integer.parseInt(updateMessageText)));
//                            if (isUpdate) recipientRepository.save(recipient);
                        }
                        deleteMessageId = getEmploymentType();
                        waitingType = WaitingType.EMPLOYMENT_TYPE;
                    } else {
                        secondDeleteMessageId = wrongData();
                        deleteMessageId = getAliments();
                    }
                    return COMEBACK;
                case EMPLOYMENT_TYPE:
                    deleteMess();
                    if (hasCallbackQuery()) {
                        if (!list.get(Integer.parseInt(updateMessageText)).equals(getText(Const.SKIP_MESSAGE))) {
                            if (list.get(Integer.parseInt(updateMessageText)).equals("Работаю")) {
                                recipient.setEmploymentType(list.get(Integer.parseInt(updateMessageText)));
                                deleteMessageId = getEmployment();
                                waitingType = WaitingType.EMPLOYMENT;
                            } else if (list.get(Integer.parseInt(updateMessageText)).equals(getText(Const.OTHERS_MESSAGE))) {
                                deleteMessageId = getOther();
                                waitingType = WaitingType.OTHER_EMPLOYMENT_TYPE;
                            } else {
                                recipient.setEmploymentType(list.get(Integer.parseInt(updateMessageText)));
//                                if (isUpdate) recipientRepository.save(recipient);
                                deleteMessageId = getEducation();
                                waitingType = WaitingType.EDUCATION;
                            }
                        } else {
                            deleteMessageId = getEducation();
                            waitingType = WaitingType.EDUCATION;
                        }
                    } else {
                        secondDeleteMessageId = wrongData();
                        deleteMessageId = getEmploymentType();
                    }
                    return COMEBACK;
                case EMPLOYMENT:
                    deleteMess();
                    if (hasMessageText()) {
                        recipient.setEmployment(updateMessageText);
//                        if (isUpdate) recipientRepository.save(recipient);
                        deleteMessageId = getEducation();
                        waitingType = WaitingType.EDUCATION;
                    } else {
                        secondDeleteMessageId = wrongData();
                        deleteMessageId = getEmployment();
                    }
                    return COMEBACK;
                case OTHER_EMPLOYMENT_TYPE:
                    deleteMess();
                    if (hasMessageText()) {
                        recipient.setEmploymentType(updateMessageText);
//                        if (isUpdate) recipientRepository.save(recipient);
                        deleteMessageId = getEducation();
                        waitingType = WaitingType.EDUCATION;
                    } else {
                        secondDeleteMessageId = wrongData();
                        deleteMessageId = getOther();
                    }
                    return COMEBACK;
                case EDUCATION:
                    deleteMess();
                    if (hasCallbackQuery()) {
                        if (!list.get(Integer.parseInt(updateMessageText)).equals(getText(Const.SKIP_MESSAGE))) {
                            if (list.get(Integer.parseInt(updateMessageText)).equals("Школа")) {
                                recipient.setEducation(list.get(Integer.parseInt(updateMessageText)));
//                                if (isUpdate) recipientRepository.save(recipient);
                                deleteMessageId = getDisabilityType();
                                waitingType = WaitingType.DISABILITY_TYPE;
                            } else {
                                recipient.setEducation(list.get(Integer.parseInt(updateMessageText)));
                                deleteMessageId = getNameOrSpeciality();
                                waitingType = WaitingType.SET_EDUCATION_NAME;
                            }
                        } else {
                            deleteMessageId = getDisabilityType();
                            waitingType = WaitingType.DISABILITY_TYPE;
                        }
                    } else {
                        secondDeleteMessageId = wrongData();
                        deleteMessageId = getEducation();
                    }
                    return COMEBACK;
                case SET_EDUCATION_NAME:
                    deleteMess();
                    if (hasMessageText()) {
                        recipient.setEducationName(updateMessageText);
//                        if (isUpdate) recipientRepository.save(recipient);
                        deleteMessageId = getDisabilityType();
                        waitingType = WaitingType.DISABILITY_TYPE;
                    } else {
                        secondDeleteMessageId = wrongData();
                        deleteMessageId = getNameOrSpeciality();
                    }
                    return COMEBACK;
                case DISABILITY_TYPE:
                    deleteMess();
                    if (hasCallbackQuery()) {
                        if (!list.get(Integer.parseInt(updateMessageText)).equals(getText(Const.SKIP_MESSAGE))) {
                            if (list.get(Integer.parseInt(updateMessageText)).equals("Инвалидность мамы/папы")) {
                                recipient.setDisabilityType(list.get(Integer.parseInt(updateMessageText)));
//                                if (isUpdate) recipientRepository.save(recipient);
                                deleteMessageId = sendMessage("ФИО, группа");
                                waitingType = WaitingType.SET_MOTHER_FATHER_FULL_NAME;
                            } else if (list.get(Integer.parseInt(updateMessageText)).equals("Инвалидность ребенка")) {
                                recipient.setDisabilityType(list.get(Integer.parseInt(updateMessageText)));
//                                if (isUpdate) recipientRepository.save(recipient);
                                deleteMessageId = sendMessage("ФИО, группа, диагноз");
                                waitingType = WaitingType.SET_MOTHER_FATHER_FULL_NAME;
                            } else if (list.get(Integer.parseInt(updateMessageText)).equals("Инвалидность других членов семьи (бабушка, дедушка и т.д.)")) {
                                recipient.setDisabilityType(list.get(Integer.parseInt(updateMessageText)));
//                                if (isUpdate) recipientRepository.save(recipient);
                                deleteMessageId = sendMessage("ФИО, год рождения");
                                waitingType = WaitingType.SET_MOTHER_FATHER_FULL_NAME;
                            } else {
                                deleteMessageId = getCreditHistory();
                                waitingType = WaitingType.SET_CREDIT_HISTORY;
                            }
                        } else {
                            deleteMessageId = getCreditHistory();
                            waitingType = WaitingType.SET_CREDIT_HISTORY;
                        }
                    } else {
                        secondDeleteMessageId = wrongData();
                        deleteMessageId = getDisabilityType();
                    }
                    return COMEBACK;
                case SET_MOTHER_FATHER_FULL_NAME:
                    deleteMess();
                    if (hasMessageText()) {
                        recipient.setDisability(updateMessageText);
//                        if (isUpdate) recipientRepository.save(recipient);
                        deleteMessageId = getCreditHistory();
                        waitingType = WaitingType.SET_CREDIT_HISTORY;
                    } else {
                        deleteMessageId = sendMessage("ФИО, группа");
                    }
                    return COMEBACK;
                case SET_CREDIT_HISTORY:
                    deleteMess();
                    if (hasCallbackQuery()) {
                        if (list.get(Integer.parseInt(updateMessageText)).equals("есть кредит")) {
                            recipient.setCreditHistory(list.get(Integer.parseInt(updateMessageText)));
                            deleteMessageId = getCreditInfo();
                            waitingType = WaitingType.SET_CREDIT_INFO;
                        } else if (list.get(Integer.parseInt(updateMessageText)).equals("есть задолжность")) {
                            recipient.setCreditHistory(list.get(Integer.parseInt(updateMessageText)));
                            deleteMessageId = getBankName();
                            waitingType = WaitingType.SET_CREDIT_INFO_OTHER;
                        } else if (list.get(Integer.parseInt(updateMessageText)).equals("коллекторы (какой банк)")) {
                            recipient.setCreditHistory(list.get(Integer.parseInt(updateMessageText)));
                            deleteMessageId = getBankName();
                            waitingType = WaitingType.SET_CREDIT_INFO_OTHER;
                        } else if (list.get(Integer.parseInt(updateMessageText)).equals("Другое")) {
                            deleteMessageId = getOther();
                            waitingType = WaitingType.SET_OTHER_CREDIT_HISTORY;
                        } else {
                            recipient.setCreditHistory(list.get(Integer.parseInt(updateMessageText)));
//                            if (isUpdate) {
//                                recipientRepository.save(recipient);
//                            } else {
//                            }
                            if(recipient.getDistrict() == null)
                                recipient.setDistrict(new Bot().getTableSchema());
                            recipientRepository.save(recipient);
//                            sendMessage(Const.DONE_JOIN_MESSAGE);
                            sendMessageWithKeyboard(getText(Const.DONE_JOIN_MESSAGE), 28);

                            return EXIT;
                        }
                    } else {
                        secondDeleteMessageId = wrongData();
                        deleteMessageId = getCreditHistory();
                    }
                    return COMEBACK;
                case SET_OTHER_CREDIT_HISTORY:
                    deleteMess();
                    if (hasMessageText()) {
                        recipient.setCreditHistory(updateMessageText);
//                        if (isUpdate) {
//                            recipientRepository.save(recipient);
//                        } else {
//                        }

                        if(recipient.getDistrict() == null)
                            recipient.setDistrict(new Bot().getTableSchema());
                        recipientRepository.save(recipient);
//                        sendMessage(Const.DONE_JOIN_MESSAGE);
                        sendMessageWithKeyboard(getText(Const.DONE_JOIN_MESSAGE), 28);

                        return EXIT;
                    } else {
                        secondDeleteMessageId = wrongData();
                        deleteMessageId = getOther();
                    }
                    return COMEBACK;
                case SET_CREDIT_INFO:
                    deleteMess();
                    if (hasMessageText()) {
                        recipient.setCreditInfo(updateMessageText);
//                        if (isUpdate) {
//                            recipientRepository.save(recipient);
//                        } else {
//                        }

                        if(recipient.getDistrict() == null)
                            recipient.setDistrict(new Bot().getTableSchema());
                        recipientRepository.save(recipient);
//                        sendMessage(Const.DONE_JOIN_MESSAGE);
                        sendMessageWithKeyboard(getText(Const.DONE_JOIN_MESSAGE), 28);

                        return EXIT;
                    } else {
                        secondDeleteMessageId = wrongData();
                        deleteMessageId = getCreditInfo();
                    }
                    return COMEBACK;
                case SET_CREDIT_INFO_OTHER:
                    deleteMess();
                    if (hasMessageText()) {
                        recipient.setCreditInfo(updateMessageText);
//                        if (isUpdate && isReg) {
//                            recipientRepository.save(recipient);
//                        } else {
//                        }

                        if(recipient.getDistrict() == null)
                            recipient.setDistrict(new Bot().getTableSchema());
                        recipientRepository.save(recipient);
//                        sendMessage(Const.DONE_JOIN_MESSAGE);
                        sendMessageWithKeyboard(getText(Const.DONE_JOIN_MESSAGE), 28);
                        return EXIT;
                    } else {
                        secondDeleteMessageId = wrongData();
                        deleteMessageId = getBankName();
                    }
                    return COMEBACK;

            }

        return EXIT;
    }

    private int getStatus() throws TelegramApiException {
        list = new ArrayList<>();
        List<String> listIds = new ArrayList<>();
        List<Status> statuses = statusRepository.findAllByStatusId(1);
        statuses.addAll(statusRepository.findAllByStatusId(2));
        for (Status status : statuses) {
            if (getLanguage().getId() == 1) {
                list.add(status.getNameRus());
            } else {
                list.add(status.getNameKaz());
            }
            listIds.add(String.valueOf(status.getId()));
        }

        if (recipient.getStatus() != null && recipient.getStatus().size() != 0) {
            list.add(getText(Const.SKIP_MESSAGE));
            listIds.add(getText(Const.SKIP_MESSAGE));
            buttonsLeaf = new ButtonsLeaf(list, listIds);
            StringBuilder stringBuilder = new StringBuilder();
            for (Status status : recipient.getStatus()) {
                if (getLanguage().getId() == 1) {
                    stringBuilder.append(status.getNameRus()).append("; ");
                } else {
                    stringBuilder.append(status.getNameKaz()).append("; ");
                }
            }
            return toDeleteKeyboard(sendMessageWithKeyboard(getUpdateText(stringBuilder.toString(), getText(1088)), buttonsLeaf.getListButtonWhereIdIsData()));
        } else {
            buttonsLeaf = new ButtonsLeaf(list, listIds);
            return toDeleteKeyboard(sendMessageWithKeyboard(getText(1088), buttonsLeaf.getListButtonWhereIdIsData()));
        }
    }

    private int getInStatus(int statusId) throws TelegramApiException {
        list = new ArrayList<>();
        List<String> listIds = new ArrayList<>();
        for (Status status : statusRepository.findAllByStatusId(statusId)) {
            if (getLanguage().getId() == 1) {
                list.add(status.getNameRus());
            } else {
                list.add(status.getNameKaz());
            }
            listIds.add(String.valueOf(status.getId()));
        }

        buttonsLeaf = new ButtonsLeaf(list, listIds);

        return toDeleteKeyboard(sendMessageWithKeyboard(getText(1088), buttonsLeaf.getListButtonWhereIdIsData()));

    }

    private void delete() {
        deleteMessage(updateMessageId);
        deleteMessage(deleteMessageId);
        deleteMessage(secondDeleteMessageId);
    }

    private void deleteUpdateMess() {
        deleteMessage(updateMessageId);
    }

    private int wrongData() throws TelegramApiException {
        return sendMessageWithKeyboard(getText(Const.WRONG_DATA_TEXT), 28);
    }

    private int getName() throws TelegramApiException {
        return sendMessage(getText(Const.SET_FULL_NAME_MESSAGE));
    }

    private int getPhone() throws TelegramApiException {
        return sendMessage(getText(Const.SEND_USERS_CONTACT));
    }

    private int getIin() throws TelegramApiException {
        return sendMessage(getText(Const.SET_IIN_MESSAGE));
    }

    private int wrongIinNotNumber() throws TelegramApiException {
        return sendMessage(getText(Const.IIN_WRONG_MESSAGE));
    }

    private int getOther() throws TelegramApiException {
        return sendMessage(getText(Const.SET_YOUR_OPTION_MESSAGE));
    }

    public String getText(int messageIdFromDb) {
        return messageRepository.getMessageText(messageIdFromDb, 1);
    }

    private int getAddress() throws TelegramApiException {
       /* if (isUpdate) {
            list = new ArrayList<>();
            list.add(getText(Const.SKIP_MESSAGE));
            buttonsLeaf = new ButtonsLeaf(list);
            return toDeleteKeyboard(sendMessageWithKeyboard(getUpdateText(recipient.getAddress(), getText(Const.SET_ADDRESS_MESSAGE)), buttonsLeaf.getListButton()));
        } else {
        }*/
        return sendMessage(Const.SET_ADDRESS_MESSAGE);
    }

    private String getUpdateText(String currentInfo, String sendMessage) {
        String format = getText(Const.UPDATE_REGISTRATION_MESSAGE);
        return String.format(format, currentInfo != null ? currentInfo : " ", sendMessage);
    }

    private int getVisa() throws TelegramApiException {
        list.clear();
        Arrays.asList(getText(Const.REGISTRATION_TYPE_MESSAGE).split(Const.SPLIT)).forEach((e) -> list.add(e));
        buttonsLeaf = new ButtonsLeaf(list);
//        if (isUpdate) list.add(getText(Const.SKIP_MESSAGE));
//        if (isUpdate) {
//            return toDeleteKeyboard(sendMessageWithKeyboard(getUpdateText(recipient.getVisa(), getText(Const.TYPE_OF_REGISTRATION)), buttonsLeaf.getListButton()));
//        }
//        else {
//        }
        return toDeleteKeyboard(sendMessageWithKeyboard(getText(Const.TYPE_OF_REGISTRATION), buttonsLeaf.getListButton()));
    }

    private int getApartment() throws TelegramApiException {
        list.clear();
        Arrays.asList(getText(Const.APARTMENT_MESSAGE).split(Const.SPLIT)).forEach((e) -> list.add(e));
        list.add(getText(Const.OTHERS_MESSAGE));
//        if (isUpdate) list.add(getText(Const.SKIP_MESSAGE));
        buttonsLeaf = new ButtonsLeaf(list);
//        if (isUpdate) {
//            return toDeleteKeyboard(sendMessageWithKeyboard(getUpdateText(recipient.getApartment(), getText(Const.HOUSING_AVAILABILITY_MESSAGE)), buttonsLeaf.getListButton()));
//        } else {
//        }
        return toDeleteKeyboard(sendMessageWithKeyboard(getText(Const.HOUSING_AVAILABILITY_MESSAGE), buttonsLeaf.getListButton()));
    }

    private int getChildren() throws TelegramApiException {
        return sendMessage(Const.SET_CHILDREN_MESSAGE);
    }

    private int getChildrenIin() throws TelegramApiException {
        return sendMessage(Const.NAME_CHILDREN_MESSAGE);
    }

    private int getSocialBenefits() throws TelegramApiException {
        list.clear();
        Arrays.asList(getText(Const.SOCIAL_BENEFITS_MESSAGE).split(Const.SPLIT)).forEach((e) -> list.add(e));
        list.add(getText(Const.OTHERS_MESSAGE));
        list.add(getText(Const.NEXT_MESSAGE));
//        if (isUpdate) list.add(getText(Const.SKIP_MESSAGE));
        buttonsLeaf = new ButtonsLeaf(list);
//        if (isUpdate) {
//            return toDeleteKeyboard(sendMessageWithKeyboard(getUpdateText(recipient.getSocialBenefits(), getText(Const.SOCIAL_PENSION_MESSAGE)), buttonsLeaf.getListButton()));
//        } else {
//        }
        return toDeleteKeyboard(sendMessageWithKeyboard(getText(Const.SOCIAL_PENSION_MESSAGE), buttonsLeaf.getListButton()));
    }

    private int getMaritalStatus() throws TelegramApiException {
        list.clear();
        Arrays.asList(getText(Const.MARRIED_TYPE_MESSAGE).split(Const.SPLIT)).forEach((e) -> list.add(e));
//        if (isUpdate) list.add(getText(Const.SKIP_MESSAGE));
        buttonsLeaf = new ButtonsLeaf(list);
//        if (isUpdate) {
//            return toDeleteKeyboard(sendMessageWithKeyboard(getUpdateText(recipient.getMaritalStatus(), getText(Const.MARITAL_STATUS_MESSAGE)), buttonsLeaf.getListButton()));
//        } else {
//        }
        return toDeleteKeyboard(sendMessageWithKeyboard(getText(Const.MARITAL_STATUS_MESSAGE), buttonsLeaf.getListButton()));
    }

    private int getAliments() throws TelegramApiException {
        list.clear();
        Arrays.asList(getText(Const.ALIMENT_TYPE_MESSAGE).split(Const.SPLIT)).forEach((e) -> list.add(e));
//        if (isUpdate) list.add(getText(Const.SKIP_MESSAGE));
        buttonsLeaf = new ButtonsLeaf(list);
//        if (isUpdate) {
//            return toDeleteKeyboard(sendMessageWithKeyboard(getUpdateText(recipient.getAliments(), getText(Const.ALIMENTS_MESSAGE)), buttonsLeaf.getListButton()));
//        } else {
//        }
        return toDeleteKeyboard(sendMessageWithKeyboard(getText(Const.ALIMENTS_MESSAGE), buttonsLeaf.getListButton()));
    }

    private int getEmploymentType() throws TelegramApiException {
        list.clear();
        Arrays.asList(getText(Const.EMPLOYEE_TYPE_MESSAGE).split(Const.SPLIT)).forEach((e) -> list.add(e));
        list.add(getText(Const.OTHERS_MESSAGE));
//        if (isUpdate) list.add(getText(Const.SKIP_MESSAGE));
        buttonsLeaf = new ButtonsLeaf(list);
//        if (isUpdate) {
//            return toDeleteKeyboard(sendMessageWithKeyboard(getUpdateText(recipient.getEmploymentType(), getText(Const.EMPLOYMENT_MESSAGE)), buttonsLeaf.getListButton()));
//        } else {
//        }
        return toDeleteKeyboard(sendMessageWithKeyboard(getText(Const.EMPLOYMENT_MESSAGE), buttonsLeaf.getListButton()));
    }

    private int getEmployment() throws TelegramApiException {
        return sendMessage(Const.WORK_PLACE_MESSAGE);
    }

    private int getEducation() throws TelegramApiException {
        list.clear();
        Arrays.asList(getText(Const.EDUCATION_TYPE_CHOOSE_MESSAGE).split(Const.SPLIT)).forEach((e) -> list.add(e));
        buttonsLeaf = new ButtonsLeaf(list);
//        if (isUpdate) list.add(getText(Const.SKIP_MESSAGE));
//        if (isUpdate) {
//            return toDeleteKeyboard(sendMessageWithKeyboard(getUpdateText(recipient.getEducation(), getText(Const.EDUCATION_MESSAGE)), buttonsLeaf.getListButton()));
//        } else {
//        }
        return toDeleteKeyboard(sendMessageWithKeyboard(getText(Const.EDUCATION_MESSAGE), buttonsLeaf.getListButton()));
    }

    private int getDisabilityType() throws TelegramApiException {
        list.clear();
        Arrays.asList(getText(Const.DISABILITY_CHOOSE_TYPE_MESSAGE).split(Const.SPLIT)).forEach((e) -> list.add(e));
        list.add(getText(Const.SKIP_MESSAGE));
        buttonsLeaf = new ButtonsLeaf(list);
//        if (isUpdate) {
//            return toDeleteKeyboard(sendMessageWithKeyboard(getUpdateText(recipient.getDisabilityType(), getText(Const.DISABILITY_TYPE_MESSAGE)), buttonsLeaf.getListButton()));
//        } else {
//        }
        return toDeleteKeyboard(sendMessageWithKeyboard(getText(Const.DISABILITY_TYPE_MESSAGE), buttonsLeaf.getListButton()));
    }

    private int getNameOrSpeciality() throws TelegramApiException {
        return sendMessage(Const.NAME_OR_SPECIALITY_MESSAGE);
    }

    private int getCreditHistory() throws TelegramApiException {
        list.clear();
        Arrays.asList(getText(Const.CREDIT_TYPE_MESSAGE).split(Const.SPLIT)).forEach((e) -> list.add(e));
        list.add(getText(Const.OTHERS_MESSAGE));
        buttonsLeaf = new ButtonsLeaf(list);
        return toDeleteKeyboard(sendMessageWithKeyboard(getText(Const.CREDIT_HISTORY_MESSAGE), buttonsLeaf.getListButton()));
    }

    private int getCreditInfo() throws TelegramApiException {
        return sendMessage(Const.CREDIT_INFO_MESSAGE    );
    }

    private int getBankName() throws TelegramApiException {
        return sendMessage(Const.BANK_NAME_MESSAGE);
    }
    private void deleteMess(){
        deleteMessage(updateMessageId);
        deleteMessage(deleteMessageId);
        deleteMessage(secondDeleteMessageId);
    }

}
