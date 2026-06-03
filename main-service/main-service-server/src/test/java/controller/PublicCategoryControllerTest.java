package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.MainApplication;
import ru.practicum.dto.CategoryDto;
import ru.practicum.exception.NotFoundException;
import ru.practicum.service.CategoryService;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = MainApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PublicCategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CategoryService categoryService;

    @Test
    void getCategories_shouldReturnListOfCategories() throws Exception {
        List<CategoryDto> categories = List.of(
                CategoryDto.builder().id(1L).name("Category 1").build(),
                CategoryDto.builder().id(2L).name("Category 2").build()
        );

        when(categoryService.getCategories(0, 10)).thenReturn(categories);

        mockMvc.perform(get("/categories")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Category 1"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Category 2"));
    }

    @Test
    void getCategories_shouldReturnEmptyList_whenNoCategories() throws Exception {
        when(categoryService.getCategories(0, 10)).thenReturn(List.of());

        mockMvc.perform(get("/categories")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getCategories_shouldUseDefaultValues_whenParamsNotProvided() throws Exception {
        List<CategoryDto> categories = List.of(
                CategoryDto.builder().id(1L).name("Category 1").build()
        );

        when(categoryService.getCategories(0, 10)).thenReturn(categories);

        mockMvc.perform(get("/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getCategory_shouldReturnCategory_whenExists() throws Exception {
        CategoryDto category = CategoryDto.builder()
                .id(1L)
                .name("Test Category")
                .build();

        when(categoryService.getCategoryById(1L)).thenReturn(category);

        mockMvc.perform(get("/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Category"));
    }

    @Test
    void getCategory_shouldReturnNotFound_whenCategoryDoesNotExist() throws Exception {
        when(categoryService.getCategoryById(999L))
                .thenThrow(new NotFoundException("Category with id=999 was not found"));

        mockMvc.perform(get("/categories/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("NOT_FOUND"));
    }

    @Test
    void getCategory_shouldReturnBadRequest_whenInvalidId() throws Exception {
        mockMvc.perform(get("/categories/invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.reason").value("Incorrectly made request."));
    }
}