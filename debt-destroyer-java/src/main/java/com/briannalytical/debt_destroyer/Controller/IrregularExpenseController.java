package com.briannalytical.debt_destroyer.Controller;

import com.briannalytical.debt_destroyer.Model.Entity.IrregularExpense;
import com.briannalytical.debt_destroyer.Repository.IrregularExpenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/irregular-expenses")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class IrregularExpenseController {

    @Autowired
    private IrregularExpenseRepository irregularExpenseRepository;

    /**
     get all active irregular expenses
     * GET /api/irregular-expenses
     **/
    @GetMapping
    public ResponseEntity<List<IrregularExpense>> getAllIrregularExpenses() {
        List<IrregularExpense> expenses = irregularExpenseRepository.findByIsActiveTrue();
        return ResponseEntity.ok(expenses);
    }

    /**
     get irregular expense by ID
     * GET /api/irregular-expenses/{id}
     **/
    @GetMapping("/{id}")
    public ResponseEntity<IrregularExpense> getIrregularExpenseById(@PathVariable Long id) {
        return irregularExpenseRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     create new irregular expense
     * POST /api/irregular-expenses
     **/
    @PostMapping
    public ResponseEntity<IrregularExpense> createIrregularExpense(
            @RequestBody IrregularExpense expense) {
        IrregularExpense saved = irregularExpenseRepository.save(expense);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     update irregular expense
     * PUT /api/irregular-expenses/{id}
     **/
    @PutMapping("/{id}")
    public ResponseEntity<IrregularExpense> updateIrregularExpense(
            @PathVariable Long id,
            @RequestBody IrregularExpense expense) {

        if (!irregularExpenseRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        expense.setId(id);
        IrregularExpense saved = irregularExpenseRepository.save(expense);
        return ResponseEntity.ok(saved);
    }

    /**
     delete (deactivate) irregular expense
     * DELETE /api/irregular-expenses/{id}
     **/
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIrregularExpense(@PathVariable Long id) {
        return irregularExpenseRepository.findById(id)
                .map(expense -> {
                    expense.setIsActive(false);
                    irregularExpenseRepository.save(expense);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}