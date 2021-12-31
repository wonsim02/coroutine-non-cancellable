package com.example.coroutinenoncancellable.grpc.service

import com.example.coroutinenoncancellable.App
import com.example.coroutinenoncancellable.component.SampleComponent
import com.example.coroutinenoncancellable.grpc.SampleServiceGrpcKt
import com.example.coroutinenoncancellable.grpc.SampleServiceOuterClass.ClearIntsRequest
import com.example.coroutinenoncancellable.grpc.SampleServiceOuterClass.ListIntsRequest
import com.example.coroutinenoncancellable.grpc.SampleServiceOuterClass.PutTwoIntsRequest
import com.ninjasquad.springmockk.SpykBean
import io.grpc.ManagedChannelBuilder
import io.mockk.clearAllMocks
import io.mockk.coEvery
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.lognet.springboot.grpc.autoconfigure.GRpcServerProperties
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(
    classes = [App::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
class GrpcSampleServiceTest {
    @Autowired
    private lateinit var grpcServiceProperties: GRpcServerProperties

    @SpykBean
    private lateinit var sampleComponent: SampleComponent

    private val logger = LoggerFactory.getLogger("rpc call test")
    private val stub: SampleServiceGrpcKt.SampleServiceCoroutineStub by lazy {
        val port = grpcServiceProperties.runningPort
        val channel = ManagedChannelBuilder
            .forAddress("localhost", port)
            .usePlaintext()
            .build()
        SampleServiceGrpcKt.SampleServiceCoroutineStub(channel)
    }

    @AfterEach
    fun clear(): Unit = runBlocking {
        clearAllMocks()
        stub.clearInts(ClearIntsRequest.getDefaultInstance())
    }

    @Test
    fun `cancellable rpc call`(): Unit = runBlocking {
        // given
        val a = 1
        val b = 2

        var functionCalled = false
        coEvery { sampleComponent.perform() } coAnswers {
            logger.info("Enter sampleComponent.perform() call")
            functionCalled = true
            delay(1500L)
            logger.info("Escape sampleComponent.perform() call")
        }

        val callRpcJob = launch {
            logger.info("Send request CancellablePutTwoInts")
            stub.cancellablePutTwoInts(
                PutTwoIntsRequest.newBuilder()
                    .setA(a).setB(b).build()
            )
        }

        while (!functionCalled) { delay(500L) }
        logger.info("Cancel request CancellablePutTwoInts")
        callRpcJob.cancelAndJoin()
        delay(1500L)

        val response = stub.listInts(ListIntsRequest.getDefaultInstance())
        val ints = response.intsList

        Assertions.assertTrue(ints.contains(a))
        Assertions.assertFalse(ints.contains(b))
    }

    @Test
    fun `non cancellable rpc call`(): Unit = runBlocking {
        // given
        val a = 1
        val b = 2

        var functionCalled = false
        coEvery { sampleComponent.perform() } coAnswers {
            logger.info("Enter sampleComponent.perform() call")
            functionCalled = true
            delay(1500L)
            logger.info("Escape sampleComponent.perform() call")
        }

        val callRpcJob = launch {
            logger.info("Send request NonCancellablePutTwoInts")
            stub.nonCancellablePutTwoInts(
                PutTwoIntsRequest.newBuilder()
                    .setA(a).setB(b).build()
            )
        }

        while (!functionCalled) { delay(500L) }
        logger.info("Cancel request NonCancellablePutTwoInts")
        callRpcJob.cancelAndJoin()
        delay(1500L)

        val response = stub.listInts(ListIntsRequest.getDefaultInstance())
        val ints = response.intsList

        Assertions.assertTrue(ints.contains(a))
        Assertions.assertTrue(ints.contains(b))
    }
}
