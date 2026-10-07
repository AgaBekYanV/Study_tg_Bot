package com.my.study_tg_bot.entity.task;

import com.my.study_tg_bot.entity.user.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tasks")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Task {

    @Id
    @Column(name = "id")
    @GeneratedValue
    UUID id;

    @Column(name = "title")
    String title;

    @Column(name = "text_content")
    String textContent;

    @Column(name = "actual_message_id")
    Integer messageId;

    @Column(name = "in_creation")
    Boolean isInCreation;

    @ManyToMany
    @JoinTable(
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"),
            name = "tasks_teacher_student"
    )
    List<User> users;
}
