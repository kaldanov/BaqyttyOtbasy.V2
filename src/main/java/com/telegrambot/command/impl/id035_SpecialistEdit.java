package com.telegrambot.command.impl;

import com.telegrambot.command.Command;
import com.telegrambot.entity.User;
import com.telegrambot.entity.custom.Registration_Service;
import com.telegrambot.entity.custom.Services_Spec;
import com.telegrambot.entity.custom.Specialist;
import com.telegrambot.entity.enums.WaitingType;
import com.telegrambot.util.ButtonsLeaf;
import com.telegrambot.util.Const;
import com.telegrambot.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
public class id035_SpecialistEdit extends Command {

    private StringBuilder   text;
    private List<Long>      allAdmins = new ArrayList<>();
    private int             message;
    private static String   delete;
    private static String   deleteIcon;
    private static String   showIcon;
    private int deleteMessageId;
    private int secondDeleteMessageId;
    private ButtonsLeaf buttonsLeaf;

//    private long currentSpecChatId;
    private long currentSpecId;
    private Specialist currentSpecialist;
    private StringBuilder currentSpecInfo = new StringBuilder();

    @Override
    public boolean  execute()           throws TelegramApiException {
        try{
            if (!isAdmin() && !isMainAdmin()) {
                sendMessage(Const.NO_ACCESS);
                return EXIT;
            }

            else{
                switch (waitingType) {
                    case START:
                        if(userRepository.findByChatId(chatId) == null){
                            sendMessageWithKeyboard("Вы не прошли регистрацию" , 57);
                            return EXIT;
                        }
                        allSpecs();
                        return COMEBACK;
                    case CHOOSE_SPEC_FOR_EDIT:
                        if (hasCallbackQuery()){
                            currentSpecId = Long.parseLong(updateMessageText);
                            sendSpecInfo();
                        }
                        else{
                            sendWrongDataWithEditKeyboard();
                            allSpecs();
                        }
                        return COMEBACK;
                    case CHOOSE_OPTION_EDIT_SPEC:
                        deleteMess();
                        if (hasCallbackQuery()){
                            if (isButton(1005)){ // back
                                allSpecs();
                            }
                            if (isButton(92)){ // edit name
                                deleteMessageId = sendMessageWithKeyboard(getText(57),46); // 57 - введите новое имя// 46 - отмена
                                waitingType = WaitingType.SET_NEW_NAME_SPEC;
                            }
                            if (isButton(93)){ // edit desc
                                deleteMessageId = sendMessageWithKeyboard(getText(54),46); // 58 - новое описание
                                waitingType = WaitingType.SET_NEW_DESC_SPEC_KAZ;
                            }
                            if (isButton(90)){ // edit photo
                                deleteMessageId = sendMessageWithKeyboard(getText(59), 46);
                                waitingType = WaitingType.SET_NEW_PHOTO_SPEC;
                            }
                            if (isButton(105)){ //delete spec // now set active
                                Specialist specialist = specialistRepository.findById(currentSpecId);
                                if (specialist != null){
                                    specialist.setActive(!specialist.isActive());
                                    specialistRepository.save(specialist);
                                }
                                sendSpecInfo();
                            }
                        }
                        else{
                            sendWrongDataWithEditKeyboard();
                            sendSpecInfo();
                        }
                        return COMEBACK;
                    case SET_NEW_NAME_SPEC:
                        deleteMess();
                        if (hasMessageText()){
                            currentSpecialist.setFullName(updateMessageText);
                            specialistRepository.save(currentSpecialist);
                            sendSpecInfo();
                        }
                        else if (isButton(95)){
                            sendMessageWithKeyboard(getText(12), 11);
                            sendSpecInfo();
                        }
                        else {
                           sendWrongDataWithEditKeyboard();
                           sendSpecInfo();
                        }

                        return COMEBACK;
                    case SET_NEW_DESC_SPEC_KAZ:
                        if (isButton(95)){
                            sendMessageWithKeyboard(getText(12), 11);
                            sendSpecInfo();
                        }
                        else if (hasMessageText()){
                            currentSpecialist.setDescriptionKaz(updateMessageText);
                            deleteMessageId = sendMessageWithKeyboard(getText(55),46); // 58 - новое описание
                            waitingType = WaitingType.SET_NEW_DESC_SPEC_RUS;
                        }
                        else{
                            sendWrongDataWithEditKeyboard();
                            sendSpecInfo();
                        }
//                        sendSpecInfo();
                        return COMEBACK;

                    case SET_NEW_DESC_SPEC_RUS:
                        if (isButton(95)){
                            sendMessageWithKeyboard(getText(12), 11);
                            sendSpecInfo();
                        }
                        else  if (hasMessageText()){
                            currentSpecialist.setDescriptionRus(updateMessageText);
                            specialistRepository.save(currentSpecialist);
                            sendSpecInfo();
                        }
                        else{
                            sendWrongData();
                            deleteMessageId = sendMessageWithKeyboard(getText(55),46); // 58 - новое описание
                        }
                        return COMEBACK;

                    case SET_NEW_PHOTO_SPEC:
                        if (isButton(95)) {
                            sendMessageWithKeyboard(getText(12), 11); // 12-редактирование 11 - клавиатоура редактирование
                            sendSpecInfo();
                        }
                        else{
                            if (hasPhoto()){
                                currentSpecialist.setPhoto(getUpdateMessagePhoto());
                                specialistRepository.save(currentSpecialist);
                                sendSpecInfo();
                            }
                            else {
                                sendMessage(1002);
                                deleteMessageId = sendMessageWithKeyboard(getText(59), 46);
                            }
                        }
                        return COMEBACK;


                    default: return EXIT;
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
            sendMessageWithKeyboard("Добро пожаловать!", 1);
            return EXIT;
        }

    }

    private void allSpecs() throws TelegramApiException {
        deleteMess();
        List<Specialist> specialists = specialistRepository.findAllByOrderById();
        buttonsLeaf = new ButtonsLeaf(getNamesSpesc(specialists));

        secondDeleteMessageId = sendMessageWithKeyboard(getText(56), 44);

        deleteMessageId = sendMessageWithKeyboard(getText(42), buttonsLeaf.getListButtonWhereIdIsData(getIdsSpecs(specialists)));

        waitingType = WaitingType.CHOOSE_SPEC_FOR_EDIT;
    }

    private void sendSpecInfo() throws TelegramApiException {
        deleteMess();
        currentSpecInfo = new StringBuilder();
        //currentSpecChatId = Long.parseLong(updateMessageText); //i have current spec,
        currentSpecialist = specialistRepository.findById(currentSpecId);

//        currentSpecInfo.append("№ ").append(currentSpecialist.getId()).append(next);
        currentSpecInfo.append(getText(66)).append(currentSpecialist.getFullName()).append(next);
        currentSpecInfo.append(getText(67)).append(getLanguage().getId() == 1 ? currentSpecialist.getDescriptionRus() :currentSpecialist.getDescriptionKaz() ).append(next);
        currentSpecInfo.append("Активность: ").append(currentSpecialist.isActive()? "Активный": "Не активный" ).append(next);

        try {

            deleteMessageId = sendMessageWithPhotoAndKeyboard(currentSpecInfo.toString(),45,currentSpecialist.getPhoto());
        }catch (Exception e){
            deleteMessageId = sendMessageWithKeyboard(currentSpecInfo.toString(),45);

        }

        waitingType =  WaitingType.CHOOSE_OPTION_EDIT_SPEC;

    }

    private void registerNewSpec()   throws TelegramApiException {
        String phone = update.getMessage().getContact().getPhoneNumber();
        if(phone.charAt(0) == '8')
            phone = phone.replaceFirst("8" , "+7");
        else if(phone.charAt(0) == '7')
            phone = phone.replaceFirst("7" , "+7");



        if (userRepository.findByPhone(phone) == null){
            sendMessage("Пользователь не зарегестрирован в данном боте!\nОтправьте зарегистрированного пользователя!");
        }
        else {
            long newSpecChatId     = userRepository.findByPhone(phone).getChatId();
            if (specialistRepository.findById(newSpecChatId) != null) {
                sendMessage("Пользователь уже является специалистом");
            } else {
                User user       = userRepository.findByChatId(newSpecChatId);
                specialistRepository.save(new Specialist().setChatId(newSpecChatId).setFullName(String.format("%s %s %s", user.getUserName(), user.getPhone(), DateUtil.getDbMmYyyyHhMmSs(new Date()))));
                User userAdmin  = userRepository.findByChatId(chatId);
                log.info("{} added new spec - {} ", getInfoByUser(userAdmin), getInfoByUser(user));
                sendEditorAdmin();
            }
        }
    }

    private String  getInfoByUser(User user) { return String.format("%s %s %s", user.getFullName(), user.getPhone(), user.getChatId()); }

    private void    sendEditorAdmin()   throws TelegramApiException {
        deleteMessage(updateMessageId);
        try {
            getText(EXIT);
            message = sendMessage(String.format(getText(1159), text.toString()));
        } catch (TelegramApiException e) {
            getText(COMEBACK);
            message = sendMessage(String.format(getText(1159), text.toString()));
        }
        toDeleteMessage(message);
    }

    private List<String> getIdsSpecs(List<Specialist> specialists) {
        List<String> ids = new ArrayList<>();

        for (Specialist specialist : specialists){
            ids.add(String.valueOf(specialist.getId()));
        }
        return ids;
    }

    private List<String> getNamesSpesc(List<Specialist> specialists) {
        List<String> names = new ArrayList<>();

        for (Specialist specialist : specialists){
            names.add("№" + specialist.getId()+" "+specialist.getFullName());
        }
        return names;
    }

    private void    getText(boolean withLink) {
        text        = new StringBuilder();
            specialistRepository.findAll().forEach(specialist -> {
            allAdmins.add(specialist.getChatId());
        });
//        allAdmins   = specialistDao.getAll();
////        allAdmins   = adminDao.getAll();
        int count   = 0;
        for (Long admin : allAdmins) {
            try {
                User user = userRepository.findByChatId(admin);
                if (allAdmins.size() == 1) {
                    if (withLink) {
                        text.append(getLinkForUser(user.getChatId(), user.getUserName())).append(space).append(next);
                    } else {
                        text.append(getInfoByUser(user)).append(space).append(next);
                    }
                    text.append("Должен быть минимум 1 специалист.").append(next);
                } else {
                    if (withLink) {
                        text.append(delete).append(count++).append(deleteIcon).append(" - ").append(showIcon).append(getLinkForUser(user.getChatId(), user.getUserName())).append(space).append(next);
                    } else {
                        text.append(delete).append(count++).append(deleteIcon).append(" - ").append(getInfoByUser(user)).append(space).append(next);
                    }
                }
                count++;
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    private void sendWrongData() throws TelegramApiException {
        sendMessage(1002);
    }
    private void sendWrongDataWithEditKeyboard() throws TelegramApiException {
        sendMessageWithKeyboard(getText(1002), 11);
    }

    private void deleteMess(){
        deleteMessage(updateMessageId);
        deleteMessage(deleteMessageId);
        deleteMessage(secondDeleteMessageId);
    }
}
