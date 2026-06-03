package integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.MainApplication;
import ru.practicum.dto.NewCategoryDto;
import ru.practicum.dto.NewUserRequest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = MainApplication.class)
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class MainServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createAndGetCategory_shouldWork() throws Exception {
        NewCategoryDto categoryDto = new NewCategoryDto();
        categoryDto.setName("Integration Test Category");

        String categoryResponse = mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Integration Test Category"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long categoryId = objectMapper.readTree(categoryResponse).get("id").asLong();

        mockMvc.perform(get("/categories/{catId}", categoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(categoryId))
                .andExpect(jsonPath("$.name").value("Integration Test Category"));
    }

    @Test
    void createAndGetUser_shouldWork() throws Exception {
        NewUserRequest userRequest = new NewUserRequest();
        userRequest.setEmail("integration@example.com");
        userRequest.setName("Integration User");

        String userResponse = mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("integration@example.com"))
                .andExpect(jsonPath("$.name").value("Integration User"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long userId = objectMapper.readTree(userResponse).get("id").asLong();

        mockMvc.perform(get("/admin/users")
                        .param("ids", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].email").value("integration@example.com"));
    }

    @Test
    void createCategory_shouldReturnConflict_whenNameDuplicate() throws Exception {
        NewCategoryDto categoryDto = new NewCategoryDto();
        categoryDto.setName("Duplicate Category");

        // Первое создание
        mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryDto)))
                .andExpect(status().isCreated());

        // Попытка создать дубликат - ожидаем CONFLICT (409)
        mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryDto)))
                .andExpect(status().isConflict())  // 409
                .andExpect(jsonPath("$.status").value("CONFLICT"));
    }

    @Test
    void createUser_shouldReturnConflict_whenEmailDuplicate() throws Exception {
        NewUserRequest userRequest = new NewUserRequest();
        userRequest.setEmail("duplicate@example.com");
        userRequest.setName("Duplicate User");

        // Первое создание
        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated());

        // Попытка создать дубликат - ожидаем CONFLICT (409)
        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isConflict())  // 409
                .andExpect(jsonPath("$.status").value("CONFLICT"));
    }

    @Test
    void deleteCategory_shouldReturnNoContent() throws Exception {
        NewCategoryDto categoryDto = new NewCategoryDto();
        categoryDto.setName("Category To Delete");

        String categoryResponse = mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryDto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long categoryId = objectMapper.readTree(categoryResponse).get("id").asLong();

        // Удаляем категорию
        mockMvc.perform(delete("/admin/categories/{catId}", categoryId))
                .andExpect(status().isNoContent());  // 204
    }

    @Test
    void deleteUser_shouldReturnNoContent() throws Exception {
        NewUserRequest userRequest = new NewUserRequest();
        userRequest.setEmail("delete@example.com");
        userRequest.setName("User To Delete");

        String userResponse = mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long userId = objectMapper.readTree(userResponse).get("id").asLong();

        // Удаляем пользователя
        mockMvc.perform(delete("/admin/users/{userId}", userId))
                .andExpect(status().isNoContent());  // 204
    }
}