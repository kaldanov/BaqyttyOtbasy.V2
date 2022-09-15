package com.telegrambot.command.impl;

import com.telegrambot.command.Command;
import com.telegrambot.entity.custom.QuestMessage;
import com.telegrambot.entity.custom.Question;
import com.telegrambot.entity.enums.WaitingType;
import com.telegrambot.enums.Language;
import com.telegrambot.util.Const;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

public class id013_AddSurvey extends Command {

    private Question           questionRu;
    private Question           questionKz;
    private List<QuestMessage> questMessageListRu;
    private List<QuestMessage> questMessageListKz;
    private boolean            isCan = false;
    private int                deleteMessageId;

    @Override
    public boolean  execute() throws TelegramApiException {
        if(userRepository.findByChatId(chatId) == null){
            sendMessageWithKeyboard("Вы не прошли регистрацию" , 57);
            return EXIT;
        }
        if (!isAdmin() && !isMainAdmin()) {
            sendMessage(Const.NO_ACCESS);
            return EXIT;
        }
        if (isButton(Const.DONE_BUTTON)) {
            if (isCan) {
                insert();
                sendMessage("Опрос создан");
                return EXIT;
            } else {
                sendMessage("Опрос не завершен");
                return COMEBACK;
            }
        }
        switch (waitingType) {
            case START:
                delete();
                sendMessage(Const.RUS_NAME_FROM_NEW_SURVEY_MESSAGE);
                waitingType = WaitingType.SET_NAME_RU;
                return COMEBACK;
            case SET_NAME_RU:
                delete();
                if (hasMessageText()) {
                    questionRu = new Question();
                    questionRu.setLanguageId(Language.ru.getId());
                    questionRu.setName(updateMessageText);
                    waitingType = WaitingType.SET_NAME_KZ;
                    deleteMessageId = sendMessage(Const.KZ_NAME_FROM_NEW_SURVEY_MESSAGE);
                }
                return COMEBACK;
            case SET_NAME_KZ:
                delete();
                if (hasMessageText()) {
                    questionKz = new Question();
                    questionKz.setLanguageId(Language.kz.getId());
                    questionKz.setName(updateMessageText);
                    deleteMessageId = sendMessage("Напишите вопрос для русского языка"); // TODO: 15.03.2020 сделать запись в бд
                    waitingType = WaitingType.SET_TEXT_RU;
                }
                return COMEBACK;
            case SET_TEXT_RU:
                delete();
                if (hasMessageText()) {
                    questionRu.setDescription(updateMessageText);
                    waitingType = WaitingType.SET_TEXT_KZ;
                    deleteMessageId = sendMessage("Напишите вопрос для казахского языка"); // TODO: 15.03.2020 сделать запись в бд
                }
                return COMEBACK;
            case SET_TEXT_KZ:
                delete();
                if (hasMessageText()) {
                    questionKz.setDescription(updateMessageText);
                    deleteMessageId = sendMessage(String.format("Добавьте группу ответов на русском через '%s' Примеры : \n1,2,3,4\nхорошо,средне,плохо\n*,**,***", Const.SPLIT_RANGE)); // TODO: 15.03.2020 сделать запись в бд
                    questMessageListRu = new ArrayList<>();
                    questMessageListKz = new ArrayList<>();
                    waitingType = WaitingType.SET_ANSWER_OF_QUEST_RU;
                }
                return COMEBACK;
            case SET_ANSWER_OF_QUEST_RU:
                delete();
                if (hasMessageText()) {
                    questMessageListRu.add(new QuestMessage().setRange(updateMessageText).setIdLanguage(Language.ru.getId()));
                    deleteMessageId = sendMessage(String.format("Добавьте группу ответов на казахском через '%s' Примеры : \n1,2,3,4\nхорошо,средне,плохо\n*,**,***", Const.SPLIT_RANGE)); // TODO: 15.03.2020 сделать запись в бд
                    isCan = false;
                    waitingType = WaitingType.SET_ANSWER_OF_QUEST_KZ;
                }
                return COMEBACK;
            case SET_ANSWER_OF_QUEST_KZ:
                delete();
                if (hasMessageText()) {
                    if (!checkCount()) {
                        deleteMessageId = sendMessage("Количество вариантов на казахском не совпадает с русским"); // TODO: 15.03.2020 сделать запись в бд
                        return COMEBACK;
                    }
                    questMessageListKz.add(new QuestMessage().setRange(updateMessageText).setIdLanguage(Language.kz.getId()));
                    deleteMessageId = sendMessage("Напишите сообщение для этой группы ответов на русском. Пример:\n 'Нам очень жаль что мы не оправдали Ваши ожидания. Хотелось бы узнать что нам необходимо улучшить?");
                    waitingType = WaitingType.SET_MESSAGE_RU;
                }
                return COMEBACK;
            case SET_MESSAGE_RU:
                delete();
                if (hasMessageText()) {
                    questMessageListRu.get(questMessageListRu.size() -1).setMessage(updateMessageText);
                    deleteMessageId = sendMessage("Напишите сообщение для этой группы ответов на казахском. Пример:\n 'Нам очень жаль что мы не оправдали Ваши ожидания. Хотелось бы узнать что нам необходимо улучшить?");
                    waitingType = WaitingType.SET_MESSAGE_KZ;
                }
                return COMEBACK;
            case SET_MESSAGE_KZ:
                delete();
                questMessageListKz.get(questMessageListKz.size() - 1).setMessage(updateMessageText);
                deleteMessageId = sendMessage(String.format("Нажмите <b>готово</b> или добавьте группу ответов на русском языке через '%s' Примеры:1,2,3,4 хорошо,средне,плохо ", Const.SPLIT_RANGE));
                isCan = true;
                waitingType = WaitingType.SET_ANSWER_OF_QUEST_RU;
                return COMEBACK;
        }
        return COMEBACK;
    }

    private void    insert() {
        long questId;
        if (questionRepository.findAllByOrderById() == null || questionRepository.findAllByOrderById().size() == 0){
             questId = 1;
        }
        else{

             questId = questionRepository.findAllByOrderById().get(questionRepository.findAllByOrderById().size() - 1).getId2() + 1;
        }

        questionRu.setId2(questId);
        questionKz.setId2(questId);
        questionRepository.save(questionRu);
        questionRepository.save(questionKz);
        for (int i = 0; i < questMessageListRu.size(); i++) {
            long questMessageId = 1;

            if(questMessageRepository.findAll().size() != 0){

                 questMessageId = questMessageRepository.findAll().get(i).getId();
            }
            questMessageRepository.save(questMessageListRu.get(i).setId(questMessageId).setIdQuest(questId));
            questMessageRepository.save(questMessageListKz.get(i).setId(questMessageId).setIdQuest(questId));
        }
    }

    private void    delete() {
        deleteMessage(deleteMessageId);
        deleteMessage(updateMessageId);
    }

    private boolean checkCount() {
        int countRu = questMessageListRu.get(questMessageListRu.size() - 1).getRange().split(Const.SPLIT_RANGE).length;
        int countKz = updateMessageText.split(Const.SPLIT_RANGE).length;
        return countKz == countRu;
    }
}
