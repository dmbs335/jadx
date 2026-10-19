.class public Lloops/TestCoroutineResultFieldDispatch;
.super Lkotlin/coroutines/jvm/internal/SuspendLambda;

.field private label:I
.field private result:Ljava/lang/Object;
.field private rounds:I

.method private static suspendCall(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 1
    const/4 v0, 0x0
    return-object v0
.end method

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .locals 5

    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v0
    iget-object v1, p0, Lloops/TestCoroutineResultFieldDispatch;->result:Ljava/lang/Object;
    iget v2, p0, Lloops/TestCoroutineResultFieldDispatch;->label:I
    packed-switch v2, :state_switch

    new-instance p1, Ljava/lang/IllegalStateException;
    invoke-direct {p1}, Ljava/lang/IllegalStateException;-><init>()V
    throw p1

    :state_zero
    invoke-static {v1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :call_one

    :state_one
    invoke-static {v1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :after_one

    :state_two
    invoke-static {v1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :after_two

    :call_one
    const/4 v2, 0x1
    iput v2, p0, Lloops/TestCoroutineResultFieldDispatch;->label:I
    invoke-static {v2, p0}, Lloops/TestCoroutineResultFieldDispatch;->suspendCall(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v3
    if-eq v3, v0, :suspended
    move-object v1, v3
    goto :after_one

    :after_one
    const/4 v2, 0x2
    iput v2, p0, Lloops/TestCoroutineResultFieldDispatch;->label:I
    invoke-static {v2, p0}, Lloops/TestCoroutineResultFieldDispatch;->suspendCall(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v3
    if-eq v3, v0, :suspended
    move-object v1, v3
    goto :after_two

    :after_two
    iget v4, p0, Lloops/TestCoroutineResultFieldDispatch;->rounds:I
    add-int/lit8 v4, v4, -0x1
    iput v4, p0, Lloops/TestCoroutineResultFieldDispatch;->rounds:I
    if-gez v4, :call_one
    return-object v1

    :suspended
    return-object v0

    :state_switch
    .packed-switch 0x0
        :state_zero
        :state_one
        :state_two
    .end packed-switch
.end method
