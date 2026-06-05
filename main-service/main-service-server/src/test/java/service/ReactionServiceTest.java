package service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.practicum.client.StatisticsClient;
import ru.practicum.dto.*;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.ReactionMapper;
import ru.practicum.model.*;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.ReactionRepository;
import ru.practicum.repository.UserRepository;
import ru.practicum.service.impl.ReactionServiceImpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static ru.practicum.dto.EventState.PENDING;
import static ru.practicum.dto.EventState.PUBLISHED;
import static ru.practicum.dto.ReactionType.DISLIKE;
import static ru.practicum.dto.ReactionType.LIKE;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты для ReactionService")
public class ReactionServiceTest {

    @Mock
    private ReactionRepository reactionRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReactionMapper reactionMapper;

    @Mock
    private StatisticsClient statisticsClient;

    @InjectMocks
    private ReactionServiceImpl reactionService;

    private User testUser;
    private Event testEvent;
    private Reaction testReaction;
    private ReactionDto testReactionDto;
    private ReactionRequestDto testRequest;
    private Category testCategory;

    @BeforeEach
    @DisplayName("Инициализация тестовых данных")
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("user@example.com")
                .name("Test User")
                .build();

        testCategory = Category.builder()
                .id(1L)
                .name("Test Category")
                .build();

        ru.practicum.model.Location location = new ru.practicum.model.Location();
        location.setLat(55.754167f);
        location.setLon(37.62f);

        testEvent = Event.builder()
                .id(1L)
                .annotation("Test Annotation")
                .description("Test Description")
                .title("Test Event")
                .eventDate(LocalDateTime.now().plusDays(7))
                .initiator(testUser)
                .category(testCategory)
                .location(location)
                .paid(false)
                .participantLimit(10)
                .requestModeration(true)
                .state(PUBLISHED)
                .createdOn(LocalDateTime.now())
                .build();

        ReactionId reactionId = new ReactionId(1L, 2L);
        testReaction = Reaction.builder()
                .id(reactionId)
                .event(testEvent)
                .user(testUser)
                .reactionType(LIKE)
                .created(LocalDateTime.now())
                .build();

        testReactionDto = ReactionDto.builder()
                .id(1000001L)
                .eventId(1L)
                .userId(2L)
                .reactionType(LIKE)
                .created(LocalDateTime.now())
                .build();

