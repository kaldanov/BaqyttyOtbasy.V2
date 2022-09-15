package com.telegrambot.command.impl;

import com.telegrambot.command.Command;
import com.telegrambot.entity.custom.QuestMessage;
import com.telegrambot.entity.custom.Question;
import com.telegrambot.entity.enums.WaitingType;
import com.telegrambot.enums.Language;
import com.telegrambot.service.LanguageService;
import com.telegrambot.util.ButtonUtil;
import com.telegrambot.util.ButtonsLeaf;
import com.telegrambot.util.Const;
import com.telegrambot.util.UpdateUtil;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class id014_EditSurvey extends Command {

    private ButtonsLeaf         buttonsLeaf;
    private List<Question>      all;
    private long                 questId;
    private long                 questMessageId;
    private Question            question;
    private QuestMessage        questMessage;
    private List<QuestMessage>  questMessageList;
    private Language currentLanguage;
    private int                 editionMessageId;
    private QuestMessage        addQuestMessage;
    private WaitingType         updateType = WaitingType.START;

    @Override
    public boolean  execute()           throws TelegramApiException {
        if(userRepository.findByChatId(chatId) == null){
            sendMessageWithKeyboard("Вы не прошли регистрацию" , 57);
            return EXIT;
        }
        if (!isAdmin() && !isMainAdmin()) {
            sendMessage(Const.NO_ACCESS);
            return EXIT;
        }
        switch (updateType) {
            case UPDATE_QUEST:
                if (isCommand()) return COMEBACK;
                break;
            case UPDATE_MESSAGE:
                if (isCommandMessage()) return COMEBACK;
                break;
        }
        switch (waitingType) {
            case START:
                deleteMessage(updateMessageId);
                currentLanguage = LanguageService.getLanguage(chatId);
                all             = questionRepository.findAll();
                buttonsLeaf     = new ButtonsLeaf(all.stream().map(Question::getName).collect(Collectors.toList()));
                toDeleteKeyboard(sendMessageWithKeyboard("Выберите опрос для редактирования", buttonsLeaf.getListButton()));
                waitingType     = WaitingType.CHOOSE_QUESTION;
                return COMEBACK;
            case CHOOSE_QUESTION:
                deleteMessage(updateMessageId);
                if (hasCallbackQuery()) {
                    if (buttonsLeaf.isNext(updateMessageText)) toDeleteKeyboard(sendMessageWithKeyboard("Выберите опрос для редактирования", buttonsLeaf.getListButton()));
                    questId     = all.get(Integer.parseInt(updateMessageText)).getId();
                    sendMessage(1045);
                    waitingType = WaitingType.EDITION;
                    updateType  = WaitingType.UPDATE_QUEST;
                    sendEditor();
                } else {
                    toDeleteKeyboard(sendMessageWithKeyboard("Выберите опрос для редактирования", buttonsLeaf.getListButton()));
                }
                return COMEBACK;
            case EDITION_MESSAGE:
                isCommandMessage();
                return COMEBACK;
            case SET_NAME:
                if (hasMessageText()) {
                    question.setName(ButtonUtil.getButtonName(updateMessageText,200));
                    questionRepository.save(question);
                    sendEditor();
                }
                return COMEBACK;
            case SET_DESCRIPTION:
                if (hasMessageText()) {
                    question.setDescription(updateMessageText);
                    questionRepository.save(question);
                    sendEditor();
                }
                return COMEBACK;
            case SET_RANGE:
                if (hasMessageText()) {
                    if (questMessage.getRange().split(Const.SPLIT_RANGE).length == updateMessageText.split(Const.SPLIT_RANGE).length) {
                        questMessage.setRange(updateMessageText);
                        questMessageRepository.save(questMessage);
                    } else {
                        for (Language language : Language.values()) {
                            QuestMessage byId = questMessageRepository.findByIdAndLanguageId(questMessageId, language.getId());
                            byId.setRange(updateMessageText);
                            questMessageRepository.save(byId);
                        }
                        sendMessage("Количество вариантов не совпадает с ранней версией - данные обновлены для всех языков");
                    }
                    sendEditorMessage();
                }
                return COMEBACK;
            case SET_MESSAGE:
                if (hasMessageText()) {
                    questMessage.setMessage(updateMessageText);
                    questMessageRepository.save(questMessage);
                    sendEditorMessage();
                }
                return COMEBACK;
            case DELETE:
                if (hasMessageText() && updateMessageText.equals("удалить")) {
                    surveyAnswerRepository.deleteBySurveyId(questId);
                    questMessageRepository.deleteByIdQuest(questId);
                    questionRepository.deleteById(questId);
                    log.info("Deleted question №{} - {}", questId, UpdateUtil.getUser(update).toString());
                    sendMessage(1050);
                    return EXIT;
                }
                return COMEBACK;
            case DELETE_MESSAGE:
                if (hasMessageText() && updateMessageText.equalsIgnoreCase("удалить")) {
                    questMessageRepository.deleteById(questMessageId);
                    log.info("Deleted message №{} for quest №{} - {}", questMessageId, questId, UpdateUtil.getUser(update).toString());
                    sendMessage(1049);
                    sendEditor();
                }
                return COMEBACK;
            case GET_RANGE:
                if (hasMessageText()) {
                    addQuestMessage = new QuestMessage();
                    addQuestMessage.setRange(updateMessageText).setIdQuest(questId);
                    sendMessage("Напишите сообщение для этой группы. Пример:\nНам очень жаль что мы не оправдали Ваши ожидания. Хотелось бы узнать что нам необходимо улучшить?");
                    waitingType = WaitingType.GET_MESSAGE;
                }
                return COMEBACK;
            case GET_MESSAGE:
                if (hasMessageText()) {
                    addQuestMessage.setMessage(updateMessageText);

                    addQuestMessage.setId(questMessageRepository.findAll().get(questMessageRepository.findAll().size()-1).getId());
                    for (Language language : Language.values()) {
                        questMessageRepository.save(addQuestMessage.setIdLanguage(language.getId()));
                    }
                    sendEditor();
                }
                return COMEBACK;
        }
        return EXIT;
    }

    private void    sendEditor()        throws TelegramApiException {
        deleteMessage(editionMessageId);
        deleteMessage(updateMessageId);
        loadQuest();
        String text         = String.format(getText(1046), messageRepository.getMessageText(1047, currentLanguage.getId()), question.getName(), question.getDescription());
        buttonsLeaf         = new ButtonsLeaf(questMessageList.stream().map(QuestMessage::getRange).collect(Collectors.toList()));
        editionMessageId    =  sendMessageWithKeyboard(text, buttonsLeaf.getListButton());
        toDeleteKeyboard(editionMessageId);
    }

    private void    loadQuest() {
        question            = questionRepository.findByIdAndLanguageId(questId, currentLanguage.getId());
        questMessageList    = questMessageRepository.findAllByIdAndLanguageIdOrderById(questId, currentLanguage.getId());
    }

    private boolean isCommand()         throws TelegramApiException {
        if (hasCallbackQuery()) {
            questMessageId  = questMessageList.get(Integer.parseInt(updateMessageText)).getId();
            updateType      = WaitingType.UPDATE_MESSAGE;
            sendEditorMessage();
        } else if (isButton(1009)) {
            sendMessage("Введите новое название");
            waitingType = WaitingType.SET_NAME;
        } else if (isButton(29)) {
            sendMessage("Введите новое описание");
            waitingType = WaitingType.SET_DESCRIPTION;
        } else if (isButton(1011)) {
            sendMessage("Напишите слово 'удалить' для подтвержения. Внимание, вместе с опросом удаляется ответы на этот опрос.");
            waitingType = WaitingType.DELETE;
        } else if (isButton(32)) {
            changeLanguage();
            sendEditor();
        } else if (isButton(1010)) {
            sendMessage("Добавьте группу ответов на русском через '%s' Примеры \n1,2,3,4\nхорошо,средне,плохо\n*,**,*** ");
            waitingType = WaitingType.GET_RANGE;
        } else {
            return COMEBACK;
        }
        return EXIT;
    }

    private void    sendEditorMessage() throws TelegramApiException {
        deleteMessage(editionMessageId);
        deleteMessage(updateMessageId);
        questMessage    = questMessageRepository.findByIdAndLanguageId(questMessageId, currentLanguage.getId());
        String text     = String.format(getText(1048), messageRepository.getMessageText(1047, currentLanguage.getId()), questMessage.getRange(), questMessage.getMessage());
        sendMessageWithKeyboard(text, 16);
        waitingType     = WaitingType.EDITION_MESSAGE;
    }

    private void    changeLanguage() {
        if (currentLanguage == Language.ru) {
            currentLanguage = Language.kz;
        } else {
            currentLanguage = Language.ru;
        }
    }

    public boolean  isCommandMessage()  throws TelegramApiException {
        if (isButton(1012)) {
            sendMessage("Введите новые варианты ответа");
            waitingType = WaitingType.SET_RANGE;
        } else if (isButton(29)) {
            sendMessage("Введите новое сообщение");
            waitingType = WaitingType.SET_MESSAGE;
        } else if (isButton(32)) {
            changeLanguage();
            sendEditorMessage();
        } else if (isButton(1011)) {
            sendMessage("Напишите слово 'удалить' для подтверждения");
            waitingType = WaitingType.DELETE_MESSAGE;
        } else if (isButton(1005)) {
            updateType = WaitingType.UPDATE_QUEST;
            sendMessage(1045);
            sendEditor();
        } else {
            return COMEBACK;
        }
        return EXIT;
    }
}
