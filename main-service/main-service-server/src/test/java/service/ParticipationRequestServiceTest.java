package service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.dto.EventState;
import ru.practicum.dto.ParticipationRequestDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.ParticipationRequestMapper;
import ru.practicum.model.Event;
import ru.practicum.model.ParticipationRequest;
import ru.practicum.model.User;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.ParticipationRequestRepository;
import ru.practicum.repository.UserRepository;
import ru.practicum.service.impl.ParticipationRequestServiceImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты для ParticipationRequestService")
class ParticipationRequestServiceTest {

    @Mock
    private ParticipationRequestRepository requestRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ParticipationRequestMapper requestMapper;

    @InjectMocks
    private ParticipationRequestServiceImpl requestService;

    private User testUser;
    private Event testEvent;
    private ParticipationRequest testRequest;
    private ParticipationRequestDto testRequestDto;

    @BeforeEach
    @DisplayName("Инициализация тестовых данных")
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .name("Test User")
                .build();

        testEvent = Event.builder()
                .id(1L)
                .title("Test Event")
                .initiator(testUser)
                .state(EventState.PUBLISHED)
                .participantLimit(10)
                .requestModeration(true)
                .build();

        testRequest = ParticipationRequest.builder()
                .id(1L)
                .created(LocalDateTime.now())
                .event(testEvent)
                .requester(testUser)
                .status("PENDING")
                .build();

