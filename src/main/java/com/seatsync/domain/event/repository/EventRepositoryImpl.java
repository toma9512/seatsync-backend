package com.seatsync.domain.event.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.seatsync.domain.event.entity.Event;
import com.seatsync.domain.event.entity.QEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class EventRepositoryImpl implements EventRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Event> searchEvents(String genre, String venue, String keyword) {
        QEvent event = QEvent.event;

        return queryFactory
                .selectFrom(event)
                .where(
                        genreEq(genre),
                        venueContains(venue),
                        keywordContains(keyword)
                )
                .orderBy(event.createdAt.desc())
                .fetch();
    }

    private BooleanExpression genreEq(String genre) {
        return genre != null ? QEvent.event.genre.eq(genre) : null;
    }

    private BooleanExpression venueContains(String venue) {
        return venue != null ? QEvent.event.venue.contains(venue) : null;
    }

    private BooleanExpression keywordContains(String keyword) {
        return keyword != null ? QEvent.event.title.contains(keyword) : null;
    }
}