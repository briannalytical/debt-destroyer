package com.briannalytical.debt_destroyer.Controller;

import com.briannalytical.debt_destroyer.Model.Entity.Expense;
import com.briannalytical.debt_destroyer.Model.Entity.VariableExpenseAmount;
import com.briannalytical.debt_destroyer.Repository.ExpenseRepository;
import com.briannalytical.debt_destroyer.Repository.VariableExpenseAmountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class ExpenseController {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private VariableExpenseAmountRepository variableExpenseAmountRepository;

    /**
     get all active expenses
     * GET /api/expenses
     **/
    @GetMapping
    public ResponseEntity<List<Expense>> getAllExpenses() {
        List<Expense> expenses = expenseRepository.findByIsActiveTrue();
        return ResponseEntity.ok(expenses);
    }

    /**
     get expense by ID
     * GET /api/expenses/{id}
     **/
    @GetMapping("/{id}")
    public ResponseEntity<Expense> getExpenseById(@PathVariable Long id) {
        return expenseRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     create new expense
     * POST /api/expenses
     **/
    @PostMapping
    public ResponseEntity<Expense> createExpense(@RequestBody Expense expense) {
        Expense saved = expenseRepository.save(expense);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     update expense
     * PUT /api/expenses/{id}
     **/
    @PutMapping("/{id}")
    public ResponseEntity<Expense> updateExpense(
            @PathVariable Long id,
            @RequestBody Expense expense) {

        if (!expenseRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        expense.setId(id);
        Expense saved = expenseRepository.save(expense);
        return ResponseEntity.ok(saved);
    }

    /**
     delete (deactivate) expense
     * DELETE /api/expenses/{id}
     **/
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id) {
        return expenseRepository.findById(id)
                .map(expense -> {
                    expense.setIsActive(false);
                    expenseRepository.save(expense);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     get variable expense amounts for a specific month
     * GET /api/expenses/variable/{month}/{year}
     **/
    @GetMapping("/variable/{month}/{year}")
    public ResponseEntity<List<VariableExpenseAmount>> getVariableAmounts(
            @PathVariable Integer month,
            @PathVariable Integer year) {

        List<VariableExpenseAmount> amounts =
                variableExpenseAmountRepository.findByMonthAndYear(month, year);
        return ResponseEntity.ok(amounts);
    }

    /**
     add variable expense amount for a month
     * POST /api/expenses/variable
     **/
    @PostMapping("/variable")
    public ResponseEntity<VariableExpenseAmount> addVariableAmount(
            @RequestBody VariableExpenseAmount amount) {

        VariableExpenseAmount saved = variableExpenseAmountRepository.save(amount);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
}