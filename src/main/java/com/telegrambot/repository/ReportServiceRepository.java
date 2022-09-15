package com.telegrambot.repository;

import com.telegrambot.entity.custom.ReportToService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportServiceRepository extends JpaRepository<ReportToService,Integer> {
    List<ReportToService> findAllByServiceId(long serviceId);
}
