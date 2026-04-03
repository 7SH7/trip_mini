package com.study.booking.application.service;

import com.study.booking.application.dto.CreateBookingRequest;
import com.study.booking.application.dto.BookingResponse;
import com.study.booking.domain.entity.Booking;
import com.study.booking.domain.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingService {

    private final BookingRepository bookingRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Transactional
    public BookingResponse createBooking(Long userId, CreateBookingRequest request) {
        Booking booking = Booking.builder()
                .userId(userId)
                .tripId(request.getTripId())
                .build();
        Booking saved = bookingRepository.save(booking);
        kafkaTemplate.send("booking-events", "BOOKING_CREATED:" + saved.getId());
        return BookingResponse.from(saved);
    }

    public BookingResponse getBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + id));
        return BookingResponse.from(booking);
    }

    public List<BookingResponse> getBookingsByUser(Long userId) {
        return bookingRepository.findByUserId(userId).stream()
                .map(BookingResponse::from)
                .toList();
    }

    @Transactional
    public BookingResponse confirmBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + id));
        booking.confirm();
        kafkaTemplate.send("booking-events", "BOOKING_CONFIRMED:" + id);
        return BookingResponse.from(booking);
    }
}
