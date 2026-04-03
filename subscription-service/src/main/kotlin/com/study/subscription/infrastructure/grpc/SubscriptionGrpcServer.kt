package com.study.subscription.infrastructure.grpc

import com.study.grpc.subscription.*
import com.study.subscription.domain.repository.SubscriptionRepository
import com.study.subscription.domain.entity.Subscription
import io.grpc.stub.StreamObserver
import net.devh.boot.grpc.server.service.GrpcService

@GrpcService
class SubscriptionGrpcServer(
    private val subscriptionRepository: SubscriptionRepository
) : SubscriptionGrpcServiceGrpc.SubscriptionGrpcServiceImplBase() {

    override fun checkCredits(request: CheckCreditsRequest, responseObserver: StreamObserver<CheckCreditsResponse>) {
        val subscription = subscriptionRepository.findByUserId(request.userId).orElse(null)
        val response = if (subscription != null) {
            CheckCreditsResponse.newBuilder()
                .setHasCredits(subscription.hasCredits())
                .setRemainingCredits(subscription.videoCallCredits)
                .build()
        } else {
            CheckCreditsResponse.newBuilder()
                .setHasCredits(true)
                .setRemainingCredits(5)
                .build()
        }
        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }

    override fun useVideoCall(request: UseVideoCallRequest, responseObserver: StreamObserver<UseVideoCallResponse>) {
        val subscription = subscriptionRepository.findByUserId(request.userId)
            .orElseGet { subscriptionRepository.save(Subscription(userId = request.userId)) }

        val success = subscription.useVideoCall()
        if (success) subscriptionRepository.save(subscription)

        val response = UseVideoCallResponse.newBuilder()
            .setSuccess(success)
            .setRemainingCredits(subscription.videoCallCredits)
            .setMessage(if (success) "크레딧을 사용했습니다." else "크레딧이 부족합니다.")
            .build()
        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }
}
