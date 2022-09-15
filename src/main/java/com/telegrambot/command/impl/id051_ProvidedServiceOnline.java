package com.telegrambot.command.impl;

import com.telegrambot.command.Command;
import com.telegrambot.entity.custom.*;
import com.telegrambot.entity.enums.WaitingType;
import com.telegrambot.repository.StatusRepository;
import com.telegrambot.repository.TelegramBotRepositoryProvider;
import com.telegrambot.util.ButtonsLeaf;
import com.telegrambot.util.Const;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class id051_ProvidedServiceOnline extends Command {
    private List<String> list;
    private ButtonsLeaf buttonsLeaf;
    private WaitingType waitingType = WaitingType.START;
    private boolean COMEBACK = false;
    private boolean EXIT = true;

    private int deleteMessageId;
    private int secondDeleteMessageId;
    // for request service
    private List<Category_Indicator> allCategories;
    private Category_Indicator currentCategory;
    private long currentCategoryId;

    private List<Service> servicesOfCategory;
    private Service currentService;
    private long currentServiceId;

    List<Service> myServices;
    private List<Direction> directionsForSelect;
    private ButtonsLeaf buttonsLeafDirections;
    private long currentDirId;
    private Registration_Service newRegistrationService;
    private long newRegId;


    private String currentIin;

    private List<Status> statuses = new ArrayList<>();

    private StatusRepository statusRepository = TelegramBotRepositoryProvider.getStatusRepository();


    @Override
    public boolean execute() throws TelegramApiException {
        if (specialistRepository.existsByChatId(chatId)) {

            switch (waitingType) {
                case START:
                    if (userRepository.findByChatId(chatId) == null) {
                        sendMessageWithKeyboard("Вы не прошли регистрацию", 57);
                        return EXIT;
                    }

                    deleteMess();

                    deleteMessageId = sendMessage(Const.SET_IIN_MESSAGE);
                    waitingType = WaitingType.SET_IIN_FOR_REQUEST;
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
                            sendMessageWithKeyboard(getText(68), 28);
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
                                return COMEBACK;
                            } else {
                                List<Specialist> specialists = specialistRepository.findAllByChatIdOrderById(chatId);

                                List<Services_Spec> services_specs = getServices_spec(specialists);

                                myServices = getServicesOfSpec(services_specs);

                                buttonsLeaf = new ButtonsLeaf(getNamesServices(myServices));

                                deleteMessageId = sendMessageWithKeyboard(getText(46), buttonsLeaf.getListButtonWhereIdIsData(getIdsServices(myServices)));
                                waitingType = WaitingType.CHOOSE_SERVICE;
                            }
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

                case SET_STATUS:
                    deleteMess();
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
                        }
                    } else {
                        deleteMessageId = sendMessage(Const.WRONG_DATA_TEXT, chatId);
                        deleteMessageId = getStatus();
                    }
                    return COMEBACK;
                case SET_IN_STATUS:
                    deleteMess();
                    if (hasCallbackQuery()) {
                        if (!update.getCallbackQuery().getData().equals(getText(Const.SKIP_MESSAGE))) {
                            statuses.add(statusRepository.getOne(Integer.parseInt(update.getCallbackQuery().getData())));
                            Recipient recipient = recipientRepository.findByIin(currentIin);
                            recipient.setStatus(statuses);
                            recipientRepository.save(recipient);

                            List<Specialist> specialists = specialistRepository.findAllByChatIdOrderById(chatId);

                            List<Services_Spec> services_specs = getServices_spec(specialists);

                            myServices = getServicesOfSpec(services_specs);

                            buttonsLeaf = new ButtonsLeaf(getNamesServices(myServices));

                            deleteMessageId = sendMessageWithKeyboard(getText(46), buttonsLeaf.getListButtonWhereIdIsData(getIdsServices(myServices)));
                            waitingType = WaitingType.CHOOSE_SERVICE;
                        }

                    } else {
                        deleteMessageId = sendMessage(Const.WRONG_DATA_TEXT, chatId);
                        deleteMessageId = getInStatus(3);
                    }
                    return COMEBACK;
                case CHOOSE_SERVICE:
                    if (hasCallbackQuery()) {
                        deleteMess();
                        currentServiceId = Long.parseLong(updateMessageText);
                        currentService  = serviceRepository.findById2AndLangId(currentServiceId, getLanguage().getId());
                        if (currentService.getStatusId() == 2){
                            Calendar start = Calendar.getInstance();
                            start.set(Calendar.DAY_OF_MONTH, 1);
                            start.set(Calendar.MONTH, 1);
                            start.set(Calendar.HOUR_OF_DAY, 0);
                            start.set(Calendar.MINUTE, 0);
                            start.set(Calendar.SECOND, 1);
                            Calendar end = Calendar.getInstance();
                            end.set(Calendar.MONTH, 12);
                            end.set(Calendar.DAY_OF_MONTH, 31);
                            end.set(Calendar.HOUR_OF_DAY, 23);
                            end.set(Calendar.MINUTE, 59);
                            end.set(Calendar.SECOND, 59);
                            if (registrationServiceRepository.findAllByIinAndDateRegBetween(currentIin,start.getTime(), end.getTime()).size() > 0){
                                sendMessage("Вы уже полуили эту услугу за этот год!");
                                deleteMessageId = sendMessageWithKeyboard(getText(46), buttonsLeaf.getListButtonWhereIdIsData(getIdsServices(myServices)));
                                return COMEBACK;
                            }
                        }

                        directionsForSelect = directionRepository.findAllByServiceIdOrderById(currentServiceId);
                        buttonsLeafDirections = new ButtonsLeaf(getNamesDirections(directionsForSelect));
                        if (directionsForSelect.size() == 0) {
//                            deleteMess();
                            sendMessageWithKeyboard(getText(92), 28);
                            return EXIT;
                        }

                        deleteMessageId = sendMessageWithKeyboard(getText(75), buttonsLeafDirections.getListButtonWhereIdIsData(idsDirections(directionsForSelect)));
                        newRegistrationService = new Registration_Service();
                        newRegistrationService.setDateReg(new Date());
                        newRegistrationService.setIin(currentIin);
                        newRegistrationService.setServiceId(currentServiceId);
                        newRegistrationService.setSpecId(getSpecId(currentServiceId));
                        newRegistrationService.setUserChatId(0);

                        newRegId = registrationServiceRepository.saveAndFlush(newRegistrationService).getId();
                        waitingType = WaitingType.CHOOSE_DIRECTION;

                    }

                    return COMEBACK;

                case CHOOSE_DIRECTION:
                    deleteMess();
                    if (hasCallbackQuery()) {

                        currentDirId = Long.parseLong(updateMessageText);
                        if (currentDirId == -1) {
                            deleteMessageId = sendMessageWithKeyboard(getText(46), buttonsLeaf.getListButtonWhereIdIsData(getIdsServices(myServices)));
                            waitingType = WaitingType.CHOOSE_SERVICE;
                        } else if (directionRepository.findById(currentDirId) != null) {

                            DirectionRegistration directionRegistration = new DirectionRegistration(newRegId, currentDirId);
                            directionRegistrationRepository.save(directionRegistration);

                            directionsForSelect.remove(directionRepository.findById(currentDirId));

                            buttonsLeafDirections = new ButtonsLeaf(getNamesDirectionsWithNext(directionsForSelect));

                            deleteMessageId = sendMessageWithKeyboard(getText(75), buttonsLeafDirections.getListButtonWhereIdIsData(idsDirectionsWithNext(directionsForSelect)));

                            waitingType = WaitingType.CHOOSE_DIRECTION_OR_NEXT;
                        } else {
                            deleteMessageId = sendMessageWithKeyboard(getText(75), buttonsLeafDirections.getListButtonWhereIdIsData(idsDirections(directionsForSelect)));
                            waitingType = WaitingType.CHOOSE_DIRECTION;
                        }
                    } else {
                        sendMessageWithKeyboard(getText(1002) + "\n" + getText(82), 28);
                        waitingType = WaitingType.START;
                    }
                    return COMEBACK;

                case CHOOSE_DIRECTION_OR_NEXT:
                    deleteMess();
                    if (hasCallbackQuery()) {

                        currentDirId = Long.parseLong(updateMessageText);

                        if (currentDirId == -1) {// dalee
                            newRegistrationService.setFinish(true);
                            registrationServiceRepository.save(newRegistrationService);
                            deleteMessageId = sendMessageWithKeyboard(getText(Const.DONE_JOIN_MESSAGE), 28);
                        } else if (directionRepository.findById(currentDirId) != null) {

                            DirectionRegistration directionRegistration = new DirectionRegistration(newRegId, currentDirId);
                            directionRegistrationRepository.save(directionRegistration);

                            directionsForSelect.remove(directionRepository.findById(currentDirId));

                            if (directionsForSelect.size() == 0) {
                                newRegistrationService.setFinish(true);
                                registrationServiceRepository.save(newRegistrationService);
                                deleteMessageId = sendMessageWithKeyboard(getText(Const.DONE_JOIN_MESSAGE), 28);
                                return EXIT;
                            }
                            buttonsLeafDirections = new ButtonsLeaf(getNamesDirectionsWithNext(directionsForSelect));

                            deleteMessageId = sendMessageWithKeyboard(getText(75), buttonsLeafDirections.getListButtonWhereIdIsData(idsDirectionsWithNext(directionsForSelect)));

                            waitingType = WaitingType.CHOOSE_DIRECTION_OR_NEXT;
                        } else {
                            deleteMessageId = sendMessageWithKeyboard(getText(75), buttonsLeafDirections.getListButtonWhereIdIsData(idsDirectionsWithNext(directionsForSelect)));
                            waitingType = WaitingType.CHOOSE_DIRECTION_OR_NEXT;
                        }
                    } else {
                        sendMessageWithKeyboard(getText(1002) + "\n" + getText(82), 28);
                        waitingType = WaitingType.START;
                    }
                    return COMEBACK;
            }

            return true;
        } else {
            sendMessage(Const.NO_ACCESS);
        }
        return EXIT;
    }

    private int getStatus() throws TelegramApiException {
        List<String> list = new ArrayList<>();
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
        buttonsLeaf = new ButtonsLeaf(list, listIds);
        return toDeleteKeyboard(sendMessageWithKeyboard(getText(1088), buttonsLeaf.getListButtonWhereIdIsData()));

    }

    private int getInStatus(int statusId) throws TelegramApiException {
        List<String> list = new ArrayList<>();
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

    private boolean chekStatus() throws TelegramApiException {
        Recipient recipient = recipientRepository.findByIin(currentIin);
        if (recipient.getStatus().size() == 0) {
            deleteMessageId = getStatus();
            waitingType = WaitingType.SET_STATUS;
            return true;
        } else return false;
    }

    private long getSpecId(long currentServiceId) {
        List<Specialist> specialists = specialistRepository.findAllByChatIdOrderById(chatId);

        List<Services_Spec> services_specs = getServices_spec(specialists);
        for (Services_Spec services_spec : services_specs) {
            if (services_spec.getServiceId() == currentServiceId) {
                return services_spec.getSpecId();
            }
        }
        return 0;
    }

    private List<String> idsDirectionsWithNext(List<Direction> directionsForSelect) {
        List<String> ids = new ArrayList<>();

        for (Direction direction : directionsForSelect) {
            ids.add(String.valueOf(direction.getId()));
        }
        ids.add("-1");
        return ids;
    }

    private List<String> getNamesDirectionsWithNext(List<Direction> directionsForSelect) {
        List<String> names = new ArrayList<>();
        for (Direction direction : directionsForSelect) {
            names.add(getLanguage().getId() == 1 ? direction.getNameRus() : direction.getNameKaz());
        }
        names.add(getText(1078));
        return names;
    }

    private List<Service> getServicesOfSpec(List<Services_Spec> services_specs) {
        List<Service> services = new ArrayList<>();

        for (Services_Spec spec : services_specs) {
            if (serviceRepository.findById2AndLangId(spec.getServiceId(), getLanguage().getId()).getStatusId() == 1) {
                services.add(serviceRepository.findById2AndLangId(spec.getServiceId(), getLanguage().getId()));
            }
        }
        return services;
    }

    private List<String> idsDirections(List<Direction> allByServiceId) {
        List<String> ids = new ArrayList<>();

        for (Direction direction : allByServiceId) {
            ids.add(String.valueOf(direction.getId()));
        }
        ids.add("-1");
        return ids;
    }

    private List<String> getNamesDirections(List<Direction> allByServiceId) {
        List<String> names = new ArrayList<>();
        for (Direction direction : allByServiceId) {
            names.add(getLanguage().getId() == 1 ? direction.getNameRus() : direction.getNameKaz());
        }
        names.add(buttonRepository.findByIdAndLangId(1005, getLanguage().getId()).getName());
        return names;
    }

    private void sendRecipientInfo() throws TelegramApiException {
        Recipient recipientInfo = recipientRepository.findByIin(update.getMessage().getText());

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getText(89)).append(recipientInfo.getFullName()).append(next);
        stringBuilder.append(getText(90)).append(recipientInfo.getPhoneNumber()).append(next);
        stringBuilder.append(getText(91)).append(recipientInfo.getAddress()).append(next);

        deleteMessageId = sendMessageWithKeyboard(stringBuilder.toString(), 51);
        waitingType = WaitingType.THIS_PERSON_OR_NO;
    }

    private List<Services_Spec> getServices_spec(List<Specialist> specialists) {
        List<Services_Spec> services_specs = new ArrayList<>();
        for (Specialist specialist : specialists) {
            services_specs.addAll(servicesSpecsRepository.findAllBySpecIdOrderById(specialist.getId()));
        }
        return services_specs;
    }

    private void deleteUpdateMess() {
        deleteMessage(updateMessageId);
    }

    private int wrongData() throws TelegramApiException {
        return sendMessageWithKeyboard(getText(Const.WRONG_DATA_TEXT), 28);
    }


    private int getIin() throws TelegramApiException {
        return sendMessage(getText(Const.SET_IIN_MESSAGE));
    }

    private int wrongIinNotNumber() throws TelegramApiException {
        return sendMessage(getText(Const.IIN_WRONG_MESSAGE));
    }


//    public String getText(int messageIdFromDb) {
//        return messageRepository.getMessageText(messageIdFromDb, 1);
//    }


    private void deleteMess() {
        deleteMessage(updateMessageId);
        deleteMessage(deleteMessageId);
        deleteMessage(secondDeleteMessageId);
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
