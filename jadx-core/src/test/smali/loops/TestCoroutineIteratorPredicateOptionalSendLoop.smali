.class public final Lloops/TestCoroutineIteratorPredicateOptionalSendLoop;
.super Ljava/lang/Object;

.field private I$0:I
.field private L$0:Ljava/lang/Object;
.field private L$1:Ljava/lang/Object;
.field private L$2:Ljava/lang/Object;
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

.method private static invoke(ILjava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 1
    sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
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
    .locals 10

    invoke-static {}, Lloops/TestCoroutineIteratorPredicateOptionalSendLoop;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v0
    iget v1, p0, Lloops/TestCoroutineIteratorPredicateOptionalSendLoop;->label:I
    const/4 v2, 0x3
    const/4 v3, 0x2
    const/4 v4, 0x1

    if-eqz v1, :initial
    if-eq v1, v4, :resume_has_next
    if-eq v1, v3, :resume_predicate
    if-ne v1, v2, :bad_state

    :resume_send
    iget v7, p0, Lloops/TestCoroutineIteratorPredicateOptionalSendLoop;->I$0:I
    iget-object v6, p0, Lloops/TestCoroutineIteratorPredicateOptionalSendLoop;->L$1:Ljava/lang/Object;
    iget-object v5, p0, Lloops/TestCoroutineIteratorPredicateOptionalSendLoop;->L$0:Ljava/lang/Object;
    invoke-static {p1}, Lloops/TestCoroutineIteratorPredicateOptionalSendLoop;->throwOnFailure(Ljava/lang/Object;)V
    goto :loop

    :resume_predicate
    iget v7, p0, Lloops/TestCoroutineIteratorPredicateOptionalSendLoop;->I$0:I
    iget-object v8, p0, Lloops/TestCoroutineIteratorPredicateOptionalSendLoop;->L$2:Ljava/lang/Object;
    iget-object v6, p0, Lloops/TestCoroutineIteratorPredicateOptionalSendLoop;->L$1:Ljava/lang/Object;
    iget-object v5, p0, Lloops/TestCoroutineIteratorPredicateOptionalSendLoop;->L$0:Ljava/lang/Object;
    invoke-static {p1}, Lloops/TestCoroutineIteratorPredicateOptionalSendLoop;->throwOnFailure(Ljava/lang/Object;)V
    goto :predicate_result

    :resume_has_next
    iget v7, p0, Lloops/TestCoroutineIteratorPredicateOptionalSendLoop;->I$0:I
    iget-object v6, p0, Lloops/TestCoroutineIteratorPredicateOptionalSendLoop;->L$1:Ljava/lang/Object;
    iget-object v5, p0, Lloops/TestCoroutineIteratorPredicateOptionalSendLoop;->L$0:Ljava/lang/Object;
    invoke-static {p1}, Lloops/TestCoroutineIteratorPredicateOptionalSendLoop;->throwOnFailure(Ljava/lang/Object;)V
    goto :has_next_result

    :bad_state
    new-instance p1, Ljava/lang/IllegalStateException;
    invoke-direct {p1}, Ljava/lang/IllegalStateException;-><init>()V
    throw p1

    :initial
    invoke-static {p1}, Lloops/TestCoroutineIteratorPredicateOptionalSendLoop;->throwOnFailure(Ljava/lang/Object;)V
    const-string v5, "producer"
    const-string v6, "iterator"
    const/4 v7, 0x0

    :loop
    iput-object v5, p0, Lloops/TestCoroutineIteratorPredicateOptionalSendLoop;->L$0:Ljava/lang/Object;
    iput-object v6, p0, Lloops/TestCoroutineIteratorPredicateOptionalSendLoop;->L$1:Ljava/lang/Object;
    const/4 v1, 0x0
    iput-object v1, p0, Lloops/TestCoroutineIteratorPredicateOptionalSendLoop;->L$2:Ljava/lang/Object;
    iput v7, p0, Lloops/TestCoroutineIteratorPredicateOptionalSendLoop;->I$0:I
    iput v4, p0, Lloops/TestCoroutineIteratorPredicateOptionalSendLoop;->label:I
    invoke-static {v6, p2}, Lloops/TestCoroutineIteratorPredicateOptionalSendLoop;->hasNext(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-eq p1, v0, :suspended

    :has_next_result
    check-cast p1, Ljava/lang/Boolean;
    invoke-virtual {p1}, Ljava/lang/Boolean;->booleanValue()Z
    move-result p1
    if-eqz p1, :done
    invoke-static {v6}, Lloops/TestCoroutineIteratorPredicateOptionalSendLoop;->next(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v8
    add-int/lit8 v9, v7, 0x1
    iput-object v5, p0, Lloops/TestCoroutineIteratorPredicateOptionalSendLoop;->L$0:Ljava/lang/Object;
    iput-object v6, p0, Lloops/TestCoroutineIteratorPredicateOptionalSendLoop;->L$1:Ljava/lang/Object;
    iput-object v8, p0, Lloops/TestCoroutineIteratorPredicateOptionalSendLoop;->L$2:Ljava/lang/Object;
    iput v9, p0, Lloops/TestCoroutineIteratorPredicateOptionalSendLoop;->I$0:I
    iput v3, p0, Lloops/TestCoroutineIteratorPredicateOptionalSendLoop;->label:I
    invoke-static {v7, v8, p2}, Lloops/TestCoroutineIteratorPredicateOptionalSendLoop;->invoke(ILjava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    move v7, v9
    if-eq p1, v0, :suspended

    :predicate_result
    check-cast p1, Ljava/lang/Boolean;
    invoke-virtual {p1}, Ljava/lang/Boolean;->booleanValue()Z
    move-result p1
    if-eqz p1, :shared_send_latch
    iput-object v5, p0, Lloops/TestCoroutineIteratorPredicateOptionalSendLoop;->L$0:Ljava/lang/Object;
    iput-object v6, p0, Lloops/TestCoroutineIteratorPredicateOptionalSendLoop;->L$1:Ljava/lang/Object;
    const/4 v1, 0x0
    iput-object v1, p0, Lloops/TestCoroutineIteratorPredicateOptionalSendLoop;->L$2:Ljava/lang/Object;
    iput v7, p0, Lloops/TestCoroutineIteratorPredicateOptionalSendLoop;->I$0:I
    iput v2, p0, Lloops/TestCoroutineIteratorPredicateOptionalSendLoop;->label:I
    invoke-static {v5, v8, p2}, Lloops/TestCoroutineIteratorPredicateOptionalSendLoop;->send(Ljava/lang/Object;Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-eq p1, v0, :suspended

    :shared_send_latch
    move-object p1, v5
    goto :loop

    :done
    const-string p1, "done"
    return-object p1

    :suspended
    return-object v0
.end method