        testRequest = new ReactionRequestDto();
        testRequest.setEventId(1L);
        testRequest.setReactionType(LIKE);
    }

    @Test
    @DisplayName("Добавление лайка - успешный сценарий")
    void addReaction_shouldAddLikeSuccessfully() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(testUser));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(reactionRepository.findById_EventIdAndId_UserId(1L, 2L)).thenReturn(Optional.empty());
        when(reactionRepository.save(any(Reaction.class))).thenReturn(testReaction);
        when(reactionMapper.toDto(any(Reaction.class))).thenReturn(testReactionDto);

        ReactionDto result = reactionService.addReaction(2L, testRequest);

        assertNotNull(result);
        assertEquals(LIKE, result.getReactionType());
        assertEquals(1L, result.getEventId());
        assertEquals(2L, result.getUserId());

        verify(reactionRepository, times(1)).save(any(Reaction.class));
    }

    @Test
    @DisplayName("Добавление лайка - ошибка при несуществующем пользователе")
    void addReaction_shouldThrowNotFound_whenUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> reactionService.addReaction(999L, testRequest));

        verify(reactionRepository, never()).save(any(Reaction.class));
    }

    @Test
    @DisplayName("Добавление лайка - ошибка при несуществующем событии")
    void addReaction_shouldThrowNotFound_whenEventNotFound() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(testUser));
        when(eventRepository.findById(999L)).thenReturn(Optional.empty());

        ReactionRequestDto invalidRequest = new ReactionRequestDto();
        invalidRequest.setEventId(999L);
        invalidRequest.setReactionType(LIKE);

        assertThrows(NotFoundException.class, () -> reactionService.addReaction(2L, invalidRequest));

        verify(reactionRepository, never()).save(any(Reaction.class));
    }

    @Test
    @DisplayName("Добавление лайка - ошибка при неопубликованном событии")
    void addReaction_shouldThrowConflict_whenEventNotPublished() {
        testEvent.setState(PENDING);

        when(userRepository.findById(2L)).thenReturn(Optional.of(testUser));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));

        assertThrows(ConflictException.class, () -> reactionService.addReaction(2L, testRequest));

        verify(reactionRepository, never()).save(any(Reaction.class));
    }

    @Test
    @DisplayName("Добавление лайка - ошибка при повторной реакции")
    void addReaction_shouldThrowConflict_whenReactionAlreadyExists() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(testUser));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(reactionRepository.findById_EventIdAndId_UserId(1L, 2L)).thenReturn(Optional.of(testReaction));

        assertThrows(ConflictException.class, () -> reactionService.addReaction(2L, testRequest));

        verify(reactionRepository, never()).save(any(Reaction.class));
    }

    @Test
    @DisplayName("Обновление реакции - успешная смена лайка на дизлайк")
    void updateReaction_shouldChangeLikeToDislikeSuccessfully() {
        ReactionRequestDto updateRequest = new ReactionRequestDto();
        updateRequest.setEventId(1L);
        updateRequest.setReactionType(DISLIKE);

        Reaction updatedReaction = Reaction.builder()
                .id(new ReactionId(1L, 2L))
                .event(testEvent)
                .user(testUser)
                .reactionType(DISLIKE)
                .created(LocalDateTime.now())
                .build();

        ReactionDto updatedDto = ReactionDto.builder()
                .id(1000001L)
                .eventId(1L)
                .userId(2L)
                .reactionType(DISLIKE)
                .created(LocalDateTime.now())
                .build();

        when(reactionRepository.findById_EventIdAndId_UserId(1L, 2L)).thenReturn(Optional.of(testReaction));
        when(reactionRepository.save(any(Reaction.class))).thenReturn(updatedReaction);
        when(reactionMapper.toDto(updatedReaction)).thenReturn(updatedDto);

        ReactionDto result = reactionService.updateReaction(2L, updateRequest);

        assertNotNull(result);
        assertEquals(DISLIKE, result.getReactionType());

        verify(reactionRepository, times(1)).save(any(Reaction.class));
    }

    @Test
    @DisplayName("Обновление реакции - ошибка при отсутствии реакции")
    void updateReaction_shouldThrowNotFound_whenReactionNotFound() {
        when(reactionRepository.findById_EventIdAndId_UserId(1L, 2L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> reactionService.updateReaction(2L, testRequest));

        verify(reactionRepository, never()).save(any(Reaction.class));
    }

    @Test
    @DisplayName("Удаление реакции - успешный сценарий")
    void deleteReaction_shouldDeleteSuccessfully() {
        when(reactionRepository.findById_EventIdAndId_UserId(1L, 2L)).thenReturn(Optional.of(testReaction));
        doNothing().when(reactionRepository).delete(any(Reaction.class));

        assertDoesNotThrow(() -> reactionService.deleteReaction(2L, 1L));

        verify(reactionRepository, times(1)).delete(testReaction);
    }

    @Test
    @DisplayName("Удаление реакции - ошибка при отсутствии реакции")
    void deleteReaction_shouldThrowNotFound_whenReactionNotFound() {
        when(reactionRepository.findById_EventIdAndId_UserId(1L, 2L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> reactionService.deleteReaction(2L, 1L));

        verify(reactionRepository, never()).delete(any(Reaction.class));
    }

    @Test
    @DisplayName("Получение реакции пользователя - успешный сценарий")
    void getUserReaction_shouldReturnReaction_whenExists() {
        when(reactionRepository.findById_EventIdAndId_UserId(1L, 2L)).thenReturn(Optional.of(testReaction));
        when(reactionMapper.toDto(testReaction)).thenReturn(testReactionDto);

        ReactionDto result = reactionService.getUserReaction(2L, 1L);

        assertNotNull(result);
        assertEquals(ReactionType.LIKE, result.getReactionType());
    }

    @Test
    @DisplayName("Получение реакции пользователя - null при отсутствии реакции")
    void getUserReaction_shouldReturnNull_whenNotExists() {
        when(reactionRepository.findById_EventIdAndId_UserId(1L, 2L)).thenReturn(Optional.empty());

        ReactionDto result = reactionService.getUserReaction(2L, 1L);

        assertNull(result);
    }

    @Test
    @DisplayName("Получение рейтинга события - успешный сценарий с реакциями")
    void getEventRating_shouldReturnRatingWithReactions() {
        List<Object[]> ratingData = new ArrayList<>();
        ratingData.add(new Object[]{1L, 5L, 4L, 1L});

        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(reactionRepository.getEventRating(1L)).thenReturn(ratingData);

        EventRatingDto result = reactionService.getEventRating(1L);

        assertNotNull(result);
        assertEquals(1L, result.getEventId());
        assertEquals("Test Event", result.getEventTitle());
        assertEquals(4L, result.getLikesCount());
        assertEquals(1L, result.getDislikesCount());
        assertEquals(5L, result.getTotalReactions());
        assertEquals(80, result.getRating()); // 4/5 * 100 = 80%
    }

    @Test
    @DisplayName("Получение рейтинга события - нулевой рейтинг при отсутствии реакций")
    void getEventRating_shouldReturnZeroRating_whenNoReactions() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(reactionRepository.getEventRating(1L)).thenReturn(List.of());

        EventRatingDto result = reactionService.getEventRating(1L);

        assertNotNull(result);
        assertEquals(1L, result.getEventId());
        assertEquals(0L, result.getLikesCount());
        assertEquals(0L, result.getDislikesCount());
        assertEquals(0L, result.getTotalReactions());
        assertEquals(0, result.getRating());
    }

    @Test
    @DisplayName("Получение рейтинга события - ошибка при несуществующем событии")
    void getEventRating_shouldThrowNotFound_whenEventNotFound() {
        when(eventRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> reactionService.getEventRating(999L));
    }

    @Test
    @DisplayName("Получение рейтинга нескольких событий - успешный сценарий")
    void getEventsRating_shouldReturnRatingsForMultipleEvents() {
        List<Long> eventIds = List.of(1L, 2L);
        List<Event> events = List.of(testEvent);
        List<Object[]> ratingsData = new ArrayList<>();
        ratingsData.add(new Object[]{1L, 5L, 4L, 1L});

        when(eventRepository.findAllById(eventIds)).thenReturn(events);
        when(reactionRepository.getEventsRating(eventIds)).thenReturn(ratingsData);

        List<EventRatingDto> results = reactionService.getEventsRating(eventIds);

        assertNotNull(results);
        assertEquals(2, results.size()); // Одно событие с реакциями, одно без
    }

    @Test
    @DisplayName("Получение рейтинга нескольких событий - пустой список при null аргументе")
    void getEventsRating_shouldReturnEmptyList_whenEventIdsNull() {
        List<EventRatingDto> results = reactionService.getEventsRating(null);

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("Получение рейтинга пользователя - успешный сценарий")
    void getUserRating_shouldReturnUserRatingSuccessfully() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(testUser));
        when(reactionRepository.countByUserIdAndReactionType(eq(2L), eq(LIKE))).thenReturn(5L);
        when(reactionRepository.countByUserIdAndReactionType(eq(2L), eq(DISLIKE))).thenReturn(2L);

        Page<Event> userEvents = new PageImpl<>(List.of(testEvent));
        when(eventRepository.findByInitiatorId(2L, PageRequest.of(0, 5))).thenReturn(userEvents);

        List<Object[]> ratingsData = new ArrayList<>();
        ratingsData.add(new Object[]{1L, 5L, 4L, 1L});
        when(reactionRepository.getEventsRating(List.of(1L))).thenReturn(ratingsData);

        UserRatingDto result = reactionService.getUserRating(2L);

        assertNotNull(result);
        assertEquals(2L, result.getUserId());
        assertEquals("Test User", result.getUserName());
        assertEquals(5L, result.getTotalLikesGiven());
        assertEquals(2L, result.getTotalDislikesGiven());
        assertEquals(7L, result.getTotalReactionsGiven());
        assertNotNull(result.getTopRatedEvents());
    }

    @Test
    @DisplayName("Получение рейтинга пользователя - ошибка при несуществующем пользователе")
    void getUserRating_shouldThrowNotFound_whenUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> reactionService.getUserRating(999L));
    }

    @Test
    @DisplayName("Получение топ событий по рейтингу - успешный сценарий")
    void getTopRatedEvents_shouldReturnTopEvents() {
        List<Object[]> ratingsData = List.of(
                new Object[]{1L, 10L, 9L, 1L},
                new Object[]{2L, 8L, 5L, 3L},
                new Object[]{3L, 6L, 2L, 4L}
        );

        List<Event> events = List.of(
                testEvent,
                Event.builder().id(2L).title("Event 2").build(),
                Event.builder().id(3L).title("Event 3").build()
        );

        when(reactionRepository.getEventsRating(null)).thenReturn(ratingsData);
        when(eventRepository.findAllById(List.of(1L, 2L, 3L))).thenReturn(events);

        List<EventRatingDto> results = reactionService.getTopRatedEvents(10);

        assertNotNull(results);
        assertEquals(3, results.size());
        // Первое событие должно иметь наивысший рейтинг (90%)
        assertEquals(90, results.get(0).getRating());
    }

    @Test
    @DisplayName("Получение топ событий по рейтингу - пустой список при отсутствии реакций")
    void getTopRatedEvents_shouldReturnEmptyList_whenNoRatings() {
        when(reactionRepository.getEventsRating(null)).thenReturn(List.of());

        List<EventRatingDto> results = reactionService.getTopRatedEvents(10);

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("Получение рейтинга события - корректный расчет процентов")
    void getEventRating_shouldCalculatePercentageCorrectly() {
        // Тест с разными соотношениями лайков и дизлайков

        // 100% лайков
        List<Object[]> ratingData100 = new ArrayList<>();
        ratingData100.add(new Object[]{1L, 10L, 10L, 0L});
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(reactionRepository.getEventRating(1L)).thenReturn(ratingData100);

        EventRatingDto result100 = reactionService.getEventRating(1L);
        assertEquals(100, result100.getRating());

        // 50% лайков
        List<Object[]> ratingData50 = new ArrayList<>();
        ratingData50.add(new Object[]{1L, 10L, 5L, 5L});
        when(reactionRepository.getEventRating(1L)).thenReturn(ratingData50);

        EventRatingDto result50 = reactionService.getEventRating(1L);
        assertEquals(50, result50.getRating());

        // 0% лайков
        List<Object[]> ratingData0 = new ArrayList<>();
        ratingData0.add(new Object[]{1L, 10L, 0L, 10L});
        when(reactionRepository.getEventRating(1L)).thenReturn(ratingData0);

        EventRatingDto result0 = reactionService.getEventRating(1L);
        assertEquals(0, result0.getRating());
    }
}
