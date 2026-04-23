package com.seatsync.domain.event.service;

import com.seatsync.domain.event.entity.Event;
import com.seatsync.domain.event.repository.EventRepository;
import com.seatsync.domain.event.repository.EventRepositoryCustom;
import com.seatsync.global.exception.CustomException;
import com.seatsync.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final EventRepositoryCustom eventRepositoryCustom;

    @Transactional
    public Event createEvent(String title, String genre, String venue,
                             String description, String posterUrl) {
        Event event = Event.builder()
                .title(title)
                .genre(genre)
                .venue(venue)
                .description(description)
                .posterUrl(posterUrl)
                .build();

        return eventRepository.save(event);
    }

    @Transactional(readOnly = true)
    public List<Event> searchEvents(String genre, String venue, String keyword) {
        return eventRepositoryCustom.searchEvents(genre, venue, keyword);
    }

    @Transactional(readOnly = true)
    public Event getEvent(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.EVENT_NOT_FOUND));
    }

    @Transactional
    public void deleteEvent(Long id) {
        Event event = getEvent(id);
        eventRepository.delete(event);
    }
}