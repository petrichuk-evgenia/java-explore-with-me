package service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.practicum.dto.CategoryDto;
import ru.practicum.dto.NewCategoryDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.CategoryMapper;
import ru.practicum.model.Category;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.service.impl.CategoryServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category testCategory;
    private CategoryDto testCategoryDto;
    private NewCategoryDto testNewCategoryDto;

    @BeforeEach
    void setUp() {
        testCategory = Category.builder()
                .id(1L)
                .name("Test Category")
                .build();

        testCategoryDto = CategoryDto.builder()
                .id(1L)
                .name("Test Category")
                .build();

        testNewCategoryDto = new NewCategoryDto();
        testNewCategoryDto.setName("Test Category");
    }

    @Test
    void createCategory_shouldSaveAndReturnCategory() {
        when(categoryRepository.existsByName("Test Category")).thenReturn(false);
        when(categoryMapper.toEntity(testNewCategoryDto)).thenReturn(testCategory);
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);
        when(categoryMapper.toDto(testCategory)).thenReturn(testCategoryDto);

        CategoryDto result = categoryService.createCategory(testNewCategoryDto);

        assertNotNull(result);
        assertEquals(testCategoryDto.getId(), result.getId());
        assertEquals(testCategoryDto.getName(), result.getName());

        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void createCategory_shouldThrowConflictException_whenCategoryNameExists() {
        when(categoryRepository.existsByName("Test Category")).thenReturn(true);

        assertThrows(ConflictException.class, () -> categoryService.createCategory(testNewCategoryDto));

        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void getCategoryById_shouldReturnCategory_whenExists() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(categoryMapper.toDto(testCategory)).thenReturn(testCategoryDto);

        CategoryDto result = categoryService.getCategoryById(1L);

        assertNotNull(result);
        assertEquals(testCategoryDto.getId(), result.getId());
    }

    @Test
    void getCategoryById_shouldThrowNotFoundException_whenNotExists() {
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> categoryService.getCategoryById(999L));
    }

    @Test
    void getCategories_shouldReturnListOfCategories() {
        Page<Category> categoryPage = new PageImpl<>(List.of(testCategory));
        when(categoryRepository.findAll(any(PageRequest.class))).thenReturn(categoryPage);
        when(categoryMapper.toDto(testCategory)).thenReturn(testCategoryDto);

        List<CategoryDto> result = categoryService.getCategories(0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testCategoryDto.getId(), result.get(0).getId());
    }

    @Test
    void updateCategory_shouldUpdateAndReturnCategory() {
        CategoryDto updateDto = CategoryDto.builder()
                .name("Updated Category")
                .build();

        Category updatedCategory = Category.builder()
                .id(1L)
                .name("Updated Category")
                .build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.existsByName("Updated Category")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(updatedCategory);
        when(categoryMapper.toDto(updatedCategory)).thenReturn(CategoryDto.builder()
                .id(1L)
                .name("Updated Category")
                .build());

        CategoryDto result = categoryService.updateCategory(1L, updateDto);

        assertNotNull(result);
        assertEquals("Updated Category", result.getName());
    }

    @Test
    void deleteCategory_shouldDeleteCategory_whenNoEvents() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(eventRepository.existsByCategoryId(1L)).thenReturn(false);

        assertDoesNotThrow(() -> categoryService.deleteCategory(1L));

        verify(categoryRepository, times(1)).delete(testCategory);
    }

    @Test
    void deleteCategory_shouldThrowConflictException_whenCategoryHasEvents() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(eventRepository.existsByCategoryId(1L)).thenReturn(true);

        assertThrows(ConflictException.class, () -> categoryService.deleteCategory(1L));

        verify(categoryRepository, never()).delete(any(Category.class));
    }
}