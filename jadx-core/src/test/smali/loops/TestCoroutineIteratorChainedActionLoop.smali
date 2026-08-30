.class public final Lloops/TestCoroutineIteratorChainedActionLoop;
.super Ljava/lang/Object;

.field private I$0:I
.field private I$1:I
.field private L$0:Ljava/lang/Object;
.field private L$1:Ljava/lang/Object;
.field private L$2:Ljava/lang/Object;
.field private label:I

.method private static getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    .locals 1
    const-string v0, "suspended"
    return-object v0
.end method

.method private static iterator()Ljava/util/Iterator;
    .locals 1
    const/4 v0, 0x0
    return-object v0
.end method

.method private static firstAction(Ljava/lang/Object;Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 0
    return-object p1
.end method

.method private static prepare(Ljava/lang/Object;)Ljava/lang/Object;
    .locals 0
    return-object p0
.end method

.method private static secondAction(Ljava/lang/Object;Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 0
    return-object p0
.end method

.method private static throwOnFailure(Ljava/lang/Object;)V
    .locals 0
    return-void
.end method

.method public invokeSuspend(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 10

    iget-object v4, p0, Lloops/TestCoroutineIteratorChainedActionLoop;->L$0:Ljava/lang/Object;
    check-cast v4, Ljava/lang/String;
    invoke-static {}, Lloops/TestCoroutineIteratorChainedActionLoop;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v0
    iget v1, p0, Lloops/TestCoroutineIteratorChainedActionLoop;->label:I
    const/4 v2, 0x2
    const/4 v3, 0x1

    if-nez v1, :non_initial
    invoke-static {p1}, Lloops/TestCoroutineIteratorChainedActionLoop;->throwOnFailure(Ljava/lang/Object;)V
    invoke-static {}, Lloops/TestCoroutineIteratorChainedActionLoop;->iterator()Ljava/util/Iterator;
    move-result-object v5
    const/4 v6, 0x0
    const/4 v9, 0x0
    goto :loop

    :non_initial
    if-ne v1, v3, :check_second_state
    iget v6, p0, Lloops/TestCoroutineIteratorChainedActionLoop;->I$0:I
    iget v9, p0, Lloops/TestCoroutineIteratorChainedActionLoop;->I$1:I
    iget-object v7, p0, Lloops/TestCoroutineIteratorChainedActionLoop;->L$2:Ljava/lang/Object;
    iget-object v8, p0, Lloops/TestCoroutineIteratorChainedActionLoop;->L$1:Ljava/lang/Object;
    invoke-static {p1}, Lloops/TestCoroutineIteratorChainedActionLoop;->throwOnFailure(Ljava/lang/Object;)V
    goto :first_result

    :check_second_state
    if-ne v1, v2, :bad_state
    iget v6, p0, Lloops/TestCoroutineIteratorChainedActionLoop;->I$0:I
    iget-object v5, p0, Lloops/TestCoroutineIteratorChainedActionLoop;->L$1:Ljava/lang/Object;
    invoke-static {p1}, Lloops/TestCoroutineIteratorChainedActionLoop;->throwOnFailure(Ljava/lang/Object;)V
    goto :reset_latch

    :bad_state
    new-instance p1, Ljava/lang/IllegalStateException;
    invoke-direct {p1}, Ljava/lang/IllegalStateException;-><init>()V
    throw p1

    :loop
    invoke-interface {v5}, Ljava/util/Iterator;->hasNext()Z
    move-result p1
    if-eqz p1, :done
    invoke-interface {v5}, Ljava/util/Iterator;->next()Ljava/lang/Object;
    move-result-object v7
    iput-object v5, p0, Lloops/TestCoroutineIteratorChainedActionLoop;->L$1:Ljava/lang/Object;
    iput-object v7, p0, Lloops/TestCoroutineIteratorChainedActionLoop;->L$2:Ljava/lang/Object;
    iput v6, p0, Lloops/TestCoroutineIteratorChainedActionLoop;->I$0:I
    iput v9, p0, Lloops/TestCoroutineIteratorChainedActionLoop;->I$1:I
    iput v3, p0, Lloops/TestCoroutineIteratorChainedActionLoop;->label:I
    invoke-static {v4, v7, p2}, Lloops/TestCoroutineIteratorChainedActionLoop;->firstAction(Ljava/lang/Object;Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-eq p1, v0, :suspended
    move-object v8, v5

    :first_result
    invoke-static {p1}, Lloops/TestCoroutineIteratorChainedActionLoop;->prepare(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v1
    iput-object v8, p0, Lloops/TestCoroutineIteratorChainedActionLoop;->L$1:Ljava/lang/Object;
    iput v6, p0, Lloops/TestCoroutineIteratorChainedActionLoop;->I$0:I
    iput v9, p0, Lloops/TestCoroutineIteratorChainedActionLoop;->I$1:I
    iput v2, p0, Lloops/TestCoroutineIteratorChainedActionLoop;->label:I
    invoke-static {v1, v7, p2}, Lloops/TestCoroutineIteratorChainedActionLoop;->secondAction(Ljava/lang/Object;Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-eq p1, v0, :suspended
    move-object v5, v8

    :reset_latch
    const/4 v9, 0x0
    const/4 v3, 0x1
    goto :loop

    :done
    return-object v4

    :suspended
    return-object v0
.end method
