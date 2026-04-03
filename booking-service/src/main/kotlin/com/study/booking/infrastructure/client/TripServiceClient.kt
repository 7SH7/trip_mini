package com.study.booking.infrastructure.client

import com.study.common.exception.EntityNotFoundException
import com.study.common.exception.ServiceUnavailableException
import com.study.grpc.trip.CheckTripExistsRequest
import com.study.grpc.trip.TripGrpcServiceGrpc
import io.grpc.StatusRuntimeException
import net.devh.boot.grpc.client.inject.GrpcClient
import org.springframework.stereotype.Component

@Component
class TripServiceClient {

    @GrpcClient("trip-service")
    private lateinit var tripStub: TripGrpcServiceGrpc.TripGrpcServiceBlockingStub

    fun verifyTripExists(tripId: Long) {
        try {
            val response = tripStub.checkTripExists(
                CheckTripExistsRequest.newBuilder().setTripId(tripId).build()
            )
            if (!response.exists) {
                throw EntityNotFoundException("Trip", tripId)
            }
        } catch (e: StatusRuntimeException) {
            throw ServiceUnavailableException("trip-service")
        }
    }
}
