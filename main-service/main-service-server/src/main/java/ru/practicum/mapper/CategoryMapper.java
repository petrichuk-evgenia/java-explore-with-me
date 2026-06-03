package ru.practicum.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.dto.NewCategoryDto;
import ru.practicum.model.Category;

@Component
public class CategoryMapper {

    public ru.practicum.dto.CategoryDto toDto(Category category) {
        return ru.practicum.dto.CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }

    public Category toEntity(NewCategoryDto dto) {
        return Category.builder()
                .name(dto.getName())
                .build();
    }
}
