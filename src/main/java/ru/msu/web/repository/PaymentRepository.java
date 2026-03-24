package ru.msu.web.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.msu.web.entity.Payment;
import ru.msu.web.entity.PolicyType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByTransactioned(boolean transactioned);

    List<Payment> findByPaymentDateBetween(LocalDate from, LocalDate to);

    List<Payment> findByAmountBetween(BigDecimal min, BigDecimal max);

    @Query("SELECT p FROM Payment p WHERE p.assignment.employee.employeeId = :employeeId")
    List<Payment> findByEmployeeId(@Param("employeeId") Long employeeId);

    @Query("SELECT p FROM Payment p WHERE p.policy.policyType = :policyType")
    List<Payment> findByPolicyType(@Param("policyType") PolicyType policyType);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.assignment.assignmentId = :assignmentId")
    BigDecimal sumByAssignmentId(@Param("assignmentId") Long assignmentId);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p " +
           "WHERE p.assignment.employee.employeeId = :employeeId " +
           "AND p.periodStart >= :periodStart AND p.periodEnd <= :periodEnd")
    BigDecimal sumByEmployeeIdAndPeriod(
            @Param("employeeId") Long employeeId,
            @Param("periodStart") LocalDate periodStart,
            @Param("periodEnd") LocalDate periodEnd);
}
