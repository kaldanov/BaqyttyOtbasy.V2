package com.telegrambot.repository;

import com.telegrambot.entity.custom.ServiceSurveyAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceSurveyAnswerRepository extends JpaRepository<ServiceSurveyAnswer, Integer> {
    List<ServiceSurveyAnswer> findAllByChatIdAndHandlingTypeOrderById(long chatId, String handlingType);
}
