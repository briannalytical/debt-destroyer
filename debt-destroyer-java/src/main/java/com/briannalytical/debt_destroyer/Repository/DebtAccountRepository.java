package com.briannalytical.debt_destroyer.Repository;

import com.briannalytical.debt_destroyer.Model.Entity.DebtAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DebtAccountRepository extends JpaRepository<DebtAccount, Long> {

    // Find all active debt accounts
    List<DebtAccount> findByIsActiveTrue();
}