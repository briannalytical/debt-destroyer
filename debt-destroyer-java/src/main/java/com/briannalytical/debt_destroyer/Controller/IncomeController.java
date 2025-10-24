package com.briannalytical.debt_destroyer.Controller;

import com.briannalytical.debt_destroyer.Model.Entity.MonthlyIncome;
import com.briannalytical.debt_destroyer.Repository.MonthlyIncomeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/income")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class IncomeController {

    @Autowired
    private MonthlyIncomeRepository incomeRepository;

    /**
     get all income records
     * GET /api/income
     **/
    @GetMapping
    public ResponseEntity<List<MonthlyIncome>> getAllIncome() {
        List<MonthlyIncome> incomes = incomeRepository.findAll();
        return ResponseEntity.ok(incomes);
    }

    /**
     get income for a specific month
     * GET /api/income/{month}/{year}
     **/
    @GetMapping("/{month}/{year}")
    public ResponseEntity<MonthlyIncome> getIncomeByMonthYear(
            @PathVariable Integer month,
            @PathVariable Integer year) {

        Optional<MonthlyIncome> income = incomeRepository.findByMonthAndYear(month, year);
        return income.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     create or update income for a month
     * POST /api/income
     **/
    @PostMapping
    public ResponseEntity<MonthlyIncome> createOrUpdateIncome(@RequestBody MonthlyIncome income) {
        // Check if income already exists for this month/year
        Optional<MonthlyIncome> existing = incomeRepository
                .findByMonthAndYear(income.getMonth(), income.getYear());

        if (existing.isPresent()) {
            // Update existing
            MonthlyIncome existingIncome = existing.get();
            existingIncome.setAmount(income.getAmount());
            MonthlyIncome saved = incomeRepository.save(existingIncome);
            return ResponseEntity.ok(saved);
        } else {
            // Create new
            MonthlyIncome saved = incomeRepository.save(income);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        }
    }

    /**
     delete income record
     * DELETE /api/income/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIncome(@PathVariable Long id) {
        if (incomeRepository.existsById(id)) {
            incomeRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}