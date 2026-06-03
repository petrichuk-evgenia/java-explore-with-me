package mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.practicum.dto.NewUserRequest;
import ru.practicum.dto.UserDto;
import ru.practicum.dto.UserShortDto;
import ru.practicum.mapper.UserMapper;
import ru.practicum.model.User;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тесты для UserMapper")
public class UserMapperTest {

    private final UserMapper userMapper = new UserMapper();

    @Test
    @DisplayName("Преобразование User в UserDto - успешное преобразование")
    void toDto_shouldMapUserToUserDto() {
        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .name("Test User")
                .build();

        UserDto result = userMapper.toDto(user);

        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        assertEquals(user.getEmail(), result.getEmail());
        assertEquals(user.getName(), result.getName());
    }

    @Test
    @DisplayName("Преобразование User в UserShortDto - успешное преобразование")
    void toShortDto_shouldMapUserToUserShortDto() {
        User user = User.builder()
                .id(1L)
                .name("Test User")
                .build();

        UserShortDto result = userMapper.toShortDto(user);

        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        assertEquals(user.getName(), result.getName());
    }

    @Test
    @DisplayName("Преобразование NewUserRequest в User - успешное преобразование")
    void toEntity_shouldMapNewUserRequestToUser() {
        NewUserRequest request = new NewUserRequest();
        request.setEmail("test@example.com");
        request.setName("Test User");

        User result = userMapper.toEntity(request);

        assertNotNull(result);
        assertEquals(request.getEmail(), result.getEmail());
        assertEquals(request.getName(), result.getName());
    }
}
