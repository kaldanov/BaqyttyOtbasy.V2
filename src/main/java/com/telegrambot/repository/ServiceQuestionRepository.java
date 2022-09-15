package com.telegrambot.repository;

import com.telegrambot.entity.custom.ServiceQuestion;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceQuestionRepository extends CrudRepository<ServiceQuestion, Integer> {

    List<ServiceQuestion> findAllByServiceIdAndHiderFalseAndHandlingTypeAndLanguageIdAndIdNotInOrderById(int serviceId, String handlingType, int langId, List<Integer> serviceIds);

    List<ServiceQuestion> findAllByServiceIdAndLanguageIdAndHandlingTypeAndIdNotInAndHiderFalseOrderById(int serviceId, int languageId, String handlingType, List<Integer> serviceSurveyAnswer);
}
