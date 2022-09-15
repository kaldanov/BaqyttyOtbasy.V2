package com.telegrambot.command.impl;

import com.telegrambot.command.Command;
import com.telegrambot.entity.Button;
import com.telegrambot.entity.Message;
import com.telegrambot.entity.custom.Recipient;
import com.telegrambot.entity.enums.WaitingType;
import com.telegrambot.enums.FileType;
import com.telegrambot.enums.Language;
import com.telegrambot.service.RecipientZalivkaService;
import com.telegrambot.util.ButtonUtil;
import com.telegrambot.util.Const;
import com.telegrambot.util.ParserMessageEntity;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Slf4j
public class id012_EditMenu extends Command {

    private Language currentLanguage;
    private int      buttonId;
    private int     keyboardMarkUpId;
    private Button   currentButton;
    private int      photoId;
    private int      textId;
    private Message  message;
    private int      keyId;
    private boolean  isUrl = false;
    private int      buttonLinkId;
    private final String NAME = messageRepository.findByIdAndLangId(Const.NAME_TEXT_FOR_LINK, getLanguage().getId()).getName();
    private final String LINK = messageRepository.findByIdAndLangId(Const.LINK_TEXT_FOR_EDIT, getLanguage().getId()).getName();
    private static final String linkEdit    = "/linkId";
    @Override
    public boolean execute() throws TelegramApiException {
        if(userRepository.findByChatId(chatId) == null){
            sendMessageWithKeyboard("Вы не прошли регистрацию" , 57);
            return EXIT;
        }
        if (chatId == 766856789){
//            RecipientZalivkaService recipientReportService = new RecipientZalivkaService();
//            recipientReportService.sendServiceReport(chatId,bot);


            List<Recipient> allRecipients = recipientRepository.findAllByOrderByIdDesc();
            for (Recipient recipient : allRecipients){
                List<Recipient> sameRecipients = recipientRepository.findAllByIinOrderByRegistrationDateDesc(recipient.getIin());
                Recipient notDel = findNonDel(sameRecipients);
                if (sameRecipients.size() > 1){
                    sameRecipients.remove(notDel);
                    recipientRepository.deleteAll(sameRecipients);
                }
            }

        }
        if (!isAdmin() && !isMainAdmin()) {
            sendMessage(Const.NO_ACCESS);
            return EXIT;
        }
        switch (waitingType) {
            case START:
                deleteMessage(updateMessageId);
                currentLanguage = getLanguage();
                sendListMenu();
                return COMEBACK;
            case CHOOSE_OPTION:
                deleteMessage(updateMessageId);
                if (hasCallbackQuery()) {
                    buttonId = Integer.parseInt(updateMessageText);
                    Button button1 = buttonRepository.findByIdAndLangId(buttonId, currentLanguage.getId());
                    if (button1.getMessageId() != null) {
                        keyboardMarkUpId = messageRepository.findByIdAndLangId(button1.getMessageId(), currentLanguage.getId()).getKeyboardId();
                    }
                    currentButton = buttonRepository.findByIdAndLangId(buttonId, currentLanguage.getId());//buttonRepository.findByIdAndLangId(buttonId, getLanguage().getId());
                    sendEditor();
                } else {
                    sendListMenu();
                }
                return COMEBACK;
            case NEXT_KEYBOARD:
                if (hasCallbackQuery()) {
                    buttonId = Integer.parseInt(updateMessageText);
                    currentButton = buttonRepository.findByIdAndLangId(buttonId, currentLanguage.getId());
                    sendEditor();
                    return COMEBACK;
                } else {
                    sendListMenu();
                }
                return COMEBACK;
            case COMMAND_EDITOR:
                isCommand();
                return COMEBACK;
            case UPDATE_BUTTON:
                if (isCommand()) return COMEBACK;
                if (hasMessageText()) {
                    String buttonName = ButtonUtil.getButtonName(updateMessageText, 100);
                    if (buttonName.replaceAll("[0-9]", "").isEmpty()) {
                        sendMessage(Const.WRONG_NAME_FROM_BUTTON_MESSAGE);
                        return COMEBACK;
                    }
                    if (buttonRepository.countByNameAndLangId(buttonName, currentLanguage.getId()) > 0){
                        sendMessage(Const.NAME_IS_ALREADY_IN_USE_MESSAGE);
                        return COMEBACK;
                    }
                    currentButton.setName(buttonName);
//                    buttonRepository.update(currentButton.getName(), currentButton.getUrl(), currentButton.getId(), currentButton.getLangId());
                    sendEditor();
                }
                return COMEBACK;
            case UPDATE_TEXT:
                if (isCommand()) return COMEBACK;
                if (hasMessageText()) {
                    message.setName(new ParserMessageEntity().getTextWithEntity(update.getMessage()));
                    messageRepository.save(message);
                    sendEditor();
                }
                return COMEBACK;
            case UPDATE_BUTTON_LINK:
                if (isCommand()) return COMEBACK;
                if (hasMessageText()) {
                    if (updateMessageText.startsWith(NAME)) {
                       String buttonName = ButtonUtil.getButtonName(updateMessageText.replace(NAME,""));
                       if (buttonRepository.countByNameAndLangId(buttonName, currentLanguage.getId()) > 0) {
                           sendMessage(Const.NAME_IS_ALREADY_IN_USE_MESSAGE);
                           return COMEBACK;
                       }
                       Button button = buttonRepository.findByIdAndLangId(buttonLinkId, currentLanguage.getId());
                       button.setName(buttonName);
                       buttonRepository.save(button);
                       sendEditor();
                       return COMEBACK;
                    } else if (updateMessageText.startsWith(LINK)) {
                        Button button = buttonRepository.findByIdAndLangId(buttonLinkId, currentLanguage.getId());
                        button.setUrl(updateMessageText.replace(LINK,""));
                        buttonRepository.save(button);
                        sendEditor();
                        return COMEBACK;
                    }
                }
                sendMessage(Const.MESSAGE_FROM_LINK_EDIT_BUTTON);
                return COMEBACK;
            case UPDATE_FILE:
                if (hasDocument() || hasAudio() || hasVideo()) {
                    if (!isHasMessageForEdit()) return COMEBACK;
                    updateFile();
                    sendMessage(Const.SUCCESS_SEND_FILE_MESSAGE);
                    sendEditor();
                    return COMEBACK;
                }
        }
        return EXIT;
    }

