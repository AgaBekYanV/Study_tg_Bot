package com.my.study_tg_bot.repository;

import com.my.study_tg_bot.entity.timetable.Timetable;
import com.my.study_tg_bot.entity.timetable.WeekDay;
import com.my.study_tg_bot.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TimetableRepository extends JpaRepository<Timetable, UUID> {

    List <Timetable> findAllByUsersContainingAndWeekDay(User user, WeekDay weekDay);

    Timetable findTimetableById(UUID id); //18:58
}
