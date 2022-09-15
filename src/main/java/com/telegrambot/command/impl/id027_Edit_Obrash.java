package com.telegrambot.command.impl;

import com.telegrambot.command.Command;
import com.telegrambot.entity.custom.*;
import com.telegrambot.entity.enums.WaitingType;
import com.telegrambot.util.ButtonsLeaf;
import com.telegrambot.util.Const;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Component
public class id027_Edit_Obrash extends Command {

    private List<String> list;

    private List<Specialist> specialists;
    private ButtonsLeaf buttonsLeaf;

    private List<Service> services;
    public List<Services_Spec> services_specs;

    public Service currentServiceKaz;
    public Service currentServiceRus;

    private Service newServiceKaz;
    private Service newServiceRus;

    public int deleteMessageId;
    public int secondDeleteMessageId;
    private int thirdDeleteMessageId;

    private long currentCategoryId;
    private long currentServiceId;
    private String newNameServiceKaz;
    private String newNameServiceRus;

    private Category_Indicator category_for_edit_kaz;
    private Category_Indicator category_for_edit_rus;

    private List<Category_Indicator> categories;

    private List<Direction> directions;
    private StringBuilder directionsInfo = new StringBuilder();

    private Direction newDirection = new Direction();
    private Direction currentDirection;
    private long currentDirectionId;

    private String newNameDirKaz;
    private String newNameDirRus;

    private StringBuilder currentSpecInfo = new StringBuilder();
    private Specialist currentSpecialist;
    long spec_id;
    private Category_Indicator category_indicator_ru;
    private Category_Indicator category_indicator_kz;
    private long last_id_2 = 1L;


