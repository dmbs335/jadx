.class public Lloops/TestCoroutineDelayPhiStateBackEdge;
.super Ljava/lang/Object;

.field private label:I

.method private static action(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 1
    const/4 v0, 0x0
    return-object v0
.end method

.method private static delay(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
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

    invoke-static {}, Lloops/TestCoroutineDelayPhiStateBackEdge;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v0
    iget v1, p0, Lloops/TestCoroutineDelayPhiStateBackEdge;->label:I
    const/4 v2, 0x2
    const/4 v3, 0x1

    if-eqz v1, :initial
    if-eq v1, v3, :resume_action
    if-ne v1, v2, :bad_state

    invoke-static {p1}, Lloops/TestCoroutineDelayPhiStateBackEdge;->throwOnFailure(Ljava/lang/Object;)V
    goto :loop_latch

    :bad_state
    new-instance v1, Ljava/lang/IllegalStateException;
    invoke-direct {v1}, Ljava/lang/IllegalStateException;-><init>()V
    throw v1

    :resume_action
    invoke-static {p1}, Lloops/TestCoroutineDelayPhiStateBackEdge;->throwOnFailure(Ljava/lang/Object;)V
    move v4, v2
    goto :delay_setup

    :initial
    invoke-static {p1}, Lloops/TestCoroutineDelayPhiStateBackEdge;->throwOnFailure(Ljava/lang/Object;)V

    :body
    iput v3, p0, Lloops/TestCoroutineDelayPhiStateBackEdge;->label:I
    invoke-static {p0}, Lloops/TestCoroutineDelayPhiStateBackEdge;->action(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-eq p1, v0, :suspended
    const/4 v4, 0x2

    :delay_setup
    iput v4, p0, Lloops/TestCoroutineDelayPhiStateBackEdge;->label:I
    invoke-static {p0}, Lloops/TestCoroutineDelayPhiStateBackEdge;->delay(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-eq p1, v0, :suspended

    :loop_latch
    move-object v5, p1
    move-object p1, v5
    goto :body

    :suspended
    return-object v0
.end method
