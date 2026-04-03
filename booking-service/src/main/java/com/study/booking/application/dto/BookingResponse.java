package com.study.booking.application.dto;

import com.study.booking.domain.entity.Booking;
import com.study.booking.domain.entity.BookingStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class BookingResponse {
    private Long id;
    private Long userId;
    private Long tripId;
    private BookingStatus status;
    private LocalDateTime bookedAt;
    private LocalDateTime createdAt;

    public static BookingResponse from(Booking booking) {
        return BookingResponse.builder()
                .id(booking.getId())
                .userId(booking.getUserId())
                .tripId(booking.getTripId())
                .status(booking.getStatus())
                .bookedAt(booking.getBookedAt())
                .createdAt(booking.getCreatedAt())
                .build();
    }
}
