.class public final Lloops/TestCoroutineWhileSelectBooleanCompletion;
.super Lkotlin/coroutines/jvm/internal/ContinuationImpl;

.field private L$0:Ljava/lang/Object;
.field private label:I
.field private result:Ljava/lang/Object;

.method public final P(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 8

    iput-object p1, p0, Lloops/TestCoroutineWhileSelectBooleanCompletion;->result:Ljava/lang/Object;
    invoke-static {}, Lkotlin/coroutines/intrinsics/a;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v0
    iget v1, p0, Lloops/TestCoroutineWhileSelectBooleanCompletion;->label:I
    const/4 v2, 0x1
    if-eqz v1, :initial
    if-ne v1, v2, :invalid

    iget-object v3, p0, Lloops/TestCoroutineWhileSelectBooleanCompletion;->L$0:Ljava/lang/Object;
    check-cast v3, Lkotlin/jvm/functions/Function1;
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    move-object v4, p0
    goto :result_decision

    :invalid
    new-instance p1, Ljava/lang/IllegalStateException;
    invoke-direct {p1}, Ljava/lang/IllegalStateException;-><init>()V
    throw p1

    :initial
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    iget-object v3, p0, Lloops/TestCoroutineWhileSelectBooleanCompletion;->L$0:Ljava/lang/Object;
    check-cast v3, Lkotlin/jvm/functions/Function1;
    move-object v4, p0

    :loop
    new-instance v5, Lkotlinx/coroutines/selects/SelectImplementation;
    const/4 v1, 0x0
    invoke-direct {v5, v1}, Lkotlinx/coroutines/selects/SelectImplementation;-><init>(Lkotlin/coroutines/CoroutineContext;)V
    invoke-interface {v3, v5}, Lkotlin/jvm/functions/Function1;->invoke(Ljava/lang/Object;)Ljava/lang/Object;
    iput-object v3, v4, Lloops/TestCoroutineWhileSelectBooleanCompletion;->L$0:Ljava/lang/Object;
    iput v2, v4, Lloops/TestCoroutineWhileSelectBooleanCompletion;->label:I
    invoke-virtual {v5, v4}, Lkotlinx/coroutines/selects/SelectImplementation;->g(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v5
    if-eq v5, v0, :suspended

    move-object p1, v5
    move-object v4, p0

    :result_decision
    check-cast p1, Ljava/lang/Boolean;
    invoke-virtual {p1}, Ljava/lang/Boolean;->booleanValue()Z
    move-result p1
    if-nez p1, :loop
    sget-object p1, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object p1

    :suspended
    return-object v0
.end method
