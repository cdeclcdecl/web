package ru.msu.web.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "assignments")
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "assignmentid")
    private Long assignmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employeeid", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projectid", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "positionid", nullable = false)
    private Position position;

    @Column(name = "weeklyhours")
    private Integer weeklyHours = 40;

    @Column(name = "startdate", nullable = false)
    private LocalDate startDate;

    @Column(name = "enddate")
    private LocalDate endDate;

    @OneToMany(mappedBy = "assignment")
    private List<Payment> payments = new ArrayList<>();

    public Assignment() {}

    public boolean isActive() { return endDate == null; }

    public long getDurationMonths() {
        LocalDate end = this.endDate != null ? this.endDate : LocalDate.now();
        return ChronoUnit.MONTHS.between(this.startDate, end);
    }

    public Long getAssignmentId() { return assignmentId; }
    public void setAssignmentId(Long assignmentId) { this.assignmentId = assignmentId; }

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }

    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }

    public Position getPosition() { return position; }
    public void setPosition(Position position) { this.position = position; }

    public Integer getWeeklyHours() { return weeklyHours; }
    public void setWeeklyHours(Integer weeklyHours) { this.weeklyHours = weeklyHours; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public List<Payment> getPayments() { return payments; }
    public void setPayments(List<Payment> payments) { this.payments = payments; }
}
