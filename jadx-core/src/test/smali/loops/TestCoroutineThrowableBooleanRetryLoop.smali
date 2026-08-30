.class public Lloops/TestCoroutineThrowableBooleanRetryLoop;
.super Ljava/lang/Object;

.field private final upstream:Ltest/RetryUpstream;
.field private final predicate:Ltest/RetryPredicate;

.method public final retry(Ltest/Collector;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 13

    instance-of v0, p2, Lloops/TestCoroutineThrowableBooleanRetryLoop$retry$1;
    if-eqz v0, :new_continuation
    move-object v0, p2
    check-cast v0, Lloops/TestCoroutineThrowableBooleanRetryLoop$retry$1;
    iget v2, v0, Lloops/TestCoroutineThrowableBooleanRetryLoop$retry$1;->label:I
    const/high16 v3, -0x80000000
    and-int v4, v2, v3
    if-eqz v4, :new_continuation
    sub-int/2addr v2, v3
    iput v2, v0, Lloops/TestCoroutineThrowableBooleanRetryLoop$retry$1;->label:I
    goto :dispatch

    :new_continuation
    new-instance v0, Lloops/TestCoroutineThrowableBooleanRetryLoop$retry$1;
    invoke-direct {v0, p0, p2}, Lloops/TestCoroutineThrowableBooleanRetryLoop$retry$1;-><init>(Lloops/TestCoroutineThrowableBooleanRetryLoop;Lkotlin/coroutines/Continuation;)V

    :dispatch
    iget-object v12, v0, Lloops/TestCoroutineThrowableBooleanRetryLoop$retry$1;->result:Ljava/lang/Object;
    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v1
    iget v2, v0, Lloops/TestCoroutineThrowableBooleanRetryLoop$retry$1;->label:I
    const/4 v3, 0x2
    const/4 v4, 0x1
    if-eqz v2, :initial
    if-eq v2, v4, :state_one
    if-ne v2, v3, :bad_state

    :state_two
    iget-wide v5, v0, Lloops/TestCoroutineThrowableBooleanRetryLoop$retry$1;->J$0:J
    iget-object v11, v0, Lloops/TestCoroutineThrowableBooleanRetryLoop$retry$1;->T$0:Ljava/lang/Throwable;
    iget-object v2, v0, Lloops/TestCoroutineThrowableBooleanRetryLoop$retry$1;->L$0:Ljava/lang/Object;
    check-cast v2, Ltest/Collector;
    iget-object v7, v0, Lloops/TestCoroutineThrowableBooleanRetryLoop$retry$1;->L$1:Ljava/lang/Object;
    check-cast v7, Lloops/TestCoroutineThrowableBooleanRetryLoop;
    invoke-static {v12}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :boolean_projection

    :state_one
    iget-wide v5, v0, Lloops/TestCoroutineThrowableBooleanRetryLoop$retry$1;->J$0:J
    iget-object v11, v0, Lloops/TestCoroutineThrowableBooleanRetryLoop$retry$1;->L$0:Ljava/lang/Object;
    check-cast v11, Ltest/Collector;
    iget-object v2, v0, Lloops/TestCoroutineThrowableBooleanRetryLoop$retry$1;->L$1:Ljava/lang/Object;
    check-cast v2, Lloops/TestCoroutineThrowableBooleanRetryLoop;
    invoke-static {v12}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    move-object v7, v2

    :collector_handoff
    move-object v2, v11
    goto :throwable_projection

    :initial
    invoke-static {v12}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    const-wide/16 v5, 0x0
    move-object v12, p0

    :retry_loop
    iget-object v2, v12, Lloops/TestCoroutineThrowableBooleanRetryLoop;->upstream:Ltest/RetryUpstream;
    iput-object v12, v0, Lloops/TestCoroutineThrowableBooleanRetryLoop$retry$1;->L$1:Ljava/lang/Object;
    iput-object v11, v0, Lloops/TestCoroutineThrowableBooleanRetryLoop$retry$1;->L$0:Ljava/lang/Object;
    iput-wide v5, v0, Lloops/TestCoroutineThrowableBooleanRetryLoop$retry$1;->J$0:J
    iput v4, v0, Lloops/TestCoroutineThrowableBooleanRetryLoop$retry$1;->label:I
    invoke-interface {v2, v11, v0}, Ltest/RetryUpstream;->catchError(Ltest/Collector;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v2
    if-ne v2, v1, :direct_first
    return-object v1

    :direct_first
    move-object v7, v12
    move-object v12, v2
    goto :collector_handoff

    :throwable_projection
    move-object v11, v12
    check-cast v11, Ljava/lang/Throwable;
    if-eqz v11, :no_failure

    iget-object v12, v7, Lloops/TestCoroutineThrowableBooleanRetryLoop;->predicate:Ltest/RetryPredicate;
    new-instance v8, Ljava/lang/Long;
    invoke-direct {v8, v5, v6}, Ljava/lang/Long;-><init>(J)V
    iput-object v7, v0, Lloops/TestCoroutineThrowableBooleanRetryLoop$retry$1;->L$1:Ljava/lang/Object;
    iput-object v2, v0, Lloops/TestCoroutineThrowableBooleanRetryLoop$retry$1;->L$0:Ljava/lang/Object;
    iput-object v11, v0, Lloops/TestCoroutineThrowableBooleanRetryLoop$retry$1;->T$0:Ljava/lang/Throwable;
    iput-wide v5, v0, Lloops/TestCoroutineThrowableBooleanRetryLoop$retry$1;->J$0:J
    iput v3, v0, Lloops/TestCoroutineThrowableBooleanRetryLoop$retry$1;->label:I
    invoke-interface {v12, v2, v11, v8, v0}, Ltest/RetryPredicate;->shouldRetry(Ltest/Collector;Ljava/lang/Throwable;Ljava/lang/Long;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v12
    if-ne v12, v1, :boolean_projection
    return-object v1

    :boolean_projection
    check-cast v12, Ljava/lang/Boolean;
    invoke-virtual {v12}, Ljava/lang/Boolean;->booleanValue()Z
    move-result v12
    if-eqz v12, :do_not_retry
    const-wide/16 v11, 0x1
    add-long/2addr v5, v11
    const/4 v11, 0x1

    :carry
    move-object v12, v7
    if-eqz v11, :done
    move-object v11, v2
    goto :retry_loop

    :no_failure
    const/4 v11, 0x0
    goto :carry

    :do_not_retry
    throw v11

    :done
    sget-object v1, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object v1

    :bad_state
    new-instance v9, Ljava/lang/IllegalStateException;
    invoke-direct {v9}, Ljava/lang/IllegalStateException;-><init>()V
    throw v9
.end method
