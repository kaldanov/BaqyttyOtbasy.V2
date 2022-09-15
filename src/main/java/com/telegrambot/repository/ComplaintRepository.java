package com.telegrambot.repository;

import com.telegrambot.entity.custom.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    List<Complaint> findAllByPostDateBetweenOrderById(Date start, Date end);
}
