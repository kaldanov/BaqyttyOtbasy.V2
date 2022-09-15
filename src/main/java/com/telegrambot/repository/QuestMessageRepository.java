package com.telegrambot.repository;


import com.telegrambot.entity.custom.QuestMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestMessageRepository extends JpaRepository<QuestMessage, Integer> {
    List<QuestMessage> findAllByIdQuestAndLanguageIdOrderById(long id, long languageId);
    List<QuestMessage> findAllByIdAndLanguageIdOrderById(long id, long languageId);
    QuestMessage findByIdAndLanguageId(long id, long languageId);
    void deleteByIdQuest(long questId);
    QuestMessage deleteById(long id);


//    @Query("SELECT max(id) from :tableName")
//    QuestMessage findNextId(@Param("tableName") String tableName);

}
