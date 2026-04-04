package com.study.trip.application.service

import com.study.common.exception.EntityNotFoundException
import com.study.common.exception.InvalidRequestException
import com.study.trip.application.dto.CreatePlaceRequest
import com.study.trip.application.dto.TripPlaceResponse
import com.study.trip.application.dto.UpdatePlaceRequest
import com.study.trip.domain.entity.TripPlace
import com.study.trip.domain.repository.TripPlaceRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class TripPlaceService(
    private val placeRepository: TripPlaceRepository,
    private val memberService: TripMemberService
) {
    fun getPlaces(tripId: Long): List<TripPlaceResponse> =
        placeRepository.findByTripIdOrderByCreatedAtDesc(tripId).map { TripPlaceResponse.from(it) }

    @Transactional
    fun create(tripId: Long, userId: Long, request: CreatePlaceRequest): TripPlaceResponse {
        if (!memberService.isMember(tripId, userId)) throw InvalidRequestException("여행 멤버가 아닙니다.")
        val place = placeRepository.save(TripPlace(
            tripId = tripId, name = request.name, address = request.address,
            latitude = request.latitude, longitude = request.longitude,
            category = request.category, notes = request.notes, addedBy = userId
        ))
        return TripPlaceResponse.from(place)
    }

    @Transactional
    fun update(tripId: Long, placeId: Long, userId: Long, request: UpdatePlaceRequest): TripPlaceResponse {
        if (!memberService.isMember(tripId, userId)) throw InvalidRequestException("여행 멤버가 아닙니다.")
        val place = placeRepository.findById(placeId).orElseThrow { EntityNotFoundException("TripPlace", placeId) }
        place.update(request.name, request.address, request.latitude, request.longitude, request.category, request.notes)
        return TripPlaceResponse.from(place)
    }

    @Transactional
    fun delete(tripId: Long, placeId: Long, userId: Long) {
        if (!memberService.isMember(tripId, userId)) throw InvalidRequestException("여행 멤버가 아닙니다.")
        placeRepository.deleteById(placeId)
    }
}
