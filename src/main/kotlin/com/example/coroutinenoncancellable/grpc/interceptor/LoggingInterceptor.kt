package com.example.coroutinenoncancellable.grpc.interceptor

import com.google.protobuf.AbstractMessage
import com.google.protobuf.util.JsonFormat
import io.grpc.Context
import io.grpc.Contexts
import io.grpc.ForwardingServerCall
import io.grpc.ForwardingServerCallListener
import io.grpc.Metadata
import io.grpc.MethodDescriptor
import io.grpc.ServerCall
import io.grpc.ServerCallHandler
import io.grpc.ServerInterceptor
import io.grpc.Status
import org.lognet.springboot.grpc.GRpcGlobalInterceptor
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@GRpcGlobalInterceptor
class LoggingInterceptor : ServerInterceptor {
    private val logger = LoggerFactory.getLogger("GrpcLogger")

    override fun <ReqT : Any?, RespT : Any?> interceptCall(
        call: ServerCall<ReqT, RespT>,
        headers: Metadata,
        next: ServerCallHandler<ReqT, RespT>?,
    ): ServerCall.Listener<ReqT> {
        val context = Context.current().withMethodMetadata(call.methodDescriptor)

        val onResponseApplied = ConfigurableInterceptorOnResponse(context, logger, call)
        val onErrorApplied = ConfigurableInterceptorOnError(context, logger, onResponseApplied)

        val serverCall = Contexts.interceptCall(context, onErrorApplied, headers, next)

        return ConfigurableInterceptorOnRequest(context, logger, serverCall)
    }

    internal class ConfigurableInterceptorOnResponse<ReqT, RespT>(
        private val context: Context,
        private val logger: Logger,
        serverCall: ServerCall<ReqT, RespT>,
    ) : ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT>(serverCall) {
        override fun sendMessage(message: RespT) {
            val method = METHOD_KEY[context]
            val protoMessage = message as? AbstractMessage
            val jsonFormattedMessage = protoMessage?.toJsonFormat()

            logger.info("Response to $method with response payload: $jsonFormattedMessage")
            super.sendMessage(message)
        }
    }

    internal class ConfigurableInterceptorOnRequest<ReqT>(
        private val context: Context,
        private val logger: Logger,
        listener: ServerCall.Listener<ReqT>,
    ) : ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(listener) {
        override fun onMessage(message: ReqT) {
            val method = METHOD_KEY[context]
            val protoMessage = message as? AbstractMessage
            val jsonFormattedMessage = protoMessage?.toJsonFormat()

            logger.info("Request to $method with request payload: $jsonFormattedMessage")
            super.onMessage(message)
        }
    }

    internal class ConfigurableInterceptorOnError<ReqT, RespT>(
        private val context: Context,
        private val logger: Logger,
        serverCall: ServerCall<ReqT, RespT>
    ) : ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT>(serverCall) {
        override fun close(status: Status, trailers: Metadata) {
            val method = METHOD_KEY[context]
            logger.warn("Response to $method with status: ${status.code}")
            super.close(status, trailers)
        }
    }

    companion object {
        private val METHOD_KEY: Context.Key<String> = Context.keyWithDefault("method", null)
        private val jsonFormatPrinter = JsonFormat.printer().omittingInsignificantWhitespace()

        private fun Context.withMethodMetadata(methodDescriptor: MethodDescriptor<*, *>): Context {
            return withValue(METHOD_KEY, methodDescriptor.fullMethodName)
        }

        private fun AbstractMessage.toJsonFormat(): String {
            return jsonFormatPrinter.print(this)
        }
    }
}
