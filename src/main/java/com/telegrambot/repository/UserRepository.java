package com.telegrambot.repository;

import com.telegrambot.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
//    User            findByChatId(long chatId);
    List<User>      findAllByChatIdOrderById(long chatId);
//    List<User>      findAllByEmailOrderById(String email);
    int             countByChatId(long chatId);
    User            findByIin(String iin);
    List<User>            findAllByIin(String iin);
    User  findByPhone(String phone);
//    int             countByChatIdAndDistrict(long chatId, String district);
    //User findByChatId(long chatId);

    void deleteByChatId(Long chatId);
    User  findByChatId(long chatId);

}
