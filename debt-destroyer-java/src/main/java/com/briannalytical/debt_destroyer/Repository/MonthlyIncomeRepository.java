package com.briannalytical.debt_destroyer.Repository;

import com.briannalytical.debt_destroyer.Model.Entity.MonthlyIncome;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MonthlyIncomeRepository extends JpaRepository<MonthlyIncome, Long> {

    // Find income for a specific month/year
    Optional<MonthlyIncome> findByMonthAndYear(Integer month, Integer year);
}