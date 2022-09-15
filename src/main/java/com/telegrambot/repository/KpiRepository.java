package com.telegrambot.repository;

import com.telegrambot.entity.custom.Kpi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface KpiRepository extends JpaRepository<Kpi, Integer> {
    List<Kpi> findAllByDateBeforeAndDateAfterAndKpiType(Date start, Date end, String type);

    List<Kpi> findAllByDateBeforeAndDateAfter(Date start, Date end);
    List<Kpi> findAllByDateBetweenAndKpiType(Date start, Date end, String type);
    List<Kpi> findAllByDateBetween(Date start, Date end);

}
