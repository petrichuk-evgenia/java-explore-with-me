package ru.practicum.controller.public_api;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.CategoryDto;
import ru.practicum.service.CategoryService;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@Validated
public class PublicCategoryController {
    private final CategoryService categoryService;

    @GetMapping
    public List<CategoryDto> getCategories(@RequestParam(defaultValue = "0") @PositiveOrZero(message = "From must be positive or zero") Integer from,
                                           @RequestParam(defaultValue = "10") @Positive(message = "Size must be positive") Integer size) {
        return categoryService.getCategories(from, size);
    }

    @GetMapping("/{catId}")
    public CategoryDto getCategory(@PathVariable @Positive Long catId) {
        return categoryService.getCategoryById(catId);
    }
}