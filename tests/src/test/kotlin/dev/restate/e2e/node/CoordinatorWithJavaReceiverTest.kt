package dev.restate.e2e.node

import dev.restate.e2e.Containers
import dev.restate.e2e.functions.coordinator.CoordinatorGrpc
import dev.restate.e2e.functions.receiver.ReceiverGrpc
import dev.restate.e2e.multi.BaseCoordinatorTest
import dev.restate.e2e.utils.RestateDeployer
import dev.restate.e2e.utils.RestateDeployerExtension
import org.junit.jupiter.api.extension.RegisterExtension

class CoordinatorWithJavaReceiverTest : BaseCoordinatorTest() {
  companion object {
    @RegisterExtension
    val deployerExt: RestateDeployerExtension =
        RestateDeployerExtension(
            RestateDeployer.Builder()
                .withEnv(Containers.getRestateEnvironment())
                .withServiceEndpoint(
                    Containers.nodeServicesContainer(
                        "node-coordinator", CoordinatorGrpc.SERVICE_NAME))
                .withServiceEndpoint(
                    Containers.javaServicesContainer("java-coordinator", ReceiverGrpc.SERVICE_NAME))
                .build())
  }
}