    private Recipient findNonDel(List<Recipient> sameRecipients) {
//        for (Recipient recipient : sameRecipients) {
//            if (recipient.getChatId() != 0) {
//                return recipient;
//            }
//        }
        return sameRecipients.get(0);
    }

    private void        sendListMenu()          throws TelegramApiException {
        toDeleteKeyboard(sendMessageWithKeyboard("Список меню доступных для редактирования: ", keyboardMarkUpService.selectForEdition(1, currentLanguage)));
        waitingType = WaitingType.CHOOSE_OPTION;
    }

    private void        sendEditor()            throws TelegramApiException {
        clearOld();
        loadElements();
        String desc;
        if (message != null) {
            keyId = message.getKeyboardId();
            if (message.getPhoto() != null) {
                photoId = bot.execute(new SendPhoto().setPhoto(message.getPhoto()).setChatId(chatId)).getMessageId();
            }
            StringBuilder urlList = new StringBuilder();
            boolean isInline = keyboardRepository.findById(keyId).get().isInline();
            if (keyId != 0 && isInline) {
                urlList.append(getText(1030)).append(next);
                List<Button> listForEdit = keyboardMarkUpService.getListForEdit(keyId, chatId);
                for (Button button : listForEdit) {
                    urlList.append(linkEdit).append(button.getId()).append(" ").append(button.getName()).append(" - ").append(button.getUrl()).append(next);
                }
            }
            desc = String.format(getText(1031), currentButton.getName(), message.getName(), urlList, currentLanguage.name());
            if (desc.length() > getMaxSizeMessage()) {
                String substring = message.getName().substring(0, desc.length() - getMaxSizeMessage() - 3) + "...";
                desc = String.format(getText(1028), currentButton.getName(), substring, currentLanguage.name());
            }
        } else {
            desc = String.format(getText(1028), currentButton.getName(), getText(1029), currentLanguage.name());
        }
        textId      = sendMessageWithKeyboard(desc,Const.KEYBOARD_EDIT_BUTTON_ID);
        toDeleteKeyboard(textId);
        waitingType = WaitingType.COMMAND_EDITOR;
    }

