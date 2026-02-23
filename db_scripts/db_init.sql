CREATE TYPE PROJECTSTATUS AS ENUM (
    'planning', 
    'active',       
    'completed',    
    'cancelled',    
    'on_hold'       
);

CREATE TYPE POLICY AS ENUM (
    'salary',           
    'seniority',        
    'calendar_bonus',   
    'one_time_bonus'
);

-- Должности
CREATE TABLE Positions (
    PositionId    BIGSERIAL PRIMARY KEY,
    Name          VARCHAR(150) NOT NULL
);

CREATE INDEX idx_positions_name ON Positions(Name);

-- Сотрудники
CREATE TABLE Employees (
    EmployeeId    BIGSERIAL PRIMARY KEY,
    FullName      VARCHAR(255) NOT NULL,
    Address       TEXT,
    BirthDate     DATE NOT NULL,
    Degree        TEXT,
    HireDate      DATE NOT NULL,
    LeaveDate     DATE,
    Email         VARCHAR(255) UNIQUE,
    PhoneNumber   VARCHAR(50),
    
    CONSTRAINT employees_hire_date_check CHECK (HireDate <= CURRENT_DATE),
    CONSTRAINT employees_birth_date_check CHECK (BirthDate < CURRENT_DATE),
    CONSTRAINT employees_leave_date_check CHECK (LeaveDate IS NULL OR (LeaveDate >= HireDate AND LeaveDate <= CURRENT_DATE))
);

CREATE INDEX idx_employees_hire_date ON Employees(HireDate);
CREATE INDEX idx_employees_birth_date ON Employees(BirthDate);
CREATE INDEX idx_employees_search ON Employees USING gin(to_tsvector('russian', FullName));
-- Проекты
CREATE TABLE Projects (
    ProjectId       BIGSERIAL PRIMARY KEY,
    ProjectName     VARCHAR(200) UNIQUE NOT NULL,
    StartDate       DATE NOT NULL,
    EndDate         DATE,
    ProjectStatus   PROJECTSTATUS DEFAULT 'planning',
    
    CONSTRAINT projects_dates_check CHECK (EndDate IS NULL OR EndDate >= StartDate)
);

CREATE INDEX idx_projects_active ON Projects(ProjectId) WHERE ProjectStatus = 'active';
CREATE INDEX idx_projects_status ON Projects(ProjectStatus);
CREATE INDEX idx_projects_dates ON Projects(StartDate, EndDate);
CREATE INDEX idx_projects_search ON Projects USING gin(to_tsvector('english', ProjectName));

-- Принадлежность сотрудников к должностям и проектам
CREATE TABLE Assignments (
    AssignmentId    BIGSERIAL PRIMARY KEY,
    EmployeeId      BIGINT NOT NULL REFERENCES Employees(EmployeeId),
    ProjectId       BIGINT NOT NULL REFERENCES Projects(ProjectId),
    PositionId      BIGINT NOT NULL REFERENCES Positions(PositionId),
    WeeklyHours     INT DEFAULT 40,
    StartDate       DATE NOT NULL,
    EndDate         DATE,
    
    CONSTRAINT assignments_dates_check CHECK (EndDate IS NULL OR EndDate >= StartDate),
    CONSTRAINT assignments_weekly_hours_check CHECK (WeeklyHours > 0 AND WeeklyHours <= 168)
);

CREATE INDEX idx_assignments_employee ON Assignments(EmployeeId) WHERE EndDate IS NULL;
CREATE INDEX idx_assignments_project ON Assignments(ProjectId) WHERE EndDate IS NULL;
CREATE INDEX idx_assignments_position ON Assignments(PositionId) WHERE EndDate IS NULL;
CREATE INDEX idx_assignments_active ON Assignments(EmployeeId, ProjectId) WHERE EndDate IS NULL;
CREATE INDEX idx_assignments_dates ON Assignments(StartDate, EndDate);

-- Политики выплат
CREATE TABLE PaymentPolicies (
    PolicyId            BIGSERIAL PRIMARY KEY,
    PolicyType          POLICY NOT NULL,
    IsFixed             BOOLEAN NOT NULL DEFAULT TRUE,
    Amount              NUMERIC(12, 2) NOT NULL,
    Info                JSONB,
    
    CONSTRAINT payment_policies_amount_check CHECK (Amount >= 0)
);

-- Выплаты
CREATE TABLE Payments (
    PaymentId           BIGSERIAL PRIMARY KEY,
    AssignmentId        BIGINT REFERENCES Assignments(AssignmentId),
    PolicyId            BIGINT REFERENCES PaymentPolicies(PolicyId),
    Amount              NUMERIC(12, 2) NOT NULL,
    PeriodStart         DATE,
    PeriodEnd           DATE,
    IsTransactioned     BOOLEAN DEFAULT FALSE,
    PaymentDate         DATE,
    
    CONSTRAINT payments_amount_check CHECK (Amount >= 0),
    CONSTRAINT payments_period_check CHECK (PeriodEnd IS NULL OR PeriodStart IS NULL OR PeriodEnd >= PeriodStart)
);

CREATE INDEX idx_payments_assignment ON Payments(AssignmentId);
CREATE INDEX idx_payments_policy ON Payments(PolicyId);
CREATE INDEX idx_payments_period ON Payments(PeriodStart, PeriodEnd) WHERE PeriodStart IS NOT NULL;
CREATE INDEX idx_payments_transactioned ON Payments(IsTransactioned) WHERE IsTransactioned = FALSE;


-- Вьюшки и триггеры для дальнейшего удобства

-- Текущие назначения сотрудников
CREATE VIEW v_current_assignments AS
SELECT 
    e.EmployeeId,
    e.FullName,
    p.Name as PositionName,
    pr.ProjectId,
    pr.ProjectName,
    a.WeeklyHours,
    a.StartDate
FROM Employees e
JOIN Assignments a ON e.EmployeeId = a.EmployeeId
JOIN Projects pr ON a.ProjectId = pr.ProjectId
JOIN Positions p ON a.PositionId = p.PositionId
WHERE a.EndDate IS NULL 
  AND e.LeaveDate IS NULL
  AND pr.ProjectStatus = 'active';


-- Стаж сотрудников
CREATE VIEW v_employee_seniority AS
SELECT 
    EmployeeId,
    FullName,
    HireDate,
    EXTRACT(YEAR FROM AGE(CURRENT_DATE, HireDate)) as YearsOfWork,
    EXTRACT(MONTH FROM AGE(CURRENT_DATE, HireDate)) as MonthsOfWork
FROM Employees
WHERE LeaveDate IS NULL;