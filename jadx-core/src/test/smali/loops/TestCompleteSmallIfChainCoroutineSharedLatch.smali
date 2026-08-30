.class public Lloops/TestCompleteSmallIfChainCoroutineSharedLatch;
.super Ljava/lang/Object;

.field private label:I
.field private marker:I

.method private static awaitValue()Ljava/lang/Object;
    .locals 1
    sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
    return-object v0
.end method

.method private static sendValue()Ljava/lang/Object;
    .locals 1
    sget-object v0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object v0
.end method

.method private static throwOnFailure(Ljava/lang/Object;)V
    .locals 0
    return-void
.end method

.method public run(Ljava/lang/Object;Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 3

    iget v0, p0, Lloops/TestCompleteSmallIfChainCoroutineSharedLatch;->label:I
    if-eqz v0, :initial
    const/4 v1, 0x1
    if-eq v0, v1, :resume_await
    const/4 v1, 0x2
    if-eq v0, v1, :resume_send
    new-instance v0, Ljava/lang/IllegalStateException;
    invoke-direct {v0}, Ljava/lang/IllegalStateException;-><init>()V
    throw v0

    :resume_send
    invoke-static {p1}, Lloops/TestCompleteSmallIfChainCoroutineSharedLatch;->throwOnFailure(Ljava/lang/Object;)V
    goto :send_latch

    :resume_await
    invoke-static {p1}, Lloops/TestCompleteSmallIfChainCoroutineSharedLatch;->throwOnFailure(Ljava/lang/Object;)V
    goto :await_done

    :initial
    invoke-static {p1}, Lloops/TestCompleteSmallIfChainCoroutineSharedLatch;->throwOnFailure(Ljava/lang/Object;)V

    :loop_header
    const/4 v0, 0x1
    iput v0, p0, Lloops/TestCompleteSmallIfChainCoroutineSharedLatch;->label:I
    invoke-static {}, Lloops/TestCompleteSmallIfChainCoroutineSharedLatch;->awaitValue()Ljava/lang/Object;
    move-result-object p1
    if-eq p1, p2, :suspended

    :await_done
    const/4 v0, 0x2
    iput v0, p0, Lloops/TestCompleteSmallIfChainCoroutineSharedLatch;->label:I
    invoke-static {}, Lloops/TestCompleteSmallIfChainCoroutineSharedLatch;->sendValue()Ljava/lang/Object;
    move-result-object p1
    if-eq p1, p2, :suspended

    :direct_send_bridge
    move-object v2, p1
    goto :send_latch

    :send_latch
    const/4 v1, 0x1
    iput v1, p0, Lloops/TestCompleteSmallIfChainCoroutineSharedLatch;->marker:I
    goto :loop_header

    :suspended
    return-object p2
.end method
