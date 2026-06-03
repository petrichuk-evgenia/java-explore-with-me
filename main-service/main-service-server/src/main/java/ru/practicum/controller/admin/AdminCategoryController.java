package ru.practicum.controller.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.CategoryDto;
import ru.practicum.dto.NewCategoryDto;
import ru.practicum.service.CategoryService;

@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
@Validated
public class AdminCategoryController {
    private final CategoryService categoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto createCategory(@Valid @RequestBody NewCategoryDto dto) {
        return categoryService.createCategory(dto);
    }

    @DeleteMapping("/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable @Positive Long catId) {
        categoryService.deleteCategory(catId);
    }

    @PatchMapping("/{catId}")
    public CategoryDto updateCategory(@PathVariable @Positive Long catId,
                                      @Valid @RequestBody CategoryDto dto) {
        return categoryService.updateCategory(catId, dto);
    }
}