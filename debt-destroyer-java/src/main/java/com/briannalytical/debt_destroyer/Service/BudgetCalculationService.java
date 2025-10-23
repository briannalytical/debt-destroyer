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
     * Calculate total fixed expenses (rent, car payment, etc.)
     */
    public BigDecimal calculateFixedExpenses() {
        List<Expense> fixedExpenses = expenseRepository
                .findByIsActiveTrueAndExpenseType(ExpenseType.FIXED);

        return fixedExpenses.stream()
                .map(Expense::getFixedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calculate total variable expenses for a specific month
     */
    public BigDecimal calculateVariableExpenses(Integer month, Integer year) {
        List<VariableExpenseAmount> variableAmounts =
                variableExpenseAmountRepository.findByMonthAndYear(month, year);

        return variableAmounts.stream()
                .map(VariableExpenseAmount::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calculate monthly sinking fund for irregular expenses
     * (e.g., $1200 annual insurance = $100/month to set aside)
     */
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
     * Calculate total essentials for a specific month
     */
    public BigDecimal calculateTotalEssentials(Integer month, Integer year) {
        BigDecimal fixed = calculateFixedExpenses();
        BigDecimal variable = calculateVariableExpenses(month, year);
        BigDecimal sinkingFund = calculateMonthlySinkingFund();

        return fixed.add(variable).add(sinkingFund);
    }

    /**
     * Calculate leftover funds after essentials
     */
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
     * Generate debt payment recommendations based on utilization
     */
    public List<DebtRecommendation> generateRecommendations(Integer month, Integer year) {
        BigDecimal leftover = calculateLeftoverFunds(month, year);
        List<DebtAccount> debts = debtAccountRepository.findByIsActiveTrue();

        if (leftover.compareTo(BigDecimal.ZERO) <= 0 ||