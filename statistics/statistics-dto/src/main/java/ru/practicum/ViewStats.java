package ru.practicum;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ViewStats {

    /**
     * Название сервиса
     */
    private String app;

    /**
     * URI сервиса
     */
    private String uri;

    /**
     * Количество просмотров
     */
    private Long hits;
}