package com.study.trip.application.service;

import com.study.trip.application.dto.CreateTripRequest;
import com.study.trip.application.dto.TripResponse;
import com.study.trip.domain.entity.Trip;
import com.study.trip.domain.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TripService {

    private final TripRepository tripRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Transactional
    public TripResponse createTrip(CreateTripRequest request) {
        Trip trip = Trip.builder()
                .userId(request.getUserId())
                .title(request.getTitle())
                .description(request.getDescription())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();
        Trip saved = tripRepository.save(trip);
        kafkaTemplate.send("trip-events", "TRIP_CREATED:" + saved.getId());
        return TripResponse.from(saved);
    }

    public TripResponse getTrip(Long id) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Trip not found: " + id));
        return TripResponse.from(trip);
    }

    public List<TripResponse> getTripsByUser(Long userId) {
        return tripRepository.findByUserId(userId).stream()
                .map(TripResponse::from)
                .toList();
    }
}
