package com.telegrambot.repository;


import com.telegrambot.entity.custom.Status;
import com.telegrambot.entity.custom.SurveyAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StatusRepository extends JpaRepository<Status, Integer> {
    Status findById(int id);

    List<Status> findAll();

    List<Status> findAllByStatusId(int id);

    List<Status> findAllByIdOrderById(long id);

    List<Status> findAllOrderById(long chatId);

}
