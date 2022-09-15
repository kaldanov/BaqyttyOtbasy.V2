package com.telegrambot.command.impl;

import com.telegrambot.command.Command;
import com.telegrambot.entity.custom.*;
import com.telegrambot.entity.enums.WaitingType;
import com.telegrambot.util.ButtonsLeaf;
import com.telegrambot.util.Const;
import com.telegrambot.util.DateKeyboard;
import com.telegrambot.util.DateUtil;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Component
public class id032_SpecialistInfo extends Command {

    private DateKeyboard dateKeyboard;
    private int deleteMessageId;
    private int secondDeleteMessageId;


    private List<String> userNameList = new ArrayList<>();
    private ButtonsLeaf buttonsLeaf;
    private ButtonsLeaf buttonsLeafDirections;
    List<Service> myServices;

    private long currentServiceId;
    private Registration_Service currentRegistration;

    List<Registration_Service> registration_services;

    private Service currentServiceKaz;
    private Service currentServiceRus;

    private List<Direction> directionsForSelect;

    private long currentDirId;


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

                if (isButton(86)) { // 86 - мои услуги
                    List<Specialist> specialists = specialistRepository.findAllByChatIdOrderById(chatId);

                    List<Services_Spec> services_specs = getServices_spec(specialists);

                    myServices = getServicesOfSpec(services_specs);

                    buttonsLeaf = new ButtonsLeaf(getNamesServices(myServices));

                    deleteMessageId = sendMessageWithKeyboard(getText(46), buttonsLeaf.getListButtonWhereIdIsData(getIdsServices(myServices)));
                    waitingType = WaitingType.CHOOSE_SERVICE;
                }

                return COMEBACK;

            case CHOOSE_SERVICE:
                deleteMess();

                if (hasCallbackQuery()) {
                    currentServiceId = Long.parseLong(updateMessageText);

                    currentServiceKaz = serviceRepository.findById2AndLangId(currentServiceId, 2);
                    currentServiceRus = serviceRepository.findById2AndLangId(currentServiceId, 1);

                    if (currentServiceRus.getStatusId() == 1 || currentServiceRus.getStatusId() == 2) {

                        registration_services = getRegistrations(currentServiceId);

                        if (registration_services == null || registration_services.size() == 0) {
                            deleteMessageId = sendMessage(Const.SERVICE_LIST_EMPTY_MESSAGE);
                            return EXIT;
                        } else {
                            if (currentServiceRus.getStatusId() == 2) {
                                registration_services = removeExcess(registration_services);
                                for (Registration_Service registration : registration_services) {
                                    if (registration.getParentIIN() == null) {
                                            userNameList.add(getNameLastName(recipientRepository.findByIin(registration.getIin()).getFullName()) + " | " + DateUtil.getDayDate(registration.getDateReg()) + " ( " + comesCourseRepository.findAllByRegistrationServiceIdOrderById(registration.getId()).size() + " " + getText(100) + ")");
                                    }
                                    else{
                                            userNameList.add(getNameLastName(recipientRepository.findByIin(registration.getParentIIN()).getFullName()) + " | " + DateUtil.getDayDate(registration.getDateReg()) + " ( " + comesCourseRepository.findAllByRegistrationServiceIdOrderById(registration.getId()).size() + " " + getText(100) + ")");
                                    }
                                }

                            }
                            else {
                                for (Registration_Service registration : registration_services) {
                                    if (registration.getParentIIN() == null) {
                                        userNameList.add(getNameLastName(recipientRepository.findByIin(registration.getIin()).getFullName()) + " | " + DateUtil.getDayDate(registration.getDateReg()));
                                    }
                                    else{
                                        userNameList.add(getNameLastName(recipientRepository.findByIin(registration.getParentIIN()).getFullName()) + " | " + DateUtil.getDayDate(registration.getDateReg()));
                                    }
                                }
                            }

                            buttonsLeaf = new ButtonsLeaf(userNameList, 10);
                            deleteMessageId = toDeleteKeyboard(sendMessageWithKeyboard(Const.SERVICE_LIST_MESSAGE, buttonsLeaf.getListButtonWhereIdIsData(getIdsRegistr(registration_services))));
                            waitingType = WaitingType.SET_SERVICE;
                        }

                    }
                    if (currentServiceRus.getStatusId() == 3) {
                        deleteMessageId = sendMessageWithKeyboard(getText(24), 49);
                        waitingType = WaitingType.CHOOSE_LIST_OR_ADD_REPORT;
                    }
                } else {
                    sendMessageWithKeyboard(getText(1002) + "\n" + getText(82), 28);
                    waitingType = WaitingType.START;
                }
                return COMEBACK;


