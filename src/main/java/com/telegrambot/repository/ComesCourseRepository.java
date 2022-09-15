package com.telegrambot.repository;

import com.telegrambot.entity.custom.ComesCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ComesCourseRepository extends JpaRepository<ComesCourse, Integer> {

    List<ComesCourse> findAllByRegistrationServiceIdAndActionDateBetweenOrderByRegistrationServiceId(long regServiceId, Date start, Date end);

    List<ComesCourse> findAllByRegistrationServiceIdOrderById(long regServiceId);
    List<ComesCourse> findAllByActionDateBetweenOrderById( Date start, Date end);

    List<ComesCourse> findAllByRegistrationServiceIdAndActionDateBetweenOrderById(long regServiceId, Date start, Date end);

//    boolean existsByActionDateAndRegistrationServiceId(Date)
}
