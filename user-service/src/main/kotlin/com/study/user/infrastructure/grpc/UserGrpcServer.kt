package com.study.user.infrastructure.grpc

import com.study.grpc.user.*
import com.study.user.domain.repository.UserRepository
import io.grpc.Status
import io.grpc.stub.StreamObserver
import net.devh.boot.grpc.server.service.GrpcService

@GrpcService
class UserGrpcServer(
    private val userRepository: UserRepository
) : UserGrpcServiceGrpc.UserGrpcServiceImplBase() {

    override fun getUser(request: GetUserRequest, responseObserver: StreamObserver<GetUserResponse>) {
        val user = userRepository.findById(request.userId).orElse(null)
        if (user == null) {
            responseObserver.onError(Status.NOT_FOUND.withDescription("User not found: ${request.userId}").asRuntimeException())
            return
        }
        val response = GetUserResponse.newBuilder()
            .setId(user.id)
            .setEmail(user.email)
            .setName(user.name)
            .build()
        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }

    override fun checkUserExists(request: CheckUserExistsRequest, responseObserver: StreamObserver<CheckUserExistsResponse>) {
        val exists = userRepository.existsById(request.userId)
        val response = CheckUserExistsResponse.newBuilder()
            .setExists(exists)
            .build()
        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }
}