            case CHOOSE_LIST_OR_ADD_REPORT:
                deleteMess();
                userNameList = new ArrayList<>();
                if (hasCallbackQuery()) {
                    if (isButton(98)) { // list registrations
                        registration_services = getRegistrations(currentServiceId);

                        if (registration_services == null || registration_services.size() == 0) {
                            deleteMessageId = sendMessage(Const.SERVICE_LIST_EMPTY_MESSAGE);
                            return EXIT;
                        } else {

                            if (currentServiceRus.getStatusId() == 2) {
                                registration_services = removeExcess(registration_services);
                                for (Registration_Service registration : registration_services) {
                                    if (registration.getParentIIN() == null) {
                                        userNameList.add(getNameLastName(recipientRepository.findByIin(registration.getIin()).getFullName()) + " | " + DateUtil.getDayDate(registration.getDateReg()) + " ( " + comesCourseRepository.findAllByRegistrationServiceIdOrderById(registration.getId()).size() + " " + getText(100) + ")");
                                    }
                                    else{
                                        userNameList.add(getNameLastName(recipientRepository.findByIin(registration.getParentIIN()).getFullName()) + " | " + DateUtil.getDayDate(registration.getDateReg()) + " ( " + comesCourseRepository.findAllByRegistrationServiceIdOrderById(registration.getId()).size() + " " + getText(100) + ")");
                                    }
                                }
                            } else {
                                for (Registration_Service registration : registration_services) {
                                    if (registration.getParentIIN() == null) {
                                        userNameList.add(getNameLastName(recipientRepository.findByIin(registration.getIin()).getFullName()) + " | " + DateUtil.getDayDate(registration.getDateReg()));
                                    }
                                    else{
                                        userNameList.add(getNameLastName(recipientRepository.findByIin(registration.getParentIIN()).getFullName()) + " | " + DateUtil.getDayDate(registration.getDateReg()));
                                    }
                                }
                            }

                            buttonsLeaf = new ButtonsLeaf(userNameList, 10);
                            deleteMessageId = toDeleteKeyboard(sendMessageWithKeyboard(Const.SERVICE_LIST_MESSAGE, buttonsLeaf.getListButtonWhereIdIsData(getIdsRegistr(registration_services))));
                            waitingType = WaitingType.SET_SERVICE;
                        }
                    }
                    if (isButton(99)) { // add report
                        sendMessageWithKeyboard(getText(79), 47); // 47 - cancel
                        waitingType = WaitingType.SET_TEXT_REPORT_OF_SERVICE;
                    }
                } else {
                    sendMessageWithKeyboard(getText(1002) + "\n" + getText(82), 28);
                    waitingType = WaitingType.START;
                }
                return COMEBACK;

            case SET_TEXT_REPORT_OF_SERVICE:
                deleteMess();

                if (isButton(96)) { // to do cancel
                    sendMessageWithKeyboard(getText(40), 28);
                    deleteMessageId = sendMessageWithKeyboard(getText(46), buttonsLeaf.getListButtonWhereIdIsData(getIdsServices(myServices)));

//                    deleteMessageId = sendMessageWithKeyboard(getText(24),49 );
                    waitingType = WaitingType.CHOOSE_SERVICE;
                } else {
                    if (hasDocument()) {
                        ReportToService reportToService = new ReportToService(update.getMessage().getDocument().getFileId());
                        reportToService.setServiceId(currentServiceId);
                        reportToService.setSenderChatId(chatId);
                        reportToService.setSendDate(new Date());
                        reportServiceRepository.save(reportToService);
                        sendMessageWithKeyboard(getText(80), 28);
                    } else {
                        sendMessage(1002);
                        sendMessageWithKeyboard(getText(79), 47); // 47 - cancel

                        waitingType = WaitingType.SET_TEXT_REPORT_OF_SERVICE;
                    }
                }

