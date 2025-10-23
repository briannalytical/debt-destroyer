package com.briannalytical.debt_destroyer.Repository;

import com.briannalytical.debt_destroyer.Model.Entity.Expense;
import com.briannalytical.debt_destroyer.Model.Enum.ExpenseType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    // Find all active expenses
    List<Expense> findByIsActiveTrue();

    // Find all active expenses by type (FIXED or VARIABLE)
    List<Expense> findByIsActiveTrueAndExpenseType(ExpenseType expenseType);
}