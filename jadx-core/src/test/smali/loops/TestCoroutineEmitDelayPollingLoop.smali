.class public Lloops/TestCoroutineEmitDelayPollingLoop;
.super Ljava/lang/Object;

.field private J$0:J
.field private J$1:J
.field private L$0:Ljava/lang/Object;
.field private duration:J
.field private label:I

.method private static boxLong(J)Ljava/lang/Object;
    .locals 1
    const/4 v0, 0x0
    return-object v0
.end method

.method private static currentPosition()J
    .locals 2
    const-wide/16 v0, 0xa
    return-wide v0
.end method

.method private static delay(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 1
    const/4 v0, 0x0
    return-object v0
.end method

.method private static emit(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 1
    const/4 v0, 0x0
    return-object v0
.end method

.method private static getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    .locals 1
    const/4 v0, 0x0
    return-object v0
.end method

.method private static repeatMode()I
    .locals 1
    const/4 v0, 0x0
    return v0
.end method

.method private static throwOnFailure(Ljava/lang/Object;)V
    .locals 0
    return-void
.end method

.method public invokeSuspend(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 12

    iget-object v0, p0, Lloops/TestCoroutineEmitDelayPollingLoop;->L$0:Ljava/lang/Object;
    check-cast v0, Ljava/lang/String;
    invoke-static {}, Lloops/TestCoroutineEmitDelayPollingLoop;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v1
    iget v2, p0, Lloops/TestCoroutineEmitDelayPollingLoop;->label:I
    const-wide/16 v3, 0x64
    const/4 v5, 0x1
    const/4 v6, 0x2
    if-eqz v2, :initial
    if-eq v2, v5, :resume_emit
    if-ne v2, v6, :bad_state

    :resume_delay
    iget-wide v7, p0, Lloops/TestCoroutineEmitDelayPollingLoop;->J$1:J
    invoke-static {p1}, Lloops/TestCoroutineEmitDelayPollingLoop;->throwOnFailure(Ljava/lang/Object;)V
    goto :loop_condition

    :bad_state
    new-instance p1, Ljava/lang/IllegalStateException;
    invoke-direct {p1}, Ljava/lang/IllegalStateException;-><init>()V
    throw p1

    :resume_emit
    iget-wide v7, p0, Lloops/TestCoroutineEmitDelayPollingLoop;->J$1:J
    iget-wide v9, p0, Lloops/TestCoroutineEmitDelayPollingLoop;->J$0:J
    invoke-static {p1}, Lloops/TestCoroutineEmitDelayPollingLoop;->throwOnFailure(Ljava/lang/Object;)V
    goto :after_emit

    :initial
    invoke-static {p1}, Lloops/TestCoroutineEmitDelayPollingLoop;->throwOnFailure(Ljava/lang/Object;)V
    invoke-static {}, Lloops/TestCoroutineEmitDelayPollingLoop;->currentPosition()J
    move-result-wide v7

    :loop_condition
    add-long v9, v7, v3
    iget-wide v7, p0, Lloops/TestCoroutineEmitDelayPollingLoop;->duration:J
    cmp-long v11, v9, v7
    if-lez v11, :poll
    invoke-static {}, Lloops/TestCoroutineEmitDelayPollingLoop;->repeatMode()I
    move-result v11
    if-eq v11, v6, :poll
    const-string p1, "done"
    return-object p1

    :poll
    invoke-static {}, Lloops/TestCoroutineEmitDelayPollingLoop;->currentPosition()J
    move-result-wide v7
    const-wide/16 v9, 0xa
    add-long/2addr v7, v9
    invoke-static {v7, v8}, Lloops/TestCoroutineEmitDelayPollingLoop;->boxLong(J)Ljava/lang/Object;
    move-result-object p1
    iput-object v0, p0, Lloops/TestCoroutineEmitDelayPollingLoop;->L$0:Ljava/lang/Object;
    iput-wide v9, p0, Lloops/TestCoroutineEmitDelayPollingLoop;->J$0:J
    iput-wide v7, p0, Lloops/TestCoroutineEmitDelayPollingLoop;->J$1:J
    iput v5, p0, Lloops/TestCoroutineEmitDelayPollingLoop;->label:I
    invoke-static {p1, p2}, Lloops/TestCoroutineEmitDelayPollingLoop;->emit(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-eq p1, v1, :suspended

    :after_emit
    iput-object v0, p0, Lloops/TestCoroutineEmitDelayPollingLoop;->L$0:Ljava/lang/Object;
    iput-wide v9, p0, Lloops/TestCoroutineEmitDelayPollingLoop;->J$0:J
    iput-wide v7, p0, Lloops/TestCoroutineEmitDelayPollingLoop;->J$1:J
    iput v6, p0, Lloops/TestCoroutineEmitDelayPollingLoop;->label:I
    invoke-static {p2}, Lloops/TestCoroutineEmitDelayPollingLoop;->delay(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-ne p1, v1, :loop_condition

    :suspended
    return-object v1
.end method
