package com.telegrambot.repository;

import com.telegrambot.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdminRepository extends JpaRepository<Admin,Integer> {

    Integer countAllByUserId(long chatId);
    Integer countByUserId(long userId);

    List<Admin> findAllByOrderById();

    void deleteByUserId(long userId);


//    @Query("SELECT count(a) FROM public.admin a WHERE a.USER_ID = ?1")
//    Integer isMainAdmin(long chatId);

    Admin findByUserId(long chatId);
}
