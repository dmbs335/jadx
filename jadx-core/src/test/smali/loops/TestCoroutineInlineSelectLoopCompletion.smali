.class public final Lloops/TestCoroutineInlineSelectLoopCompletion;
.super Ljava/lang/Object;

.field private L$0:Ljava/lang/Object;
.field private L$1:Ljava/lang/Object;
.field private L$2:Ljava/lang/Object;
.field private label:I

.method private static emit(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 0
    return-object p0
.end method

.method private static getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    .locals 1
    const-string v0, "suspended"
    return-object v0
.end method

.method private static newSelect()Lkotlinx/coroutines/selects/SelectImplementation;
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

    invoke-static {}, Lloops/TestCoroutineInlineSelectLoopCompletion;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v0
    iget v1, p0, Lloops/TestCoroutineInlineSelectLoopCompletion;->label:I
    const/4 v2, 0x2
    const/4 v3, 0x1

    if-eqz v1, :initial
    if-eq v1, v3, :resume_emit
    if-ne v1, v2, :bad_state

    :resume_select
    iget-object v8, p0, Lloops/TestCoroutineInlineSelectLoopCompletion;->L$0:Ljava/lang/Object;
    iget-object v9, p0, Lloops/TestCoroutineInlineSelectLoopCompletion;->L$1:Ljava/lang/Object;
    iget-object v10, p0, Lloops/TestCoroutineInlineSelectLoopCompletion;->L$2:Ljava/lang/Object;
    invoke-static {p1}, Lloops/TestCoroutineInlineSelectLoopCompletion;->throwOnFailure(Ljava/lang/Object;)V
    goto :shared_latch

    :bad_state
    new-instance p1, Ljava/lang/IllegalStateException;
    invoke-direct {p1}, Ljava/lang/IllegalStateException;-><init>()V
    throw p1

    :resume_emit
    iget-object v4, p0, Lloops/TestCoroutineInlineSelectLoopCompletion;->L$0:Ljava/lang/Object;
    iget-object v5, p0, Lloops/TestCoroutineInlineSelectLoopCompletion;->L$1:Ljava/lang/Object;
    iget-object v6, p0, Lloops/TestCoroutineInlineSelectLoopCompletion;->L$2:Ljava/lang/Object;
    invoke-static {p1}, Lloops/TestCoroutineInlineSelectLoopCompletion;->throwOnFailure(Ljava/lang/Object;)V
    goto :after_emit

    :initial
    invoke-static {p1}, Lloops/TestCoroutineInlineSelectLoopCompletion;->throwOnFailure(Ljava/lang/Object;)V
    const-string v4, "pending"
    const-string v5, "values"
    const-string v6, "downstream"
    goto :loop

    :loop
    iput-object v4, p0, Lloops/TestCoroutineInlineSelectLoopCompletion;->L$0:Ljava/lang/Object;
    iput-object v5, p0, Lloops/TestCoroutineInlineSelectLoopCompletion;->L$1:Ljava/lang/Object;
    iput-object v6, p0, Lloops/TestCoroutineInlineSelectLoopCompletion;->L$2:Ljava/lang/Object;
    iput v3, p0, Lloops/TestCoroutineInlineSelectLoopCompletion;->label:I
    invoke-static {v4, p2}, Lloops/TestCoroutineInlineSelectLoopCompletion;->emit(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-eq p1, v0, :suspended

    :after_emit
    invoke-static {}, Lloops/TestCoroutineInlineSelectLoopCompletion;->newSelect()Lkotlinx/coroutines/selects/SelectImplementation;
    move-result-object v7
    move-object v8, v4
    move-object v9, v5
    move-object v10, v6
    iput-object v8, p0, Lloops/TestCoroutineInlineSelectLoopCompletion;->L$0:Ljava/lang/Object;
    iput-object v9, p0, Lloops/TestCoroutineInlineSelectLoopCompletion;->L$1:Ljava/lang/Object;
    iput-object v10, p0, Lloops/TestCoroutineInlineSelectLoopCompletion;->L$2:Ljava/lang/Object;
    iput v2, p0, Lloops/TestCoroutineInlineSelectLoopCompletion;->label:I
    invoke-virtual {v7, p2}, Lkotlinx/coroutines/selects/SelectImplementation;->doSelect(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-eq p1, v0, :suspended

    :shared_latch
    move-object v4, v8
    move-object v5, v9
    move-object v6, v10
    goto :loop

    :suspended
    return-object v0
.end method
