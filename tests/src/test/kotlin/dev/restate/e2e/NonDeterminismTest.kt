// Copyright (c) 2023 - Restate Software, Inc., Restate GmbH
//
// This file is part of the Restate e2e tests,
// which are released under the MIT license.
//
// You can find a copy of the license in file LICENSE in the root
// directory of this repository or package, or at
// https://github.com/restatedev/e2e/blob/main/LICENSE

package dev.restate.e2e

import com.google.protobuf.Empty
import dev.restate.e2e.services.counter.CounterGrpc
import dev.restate.e2e.services.counter.CounterGrpc.CounterBlockingStub
import dev.restate.e2e.services.counter.CounterProto
import dev.restate.e2e.services.nondeterminism.NonDeterminismProto.NonDeterministicRequest
import dev.restate.e2e.services.nondeterminism.NonDeterministicServiceGrpc
import dev.restate.e2e.utils.*
import dev.restate.e2e.utils.ServiceSpec.*
import io.grpc.*
import io.grpc.stub.ClientCalls
import java.util.*
import java.util.stream.Stream
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.InstanceOfAssertFactories
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.extension.RegisterExtension
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

@Tag("only-always-suspending")
class JavaNonDeterminismTest : NonDeterminismTest() {
  companion object {
    @RegisterExtension
    val deployerExt: RestateDeployerExtension =
        RestateDeployerExtension(
            RestateDeployer.Builder()
                .withEnv(Containers.getRestateEnvironment())
                .withInvokerRetryPolicy(RestateDeployer.RetryPolicy.None)
                .withServiceEndpoint(
                    Containers.javaServicesContainer(
                        "java-non-determinism",
                        NonDeterministicServiceGrpc.SERVICE_NAME,
                        CounterGrpc.SERVICE_NAME))
                .build())
  }
}

@Tag("only-always-suspending")
class NodeNonDeterminismTest : NonDeterminismTest() {
  companion object {
    @RegisterExtension
    val deployerExt: RestateDeployerExtension =
        RestateDeployerExtension(
            RestateDeployer.Builder()
                .withEnv(Containers.getRestateEnvironment())
                // Disable the retries so we get the error propagated back
                .withInvokerRetryPolicy(RestateDeployer.RetryPolicy.None)
                .withServiceEndpoint(
                    Containers.nodeServicesContainer(
                        "node-non-determinism",
                        NonDeterministicServiceGrpc.SERVICE_NAME,
                        CounterGrpc.SERVICE_NAME))
                .build())
  }
}

/** Test non-determinism/journal mismatch checks in the SDKs. */
abstract class NonDeterminismTest {
  companion object {
    @JvmStatic
    fun method(): Stream<Arguments> {
      return NonDeterministicServiceGrpc.getServiceDescriptor().methods.stream().map {
        Arguments.of(it.bareMethodName, it)
      }
    }
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource
  @Execution(ExecutionMode.CONCURRENT)
  fun method(
      methodName: String,
      methodDescriptor: MethodDescriptor<NonDeterministicRequest, Empty>,
      @InjectChannel channel: ManagedChannel,
      @InjectBlockingStub counterService: CounterBlockingStub
  ) {
    val counterName = UUID.randomUUID().toString()

    Assertions.assertThatThrownBy {
          ClientCalls.blockingUnaryCall(
              channel,
              methodDescriptor,
              CallOptions.DEFAULT,
              NonDeterministicRequest.newBuilder().setKey(counterName).build())
        }
        .asInstanceOf(InstanceOfAssertFactories.type(StatusRuntimeException::class.java))
        .extracting(StatusRuntimeException::getStatus)
        .extracting(Status::getCode)
        // SDKs might return different error codes, because the gRPC error space doesn't have a
        // journal mismatch error type defined, and the service-protocol doesn't strictly enforce
        // the usage of JOURNAL_MISMATCH error code.
        .isIn(Status.Code.INTERNAL, Status.Code.UNKNOWN)

    // Assert the counter was not incremented
    assertThat(
            counterService
                .get(CounterProto.CounterRequest.newBuilder().setCounterName(methodName).build())
                .value)
        .isZero()
  }
}
