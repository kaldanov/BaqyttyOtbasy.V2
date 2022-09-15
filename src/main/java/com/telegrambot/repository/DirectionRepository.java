package com.telegrambot.repository;

import com.telegrambot.entity.custom.Direction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DirectionRepository extends JpaRepository<Direction,Integer> {

    List<Direction> findAllByServiceIdOrderById(long serviceId);


    Direction findById(long id);


}
