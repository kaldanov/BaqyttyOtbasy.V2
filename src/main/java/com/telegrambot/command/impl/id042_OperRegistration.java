package com.telegrambot.command.impl;

import com.telegrambot.command.Command;
import com.telegrambot.config.Bot;
import com.telegrambot.entity.custom.*;
import com.telegrambot.entity.enums.WaitingType;
import com.telegrambot.repository.StatusRepository;
import com.telegrambot.repository.TelegramBotRepositoryProvider;
import com.telegrambot.util.ButtonsLeaf;
import com.telegrambot.util.Const;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;


public class id042_OperRegistration extends Command {
    //    private User user;
    private Recipient recipient;
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
    private Category_Indicator currentCategory;
    private long currentCategoryId;

    private List<Service> servicesOfCategory;
    private Service currentService;
    private long currentServiceId;


    private List<Specialist> specialistsOfService;
    private Specialist currentSpecialist;
    private long currentSpecialistId;
    private StringBuilder currentSpecInfo;

    private List<Services_Spec> services_specs;

    private String currentIin;
    List<String> children;
    List<String> newChildren;

    private boolean isStatus = false;

    private List<Status> statuses = new ArrayList<>();

    private StatusRepository statusRepository = TelegramBotRepositoryProvider.getStatusRepository();


