package com.telegrambot.repository;

import com.telegrambot.entity.Operator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OperatorRepository extends JpaRepository<Operator, Integer> {
    int countByUserId(long chatId);
    void deleteByUserId(Long chatId);
    Operator findByUserId(long id);
}
