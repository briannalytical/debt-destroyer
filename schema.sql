-- ============================================
-- INCOME
-- ============================================
CREATE TABLE monthly_income (
    id SERIAL PRIMARY KEY,
    month INTEGER NOT NULL CHECK (month BETWEEN 1 AND 12),
    year INTEGER NOT NULL CHECK (year >= 2024),
    amount DECIMAL(10, 2) NOT NULL CHECK (amount >= 0),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(month, year)
);

-- ============================================
-- EXPENSES
-- ============================================

-- Expense category enum (what the expense is)
CREATE TYPE essential_expense_category_enum AS ENUM (
    'rent',
    'car_payment',
    'internet',
    'groceries',
    'electric',
    'gas',
	'water',
    'phone',
    'subscriptions',
    'other'
);

-- Expense type enum (how it's paid)
CREATE TYPE expense_type_enum AS ENUM (
    'fixed',
    'variable'
);

-- Main expenses table (both fixed and variable)
CREATE TABLE expenses (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    category essential_expense_category_enum NOT NULL,
    expense_type expense_type_enum NOT NULL,
    fixed_amount DECIMAL(10, 2), -- Only populated for fixed expenses
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    -- Constraint: fixed expenses must have fixed_amount, variable must not
    CHECK (
        (expense_type = 'fixed' AND fixed_amount IS NOT NULL) OR
        (expense_type = 'variable' AND fixed_amount IS NULL)
    )
);

-- Monthly amounts for variable expenses
CREATE TABLE variable_expense_amounts (
    id SERIAL PRIMARY KEY,
    expense_id INTEGER NOT NULL REFERENCES expenses(id) ON DELETE CASCADE,
    month INTEGER NOT NULL CHECK (month BETWEEN 1 AND 12),
    year INTEGER NOT NULL CHECK (year >= 2024),
    amount DECIMAL(10, 2) NOT NULL CHECK (amount >= 0),
    notes VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(expense_id, month, year)
);

-- ============================================
-- IRREGULAR EXPENSES (Annual, Quarterly, etc.)
-- ============================================

CREATE TYPE frequency_enum AS ENUM (
    'annual',
    'semi_annual',
    'quarterly'
);

CREATE TABLE irregular_expenses (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL CHECK (amount >= 0),
    frequency frequency_enum NOT NULL,
    next_due_date DATE NOT NULL,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- DEBT ACCOUNTS
-- ============================================

CREATE TYPE account_type_enum AS ENUM (
    'credit_card',
    'personal_loan',
    'student_loan',
    'other'
);

CREATE TABLE debt_accounts (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    account_type account_type_enum NOT NULL,
    current_balance DECIMAL(10, 2) NOT NULL CHECK (current_balance >= 0),
    credit_limit DECIMAL(10, 2), -- NULL for non-revolving debt (loans)
    minimum_payment DECIMAL(10, 2) NOT NULL CHECK (minimum_payment >= 0),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- DEBT PAYMENT HISTORY
-- ============================================

CREATE TABLE debt_payments (
    id SERIAL PRIMARY KEY,
    debt_account_id INTEGER NOT NULL REFERENCES debt_accounts(id) ON DELETE CASCADE,
    month INTEGER NOT NULL CHECK (month BETWEEN 1 AND 12),
    year INTEGER NOT NULL CHECK (year >= 2024),
    amount_paid DECIMAL(10, 2) NOT NULL CHECK (amount_paid >= 0),
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(debt_account_id, month, year)
);

-- ============================================
-- INDEXES
-- ============================================

CREATE INDEX idx_monthly_income_date ON monthly_income(year, month);
CREATE INDEX idx_expenses_active ON expenses(id) WHERE is_active = true;
CREATE INDEX idx_variable_amounts_date ON variable_expense_amounts(expense_id, year, month);
CREATE INDEX idx_debt_accounts_active ON debt_accounts(id) WHERE is_active = true;
CREATE INDEX idx_debt_payments_date ON debt_payments(year, month);