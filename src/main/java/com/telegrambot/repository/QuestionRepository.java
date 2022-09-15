package com.telegrambot.repository;

import com.telegrambot.entity.custom.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Integer> {
    List<Question> findAllByIsHideIsFalseAndLanguageIdOrderById(int languageId);
    List<Question> findAllByOrderById();
    Question deleteById(long questId);
    Question findByIdAndLanguageId(long id, long langId);
//    Question find();
}
