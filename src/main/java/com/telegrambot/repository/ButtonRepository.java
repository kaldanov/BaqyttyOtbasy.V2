package com.telegrambot.repository;

import com.telegrambot.entity.Button;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface ButtonRepository extends JpaRepository<Button, Long> {

    Button findByNameAndLangId(String buttonName, int languageId);


    Button findByIdAndLangId(long buttonId, int languageId);
//    Button findByIdAndLangId(long id);

    @Query("select b.name from Button b where b.id =?1 and b.langId =?2")
    String getButtonText(long id, int languageId);

    @Transactional
    @Modifying
    @Query("update Button set name = ?1 where id = ?2 and langId = ?3")
    void update(String name, long id, int langId);


    Long countByNameAndLangId(String name, int langId);

    Optional<Button> findByName(String buttonId);

    List<Button> findAllByNameContainingAndLangIdOrderById(String value, int langId);

}
