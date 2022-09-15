package com.telegrambot.repository;

import com.telegrambot.entity.custom.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Integer> {
    List<Event> findAllByHiderIsFalseOrderById();

    Event deleteById(int eventId);

    Event findById(long id);
}
