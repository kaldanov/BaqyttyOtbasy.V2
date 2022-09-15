package com.telegrambot.repository;

import com.telegrambot.entity.custom.CountHandlingPlan;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CountHandlingPlanRepository extends CrudRepository<CountHandlingPlan, Integer> {
}
