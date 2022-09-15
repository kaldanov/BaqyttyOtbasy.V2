package com.telegrambot.command.impl;

import com.telegrambot.command.Command;
import com.telegrambot.entity.Button;
import com.telegrambot.entity.enums.WaitingType;
import com.telegrambot.enums.Language;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

public class id052_EditMenuButtons extends Command {
    private int inlineMessId;
    private int wrongMessId;
    private int infoMessId;
    private int notFoundMess;
    private Button currentButton;
    private Language currentLang = getLanguage();
    private List<Button> searchResultButtons;

    @Override
    public boolean execute() throws TelegramApiException {
//        if (!isRegistered()) {
//            sendMessageWithKeyboard(getText(7), 4);
//            return EXIT;
//        }
        if (!isAdmin()) {
            sendMessage(10);
            return EXIT;
        }
        switch (waitingType) {
            case START:
                deleteUpdateMess();
                infoMessId = sendMessage(105);
                waitingType = WaitingType.SEARCH_BUTTON;
                return COMEBACK;
            case SEARCH_BUTTON:
                deleteUpdateMess();
                if (hasMessageText()) {
                    currentLang = getLanguage();
                    searchResultButtons = buttonRepository.findAllByNameContainingAndLangIdOrderById(updateMessageText, currentLang.getId());
                    if (searchResultButtons.size() != 0) {
                        deleteMessage(notFoundMess);
                        deleteMessage(infoMessId);
                        inlineMessId = sendMessage(getInfoButtons(searchResultButtons));
                        waitingType = WaitingType.CHOOSE_OPTION;
                    } else {
                        sendNotFound();
                    }
                } else {
                    deleteUpdateMess();
                    sendWrongData();
                }
                return COMEBACK;
            case CHOOSE_OPTION:
                deleteUpdateMess();
                if (updateMessageText.contains("/editName")) { //edit name
                    currentButton = buttonRepository.findByIdAndLangId(getLong(updateMessageText.substring(9)), currentLang.getId());
                    if (currentButton == null) {
                        sendWrongData();
                        return COMEBACK;
                    }
                    deleteMessage(inlineMessId);
                    inlineMessId = sendMessage(getInfoForEdit(currentButton));
                    waitingType = WaitingType.SET_TEXT;
                } else if (updateMessageText.contains("/back")) { // back
                    deleteMessage(infoMessId);
                    deleteMessage(inlineMessId);
                    infoMessId = sendMessage(105);
                    waitingType = WaitingType.SEARCH_BUTTON;
                } else if (updateMessageText.contains("/swapLanguage")) { //swap lang
                    if (currentLang.getId() == 1)
                        currentLang = Language.kz;
                    else
                        currentLang = Language.ru;
                    List<Button> newSearchRes = new ArrayList<>();
                    for (Button button : searchResultButtons) {
                        newSearchRes.add(buttonRepository.findByIdAndLangId(button.getId(), currentLang.getId()));
                    }
                    searchResultButtons = newSearchRes;
                    if (currentButton != null)
                        currentButton = buttonRepository.findByIdAndLangId(currentButton.getId(), currentLang.getId());
                    newSearchRes = null;
                    editMessage(getInfoButtons(searchResultButtons), inlineMessId);
                }
                return COMEBACK;
            case SET_TEXT:
                deleteUpdateMess();
                if (hasMessageText() && updateMessageText.length() < 100) {
                    if (updateMessageText.equals("/cancel")) {
                        deleteMessage(infoMessId);
                        deleteMessage(inlineMessId);
                        inlineMessId = sendMessage(getInfoButtons(searchResultButtons));
                        waitingType = WaitingType.CHOOSE_OPTION;
                        return COMEBACK;
                    } else if (buttonRepository.findByNameAndLangId(updateMessageText, 1) != null || buttonRepository.findByNameAndLangId(updateMessageText, 2) != null || updateMessageText.equals("/swapLanguage") || updateMessageText.equals("/back") || updateMessageText.contains("/editName")) {
                        deleteMessage(infoMessId);
                        infoMessId = sendMessage(106);
                        return COMEBACK;
                    } else {
                        deleteWrongMess();
                        buttonRepository.update(updateMessageText, currentButton.getId(), currentLang.getId());
                        deleteMessage(inlineMessId);
                        deleteMessage(infoMessId);

                        searchResultButtons =  updateButtons(searchResultButtons);
//                        searchResultButtons = buttonRepository.findAllByNameContainingAndLangIdOrderById(currentSearchValue, currentLang.getId());
                        currentButton = buttonRepository.findByIdAndLangId(currentButton.getId(), currentLang.getId());
                        inlineMessId = sendMessage(getInfoButtons(searchResultButtons));
                        waitingType = WaitingType.CHOOSE_OPTION;
                    }
                } else {
                    sendWrongData();
                }
                return COMEBACK;
        }
        return EXIT;
    }

    private List<Button> updateButtons(List<Button> searchResultButtons) {
        List<Button> newSearchRes = new ArrayList<>();
        for (Button button : searchResultButtons) {
            newSearchRes.add(buttonRepository.findByIdAndLangId(button.getId(), currentLang.getId()));
        }
        return newSearchRes;
    }

    private String getInfoForEdit(Button currentButton) {
        return getText(107) + currentButton.getName() + next +
                getText(108);
    }

    private String getInfoButtons(List<Button> searchResultButtons) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Button button : searchResultButtons) {
            stringBuilder.append(button.getName()).append(" \uD83D\uDD8A /editName").append(button.getId()).append(next).append(next);
        }
        return String.format(getText(110), stringBuilder.toString(), currentLang.name());
    }

    private void sendNotFound() throws TelegramApiException {
        deleteMessage(updateMessageId);
        deleteMessage(notFoundMess);
        notFoundMess = sendMessage(109, chatId);
    }
    private Long getLong(String updateMessageText) {
        try {
            return Long.parseLong(updateMessageText);
        } catch (Exception e) {
            return -1L;
        }
    }

    private void deleteUpdateMess() {
        deleteMessage(updateMessageId);
    }

    private void deleteWrongMess() {
        deleteMessage(wrongMessId);
    }

    private void sendWrongData() throws TelegramApiException {
        deleteMessage(updateMessageId);
        deleteMessage(wrongMessId);
        wrongMessId = sendMessage(1002, chatId);
    }
}