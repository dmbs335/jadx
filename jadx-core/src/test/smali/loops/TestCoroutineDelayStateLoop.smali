.class public Lloops/TestCoroutineDelayStateLoop;
.super Ljava/lang/Object;

.field private label:I
.field private state:I

.method private static consume(I)V
    .locals 0
    return-void
.end method

.method private static delay()Ljava/lang/Object;
    .locals 1
    const/4 v0, 0x0
    return-object v0
.end method

.method private static getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    .locals 1
    const/4 v0, 0x0
    return-object v0
.end method

.method private static throwOnFailure(Ljava/lang/Object;)V
    .locals 0
    return-void
.end method

.method public run(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 6

    invoke-static {}, Lloops/TestCoroutineDelayStateLoop;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v0
    iget v1, p0, Lloops/TestCoroutineDelayStateLoop;->label:I
    const/4 v2, 0x1
    if-eqz v1, :initial
    if-ne v1, v2, :bad_state

    iget v3, p0, Lloops/TestCoroutineDelayStateLoop;->state:I
    invoke-static {p1}, Lloops/TestCoroutineDelayStateLoop;->throwOnFailure(Ljava/lang/Object;)V
    goto :toggle

    :bad_state
    new-instance v4, Ljava/lang/IllegalStateException;
    invoke-direct {v4}, Ljava/lang/IllegalStateException;-><init>()V
    throw v4

    :initial
    invoke-static {p1}, Lloops/TestCoroutineDelayStateLoop;->throwOnFailure(Ljava/lang/Object;)V
    goto :set_zero

    :body
    invoke-static {v3}, Lloops/TestCoroutineDelayStateLoop;->consume(I)V
    iput v3, p0, Lloops/TestCoroutineDelayStateLoop;->state:I
    iput v2, p0, Lloops/TestCoroutineDelayStateLoop;->label:I
    invoke-static {}, Lloops/TestCoroutineDelayStateLoop;->delay()Ljava/lang/Object;
    move-result-object v4
    if-eq v4, v0, :suspended

    :toggle
    if-nez v3, :set_zero
    const/4 v3, 0x1
    goto :body

    :set_zero
    const/4 v3, 0x0
    goto :body

    :suspended
    return-object v0
.end method
