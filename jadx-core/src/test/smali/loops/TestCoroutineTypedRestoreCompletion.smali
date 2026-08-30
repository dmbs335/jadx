.class final Lloops/TestCoroutineTypedRestoreCompletion;
.super Lkotlin/coroutines/jvm/internal/RestrictedSuspendLambda;

.field I$0:I
.field L$0:Ljava/lang/Object;
.field L$1:Ljava/lang/Object;
.field L$2:Ljava/lang/Object;
.field label:I

.method private static action(Ljava/lang/StringBuilder;)V
    .registers 1
    return-void
.end method

.method private static suspendAction(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 1
    sget-object p0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object p0
.end method

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 10

    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v0
    iget v1, p0, Lloops/TestCoroutineTypedRestoreCompletion;->label:I
    const/4 v2, 0x0
    const/4 v3, 0x1
    if-eqz v1, :initial
    if-ne v1, v3, :bad_state

    iget v1, p0, Lloops/TestCoroutineTypedRestoreCompletion;->I$0:I
    iget-object v4, p0, Lloops/TestCoroutineTypedRestoreCompletion;->L$2:Ljava/lang/Object;
    check-cast v4, Ljava/lang/StringBuilder;
    iget-object v5, p0, Lloops/TestCoroutineTypedRestoreCompletion;->L$1:Ljava/lang/Object;
    check-cast v5, Ljava/util/List;
    iget-object v6, p0, Lloops/TestCoroutineTypedRestoreCompletion;->L$0:Ljava/lang/Object;
    check-cast v6, Ljava/lang/Object;
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :effect_tail

    :bad_state
    new-instance p1, Ljava/lang/IllegalStateException;
    const-string v0, "bad coroutine state"
    invoke-direct {p1, v0}, Ljava/lang/IllegalStateException;-><init>(Ljava/lang/String;)V
    throw p1

    :initial
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    new-instance v4, Ljava/lang/StringBuilder;
    invoke-direct {v4}, Ljava/lang/StringBuilder;-><init>()V
    new-instance v5, Ljava/util/ArrayList;
    invoke-direct {v5}, Ljava/util/ArrayList;-><init>()V
    move-object v6, p0
    move v1, v2

    :loop
    iput-object v6, p0, Lloops/TestCoroutineTypedRestoreCompletion;->L$0:Ljava/lang/Object;
    iput-object v5, p0, Lloops/TestCoroutineTypedRestoreCompletion;->L$1:Ljava/lang/Object;
    iput-object v4, p0, Lloops/TestCoroutineTypedRestoreCompletion;->L$2:Ljava/lang/Object;
    iput v1, p0, Lloops/TestCoroutineTypedRestoreCompletion;->I$0:I
    iput v3, p0, Lloops/TestCoroutineTypedRestoreCompletion;->label:I
    invoke-static {p0}, Lloops/TestCoroutineTypedRestoreCompletion;->suspendAction(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-ne p1, v0, :effect_tail
    return-object v0

    :effect_tail
    invoke-static {v4}, Lloops/TestCoroutineTypedRestoreCompletion;->action(Ljava/lang/StringBuilder;)V
    add-int/lit8 v1, v1, 0x1
    const/4 v7, 0x3
    if-lt v1, v7, :loop

    sget-object p1, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object p1
.end method
