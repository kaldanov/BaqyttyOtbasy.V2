package com.telegrambot.command.impl;

import com.telegrambot.command.Command;
import com.telegrambot.config.Bot;
import com.telegrambot.entity.custom.*;
import com.telegrambot.entity.enums.WaitingType;
import com.telegrambot.service.OtmetkaService;
import com.telegrambot.util.ButtonsLeaf;
import com.telegrambot.util.Const;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.util.*;


public class id040_MassovayaOtmetka extends Command {
    private ButtonsLeaf buttonsLeaf;


    private int secondDeleteMessageId;
    private ArrayList<String> socialBenefitsList = new ArrayList<>();
    private int deleteMessageId;
    // for request service
    private List<Category_Indicator> allCategories;
    private Category_Indicator currentCategory;
    private long currentCategoryId;

    private List<Service> servicesOfCategory;
    private Service currentService;
    private long currentServiceId;
    ButtonsLeaf buttonsLeafDir;
    Map<Long, Boolean> checkBox;


    @Override
    public boolean execute() throws TelegramApiException {

        switch (waitingType) {
            case START:
                if (!isOper()) {
                    sendMessage(Const.NO_ACCESS);
                    return EXIT;
                }
                if (userRepository.findByChatId(chatId) == null) {
                    sendMessageWithKeyboard("Вы не прошли регистрацию", 57);
                    return EXIT;
                }
                checkBox = new HashMap<>();
                deleteMess();
                getAllCategories();

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
                        if (currentService.getStatusId() == 1) {
                            sendDirections();
                        } else {
                            sendGetFie();
                        }
                    }
                }
                return COMEBACK;

