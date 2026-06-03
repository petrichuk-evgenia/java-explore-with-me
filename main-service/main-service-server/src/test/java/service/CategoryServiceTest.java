package service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
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
@DisplayName("Тесты для CategoryService")
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
    @DisplayName("Инициализация тестовых данных")
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
    @DisplayName("Создание категории - успешный сценарий")
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
    @DisplayName("Создание категории - ошибка при дублировании имени")
    void createCategory_shouldThrowConflictException_whenCategoryNameExists() {
        when(categoryRepository.existsByName("Test Category")).thenReturn(true);

        assertThrows(ConflictException.class, () -> categoryService.createCategory(testNewCategoryDto));

        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    @DisplayName("Создание категории - ошибка при нарушении целостности данных")
    void createCategory_shouldThrowConflictException_whenDataIntegrityViolation() {
        when(categoryRepository.existsByName("Test Category")).thenReturn(false);
        when(categoryMapper.toEntity(testNewCategoryDto)).thenReturn(testCategory);
        when(categoryRepository.save(any(Category.class))).thenThrow(DataIntegrityViolationException.class);

        assertThrows(ConflictException.class, () -> categoryService.createCategory(testNewCategoryDto));

        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    @DisplayName("Получение категории по id - успешный сценарий")
    void getCategoryById_shouldReturnCategory_whenExists() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(categoryMapper.toDto(testCategory)).thenReturn(testCategoryDto);

        CategoryDto result = categoryService.getCategoryById(1L);

        assertNotNull(result);
        assertEquals(testCategoryDto.getId(), result.getId());
        assertEquals(testCategoryDto.getName(), result.getName());
    }

    @Test
    @DisplayName("Получение категории по id - категория не найдена")
    void getCategoryById_shouldThrowNotFoundException_whenNotExists() {
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> categoryService.getCategoryById(999L));
    }

    @Test
    @DisplayName("Получение списка категорий - успешный сценарий")
    void getCategories_shouldReturnListOfCategories() {
        Page<Category> categoryPage = new PageImpl<>(List.of(testCategory));
        when(categoryRepository.findAll(any(PageRequest.class))).thenReturn(categoryPage);
        when(categoryMapper.toDto(testCategory)).thenReturn(testCategoryDto);

        List<CategoryDto> result = categoryService.getCategories(0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testCategoryDto.getId(), result.get(0).getId());
        assertEquals(testCategoryDto.getName(), result.get(0).getName());
    }

    @Test
    @DisplayName("Получение списка категорий - пустой список")
    void getCategories_shouldReturnEmptyList_whenNoCategories() {
        Page<Category> emptyPage = new PageImpl<>(List.of());
        when(categoryRepository.findAll(any(PageRequest.class))).thenReturn(emptyPage);

        List<CategoryDto> result = categoryService.getCategories(0, 10);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Обновление категории - успешный сценарий")
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
        assertEquals(1L, result.getId());
    }

    @Test
    @DisplayName("Обновление категории - ошибка при дублировании имени")
    void updateCategory_shouldThrowConflictException_whenNameAlreadyExists() {
        CategoryDto updateDto = CategoryDto.builder()
                .name("Existing Category")
                .build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.existsByName("Existing Category")).thenReturn(true);

        assertThrows(ConflictException.class, () -> categoryService.updateCategory(1L, updateDto));

        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    @DisplayName("Обновление категории - категория не найдена")
    void updateCategory_shouldThrowNotFoundException_whenCategoryNotFound() {
        CategoryDto updateDto = CategoryDto.builder()
                .name("Updated Category")
                .build();

        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> categoryService.updateCategory(999L, updateDto));

        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    @DisplayName("Удаление категории - успешный сценарий")
    void deleteCategory_shouldDeleteCategory_whenNoEvents() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(eventRepository.existsByCategoryId(1L)).thenReturn(false);

        assertDoesNotThrow(() -> categoryService.deleteCategory(1L));

        verify(categoryRepository, times(1)).delete(testCategory);
    }

    @Test
    @DisplayName("Удаление категории - ошибка при наличии событий")
    void deleteCategory_shouldThrowConflictException_whenCategoryHasEvents() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(eventRepository.existsByCategoryId(1L)).thenReturn(true);

        ConflictException exception = assertThrows(ConflictException.class,
                () -> categoryService.deleteCategory(1L));

        assertTrue(exception.getMessage().contains("not empty"));
        verify(categoryRepository, never()).delete(any(Category.class));
    }

    @Test
    @DisplayName("Удаление категории - категория не найдена")
    void deleteCategory_shouldThrowNotFoundException_whenCategoryNotFound() {
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> categoryService.deleteCategory(999L));

        verify(categoryRepository, never()).delete(any(Category.class));
    }
}