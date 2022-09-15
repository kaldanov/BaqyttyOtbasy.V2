package com.telegrambot.repository;

import com.telegrambot.entity.custom.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {

    void deleteAllByCategoryId(long categoryId);

    List<Service> findAllByCategoryIdAndLangIdOrderById(long categoryId, long langId);
    List<Service> findAllByCategoryIdAndLangIdAndActiveTrueOrderById(long categoryId, long langId);

    List<Service> findAllByCategoryIdOrderById(long categoryId);

    List<Service> findAllByLangIdOrderById(long langId);

//    Service findById(long id);

    Service findById2AndLangId(long id, long langId);


}
