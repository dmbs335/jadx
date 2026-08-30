.class public final Lloops/TestCoroutineIteratorPredicateActionLoop;
.super Ljava/lang/Object;

.field private L$0:Ljava/lang/Object;
.field private L$1:Ljava/lang/Object;
.field private L$2:Ljava/lang/Object;
.field private L$3:Ljava/lang/Object;
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

.method private static shouldMigrate(Ljava/lang/Object;Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 1
    sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
    return-object v0
.end method

.method private static migrate(Ljava/lang/Object;Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 0
    return-object p0
.end method

.method private static record(Ljava/lang/Object;)V
    .locals 0
    return-void
.end method

.method private static throwOnFailure(Ljava/lang/Object;)V
    .locals 0
    return-void
.end method

.method public invokeSuspend(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 10

    invoke-static {}, Lloops/TestCoroutineIteratorPredicateActionLoop;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v0
    iget v1, p0, Lloops/TestCoroutineIteratorPredicateActionLoop;->label:I
    const/4 v2, 0x2
    const/4 v3, 0x1

    if-nez v1, :non_initial

    :initial
    invoke-static {p1}, Lloops/TestCoroutineIteratorPredicateActionLoop;->throwOnFailure(Ljava/lang/Object;)V
    const-string v4, "data"
    invoke-static {}, Lloops/TestCoroutineIteratorPredicateActionLoop;->iterator()Ljava/util/Iterator;
    move-result-object v5
    goto :loop

    :non_initial
    if-ne v1, v3, :check_action_state

    :resume_predicate
    iget-object v7, p0, Lloops/TestCoroutineIteratorPredicateActionLoop;->L$3:Ljava/lang/Object;
    iget-object v6, p0, Lloops/TestCoroutineIteratorPredicateActionLoop;->L$2:Ljava/lang/Object;
    iget-object v8, p0, Lloops/TestCoroutineIteratorPredicateActionLoop;->L$1:Ljava/lang/Object;
    invoke-static {p1}, Lloops/TestCoroutineIteratorPredicateActionLoop;->throwOnFailure(Ljava/lang/Object;)V
    goto :predicate_result

    :check_action_state
    if-ne v1, v2, :bad_state

    :resume_action
    iget-object v5, p0, Lloops/TestCoroutineIteratorPredicateActionLoop;->L$1:Ljava/lang/Object;
    invoke-static {p1}, Lloops/TestCoroutineIteratorPredicateActionLoop;->throwOnFailure(Ljava/lang/Object;)V
    move-object v4, p1
    goto :loop

    :bad_state
    new-instance p1, Ljava/lang/IllegalStateException;
    invoke-direct {p1}, Ljava/lang/IllegalStateException;-><init>()V
    throw p1

    :loop
    invoke-interface {v5}, Ljava/util/Iterator;->hasNext()Z
    move-result v9
    if-eqz v9, :done
    invoke-interface {v5}, Ljava/util/Iterator;->next()Ljava/lang/Object;
    move-result-object v6
    iput-object v5, p0, Lloops/TestCoroutineIteratorPredicateActionLoop;->L$1:Ljava/lang/Object;
    iput-object v6, p0, Lloops/TestCoroutineIteratorPredicateActionLoop;->L$2:Ljava/lang/Object;
    iput-object v4, p0, Lloops/TestCoroutineIteratorPredicateActionLoop;->L$3:Ljava/lang/Object;
    iput v3, p0, Lloops/TestCoroutineIteratorPredicateActionLoop;->label:I
    invoke-static {v4, v6, p2}, Lloops/TestCoroutineIteratorPredicateActionLoop;->shouldMigrate(Ljava/lang/Object;Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-eq p1, v0, :suspended
    move-object v8, v5
    move-object v7, v4

    :predicate_result
    check-cast p1, Ljava/lang/Boolean;
    invoke-virtual {p1}, Ljava/lang/Boolean;->booleanValue()Z
    move-result v9
    if-eqz v9, :predicate_false
    invoke-static {v6}, Lloops/TestCoroutineIteratorPredicateActionLoop;->record(Ljava/lang/Object;)V
    iput-object v8, p0, Lloops/TestCoroutineIteratorPredicateActionLoop;->L$1:Ljava/lang/Object;
    const/4 v1, 0x0
    iput-object v1, p0, Lloops/TestCoroutineIteratorPredicateActionLoop;->L$2:Ljava/lang/Object;
    iput-object v1, p0, Lloops/TestCoroutineIteratorPredicateActionLoop;->L$3:Ljava/lang/Object;
    iput v2, p0, Lloops/TestCoroutineIteratorPredicateActionLoop;->label:I
    invoke-static {v7, v6, p2}, Lloops/TestCoroutineIteratorPredicateActionLoop;->migrate(Ljava/lang/Object;Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-eq p1, v0, :suspended

    :shared_latch
    move-object v4, p1
    move-object v5, v8
    goto :loop

    :predicate_false
    move-object p1, v7
    goto :shared_latch

    :done
    return-object v4

    :suspended
    return-object v0
.end method
