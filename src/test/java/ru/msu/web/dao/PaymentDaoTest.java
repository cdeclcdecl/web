package ru.msu.web.dao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.msu.web.AbstractDaoTest;
import ru.msu.web.entity.Payment;
import ru.msu.web.entity.PolicyType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PaymentDaoTest extends AbstractDaoTest {

    @Autowired
    private PaymentDao paymentDao;

    // findAll
    @Test
    void findAll_returnsAllPayments() {
        List<Payment> all = paymentDao.findAll();
        assertTrue(all.size() > 80);
    }

    // findByEmployee
    @Test
    void findByEmployee_found() {
        List<Payment> result = paymentDao.findByEmployee(1L);
        assertTrue(result.size() >= 6);
    }

    @Test
    void findByEmployee_notFound() {
        assertTrue(paymentDao.findByEmployee(9999L).isEmpty());
    }

    // findByDateRange
    @Test
    void findByDateRange_found() {
        List<Payment> result = paymentDao.findByDateRange(
                LocalDate.of(2024, 2, 10), LocalDate.of(2024, 2, 10));
        assertFalse(result.isEmpty());
        assertTrue(result.stream().allMatch(Payment::isPaid));
    }

    @Test
    void findByDateRange_notFound() {
        assertTrue(paymentDao.findByDateRange(
                LocalDate.of(2030, 1, 1), LocalDate.of(2030, 12, 31)).isEmpty());
    }

    // findByAmountRange
    @Test
    void findByAmountRange_found() {
        List<Payment> result = paymentDao.findByAmountRange(
                new BigDecimal("350000"), new BigDecimal("500000"));
        assertFalse(result.isEmpty());
    }

    @Test
    void findByAmountRange_notFound() {
        assertTrue(paymentDao.findByAmountRange(
                new BigDecimal("999999"), new BigDecimal("9999999")).isEmpty());
    }

    // findByPolicyType
    @Test
    void findByPolicyType_found() {
        List<Payment> result = paymentDao.findByPolicyType(PolicyType.salary);
        assertTrue(result.size() > 50);
    }

    // findUnpaid
    @Test
    void findUnpaid_returnsUnpaidPayments() {
        List<Payment> unpaid = paymentDao.findUnpaid();
        assertEquals(3, unpaid.size());
        assertTrue(unpaid.stream().noneMatch(Payment::isPaid));
    }

    // getTotalByEmployeeAndPeriod
    @Test
    void getTotalByEmployeeAndPeriod_found() {
        BigDecimal total = paymentDao.getTotalByEmployeeAndPeriod(
                1L, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));
        assertEquals(0, new BigDecimal("500000.00").compareTo(total));
    }

    @Test
    void getTotalByEmployeeAndPeriod_notFound() {
        BigDecimal total = paymentDao.getTotalByEmployeeAndPeriod(
                1L, LocalDate.of(2030, 1, 1), LocalDate.of(2030, 12, 31));
        assertEquals(0, BigDecimal.ZERO.compareTo(total));
    }

    // getTotalByAssignment
    @Test
    void getTotalByAssignment_found() {
        BigDecimal total = paymentDao.getTotalByAssignment(1L);
        assertEquals(0, new BigDecimal("3180000.00").compareTo(total));
    }

    @Test
    void getTotalByAssignment_notFound() {
        BigDecimal total = paymentDao.getTotalByAssignment(9999L);
        assertEquals(0, BigDecimal.ZERO.compareTo(total));
    }

    // findById
    @Test
    void findById_found() {
        Payment p = paymentDao.findById(1L);
        assertNotNull(p);
        assertTrue(p.isPaid());
        assertTrue(p.isTransactioned());
    }

    // save
    @Test
    void save_createsNewPayment() {
        Payment p = new Payment();
        p.setAmount(new BigDecimal("75000.00"));
        p.setPeriodStart(LocalDate.of(2025, 1, 1));
        p.setPeriodEnd(LocalDate.of(2025, 1, 31));
        p.setTransactioned(false);
        Payment saved = paymentDao.save(p);
        assertNotNull(saved.getPaymentId());

        Payment found = paymentDao.findById(saved.getPaymentId());
        assertNotNull(found);
        assertEquals(0, new BigDecimal("75000.00").compareTo(found.getAmount()));
        assertFalse(found.isPaid());
    }
}
