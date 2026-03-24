package ru.msu.web.dao;

import org.springframework.stereotype.Service;
import ru.msu.web.entity.Position;
import ru.msu.web.repository.PositionRepository;

import java.util.List;
import java.util.Optional;

@Service
public class PositionDao {

    private final PositionRepository positionRepository;

    public PositionDao(PositionRepository positionRepository) {
        this.positionRepository = positionRepository;
    }

    public List<Position> findAll() {
        return positionRepository.findAll();
    }

    public Position findById(Long id) {
        return positionRepository.findById(id).orElse(null);
    }

    public Optional<Position> findByName(String name) {
        return positionRepository.findByNameIgnoreCase(name);
    }

    public List<Position> searchByName(String fragment) {
        return positionRepository.findByNameContainingIgnoreCase(fragment);
    }

    public Position save(Position position) {
        return positionRepository.save(position);
    }

    public void delete(Position position) {
        positionRepository.delete(position);
    }
}
