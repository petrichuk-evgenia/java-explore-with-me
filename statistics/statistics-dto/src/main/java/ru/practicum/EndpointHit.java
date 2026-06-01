package ru.practicum;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EndpointHit {

    /**
     * Идентификатор записи (генерируется автоматически)
     */
    private Long id;

    /**
     * Идентификатор сервиса для которого записывается информация
     */
    @NotBlank(message = "App name cannot be blank")
    private String app;

    /**
     * URI для которого был осуществлен запрос
     */
    @NotBlank(message = "URI cannot be blank")
    private String uri;

    /**
     * IP-адрес пользователя, осуществившего запрос
     */
    @NotBlank(message = "IP cannot be blank")
    private String ip;

    /**
     * Дата и время, когда был совершен запрос к эндпоинту (в формате "yyyy-MM-dd HH:mm:ss")
     */
    @NotNull(message = "Timestamp cannot be null")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
}
