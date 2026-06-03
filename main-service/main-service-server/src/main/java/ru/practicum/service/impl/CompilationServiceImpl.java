package ru.practicum.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.CompilationDto;
import ru.practicum.dto.NewCompilationDto;
import ru.practicum.dto.UpdateCompilationRequest;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.CompilationMapper;
import ru.practicum.model.Compilation;
import ru.practicum.repository.CompilationRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.service.CompilationService;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final CompilationMapper compilationMapper;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false)
    public CompilationDto createCompilation(NewCompilationDto dto) {
        if (compilationRepository.existsByTitle(dto.getTitle())) {
            throw new ConflictException("Compilation with title \"" + dto.getTitle() + "\" already exists");
        }

        Compilation compilation = compilationMapper.toEntity(dto);

        // Загружаем полные сущности событий из базы данных
        if (compilation.getEvents() != null && !compilation.getEvents().isEmpty()) {
            Set<ru.practicum.model.Event> fullEvents = eventRepository.findAllById(
                    compilation.getEvents().stream().map(ru.practicum.model.Event::getId).collect(Collectors.toSet())
            ).stream().collect(Collectors.toSet());
            compilation.setEvents(fullEvents);
        }

        try {
            return compilationMapper.toDto(compilationRepository.save(compilation));
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Compilation with title \"" + dto.getTitle() + "\" already exists");
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false)
    public void deleteCompilation(Long compId) {
        Compilation compilation = getCompilationEntity(compId);
        compilationRepository.delete(compilation);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false)
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest dto) {
        Compilation compilation = getCompilationEntity(compId);

        if (dto.getTitle() != null && !compilation.getTitle().equals(dto.getTitle())) {
            if (compilationRepository.existsByTitle(dto.getTitle())) {
                throw new ConflictException("Compilation with title \"" + dto.getTitle() + "\" already exists");
            }
            compilation.setTitle(dto.getTitle());
        }

        if (dto.getPinned() != null) {
            compilation.setPinned(dto.getPinned());
        }

        if (dto.getEvents() != null) {
            Set<ru.practicum.model.Event> events = eventRepository.findAllById(dto.getEvents()).stream().collect(Collectors.toSet());
            compilation.setEvents(events);
        }

        return compilationMapper.toDto(compilationRepository.save(compilation));
    }

    @Override
    public List<CompilationDto> getCompilations(Integer from, Integer size, Boolean pinned) {
        if (pinned != null) {
            return compilationRepository.findAll(PageRequest.of(from / size, size))
                    .stream()
                    .map(compilationMapper::toDto)
                    .filter(c -> c.getPinned().equals(pinned))
                    .collect(Collectors.toList());
        }
        return compilationRepository.findAll(PageRequest.of(from / size, size))
                .stream()
                .map(compilationMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CompilationDto getCompilationById(Long compId) {
        return compilationMapper.toDto(getCompilationEntity(compId));
    }

    private Compilation getCompilationEntity(Long compId) {
        return compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation with id=" + compId + " was not found"));
    }
}
