.class public Lloops/TestProtectedPollingDelayStateDispatch;
.super Lkotlin/coroutines/jvm/internal/SuspendLambda;

.field private label:I
.field private L$0:Ljava/lang/Object;
.field private J$0:J
.field private J$1:J
.field private J$2:J

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 15

    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v0
    iget v1, p0, Lloops/TestProtectedPollingDelayStateDispatch;->label:I
    packed-switch v1, :state_switch
    goto :bad_state

    :resume_delay
    iget-object v2, p0, Lloops/TestProtectedPollingDelayStateDispatch;->L$0:Ljava/lang/Object;
    iget-wide v3, p0, Lloops/TestProtectedPollingDelayStateDispatch;->J$0:J
    iget-wide v5, p0, Lloops/TestProtectedPollingDelayStateDispatch;->J$1:J
    iget-wide v7, p0, Lloops/TestProtectedPollingDelayStateDispatch;->J$2:J
    :try_start_resume
    invoke-static/range {p1 .. p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    :try_end_resume
    .catch Ljava/lang/Exception; {:try_start_resume .. :try_end_resume} :catch_error
    move-wide v7, v3
    const/4 v11, 0x4
    const/4 v12, 0x0
    move-wide v3, v7
    goto :duration_decision

    :initial
    invoke-static/range {p1 .. p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    const-string v2, "poll"
    invoke-static {}, Ljava/lang/System;->currentTimeMillis()J
    move-result-wide v3
    const-wide/16 v5, 0x64

    :poll_loop
    invoke-static {}, Ljava/lang/System;->currentTimeMillis()J
    move-result-wide v7
    invoke-static {}, Ljava/lang/System;->currentTimeMillis()J
    move-result-wide v9
    sub-long/2addr v9, v7
    const-wide/16 v11, 0xa
    sub-long/2addr v11, v9
    const-wide/16 v9, 0x0

    :try_start_delay
    invoke-static {v9, v10, v11, v12}, Ljava/lang/Math;->max(JJ)J
    move-result-wide v11
    cmp-long v9, v11, v9
    if-lez v9, :zero_delay

    iput-object v2, p0, Lloops/TestProtectedPollingDelayStateDispatch;->L$0:Ljava/lang/Object;
    iput-wide v3, p0, Lloops/TestProtectedPollingDelayStateDispatch;->J$0:J
    move-wide v9, v7
    iput-wide v9, p0, Lloops/TestProtectedPollingDelayStateDispatch;->J$1:J
    iput-wide v11, p0, Lloops/TestProtectedPollingDelayStateDispatch;->J$2:J
    const/4 v1, 0x1
    iput v1, p0, Lloops/TestProtectedPollingDelayStateDispatch;->label:I
    invoke-static {v11, v12, p0}, Lkotlinx/coroutines/DelayKt;->delay(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    :try_end_delay
    .catch Ljava/lang/Exception; {:try_start_delay .. :try_end_delay} :catch_error

    move-object v9, v0
    if-eq p1, v9, :suspended
    goto :shared_direct

    :zero_delay
    move-object v9, v0

    :shared_direct
    move-wide v7, v3
    move-wide v11, v5
    move-wide v3, v7
    move-wide v5, v11

    :duration_decision
    :try_start_duration
    invoke-static {}, Ljava/lang/System;->currentTimeMillis()J
    move-result-wide v7
    sub-long/2addr v7, v3
    cmp-long v9, v7, v5
    if-gez v9, :timeout
    goto :poll_loop

    :timeout
    new-instance v1, Ljava/util/concurrent/TimeoutException;
    invoke-direct {v1}, Ljava/util/concurrent/TimeoutException;-><init>()V
    throw v1
    :try_end_duration
    .catch Ljava/lang/Exception; {:try_start_duration .. :try_end_duration} :catch_error

    :catch_error
    move-exception v1
    sget-object v1, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object v1

    :bad_state
    new-instance v1, Ljava/lang/IllegalStateException;
    invoke-direct {v1}, Ljava/lang/IllegalStateException;-><init>()V
    throw v1

    :suspended
    return-object v9

    :state_switch
    .packed-switch 0x0
        :initial
        :resume_delay
    .end packed-switch
.end method
