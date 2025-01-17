// Copyright (c) 2023 - Restate Software, Inc., Restate GmbH
//
// This file is part of the Restate e2e tests,
// which are released under the MIT license.
//
// You can find a copy of the license in file LICENSE in the root
// directory of this repository or package, or at
// https://github.com/restatedev/e2e/blob/main/LICENSE

package dev.restate.e2e.node

import dev.restate.e2e.Containers
import dev.restate.e2e.services.collections.list.ListProto.*
import dev.restate.e2e.services.collections.list.ListServiceGrpc.ListServiceBlockingStub
import dev.restate.e2e.utils.InjectBlockingStub
import dev.restate.e2e.utils.RestateDeployer
import dev.restate.e2e.utils.RestateDeployerExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode

/** Test that we can ser/de proto generated objects (check the source of ListService.append) */
@Tag("always-suspending")
class StateSerDeTest {
  companion object {
    @RegisterExtension
    val deployerExt: RestateDeployerExtension =
        RestateDeployerExtension(
            RestateDeployer.Builder()
                .withEnv(Containers.getRestateEnvironment())
                .withServiceEndpoint(Containers.NODE_COLLECTIONS_SERVICE_SPEC)
                .build())
  }

  @Test
  @Execution(ExecutionMode.CONCURRENT)
  fun addAndClear(@InjectBlockingStub listClient: ListServiceBlockingStub) {
    listClient.append(AppendRequest.newBuilder().setListName("list-a").setValue("1").build())
    listClient.append(AppendRequest.newBuilder().setListName("list-b").setValue("2").build())
    listClient.append(AppendRequest.newBuilder().setListName("list-a").setValue("3").build())
    listClient.append(AppendRequest.newBuilder().setListName("list-b").setValue("4").build())
    listClient.append(AppendRequest.newBuilder().setListName("list-a").setValue("5").build())
    listClient.append(AppendRequest.newBuilder().setListName("list-b").setValue("6").build())

    val listAContent =
        listClient.clear(Request.newBuilder().setListName("list-a").build()).valuesList
    val listBContent =
        listClient.clear(Request.newBuilder().setListName("list-b").build()).valuesList

    assertThat(listAContent).containsExactly("1", "3", "5")
    assertThat(listBContent).containsExactly("2", "4", "6")
  }
}
