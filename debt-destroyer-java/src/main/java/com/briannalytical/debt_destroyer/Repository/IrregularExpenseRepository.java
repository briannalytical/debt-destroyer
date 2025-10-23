package com.briannalytical.debt_destroyer.Repository;

import com.briannalytical.debt_destroyer.Model.Entity.IrregularExpense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IrregularExpenseRepository extends JpaRepository<IrregularExpense, Long> {

    // Find all active irregular expenses
    List<IrregularExpense> findByIsActiveTrue();
}