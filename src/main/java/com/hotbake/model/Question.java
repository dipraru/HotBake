package com.hotbake.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "questions")
@Getter @Setter @NoArgsConstructor
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "asker_id", nullable = false)
    private User asker;

    @Column(nullable = false, length = 500)
    private String questionText;

    @Column(length = 1000)
    private String answerText;

    @Column(nullable = false, updatable = false)
    private LocalDateTime askedAt = LocalDateTime.now();

    private LocalDateTime answeredAt;
}
