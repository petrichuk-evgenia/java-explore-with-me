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
import ru.practicum.dto.ReactionDto;
import ru.practicum.dto.ReactionRequestDto;
import ru.practicum.service.ReactionService;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.dto.ReactionType.DISLIKE;
import static ru.practicum.dto.ReactionType.LIKE;

@SpringBootTest(classes = MainApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Тесты для PrivateReactionController")
class PrivateReactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReactionService reactionService;

    @Test
    @DisplayName("Добавление реакции - успешный сценарий (201 Created)")
    void addReaction_shouldReturnCreated() throws Exception {
        ReactionRequestDto request = new ReactionRequestDto();
        request.setEventId(1L);
        request.setReactionType(LIKE);

        ReactionDto response = ReactionDto.builder()
                .id(1L)
                .eventId(1L)
                .userId(1L)
                .reactionType(LIKE)
                .created(LocalDateTime.now())
                .build();

        when(reactionService.addReaction(eq(1L), any(ReactionRequestDto.class))).thenReturn(response);

        mockMvc.perform(post("/users/1/reactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.eventId").value(1))
                .andExpect(jsonPath("$.reactionType").value("LIKE"));
    }

    @Test
    @DisplayName("Добавление реакции - ошибка валидации при отсутствии eventId")
    void addReaction_shouldReturnBadRequest_whenEventIdMissing() throws Exception {
        String invalidRequest = "{\"reactionType\": \"LIKE\"}";

        mockMvc.perform(post("/users/1/reactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Добавление реакции - ошибка валидации при отсутствии reactionType")
    void addReaction_shouldReturnBadRequest_whenReactionTypeMissing() throws Exception {
        String invalidRequest = "{\"eventId\": 1}";

        mockMvc.perform(post("/users/1/reactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Обновление реакции - успешный сценарий (200 OK)")
    void updateReaction_shouldReturnOk() throws Exception {
        ReactionRequestDto request = new ReactionRequestDto();
        request.setEventId(1L);
        request.setReactionType(DISLIKE);

        ReactionDto response = ReactionDto.builder()
                .id(1L)
                .eventId(1L)
                .userId(1L)
                .reactionType(DISLIKE)
                .created(LocalDateTime.now())
                .build();

        when(reactionService.updateReaction(eq(1L), any(ReactionRequestDto.class))).thenReturn(response);

        mockMvc.perform(put("/users/1/reactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reactionType").value("DISLIKE"));
    }

    @Test
    @DisplayName("Удаление реакции - успешный сценарий (204 No Content)")
    void deleteReaction_shouldReturnNoContent() throws Exception {
        doNothing().when(reactionService).deleteReaction(1L, 1L);

        mockMvc.perform(delete("/users/1/reactions/1"))
                .andExpect(status().isNoContent());

        verify(reactionService, times(1)).deleteReaction(1L, 1L);
    }

    @Test
    @DisplayName("Получение реакции пользователя - успешный сценарий (200 OK)")
    void getUserReaction_shouldReturnReaction() throws Exception {
        ReactionDto response = ReactionDto.builder()
                .id(1L)
                .eventId(1L)
                .userId(1L)
                .reactionType(LIKE)
                .created(LocalDateTime.now())
                .build();

        when(reactionService.getUserReaction(1L, 1L)).thenReturn(response);

        mockMvc.perform(get("/users/1/reactions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reactionType").value("LIKE"));
    }

    @Test
    @DisplayName("Получение реакции пользователя - 404 при отсутствии")
    void getUserReaction_shouldReturnNotFound() throws Exception {
        when(reactionService.getUserReaction(1L, 999L)).thenReturn(null);

        mockMvc.perform(get("/users/1/reactions/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").doesNotExist());
    }
}