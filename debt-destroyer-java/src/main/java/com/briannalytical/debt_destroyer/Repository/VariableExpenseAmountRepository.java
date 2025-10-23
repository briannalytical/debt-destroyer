package com.briannalytical.debt_destroyer.Repository;

import com.briannalytical.debt_destroyer.Model.Entity.VariableExpenseAmount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VariableExpenseAmountRepository extends JpaRepository<VariableExpenseAmount, Long> {

    // Find all variable expense amounts for a specific month/year
    List<VariableExpenseAmount> findByMonthAndYear(Integer month, Integer year);

    // Find all amounts for a specific expense
    List<VariableExpenseAmount> findByExpenseId(Long expenseId);
}