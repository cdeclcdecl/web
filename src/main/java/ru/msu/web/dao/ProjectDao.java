package ru.msu.web.dao;

import org.springframework.stereotype.Service;
import ru.msu.web.entity.Project;
import ru.msu.web.entity.ProjectStatus;
import ru.msu.web.repository.ProjectRepository;

import java.time.LocalDate;
import java.util.List;

/*
 * DAO для работы с проектами
 * Поддерживает операции страницы "Список проектов" и "Информация о проекте"
 */
@Service
public class ProjectDao {

    private final ProjectRepository projectRepository;

    public ProjectDao(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    /* Все проекты */
    public List<Project> findAll() {
        return projectRepository.findAll();
    }

    /* Проекты по статусу */
    public List<Project> findByStatus(ProjectStatus status) {
        return projectRepository.findByProjectStatus(status);
    }

    /* Поиск по названию (содержание подстроки) */
    public List<Project> searchByName(String query) {
        return projectRepository.findByProjectNameContainingIgnoreCase(query);
    }

    /* Проекты по диапазону дат начала */
    public List<Project> findByStartDateRange(LocalDate from, LocalDate to) {
        return projectRepository.findByStartDateBetween(from, to);
    }

    /* Получить проект по ID. Возвращает null, если не найден */
    public Project findById(Long id) {
        return projectRepository.findById(id).orElse(null);
    }

    /* Получить проект по точному названию. Возвращает null, если не найден */
    public Project findByName(String name) {
        return projectRepository.findByProjectName(name).orElse(null);
    }

    /* Сохранить (создать или обновить) проект */
    public Project save(Project project) {
        return projectRepository.save(project);
    }
}
