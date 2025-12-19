package com.my.study_tg_bot.entity.timetable;

import com.my.study_tg_bot.entity.user.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "timetable")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Timetable {

    @Id
    @Column(name = "id")
    @GeneratedValue
    UUID id;

    @Column(name = "title")
    String title;

    @Column(name = "description")
    String description;

    @Enumerated(EnumType.STRING)
    WeekDay weekDay;

    @Column(name = "hour")
    Short hour;

    @Column(name = "minute")
    Short minute;

    @ManyToMany
    @JoinTable(
            joinColumns = @JoinColumn(name = "timetable_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"),
            name = "users_timetable"
    )
    List<User> users;

    public void addUser(User user){
        if(users == null){
            users = new ArrayList<>();
        }
        users.add(user);
    }
}