    @Override
    public boolean execute() throws TelegramApiException {
        if (!isAdmin() && !isMainAdmin()) {
            sendMessage(Const.NO_ACCESS);
            return EXIT;
        }
        try {

            switch (waitingType) {
                case START:
                    deleteMess();
                    if (isButton(77) || isButton(78)) {
                        sendMessageWithKeyboard(getText(35), 11);
                    }

                    getAllCategories();

                    waitingType = WaitingType.CHOOSE_CATEGORY;
                    return COMEBACK;
                case CHOOSE_CATEGORY:
                    deleteMess();
                    if (hasCallbackQuery()) {
                        if (isButton(72)) {
                            category_indicator_ru = new Category_Indicator();
                            category_indicator_kz = new Category_Indicator();
                            deleteMessageId = sendMessageWithKeyboard(getText(32), 37);

                            waitingType = WaitingType.SET_NAME_KZ;
                        } else {
                            currentCategoryId = Integer.parseInt(updateMessageText);
                            choosenCategory();

                            waitingType = WaitingType.CHOOSE_SERVICE_OR_EDIT_CATEGOR;
                        }
                    } else {
                        sendMessageWithKeyboard(getText(1002) + "\n" + getText(82), 11);
                        waitingType = WaitingType.START;
                    }


                    return COMEBACK;

                case CHOOSE_SERVICE_OR_EDIT_CATEGOR:
                    deleteMess();
                    if (hasCallbackQuery()) {


                        if (isButton(73)) { // 73 = редактировать текущую категорию
                            deleteMessageId = sendMessageWithKeyboard(getText(26), 35);
                            waitingType = WaitingType.DELETE_OR_EDIT_CATEGOR;
                            return COMEBACK;
                        }
                        if (isButton(74)) { //74 = добавить услугу
                            newServiceRus = new Service();
                            newServiceKaz = new Service();
                            deleteMess();
//                            deleteMessageId = sendMessage(36);
                            deleteMessageId = sendMessageWithKeyboard(getText(36), 11);

                            waitingType = WaitingType.SET_SERVICE_NAME_KAZ;

                        } else { // рекдактировать услугу

                            currentServiceId = Integer.parseInt(updateMessageText);
                            if (currentServiceId == -1) {
                                getAllCategories();

                                waitingType = WaitingType.CHOOSE_CATEGORY;
                            } else {
                                currentServiceKaz = serviceRepository.findById2AndLangId(currentServiceId, 2);
                                currentServiceRus = serviceRepository.findById2AndLangId(currentServiceId, 1);

                                pageEditService();
                            }

                        }
                    } else {
                        sendMessageWithKeyboard(getText(1002) + "\n" + getText(82), 11);
                        waitingType = WaitingType.START;
                    }
                    return COMEBACK;


                case DELETE_OR_EDIT_CATEGOR:
                    deleteMess();
                    if (hasCallbackQuery()) {
                        if (isButton(76)) { // 76 изменить название
                            category_for_edit_rus = categoriesIndicatorRepository.findBySecondAndLangId(currentCategoryId, 1);
                            category_for_edit_kaz = categoriesIndicatorRepository.findBySecondAndLangId(currentCategoryId, 2);
                            StringBuilder stringBuilder = new StringBuilder();

                            stringBuilder.append(getText(29)).append(category_for_edit_kaz.getName()).append(next);
                            stringBuilder.append(getText(28));

                            deleteMessageId = sendMessageWithKeyboard(stringBuilder.toString(), 36);
                            waitingType = WaitingType.SET_NEW_NAME_CATEGORY_KAZ;

                        }
                        if (isButton(1005)) { // 1005 - button back
                            deleteMess();
                            choosenCategory();
                            waitingType = WaitingType.CHOOSE_SERVICE_OR_EDIT_CATEGOR;

                        }
                    } else {
                        sendMessageWithKeyboard(getText(1002) + "\n" + getText(82), 11);
                        waitingType = WaitingType.START;
                    }
                    return COMEBACK;

                case SET_NEW_NAME_CATEGORY_KAZ:
                    if (hasMessageText()) {
                        deleteMess();

                        category_for_edit_kaz.setName(updateMessageText);


                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append(getText(29)).append(category_for_edit_rus.getName()).append(next);
                        stringBuilder.append(getText(30));

                        deleteMessageId = sendMessageWithKeyboard(stringBuilder.toString(), 36);
                        waitingType = WaitingType.SET_NEW_NAME_CATEGORY_RUS;
                    } else {
                        sendMessageWithKeyboard(getText(1002) + "\n" + getText(82), 11);
                        waitingType = WaitingType.START;
                    }

                    return COMEBACK;
                case SET_NEW_NAME_CATEGORY_RUS:
                    if (hasMessageText()) {
                        deleteMess();

                        category_for_edit_rus.setName(updateMessageText);

                        categoriesIndicatorRepository.save(category_for_edit_kaz);
                        categoriesIndicatorRepository.save(category_for_edit_rus);
                        deleteMessageId = sendMessageWithKeyboard(getText(31), 11);
                    } else {
                        sendMessageWithKeyboard(getText(1002) + "\n" + getText(82), 11);
                        waitingType = WaitingType.START;
                    }
                    return COMEBACK;
                case SET_SERVICE_NAME_KAZ:
                    if (hasMessageText()) {
                        deleteMess();
//                    newServiceKaz = new Service();
                        newServiceKaz.setName(updateMessageText);
//                        deleteMessageId = sendMessage(37);
                        deleteMessageId = sendMessageWithKeyboard(getText(37), 11);

                        waitingType = WaitingType.SET_SERVICE_NAME_RUS;
                    } else {
                        sendMessageWithKeyboard(getText(1002) + "\n" + getText(82), 11);
                        waitingType = WaitingType.START;
                    }

                    return COMEBACK;

                case SET_SERVICE_NAME_RUS:
                    if (hasMessageText()) {
                        deleteMess();
//                    newServiceRus = new Service();
                        newServiceRus.setName(updateMessageText);
                        deleteMessageId = sendMessageWithKeyboard(getText(38), 38);
                        waitingType = WaitingType.SET_MODE_SERVICE;
                    } else {
                        sendMessageWithKeyboard(getText(1002) + "\n" + getText(82), 11);
                        waitingType = WaitingType.START;
                    }


                    return COMEBACK;

                case SET_MODE_SERVICE:
                    deleteMess();
                    if (hasCallbackQuery()) {
                        if (isButton(79)) { // 79 - кнопка разовое посещение
                            newServiceKaz.setStatusId(1);
                            newServiceRus.setStatusId(1);

                        }
                        if (isButton(80)) { // 80 - курс
                            newServiceKaz.setStatusId(2);
                            newServiceRus.setStatusId(2);

                        }
                        if (isButton(81)) { // 81 - персон
                            newServiceKaz.setStatusId(3);
                            newServiceRus.setStatusId(3);
                        }
//                        deleteMessageId = sendMessage(43);
                        deleteMessageId = sendMessageWithKeyboard(getText(43), 11);

                        waitingType = WaitingType.SET_DESC_SERVICE_KAZ;
                    } else {
//                        sendMessage(1002);
                        sendMessageWithKeyboard(getText(1002), 11);

                        deleteMessageId = sendMessageWithKeyboard(getText(38), 38);
                        waitingType = WaitingType.SET_MODE_SERVICE;
                    }
                    return COMEBACK;


                case SET_DESC_SERVICE_KAZ:
                    deleteMess();
                    if (hasMessageText()) {

                        newServiceKaz.setDescription(updateMessageText);

//                        deleteMessageId = sendMessage(45);
                        deleteMessageId = sendMessageWithKeyboard(getText(45), 11);
                        waitingType = WaitingType.SET_DESC_SERVICE_RUS;
                    } else {
                        sendMessageWithKeyboard(getText(1002) + "\n" + getText(82), 11);
                        waitingType = WaitingType.START;
                    }


                    return COMEBACK;
                case SET_DESC_SERVICE_RUS:
                    deleteMess();
                    if (hasMessageText()) {
                        newServiceRus.setDescription(updateMessageText);


                        services = serviceRepository.findAllByLangIdOrderById(getLanguage().getId());
                        long last_id_2 = 1;
                        if (services != null) {
                            if (services.size() != 0) {
                                last_id_2 = services.get(services.size() - 1).getId2() + 1;
                            }
                        }

                        newServiceKaz.setLangId(2);
                        newServiceRus.setLangId(1);

                        newServiceKaz.setCategoryId(currentCategoryId);
                        newServiceRus.setCategoryId(currentCategoryId);

                        newServiceKaz.setId2(last_id_2);
                        newServiceRus.setId2(last_id_2);

                        if (newServiceKaz.getStatusId() == 3) {
                            waitingType = WaitingType.SET_AFISHA;
                            deleteMessageId = sendMessageWithKeyboard(getText(44), 11);
                        } else {
                            serviceRepository.save(newServiceKaz);
                            serviceRepository.save(newServiceRus);

                            list.clear();
                            deleteMess();

                            choosenCategory();
                            waitingType = WaitingType.CHOOSE_SERVICE_OR_EDIT_CATEGOR;

                        }
                    } else {
                        sendMessageWithKeyboard(getText(1002) + "\n" + getText(82), 11);
                        waitingType = WaitingType.START;
                    }


                    return COMEBACK;
                case ADD_OR_EDIT_SERVICE_AND_ADD_OR_DELETE_SPEC_OR_EDIT_DESC:
                    deleteMess();

                    if (hasCallbackQuery()) {
                        if(isButton(114)){ //on/off
                            deleteMess();

                            currentServiceKaz.setActive(!currentServiceKaz.isActive());
                            currentServiceRus.setActive(!currentServiceRus.isActive());

                            serviceRepository.save(currentServiceKaz);
                            serviceRepository.save(currentServiceRus);
                            pageEditService();
                        }

                        if (isButton(1009)) { // изменить название услуги
                            deleteMess();
                            deleteMessageId = sendMessageWithKeyboard(getText(49), 11);
                            waitingType = WaitingType.SET_NEW_NAME_SERVICE_KAZ;
                        }
//                        else if (isButton(83)) { // удалить услугу
//                            deleteMess();
//                            deleteMessageId = sendMessageWithKeyboard(getText(51), 42);
//                            waitingType = WaitingType.CONFIRM_DELETE_SERVICE;
//                        }

                        else if (isButton(97)) {// редактор направлений
                            deleteMess();
                            if (currentServiceId != 0) {
                                getDirections();
                            } else {
//                                sendMessage("Ошибка");
                                sendMessageWithKeyboard("Ошибка", 11);
                            }
                        } else if (isButton(88)) { // 88 - edit desc
                            deleteMess();
//                            deleteMessageId = sendMessage(54);
                            deleteMessageId = sendMessageWithKeyboard(getText(54), 11);
                            waitingType = WaitingType.SET_NEW_DESC_SERVICE_KAZ;
                        } else if (isButton(90)) { // 88 - edit photo
                            if (currentServiceRus.getStatusId() == 3) {
                                deleteMess();

//                                deleteMessageId = sendMessage(53);
                                deleteMessageId = sendMessageWithKeyboard(getText(53), 11);

                                waitingType = WaitingType.SET_NEW_PHOTO_SERVICE;

                            } else {
                                deleteMess();
//                                deleteMessageId = sendMessage( "К этой услуге нельзя прикрепить фото");
                                deleteMessageId = sendMessageWithKeyboard("К этой услуге нельзя прикрепить фото", 11);

                                pageEditService();

                            }
                        }
                    } else if (hasMessageText()) {
                        if (updateMessageText.contains("/back")) { // back
                            deleteMess();
                            sendMessageWithKeyboard(getText(12), 11);
                            choosenCategory();
                            waitingType = WaitingType.CHOOSE_SERVICE_OR_EDIT_CATEGOR;
                        }
                        else if (isButton(85)) { // добавить специалиста
                            deleteMess();
                            List<Specialist> specialistList = specialistRepository.findAll();
                            if (specialistList.size() == 0 || specialistList == null) {
                                sendMessageWithKeyboard(getText(88), 11);
                                pageEditService();
                                return COMEBACK;
                            } else {
                                ButtonsLeaf buttonsLeaf = new ButtonsLeaf(getNamesSpecs(specialistList));
                                deleteMessageId = sendMessageWithKeyboard(42, buttonsLeaf.getListButtonWhereIdIsData(getIdsSpecs(specialistList)));
                                waitingType = WaitingType.CHOOSE_SPEC;
                            }
                        } else {
                            sendMessageWithKeyboard(getText(1002), 11);
                            pageEditService();
                        }
                    } else {
                        sendMessageWithKeyboard(getText(1002), 11);
                        pageEditService();
                        waitingType = WaitingType.ADD_OR_EDIT_SERVICE_AND_ADD_OR_DELETE_SPEC_OR_EDIT_DESC;

                    }
                    return COMEBACK;

                case SET_NEW_DESC_SERVICE_KAZ:
                    deleteMess();
                    if (hasMessageText()) {
                        currentServiceKaz.setDescription(updateMessageText);
//                        deleteMessageId = sendMessage(55);
                        deleteMessageId = sendMessageWithKeyboard(getText(55), 11);

                        waitingType = WaitingType.SET_NEW_DESC_SERVICE_RUS;
                    } else {
                        sendMessageWithKeyboard(getText(1002), 11);
                        deleteMess();
                        deleteMessageId = sendMessageWithKeyboard(getText(54), 11);
//                        deleteMessageId = sendMessage(54);
                        waitingType = WaitingType.SET_NEW_DESC_SERVICE_KAZ;
                    }
                    return COMEBACK;
                case SET_NEW_DESC_SERVICE_RUS:
                    deleteMess();
                    if (hasMessageText()) {
                        currentServiceRus.setDescription(updateMessageText);
                        serviceRepository.save(currentServiceKaz);
                        serviceRepository.save(currentServiceRus);
//                        deleteMessageId = sendMessage(31);
                        deleteMessageId = sendMessageWithKeyboard(getText(31), 11);

                        pageEditService();

                        waitingType = WaitingType.ADD_OR_EDIT_SERVICE_AND_ADD_OR_DELETE_SPEC_OR_EDIT_DESC;
                    } else {
                        sendMessageWithKeyboard(getText(1002), 11);
//                        deleteMessageId = sendMessage(55);
                        deleteMessageId = sendMessageWithKeyboard(getText(55), 11);

                        waitingType = WaitingType.SET_NEW_DESC_SERVICE_RUS;
                    }
                    return COMEBACK;
                case SET_NEW_PHOTO_SERVICE:

                    deleteMess();

                    if (hasPhoto()) {
                        currentServiceRus = serviceRepository.findById2AndLangId(currentServiceId, 1);
                        currentServiceKaz = serviceRepository.findById2AndLangId(currentServiceId, 2);

                        currentServiceRus.setPhoto(updateMessage.getPhoto().get(0).getFileId());
                        currentServiceKaz.setPhoto(updateMessage.getPhoto().get(0).getFileId());

                        serviceRepository.save(currentServiceKaz);
                        serviceRepository.save(currentServiceRus);

//                        deleteMessageId = sendMessage(31);
                        deleteMessageId = sendMessageWithKeyboard(getText(31), 11);

                        choosenCategory();
                        waitingType = WaitingType.CHOOSE_SERVICE_OR_EDIT_CATEGOR;
                    } else {
                        sendMessageWithKeyboard(getText(1002), 11);
//                        deleteMessageId = sendMessage(53);
                        deleteMessageId = sendMessageWithKeyboard(getText(53), 11);

                        waitingType = WaitingType.SET_NEW_PHOTO_SERVICE;
                    }
                    return COMEBACK;
                case SET_NEW_NAME_SERVICE_KAZ:
                    deleteMess();
                    if (hasMessageText()) {
                        newNameServiceKaz = updateMessageText;
//                        deleteMessageId = sendMessage(50);
                        deleteMessageId = sendMessageWithKeyboard(getText(50), 11);

                        waitingType = WaitingType.SET_NEW_NAME_SERVICE_RUS;
                    } else {
                        sendMessageWithKeyboard(getText(1002), 11);
//                        deleteMessageId = sendMessage(49);
                        deleteMessageId = sendMessageWithKeyboard(getText(49), 11);

                        waitingType = WaitingType.SET_NEW_NAME_SERVICE_KAZ;
                    }

                    return COMEBACK;

                case SET_NEW_NAME_SERVICE_RUS:
                    deleteMess();
                    if (hasMessageText()) {
                        newNameServiceRus = updateMessageText;
//                        deleteMessageId = sendMessage(31);
                        deleteMessageId = sendMessageWithKeyboard(getText(31), 11);


                        currentServiceKaz.setName(newNameServiceKaz);
                        currentServiceRus.setName(newNameServiceRus);

                        serviceRepository.save(currentServiceRus);
                        serviceRepository.save(currentServiceKaz);


                        pageEditService();

                        waitingType = WaitingType.ADD_OR_EDIT_SERVICE_AND_ADD_OR_DELETE_SPEC_OR_EDIT_DESC;
                    } else {
                        sendMessageWithKeyboard(getText(1002), 11);
//                        deleteMessageId = sendMessage(50);
                        deleteMessageId = sendMessageWithKeyboard(getText(50), 11);

                        waitingType = WaitingType.SET_NEW_NAME_SERVICE_RUS;
                    }

                    return COMEBACK;

                case CHOOSE_SPEC:
                    deleteMess();

                    if (hasCallbackQuery()) {
                        spec_id = Long.parseLong(updateMessageText);
                        getSpecInfo(spec_id);
                    } else {
//                        sendMessage(1002);
                        deleteMessageId = sendMessageWithKeyboard(getText(1002), 11);

                        List<Specialist> specialistList = specialistRepository.findAll();
                        deleteMessageId = sendMessageWithKeyboard(42, buttonsLeaf.getListButtonWhereIdIsData(getIdsSpecs(specialistList)));
                        waitingType = WaitingType.CHOOSE_SPEC;
                    }


                    return COMEBACK;

                case CHOOSE_OPTION_FOR_SPEC:
                    deleteMess();
                    if (hasCallbackQuery()) {
                        if (isButton(103)) {

                            Services_Spec services_spec = new Services_Spec(currentServiceId, spec_id);
                            servicesSpecsRepository.save(services_spec);
                            pageEditService();
                            waitingType = WaitingType.ADD_OR_EDIT_SERVICE_AND_ADD_OR_DELETE_SPEC_OR_EDIT_DESC;
                        } else if (isButton(1005)) {
                            List<Specialist> specialistList = specialistRepository.findAll();
                            ButtonsLeaf buttonsLeaf = new ButtonsLeaf(getNamesSpecs(specialistList));
                            deleteMessageId = sendMessageWithKeyboard(42, buttonsLeaf.getListButtonWhereIdIsData(getIdsSpecs(specialistList)));
                            waitingType = WaitingType.CHOOSE_SPEC;
                        } else {
//                            sendMessage(1002);
                            deleteMessageId = sendMessageWithKeyboard(getText(1002), 11);

                            getSpecInfo(spec_id);
                        }
                    } else {
//                        sendMessage(1002);
                        sendMessageWithKeyboard(getText(1002), 11);

                        getSpecInfo(spec_id);
                    }
                    return COMEBACK;

                case SET_AFISHA:
                    deleteMess();
                    if (hasPhoto()) {
                        newServiceKaz.setPhoto(update.getMessage().getPhoto().get(0).getFileId());
                        newServiceRus.setPhoto(update.getMessage().getPhoto().get(0).getFileId());
                        //                sendMessageWithKeyboard(getText(39), 11);
                        serviceRepository.save(newServiceKaz);
                        serviceRepository.save(newServiceRus);


                        choosenCategory();

                        waitingType = WaitingType.CHOOSE_SERVICE_OR_EDIT_CATEGOR;

                    } else {
//                        secondDeleteMessageId = sendMessage(1002);
                        secondDeleteMessageId = sendMessageWithKeyboard(getText(1002), 11);

                        waitingType = WaitingType.SET_AFISHA;
//                        deleteMessageId = sendMessage(44);
                        deleteMessageId = sendMessageWithKeyboard(getText(44), 11);

                    }
                    return COMEBACK;

                case CHOOSE_OPTION:
                    deleteMess();
                    if (hasMessageText()) {
                        if (updateMessageText.contains("/new")) {
                            newDirection = new Direction();

                            deleteMessageId = sendMessageWithKeyboard(getText(71), 46);
                            waitingType = WaitingType.SET_KAZ_NAME_DIRECTION;
                        }

                        if (updateMessageText.contains("/edit")) {
                            try {
                                currentDirectionId = Long.parseLong(updateMessageText.replaceAll("/edit", ""));

                                if (currentDirectionId != 0) {
                                    deleteMessageId = sendMessageWithKeyboard(getText(73), 46);
                                    waitingType = WaitingType.SET_NEW_KAZ_NAME_DIRECTION;
                                } else {
                                    getDirections();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                getDirections();
                            }

                        }

                        if (updateMessageText.contains("/back")) {
                            currentServiceKaz = serviceRepository.findById2AndLangId(currentServiceId, 2);
                            currentServiceRus = serviceRepository.findById2AndLangId(currentServiceId, 1);

                            deleteMess();
                            if (currentServiceRus.getStatusId() == 3) {
                                deleteMessageId = sendMessageWithPhotoAndKeyboard(getText(29) + (getLanguage().getId() == 1 ? currentServiceRus.getName() : currentServiceKaz.getName()) + next + getText(69) + (getLanguage().getId() == 1 ? currentServiceRus.getDescription() : currentServiceKaz.getDescription()), 39, currentServiceRus.getPhoto());
                            } else if (currentServiceRus.getStatusId() == 1) {
                                deleteMessageId = sendMessageWithKeyboard(getText(29) + (getLanguage().getId() == 1 ? currentServiceRus.getName() : currentServiceKaz.getName()) + next + getText(69) + (getLanguage().getId() == 1 ? currentServiceRus.getDescription() : currentServiceKaz.getDescription()), 48);
                            } else {
                                deleteMessageId = sendMessageWithKeyboard(getText(29) + (getLanguage().getId() == 1 ? currentServiceRus.getName() : currentServiceKaz.getName()) + next + getText(69) + (getLanguage().getId() == 1 ? currentServiceRus.getDescription() : currentServiceKaz.getDescription()), 43);
                            }
                            services_specs = servicesSpecsRepository.findAllByServiceIdOrderById(currentServiceId);
                            secondDeleteMessageId = sendMessageWithKeyboard(String.format(getText(41), parseSpec(services_specs)), 11);
                            waitingType = WaitingType.ADD_OR_EDIT_SERVICE_AND_ADD_OR_DELETE_SPEC_OR_EDIT_DESC;
                        }
                    } else {
                        sendMessageWithKeyboard(getText(1002), 11);
                        getDirections();
                    }


                    return COMEBACK;
                case SET_NEW_KAZ_NAME_DIRECTION:
                    if (hasMessageText()) {
                        if (isButton(95)) {
                            getDirections();
                        } else {
                            newNameDirKaz = updateMessageText;
                            deleteMessageId = sendMessageWithKeyboard(getText(74), 46);
                            waitingType = WaitingType.SET_NEW_RUS_NAME_DIRECTION;
                        }
                    } else {
                        sendMessageWithKeyboard(getText(1002), 11);

                        deleteMessageId = sendMessageWithKeyboard(getText(73), 46);
                        waitingType = WaitingType.SET_NEW_KAZ_NAME_DIRECTION;

                    }
                    return COMEBACK;

                case SET_NEW_RUS_NAME_DIRECTION:
                    if (hasMessageText()) {
                        if (isButton(95)) {
                            getDirections();
                        } else {
                            newNameDirRus = updateMessageText;
                            currentDirection = directionRepository.findById(currentDirectionId);

                            currentDirection.setNameKaz(newNameDirKaz);
                            currentDirection.setNameRus(newNameDirRus);
                            directionRepository.save(currentDirection);


                            getDirections();
                        }
                    } else {
                        sendMessageWithKeyboard(getText(1002), 11);
                        deleteMessageId = sendMessageWithKeyboard(getText(74), 46);
                        waitingType = WaitingType.SET_NEW_RUS_NAME_DIRECTION;
                    }
                    return COMEBACK;
                case SET_KAZ_NAME_DIRECTION:
                    if (hasMessageText()) {
                        if (isButton(95)) {
                            getDirections();
                        } else {

                            deleteMess();
                            newDirection.setNameKaz(updateMessageText);

                            deleteMessageId = sendMessageWithKeyboard(getText(72), 46);
                            waitingType = WaitingType.SET_RUS_NAME_DIRECTION;
                        }
                    } else {
                        sendMessageWithKeyboard(getText(1002), 11);

                        deleteMessageId = sendMessageWithKeyboard(getText(71), 46);
                        waitingType = WaitingType.SET_KAZ_NAME_DIRECTION;
                    }
                    return COMEBACK;

                case SET_RUS_NAME_DIRECTION:
                    if (hasMessageText()) {
                        if (isButton(95)) {
                            getDirections();
                        } else {
                            deleteMess();

                            newDirection.setNameRus(updateMessageText);
                            deleteMessageId = sendMessageWithKeyboard(getText(72), 46);
                            newDirection.setServiceId(currentServiceId);

                            directionRepository.save(newDirection);
                            getDirections();
                        }
                    } else {
                        sendMessageWithKeyboard(getText(1002), 11);

                        deleteMessageId = sendMessageWithKeyboard(getText(72), 46);
                        waitingType = WaitingType.SET_RUS_NAME_DIRECTION;
                    }

                    return COMEBACK;
                case SET_NAME_KZ:
                    if (hasMessageText()) {
                        deleteMess();
                        category_indicator_kz.setName(updateMessageText);
                        category_indicator_kz.setLangId(2);

                        waitingType = WaitingType.SET_NAME_RU;
                        deleteMessageId = sendMessageWithKeyboard(getText(33), 37);
                    } else {
                        deleteMessageId = sendMessageWithKeyboard(getText(32), 37);
                        waitingType = WaitingType.SET_NAME_KZ;
                    }

                    return COMEBACK;

                case SET_NAME_RU:
                    if (hasMessageText()) {
                        deleteMess();
                        category_indicator_ru.setName(updateMessageText);
                        category_indicator_ru.setLangId(1);

                        List<Category_Indicator> category_indicators = categoriesIndicatorRepository.findAllByLangIdOrderById(getLanguage().getId());

                        if (category_indicators != null) {
                            if (category_indicators.size() != 0) {
                                last_id_2 = category_indicators.get(category_indicators.size() - 1).getSecond() + 1;
                            }
                        } else {
                            last_id_2 = 1;
                        }

                        category_indicator_kz.setSecond(last_id_2);
                        category_indicator_ru.setSecond(last_id_2);
                        categoriesIndicatorRepository.saveAndFlush(category_indicator_kz);

                        categoriesIndicatorRepository.saveAndFlush(category_indicator_ru);


                        sendMessageWithKeyboard(getText(34), 11);
                        getAllCategories();
                    } else {
                        waitingType = WaitingType.SET_NAME_RU;
                        deleteMessageId = sendMessageWithKeyboard(getText(33), 37);
                    }

                    return COMEBACK;


            }
        } catch (Exception e) {
            e.printStackTrace();
            sendMessageWithKeyboard("Добро пожаловать!", 1);
        }
        return EXIT;
    }

    private void getSpecInfo(long spec_id) throws TelegramApiException {
        deleteMess();
        currentSpecInfo = new StringBuilder();
        currentSpecialist = specialistRepository.findById(spec_id);

        currentSpecInfo.append(getText(66)).append(currentSpecialist.getFullName()).append(next);
        currentSpecInfo.append(getText(67)).append(getLanguage().getId() == 1 ? currentSpecialist.getDescriptionRus() : currentSpecialist.getDescriptionKaz()).append(next);
        deleteMessageId = sendMessageWithPhotoAndKeyboard(currentSpecInfo.toString(), 51, currentSpecialist.getPhoto());
        waitingType = WaitingType.CHOOSE_OPTION_FOR_SPEC;
    }

    private void getAllCategories() throws TelegramApiException {
        list = new ArrayList<>();
        deleteMessageId = sendMessageWithKeyboard(getText(21), 32);

        categories = categoriesIndicatorRepository.findAllByLangIdOrderById(getLanguage().getId());
        buttonsLeaf = new ButtonsLeaf(getListStringCategoryIndicator(categories));
        secondDeleteMessageId = sendMessageWithKeyboard(getText(22), buttonsLeaf.getListButtonWhereIdIsData(getIdsCategories(categories)));
        waitingType = WaitingType.CHOOSE_CATEGORY;

    }

    private void pageEditService() throws TelegramApiException {
        deleteMess();
        if (currentServiceRus.getStatusId() == 3) {

            try {

                deleteMessageId = sendMessageWithPhotoAndKeyboard(getInfoService(currentServiceRus, currentServiceKaz), 39, currentServiceRus.getPhoto());
            }catch (Exception e){
                deleteMessageId = sendMessageWithKeyboard(getInfoService(currentServiceRus, currentServiceKaz), 39);

            }
//
//            deleteMessageId = sendMessageWithKeyboard(getText(40), 39);
        } else if (currentServiceRus.getStatusId() == 1) {
            deleteMessageId = sendMessageWithKeyboard(getInfoService(currentServiceRus, currentServiceKaz),48);
        } else {
            deleteMessageId = sendMessageWithKeyboard(getInfoService(currentServiceRus, currentServiceKaz), 43);
        }
        services_specs = servicesSpecsRepository.findAllByServiceIdOrderById(currentServiceId);
//        secondDeleteMessageId = sendMessage(String.format(getText(41), parseSpec(services_specs) ));
        secondDeleteMessageId = sendMessageWithKeyboard(String.format(getText(41), parseSpec(services_specs)), 11);

        waitingType = WaitingType.ADD_OR_EDIT_SERVICE_AND_ADD_OR_DELETE_SPEC_OR_EDIT_DESC;

    }

    private String getInfoService(Service currentServiceRus, Service currentServiceKaz) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getText(29)).append((getLanguage().getId() == 1 ? currentServiceRus.getName() : currentServiceKaz.getName())).append(next)
                .append(getText(69)).append(getLanguage().getId() == 1 ? currentServiceRus.getDescription() : currentServiceKaz.getDescription()).append(next)
                .append(getText(1088)).append(currentServiceKaz.isActive() ? getText(111) : getText(112));

