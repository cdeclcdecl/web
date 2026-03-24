package ru.msu.web.dao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.msu.web.AbstractDaoTest;
import ru.msu.web.entity.Position;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class PositionDaoTest extends AbstractDaoTest {

    @Autowired
    private PositionDao positionDao;

    // findAll
    @Test
    void findAll_returnsAllPositions() {
        List<Position> all = positionDao.findAll();
        assertEquals(31, all.size());
    }

    // findById
    @Test
    void findById_found() {
        Position pos = positionDao.findById(1L);
        assertNotNull(pos);
        assertEquals("Генеральный директор", pos.getName());
    }

    @Test
    void findById_notFound() {
        assertNull(positionDao.findById(9999L));
    }

    // findByName
    @Test
    void findByName_found() {
        Optional<Position> result = positionDao.findByName("QA-инженер");
        assertTrue(result.isPresent());
        assertEquals("QA-инженер", result.get().getName());
    }

    @Test
    void findByName_notFound() {
        assertTrue(positionDao.findByName("Несуществующая").isEmpty());
    }

    // searchByName
    @Test
    void searchByName_found() {
        List<Position> result = positionDao.searchByName("Разработчик");
        assertEquals(3, result.size());
    }

    @Test
    void searchByName_notFound() {
        assertTrue(positionDao.searchByName("xyz_не_бывает").isEmpty());
    }

    // save
    @Test
    void save_createsNewPosition() {
        Position pos = new Position();
        pos.setName("Новая должность");
        Position saved = positionDao.save(pos);
        assertNotNull(saved.getPositionId());
        assertEquals("Новая должность", saved.getName());

        Position found = positionDao.findById(saved.getPositionId());
        assertNotNull(found);
        assertEquals("Новая должность", found.getName());
    }
}
