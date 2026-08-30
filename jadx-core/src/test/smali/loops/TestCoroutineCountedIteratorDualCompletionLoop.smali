.class public final Lloops/TestCoroutineCountedIteratorDualCompletionLoop;
.super Ljava/lang/Object;

.field private $this_drop:Ljava/lang/Object;
.field private I$0:I
.field private L$0:Ljava/lang/Object;
.field private L$1:Ljava/lang/Object;
.field private label:I

.method private static getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    .locals 1
    const-string v0, "suspended"
    return-object v0
.end method

.method private static hasNext(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 1
    sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
    return-object v0
.end method

.method private static next()Ljava/lang/Object;
    .locals 1
    const-string v0, "element"
    return-object v0
.end method

.method private static send(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 0
    return-object p0
.end method

.method private static throwOnFailure(Ljava/lang/Object;)V
    .locals 0
    return-void
.end method

.method public invokeSuspend(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 8

    invoke-static {}, Lloops/TestCoroutineCountedIteratorDualCompletionLoop;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v0
    iget v1, p0, Lloops/TestCoroutineCountedIteratorDualCompletionLoop;->label:I
    const/4 v2, 0x3
    const/4 v3, 0x2
    const/4 v4, 0x1

    if-eqz v1, :initial
    if-eq v1, v4, :resume_skip_has_next
    if-eq v1, v3, :resume_forward_has_next
    if-ne v1, v2, :bad_state

    :resume_send
    iget-object v5, p0, Lloops/TestCoroutineCountedIteratorDualCompletionLoop;->L$1:Ljava/lang/Object;
    iget-object v6, p0, Lloops/TestCoroutineCountedIteratorDualCompletionLoop;->L$0:Ljava/lang/Object;
    invoke-static {p1}, Lloops/TestCoroutineCountedIteratorDualCompletionLoop;->throwOnFailure(Ljava/lang/Object;)V
    goto :forward_loop

    :resume_forward_has_next
    iget-object v5, p0, Lloops/TestCoroutineCountedIteratorDualCompletionLoop;->L$1:Ljava/lang/Object;
    iget-object v6, p0, Lloops/TestCoroutineCountedIteratorDualCompletionLoop;->L$0:Ljava/lang/Object;
    invoke-static {p1}, Lloops/TestCoroutineCountedIteratorDualCompletionLoop;->throwOnFailure(Ljava/lang/Object;)V
    goto :forward_result

    :resume_skip_has_next
    iget v7, p0, Lloops/TestCoroutineCountedIteratorDualCompletionLoop;->I$0:I
    iget-object v5, p0, Lloops/TestCoroutineCountedIteratorDualCompletionLoop;->L$1:Ljava/lang/Object;
    iget-object v6, p0, Lloops/TestCoroutineCountedIteratorDualCompletionLoop;->L$0:Ljava/lang/Object;
    invoke-static {p1}, Lloops/TestCoroutineCountedIteratorDualCompletionLoop;->throwOnFailure(Ljava/lang/Object;)V
    goto :skip_result

    :bad_state
    new-instance p1, Ljava/lang/IllegalStateException;
    invoke-direct {p1}, Ljava/lang/IllegalStateException;-><init>()V
    throw p1

    :initial
    invoke-static {p1}, Lloops/TestCoroutineCountedIteratorDualCompletionLoop;->throwOnFailure(Ljava/lang/Object;)V
    const-string v5, "iterator"
    const-string v6, "scope"
    const/4 v7, 0x2

    :skip_loop
    iput-object v6, p0, Lloops/TestCoroutineCountedIteratorDualCompletionLoop;->L$0:Ljava/lang/Object;
    iput-object v5, p0, Lloops/TestCoroutineCountedIteratorDualCompletionLoop;->L$1:Ljava/lang/Object;
    iput v7, p0, Lloops/TestCoroutineCountedIteratorDualCompletionLoop;->I$0:I
    iput v4, p0, Lloops/TestCoroutineCountedIteratorDualCompletionLoop;->label:I
    invoke-static {p2}, Lloops/TestCoroutineCountedIteratorDualCompletionLoop;->hasNext(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-ne p1, v0, :skip_result
    goto :suspended

    :skip_result
    check-cast p1, Ljava/lang/Boolean;
    invoke-virtual {p1}, Ljava/lang/Boolean;->booleanValue()Z
    move-result p1
    if-eqz p1, :done
    invoke-static {}, Lloops/TestCoroutineCountedIteratorDualCompletionLoop;->next()Ljava/lang/Object;
    add-int/lit8 v7, v7, -0x1
    if-nez v7, :skip_loop

    :forward_loop
    iput-object v6, p0, Lloops/TestCoroutineCountedIteratorDualCompletionLoop;->L$0:Ljava/lang/Object;
    iput-object v5, p0, Lloops/TestCoroutineCountedIteratorDualCompletionLoop;->L$1:Ljava/lang/Object;
    iput v3, p0, Lloops/TestCoroutineCountedIteratorDualCompletionLoop;->label:I
    invoke-static {p2}, Lloops/TestCoroutineCountedIteratorDualCompletionLoop;->hasNext(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-ne p1, v0, :forward_result
    goto :suspended

    :forward_result
    check-cast p1, Ljava/lang/Boolean;
    invoke-virtual {p1}, Ljava/lang/Boolean;->booleanValue()Z
    move-result p1
    if-eqz p1, :done
    invoke-static {}, Lloops/TestCoroutineCountedIteratorDualCompletionLoop;->next()Ljava/lang/Object;
    move-result-object p1
    iput-object v6, p0, Lloops/TestCoroutineCountedIteratorDualCompletionLoop;->L$0:Ljava/lang/Object;
    iput-object v5, p0, Lloops/TestCoroutineCountedIteratorDualCompletionLoop;->L$1:Ljava/lang/Object;
    iput v2, p0, Lloops/TestCoroutineCountedIteratorDualCompletionLoop;->label:I
    invoke-static {p1, p2}, Lloops/TestCoroutineCountedIteratorDualCompletionLoop;->send(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-ne p1, v0, :forward_loop

    :suspended
    return-object v0

    :done
    const-string p1, "done"
    return-object p1
.end method
