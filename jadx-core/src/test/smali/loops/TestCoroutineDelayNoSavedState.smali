.class public Lloops/TestCoroutineDelayNoSavedState;
.super Ljava/lang/Object;

.field private label:I
.field private runningDelay:I
.field private scope:Ljava/lang/Object;

.method private static consume()V
    .locals 0
    return-void
.end method

.method private static consumeScope(Lkotlinx/coroutines/CoroutineScope;)V
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
    .locals 5

    iget-object v4, p0, Lloops/TestCoroutineDelayNoSavedState;->scope:Ljava/lang/Object;
    check-cast v4, Lkotlinx/coroutines/CoroutineScope;
    invoke-static {}, Lloops/TestCoroutineDelayNoSavedState;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v0
    iget v1, p0, Lloops/TestCoroutineDelayNoSavedState;->label:I
    const/4 v2, 0x2
    if-eqz v1, :initial
    if-ne v1, v2, :bad_state

    invoke-static {p1}, Lloops/TestCoroutineDelayNoSavedState;->throwOnFailure(Ljava/lang/Object;)V
    goto :after_delay

    :bad_state
    new-instance v3, Ljava/lang/IllegalStateException;
    invoke-direct {v3}, Ljava/lang/IllegalStateException;-><init>()V
    throw v3

    :initial
    invoke-static {p1}, Lloops/TestCoroutineDelayNoSavedState;->throwOnFailure(Ljava/lang/Object;)V

    :body
    invoke-static {v4}, Lloops/TestCoroutineDelayNoSavedState;->consumeScope(Lkotlinx/coroutines/CoroutineScope;)V
    invoke-static {}, Lloops/TestCoroutineDelayNoSavedState;->consume()V
    iput v2, p0, Lloops/TestCoroutineDelayNoSavedState;->label:I
    invoke-static {}, Lloops/TestCoroutineDelayNoSavedState;->delay()Ljava/lang/Object;
    move-result-object v3
    if-eq v3, v0, :suspended

    :after_delay
    iget v4, p0, Lloops/TestCoroutineDelayNoSavedState;->runningDelay:I
    if-lez v4, :done
    goto :body

    :suspended
    return-object v0

    :done
    sget-object v4, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object v4
.end method
