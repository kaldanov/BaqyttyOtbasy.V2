package com.telegrambot.repository;

import com.telegrambot.entity.custom.Category_Indicator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoriesIndicatorRepository extends JpaRepository<Category_Indicator,Integer> {

//    Category_Indicator findById(long id);



//    Category_Indicator findBySecond(long id_2);

    Category_Indicator findBySecondAndLangId(long second, int langId);

    List<Category_Indicator> findAllByLangIdOrderById(int langId);

//    void deleteBySecond(long currentCategoryId);

//    List<Category_Indicator> findAllOrderById();



}
