package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.MainApplication;
import ru.practicum.dto.CategoryDto;
import ru.practicum.dto.NewCategoryDto;
import ru.practicum.service.CategoryService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = MainApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Тесты для AdminCategoryController")
class AdminCategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CategoryService categoryService;

    @Test
    @DisplayName("Создание категории - успешный сценарий")
    void createCategory_shouldReturnCreatedCategory() throws Exception {
        NewCategoryDto request = new NewCategoryDto();
        request.setName("Test Category");

        CategoryDto response = CategoryDto.builder()
                .id(1L)
                .name("Test Category")
                .build();

        when(categoryService.createCategory(any(NewCategoryDto.class))).thenReturn(response);

        mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Category"));
    }

    @Test
    @DisplayName("Создание категории - ошибка валидации при пустом имени")
    void createCategory_shouldReturnBadRequest_whenNameIsBlank() throws Exception {
        NewCategoryDto request = new NewCategoryDto();
        request.setName("");

        mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Создание категории - ошибка валидации при null имени")
    void createCategory_shouldReturnBadRequest_whenNameIsNull() throws Exception {
        NewCategoryDto request = new NewCategoryDto();
        request.setName(null);

        mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Создание категории - ошибка валидации при слишком длинном имени")
    void createCategory_shouldReturnBadRequest_whenNameTooLong() throws Exception {
        NewCategoryDto request = new NewCategoryDto();
        request.setName("A".repeat(60));

        mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Обновление категории - успешный сценарий")
    void updateCategory_shouldReturnUpdatedCategory() throws Exception {
        CategoryDto request = CategoryDto.builder()
                .name("Updated Category")
                .build();

        CategoryDto response = CategoryDto.builder()
                .id(1L)
                .name("Updated Category")
                .build();

        when(categoryService.updateCategory(eq(1L), any(CategoryDto.class))).thenReturn(response);

        mockMvc.perform(patch("/admin/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Updated Category"));
    }

    @Test
    @DisplayName("Обновление категории - ошибка валидации при пустом имени")
    void updateCategory_shouldReturnBadRequest_whenNameIsBlank() throws Exception {
        CategoryDto request = CategoryDto.builder()
                .name("")
                .build();

        mockMvc.perform(patch("/admin/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Удаление категории - успешный сценарий")
    void deleteCategory_shouldReturnNoContent() throws Exception {
        doNothing().when(categoryService).deleteCategory(1L);

        mockMvc.perform(delete("/admin/categories/1"))
                .andExpect(status().isNoContent());

        verify(categoryService, times(1)).deleteCategory(1L);
    }

    @Test
    @DisplayName("Удаление категории - ошибка при невалидном id")
    void deleteCategory_shouldReturnBadRequest_whenInvalidId() throws Exception {
        mockMvc.perform(delete("/admin/categories/invalid"))
                .andExpect(status().isBadRequest());
    }
}