                return COMEBACK;

            case SET_SERVICE:
                if (hasCallbackQuery()) {
                    if (buttonsLeaf.isNext(updateMessageText)) {
                        editMessageWithKeyboard(getText(Const.SERVICE_LIST_MESSAGE), deleteMessageId, (InlineKeyboardMarkup) buttonsLeaf.getListButtonWhereIdIsData(getIdsRegistr(registration_services)));
//                        deleteMessageId = toDeleteKeyboard(sendMessageWithKeyboard(Const.SERVICE_LIST_MESSAGE, buttonsLeaf.getListButtonWhereIdIsData(getIdsRegistr(registration_services))));
                        return COMEBACK;
                    }
                    deleteMess();

                    System.out.println(updateMessageText);
                    currentRegistration = registrationServiceRepository.findById(Long.parseLong(updateMessageText));

                    StringBuilder message = new StringBuilder();
                    message.append(getText(95)).append(currentRegistration.getId()).append(next);

                    if(currentRegistration.getParentIIN() != null){
                        message.append(getText(113)).append(currentRegistration.getIin()).append(next);
                        message.append(getText(96)).append(recipientRepository.findByIin(currentRegistration.getParentIIN()).getFullName()).append(next);
                        message.append(getText(118)).append(currentRegistration.getParentIIN()).append(next);
                        message.append(getText(90)).append(recipientRepository.findByIin(currentRegistration.getParentIIN()).getPhoneNumber()).append(next);

                    }
                    else {
                        message.append(getText(96)).append(recipientRepository.findByIin(currentRegistration.getIin()).getFullName()).append(next);
                        message.append(getText(97)).append(currentRegistration.getIin()).append(next);
                        message.append(getText(90)).append(recipientRepository.findByIin(currentRegistration.getIin()).getPhoneNumber()).append(next);
                    }

                    message.append(getText(98)).append(DateUtil.getDateAndTime(currentRegistration.getDateReg())).append(next);
                    if (serviceRepository.findById2AndLangId(currentRegistration.getServiceId(), 1).getStatusId() == 2) {
                        message.append("<b>Статус : </b>").append(getCountCome(currentRegistration) > 0 ? getText(99) + getCountCome(currentRegistration) + getText(100) : getText(101)).append(next);
                    } else {
                        message.append("<b>Статус : </b>").append(currentRegistration.isFinish() ? getText(99) : getText(101)).append(next);
                    }
                    message.append(getText(102)).append(currentRegistration.getInviteDate() != null ? getText(103) + DateUtil.getDayDate(currentRegistration.getInviteDate()) + space + currentRegistration.getInviteTime() : getText(104));


                    if (currentServiceRus.getStatusId() == 1) {
                        if (!currentRegistration.isFinish()) {
                            deleteMessageId = toDeleteKeyboard(sendMessageWithKeyboard(message.toString(), 26));
                        } else {
                            deleteMessageId = toDeleteKeyboard(sendMessageWithKeyboard(message.toString(), 27));
                        }
                    }

                    if (currentServiceRus.getStatusId() == 2) {
                        if (!currentRegistration.isFinish()) {
                            deleteMessageId = toDeleteKeyboard(sendMessageWithKeyboard(message.toString(), 29));
                        } else {
                            deleteMessageId = toDeleteKeyboard(sendMessageWithKeyboard(message.toString(), 27));
                        }
                    }
                    if (currentServiceRus.getStatusId() == 3) {
                        // nothing
                        if (!currentRegistration.isFinish()) {
                            deleteMessageId = toDeleteKeyboard(sendMessageWithKeyboard(message.toString(), 26));
                        } else {
                            deleteMessageId = toDeleteKeyboard(sendMessageWithKeyboard(message.toString(), 27));
                        }
                    }

                    waitingType = WaitingType.CHOOSE_OPTION;
                } else {
                    deleteMess();
                    sendMessageWithKeyboard(getText(1002) + "\n" + getText(82), 28);
                    waitingType = WaitingType.START;
                }
                return COMEBACK;
            case CHOOSE_OPTION:
                deleteMess();

