.class public Lloops/TestCoroutineDelaySavedContextDispatch;
.super Ljava/lang/Object;

.field private L$0:Ljava/lang/Object;
.field private L$1:Ljava/lang/Object;
.field private label:I

.method private static delay(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 1
    const/4 v0, 0x0
    return-object v0
.end method

.method private static emit(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 1
    const/4 v0, 0x0
    return-object v0
.end method

.method private static getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    .locals 1
    const/4 v0, 0x0
    return-object v0
.end method

.method private static throwOnFailure(Ljava/lang/Object;)V
    .locals 0
    return-void
.end method

.method public run(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 6

    iget-object v0, p0, Lloops/TestCoroutineDelaySavedContextDispatch;->L$0:Ljava/lang/Object;
    check-cast v0, Ljava/lang/String;
    invoke-static {}, Lloops/TestCoroutineDelaySavedContextDispatch;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v1
    iget v2, p0, Lloops/TestCoroutineDelaySavedContextDispatch;->label:I
    const/4 v3, 0x2
    const/4 v4, 0x1
    if-eqz v2, :shared_initial_delay_resume
    if-eq v2, v4, :resume_emit
    if-ne v2, v3, :bad_state

    iget-object v2, p0, Lloops/TestCoroutineDelaySavedContextDispatch;->L$1:Ljava/lang/Object;
    check-cast v2, Ljava/lang/String;
    goto :shared_initial_delay_resume

    :bad_state
    new-instance v2, Ljava/lang/IllegalStateException;
    invoke-direct {v2}, Ljava/lang/IllegalStateException;-><init>()V
    throw v2

    :resume_emit
    iget-object v2, p0, Lloops/TestCoroutineDelaySavedContextDispatch;->L$1:Ljava/lang/Object;
    check-cast v2, Ljava/lang/String;
    invoke-static {p1}, Lloops/TestCoroutineDelaySavedContextDispatch;->throwOnFailure(Ljava/lang/Object;)V
    goto :after_emit

    :shared_initial_delay_resume
    invoke-static {p1}, Lloops/TestCoroutineDelaySavedContextDispatch;->throwOnFailure(Ljava/lang/Object;)V

    :body
    invoke-static {}, Ljava/lang/System;->currentTimeMillis()J
    move-result-wide v2
    invoke-static {v2, v3}, Ljava/lang/String;->valueOf(J)Ljava/lang/String;
    move-result-object v2
    invoke-virtual {v2}, Ljava/lang/String;->length()I
    move-result v5
    if-eqz v5, :empty_value
    const-string v2, "non-empty"
    goto :value_ready

    :empty_value
    const-string v2, "empty"

    :value_ready
    iput-object v0, p0, Lloops/TestCoroutineDelaySavedContextDispatch;->L$0:Ljava/lang/Object;
    iput-object v2, p0, Lloops/TestCoroutineDelaySavedContextDispatch;->L$1:Ljava/lang/Object;
    iput v4, p0, Lloops/TestCoroutineDelaySavedContextDispatch;->label:I
    invoke-static {v2, p2}, Lloops/TestCoroutineDelaySavedContextDispatch;->emit(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-eq p1, v1, :suspended

    :after_emit
    iput-object v0, p0, Lloops/TestCoroutineDelaySavedContextDispatch;->L$0:Ljava/lang/Object;
    iput-object v2, p0, Lloops/TestCoroutineDelaySavedContextDispatch;->L$1:Ljava/lang/Object;
    iput v3, p0, Lloops/TestCoroutineDelaySavedContextDispatch;->label:I
    invoke-static {p2}, Lloops/TestCoroutineDelaySavedContextDispatch;->delay(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-ne p1, v1, :body

    :suspended
    return-object v1
.end method
