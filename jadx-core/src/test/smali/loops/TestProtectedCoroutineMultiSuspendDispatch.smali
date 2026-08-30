.class public Lloops/TestProtectedCoroutineMultiSuspendDispatch;
.super Ljava/lang/Object;

.field private a:Ljava/lang/Object;
.field private b:Ljava/lang/Object;
.field private f:I
.field private i:I

.method private static commit(Ljava/lang/Object;)V
    .locals 0
    return-void
.end method

.method private static consume(Ljava/lang/Object;)V
    .locals 0
    return-void
.end method

.method private static delay(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 1
    const/4 v0, 0x0
    return-object v0
.end method

.method private static getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    .locals 1
    const/4 v0, 0x0
    return-object v0
.end method

.method private static isActive(Ljava/lang/Object;)Z
    .locals 1
    const/4 v0, 0x1
    return v0
.end method

.method private static send(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 1
    const/4 v0, 0x0
    return-object v0
.end method

.method private static step(Ljava/lang/Object;)V
    .locals 0
    return-void
.end method

.method private static suspendClock(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 1
    const/4 v0, 0x0
    return-object v0
.end method

.method private static throwOnFailure(Ljava/lang/Object;)V
    .locals 0
    return-void
.end method

.method public invokeSuspend(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 11

    invoke-static {}, Lloops/TestProtectedCoroutineMultiSuspendDispatch;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v0
    iget v1, p0, Lloops/TestProtectedCoroutineMultiSuspendDispatch;->f:I
    const/4 v2, 0x1
    const/4 v3, 0x2
    const/4 v7, 0x3
    if-eqz v1, :initial
    if-eq v1, v2, :resume_clock
    if-eq v1, v3, :resume_send
    if-ne v1, v7, :bad_state

    iget-object v5, p0, Lloops/TestProtectedCoroutineMultiSuspendDispatch;->a:Ljava/lang/Object;
    invoke-static {p1}, Lloops/TestProtectedCoroutineMultiSuspendDispatch;->throwOnFailure(Ljava/lang/Object;)V
    goto :outer_header

    :resume_send
    iget-object v4, p0, Lloops/TestProtectedCoroutineMultiSuspendDispatch;->a:Ljava/lang/Object;
    iget-object v5, p0, Lloops/TestProtectedCoroutineMultiSuspendDispatch;->b:Ljava/lang/Object;
    iget v6, p0, Lloops/TestProtectedCoroutineMultiSuspendDispatch;->i:I
    :try_start_resume_send
    invoke-static {p1}, Lloops/TestProtectedCoroutineMultiSuspendDispatch;->throwOnFailure(Ljava/lang/Object;)V
    :try_end_resume_send
    .catch Ljava/lang/RuntimeException; {:try_start_resume_send .. :try_end_resume_send} :recover
    .catchall {:try_start_resume_send .. :try_end_resume_send} :handler
    goto :after_send

    :resume_clock
    iget-object v4, p0, Lloops/TestProtectedCoroutineMultiSuspendDispatch;->a:Ljava/lang/Object;
    iget-object v5, p0, Lloops/TestProtectedCoroutineMultiSuspendDispatch;->b:Ljava/lang/Object;
    iget v6, p0, Lloops/TestProtectedCoroutineMultiSuspendDispatch;->i:I
    :try_start_resume_clock
    invoke-static {p1}, Lloops/TestProtectedCoroutineMultiSuspendDispatch;->throwOnFailure(Ljava/lang/Object;)V
    :try_end_resume_clock
    .catch Ljava/lang/RuntimeException; {:try_start_resume_clock .. :try_end_resume_clock} :recover
    .catchall {:try_start_resume_clock .. :try_end_resume_clock} :handler
    move-object v8, p1
    goto :after_clock

    :bad_state
    new-instance v1, Ljava/lang/IllegalStateException;
    invoke-direct {v1}, Ljava/lang/IllegalStateException;-><init>()V
    throw v1

    :initial
    invoke-static {p1}, Lloops/TestProtectedCoroutineMultiSuspendDispatch;->throwOnFailure(Ljava/lang/Object;)V
    move-object v5, p2

    :outer_header
    invoke-static {v5}, Lloops/TestProtectedCoroutineMultiSuspendDispatch;->isActive(Ljava/lang/Object;)Z
    move-result v1
    if-eqz v1, :done
    move-object v4, p0
    const/4 v6, 0x1

    :inner_header
    if-lez v6, :after_inner
    move-object v9, v4
    move-object v10, v5
    :try_start_clock
    iput-object v4, p0, Lloops/TestProtectedCoroutineMultiSuspendDispatch;->a:Ljava/lang/Object;
    iput-object v5, p0, Lloops/TestProtectedCoroutineMultiSuspendDispatch;->b:Ljava/lang/Object;
    iput v6, p0, Lloops/TestProtectedCoroutineMultiSuspendDispatch;->i:I
    iput v2, p0, Lloops/TestProtectedCoroutineMultiSuspendDispatch;->f:I
    invoke-static {p0}, Lloops/TestProtectedCoroutineMultiSuspendDispatch;->suspendClock(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    :try_end_clock
    .catch Ljava/lang/RuntimeException; {:try_start_clock .. :try_end_clock} :recover
    .catchall {:try_start_clock .. :try_end_clock} :handler
    if-eq p1, v0, :suspended

    move-object v4, v9
    move-object v5, v10
    move-object v8, p1

    :after_clock
    :try_start_send
    invoke-static {v8}, Lloops/TestProtectedCoroutineMultiSuspendDispatch;->consume(Ljava/lang/Object;)V
    iput-object v4, p0, Lloops/TestProtectedCoroutineMultiSuspendDispatch;->a:Ljava/lang/Object;
    iput-object v5, p0, Lloops/TestProtectedCoroutineMultiSuspendDispatch;->b:Ljava/lang/Object;
    iput v6, p0, Lloops/TestProtectedCoroutineMultiSuspendDispatch;->i:I
    iput v3, p0, Lloops/TestProtectedCoroutineMultiSuspendDispatch;->f:I
    invoke-static {p0}, Lloops/TestProtectedCoroutineMultiSuspendDispatch;->send(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    :try_end_send
    .catch Ljava/lang/RuntimeException; {:try_start_send .. :try_end_send} :recover
    .catchall {:try_start_send .. :try_end_send} :handler
    if-eq p1, v0, :suspended

    :after_send
    invoke-static {v4}, Lloops/TestProtectedCoroutineMultiSuspendDispatch;->commit(Ljava/lang/Object;)V
    invoke-static {v5}, Lloops/TestProtectedCoroutineMultiSuspendDispatch;->step(Ljava/lang/Object;)V
    add-int/lit8 v6, v6, -0x1
    goto :inner_header

    :recover
    move-exception v8
    invoke-static {v8}, Lloops/TestProtectedCoroutineMultiSuspendDispatch;->consume(Ljava/lang/Object;)V
    goto :after_send

    :handler
    move-exception v8
    return-object v8

    :after_inner
    move-object v9, v5
    iput-object v5, p0, Lloops/TestProtectedCoroutineMultiSuspendDispatch;->a:Ljava/lang/Object;
    const/4 v1, 0x0
    iput-object v1, p0, Lloops/TestProtectedCoroutineMultiSuspendDispatch;->b:Ljava/lang/Object;
    iput v7, p0, Lloops/TestProtectedCoroutineMultiSuspendDispatch;->f:I
    const-wide/16 v4, 0x1
    invoke-static {v4, v5, p0}, Lloops/TestProtectedCoroutineMultiSuspendDispatch;->delay(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-eq p1, v0, :suspended

    move-object v5, v9
    const/4 v2, 0x1
    const/4 v3, 0x2
    goto :outer_header

    :suspended
    return-object v0

    :done
    return-object p1
.end method
