package com.telegrambot.command.impl;

import com.telegrambot.command.Command;
import com.telegrambot.entity.custom.QuestMessage;
import com.telegrambot.entity.custom.Question;
import com.telegrambot.entity.custom.SurveyAnswer;
import com.telegrambot.entity.enums.WaitingType;
import com.telegrambot.enums.Language;
import com.telegrambot.util.ButtonsLeaf;
import com.telegrambot.util.Const;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class id010_Survey extends Command {

    private Language currentLanguage;
    private List<Question>     allQuestion;
    private ButtonsLeaf        buttonsLeaf;
    private int                deleteMessageId;
    private Question           question;
    private List<QuestMessage> allMessage;
    private List<String>       listAnswers;
    private SurveyAnswer       surveyAnswer;
    private Integer            surveyId;
    @Override
    public boolean execute() throws TelegramApiException {

        switch (waitingType) {
            case START:
                if(userRepository.findByChatId(chatId) == null){
                    sendMessageWithKeyboard("Вы не прошли регистрацию" , 57);
                    return EXIT;
                }

                deleteMessage(updateMessageId);
                deleteMessage(deleteMessageId);
                currentLanguage     = getLanguage();
                surveyId            = surveyAnswerRepository.findByChatId(chatId);
                List<SurveyAnswer> surveyList = surveyAnswerRepository.findAllByChatIdOrderById(chatId);
                allQuestion         = questionRepository.findAllByIsHideIsFalseAndLanguageIdOrderById(currentLanguage.getId());
                if (allQuestion == null || allQuestion.size() == 0) {
                    deleteMessageId = sendMessage(Const.SURVEY_EMPTY_MESSAGE);
                    return EXIT;
                }
                List<String> list   = new ArrayList<>();
                allQuestion.forEach((e) -> list.add(e.getName()));
                buttonsLeaf         = new ButtonsLeaf(list);
                deleteMessageId     = toDeleteKeyboard(sendMessageWithKeyboard(Const.CHOOSE_SURVEY, buttonsLeaf.getListButton()));
                waitingType         = WaitingType.CHOOSE_QUESTION;
                return COMEBACK;
            case CHOOSE_QUESTION:
                deleteMessage(updateMessageId);
                deleteMessage(deleteMessageId);
                if (hasCallbackQuery()) {
                    question        = allQuestion.get(Integer.parseInt(updateMessageText));
                    allMessage      = questMessageRepository.findAllByIdQuestAndLanguageIdOrderById(question.getId(), currentLanguage.getId());
                    listAnswers     = new ArrayList<>();
                    allMessage.forEach((e) -> Collections.addAll(listAnswers, e.getRange().split(",")));
                    buttonsLeaf     = new ButtonsLeaf(listAnswers);
                    deleteMessageId = toDeleteKeyboard(sendMessageWithKeyboard(question.getDescription(), buttonsLeaf.getListButton()));
                    waitingType     = WaitingType.CHOOSE_OPTION;
                } else {
                    deleteMessageId = toDeleteKeyboard(sendMessageWithKeyboard(Const.CHOOSE_SURVEY, buttonsLeaf.getListButton()));
                }
                return COMEBACK;
            case CHOOSE_OPTION:
                deleteMessage(updateMessageId);
                deleteMessage(deleteMessageId);
                if (hasCallbackQuery()) {
                    String answer = listAnswers.get(Integer.parseInt(updateMessageText));
                    for (QuestMessage questMessage : allMessage) {
                        for (String range: questMessage.getRange().split(",")) {
                            if (range.equals(answer)) {
                                deleteMessageId = sendMessage(questMessage.getMessage());
                                surveyAnswer = new SurveyAnswer();
                                surveyAnswer.setButton(answer);
                                surveyAnswer.setChatId(chatId);
                                surveyAnswer.setSurveyId(question.getId());
                                surveyAnswer.setText("-");
                                surveyAnswerRepository.save(surveyAnswer);
                                waitingType = WaitingType.SET_TEXT;
                            }
                        }
                    }
                } else {
                    deleteMessageId = toDeleteKeyboard(sendMessageWithKeyboard(question.getDescription(), buttonsLeaf.getListButton()));
                }
                return COMEBACK;
            case SET_TEXT:
                deleteMessage(updateMessageId);
                deleteMessage(deleteMessageId);
                if (hasMessageText()) {
                    SurveyAnswer surveyAnswer1 = surveyAnswerRepository.findById(surveyAnswer.getId());
                    surveyAnswer1.setText(updateMessageText);
                    surveyAnswerRepository.save(surveyAnswer1);
                    surveyId            = surveyAnswerRepository.findByChatId(chatId);
                    surveyList = surveyAnswerRepository.findAllByChatIdOrderById(chatId);
                    allQuestion         = questionRepository.findAllByIsHideIsFalseAndLanguageIdOrderById(currentLanguage.getId());
                    //allQuestion         = questionRepository.findAllByHiderIsFalseAndLanguageIdAndIdNotIn(currentLanguage.getId(), surveyId);
                    if (allQuestion == null || allQuestion.size() == 0) {
                        deleteMessageId = sendMessage(Const.SURVEY_EMPTY_MESSAGE);
                        return EXIT;
                    }
                    List<String> reloadList = new ArrayList<>();
                    allQuestion.forEach((e) -> reloadList.add(e.getName()));
                    buttonsLeaf = new ButtonsLeaf(reloadList);
                    deleteMessageId = toDeleteKeyboard(sendMessageWithKeyboard(Const.CHOOSE_SURVEY, buttonsLeaf.getListButton()));
                    waitingType = WaitingType.CHOOSE_QUESTION;
                }
                return COMEBACK;
        }
        return EXIT;
    }
}