                if (hasCallbackQuery()) {
                    if (isButton(Const.BACK_BUTTON)) {
                        deleteMessageId = toDeleteKeyboard(sendMessageWithKeyboard(Const.SERVICE_LIST_MESSAGE, buttonsLeaf.getListButtonWhereIdIsData(getIdsRegistr(registration_services))));
                        waitingType = WaitingType.SET_SERVICE;
                    }
                    if (isButton(Const.INVITE_BUTTON)) { // пригласить
                        dateKeyboard = new DateKeyboard();
                        sendDate();
                        waitingType = WaitingType.INVITE_DATE;
                    }
                    if (isButton(Const.FINISH_COURSE_BUTTON)) { // завершить курс
                        currentRegistration.setFinish(true);
                        registrationServiceRepository.save(currentRegistration);
                        deleteMessageId = sendMessage(getText(Const.DONE_JOIN_MESSAGE));
                        return doneFrom();
                    }
                    if (isButton(66)) { // 66 - пришел


                        if (currentRegistration != null) {

                            if (currentServiceRus.getStatusId() == 2) {
                                currentRegistration.setFinish(false);
                                registrationServiceRepository.save(currentRegistration);

                                deleteMessageId = sendMessage(getText(Const.DONE_JOIN_MESSAGE));

                                ComesCourse comesCourse = new ComesCourse(currentRegistration.getId(), new Date());
                                comesCourseRepository.save(comesCourse);
                                return doneFromCourse();
                            }

                            if (currentServiceRus.getStatusId() == 1) {

                                directionsForSelect = directionRepository.findAllByServiceIdOrderById(currentServiceId);

                                if (directionsForSelect.size() == 0) {
                                    sendMessageWithKeyboard(getText(92), 28);
                                    return EXIT;
                                }
                                buttonsLeafDirections = new ButtonsLeaf(getNamesDirections(directionsForSelect));

                                deleteMessageId = sendMessageWithKeyboard(getText(75), buttonsLeafDirections.getListButtonWhereIdIsData(idsDirections(directionsForSelect)));
                                waitingType = WaitingType.CHOOSE_DIRECTION;

                            }
                            if (currentServiceRus.getStatusId() == 3) {

                                currentRegistration.setFinish(true);
                                currentRegistration.setDateReg(new Date());
                                registrationServiceRepository.save(currentRegistration);

                                deleteMessageId = sendMessage(getText(Const.DONE_JOIN_MESSAGE));
                                return doneFrom();

                            }

//                        return EXIT;
                        }
                    }
                    if (isButton(111)) {

                        if (currentRegistration != null) {
                            if (currentServiceRus.getStatusId() == 1) {
                                registrationServiceRepository.delete(currentRegistration);
                                deleteMessageId = sendMessage(getText(Const.DONE_JOIN_MESSAGE));
                                return doneFrom();
                            }
                            if (currentServiceRus.getStatusId() == 3) {
                                registrationServiceRepository.delete(currentRegistration);
                                deleteMessageId = sendMessage(getText(Const.DONE_JOIN_MESSAGE));
                                return doneFrom();
                            }
                            return EXIT;
                        }
                    }
                } else {
                    sendMessageWithKeyboard(getText(1002) + "\n" + getText(82), 28);
                    waitingType = WaitingType.START;
                }
                return COMEBACK;

