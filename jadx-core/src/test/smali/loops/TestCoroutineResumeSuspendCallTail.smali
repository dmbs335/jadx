.class public Lloops/TestCoroutineResumeSuspendCallTail;
.super Lkotlin/coroutines/jvm/internal/SuspendLambda;

.field private callback:Lkotlin/jvm/functions/Function1;
.field private label:I

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 7

    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v0
    iget v1, p0, Lloops/TestCoroutineResumeSuspendCallTail;->label:I
    const/4 v2, 0x2
    const/4 v3, 0x1
    if-eqz v1, :initial
    if-eq v1, v3, :resume_delay
    if-ne v1, v2, :bad_state

    iget-object v4, p0, Lloops/TestCoroutineResumeSuspendCallTail;->callback:Lkotlin/jvm/functions/Function1;
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :loop

    :resume_delay
    iget-object v4, p0, Lloops/TestCoroutineResumeSuspendCallTail;->callback:Lkotlin/jvm/functions/Function1;
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :callback_call

    :bad_state
    new-instance p1, Ljava/lang/IllegalStateException;
    invoke-direct {p1}, Ljava/lang/IllegalStateException;-><init>()V
    throw p1

    :initial
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V

    :loop
    iput v3, p0, Lloops/TestCoroutineResumeSuspendCallTail;->label:I
    const-wide/16 v4, 0x1
    invoke-static {v4, v5, p0}, Lkotlinx/coroutines/DelayKt;->delay(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-eq p1, v0, :suspended

    :callback_call
    iget-object v4, p0, Lloops/TestCoroutineResumeSuspendCallTail;->callback:Lkotlin/jvm/functions/Function1;
    iput v2, p0, Lloops/TestCoroutineResumeSuspendCallTail;->label:I
    invoke-interface {v4, p0}, Lkotlin/jvm/functions/Function1;->invoke(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object p1
    if-ne p1, v0, :loop

    :suspended
    return-object v0
.end method
