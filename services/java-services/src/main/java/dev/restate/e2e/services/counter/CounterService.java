// Copyright (c) 2023 - Restate Software, Inc., Restate GmbH
//
// This file is part of the Restate e2e tests,
// which are released under the MIT license.
//
// You can find a copy of the license in file LICENSE in the root
// directory of this repository or package, or at
// https://github.com/restatedev/e2e/blob/main/LICENSE

package dev.restate.e2e.services.counter;

import static dev.restate.e2e.services.counter.CounterProto.*;

import com.google.protobuf.Empty;
import dev.restate.sdk.RestateBlockingService;
import dev.restate.sdk.common.CoreSerdes;
import dev.restate.sdk.common.StateKey;
import dev.restate.sdk.common.TerminalException;
import io.grpc.stub.StreamObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CounterService extends CounterGrpc.CounterImplBase implements RestateBlockingService {

  private static final Logger logger = LogManager.getLogger(CounterService.class);

  private static final StateKey<Long> COUNTER_KEY = StateKey.of("counter", CoreSerdes.LONG);

  @Override
  public void reset(CounterRequest request, StreamObserver<Empty> responseObserver) {
    var ctx = restateContext();

    logger.info("Counter '{}' cleaned up", request.getCounterName());

    ctx.clear(COUNTER_KEY);

    responseObserver.onNext(Empty.newBuilder().build());
    responseObserver.onCompleted();
  }

  @Override
  public void add(CounterAddRequest request, StreamObserver<Empty> responseObserver) {
    var ctx = restateContext();

    long counter = ctx.get(COUNTER_KEY).orElse(0L);
    logger.info("Old counter '{}' value: {}", request.getCounterName(), counter);

    counter += request.getValue();
    ctx.set(COUNTER_KEY, counter);

    logger.info("New counter '{}' value: {}", request.getCounterName(), counter);

    responseObserver.onNext(Empty.getDefaultInstance());
    responseObserver.onCompleted();
  }

  @Override
  public void addThenFail(CounterAddRequest request, StreamObserver<Empty> responseObserver) {
    var ctx = restateContext();

    long counter = ctx.get(COUNTER_KEY).orElse(0L);
    logger.info("Old counter value: {}", counter);

    counter += request.getValue();
    ctx.set(COUNTER_KEY, counter);

    logger.info("New counter value: {}", counter);

    throw new TerminalException(TerminalException.Code.INTERNAL, request.getCounterName());
  }

  @Override
  public void get(CounterRequest request, StreamObserver<GetResponse> responseObserver) {
    var ctx = restateContext();

    long counter = ctx.get(COUNTER_KEY).orElse(0L);
    logger.info("Get counter '{}' value: {}", request.getCounterName(), counter);

    GetResponse result = GetResponse.newBuilder().setValue(counter).build();

    responseObserver.onNext(result);
    responseObserver.onCompleted();
  }

  @Override
  public void getAndAdd(
      CounterAddRequest request, StreamObserver<CounterUpdateResult> responseObserver) {
    var ctx = restateContext();

    long oldCount = ctx.get(COUNTER_KEY).orElse(0L);
    long newCount = oldCount + request.getValue();
    ctx.set(COUNTER_KEY, newCount);

    logger.info("Old counter '{}' value: {}", request.getCounterName(), oldCount);
    logger.info("New counter '{}' value: {}", request.getCounterName(), newCount);

    responseObserver.onNext(
        CounterUpdateResult.newBuilder().setOldValue(oldCount).setNewValue(newCount).build());
    responseObserver.onCompleted();
  }

  @Override
  public void handleEvent(UpdateCounterEvent request, StreamObserver<Empty> responseObserver) {
    var ctx = restateContext();

    long counter = ctx.get(COUNTER_KEY).orElse(0L);
    logger.info("Old counter '{}' value: {}", request.getCounterName(), counter);

    counter += Long.parseLong(request.getPayload().toStringUtf8());
    ctx.set(COUNTER_KEY, counter);

    logger.info("New counter '{}' value: {}", request.getCounterName(), counter);

    responseObserver.onNext(Empty.getDefaultInstance());
    responseObserver.onCompleted();
  }
}
