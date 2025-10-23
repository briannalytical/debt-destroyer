package com.briannalytical.debt_destroyer.Model.Entity;

import com.briannalytical.debt_destroyer.Model.Enum.Frequency;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "irregular_expenses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IrregularExpense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Frequency frequency;

    @Column(name = "next_due_date", nullable = false)
    private LocalDate nextDueDate;

    @Column(name = "custom_months")
    private Integer customMonths;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}