            case CHOOSE_DIRECTION:
                if (hasCallbackQuery()) {
                    if (isButton(117) && checkBox.size() != 0) {
                        sendGetFie();
                    } else if (isButton(1005)) {
                        deleteUpdateMess();
                        getServicesOfCategory();
                    } else {
                        if(buttonsLeafDir.isNext(updateMessageText)){
                            editMessageWithKeyboard("Выберите направление:", updateMessageId, (InlineKeyboardMarkup) buttonsLeafDir.getListButtonWhereIdIsData());
                            return COMEBACK;
                        }
                        Direction cur = directionRepository.findById(getLong(updateMessageText));
                        if (cur != null) {
                            if (checkBox.get(getLong(updateMessageText)) != null) {
                                checkBox.remove(getLong(updateMessageText));
                            } else {
                                checkBox.put(getLong(updateMessageText), true);
                            }
                            sendEditDirections();
                        }
                    }
                }
                return COMEBACK;
            case SET_FILE:
                if (hasDocument()) {
                    File file = bot.downloadFile(uploadFile(update.getMessage().getDocument().getFileId()));
                    OtmetkaService otmetkaService = new OtmetkaService();

                    List<Registration_Service> regs = otmetkaService.sendServiceReport(chatId, bot, file);
                    finish(regs);
                }
                return EXIT;

        }

        return EXIT;
    }

    private void sendGetFie() throws TelegramApiException {
        sendMessage("Отправьте Excel файл в формате -> ИИН | Дата(21.08.2021) ");
        waitingType = WaitingType.SET_FILE;
    }

    private void finish(List<Registration_Service> registration_services) throws TelegramApiException {
        for (Registration_Service reg : registration_services) {
            reg.setServiceId(currentServiceId);
            reg.setSpecId(getSpecRendom(currentService));
            if (currentService.getStatusId() != 2) {
                reg.setFinish(true);
                reg = registrationServiceRepository.save(reg);
                for (Map.Entry<Long, Boolean> entry : checkBox.entrySet()) {
                    Long key = entry.getKey();
                    DirectionRegistration dir = new DirectionRegistration();
                    dir.setDirectionId(key);
                    dir.setRegistrationId(reg.getId());
                    directionRegistrationRepository.save(dir);
                }
            } else {
                List<Registration_Service> reg777 = registrationServiceRepository.findAllByIinAndServiceIdOrderById(reg.getIin(), currentServiceId);
                if (reg777.size() == 0) {
                    reg = registrationServiceRepository.save(reg);
                } else reg.setId(reg777.get(reg777.size() - 1).getId());

                ComesCourse comesCourse = new ComesCourse();
                comesCourse.setActionDate(reg.getDateReg());
                comesCourse.setRegistrationServiceId(reg.getId());
                comesCourseRepository.save(comesCourse);
            }

        }
        sendMessage("Данные добавилось: " + registration_services.size() + "человек добавлены");
    }

    private long getSpecRendom(Service currentService) {
        List<Services_Spec> spec = servicesSpecsRepository.findAllByServiceIdOrderById(currentServiceId);
        int random = (int) (Math.random() * (spec.size() - 1));
        return spec.get(random).getSpecId();
    }

    private long getLong(String updateMessageText) {
        try {
            return Long.parseLong(updateMessageText);
        } catch (Exception e) {
            return -1;
        }
    }

    private void sendDirections() throws TelegramApiException {

        List<Direction> directions = directionRepository.findAllByServiceIdOrderById(currentServiceId);
        List<String> names = new ArrayList<>();
        List<String> ids = new ArrayList<>();
        for (Direction direction : directions) {
            names.add(direction.getName(getLanguage().getId()) + getCheck(direction.getId()));
            ids.add(String.valueOf(direction.getId()));
        }
        names.add(buttonRepository.findByIdAndLangId(1005, getLanguage().getId()).getName());
        ids.add(buttonRepository.findByIdAndLangId(1005, getLanguage().getId()).getName());
        if (checkBox.size() > 0) {
            names.add(buttonRepository.findByIdAndLangId(117, getLanguage().getId()).getName());
            ids.add(buttonRepository.findByIdAndLangId(117, getLanguage().getId()).getName());
        }
        buttonsLeafDir = new ButtonsLeaf(names, ids, 95, true);
        sendMessageWithKeyboard("Выберите направление:", buttonsLeafDir.getListButtonWhereIdIsData());
        waitingType = WaitingType.CHOOSE_DIRECTION;
    }

    private void sendEditDirections() throws TelegramApiException {
        List<Direction> directions = directionRepository.findAllByServiceIdOrderById(currentServiceId);
        List<String> names = new ArrayList<>();
        List<String> ids = new ArrayList<>();
        for (Direction direction : directions) {
            names.add(direction.getName(getLanguage().getId()) + getCheck(direction.getId()));
            ids.add(String.valueOf(direction.getId()));
        }
        names.add(buttonRepository.findByIdAndLangId(1005, getLanguage().getId()).getName());
        ids.add(buttonRepository.findByIdAndLangId(1005, getLanguage().getId()).getName());
        if (checkBox.size() > 0) {
            System.out.println("DEBUG ====== 1" + "names size === " + names.size());
            names.add(buttonRepository.findByIdAndLangId(117, getLanguage().getId()).getName());
            ids.add(buttonRepository.findByIdAndLangId(117, getLanguage().getId()).getName());
        }

        buttonsLeafDir = new ButtonsLeaf(names, ids, 95, true);
        editMessageWithKeyboard("Выберите направление:", updateMessageId, (InlineKeyboardMarkup) buttonsLeafDir.getListButtonWhereIdIsData());
        waitingType = WaitingType.CHOOSE_DIRECTION;
    }

    private String getCheck(long id) {
        try {
            if (checkBox.get(id) != null) {
                return "✅";
            }
        } catch (Exception ignored) {
        }
        return "";
    }

    private void deleteUpdateMess() {
        deleteMessage(updateMessageId);
    }

    private int wrongData() throws TelegramApiException {
        return sendMessageWithKeyboard(getText(Const.WRONG_DATA_TEXT), 31);
    }


    private void deleteMess() {
        deleteMessage(updateMessageId);
        deleteMessage(deleteMessageId);
        deleteMessage(secondDeleteMessageId);
    }


    private boolean isIin(String text) {
        try {
            Long.parseLong(text);
            return text.length() == 12;
        } catch (Exception e) {
            return false;
        }
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
