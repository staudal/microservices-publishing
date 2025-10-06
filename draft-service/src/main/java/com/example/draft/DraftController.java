package com.example.draft;

import com.example.shared.model.Draft;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/drafts")
public class DraftController {

    private final DraftService draftService;

    public DraftController(DraftService draftService) {
        this.draftService = draftService;
    }

    @PostMapping
    public Draft create(@RequestBody Draft draft) {
        return draftService.create(draft);
    }

    @GetMapping
    public List<Draft> readAll() {
        return draftService.readAll();
    }

    @GetMapping("/{id}")
    public Draft readById(@PathVariable Long id) {
        return draftService.readById(id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        draftService.delete(id);
    }
}
