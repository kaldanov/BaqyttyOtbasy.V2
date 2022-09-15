package com.telegrambot.command.impl;

import com.telegrambot.command.Command;
import com.telegrambot.config.Bot;
import com.telegrambot.entity.User;
import com.telegrambot.entity.enums.WaitingType;
import com.telegrambot.repository.StatusRepository;
import com.telegrambot.repository.TelegramBotRepositoryProvider;
import com.telegrambot.util.ButtonsLeaf;
import com.telegrambot.util.Const;
import com.telegrambot.entity.custom.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class id045_RequestsForServices extends Command {


    public int deleteMessId;
    public int secondDeleteMessId;

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


    private ButtonsLeaf buttonsLeaf;
    private boolean once = false;

    private User user;
    List<String> children;
    List<String> newChildren;
    private List<Status> statuses = new ArrayList<>();

    private StatusRepository statusRepository = TelegramBotRepositoryProvider.getStatusRepository();


    @Override
    public boolean execute() throws TelegramApiException {

        try {

            switch (waitingType) {
                case START:
                    if (userRepository.findByChatId(chatId) == null) {
                        sendMessageWithKeyboard("Вы не прошли регистрацию", 57);
                        return EXIT;
                    }

                    user = userRepository.findByChatId(chatId);
                    deleteMess();
                    if (isButton(96)) { // 96- cancel
                        sendMessage(35);
                        return EXIT;
                    } else if (isButton(107)) {
                        once = true;
                    }
                    if (isButton(1036)) { // re registr
                        if (user != null && user.getIin() != null) {
                            Recipient recipient = recipientRepository.findByIin(user.getIin());
                            if (recipient != null) {
                                recipient.setDistrict(new Bot().getTableSchema());
                                recipientRepository.save(recipient);
                            }
                        }
//                    if (user!= null){
//                        user.setEmail(new Bot().getTableSchema());
//                        userRepository.save(user);
//                        sendMessage(85);
//                    }

                    }

                    if (user != null) {
                        if (user.getIin() != null) {
                            Recipient recipient = recipientRepository.findByIin(user.getIin());
                            if (recipient != null && recipient.getDistrict() == null) {
                                recipient.setDistrict(new Bot().getTableSchema());
                                recipientRepository.save(recipient);
                            }
                            if (recipient == null) {
                                sendMessageWithKeyboard(getText(68), 1);
                                return EXIT;
                            }
                            if (!new Bot().getTableSchema().equals("PUBLIC")) {
                                if (recipient != null && recipient.getDistrict() != null && !recipient.getDistrict().equals(new Bot().getTableSchema()) && !once) {
                                    sendMessageWithKeyboard(getText(Const.YOU_ALREADY_REGISTERED_MESSAGE), 53);
                                    return COMEBACK;
                                }
                            }
                        }
                        if (user.getIin() != null && !user.getIin().equals("") && isIin(user.getIin())) {
                            if (chekStatus()) {
                                return COMEBACK;
                            } else
                                getAllCategories();
                        } else {
                            deleteMessId = sendMessage(Const.SET_IIN_MESSAGE, chatId);
                            waitingType = WaitingType.SET_IIN;
                        }
                    }


                    return COMEBACK;

                case SET_IIN:
                    deleteMess();
                    if (update.getMessage().hasText() && isIin(update.getMessage().getText())) {
                        List<User> userList = userRepository.findAllByIin(update.getMessage().getText());
                        if (userList.size() != 0) {
                            deleteMessId = sendMessage(getText(93), chatId);
                            secondDeleteMessId = sendMessage(Const.SET_IIN_MESSAGE, chatId);
                            waitingType = WaitingType.SET_IIN;
                        } else {
                            user.setIin(update.getMessage().getText());
                            deleteMessId = sendMessageWithKeyboard(String.format(getText(94), update.getMessage().getText()), 55);
                            waitingType = WaitingType.CONFIRM;


                        }
                    } else {
                        deleteMessId = sendMessage(Const.WRONG_DATA_TEXT, chatId);
                        secondDeleteMessId = sendMessage(Const.SET_IIN_MESSAGE, chatId);
                        waitingType = WaitingType.SET_IIN;
                    }
                    return COMEBACK;

                case CONFIRM:
                    deleteMess();
                    if (hasCallbackQuery()) {
                        if (isButton(89)) {//confirm
                            userRepository.save(user);
                            if (chekStatus()) {
                                return COMEBACK;
                            } else
                                getAllCategories();
                        } else if (isButton(1005)) { //back
                            deleteMessId = sendMessage(Const.SET_IIN_MESSAGE, chatId);
                            waitingType = WaitingType.SET_IIN;
                        } else {
                            deleteMessId = sendMessage(Const.WRONG_DATA_TEXT, chatId);
                            deleteMessId = sendMessageWithKeyboard(getText(94), 55);
                        }

                    } else {
                        deleteMessId = sendMessage(Const.WRONG_DATA_TEXT, chatId);
                        deleteMessId = sendMessageWithKeyboard(getText(94), 55);
                    }
                    return COMEBACK;
                case SET_STATUS:
                    deleteMess();
                    if (hasCallbackQuery()) {
                        if (!update.getCallbackQuery().getData().equals(getText(Const.SKIP_MESSAGE))) {
                            Status status = statusRepository.findById(Integer.parseInt(update.getCallbackQuery().getData()));
                            statuses.add(status);
                            if (status.getStatusId() == 2) {
                                deleteMessId = getInStatus(1);
                                waitingType = WaitingType.SET_STATUS;
                            } else {
                                deleteMessId = getInStatus(3);
                                waitingType = WaitingType.SET_IN_STATUS;
                            }
                        }
                    } else {
                        deleteMessId = sendMessage(Const.WRONG_DATA_TEXT, chatId);
                        deleteMessId = getStatus();
                    }
                    return COMEBACK;
                case SET_IN_STATUS:
                    deleteMess();
                    if (hasCallbackQuery()) {
                        if (!update.getCallbackQuery().getData().equals(getText(Const.SKIP_MESSAGE))) {
                            statuses.add(statusRepository.getOne(Integer.parseInt(update.getCallbackQuery().getData())));
                            Recipient recipient = recipientRepository.findByIin(user.getIin());
                            recipient.setStatus(statuses);
                            recipientRepository.save(recipient);
                            getAllCategories();
                        }

                    } else {
                        deleteMessId = sendMessage(Const.WRONG_DATA_TEXT, chatId);
                        deleteMessId = getInStatus(3);
                    }
                    return COMEBACK;
                case CHOOSE_CATEGORY:
                    if (hasCallbackQuery()) {
                        currentCategoryId = Long.parseLong(updateMessageText);
                        currentCategory = categoriesIndicatorRepository.findBySecondAndLangId(currentCategoryId, getLanguage().getId());
                        getServicesOfCategory();
                    } else {
                        sendMessageWithKeyboard(getText(1002), 1);
                        if (chekStatus()) {
                            return COMEBACK;
                        } else
                            getAllCategories();
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
                                if (registrationServiceRepository.findAllByIinAndDateRegBetween(user.getIin(),start.getTime(), end.getTime()).size() > 0){
                                    sendMessage("Вы уже полуили эту услугу за этот год!");
                                    getServicesOfCategory();
                                    return COMEBACK;
                                }
                            }

                            if (currentService.getStatusId() == 3) {
                                getServiceInfoToEnrol(currentService);
                            } else {
                                getSpecialists();
                            }
                        }
                    } else {
                        sendMessageWithKeyboard(getText(1002), 1);
                        getServicesOfCategory();
                    }

                    return COMEBACK;

                case CHOOSE_OPTION_FOR_SERVICE:
                    deleteMess();
                    if (hasCallbackQuery()) {
                        if (isButton(1014)) {
                            //todo asd
                            if (recipientRepository.findByIin(userRepository.findByChatId(chatId).getIin()) == null) {
                                sendMessageWithKeyboard(getText(68), 1);
                                waitingType = WaitingType.START;
                            } else {
                                List<Specialist> specialists = getSpecs(servicesSpecsRepository.findAllByServiceIdOrderById(currentServiceId));

                                for (Specialist specialist : specialists) {
                                    Registration_Service registration_service = new Registration_Service(chatId, new Date(), currentServiceId, specialist.getId(), recipientRepository.findByIin(userRepository.findByChatId(chatId).getIin()).getIin(), false);
                                    registrationServiceRepository.save(registration_service);
                                    sendMessageToSpec(registration_service, specialist.getChatId());
                                }
                                sendMessageWithKeyboard(getText(76), 1);
                                waitingType = WaitingType.START;
                            }
                        } else if (isButton(1005)) {
                            getServicesOfCategory();
                        }
                    } else {
                        sendMessageWithKeyboard(getText(1002), 1);
                        getServiceInfoToEnrol(currentService);
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
                            deleteMessId = sendMessageWithPhotoAndKeyboard(currentSpecInfo.toString(), 5, currentSpecialist.getPhoto());

                            waitingType = WaitingType.REGISTER_TO_SPEC;

                        }
                    } else {
                        sendMessageWithKeyboard(getText(1002), 1);
                        services_specs = servicesSpecsRepository.findAllByServiceIdOrderById(currentServiceId);
                        specialistsOfService = getSpecs(services_specs);

                        buttonsLeaf = new ButtonsLeaf(getNamesSpecs(specialistsOfService));
                        deleteMessId = sendMessageWithKeyboard(getText(42), buttonsLeaf.getListButtonWhereIdIsData(getIdsSpecs(specialistsOfService)));
                        waitingType = WaitingType.CHOOSE_SPEC;
                    }

                    return COMEBACK;

                case REGISTER_TO_SPEC:
//                   deleteUpdateMess();
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
                                deleteMessId = sendMessageWithKeyboard(getText(114), 58);
                                waitingType = WaitingType.FOR_WHO;

                            }
