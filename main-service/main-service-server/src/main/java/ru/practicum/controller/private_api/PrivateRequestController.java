package ru.practicum.controller.private_api;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.ParticipationRequestDto;
import ru.practicum.service.ParticipationRequestService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/requests")
@RequiredArgsConstructor
@Validated
public class PrivateRequestController {
    private final ParticipationRequestService requestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto createRequest(@PathVariable @Positive Long userId,
                                                 @RequestParam(name = "eventId") @Positive Long eventId) {
        return requestService.createRequest(userId, eventId);
    }

    @PatchMapping("/{requestId}/cancel")
    public ParticipationRequestDto cancelRequest(@PathVariable @Positive Long userId,
                                                 @PathVariable @Positive Long requestId) {
        return requestService.cancelRequest(userId, requestId);
    }

    @GetMapping
    public List<ParticipationRequestDto> getUserRequests(@PathVariable @Positive Long userId) {
        return requestService.getUserRequests(userId);
    }

    @GetMapping("/{requestId}")
    public ParticipationRequestDto getRequestById(@PathVariable @Positive Long userId,
                                                  @PathVariable @Positive Long requestId) {
        return requestService.getRequestById(userId, requestId);
    }
}