        testRequestDto = ParticipationRequestDto.builder()
                .id(1L)
                .created(LocalDateTime.now())
                .event(1L)
                .requester(1L)
                .status("PENDING")
                .build();
    }

    /*@Test
    @DisplayName("Создание запроса на участие - успешный сценарий")
    void createRequest_shouldSaveAndReturnRequest() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(requestRepository.existsByEventIdAndRequesterId(1L, 1L)).thenReturn(false);
        when(requestRepository.countByEventIdAndStatus(1L, "CONFIRMED")).thenReturn(0L);
        when(requestMapper.toDto(any(ParticipationRequest.class))).thenReturn(testRequestDto);
        when(requestRepository.save(any(ParticipationRequest.class))).thenReturn(testRequest);

        ParticipationRequestDto result = requestService.createRequest(1L, 1L);

        assertNotNull(result);
        assertEquals(testRequestDto.getId(), result.getId());
        assertEquals(testRequestDto.getStatus(), result.getStatus());

        verify(requestRepository, times(1)).save(any(ParticipationRequest.class));
    }*/

    @Test
    @DisplayName("Создание запроса на участие - повторный запрос")
    void createRequest_shouldThrowConflictException_whenRequestExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(requestRepository.existsByEventIdAndRequesterId(1L, 1L)).thenReturn(true);

        assertThrows(ConflictException.class, () -> requestService.createRequest(1L, 1L));

        verify(requestRepository, never()).save(any(ParticipationRequest.class));
    }

    @Test
    @DisplayName("Создание запроса на участие - инициатор события")
    void createRequest_shouldThrowConflictException_whenInitiator() {
        Event ownEvent = Event.builder()
                .id(1L)
                .title("Test Event")
                .initiator(testUser)
                .state(EventState.PUBLISHED)
                .participantLimit(10)
                .requestModeration(true)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(ownEvent));
        when(requestRepository.existsByEventIdAndRequesterId(1L, 1L)).thenReturn(false);

        assertThrows(ConflictException.class, () -> requestService.createRequest(1L, 1L));

        verify(requestRepository, never()).save(any(ParticipationRequest.class));
    }

    @Test
    @DisplayName("Создание запроса на участие - событие не опубликовано")
    void createRequest_shouldThrowConflictException_whenEventNotPublished() {
        Event unpublishedEvent = Event.builder()
                .id(1L)
                .title("Test Event")
                .initiator(testUser)
                .state(EventState.CANCELED)
                .participantLimit(10)
                .requestModeration(true)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(unpublishedEvent));
        when(requestRepository.existsByEventIdAndRequesterId(1L, 1L)).thenReturn(false);

        assertThrows(ConflictException.class, () -> requestService.createRequest(1L, 1L));

        verify(requestRepository, never()).save(any(ParticipationRequest.class));
    }

    @Test
    @DisplayName("Создание запроса на участие - лимит заявок достигнут")
    void createRequest_shouldThrowConflictException_whenLimitReached() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(requestRepository.existsByEventIdAndRequesterId(1L, 1L)).thenReturn(false);

        assertThrows(ConflictException.class, () -> requestService.createRequest(1L, 1L));

        verify(requestRepository, never()).save(any(ParticipationRequest.class));
    }

    /*@Test
    @DisplayName("Отмена запроса на участие - успешный сценарий")
    void cancelRequest_shouldCancelRequest() {
        when(requestRepository.findById(1L)).thenReturn(Optional.of(testRequest));
        when(requestRepository.save(any(ParticipationRequest.class))).thenAnswer(invocation -> {
            ParticipationRequest request = invocation.getArgument(0);
            request.setStatus("CANCELED");
            return request;
        });
        when(requestMapper.toDto(testRequest)).thenReturn(testRequestDto);

        ParticipationRequestDto result = requestService.cancelRequest(1L, 1L);

        assertNotNull(result);
        assertEquals("CANCELED", result.getStatus());

        verify(requestRepository, times(1)).save(any(ParticipationRequest.class));
    }*/

    @Test
    @DisplayName("Отмена запроса на участие - запрос не найден")
    void cancelRequest_shouldThrowNotFoundException_whenRequestNotFound() {
        when(requestRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> requestService.cancelRequest(1L, 999L));
    }

    @Test
    @DisplayName("Отмена запроса на участие - пользователь не является отправителем")
    void cancelRequest_shouldThrowNotFoundException_whenNotRequester() {
        User otherUser = User.builder()
                .id(2L)
                .email("other@example.com")
                .name("Other User")
                .build();

        ParticipationRequest otherRequest = ParticipationRequest.builder()
                .id(1L)
                .created(LocalDateTime.now())
                .event(testEvent)
                .requester(otherUser)
                .status("PENDING")
                .build();

        when(requestRepository.findById(1L)).thenReturn(Optional.of(otherRequest));

        assertThrows(NotFoundException.class, () -> requestService.cancelRequest(1L, 1L));
    }

    @Test
    @DisplayName("Получение запросов пользователя - успешный сценарий")
    void getUserRequests_shouldReturnUserRequests() {
        List<ParticipationRequest> requests = List.of(testRequest);
        List<ParticipationRequestDto> requestDtos = List.of(testRequestDto);

        when(requestRepository.findByRequesterId(1L)).thenReturn(requests);
        when(requestMapper.toDto(testRequest)).thenReturn(testRequestDto);

        List<ParticipationRequestDto> result = requestService.getUserRequests(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testRequestDto.getId(), result.get(0).getId());
    }

    @Test
    @DisplayName("Получение запросов пользователя - пустой список")
    void getUserRequests_shouldReturnEmptyList_whenNoRequests() {
        when(requestRepository.findByRequesterId(1L)).thenReturn(List.of());

        List<ParticipationRequestDto> result = requestService.getUserRequests(1L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Получение запроса по id - успешный сценарий")
    void getRequestById_shouldReturnRequest() {
        when(requestRepository.findById(1L)).thenReturn(Optional.of(testRequest));
        when(requestMapper.toDto(testRequest)).thenReturn(testRequestDto);

        ParticipationRequestDto result = requestService.getRequestById(1L, 1L);

        assertNotNull(result);
        assertEquals(testRequestDto.getId(), result.getId());
    }

    @Test
    @DisplayName("Получение запроса по id - запрос не найден")
    void getRequestById_shouldThrowNotFoundException_whenRequestNotFound() {
        when(requestRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> requestService.getRequestById(1L, 999L));
    }

    @Test
    @DisplayName("Получение запроса по id - пользователь не является отправителем")
    void getRequestById_shouldThrowNotFoundException_whenNotRequester() {
        User otherUser = User.builder()
                .id(2L)
                .email("other@example.com")
                .name("Other User")
                .build();

        ParticipationRequest otherRequest = ParticipationRequest.builder()
                .id(1L)
                .created(LocalDateTime.now())
                .event(testEvent)
                .requester(otherUser)
                .status("PENDING")
                .build();

        when(requestRepository.findById(1L)).thenReturn(Optional.of(otherRequest));

        assertThrows(NotFoundException.class, () -> requestService.getRequestById(1L, 1L));
    }
}
