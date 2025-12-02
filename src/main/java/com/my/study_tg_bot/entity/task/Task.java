package com.my.study_tg_bot.entity.task;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

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
}
