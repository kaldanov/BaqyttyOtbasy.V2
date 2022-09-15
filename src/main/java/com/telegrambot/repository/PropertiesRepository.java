package com.telegrambot.repository;

import com.telegrambot.entity.Properties;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PropertiesRepository extends JpaRepository<Properties, Integer> {
    Properties findById(long id);
}
