package ru.msu.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.msu.web.dao.PositionDao;
import ru.msu.web.entity.Position;

@Controller
@RequestMapping("/positions")
public class PositionController {

    private final PositionDao positionDao;

    public PositionController(PositionDao positionDao) {
        this.positionDao = positionDao;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("positions", positionDao.findAll());
        return "positions/list";
    }

    @PostMapping
    public String create(@RequestParam String name, Model model) {
        if (name == null || name.isBlank()) {
            model.addAttribute("error", "Название должности обязательно для заполнения");
            model.addAttribute("positions", positionDao.findAll());
            return "positions/list";
        }
        if (positionDao.findByName(name).isPresent()) {
            model.addAttribute("error", "Должность с таким названием уже существует");
            model.addAttribute("positions", positionDao.findAll());
            return "positions/list";
        }
        Position position = new Position();
        position.setName(name);
        positionDao.save(position);
        return "redirect:/positions";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        Position position = positionDao.findById(id);
        if (position != null) {
            positionDao.delete(position);
        }
        return "redirect:/positions";
    }
}
