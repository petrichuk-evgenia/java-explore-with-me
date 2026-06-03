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
import ru.practicum.dto.NewUserRequest;
import ru.practicum.dto.UserDto;
import ru.practicum.service.UserService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = MainApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Тесты для AdminUserController")
class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    @DisplayName("Создание пользователя - успешный сценарий")
    void createUser_shouldReturnCreatedUser() throws Exception {
        NewUserRequest request = new NewUserRequest();
        request.setEmail("test@example.com");
        request.setName("Test User");

        UserDto response = UserDto.builder()
                .id(1L)
                .email("test@example.com")
                .name("Test User")
                .build();

        when(userService.createUser(any(NewUserRequest.class))).thenReturn(response);

        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.name").value("Test User"));
    }

    @Test
    @DisplayName("Создание пользователя - ошибка валидации при невалидном email")
    void createUser_shouldReturnBadRequest_whenInvalidEmail() throws Exception {
        NewUserRequest request = new NewUserRequest();
        request.setEmail("invalid-email");
        request.setName("Test User");

        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Создание пользователя - ошибка валидации при слишком длинном email")
    void createUser_shouldReturnBadRequest_whenEmailTooLong() throws Exception {
        NewUserRequest request = new NewUserRequest();
        String longEmail = "a".repeat(260) + "@example.com";
        request.setEmail(longEmail);
        request.setName("Test User");

        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Получение пользователей - успешный сценарий")
    void getUsers_shouldReturnListOfUsers() throws Exception {
        List<UserDto> users = List.of(
                UserDto.builder().id(1L).email("user1@example.com").name("User 1").build(),
                UserDto.builder().id(2L).email("user2@example.com").name("User 2").build()
        );

        when(userService.getUsers(null, 0, 10)).thenReturn(users);

        mockMvc.perform(get("/admin/users")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    @DisplayName("Получение пользователей - пустой список")
    void getUsers_shouldReturnEmptyList_whenNoUsers() throws Exception {
        when(userService.getUsers(null, 0, 10)).thenReturn(List.of());

        mockMvc.perform(get("/admin/users")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("Удаление пользователя - успешный сценарий")
    void deleteUser_shouldReturnNoContent() throws Exception {
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/admin/users/1"))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteUser(1L);
    }

    @Test
    @DisplayName("Удаление пользователя - ошибка при невалидном id")
    void deleteUser_shouldReturnBadRequest_whenInvalidId() throws Exception {
        mockMvc.perform(delete("/admin/users/invalid"))
                .andExpect(status().isBadRequest());
    }
}