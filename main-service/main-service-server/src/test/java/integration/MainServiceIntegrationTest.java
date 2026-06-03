package integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
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
@DisplayName("Интеграционные тесты основного сервиса")
class MainServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Создание и получение категории - успешный сценарий")
    void createAndGetCategory_shouldWork() throws Exception {
        NewCategoryDto categoryDto = new NewCategoryDto();
        categoryDto.setName("Integration Test Category");

        String categoryResponse = mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Integration Test Category"))
                .andExpect(jsonPath("$.id").exists())
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
    @DisplayName("Создание и получение пользователя - успешный сценарий")
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
                .andExpect(jsonPath("$.id").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long userId = objectMapper.readTree(userResponse).get("id").asLong();

        mockMvc.perform(get("/admin/users")
                        .param("ids", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(userId))
                .andExpect(jsonPath("$[0].email").value("integration@example.com"))
                .andExpect(jsonPath("$[0].name").value("Integration User"));
    }

    @Test
    @DisplayName("Создание категории - ошибка при дублировании имени (409 Conflict)")
    void createCategory_shouldReturnConflict_whenNameDuplicate() throws Exception {
        NewCategoryDto categoryDto = new NewCategoryDto();
        categoryDto.setName("Duplicate Category");

        // Первое создание
        mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Duplicate Category"));

        // Попытка создать дубликат - ожидаем CONFLICT (409)
        mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value("CONFLICT"))
                .andExpect(jsonPath("$.message").value("Category with name Duplicate Category already exists"));
    }

    @Test
    @DisplayName("Создание пользователя - ошибка при дублировании email (409 Conflict)")
    void createUser_shouldReturnConflict_whenEmailDuplicate() throws Exception {
        NewUserRequest userRequest = new NewUserRequest();
        userRequest.setEmail("duplicate@example.com");
        userRequest.setName("Duplicate User");

        // Первое создание
        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("duplicate@example.com"))
                .andExpect(jsonPath("$.name").value("Duplicate User"));

        // Попытка создать дубликат - ожидаем CONFLICT (409)
        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value("CONFLICT"))
                .andExpect(jsonPath("$.message").value("User with email duplicate@example.com already exists"));
    }

    @Test
    @DisplayName("Создание категории - ошибка валидации при пустом имени (400 Bad Request)")
    void createCategory_shouldReturnBadRequest_whenNameIsBlank() throws Exception {
        NewCategoryDto categoryDto = new NewCategoryDto();
        categoryDto.setName("");

        mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"));
    }

    @Test
    @DisplayName("Создание пользователя - ошибка валидации при невалидном email (400 Bad Request)")
    void createUser_shouldReturnBadRequest_whenInvalidEmail() throws Exception {
        NewUserRequest userRequest = new NewUserRequest();
        userRequest.setEmail("invalid-email");
        userRequest.setName("Test User");

        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"));
    }

    @Test
    @DisplayName("Удаление категории - успешный сценарий (204 No Content)")
    void deleteCategory_shouldReturnNoContent() throws Exception {
        NewCategoryDto categoryDto = new NewCategoryDto();
        categoryDto.setName("Category To Delete");

        String categoryResponse = mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Category To Delete"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long categoryId = objectMapper.readTree(categoryResponse).get("id").asLong();

        // Удаляем категорию
        mockMvc.perform(delete("/admin/categories/{catId}", categoryId))
                .andExpect(status().isNoContent());

        // Проверяем, что категория действительно удалена
        mockMvc.perform(get("/categories/{catId}", categoryId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("NOT_FOUND"));
    }

    @Test
    @DisplayName("Удаление пользователя - успешный сценарий (204 No Content)")
    void deleteUser_shouldReturnNoContent() throws Exception {
        NewUserRequest userRequest = new NewUserRequest();
        userRequest.setEmail("delete@example.com");
        userRequest.setName("User To Delete");

        String userResponse = mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("delete@example.com"))
                .andExpect(jsonPath("$.name").value("User To Delete"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long userId = objectMapper.readTree(userResponse).get("id").asLong();

        // Удаляем пользователя
        mockMvc.perform(delete("/admin/users/{userId}", userId))
                .andExpect(status().isNoContent());

        // Проверяем, что пользователь действительно удален
        mockMvc.perform(get("/admin/users")
                        .param("ids", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("Удаление категории - ошибка при удалении несуществующей категории (404 Not Found)")
    void deleteCategory_shouldReturnNotFound_whenCategoryDoesNotExist() throws Exception {
        mockMvc.perform(delete("/admin/categories/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Category with id=99999 was not found"));
    }

    @Test
    @DisplayName("Удаление пользователя - ошибка при удалении несуществующего пользователя (404 Not Found)")
    void deleteUser_shouldReturnNotFound_whenUserDoesNotExist() throws Exception {
        mockMvc.perform(delete("/admin/users/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("User with id=99999 was not found"));
    }

    @Test
    @DisplayName("Получение категории - ошибка при невалидном id (400 Bad Request)")
    void getCategory_shouldReturnBadRequest_whenInvalidId() throws Exception {
        mockMvc.perform(get("/categories/invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"));
    }

    @Test
    @DisplayName("Получение списка категорий - пагинация работает корректно")
    void getCategories_shouldReturnPaginatedResult() throws Exception {
        // Создаем 5 категорий
        for (int i = 1; i <= 5; i++) {
            NewCategoryDto categoryDto = new NewCategoryDto();
            categoryDto.setName("Category " + i);
            mockMvc.perform(post("/admin/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(categoryDto)))
                    .andExpect(status().isCreated());
        }

        // Проверяем первую страницу (первые 2 категории)
        mockMvc.perform(get("/categories")
                        .param("from", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        // Проверяем вторую страницу (следующие 2 категории)
        mockMvc.perform(get("/categories")
                        .param("from", "2")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("Получение списка пользователей - пагинация работает корректно")
    void getUsers_shouldReturnPaginatedResult() throws Exception {
        // Создаем 3 пользователей
        for (int i = 1; i <= 3; i++) {
            NewUserRequest userRequest = new NewUserRequest();
            userRequest.setEmail("user" + i + "@example.com");
            userRequest.setName("User " + i);
            mockMvc.perform(post("/admin/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(userRequest)))
                    .andExpect(status().isCreated());
        }

        // Проверяем первую страницу
        mockMvc.perform(get("/admin/users")
                        .param("from", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        // Проверяем вторую страницу
        mockMvc.perform(get("/admin/users")
                        .param("from", "2")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
}