package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.MainApplication;
import ru.practicum.dto.EventRatingDto;
import ru.practicum.dto.UserRatingDto;
import ru.practicum.service.ReactionService;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = MainApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Тесты для PublicRatingController")
class PublicRatingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReactionService reactionService;

    @Test
    @DisplayName("Получение рейтинга события - успешный сценарий (200 OK)")
    void getEventRating_shouldReturnRating() throws Exception {
        EventRatingDto response = EventRatingDto.builder()
                .eventId(1L)
                .eventTitle("Test Event")
                .likesCount(10L)
                .dislikesCount(2L)
                .totalReactions(12L)
                .rating(83)
                .build();

        when(reactionService.getEventRating(1L)).thenReturn(response);

        mockMvc.perform(get("/ratings/events/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventId").value(1))
                .andExpect(jsonPath("$.likesCount").value(10))
                .andExpect(jsonPath("$.dislikesCount").value(2))
                .andExpect(jsonPath("$.rating").value(83));
    }

    @Test
    @DisplayName("Получение рейтинга события - ошибка при невалидном id (400 Bad Request)")
    void getEventRating_shouldReturnBadRequest_whenInvalidId() throws Exception {
        mockMvc.perform(get("/ratings/events/invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Получение рейтинга нескольких событий - успешный сценарий")
    void getEventsRating_shouldReturnRatings() throws Exception {
        List<EventRatingDto> responses = List.of(
                EventRatingDto.builder().eventId(1L).eventTitle("Event 1").likesCount(10L).dislikesCount(2L).rating(83).build(),
                EventRatingDto.builder().eventId(2L).eventTitle("Event 2").likesCount(5L).dislikesCount(5L).rating(50).build()
        );

        when(reactionService.getEventsRating(List.of(1L, 2L))).thenReturn(responses);

        mockMvc.perform(get("/ratings/events")
                        .param("eventIds", "1,2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].eventId").value(1))
                .andExpect(jsonPath("$[1].eventId").value(2));
    }

    @Test
    @DisplayName("Получение рейтинга пользователя - успешный сценарий")
    void getUserRating_shouldReturnUserRating() throws Exception {
        UserRatingDto response = UserRatingDto.builder()
                .userId(1L)
                .userName("Test User")
                .totalLikesGiven(15L)
                .totalDislikesGiven(3L)
                .totalReactionsGiven(18L)
                .topRatedEvents(List.of())
                .build();

        when(reactionService.getUserRating(1L)).thenReturn(response);

        mockMvc.perform(get("/ratings/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.userName").value("Test User"))
                .andExpect(jsonPath("$.totalLikesGiven").value(15))
                .andExpect(jsonPath("$.totalDislikesGiven").value(3));
    }

    @Test
    @DisplayName("Получение топ событий - успешный сценарий с параметром limit")
    void getTopRatedEvents_shouldReturnTopEvents() throws Exception {
        List<EventRatingDto> responses = List.of(
                EventRatingDto.builder().eventId(1L).eventTitle("Top Event").likesCount(100L).dislikesCount(5L).rating(95).build(),
                EventRatingDto.builder().eventId(2L).eventTitle("Second Event").likesCount(80L).dislikesCount(20L).rating(80).build()
        );

        when(reactionService.getTopRatedEvents(5)).thenReturn(responses);

        mockMvc.perform(get("/ratings/events/top")
                        .param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].rating").value(95));
    }

    @Test
    @DisplayName("Получение топ событий - используется limit по умолчанию (10)")
    void getTopRatedEvents_shouldUseDefaultLimit() throws Exception {
        when(reactionService.getTopRatedEvents(10)).thenReturn(List.of());

        mockMvc.perform(get("/ratings/events/top"))
                .andExpect(status().isOk());

        verify(reactionService, times(1)).getTopRatedEvents(10);
    }
}