package com.telegrambot.repository;

import com.telegrambot.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Integer> {
    Message findById(long id);
    Message  findByIdAndLangId(long id, int languageId);

    @Query("select m.name from Message m WHERE m.id =?1 and m.langId =?2")
    String    getMessageText(long id, int langId);

    //Message findByIdAndLangId(int id, int langId);



//    Optional<Message> findById(int id);

//    Message             findByIdAndLangId(int id, int languageId);

    List<Message> findAllByNameContainingAndLangIdOrderById(String name, int langId);

    @Transactional
    @Modifying
    @Query("update Message set name = ?1 where id = ?2 and langId = ?3")
    void update(String name, long id, int langId);
}