            case CHOOSE_DIRECTION:
                deleteMess();
                if (hasCallbackQuery()) {

                    currentDirId = Long.parseLong(updateMessageText);

                    if (currentDirId == -1) {
//                        currentRegistration = registrationServiceRepository.findById(Long.parseLong(updateMessageText));

                        StringBuilder message = new StringBuilder();
                        message.append(getText(95)).append(currentRegistration.getId()).append(next);

                        if(currentRegistration.getParentIIN() != null){
                            message.append(getText(113)).append(currentRegistration.getIin()).append(next);
                            message.append(getText(96)).append(recipientRepository.findByIin(currentRegistration.getParentIIN()).getFullName()).append(next);
                            message.append(getText(118)).append(currentRegistration.getParentIIN()).append(next);
                            message.append(getText(90)).append(recipientRepository.findByIin(currentRegistration.getParentIIN()).getPhoneNumber()).append(next);

                        }
                        else {
                            message.append(getText(96)).append(recipientRepository.findByIin(currentRegistration.getIin()).getFullName()).append(next);
                            message.append(getText(97)).append(currentRegistration.getIin()).append(next);
                            message.append(getText(90)).append(recipientRepository.findByIin(currentRegistration.getIin()).getPhoneNumber()).append(next);
                        }
//                        message.append(getText(96)).append(recipientRepository.findByIin(currentRegistration.getIin()).getFullName()).append(next);
//                        message.append(getText(97)).append(currentRegistration.getIin()).append(next);
//                        message.append(getText(90)).append(recipientRepository.findByIin(currentRegistration.getIin()).getPhoneNumber()).append(next);
                        message.append(getText(98)).append(DateUtil.getDateAndTime(currentRegistration.getDateReg())).append(next);
                        if (serviceRepository.findById2AndLangId(currentRegistration.getServiceId(), 1).getStatusId() == 2) {
                            message.append("<b>Статус : </b>").append(getCountCome(currentRegistration) > 0 ? getText(99) + getCountCome(currentRegistration) + getText(100) : getText(101)).append(next);
                        } else {
                            message.append("<b>Статус : </b>").append(currentRegistration.isFinish() ? getText(99) : getText(101)).append(next);
                        }
                        message.append(getText(102)).append(currentRegistration.getInviteDate() != null ? getText(103) + DateUtil.getDayDate(currentRegistration.getInviteDate()) + space + currentRegistration.getInviteTime() : getText(104));


                        if (currentServiceRus.getStatusId() == 1) {
                            if (!currentRegistration.isFinish()) {
                                deleteMessageId = toDeleteKeyboard(sendMessageWithKeyboard(message.toString(), 26));
                            } else {
                                deleteMessageId = toDeleteKeyboard(sendMessageWithKeyboard(message.toString(), 27));
                            }
                        }

                        if (currentServiceRus.getStatusId() == 2) {
                            if (!currentRegistration.isFinish()) {
                                deleteMessageId = toDeleteKeyboard(sendMessageWithKeyboard(message.toString(), 29));
                            } else {
                                deleteMessageId = toDeleteKeyboard(sendMessageWithKeyboard(message.toString(), 27));
                            }
                        }
                        if (currentServiceRus.getStatusId() == 3) {
                            // nothing
                            if (!currentRegistration.isFinish()) {
                                deleteMessageId = toDeleteKeyboard(sendMessageWithKeyboard(message.toString(), 26));
                            } else {
                                deleteMessageId = toDeleteKeyboard(sendMessageWithKeyboard(message.toString(), 27));
                            }
                        }

                        waitingType = WaitingType.CHOOSE_OPTION;
                    } else if (directionRepository.findById(currentDirId) != null) {

                        DirectionRegistration directionRegistration = new DirectionRegistration(currentRegistration.getId(), currentDirId);
                        directionRegistrationRepository.save(directionRegistration);

                        directionsForSelect.remove(directionRepository.findById(currentDirId));

                        buttonsLeafDirections = new ButtonsLeaf(getNamesDirectionsWithNext(directionsForSelect));

                        deleteMessageId = sendMessageWithKeyboard(getText(75), buttonsLeafDirections.getListButtonWhereIdIsData(idsDirectionsWithNext(directionsForSelect)));

                        waitingType = WaitingType.CHOOSE_DIRECTION_OR_NEXT;
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
                        currentRegistration.setFinish(true);
                        currentRegistration.setDateReg(new Date());
                        registrationServiceRepository.save(currentRegistration);

//                        deleteMessageId = sendMessage(Const.DONE_JOIN_MESSAGE);
                        deleteMessageId = sendMessage(getText(Const.DONE_JOIN_MESSAGE));
                        return doneFrom();

                    } else if (directionRepository.findById(currentDirId) != null) {

                        DirectionRegistration directionRegistration = new DirectionRegistration(currentRegistration.getId(), currentDirId);
                        directionRegistrationRepository.save(directionRegistration);

                        currentRegistration.setDateReg(new Date());
                        registrationServiceRepository.save(currentRegistration);


                        directionsForSelect.remove(directionRepository.findById(currentDirId));

                        if (directionsForSelect.size() == 0) {
                            currentRegistration.setFinish(true);
                            registrationServiceRepository.save(currentRegistration);

                            deleteMessageId = sendMessage(getText(Const.DONE_JOIN_MESSAGE));
                            return doneFrom();
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

            case INVITE_DATE:
                deleteMess();
                if (hasCallbackQuery()) {
                    if (dateKeyboard.isNext(updateMessageText)) {
                        sendDate();
                    } else {
                        currentRegistration.setInviteDate(dateKeyboard.getDateDate(updateMessageText));
                        sendTime();
                        waitingType = WaitingType.INVITE_TIME;
                    }
                } else {
                    sendMessageWithKeyboard(getText(1002) + "\n" + getText(82), 28);
                    waitingType = WaitingType.START;
                }
                return COMEBACK;
            case INVITE_TIME:
                deleteMess();
                if (hasMessageText()) {
                    currentRegistration.setInviteTime(updateMessageText);

                    registrationServiceRepository.save(currentRegistration);

                    sendMessageToUser();
                    deleteMessageId = sendMessage(Const.MEETING_DONE_MESSAGE);
                    return EXIT;
                } else {
                    sendMessageWithKeyboard(getText(1002) + "\n" + getText(82), 28);
                    waitingType = WaitingType.START;
                }
                return COMEBACK;
        }
        return EXIT;
    }

    private boolean doneFromCourse() throws TelegramApiException {

        userNameList = new ArrayList<>();

        registration_services = removeExcess(registration_services);

        for (Registration_Service registration : registration_services) {
            if (registration.getParentIIN() == null) {
                userNameList.add(getNameLastName(recipientRepository.findByIin(registration.getIin()).getFullName()) + " | " + DateUtil.getDayDate(registration.getDateReg()) + " ( " + comesCourseRepository.findAllByRegistrationServiceIdOrderById(registration.getId()).size() + " " + getText(100) + ")");
            }
            else{
                userNameList.add(getNameLastName(recipientRepository.findByIin(registration.getParentIIN()).getFullName()) + " | " + DateUtil.getDayDate(registration.getDateReg()) + " ( " + comesCourseRepository.findAllByRegistrationServiceIdOrderById(registration.getId()).size() + " " + getText(100) + ")");
            }
        }
        buttonsLeaf = new ButtonsLeaf(userNameList, 10, buttonsLeaf.getPage());

        deleteMessageId = toDeleteKeyboard(sendMessageWithKeyboard(Const.SERVICE_LIST_MESSAGE, buttonsLeaf.getListButtonWhereIdIsData(getIdsRegistr(registration_services))));
        waitingType = WaitingType.SET_SERVICE;
        return COMEBACK;
    }

    private List<Registration_Service> removeExcess(List<Registration_Service> registration_services) {

        boolean toList;

        List<Registration_Service> registrationServiceList = new ArrayList<>();
        SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd");

        for (Registration_Service registration : registration_services) {
            toList = true;
            List<ComesCourse> comesCourseList = comesCourseRepository.findAllByRegistrationServiceIdOrderById(registration.getId());
            for (ComesCourse comesCourse : comesCourseList) {
                if (formater.format(comesCourse.getActionDate()).equals(formater.format(new Date()))) {
                    toList = false;
                    break;
                }
            }
            if (toList) {
                registrationServiceList.add(registration);
            }
        }

        return registrationServiceList;

    }

    private String getNameLastName(String fullName) {
        String[] arr = fullName.split(" ");
        StringBuilder nn = new StringBuilder();
        int j = 0;
        for (String s : arr) {
            if (!s.equals("") && !s.equals(" ")) {
                nn.append(" ").append(s);
                j++;
                if (j == 2)
                    break;
            }
        }

        return nn.toString();
    }

    private boolean doneFrom() throws TelegramApiException {

        if (currentServiceRus.getStatusId() == 1 || currentServiceRus.getStatusId() == 2) {

            registration_services = getRegistrations(currentServiceId);
            userNameList = new ArrayList<>();
            if (registration_services == null || registration_services.size() == 0) {
                deleteMessageId = sendMessage(Const.SERVICE_LIST_EMPTY_MESSAGE);
                return EXIT;
            } else {
                if (currentServiceRus.getStatusId() == 2) {
                    registration_services = removeExcess(registration_services);
                    for (Registration_Service registration : registration_services) {
                        if (registration.getParentIIN() == null) {
                            userNameList.add(getNameLastName(recipientRepository.findByIin(registration.getIin()).getFullName()) + " | " + DateUtil.getDayDate(registration.getDateReg()) + " ( " + comesCourseRepository.findAllByRegistrationServiceIdOrderById(registration.getId()).size() + " " + getText(100) + ")");
                        }
                        else{
                            userNameList.add(getNameLastName(recipientRepository.findByIin(registration.getParentIIN()).getFullName()) + " | " + DateUtil.getDayDate(registration.getDateReg()) + " ( " + comesCourseRepository.findAllByRegistrationServiceIdOrderById(registration.getId()).size() + " " + getText(100) + ")");
                        }
                    }

                } else {
                    for (Registration_Service registration : registration_services) {
                        if (registration.getParentIIN() == null) {
                            userNameList.add(getNameLastName(recipientRepository.findByIin(registration.getIin()).getFullName()) + " | " + DateUtil.getDayDate(registration.getDateReg()));
                        }
                        else{
                            userNameList.add(getNameLastName(recipientRepository.findByIin(registration.getParentIIN()).getFullName()) + " | " + DateUtil.getDayDate(registration.getDateReg()));
                        }
                    }
                }

                buttonsLeaf = new ButtonsLeaf(userNameList, 10);
                deleteMessageId = toDeleteKeyboard(sendMessageWithKeyboard(Const.SERVICE_LIST_MESSAGE, buttonsLeaf.getListButtonWhereIdIsData(getIdsRegistr(registration_services))));
                waitingType = WaitingType.SET_SERVICE;
            }

        }
        if (currentServiceRus.getStatusId() == 3) {
            deleteMessageId = sendMessageWithKeyboard(getText(24), 49);
            waitingType = WaitingType.CHOOSE_LIST_OR_ADD_REPORT;
        }
        return COMEBACK;
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

    private List<Registration_Service> getRegistrations(long currentServiceId) {

        List<Specialist> specialists = specialistRepository.findAllByChatIdOrderById(chatId);

        List<Services_Spec> services_specs = getServices_spec(specialists);

        for (Services_Spec services_spec : services_specs) {
            if (services_spec.getServiceId() == currentServiceId) {
                return trimRegs(registrationServiceRepository.findAllByServiceIdAndSpecIdAndIsFinishOrderById(currentServiceId, services_spec.getSpecId(), false));
            }
        }

        return null;

    }

    private List<Registration_Service> trimRegs(List<Registration_Service> allByServiceIdAndSpecIdAndIsFinishOrderById) {
        List<Registration_Service> trimmed = new ArrayList<>();

        for (Registration_Service reg : allByServiceIdAndSpecIdAndIsFinishOrderById){
            if (reg.getParentIIN() == null && recipientRepository.findByIin(reg.getIin()) != null)
                trimmed.add(reg);
            else if (recipientRepository.findByIin(reg.getParentIIN()) != null)
                trimmed.add(reg);
        }
        return trimmed;
    }

    private List<Services_Spec> getServices_spec(List<Specialist> specialists) {
        List<Services_Spec> services_specs = new ArrayList<>();
        for (Specialist specialist : specialists) {
            services_specs.addAll(servicesSpecsRepository.findAllBySpecIdOrderById(specialist.getId()));
        }
        return services_specs;
    }

    private int getCountCome(Registration_Service currentRegistration) {
        return comesCourseRepository.findAllByRegistrationServiceIdOrderById(currentRegistration.getId()).size();
    }

    private List<String> getIdsRegistr(List<Registration_Service> registration_services) {
        List<String> ids = new ArrayList<>();

        for (Registration_Service service : registration_services) {
                ids.add(String.valueOf(service.getId()));
        }
        return ids;
    }

    private List<String> getIdsServices(List<Service> services) {
        List<String> ids = new ArrayList<>();

        for (Service service : services) {
            ids.add(String.valueOf(service.getId2()));
        }
        return ids;

    }

    private List<String> getNamesServices(List<Service> services) {
        List<String> names = new ArrayList<>();
        for (Service service : services) {
            names.add(service.getName());
        }
        return names;
    }

    private List<Service> getServicesOfSpec(List<Services_Spec> services_specs) {
        List<Service> services = new ArrayList<>();

        for (Services_Spec spec : services_specs) {
            try {
                services.add(serviceRepository.findById2AndLangId(spec.getServiceId(), getLanguage().getId()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return services;
    }


    private void sendStartDate() throws TelegramApiException {
        deleteMessageId = toDeleteKeyboard(sendMessageWithKeyboard(Const.CHOOSE_DATE_MESSAGE, dateKeyboard.getCalendarKeyboard()));
    }

    private void sendDate() throws TelegramApiException {
        toDeleteKeyboard(sendMessageWithKeyboard(Const.MEETING_DATE_MESSAGE, dateKeyboard.getCalendarKeyboard()));
    }

    private void sendEndDate() throws TelegramApiException {
        toDeleteKeyboard(sendMessageWithKeyboard(Const.SELECT_END_DATE_MESSAGE, dateKeyboard.getCalendarKeyboard()));
    }

    private void sendTime() throws TelegramApiException {
        sendMessage(Const.MEETING_TIME_MESSAGE);
    }

    private void sendMessageToUser() throws TelegramApiException {
        String returnMessage = "";
        if (currentRegistration.getParentIIN() == null) {
            returnMessage = String.format(getText(1163),
                    recipientRepository.findByIin(currentRegistration.getIin()).getFullName(),
                    recipientRepository.findByIin(currentRegistration.getIin()).getPhoneNumber(),
                    serviceRepository.findById2AndLangId(currentRegistration.getServiceId(),
                            getLanguage(currentRegistration.getUserChatId()).getId()).getName(),
                    DateUtil.getDayDate(currentRegistration.getInviteDate()),
                    currentRegistration.getInviteTime());
        }
        else {
            returnMessage = String.format(getText(1163),
                    recipientRepository.findByIin(currentRegistration.getParentIIN()).getFullName(),
                    recipientRepository.findByIin(currentRegistration.getParentIIN()).getPhoneNumber(),
                    serviceRepository.findById2AndLangId(currentRegistration.getServiceId(),
                            getLanguage(currentRegistration.getUserChatId()).getId()).getName(),
                    DateUtil.getDayDate(currentRegistration.getInviteDate()),
                    currentRegistration.getInviteTime());
        }

        sendMessage(returnMessage, currentRegistration.getUserChatId());
    }

    private void deleteMess() {
        deleteMessage(updateMessageId);
        deleteMessage(secondDeleteMessageId);
        deleteMessage(deleteMessageId);
    }


}