        return stringBuilder.toString();
    }

    private void choosenCategory() throws TelegramApiException {

        deleteMess();
        list.clear();
        deleteMessageId = sendMessageWithKeyboard(getText(24), 33); // 24 - Чтобы редактировать выбранную категорию нажмите на "Редактировать текущую категорию"
//        secondDeleteMessageId = sendMessageWithKeyboard(getText(25), 34); // 25 - Чтобы добавить новую услугу на эту категорию нажмите на "Добавить услугу"
        services = serviceRepository.findAllByCategoryIdAndLangIdOrderById(currentCategoryId, getLanguage().getId());

        buttonsLeaf = new ButtonsLeaf(getNamesOfServices(services));

        thirdDeleteMessageId = toDeleteKeyboard(sendMessageWithKeyboard(getText(23), buttonsLeaf.getListButtonWhereIdIsData(getIdsServices(services))));
    }

    private List<String> getIdsSpecs(List<Specialist> specialistList) {
        List<String> ids = new ArrayList<>();

        for (Specialist specialist : specialistList) {
            ids.add(String.valueOf(specialist.getId()));
        }

        return ids;
    }

    private List<String> getNamesSpecs(List<Specialist> specialistList) {
        List<String> names = new ArrayList<>();
        for (Specialist spec : specialistList) {
            names.add("№ " + spec.getId() + " " + spec.getFullName());
        }
        return names;
    }

    private List<String> getListStringCategoryIndicator(List<Category_Indicator> category_indicators) {
        List<String> list777 = new ArrayList<>();
        for (Category_Indicator category_indicator : category_indicators) {
            list777.add(category_indicator.getName());
        }
        return list777;
    }

    private List<String> getNamesOfServices(List<Service> services) {
        List<String> list777 = new ArrayList<>();
        for (Service service : services) {
            list777.add(service.getName());
        }
        list777.add(buttonRepository.getButtonText(1005, getLanguage().getId()));

        return list777;
    }

    private List<String> getIdsServices(List<Service> services) {
        List<String> list777 = new ArrayList<>();
        for (Service service : services) {
            list777.add(String.valueOf(service.getId2()));
        }
        list777.add("-1");
        return list777;
    }

    private List<String> getIdsCategories(List<Category_Indicator> category_indicators) {
        List<String> list777 = new ArrayList<>();
        for (Category_Indicator category_indicator : category_indicators) {
            list777.add(String.valueOf(category_indicator.getSecond()));
        }
        return list777;
    }

    private void deleteUpdateMess() {
        deleteMessage(updateMessageId);
    }

    private void deleteMess() {
        deleteMessage(updateMessageId);
        deleteMessage(deleteMessageId);
        deleteMessage(secondDeleteMessageId);
        deleteMessage(thirdDeleteMessageId);
    }

    private void getDirections() throws TelegramApiException {
        deleteMess();
        directions = directionRepository.findAllByServiceIdOrderById(currentServiceId);
        directionsInfo = getDirectionsInfo(directions);
        deleteMessageId = sendMessageWithKeyboard(String.format(getText(70), directionsInfo.toString()), 11);
        waitingType = WaitingType.CHOOSE_OPTION;
    }

    private StringBuilder getDirectionsInfo(List<Direction> directions) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Direction direction : directions) {
            stringBuilder.append(getLanguage().getId() == 1 ? direction.getNameRus() : direction.getNameKaz()).append(space).append("✍/edit").append(direction.getId()).append(next);
        }
        return stringBuilder;
    }

    private String parseSpec(List<Services_Spec> services_specs) {
        StringBuilder specs = new StringBuilder();

        for (Services_Spec services_spec : services_specs) {
            specs.append(specialistRepository.findById(services_spec.getSpecId()).getFullName()).append(next);
        }
        return specs.toString();
    }

}