    @Override
    public boolean execute() throws TelegramApiException {
        if (isOper()) {

            switch (waitingType) {
                case START:
                    if (userRepository.findByChatId(chatId) == null) {
                        sendMessageWithKeyboard("Вы не прошли регистрацию", 57);
                        return EXIT;
                    }

                    deleteMess();
                    if (isButton(1037)) {  // recipient by operator
                        deleteMessageId = sendMessage(Const.SET_IIN_MESSAGE);
                        recipient = new Recipient();
                        recipient.setRegistrationDate(new Date());
                        recipient.setDistrict(new Bot().getTableSchema());
                        waitingType = WaitingType.SET_IIN;
                    } else if (isButton(102)) { // request for service by opeator
                        deleteMessageId = sendMessage(Const.SET_IIN_MESSAGE);
                        waitingType = WaitingType.SET_IIN_FOR_REQUEST;
                    } else
                        sendMessageWithKeyboard(getText(1179), 31);

                    return COMEBACK;

                case SET_IIN_FOR_REQUEST:

                    deleteMess();
                    try {
                        Long.parseLong(update.getMessage().getText());
                    } catch (NumberFormatException e) {
                        wrongIinNotNumber();
                        getIin();
                        return COMEBACK;
                    }
                    if (update.hasMessage() && update.getMessage().hasText() && update.getMessage().getText().length() == 12) {

                        if (recipientRepository.findByIin(update.getMessage().getText()) != null) {

                            currentIin = update.getMessage().getText();
                            sendRecipientInfo();
                            return COMEBACK;

                        } else {
                            sendMessageWithKeyboard(getText(68), 31);
                            return EXIT;
                        }
                    } else {
                        wrongData();
                        getIin();
                    }

                    return COMEBACK;

                case THIS_PERSON_OR_NO:
                    deleteMess();
                    if (hasCallbackQuery()) {
                        if (isButton(103)) { // choose
                            if (chekStatus()) {
                                isStatus = true;
                                return COMEBACK;
                            } else
                                getAllCategories();
                        } else if (isButton(1005)) { // back
                            deleteMessageId = sendMessage(Const.SET_IIN_MESSAGE);
                            waitingType = WaitingType.SET_IIN_FOR_REQUEST;
                        } else {
                            wrongData();
                            sendRecipientInfo();
                        }
                    } else {
                        wrongData();
                        sendRecipientInfo();
                    }


                    return COMEBACK;

                case CHOOSE_CATEGORY:
                    if (hasCallbackQuery()) {
                        currentCategoryId = Long.parseLong(updateMessageText);

                        currentCategory = categoriesIndicatorRepository.findBySecondAndLangId(currentCategoryId, getLanguage().getId());
                        getServicesOfCategory();
                    }

                    return COMEBACK;
                case CHOOSE_SERVICE:
                    deleteMess();
                    if (hasCallbackQuery()) {
                        currentServiceId = Long.parseLong(updateMessageText);
                        if (currentServiceId == -1) {
                            getAllCategories();
                        } else {
                            currentService = serviceRepository.findById2AndLangId(currentServiceId, getLanguage().getId());
//                            if (currentService.getStatusId() == 2){
//                                Calendar start = Calendar.getInstance();
//                                start.set(Calendar.DAY_OF_MONTH, 1);
//                                start.set(Calendar.MONTH, 1);
//                                start.set(Calendar.HOUR_OF_DAY, 0);
//                                start.set(Calendar.MINUTE, 0);
//                                start.set(Calendar.SECOND, 1);
//                                Calendar end = Calendar.getInstance();
//                                end.set(Calendar.MONTH, 12);
//                                end.set(Calendar.DAY_OF_MONTH, 31);
//                                end.set(Calendar.HOUR_OF_DAY, 23);
//                                end.set(Calendar.MINUTE, 59);
//                                end.set(Calendar.SECOND, 59);
//                                if (registrationServiceRepository.findAllByIinAndDateRegBetween(currentIin,start.getTime(), end.getTime()).size() > 0){
//                                    sendMessage("Вы уже полуили эту услугу за этот год!");
//                                    getServicesOfCategory();
//                                    return COMEBACK;
//                                }
//                            }
                            getSpecialists();
                        }
                    }
                    return COMEBACK;
                case CHOOSE_SPEC:
                    deleteMess();
                    if (hasCallbackQuery()) {
                        currentSpecialistId = Long.parseLong(updateMessageText);
                        if (currentSpecialistId == -1) {
                            getServicesOfCategory();
                        } else {

                            currentSpecialist = specialistRepository.findById(currentSpecialistId);

                            currentSpecInfo = new StringBuilder();

                            currentSpecInfo.append(getText(66)).append(currentSpecialist.getFullName()).append(next);
                            currentSpecInfo.append(getText(67)).append(getLanguage().getId() == 1 ? currentSpecialist.getDescriptionRus() : currentSpecialist.getDescriptionKaz()).append(next);
                            deleteMessageId = sendMessageWithPhotoAndKeyboard(currentSpecInfo.toString(), 5, currentSpecialist.getPhoto());

                            waitingType = WaitingType.REGISTER_TO_SPEC;

                        }
                    }
                    return COMEBACK;

                case REGISTER_TO_SPEC:
                    deleteMess();
                    if (hasCallbackQuery()) {

                        if (isButton(1005)) { // back
                            getSpecialists();
                        }
                        if (isButton(1004)) { // register

                            if (recipientRepository.findByIin(userRepository.findByChatId(chatId).getIin()) == null) {
                                sendMessageWithKeyboard(getText(68), 1);

                            }
//                               if (recipientRepository.findByChatId(chatId) == null){
//                                sendMessageWithKeyboard(getText(68) ,1);
//                            }
                            else {
                                deleteMessageId = sendMessageWithKeyboard(getText(114), 58);
                                waitingType = WaitingType.FOR_WHO;

                            }

//                            asd
//                            Registration_Service registration_service = new Registration_Service(chatId, new Date(), currentServiceId, currentSpecialistId, currentIin, false);
//                            registrationServiceRepository.save(registration_service);
//                            sendMessageToSpec(registration_service);
//                            sendMessageWithKeyboard(getText(76), 31);

                        }

                    }


                    return COMEBACK;
                case FOR_WHO:
                    if (hasCallbackQuery()) {
                        deleteUpdateMess();
                        if (isButton(115)) {  // for me
                            Registration_Service registration_service = new Registration_Service(chatId, new Date(), currentServiceId, currentSpecialistId, currentIin, false);
                            registrationServiceRepository.save(registration_service);
                            sendMessageWithKeyboard(getText(76), 31);
                            sendMessageToSpec(registration_service);
                            return EXIT;
                        } else if (isButton(116)) {  // for child
                            Recipient recipient = recipientRepository.findByIin(currentIin);

                            children = getChildren(recipient.getChildren());
                            if (children.size() == 0) {
                                deleteMessageId = sendMessageWithKeyboard(getText(116), 59);
                                newChildren = new ArrayList<>();
                                waitingType = WaitingType.SET_NEW_CHILDREN;
                                // todo set childs
                            } else {
                                children.add(buttonRepository.getButtonText(1005, getLanguage().getId()));
                                ButtonsLeaf childrenLeaf = new ButtonsLeaf(children, children, 100);
                                sendMessageWithKeyboard(getText(115), childrenLeaf.getListButtonWhereIdIsData());
                                waitingType = WaitingType.CHOOSE_CHILD;
                            }
                        } else if (isButton(1005)) {
                            deleteMessageId = sendMessageWithPhotoAndKeyboard(currentSpecInfo.toString(), 5, currentSpecialist.getPhoto());
                            waitingType = WaitingType.REGISTER_TO_SPEC;
                        }
                    }
                    return COMEBACK;
                case SET_NEW_CHILDREN:
                    deleteUpdateMess();
                    if (hasMessageText() && isIin(updateMessageText)) {
                        newChildren.add(updateMessageText);
                        editMessageWithKeyboard(getChildenInfo(newChildren), deleteMessageId, (InlineKeyboardMarkup) keyboardMarkUpService.select(59, chatId).get());
                    } else if (hasCallbackQuery() && isButton(1003)) {
                        deleteMess();
                        Recipient recipient7 = recipientRepository.findByIin(currentIin);
                        if (recipient7 != null) {
                            recipient7.setChildren(getForSetShildren(newChildren));
                            recipientRepository.save(recipient7);
                        }
                        children = newChildren;
                        ButtonsLeaf childrenLeaf = new ButtonsLeaf(children, children, 100);
                        sendMessageWithKeyboard(getText(115), childrenLeaf.getListButtonWhereIdIsData());
                        waitingType = WaitingType.CHOOSE_CHILD;
                    }
                    return COMEBACK;
                case CHOOSE_CHILD:
                    deleteMess();
                    if (hasCallbackQuery() && isButton(1005)) {
                        deleteUpdateMess();
                        deleteMessageId = sendMessageWithKeyboard(getText(114), 58);
                        waitingType = WaitingType.FOR_WHO;
                        return COMEBACK;
                    } else if (hasCallbackQuery() && children.contains(updateMessageText)) {
                        deleteUpdateMess();

                        Registration_Service registration_service = new Registration_Service(chatId, new Date(), currentServiceId, currentSpecialistId, updateMessageText, false);
                        registration_service.setParentIIN(currentIin);

                        registrationServiceRepository.save(registration_service);
                        sendMessageWithKeyboard(getText(76), 31);
                        sendMessageToSpec(registration_service);
                        return EXIT;
                    }

                    return COMEBACK;
                case SET_IIN:
                    deleteUpdateMess();
                    try {
                        Long.parseLong(update.getMessage().getText());
                    } catch (NumberFormatException e) {
                        wrongIinNotNumber();
                        deleteMessageId = getIin();
                        return COMEBACK;
                    }
                    if (update.hasMessage() && update.getMessage().hasText() && update.getMessage().getText().length() == 12) {

                        //todo check recipient
                        deleteMess();
                        currentIin = update.getMessage().getText();
                        recipient.setIin(update.getMessage().getText());

                        if (recipientRepository.findByIin(update.getMessage().getText()) == null) {
                            deleteMessageId = getName();
                            waitingType = WaitingType.SET_FULL_NAME;

                        } else {
                            sendMessageWithKeyboard(getText(81), 54);
                            waitingType = WaitingType.YES_NO;
                            return COMEBACK;
                        }
                    } else {
                        wrongData();
                        deleteMessageId = getIin();
                    }
                    if (userRepository.findByIin(updateMessageText) != null) {
                        isReg = true;
                    }

                    return COMEBACK;

                case YES_NO:
                    deleteUpdateMess();
                    if (hasCallbackQuery()) {
                        if (isButton(108)) { // re registr
                            deleteMess();
                            deleteMessageId = getName();
                            recipient = recipientRepository.findByIin(recipient.getIin());
                            waitingType = WaitingType.SET_FULL_NAME;
                        } else if (isButton(96)) { // cancel
                            deleteMess();
                            sendMessageWithKeyboard(getText(35), 31);
                            return EXIT;
                        } else {
                            wrongData();
                            sendMessageWithKeyboard(getText(81), 54);

                        }
                    } else {
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
                    if (hasMessageText()) {
                        deleteMess();
                        recipient.setPhoneNumber(update.getMessage().getText());
                        deleteMessageId = getStatus();
                        waitingType = WaitingType.SET_STATUS;
                    } else {
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
                            if (isStatus) {
                                Recipient recipient1 = recipientRepository.findByIin(currentIin);
                                recipient1.setStatus(statuses);
                                recipientRepository.save(recipient1);
                                getAllCategories();
                            } else {
                                recipient.setStatus(statuses);
                                deleteMessageId = getAddress();
                                waitingType = WaitingType.SET_ADDRESS;
                            }
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
//                        if (isUpdate) recipientRepository.save(recipient); asd
                    } else {
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
                            if (recipient.getDistrict() == null)
                                recipient.setDistrict(new Bot().getTableSchema());
                            recipient.setRegistrationDate(new Date());

                            recipientRepository.save(recipient);
//                            sendMessage(Const.DONE_JOIN_MESSAGE);
                            sendMessageWithKeyboard(getText(Const.DONE_JOIN_MESSAGE), 31);

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
                        if (recipient.getDistrict() == null)
                            recipient.setDistrict(new Bot().getTableSchema());
                        recipient.setRegistrationDate(new Date());

                        recipientRepository.save(recipient);
//                        sendMessage(Const.DONE_JOIN_MESSAGE);
                        sendMessageWithKeyboard(getText(Const.DONE_JOIN_MESSAGE), 31);

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
                        if (recipient.getDistrict() == null)
                            recipient.setDistrict(new Bot().getTableSchema());
                        recipient.setRegistrationDate(new Date());
                        recipientRepository.save(recipient);
//                        sendMessage(Const.DONE_JOIN_MESSAGE);
                        sendMessageWithKeyboard(getText(Const.DONE_JOIN_MESSAGE), 31);

                        return EXIT;
                    } else {
                        secondDeleteMessageId = wrongData();
                        deleteMessageId = getCreditInfo();
                    }
                    return COMEBACK;
                case SET_CREDIT_INFO_OTHER:
                    deleteMess();
                    if (hasMessageText()) {
                        recipient.setRegistrationDate(new Date());
                        recipient.setCreditInfo(updateMessageText);
//                        if (isUpdate && isReg) {
//                            recipientRepository.save(recipient);
//                        } else {
//                        }
                        if (recipient.getDistrict() == null)
                            recipient.setDistrict(new Bot().getTableSchema());
                        recipient.setRegistrationDate(new Date());

                        recipientRepository.save(recipient);
//                        sendMessage(Const.DONE_JOIN_MESSAGE);
                        sendMessageWithKeyboard(getText(Const.DONE_JOIN_MESSAGE), 31);
                        return EXIT;
                    } else {
                        secondDeleteMessageId = wrongData();
                        deleteMessageId = getBankName();
                    }
                    return COMEBACK;

            }

            return true;
        } else {
            sendMessage(Const.NO_ACCESS);
        }
        return EXIT;
    }

    private void sendRecipientInfo() throws TelegramApiException {
        Recipient recipientInfo = recipientRepository.findByIin(update.getMessage().getText());

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getText(89)).append(recipientInfo.getFullName()).append(next);
        stringBuilder.append(getText(97)).append(recipientInfo.getIin()).append(next);
        stringBuilder.append(getText(90)).append(recipientInfo.getPhoneNumber()).append(next);
        stringBuilder.append(getText(1088)).append(recipientInfo.getStatus() != null ? recipientInfo.getStatus() : "").append(next);
        stringBuilder.append(getText(91)).append(recipientInfo.getAddress()).append(next);

        deleteMessageId = sendMessageWithKeyboard(stringBuilder.toString(), 51);
        waitingType = WaitingType.THIS_PERSON_OR_NO;
    }

