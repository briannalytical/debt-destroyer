package com.briannalytical.debt_destroyer.Service;

import com.briannalytical.debt_destroyer.Model.Entity.*;
import com.briannalytical.debt_destroyer.Model.Enum.ExpenseType;
import com.briannalytical.debt_destroyer.Model.Enum.Frequency;
import com.briannalytical.debt_destroyer.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class BudgetCalculationService {

    @Autowired
    private MonthlyIncomeRepository monthlyIncomeRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private VariableExpenseAmountRepository variableExpenseAmountRepository;

    @Autowired
    private IrregularExpenseRepository irregularExpenseRepository;

    @Autowired
    private DebtAccountRepository debtAccountRepository;

    /**
     calculate total fixed essential expenses
     **/
    public BigDecimal calculateFixedExpenses() {
        List<Expense> fixedExpenses = expenseRepository
                .findByIsActiveTrueAndExpenseType(ExpenseType.FIXED);

        return fixedExpenses.stream()
                .map(Expense::getFixedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     calculate total variable expenses for a specific month
     **/
    public BigDecimal calculateVariableExpenses(Integer month, Integer year) {
        List<VariableExpenseAmount> variableAmounts =
                variableExpenseAmountRepository.findByMonthAndYear(month, year);

        return variableAmounts.stream()
                .map(VariableExpenseAmount::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     calculate monthly sinking fund for irregularly occurring expenses
     **/
    public BigDecimal calculateMonthlySinkingFund() {
        List<IrregularExpense> irregularExpenses =
                irregularExpenseRepository.findByIsActiveTrue();

        BigDecimal totalMonthly = BigDecimal.ZERO;

        for (IrregularExpense expense : irregularExpenses) {
            BigDecimal monthlyAmount = switch (expense.getFrequency()) {
                case ANNUAL -> expense.getAmount().divide(
                        BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
                case SEMI_ANNUAL -> expense.getAmount().divide(
                        BigDecimal.valueOf(6), 2, RoundingMode.HALF_UP);
                case QUARTERLY -> expense.getAmount().divide(
                        BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP);
            };
            totalMonthly = totalMonthly.add(monthlyAmount);
        }

        return totalMonthly;
    }

    /**
     calculate total essentials for a specific month
     **/
    public BigDecimal calculateTotalEssentials(Integer month, Integer year) {
        BigDecimal fixed = calculateFixedExpenses();
        BigDecimal variable = calculateVariableExpenses(month, year);
        BigDecimal sinkingFund = calculateMonthlySinkingFund();

        return fixed.add(variable).add(sinkingFund);
    }

    /**
     calculate leftover funds after minumums are met
     **/
    public BigDecimal calculateLeftoverFunds(Integer month, Integer year) {
        MonthlyIncome income = monthlyIncomeRepository
                .findByMonthAndYear(month, year)
                .orElse(null);

        if (income == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalEssentials = calculateTotalEssentials(month, year);
        BigDecimal leftover = income.getAmount().subtract(totalEssentials);

        return leftover.max(BigDecimal.ZERO); // Don't return negative
    }

    /**
     generate debt payment recommendations based on utilization
     **/
    public List<DebtRecommendation> generateRecommendations(Integer month, Integer year) {
        BigDecimal leftover = calculateLeftoverFunds(month, year);
        List<DebtAccount> debts = debtAccountRepository.findByIsActiveTrue();

        if (leftover.compareTo(BigDecimal.ZERO) <= 0 || debts.isEmpty()) {
            return new ArrayList<>();
        }

        // first calculate total amount of minimums
        BigDecimal totalMinimums = debts.stream()
                .map(DebtAccount::getMinimumPayment)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // if minimums cannot be met, just return minimums
        if (leftover.compareTo(totalMinimums) < 0) {
            return debts.stream()
                    .map(debt -> new DebtRecommendation(
                            debt.getId(),
                            debt.getName(),
                            debt.getMinimumPayment(),
                            0,
                            "Minimum payment only",
                            null
                    ))
                    .toList();
        }

        // handling extra/leftover funds
        BigDecimal extraFunds = leftover.subtract(totalMinimums);

        // sort debts by highest utilization
        List<DebtAccount> sortedDebts = debts.stream()
                .sorted(Comparator.comparing(this::calculateUtilization).reversed())
                .toList();

        List<DebtRecommendation> recommendations = new ArrayList<>();
        int priority = 1;

        for (DebtAccount debt : sortedDebts) {
            BigDecimal utilization = calculateUtilization(debt);
            BigDecimal recommendedPayment = debt.getMinimumPayment();
            String reason = "Minimum payment";

            // allocate extra funds
            if (extraFunds.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal extraAllocation = extraFunds.min(
                        debt.getCurrentBalance().subtract(debt.getMinimumPayment())
                );
                recommendedPayment = recommendedPayment.add(extraAllocation);
                extraFunds = extraFunds.subtract(extraAllocation);
                reason = String.format("High utilization (%.1f%%)",
                        utilization.multiply(BigDecimal.valueOf(100)));
            }

            recommendations.add(new DebtRecommendation(
                    debt.getId(),
                    debt.getName(),
                    recommendedPayment,
                    priority++,
                    reason,
                    utilization
            ));
        }

        return recommendations;
    }

    /**
     calculate utilization ratio for debt amount
     **/
    private BigDecimal calculateUtilization(DebtAccount debt) {
        if (debt.getCreditLimit() == null ||
                debt.getCreditLimit().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO; // Loans don't have utilization
        }

        return debt.getCurrentBalance()
                .divide(debt.getCreditLimit(), 4, RoundingMode.HALF_UP);
    }

    /**
    inner class for budgeting recommendation results
     **/
    public static class DebtRecommendation {
        private Long debtAccountId;
        private String accountName;
        private BigDecimal recommendedPayment;
        private Integer priorityRank;
        private String reason;
        private BigDecimal utilization;

        public DebtRecommendation(Long debtAccountId, String accountName,
                                  BigDecimal recommendedPayment, Integer priorityRank,
                                  String reason, BigDecimal utilization) {
            this.debtAccountId = debtAccountId;
            this.accountName = accountName;
            this.recommendedPayment = recommendedPayment;
            this.priorityRank = priorityRank;
            this.reason = reason;
            this.utilization = utilization;
        }

        // Getters
        public Long getDebtAccountId() {return debtAccountId;}
        public String getAccountName() {return accountName;}
        public BigDecimal getRecommendedPayment() {return recommendedPayment;}
        public Integer getPriorityRank() {return priorityRank;}
        public String getReason() {return reason;}
        public BigDecimal getUtilization() {return utilization;}
    }
}