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
import ru.practicum.dto.NewUserRequest;
import ru.practicum.dto.UserDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.UserMapper;
import ru.practicum.model.User;
import ru.practicum.repository.UserRepository;
import ru.practicum.service.impl.UserServiceImpl;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты для UserService")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UserDto testUserDto;
    private NewUserRequest testRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .name("Test User")
                .build();

        testUserDto = UserDto.builder()
                .id(1L)
                .email("test@example.com")
                .name("Test User")
                .build();

        testRequest = new NewUserRequest();
        testRequest.setEmail("test@example.com");
        testRequest.setName("Test User");
    }

    @Test
    @DisplayName("Создание пользователя - успешный сценарий")
    void createUser_shouldSaveAndReturnUser() {
        when(userRepository.existsByEmail(testRequest.getEmail())).thenReturn(false);
        when(userMapper.toEntity(testRequest)).thenReturn(testUser);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toDto(testUser)).thenReturn(testUserDto);

        UserDto result = userService.createUser(testRequest);

        assertNotNull(result);
        assertEquals(testUserDto.getId(), result.getId());
        assertEquals(testUserDto.getEmail(), result.getEmail());
        assertEquals(testUserDto.getName(), result.getName());

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Создание пользователя - ошибка при дублировании email")
    void createUser_shouldThrowConflictException_whenEmailExists() {
        when(userRepository.existsByEmail(testRequest.getEmail())).thenReturn(true);

        assertThrows(ConflictException.class, () -> userService.createUser(testRequest));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Создание пользователя - ошибка при нарушении целостности данных")
    void createUser_shouldThrowConflictException_whenDataIntegrityViolation() {
        when(userRepository.existsByEmail(testRequest.getEmail())).thenReturn(false);
        when(userMapper.toEntity(testRequest)).thenReturn(testUser);
        when(userRepository.save(any(User.class))).thenThrow(DataIntegrityViolationException.class);

        assertThrows(ConflictException.class, () -> userService.createUser(testRequest));

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Получение всех пользователей - успешный сценарий")
    void getUsers_shouldReturnAllUsers_whenIdsIsNull() {
        Page<User> userPage = new PageImpl<>(List.of(testUser));
        when(userRepository.findAll(any(PageRequest.class))).thenReturn(userPage);
        when(userMapper.toDto(testUser)).thenReturn(testUserDto);

        List<UserDto> result = userService.getUsers(null, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testUserDto.getId(), result.get(0).getId());
    }

    @Test
    @DisplayName("Получение всех пользователей - пустой список")
    void getUsers_shouldReturnEmptyList_whenNoUsers() {
        Page<User> emptyPage = new PageImpl<>(List.of());
        when(userRepository.findAll(any(PageRequest.class))).thenReturn(emptyPage);

        List<UserDto> result = userService.getUsers(null, 0, 10);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Получение пользователей по списку id - успешный сценарий")
    void getUsers_shouldReturnUsersWithGivenIds() {
        Page<User> userPage = new PageImpl<>(List.of(testUser));
        when(userRepository.findByIdIn(eq(List.of(1L)), any(PageRequest.class))).thenReturn(userPage);
        when(userMapper.toDto(testUser)).thenReturn(testUserDto);

        List<UserDto> result = userService.getUsers(List.of(1L), 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testUserDto.getId(), result.get(0).getId());
    }

    @Test
    @DisplayName("Получение пользователей по списку id - пустой список при отсутствии пользователей")
    void getUsers_shouldReturnEmptyList_whenIdsNotFound() {
        Page<User> emptyPage = new PageImpl<>(List.of());
        when(userRepository.findByIdIn(eq(List.of(999L)), any(PageRequest.class))).thenReturn(emptyPage);

        List<UserDto> result = userService.getUsers(List.of(999L), 0, 10);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Удаление пользователя - успешный сценарий")
    void deleteUser_shouldDeleteUser_whenUserExists() {
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        assertDoesNotThrow(() -> userService.deleteUser(1L));

        verify(userRepository, times(1)).existsById(1L);
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Удаление пользователя - ошибка при отсутствии пользователя")
    void deleteUser_shouldThrowNotFoundException_whenUserDoesNotExist() {
        when(userRepository.existsById(999L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> userService.deleteUser(999L));

        verify(userRepository, never()).deleteById(anyLong());
    }
}