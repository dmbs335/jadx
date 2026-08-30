.class public Lloops/TestProtectedCoroutineDurationDecision;
.super Lkotlin/coroutines/jvm/internal/SuspendLambda;

.field private label:I
.field private rounds:I
.field private J$0:J
.field private J$1:J
.field private L$0:Ljava/lang/Object;

.method private static suspendCallback(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 1
    const/4 v0, 0x0
    return-object v0
.end method

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 14

    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v0
    iget v1, p0, Lloops/TestProtectedCoroutineDurationDecision;->label:I
    packed-switch v1, :state_switch
    goto :bad_state

    :resume_delay
    iget-wide v3, p0, Lloops/TestProtectedCoroutineDurationDecision;->J$0:J
    iget-wide v5, p0, Lloops/TestProtectedCoroutineDurationDecision;->J$1:J
    iget-object v1, p0, Lloops/TestProtectedCoroutineDurationDecision;->L$0:Ljava/lang/Object;
    check-cast v1, Ljava/lang/String;
    :try_start_resume_delay
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    :try_end_resume_delay
    .catch Ljava/lang/Exception; {:try_start_resume_delay .. :try_end_resume_delay} :catch_error
    move-wide v7, v3
    move-wide v9, v5
    goto :duration_decision

    :resume_callback
    iget-wide v3, p0, Lloops/TestProtectedCoroutineDurationDecision;->J$0:J
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :after_callback

    :initial
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    invoke-static {}, Ljava/lang/System;->currentTimeMillis()J
    move-result-wide v3

    :poll_loop
    invoke-static {}, Ljava/lang/System;->currentTimeMillis()J
    move-result-wide v5
    iput-wide v3, p0, Lloops/TestProtectedCoroutineDurationDecision;->J$0:J
    const/4 v1, 0x1
    iput v1, p0, Lloops/TestProtectedCoroutineDurationDecision;->label:I
    invoke-static {p0}, Lloops/TestProtectedCoroutineDurationDecision;->suspendCallback(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-eq p1, v0, :suspended

    :after_callback
    invoke-static {}, Ljava/lang/System;->currentTimeMillis()J
    move-result-wide v7
    sub-long/2addr v7, v5
    const-wide/16 v9, 0xa
    sub-long/2addr v9, v7
    const-wide/16 v7, 0x0
    invoke-static {v7, v8, v9, v10}, Ljava/lang/Math;->max(JJ)J
    move-result-wide v9
    cmp-long v1, v9, v7
    if-lez v1, :zero_delay

    :try_start_delay
    const-string v1, "poll"
    iput-object v1, p0, Lloops/TestProtectedCoroutineDurationDecision;->L$0:Ljava/lang/Object;
    iput-wide v3, p0, Lloops/TestProtectedCoroutineDurationDecision;->J$0:J
    const-wide/16 v5, 0x64
    iput-wide v5, p0, Lloops/TestProtectedCoroutineDurationDecision;->J$1:J
    const/4 v1, 0x2
    iput v1, p0, Lloops/TestProtectedCoroutineDurationDecision;->label:I
    invoke-static {v9, v10, p0}, Lkotlinx/coroutines/DelayKt;->delay(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    :try_end_delay
    .catch Ljava/lang/Exception; {:try_start_delay .. :try_end_delay} :catch_error
    move-object v1, v0
    const-wide/16 v9, 0x64
    if-ne p1, v1, :duration_decision
    return-object v1

    :zero_delay
    move-object v1, v0
    const-wide/16 v9, 0x64

    :duration_decision
    :try_start_duration
    invoke-static {}, Ljava/lang/System;->currentTimeMillis()J
    move-result-wide v7
    sub-long/2addr v7, v3
    cmp-long v1, v7, v9
    if-gez v1, :timeout

    move-wide v5, v7
    const/4 v1, 0x0
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
    return-object v0

    :state_switch
    .packed-switch 0x0
        :initial
        :resume_callback
        :resume_delay
    .end packed-switch
.end method
