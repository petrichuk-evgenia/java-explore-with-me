package ru.practicum.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.CategoryDto;
import ru.practicum.dto.NewCategoryDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.CategoryMapper;
import ru.practicum.model.Category;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.service.CategoryService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false)
    public CategoryDto createCategory(NewCategoryDto dto) {
        if (categoryRepository.existsByName(dto.getName())) {
            throw new ConflictException("Category with name " + dto.getName() + " already exists");
        }

        Category category = categoryMapper.toEntity(dto);
        try {
            return categoryMapper.toDto(categoryRepository.save(category));
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Category with name " + dto.getName() + " already exists");
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false)
    public void deleteCategory(Long catId) {
        Category category = getCategoryEntity(catId);

        boolean hasEvents = eventRepository.existsByCategoryId(catId);
        if (hasEvents) {
            throw new ConflictException("The category is not empty");
        }

        categoryRepository.delete(category);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false)
    public CategoryDto updateCategory(Long catId, CategoryDto dto) {
        Category category = getCategoryEntity(catId);

        if (!category.getName().equals(dto.getName()) && categoryRepository.existsByName(dto.getName())) {
            throw new ConflictException("Category with name " + dto.getName() + " already exists");
        }

        category.setName(dto.getName());
        return categoryMapper.toDto(categoryRepository.save(category));
    }

    @Override
    public List<CategoryDto> getCategories(Integer from, Integer size) {
        return categoryRepository.findAll(PageRequest.of(from / size, size))
                .stream()
                .map(categoryMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDto getCategoryById(Long catId) {
        return categoryMapper.toDto(getCategoryEntity(catId));
    }

    private Category getCategoryEntity(Long catId) {
        return categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Category with id=" + catId + " was not found"));
    }
}