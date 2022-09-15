package com.telegrambot.repository;

import com.telegrambot.entity.custom.DirectionRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DirectionRegistrationRepository extends JpaRepository<DirectionRegistration,Integer> {

    List<DirectionRegistration> findAllByRegistrationIdOrderById(long id);
    List<DirectionRegistration> findAllByDirectionIdOrderById(long id);
    List<DirectionRegistration> findAllByRegistrationIdAndDirectionIdOrderById(long id, long dir_id);

}
