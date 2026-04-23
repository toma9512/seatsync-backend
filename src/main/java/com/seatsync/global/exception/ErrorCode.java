package com.seatsync.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 공통
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "잘못된 입력입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 리소스입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."),

    // 유저
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다."),

    // 공연
    EVENT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 공연입니다."),

    // 회차
    SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 회차입니다."),

    // 좌석
    SEAT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 좌석입니다."),
    SEAT_NOT_AVAILABLE(HttpStatus.CONFLICT, "이미 선점되었거나 예약된 좌석입니다."),
    SEAT_ALREADY_HELD(HttpStatus.CONFLICT, "이미 다른 사용자가 선점한 좌석입니다."),

    // 예약
    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 예약입니다."),
    RESERVATION_EXPIRED(HttpStatus.GONE, "선점 시간이 만료되었습니다. 다시 시도해주세요."),
    RESERVATION_UNAUTHORIZED(HttpStatus.FORBIDDEN, "본인의 예약만 취소할 수 있습니다.");

    private final HttpStatus status;
    private final String message;
}