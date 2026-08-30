.class public Lloops/TestCoroutineDelayInitialResumeJoin;
.super Ljava/lang/Object;

.field private J$0:J
.field private L$0:Ljava/lang/Object;
.field private label:I

.method private static delay(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;
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

.method private static getContext(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 1
    const/4 v0, 0x0
    return-object v0
.end method

.method private static isActive(Ljava/lang/Object;)Z
    .locals 1
    const/4 v0, 0x1
    return v0
.end method

.method private static throwOnFailure(Ljava/lang/Object;)V
    .locals 0
    return-void
.end method

.method public run(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 10

    iget-object v0, p0, Lloops/TestCoroutineDelayInitialResumeJoin;->L$0:Ljava/lang/Object;
    check-cast v0, Ljava/lang/String;
    invoke-static {}, Lloops/TestCoroutineDelayInitialResumeJoin;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v1
    iget v2, p0, Lloops/TestCoroutineDelayInitialResumeJoin;->label:I
    const/4 v3, 0x2
    const/4 v4, 0x1
    if-eqz v2, :initial
    if-eq v2, v4, :resume_emit
    if-ne v2, v3, :bad_state

    iget-wide v5, p0, Lloops/TestCoroutineDelayInitialResumeJoin;->J$0:J
    invoke-static {p1}, Lloops/TestCoroutineDelayInitialResumeJoin;->throwOnFailure(Ljava/lang/Object;)V
    goto :active

    :bad_state
    new-instance v2, Ljava/lang/IllegalStateException;
    invoke-direct {v2}, Ljava/lang/IllegalStateException;-><init>()V
    throw v2

    :resume_emit
    iget-wide v5, p0, Lloops/TestCoroutineDelayInitialResumeJoin;->J$0:J
    invoke-static {p1}, Lloops/TestCoroutineDelayInitialResumeJoin;->throwOnFailure(Ljava/lang/Object;)V
    goto :after_emit

    :initial
    invoke-static {p1}, Lloops/TestCoroutineDelayInitialResumeJoin;->throwOnFailure(Ljava/lang/Object;)V
    invoke-static {}, Ljava/lang/System;->currentTimeMillis()J
    move-result-wide v5

    :active
    invoke-static {p2}, Lloops/TestCoroutineDelayInitialResumeJoin;->getContext(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v7
    invoke-static {v7}, Lloops/TestCoroutineDelayInitialResumeJoin;->isActive(Ljava/lang/Object;)Z
    move-result v7
    if-eqz v7, :done
    iput-object v0, p0, Lloops/TestCoroutineDelayInitialResumeJoin;->L$0:Ljava/lang/Object;
    iput-wide v5, p0, Lloops/TestCoroutineDelayInitialResumeJoin;->J$0:J
    iput v4, p0, Lloops/TestCoroutineDelayInitialResumeJoin;->label:I
    invoke-static {v0, p2}, Lloops/TestCoroutineDelayInitialResumeJoin;->emit(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-eq p1, v1, :suspended

    :after_emit
    const-wide/16 v8, 0x1
    add-long/2addr v5, v8
    iput-object v0, p0, Lloops/TestCoroutineDelayInitialResumeJoin;->L$0:Ljava/lang/Object;
    iput-wide v5, p0, Lloops/TestCoroutineDelayInitialResumeJoin;->J$0:J
    iput v3, p0, Lloops/TestCoroutineDelayInitialResumeJoin;->label:I
    invoke-static {v5, v6, p2}, Lloops/TestCoroutineDelayInitialResumeJoin;->delay(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-ne p1, v1, :active

    :suspended
    return-object v1

    :done
    const/4 v0, 0x0
    return-object v0
.end method