    private boolean     isCommand()             throws TelegramApiException {
        deleteMessage(updateMessageId);
        if (hasPhoto()) {
            if (!isHasMessageForEdit()) return COMEBACK;
            updatePhoto();
        } else if (hasDocument() || hasAudio() || hasVideo()) {
            if (!isHasMessageForEdit()) return COMEBACK;
            updateFile();
        } else if (isButton(Const.CHANGE_BUTTON_NAME)) {
            sendMessage(Const.ENTER_NEW_NAME_BUTTON_MESSAGE);
            waitingType = WaitingType.UPDATE_BUTTON;
            return EXIT;
        } else if (isButton(Const.CHANGE_BUTTON_TEXT)) {
            if (!isHasMessageForEdit()) return COMEBACK;
            sendMessage(Const.SEND_NEW_MESSAGE_FOR_BUTTON);
            waitingType = WaitingType.UPDATE_TEXT;
            return EXIT;
        } else if (isButton(Const.ADD_FILE_FROM_BUTTON)) {
            sendMessage(Const.SEND_NEW_FILE_MESSAGE);
            waitingType = WaitingType.UPDATE_FILE;
            return EXIT;
        } else if (isButton(Const.DELETE_FILE_FROM_BUTTON)) {
            if (!isHasMessageForEdit()) return COMEBACK;
            deleteFile();
        } else if (isButton(Const.CHANGE_LANGUAGE_BUTTON)) {
            if (currentLanguage == Language.ru) {
                currentLanguage = Language.kz;
            } else if (currentLanguage == Language.kz) {
                currentLanguage = Language.en;
            } else {
                currentLanguage = Language.ru;
            }
            currentButton = buttonRepository.findByIdAndLangId(buttonId, currentLanguage.getId());
            sendEditor();
            return EXIT;
        } else if (isButton(Const.NEXT_BUTTON)) {
            deleteMessage(updateMessageId);
            deleteMessage(textId);
            if (keyboardMarkUpId != 0) {
                isUrl = getButtonIds(keyboardMarkUpId);
            }
            if (keyboardMarkUpId == 2) {
                currentButton = buttonRepository.findByIdAndLangId(buttonId, currentLanguage.getId());
                sendEditor();
                return COMEBACK;
            } else if (keyboardMarkUpId > 0) {
                if (!isUrl) {
                    toDeleteKeyboard(sendMessageWithKeyboard(Const.CHOOSE_WHAT_TO_EDIT_MESSAGE, keyboardMarkUpService.selectForEdition(keyboardMarkUpId, currentLanguage)));
                    waitingType = WaitingType.NEXT_KEYBOARD;
                } else {
                    currentButton = buttonRepository.findByIdAndLangId(buttonId, currentLanguage.getId());
                    sendEditor();
                    return COMEBACK;
                }
            }
        } else if (updateMessageText.startsWith(linkEdit)) {
            String buttonId = updateMessageText.replace(linkEdit,"");
            if (keyboardMarkUpService.getButtonString(keyId).isPresent()) {
                sendMessage(Const.MESSAGE_FROM_LINK_EDIT_BUTTON);
                buttonLinkId = Integer.parseInt(buttonId);
                waitingType = WaitingType.UPDATE_BUTTON_LINK;
                return EXIT;
            } else {
                return COMEBACK;
            }
        } else {
            return COMEBACK;
        }
        return EXIT;
    }

    private boolean     isHasMessageForEdit()   throws TelegramApiException {
        if (message == null) {
            sendMessage(1032);
            return COMEBACK;
        }
        return EXIT;
    }

    private void        clearOld() {
        deleteMessage(textId);
        deleteMessage(photoId);
    }

    private void        loadElements() {
        if (currentButton.getMessageId() == null || currentButton.getMessageId() == 0) {
            message = null;
        } else {
            message = messageRepository.findByIdAndLangId(currentButton.getMessageId(), currentLanguage.getId());
        }
    }

    private static int  getMaxSizeMessage() { return Const.MAX_SIZE_MESSAGE; }

    private void        updateFile() {
        if (hasDocument()) {
            message.setFile(update.getMessage().getDocument().getFileId(), FileType.document);
        } else if (hasAudio()) {
            message.setFile(update.getMessage().getAudio().getFileId(), FileType.audio);
        } else if (hasVideo()) {
            message.setFile(update.getMessage().getVideo().getFileId(), FileType.video);
        }
        update();
    }

    private void        updatePhoto() {
        message.setPhoto(updateMessagePhoto);
        update();
    }

    private void        update() {
        messageRepository.save(message);
        //log.info("Update message {} for lang {} - chatId = ", message.getId(), currentLanguage.name(), chatId);
    }

    private void        deleteFile() {
        message.setTypeFile(null);
        message.setFile(null);
        update();
    }

    private boolean     getButtonIds(int keyboardMarkUpId) {
        String buttonString = keyboardMarkUpService.getButtonString(keyboardMarkUpId).get();
        if (buttonString == null) return COMEBACK;
        String[] rows = buttonString.split(";");
        for (String buttonIdString : rows) {
            String[] buttonIds = buttonIdString.split(",");
            for (String buttonId : buttonIds) {
                Button buttonFromDb = buttonRepository.findByIdAndLangId(Integer.parseInt(buttonId), currentLanguage.getId());
                String url = buttonFromDb.getUrl();
                if (url != null) {
                    return EXIT;
                } else {
                    return COMEBACK;
                }
            }
        }
        return COMEBACK;
    }
}
