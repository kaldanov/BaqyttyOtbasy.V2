package com.telegrambot.repository;


import com.telegrambot.entity.custom.Suggestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface SuggestionRepository extends JpaRepository<Suggestion, Integer> {

    List<Suggestion> findAllByPostDateBeforeAndPostDateAfterOrderById(Date startDate, Date endDate);
    List<Suggestion> findAllByPostDateBetweenOrderById(Date startDate, Date endDate);
}
