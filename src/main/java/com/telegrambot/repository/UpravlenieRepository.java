package com.telegrambot.repository;

import com.telegrambot.entity.custom.Upravlenie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UpravlenieRepository extends JpaRepository<Upravlenie,Integer> {

    Upravlenie findByChatId(long chatId);

}
