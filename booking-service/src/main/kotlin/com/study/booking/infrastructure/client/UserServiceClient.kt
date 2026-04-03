package com.study.booking.infrastructure.client

import com.study.common.exception.EntityNotFoundException
import com.study.common.exception.ServiceUnavailableException
import com.study.grpc.user.CheckUserExistsRequest
import com.study.grpc.user.UserGrpcServiceGrpc
import io.grpc.StatusRuntimeException
import net.devh.boot.grpc.client.inject.GrpcClient
import org.springframework.stereotype.Component

@Component
class UserServiceClient {

    @GrpcClient("user-service")
    private lateinit var userStub: UserGrpcServiceGrpc.UserGrpcServiceBlockingStub

    fun verifyUserExists(userId: Long) {
        try {
            val response = userStub.checkUserExists(
                CheckUserExistsRequest.newBuilder().setUserId(userId).build()
            )
            if (!response.exists) {
                throw EntityNotFoundException("User", userId)
            }
        } catch (e: StatusRuntimeException) {
            throw ServiceUnavailableException("user-service")
        }
    }
}