//                           return EXIT;
                        }

                    } else {
                        sendMessageWithKeyboard(getText(1002), 1);
                        currentSpecialist = specialistRepository.findById(currentSpecialistId);

                        currentSpecInfo = new StringBuilder();

                        currentSpecInfo.append(getText(66)).append(currentSpecialist.getFullName()).append(next);
                        currentSpecInfo.append(getText(67)).append(getLanguage().getId() == 1 ? currentSpecialist.getDescriptionRus() : currentSpecialist.getDescriptionKaz()).append(next);
                        deleteMessId = sendMessageWithPhotoAndKeyboard(currentSpecInfo.toString(), 5, currentSpecialist.getPhoto());

                        waitingType = WaitingType.REGISTER_TO_SPEC;
                    }


                    return COMEBACK;
                case FOR_WHO:
                    if (hasCallbackQuery()) {
                        deleteUpdateMess();
                        if (isButton(115)) {  // for me
                            Registration_Service registration_service = new Registration_Service(chatId, new Date(), currentServiceId, currentSpecialistId, recipientRepository.findByIin(userRepository.findByChatId(chatId).getIin()).getIin(), false);
                            registrationServiceRepository.save(registration_service);
                            sendMessageWithKeyboard(getText(76), 1);
                            sendMessageToSpec(registration_service);
                            return EXIT;
                        } else if (isButton(116)) {  // for child
                            Recipient recipient = recipientRepository.findByIin(userRepository.findByChatId(chatId).getIin());

                            children = getChildren(recipient.getChildren());
                            if (children.size() == 0) {
                                deleteMessId = sendMessageWithKeyboard(getText(116), 59);
                                waitingType = WaitingType.SET_CHILDREN;
                                newChildren = new ArrayList<>();
                                // todo set childs
                            } else {
                                children.add(buttonRepository.getButtonText(1005, getLanguage().getId()));
                                ButtonsLeaf childrenLeaf = new ButtonsLeaf(children, children, 100);
                                sendMessageWithKeyboard(getText(115), childrenLeaf.getListButtonWhereIdIsData());
                                waitingType = WaitingType.CHOOSE_CHILD;
                            }
                        } else if (isButton(1005)) {
                            deleteMessId = sendMessageWithPhotoAndKeyboard(currentSpecInfo.toString(), 5, currentSpecialist.getPhoto());
                            waitingType = WaitingType.REGISTER_TO_SPEC;
                        }
                    }
                    return COMEBACK;
                case SET_CHILDREN:
                    deleteUpdateMess();
                    if (hasMessageText() && isIin(updateMessageText)) {
                        newChildren.add(updateMessageText);
                        editMessageWithKeyboard(getChildenInfo(newChildren), deleteMessId, (InlineKeyboardMarkup) keyboardMarkUpService.select(59, chatId).get());
                    } else if (hasCallbackQuery() && isButton(1003)) {
                        deleteMess();
                        Recipient recipient7 = recipientRepository.findByIin(user.getIin());
                        if (recipient7 != null) {
                            recipient7.setChildren(getForSetShildren(newChildren));
                            recipientRepository.save(recipient7);
                        }
                        children = newChildren;
                        ButtonsLeaf childrenLeaf = new ButtonsLeaf(newChildren, newChildren, 100);
                        sendMessageWithKeyboard(getText(115), childrenLeaf.getListButtonWhereIdIsData());
                        waitingType = WaitingType.CHOOSE_CHILD;
                    }
                    return COMEBACK;
                case CHOOSE_CHILD:
                    deleteMess();
                    if (hasCallbackQuery() && isButton(1005)) {
                        deleteUpdateMess();
                        deleteMessId = sendMessageWithKeyboard(getText(114), 58);
                        waitingType = WaitingType.FOR_WHO;
                        return COMEBACK;
                    }
                    if (hasCallbackQuery() && children.contains(updateMessageText)) {
                        deleteUpdateMess();

                        Registration_Service registration_service = new Registration_Service(chatId, new Date(), currentServiceId, currentSpecialistId, updateMessageText, false);
                        registration_service.setParentIIN(userRepository.findByChatId(chatId).getIin());

                        registrationServiceRepository.save(registration_service);
                        sendMessageWithKeyboard(getText(76), 1);
                        sendMessageToSpec(registration_service);
                        return EXIT;
                    }
                    return COMEBACK;

                default:
                    return EXIT;
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendMessageWithKeyboard("Добро пожаловать!", 1);
            return EXIT;
        }
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

    private void deleteUpdateMess() {
        deleteMessage(updateMessageId);
    }

    private void getServiceInfoToEnrol(Service currentService) throws TelegramApiException {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(getText(83)).append(currentService.getName()).append(next);
        stringBuilder.append(getText(84)).append(currentService.getDescription()).append(next);
        deleteMessId = sendMessageWithPhotoAndKeyboard(stringBuilder.toString(), 19, currentService.getPhoto()); //1014 -зап // 1005-назад
        waitingType = WaitingType.CHOOSE_OPTION_FOR_SERVICE;
    }

    private void sendMessageToSpec(Registration_Service registration_service) throws TelegramApiException {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(getText(77)).append(space).append(userRepository.findByChatId(chatId).getFullName()).append(next);
        if (registration_service.getParentIIN() != null)

            stringBuilder.append(getText(113)).append(registration_service.getIin()).append(next);

        else stringBuilder.append(getText(97)).append(registration_service.getIin()).append(next);

        stringBuilder.append(getText(78)).append(currentService.getName());
        sendMessage(stringBuilder.toString(), currentSpecialist.getChatId());
    }

    private void sendMessageToSpec(Registration_Service registration_service, long specChatId) throws TelegramApiException {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(getText(77)).append(space).append(userRepository.findByChatId(chatId).getFullName()).append(next);
        stringBuilder.append(getText(113)).append(registration_service.getIin()).append(next);
        stringBuilder.append(getText(78)).append(currentService.getName());
        sendMessage(stringBuilder.toString(), specChatId);
    }

    private void getSpecialists() throws TelegramApiException {
        services_specs = servicesSpecsRepository.findAllByServiceIdOrderById(currentServiceId);
        specialistsOfService = getSpecs(services_specs);

        buttonsLeaf = new ButtonsLeaf(getNamesSpecs(specialistsOfService));
        deleteMessId = sendMessageWithKeyboard(getText(42), buttonsLeaf.getListButtonWhereIdIsData(getIdsSpecs(specialistsOfService)));
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
        deleteMessId = sendMessageWithKeyboard(getText(23), buttonsLeaf.getListButtonWhereIdIsData(getIdsServices(servicesOfCategory)));
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
        Recipient recipient = recipientRepository.findByIin(user.getIin());
        if (recipient.getStatus().size() == 0) {
            deleteMessId = getStatus();
            waitingType = WaitingType.SET_STATUS;
            return true;
        } else return false;
    }

    private void getAllCategories() throws TelegramApiException {
        deleteMess();
        allCategories = categoriesIndicatorRepository.findAllByLangIdOrderById(getLanguage().getId());
        buttonsLeaf = new ButtonsLeaf(getNamesCategories(allCategories));
        deleteMessId = sendMessageWithKeyboard(getText(22), buttonsLeaf.getListButtonWhereIdIsData(getIdsCategories(allCategories)));
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

    private void deleteMess() {

        deleteMessage(updateMessageId);
        deleteMessage(deleteMessId);
        deleteMessage(secondDeleteMessId);
    }


    public String getText(int messageIdFromDb) {
        return messageRepository.getMessageText(messageIdFromDb, getLanguage().getId());
    }


}
