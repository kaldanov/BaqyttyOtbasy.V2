package com.telegrambot.command.impl;

import com.telegrambot.config.Bot;
import com.telegrambot.entity.User;
import com.telegrambot.command.Command;
import com.telegrambot.entity.custom.Recipient;
import com.telegrambot.entity.custom.Status;
import com.telegrambot.entity.enums.WaitingType;
import com.telegrambot.repository.StatusRepository;
import com.telegrambot.repository.TelegramBotRepositoryProvider;
import com.telegrambot.util.ButtonsLeaf;
import com.telegrambot.util.Const;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class id004_FirstRegistration extends Command {

    private Recipient recipient;
    private int deleteMessageId;
    private int secondDeleteMessageId;
    private ButtonsLeaf buttonsLeaf;
    private ArrayList<String> list = new ArrayList<>();
    private final ArrayList<String> socialBenefitsList = new ArrayList<>();
    private boolean isUpdate = false;
    private User user;
    private List<Status> statuses = new ArrayList<>();

    private StatusRepository statusRepository = TelegramBotRepositoryProvider.getStatusRepository();

    @Override
    public boolean execute() throws TelegramApiException {
        user = userRepository.findByChatId(chatId);

        switch (waitingType) {
            case START:
                if (userRepository.findByChatId(chatId) == null) {
                    sendMessageWithKeyboard("Вы не прошли регистрацию", 57);
                    return EXIT;
                }

                deleteMessage(updateMessageId);

                if (user.getIin() != null && !user.getIin().equals("")) {
                    if (isRecipientByIin(user.getIin())) {
                        recipient = recipientRepository.findByIin(user.getIin());
                        recipient.setRegistrationDate(new Date());
                        recipient.setFullName(user.getFullName());
                        recipient.setPhoneNumber(user.getPhone());
                        isUpdate = true;
                    } else {

                        recipient = new Recipient();
                        recipient.setDistrict(new Bot().getTableSchema());
                        recipient.setRegistrationDate(new Date());
                        recipient.setFullName(user.getFullName());
                        recipient.setPhoneNumber(user.getPhone());
                        recipient.setIin(user.getIin());
                    }
                    deleteMessageId = getStatus();
                    waitingType = WaitingType.SET_STATUS;
                } else {
                    deleteMessageId = getIin();
                    waitingType = WaitingType.SET_IIN;
                }
                return COMEBACK;
            case SET_IIN:
                delete();
                if (hasMessageText() && isIIN(updateMessageText)) {
                    if (isRecipientByIin(update.getMessage().getText())) {
                        recipient = recipientRepository.findByIin(update.getMessage().getText());
                        recipient.setRegistrationDate(new Date());
                        recipient.setFullName(user.getFullName());
                        recipient.setPhoneNumber(user.getPhone());
                        isUpdate = true;
                    } else {
                        recipient = new Recipient();
                        recipient.setDistrict(new Bot().getTableSchema());
                        recipient.setRegistrationDate(new Date());
                        recipient.setFullName(user.getFullName());
                        recipient.setPhoneNumber(user.getPhone());
                        recipient.setIin(update.getMessage().getText());
                    }
                    deleteMessageId = getStatus();
                    waitingType = WaitingType.SET_STATUS;
                } else {
                    wrongData();
                    getIin();
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
                delete();
                if (hasCallbackQuery()) {
                    waitingType = WaitingType.SET_VISA;
                } else {
                    if (hasMessageText()) {
                        recipient.setAddress(updateMessageText);
                        if (isUpdate) {
                            if (recipient.getDistrict() == null)
                                recipient.setDistrict(new Bot().getTableSchema());
                            recipientRepository.save(recipient);
                        }
                    } else {
                        secondDeleteMessageId = wrongData();
                        deleteMessageId = getAddress();
                        return COMEBACK;
                    }
                }
                deleteMessageId = getVisa();
                waitingType = WaitingType.SET_VISA;
                return COMEBACK;
            case SET_VISA:
                delete();
                if (hasCallbackQuery()) {
                    if (!list.get(Integer.parseInt(updateMessageText)).equals(getText(Const.SKIP_MESSAGE))) {
                        recipient.setVisa(list.get(Integer.parseInt(updateMessageText)));
                        if (isUpdate) {
                            if (recipient.getDistrict() == null)
                                recipient.setDistrict(new Bot().getTableSchema());
                            recipientRepository.save(recipient);
                        }
                    }
                    deleteMessageId = getApartment();
                    waitingType = WaitingType.SET_APARTMENT;
                } else {
                    secondDeleteMessageId = wrongData();
                    deleteMessageId = getVisa();
                }
                return COMEBACK;
            case SET_APARTMENT:
                delete();
                if (hasCallbackQuery()) {
                    if (!list.get(Integer.parseInt(updateMessageText)).equals(getText(Const.SKIP_MESSAGE))) {
                        if (list.get(Integer.parseInt(updateMessageText)).equals(getText(Const.OTHERS_MESSAGE))) {
                            deleteMessageId = getOther();
                            waitingType = WaitingType.OTHER_APARTMENT;
                            return COMEBACK;
                        } else {
                            recipient.setApartment(list.get(Integer.parseInt(updateMessageText)));
                            if (isUpdate) {
                                if (recipient.getDistrict() == null)
                                    recipient.setDistrict(new Bot().getTableSchema());
                                recipientRepository.save(recipient);
                            }
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
                delete();
                if (hasMessageText()) {
                    recipient.setApartment(updateMessageText);
                    if (isUpdate) {
                        if (recipient.getDistrict() == null)
                            recipient.setDistrict(new Bot().getTableSchema());
                        recipientRepository.save(recipient);
                    }
                    deleteMessageId = getChildren();
                    waitingType = WaitingType.ACTION_MENU;
                } else {
                    secondDeleteMessageId = wrongData();
                    deleteMessageId = getOther();
                }
                return COMEBACK;
            case ACTION_MENU:
                delete();
                if (isButton(Const.NEXT_BUTTON)) {
                    StringBuilder stringBuilder = new StringBuilder();
                    if (isUpdate) {
                        if (recipient.getChildren() != null) socialBenefitsList.add(0, recipient.getChildren());
                    }
                    for (String status : socialBenefitsList) {
                        stringBuilder.append(status).append(Const.SPLIT).append(space);
                    }
                    if (!stringBuilder.toString().equals(""))
                        recipient.setChildren(stringBuilder.toString().substring(0, stringBuilder.length() - 2));
                    if (isUpdate) {
                        if (recipient.getDistrict() == null)
                            recipient.setDistrict(new Bot().getTableSchema());
                        recipientRepository.save(recipient);
                    }
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
                delete();
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
                delete();
                if (hasCallbackQuery()) {
                    if (!list.get(Integer.parseInt(updateMessageText)).equals(getText(Const.SKIP_MESSAGE))) {
                        if (list.get(Integer.parseInt(updateMessageText)).equals(getText(Const.NEXT_MESSAGE))) {
                            StringBuilder stringBuilder = new StringBuilder();
                            if (isUpdate) {
                                if (recipient.getSocialBenefits() != null)
                                    socialBenefitsList.add(0, recipient.getSocialBenefits());
                            }
                            for (String socialBenefits : socialBenefitsList) {
                                stringBuilder.append(socialBenefits).append(Const.SPLIT).append(space);
                            }
                            if (!stringBuilder.toString().equals(""))
                                recipient.setSocialBenefits(stringBuilder.toString().substring(0, stringBuilder.length() - 2));
                            if (isUpdate) {
                                if (recipient.getDistrict() == null)
                                    recipient.setDistrict(new Bot().getTableSchema());
                                recipientRepository.save(recipient);
                            }
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
                delete();
                if (hasMessageText()) {
                    socialBenefitsList.add(updateMessageText);
                    deleteMessageId = getSocialBenefits();
                    waitingType = WaitingType.SOCIAL_BENEFITS;
                } else {
                    secondDeleteMessageId = wrongData();
                    deleteMessageId = getOther();
                }
                return COMEBACK;
//            case SET_MARITAL_STATUS:
//                delete();
//                if (hasCallbackQuery()) {
//                    if (!list.get(Integer.parseInt(updateMessageText)).equals(getText(Const.SKIP_MESSAGE))) {
//                        recipient.setMaritalStatus(list.get(Integer.parseInt(updateMessageText)));
//                        if (isUpdate) {
//                            if (recipient.getDistrict() == null)
//                                recipient.setDistrict(new Bot().getTableSchema());
//                            recipientRepository.save(recipient);
//                        }
//                        if (!list.get(Integer.parseInt(updateMessageText)).equals("Замужем/Женат") &&
//                                !list.get(Integer.parseInt(updateMessageText)).equals("Сожительство") &&
//                                !list.get(Integer.parseInt(updateMessageText)).equals("Не замужем/не женат")) {
//                            deleteMessageId = getAliments();
//                            waitingType = WaitingType.SET_ALIMENTS;
//                        } else {
//                            recipient.setAliments("Не получаю");
//                            if (isUpdate) {
//                                if (recipient.getDistrict() == null)
//                                    recipient.setDistrict(new Bot().getTableSchema());
//                                recipientRepository.save(recipient);
//                            }
//                            deleteMessageId = getEmploymentType();
//                            waitingType = WaitingType.EMPLOYMENT_TYPE;
//                        }
//                    } else {
//                        deleteMessageId = getEmploymentType();
//                        waitingType = WaitingType.EMPLOYMENT_TYPE;
//                    }
//                } else {
//                    secondDeleteMessageId = wrongData();
//                    deleteMessageId = getMaritalStatus();
//                }
//                return COMEBACK;
            case SET_ALIMENTS:
                delete();
                if (hasCallbackQuery()) {
                    if (!list.get(Integer.parseInt(updateMessageText)).equals(getText(Const.SKIP_MESSAGE))) {
                        recipient.setAliments(list.get(Integer.parseInt(updateMessageText)));
                        if (isUpdate) {
                            if (recipient.getDistrict() == null)
                                recipient.setDistrict(new Bot().getTableSchema());
                            recipientRepository.save(recipient);
                        }
                    }
                    deleteMessageId = getEmploymentType();
                    waitingType = WaitingType.EMPLOYMENT_TYPE;
                } else {
                    secondDeleteMessageId = wrongData();
                    deleteMessageId = getAliments();
                }
                return COMEBACK;
            case EMPLOYMENT_TYPE:
                delete();
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
                            if (isUpdate) {
                                if (recipient.getDistrict() == null)
                                    recipient.setDistrict(new Bot().getTableSchema());
                                recipientRepository.save(recipient);
                            }
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
                delete();
                if (hasMessageText()) {
                    recipient.setEmployment(updateMessageText);
                    if (isUpdate) {
                        if (recipient.getDistrict() == null)
                            recipient.setDistrict(new Bot().getTableSchema());
                        recipientRepository.save(recipient);
                    }
                    deleteMessageId = getEducation();
                    waitingType = WaitingType.EDUCATION;
                } else {
                    secondDeleteMessageId = wrongData();
                    deleteMessageId = getEmployment();
                }
                return COMEBACK;
            case OTHER_EMPLOYMENT_TYPE:
                delete();
                if (hasMessageText()) {
                    recipient.setEmploymentType(updateMessageText);
                    if (isUpdate) {
                        if (recipient.getDistrict() == null)
                            recipient.setDistrict(new Bot().getTableSchema());
                        recipientRepository.save(recipient);
                    }
                    deleteMessageId = getEducation();
                    waitingType = WaitingType.EDUCATION;
                } else {
                    secondDeleteMessageId = wrongData();
                    deleteMessageId = getOther();
                }
                return COMEBACK;
            case EDUCATION:
                delete();
                if (hasCallbackQuery()) {
                    if (!list.get(Integer.parseInt(updateMessageText)).equals(getText(Const.SKIP_MESSAGE))) {
                        if (list.get(Integer.parseInt(updateMessageText)).equals("Школа")) {
                            recipient.setEducation(list.get(Integer.parseInt(updateMessageText)));
                            if (isUpdate) {
                                if (recipient.getDistrict() == null)
                                    recipient.setDistrict(new Bot().getTableSchema());
                                recipientRepository.save(recipient);
                            }
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
                delete();
                if (hasMessageText()) {
                    recipient.setEducationName(updateMessageText);
                    if (isUpdate) {
                        if (recipient.getDistrict() == null)
                            recipient.setDistrict(new Bot().getTableSchema());
                        recipientRepository.save(recipient);
                    }
                    deleteMessageId = getDisabilityType();
                    waitingType = WaitingType.DISABILITY_TYPE;
                } else {
                    secondDeleteMessageId = wrongData();
                    deleteMessageId = getNameOrSpeciality();
                }
                return COMEBACK;
            case DISABILITY_TYPE:
                delete();
                if (hasCallbackQuery()) {
                    if (!list.get(Integer.parseInt(updateMessageText)).equals(getText(Const.SKIP_MESSAGE))) {
                        switch (list.get(Integer.parseInt(updateMessageText))) {
                            case "Инвалидность мамы/папы":
                                recipient.setDisabilityType(list.get(Integer.parseInt(updateMessageText)));
                                if (isUpdate) {
                                    if (recipient.getDistrict() == null)
                                        recipient.setDistrict(new Bot().getTableSchema());
                                    recipientRepository.save(recipient);
                                }
                                deleteMessageId = sendMessage("ФИО, группа");
                                waitingType = WaitingType.SET_MOTHER_FATHER_FULL_NAME;
                                break;
                            case "Инвалидность ребенка":
                                recipient.setDisabilityType(list.get(Integer.parseInt(updateMessageText)));
                                if (isUpdate) {
                                    if (recipient.getDistrict() == null)
                                        recipient.setDistrict(new Bot().getTableSchema());
                                    recipientRepository.save(recipient);
                                }
                                deleteMessageId = sendMessage("ФИО, группа, диагноз");
                                waitingType = WaitingType.SET_MOTHER_FATHER_FULL_NAME;
                                break;
                            case "Инвалидность других членов семьи (бабушка, дедушка и т.д.)":
                                recipient.setDisabilityType(list.get(Integer.parseInt(updateMessageText)));
                                if (isUpdate) {
                                    if (recipient.getDistrict() == null)
                                        recipient.setDistrict(new Bot().getTableSchema());
                                    recipientRepository.save(recipient);
                                }
                                deleteMessageId = sendMessage("ФИО, год рождения");
                                waitingType = WaitingType.SET_MOTHER_FATHER_FULL_NAME;
                                break;
                            default:
                                deleteMessageId = getCreditHistory();
                                waitingType = WaitingType.SET_CREDIT_HISTORY;
                                break;
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
                delete();
                if (hasMessageText()) {
                    recipient.setDisability(updateMessageText);
                    if (isUpdate) {
                        if (recipient.getDistrict() == null)
                            recipient.setDistrict(new Bot().getTableSchema());
                        recipientRepository.save(recipient);
                    }
                    deleteMessageId = getCreditHistory();
                    waitingType = WaitingType.SET_CREDIT_HISTORY;
                } else {
                    deleteMessageId = sendMessage("ФИО, группа");
                }
                return COMEBACK;
            case SET_CREDIT_HISTORY:
                delete();
                if (hasCallbackQuery()) {
                    switch (list.get(Integer.parseInt(updateMessageText))) {
                        case "есть кредит":
                            recipient.setCreditHistory(list.get(Integer.parseInt(updateMessageText)));
                            deleteMessageId = getCreditInfo();
                            waitingType = WaitingType.SET_CREDIT_INFO;
                            break;
                        case "есть задолжность":
                            recipient.setCreditHistory(list.get(Integer.parseInt(updateMessageText)));
                            deleteMessageId = getBankName();
                            waitingType = WaitingType.SET_CREDIT_INFO_OTHER;
                            break;
                        case "коллекторы (какой банк)":
                            recipient.setCreditHistory(list.get(Integer.parseInt(updateMessageText)));
                            deleteMessageId = getBankName();
                            waitingType = WaitingType.SET_CREDIT_INFO_OTHER;
                            break;
                        case "Другое":
                            deleteMessageId = getOther();
                            waitingType = WaitingType.SET_OTHER_CREDIT_HISTORY;
                            break;
                        default:
                            recipient.setCreditHistory(list.get(Integer.parseInt(updateMessageText)));
                            if (recipient.getDistrict() == null)
                                recipient.setDistrict(new Bot().getTableSchema());
                            recipientRepository.save(recipient);
                            sendMessage(Const.DONE_JOIN_MESSAGE);
                            return EXIT;
                    }
                } else {
                    secondDeleteMessageId = wrongData();
                    deleteMessageId = getCreditHistory();
                }
                return COMEBACK;
            case SET_OTHER_CREDIT_HISTORY:
                delete();
                if (hasMessageText()) {
                    recipient.setCreditHistory(updateMessageText);
                    if (recipient.getDistrict() == null)
                        recipient.setDistrict(new Bot().getTableSchema());
                    recipientRepository.save(recipient);
                    sendMessage(Const.DONE_JOIN_MESSAGE);
                    return EXIT;
                } else {
                    secondDeleteMessageId = wrongData();
                    deleteMessageId = getOther();
                }
                return COMEBACK;
            case SET_CREDIT_INFO:
                delete();
                if (hasMessageText()) {
                    recipient.setCreditInfo(updateMessageText);
                    if (recipient.getDistrict() == null)
                        recipient.setDistrict(new Bot().getTableSchema());
                    recipientRepository.save(recipient);
                    sendMessage(Const.DONE_JOIN_MESSAGE);
                    return EXIT;
                } else {
                    secondDeleteMessageId = wrongData();
                    deleteMessageId = getCreditInfo();
                }
                return COMEBACK;
            case SET_CREDIT_INFO_OTHER:
                delete();
                if (hasMessageText()) {
                    recipient.setCreditInfo(updateMessageText);
                    if (recipient.getDistrict() == null)
                        recipient.setDistrict(new Bot().getTableSchema());
                    recipientRepository.save(recipient);
                    sendMessage(Const.DONE_JOIN_MESSAGE);
                    return EXIT;
                } else {
                    secondDeleteMessageId = wrongData();
                    deleteMessageId = getBankName();
                }
                return COMEBACK;
        }
        return EXIT;
    }

    private boolean isIIN(String updateMessageText) {
        try {
            Long.parseLong(update.getMessage().getText());
            return updateMessageText.length() == 12;
        } catch (Exception e) {
            return false;
        }
    }

    private int wrongData() throws TelegramApiException {
        return botUtils.sendMessage(Const.WRONG_DATA_TEXT, chatId);
    }

    private int getIin() throws TelegramApiException {
        return botUtils.sendMessage(Const.SET_IIN_MESSAGE, chatId);
    }

    private void wrongIinNotNumber() throws TelegramApiException {
        botUtils.sendMessage(Const.IIN_WRONG_MESSAGE, chatId);
    }

    private int getAddress() throws TelegramApiException {
        if (isUpdate) {
            list = new ArrayList<>();
            list.add(getText(Const.SKIP_MESSAGE));
            buttonsLeaf = new ButtonsLeaf(list);
            return toDeleteKeyboard(sendMessageWithKeyboard(getUpdateText(recipient.getAddress(), getText(Const.SET_ADDRESS_MESSAGE)), buttonsLeaf.getListButton()));
        } else {
            return botUtils.sendMessage(Const.SET_ADDRESS_MESSAGE, chatId);
        }
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

    private int getVisa() throws TelegramApiException {
        list.clear();
        String text = getText(Const.REGISTRATION_TYPE_MESSAGE);
        String[] texts = text.split(Const.SPLIT);
        List<String> textList = Arrays.asList(texts);
        //System.out.println(text1);
        list.addAll(textList);
        //System.out.println(list);
//        Arrays.asList(getText(Const.REGISTRATION_TYPE_MESSAGE).split(Const.SPLIT)).forEach((e) -> {
//            list.add(e);
//        });
        if (isUpdate) list.add(getText(Const.SKIP_MESSAGE));
        buttonsLeaf = new ButtonsLeaf(list);
        if (isUpdate) {
            return toDeleteKeyboard(sendMessageWithKeyboard(getUpdateText(recipient.getVisa(), getText(Const.TYPE_OF_REGISTRATION)), buttonsLeaf.getListButton()));
        } else {
            ReplyKeyboard replyKeyboard = buttonsLeaf.getListButton();
            String text2 = getText(Const.TYPE_OF_REGISTRATION);
            return toDeleteKeyboard(sendMessageWithKeyboard(text2, replyKeyboard));
        }
    }

    private int getApartment() throws TelegramApiException {
        list.clear();
        list.addAll(Arrays.asList(getText(Const.APARTMENT_MESSAGE).split(Const.SPLIT)));
        list.add(getText(Const.OTHERS_MESSAGE));
        if (isUpdate) list.add(getText(Const.SKIP_MESSAGE));
        buttonsLeaf = new ButtonsLeaf(list);
        if (isUpdate) {
            return toDeleteKeyboard(sendMessageWithKeyboard(getUpdateText(recipient.getApartment(), getText(Const.HOUSING_AVAILABILITY_MESSAGE)), buttonsLeaf.getListButton()));
        } else {
            return toDeleteKeyboard(sendMessageWithKeyboard(getText(Const.HOUSING_AVAILABILITY_MESSAGE), buttonsLeaf.getListButton()));
        }
    }

    private int getOther() throws TelegramApiException {
        return botUtils.sendMessage(Const.SET_YOUR_OPTION_MESSAGE, chatId);
    }

    private int getChildren() throws TelegramApiException {
        return botUtils.sendMessage(Const.SET_CHILDREN_MESSAGE, chatId);
    }

    private int getChildrenIin() throws TelegramApiException {
        return botUtils.sendMessage(Const.NAME_CHILDREN_MESSAGE, chatId);
    }

    private int getSocialBenefits() throws TelegramApiException {
        list.clear();
        list.addAll(Arrays.asList(getText(Const.SOCIAL_BENEFITS_MESSAGE).split(Const.SPLIT)));
        list.add(getText(Const.OTHERS_MESSAGE));
        list.add(getText(Const.NEXT_MESSAGE));
        if (isUpdate) list.add(getText(Const.SKIP_MESSAGE));
        buttonsLeaf = new ButtonsLeaf(list);
        if (isUpdate) {
            return toDeleteKeyboard(sendMessageWithKeyboard(getUpdateText(recipient.getSocialBenefits(), getText(Const.SOCIAL_PENSION_MESSAGE)), buttonsLeaf.getListButton()));
        } else {
            return toDeleteKeyboard(sendMessageWithKeyboard(getText(Const.SOCIAL_PENSION_MESSAGE), buttonsLeaf.getListButton()));
        }
    }

//    private int getMaritalStatus() throws TelegramApiException {
//        list.clear();
//        list.addAll(Arrays.asList(getText(Const.MARRIED_TYPE_MESSAGE).split(Const.SPLIT)));
//        if (isUpdate) list.add(getText(Const.SKIP_MESSAGE));
//        buttonsLeaf = new ButtonsLeaf(list);
//        if (isUpdate) {
//            return toDeleteKeyboard(sendMessageWithKeyboard(getUpdateText(recipient.getMaritalStatus(), getText(Const.MARITAL_STATUS_MESSAGE)), buttonsLeaf.getListButton()));
//        } else {
//            return toDeleteKeyboard(sendMessageWithKeyboard(getText(Const.MARITAL_STATUS_MESSAGE), buttonsLeaf.getListButton()));
//        }
//    }

    private int getAliments() throws TelegramApiException {
        list.clear();
        list.addAll(Arrays.asList(getText(Const.ALIMENT_TYPE_MESSAGE).split(Const.SPLIT)));
        if (isUpdate) list.add(getText(Const.SKIP_MESSAGE));
        buttonsLeaf = new ButtonsLeaf(list);
        if (isUpdate) {
            return toDeleteKeyboard(sendMessageWithKeyboard(getUpdateText(recipient.getAliments(), getText(Const.ALIMENTS_MESSAGE)), buttonsLeaf.getListButton()));
        } else {
            return toDeleteKeyboard(sendMessageWithKeyboard(getText(Const.ALIMENTS_MESSAGE), buttonsLeaf.getListButton()));
        }
    }

    private int getEmploymentType() throws TelegramApiException {
        list.clear();
        list.addAll(Arrays.asList(getText(Const.EMPLOYEE_TYPE_MESSAGE).split(Const.SPLIT)));
        list.add(getText(Const.OTHERS_MESSAGE));
        if (isUpdate) list.add(getText(Const.SKIP_MESSAGE));
        buttonsLeaf = new ButtonsLeaf(list);
        if (isUpdate) {
            return toDeleteKeyboard(sendMessageWithKeyboard(getUpdateText(recipient.getEmploymentType(), getText(Const.EMPLOYMENT_MESSAGE)), buttonsLeaf.getListButton()));
        } else {
            return toDeleteKeyboard(sendMessageWithKeyboard(getText(Const.EMPLOYMENT_MESSAGE), buttonsLeaf.getListButton()));
        }
    }

    private int getEmployment() throws TelegramApiException {
        return botUtils.sendMessage(Const.WORK_PLACE_MESSAGE, chatId);
    }

    private int getEducation() throws TelegramApiException {
        list.clear();
        list.addAll(Arrays.asList(getText(Const.EDUCATION_TYPE_CHOOSE_MESSAGE).split(Const.SPLIT)));
        if (isUpdate) list.add(getText(Const.SKIP_MESSAGE));
        buttonsLeaf = new ButtonsLeaf(list);
        if (isUpdate) {
            return toDeleteKeyboard(sendMessageWithKeyboard(getUpdateText(recipient.getEducation(), getText(Const.EDUCATION_MESSAGE)), buttonsLeaf.getListButton()));
        } else {
            return toDeleteKeyboard(sendMessageWithKeyboard(getText(Const.EDUCATION_MESSAGE), buttonsLeaf.getListButton()));
        }
    }

    private int getDisabilityType() throws TelegramApiException {
        list.clear();
        list.addAll(Arrays.asList(getText(Const.DISABILITY_CHOOSE_TYPE_MESSAGE).split(Const.SPLIT)));
        list.add(getText(Const.SKIP_MESSAGE));
        buttonsLeaf = new ButtonsLeaf(list);
        if (isUpdate) {
            return toDeleteKeyboard(sendMessageWithKeyboard(getUpdateText(recipient.getDisabilityType(), getText(Const.DISABILITY_TYPE_MESSAGE)), buttonsLeaf.getListButton()));
        } else {
            return toDeleteKeyboard(sendMessageWithKeyboard(getText(Const.DISABILITY_TYPE_MESSAGE), buttonsLeaf.getListButton()));
        }
    }

    private int getNameOrSpeciality() throws TelegramApiException {
        return botUtils.sendMessage(Const.NAME_OR_SPECIALITY_MESSAGE, chatId);
    }

    private int getCreditHistory() throws TelegramApiException {
        list.clear();
        list.addAll(Arrays.asList(getText(Const.CREDIT_TYPE_MESSAGE).split(Const.SPLIT)));
        list.add(getText(Const.OTHERS_MESSAGE));
        buttonsLeaf = new ButtonsLeaf(list);
        return toDeleteKeyboard(sendMessageWithKeyboard(getText(Const.CREDIT_HISTORY_MESSAGE), buttonsLeaf.getListButton()));
    }

    private int getCreditInfo() throws TelegramApiException {
        return botUtils.sendMessage(Const.CREDIT_INFO_MESSAGE, chatId);
    }

    private int getBankName() throws TelegramApiException {
        return botUtils.sendMessage(Const.BANK_NAME_MESSAGE, chatId);
    }

    private String getUpdateText(String currentInfo, String sendMessage) {
        String format = getText(Const.UPDATE_REGISTRATION_MESSAGE);
        return String.format(format, currentInfo != null ? currentInfo : " ", sendMessage);
    }

    private void delete() {
        deleteMessage(updateMessageId);
        deleteMessage(deleteMessageId);
        deleteMessage(secondDeleteMessageId);
    }
}
