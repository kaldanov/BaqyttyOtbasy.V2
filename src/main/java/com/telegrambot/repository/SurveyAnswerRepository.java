package com.telegrambot.repository;


import com.telegrambot.entity.custom.SurveyAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SurveyAnswerRepository extends JpaRepository<SurveyAnswer, Integer> {
    Integer findByChatId(long chatId);
    SurveyAnswer findById(long id);
    List<SurveyAnswer> findAllByIdOrderById(long id);
    List<SurveyAnswer> findAllByChatIdOrderById(long chatId);
    void deleteBySurveyId(long surveyId);
}
