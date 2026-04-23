package com.seatsync.domain.event.repository;

import com.seatsync.domain.event.entity.Event;

import java.util.List;

public interface EventRepositoryCustom {
    List<Event> searchEvents(String genre, String venue, String keyword);
}