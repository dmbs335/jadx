.class public final Lloops/TestCoroutineIndexedIteratorSendLoop;
.super Ljava/lang/Object;

.field private $source_with_index:Ljava/lang/Object;
.field private I$0:I
.field private L$0:Ljava/lang/Object;
.field private L$1:Ljava/lang/Object;
.field private label:I

.method private static getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    .locals 1
    const-string v0, "suspended"
    return-object v0
.end method

.method private static hasNext(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 1
    sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
    return-object v0
.end method

.method private static next(Ljava/lang/Object;)Ljava/lang/Object;
    .locals 1
    const-string v0, "element"
    return-object v0
.end method

.method private static send(Ljava/lang/Object;Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 0
    return-object p1
.end method

.method private static throwOnFailure(Ljava/lang/Object;)V
    .locals 0
    return-void
.end method

.method public invokeSuspend(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 9

    invoke-static {}, Lloops/TestCoroutineIndexedIteratorSendLoop;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v0
    iget v1, p0, Lloops/TestCoroutineIndexedIteratorSendLoop;->label:I
    const/4 v2, 0x2
    const/4 v3, 0x1

    if-eqz v1, :initial
    if-eq v1, v3, :resume_has_next
    if-ne v1, v2, :bad_state

    :resume_send
    iget v7, p0, Lloops/TestCoroutineIndexedIteratorSendLoop;->I$0:I
    iget-object v6, p0, Lloops/TestCoroutineIndexedIteratorSendLoop;->L$1:Ljava/lang/Object;
    iget-object v5, p0, Lloops/TestCoroutineIndexedIteratorSendLoop;->L$0:Ljava/lang/Object;
    invoke-static {p1}, Lloops/TestCoroutineIndexedIteratorSendLoop;->throwOnFailure(Ljava/lang/Object;)V
    goto :loop

    :resume_has_next
    iget v7, p0, Lloops/TestCoroutineIndexedIteratorSendLoop;->I$0:I
    iget-object v6, p0, Lloops/TestCoroutineIndexedIteratorSendLoop;->L$1:Ljava/lang/Object;
    iget-object v5, p0, Lloops/TestCoroutineIndexedIteratorSendLoop;->L$0:Ljava/lang/Object;
    invoke-static {p1}, Lloops/TestCoroutineIndexedIteratorSendLoop;->throwOnFailure(Ljava/lang/Object;)V
    goto :has_next_result

    :bad_state
    new-instance p1, Ljava/lang/IllegalStateException;
    invoke-direct {p1}, Ljava/lang/IllegalStateException;-><init>()V
    throw p1

    :initial
    invoke-static {p1}, Lloops/TestCoroutineIndexedIteratorSendLoop;->throwOnFailure(Ljava/lang/Object;)V
    const-string v5, "producer"
    const-string v6, "iterator"
    const/4 v7, 0x0

    :loop
    iput-object v5, p0, Lloops/TestCoroutineIndexedIteratorSendLoop;->L$0:Ljava/lang/Object;
    iput-object v6, p0, Lloops/TestCoroutineIndexedIteratorSendLoop;->L$1:Ljava/lang/Object;
    iput v7, p0, Lloops/TestCoroutineIndexedIteratorSendLoop;->I$0:I
    iput v3, p0, Lloops/TestCoroutineIndexedIteratorSendLoop;->label:I
    invoke-static {v6, p2}, Lloops/TestCoroutineIndexedIteratorSendLoop;->hasNext(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-eq p1, v0, :suspended

    :has_next_result
    check-cast p1, Ljava/lang/Boolean;
    invoke-virtual {p1}, Ljava/lang/Boolean;->booleanValue()Z
    move-result p1
    if-eqz p1, :done
    invoke-static {v6}, Lloops/TestCoroutineIndexedIteratorSendLoop;->next(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v8
    add-int/lit8 v7, v7, 0x1
    iput-object v5, p0, Lloops/TestCoroutineIndexedIteratorSendLoop;->L$0:Ljava/lang/Object;
    iput-object v6, p0, Lloops/TestCoroutineIndexedIteratorSendLoop;->L$1:Ljava/lang/Object;
    iput v7, p0, Lloops/TestCoroutineIndexedIteratorSendLoop;->I$0:I
    iput v2, p0, Lloops/TestCoroutineIndexedIteratorSendLoop;->label:I
    invoke-static {v5, v8, p2}, Lloops/TestCoroutineIndexedIteratorSendLoop;->send(Ljava/lang/Object;Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-ne p1, v0, :loop

    :suspended
    return-object v0

    :done
    const-string p1, "done"
    return-object p1
.end method
