package com.telegrambot.repository;

import com.telegrambot.entity.custom.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRepository extends JpaRepository<Group, Integer> {
    Group findById(long groupId);
}
