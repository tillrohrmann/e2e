// Copyright (c) 2023 - Restate Software, Inc., Restate GmbH
//
// This file is part of the Restate e2e tests,
// which are released under the MIT license.
//
// You can find a copy of the license in file LICENSE in the root
// directory of this repository or package, or at
// https://github.com/restatedev/e2e/blob/main/LICENSE

package dev.restate.e2e.java

import dev.restate.e2e.Containers
import dev.restate.e2e.services.coordinator.CoordinatorGrpc.CoordinatorBlockingStub
import dev.restate.e2e.services.coordinator.CoordinatorProto
import dev.restate.e2e.utils.InjectBlockingStub
import dev.restate.e2e.utils.RestateDeployer
import dev.restate.e2e.utils.RestateDeployerExtension
import java.time.Duration
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode

// Only a Java test because the typescript SDK is still lacking this feature:
// https://github.com/restatedev/sdk-typescript/issues/21
@Tag("always-suspending")
class AwaitTimeoutTest {
  companion object {
    @RegisterExtension
    val deployerExt: RestateDeployerExtension =
        RestateDeployerExtension(
            RestateDeployer.Builder()
                .withEnv(Containers.getRestateEnvironment())
                .withServiceEndpoint(Containers.JAVA_COORDINATOR_SERVICE_SPEC)
                .build())
  }

  @Test
  @DisplayName("Test Awaitable#await(Duration)")
  @Execution(ExecutionMode.CONCURRENT)
  fun timeout(@InjectBlockingStub coordinatorClient: CoordinatorBlockingStub) {
    val timeout = Duration.ofMillis(100L)
    val response =
        coordinatorClient.timeout(
            CoordinatorProto.Duration.newBuilder().setMillis(timeout.toMillis()).build())

    assertThat(response.timeoutOccurred).isTrue
  }
}
