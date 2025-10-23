package com.briannalytical.debt_destroyer.Repository;

import com.briannalytical.debt_destroyer.Model.Entity.DebtPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DebtPaymentRepository extends JpaRepository<DebtPayment, Long> {

    // Find all payments for a specific month/year
    List<DebtPayment> findByMonthAndYear(Integer month, Integer year);

    // Find all payments for a specific debt account
    List<DebtPayment> findByDebtAccountId(Long debtAccountId);

    // Find a specific payment for a debt account in a month
    Optional<DebtPayment> findByDebtAccountIdAndMonthAndYear(Long debtAccountId, Integer month, Integer year);
}