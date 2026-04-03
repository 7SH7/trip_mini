package com.study.trip.infrastructure.grpc

import com.study.grpc.trip.*
import com.study.trip.domain.repository.TripRepository
import io.grpc.Status
import io.grpc.stub.StreamObserver
import net.devh.boot.grpc.server.service.GrpcService

@GrpcService
class TripGrpcServer(
    private val tripRepository: TripRepository
) : TripGrpcServiceGrpc.TripGrpcServiceImplBase() {

    override fun getTrip(request: GetTripRequest, responseObserver: StreamObserver<GetTripResponse>) {
        val trip = tripRepository.findById(request.tripId).orElse(null)
        if (trip == null) {
            responseObserver.onError(Status.NOT_FOUND.withDescription("Trip not found: ${request.tripId}").asRuntimeException())
            return
        }
        val response = GetTripResponse.newBuilder()
            .setId(trip.id)
            .setUserId(trip.userId)
            .setTitle(trip.title)
            .setStatus(trip.status.name)
            .build()
        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }

    override fun checkTripExists(request: CheckTripExistsRequest, responseObserver: StreamObserver<CheckTripExistsResponse>) {
        val exists = tripRepository.existsById(request.tripId)
        val response = CheckTripExistsResponse.newBuilder()
            .setExists(exists)
            .build()
        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }
}