    private void deleteUpdateMess() {
        deleteMessage(updateMessageId);
    }

    private int wrongData() throws TelegramApiException {
        return sendMessageWithKeyboard(getText(Const.WRONG_DATA_TEXT), 31);
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


    private boolean chekStatus() throws TelegramApiException {
        Recipient recipient = recipientRepository.findByIin(currentIin);
        if (recipient.getStatus().size() == 0) {
            deleteMessageId = getStatus();
            waitingType = WaitingType.SET_STATUS;
            return true;
        } else return false;
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
        Recipient recipient1 = recipientRepository.findByIin(currentIin);
        if (recipient1!=null&&recipient1.getStatus() != null && recipient1.getStatus().size() != 0) {
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
        return sendMessage(Const.CREDIT_INFO_MESSAGE);
    }

    private int getBankName() throws TelegramApiException {
        return sendMessage(Const.BANK_NAME_MESSAGE);
    }

    private void deleteMess() {
        deleteMessage(updateMessageId);
        deleteMessage(deleteMessageId);
        deleteMessage(secondDeleteMessageId);
    }

    private void sendMessageToSpec(Registration_Service registration_service) throws TelegramApiException {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(getText(77)).append(space).append(recipientRepository.findByIin(currentIin).getFullName()).append(next);
        stringBuilder.append(getText(78)).append(currentService.getName());
        sendMessage(stringBuilder.toString(), currentSpecialist.getChatId());
    }

    private String getForSetShildren(List<String> newChildren) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < newChildren.size(); i++) {
            stringBuilder.append(newChildren.get(i));
            if (i != newChildren.size() - 1) {
                stringBuilder.append("; ");
            }
        }
        return stringBuilder.toString();
    }

