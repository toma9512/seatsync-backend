package com.seatsync.domain.event.controller;

import com.seatsync.domain.event.entity.Event;
import com.seatsync.domain.event.service.EventService;
import com.seatsync.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PostMapping
    public ApiResponse<EventResponse> createEvent(@RequestBody EventRequest request) {
        Event event = eventService.createEvent(
                request.title(), request.genre(), request.venue(),
                request.description(), request.posterUrl()
        );
        return ApiResponse.success(EventResponse.from(event));
    }

    @GetMapping
    public ApiResponse<List<EventResponse>> searchEvents(
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String venue,
            @RequestParam(required = false) String keyword
    ) {
        List<Event> events = eventService.searchEvents(genre, venue, keyword);
        return ApiResponse.success(events.stream().map(EventResponse::from).toList());
    }

    @GetMapping("/{id}")
    public ApiResponse<EventResponse> getEvent(@PathVariable Long id) {
        return ApiResponse.success(EventResponse.from(eventService.getEvent(id)));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return ApiResponse.success("공연이 삭제되었습니다.", null);
    }

    public record EventRequest(
            String title,
            String genre,
            String venue,
            String description,
            String posterUrl
    ) {}

    public record EventResponse(
            Long id,
            String title,
            String genre,
            String venue,
            String description,
            String posterUrl
    ) {
        public static EventResponse from(Event event) {
            return new EventResponse(
                    event.getId(),
                    event.getTitle(),
                    event.getGenre(),
                    event.getVenue(),
                    event.getDescription(),
                    event.getPosterUrl()
            );
        }
    }
}