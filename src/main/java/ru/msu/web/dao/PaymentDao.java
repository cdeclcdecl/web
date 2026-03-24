package ru.msu.web.dao;

import org.springframework.stereotype.Service;
import ru.msu.web.entity.Payment;
import ru.msu.web.entity.PolicyType;
import ru.msu.web.repository.PaymentRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/*
 * DAO для работы с выплатами
 * Поддерживает операции страницы "История начислений" и формирование отчётов
 */
@Service
public class PaymentDao {

    private final PaymentRepository paymentRepository;

    public PaymentDao(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    /* Все выплаты */
    public List<Payment> findAll() {
        return paymentRepository.findAll();
    }

    /* Выплаты конкретного сотрудника (через назначения) */
    public List<Payment> findByEmployee(Long employeeId) {
        return paymentRepository.findByEmployeeId(employeeId);
    }

    /* Выплаты по диапазону дат выплаты */
    public List<Payment> findByDateRange(LocalDate from, LocalDate to) {
        return paymentRepository.findByPaymentDateBetween(from, to);
    }

    /* Выплаты по диапазону суммы */
    public List<Payment> findByAmountRange(BigDecimal min, BigDecimal max) {
        return paymentRepository.findByAmountBetween(min, max);
    }

    /* Выплаты по типу политики */
    public List<Payment> findByPolicyType(PolicyType type) {
        return paymentRepository.findByPolicyType(type);
    }

    /* Невыплаченные суммы */
    public List<Payment> findUnpaid() {
        return paymentRepository.findByTransactioned(false);
    }

    /* Общая сумма выплат сотруднику за период (по периоду начисления) */
    public BigDecimal getTotalByEmployeeAndPeriod(Long employeeId, LocalDate from, LocalDate to) {
        return paymentRepository.sumByEmployeeIdAndPeriod(employeeId, from, to);
    }

    /* Общая сумма выплат по назначению */
    public BigDecimal getTotalByAssignment(Long assignmentId) {
        return paymentRepository.sumByAssignmentId(assignmentId);
    }

    /* Получить выплату по ID */
    public Payment findById(Long id) {
        return paymentRepository.findById(id).orElse(null);
    }

    /* Сохранить (создать или обновить) выплату */
    public Payment save(Payment payment) {
        return paymentRepository.save(payment);
    }
}