    private String getChildenInfo(List<String> newChildren) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getText(117)).append(next);
        for (String child : newChildren) {
            stringBuilder.append(child).append(next);
        }
        return stringBuilder.toString();
    }

    private List<String> getChildren(String children) {
        String[] ch = children.split("; ");
        List<String> childs = new ArrayList<>();
        for (String child : ch) {
            if (isIin(child.trim())) {
                childs.add(child.trim());
            }
        }
        return childs;
    }

    private boolean isIin(String text) {
        try {
            Long.parseLong(text);
            return text.length() == 12;
        } catch (Exception e) {
            return false;
        }
    }

    private void getSpecialists() throws TelegramApiException {
        services_specs = servicesSpecsRepository.findAllByServiceIdOrderById(currentServiceId);
        specialistsOfService = getSpecs(services_specs);

        buttonsLeaf = new ButtonsLeaf(getNamesSpecs(specialistsOfService));
        deleteMessageId = sendMessageWithKeyboard(getText(42), buttonsLeaf.getListButtonWhereIdIsData(getIdsSpecs(specialistsOfService)));
        waitingType = WaitingType.CHOOSE_SPEC;
    }

    private List<String> getIdsSpecs(List<Specialist> specialistsOfService) {
        List<String> ids = new ArrayList<>();
        for (Specialist specialist : specialistsOfService) {
            ids.add(String.valueOf(specialist.getId()));
        }
        ids.add("-1");
        return ids;
    }

    private List<String> getNamesSpecs(List<Specialist> specialistsOfService) {
        List<String> names = new ArrayList<>();
        for (Specialist specialist : specialistsOfService) {
            names.add(specialist.getFullName());
        }
        names.add(buttonRepository.getButtonText(1005, getLanguage().getId()));
        return names;
    }

    private List<Specialist> getSpecs(List<Services_Spec> services_specs) {
        List<Specialist> specialistList = new ArrayList<>();
        for (Services_Spec services_spec : services_specs) {
            if (specialistRepository.findById(services_spec.getSpecId()).isActive())
                specialistList.add(specialistRepository.findById(services_spec.getSpecId()));
        }
        return specialistList;
    }

    private void getServicesOfCategory() throws TelegramApiException {
        servicesOfCategory = serviceRepository.findAllByCategoryIdAndLangIdAndActiveTrueOrderById(currentCategoryId, getLanguage().getId());

        deleteMess();
        buttonsLeaf = new ButtonsLeaf(getNamesServices(servicesOfCategory));
        deleteMessageId = sendMessageWithKeyboard(getText(23), buttonsLeaf.getListButtonWhereIdIsData(getIdsServices(servicesOfCategory)));
        waitingType = WaitingType.CHOOSE_SERVICE;

    }

    private List<String> getNamesServices(List<Service> servicesOfCategory) {
        List<String> names = new ArrayList<>();
        for (Service service : servicesOfCategory) {
            names.add(service.getName());
        }
        names.add(buttonRepository.getButtonText(1005, getLanguage().getId()));

        return names;
    }

    private List<String> getIdsServices(List<Service> servicesOfCategory) {
        List<String> ids = new ArrayList<>();
        for (Service service : servicesOfCategory) {
            ids.add(String.valueOf(service.getId2()));
        }
        ids.add("-1");
        return ids;
    }

    private void getAllCategories() throws TelegramApiException {
        deleteMess();
        allCategories = categoriesIndicatorRepository.findAllByLangIdOrderById(getLanguage().getId());
        buttonsLeaf = new ButtonsLeaf(getNamesCategories(allCategories));
        deleteMessageId = sendMessageWithKeyboard(getText(22), buttonsLeaf.getListButtonWhereIdIsData(getIdsCategories(allCategories)));
        waitingType = WaitingType.CHOOSE_CATEGORY;
    }

    private List<String> getIdsCategories(List<Category_Indicator> allCategories) {
        List<String> ids = new ArrayList<>();
        for (Category_Indicator category_indicator : allCategories) {
            ids.add(String.valueOf(category_indicator.getSecond()));
        }
//        ids.add("-1");
        return ids;
    }

    private List<String> getNamesCategories(List<Category_Indicator> allCategories) {
        List<String> names = new ArrayList<>();
        for (Category_Indicator category_indicator : allCategories) {
            names.add(category_indicator.getName());
        }
//        names.add(buttonRepository.getButtonText(1005,getLanguage().getId()));
        return names;
    }


}
