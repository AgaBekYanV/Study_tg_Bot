package com.my.study_tg_bot.repository;

import com.my.study_tg_bot.entity.task.Task;
import com.my.study_tg_bot.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {
    boolean existsByUsersContainingAndIsInCreation(User user, Boolean isInCreation);

    Task findTaskByUsersContainingAndIsInCreation(User user, Boolean isInCreation);

    void deleteByUsersContainingAndIsInCreation(User user, Boolean isInCreation);
}
