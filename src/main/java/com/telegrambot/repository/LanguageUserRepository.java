package com.telegrambot.repository;

import com.telegrambot.entity.LanguageUser;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LanguageUserRepository extends CrudRepository<LanguageUser, Integer> {

    Optional<LanguageUser> findByChatId(long chatId);


//    LanguageUser findByChatId(long chatId);
//    LanguageUser findTopByChatId(long chatId);

    boolean existsByChatId(long chatId);
}
