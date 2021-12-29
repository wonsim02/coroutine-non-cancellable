package com.example.coroutinenoncancellable.grpc.service

import com.example.coroutinenoncancellable.component.SampleComponent
import com.example.coroutinenoncancellable.grpc.SampleServiceGrpcKt
import com.example.coroutinenoncancellable.grpc.SampleServiceOuterClass.ClearIntsRequest
import com.example.coroutinenoncancellable.grpc.SampleServiceOuterClass.ClearIntsResponse
import com.example.coroutinenoncancellable.grpc.SampleServiceOuterClass.ListIntsRequest
import com.example.coroutinenoncancellable.grpc.SampleServiceOuterClass.ListIntsResponse
import com.example.coroutinenoncancellable.grpc.SampleServiceOuterClass.PutTwoIntsRequest
import com.example.coroutinenoncancellable.grpc.SampleServiceOuterClass.PutTwoIntsResponse
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import org.lognet.springboot.grpc.GRpcService

@GRpcService
class GrpcSampleService(
    private val sampleComponent: SampleComponent,
) : SampleServiceGrpcKt.SampleServiceCoroutineImplBase() {
    override suspend fun cancellablePutTwoInts(request: PutTwoIntsRequest): PutTwoIntsResponse {
        val (a, b) = request.a to request.b

        sampleComponent.store(a)
        sampleComponent.perform()
        sampleComponent.store(b)

        return PutTwoIntsResponse.getDefaultInstance()
    }

    override suspend fun nonCancellablePutTwoInts(request: PutTwoIntsRequest): PutTwoIntsResponse {
        val (a, b) = request.a to request.b

        withContext(NonCancellable) {
            sampleComponent.store(a)
            sampleComponent.perform()
            sampleComponent.store(b)
        }

        return PutTwoIntsResponse.getDefaultInstance()
    }

    override suspend fun listInts(request: ListIntsRequest): ListIntsResponse {
        return ListIntsResponse.newBuilder()
            .addAllInts(sampleComponent.list())
            .build()
    }

    override suspend fun clearInts(request: ClearIntsRequest): ClearIntsResponse {
        sampleComponent.clear()
        return ClearIntsResponse.getDefaultInstance()
    }
}
