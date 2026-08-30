.class public Lloops/TestDiscardedSequentialSuspendCompletion;
.super Lkotlin/coroutines/jvm/internal/SuspendLambda;

.field private label:I
.field private L$0:Ljava/lang/Object;
.field private final first:Lkotlin/jvm/functions/Function2;
.field private final second:Lkotlin/jvm/functions/Function1;
.field private final source:Ljava/lang/Object;

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 8

    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v4
    iget v5, p0, Lloops/TestDiscardedSequentialSuspendCompletion;->label:I
    packed-switch v5, :state_switch
    goto :bad_state

    :initial
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    iget-object v2, p0, Lloops/TestDiscardedSequentialSuspendCompletion;->first:Lkotlin/jvm/functions/Function2;
    iget-object v3, p0, Lloops/TestDiscardedSequentialSuspendCompletion;->source:Ljava/lang/Object;
    iput-object v3, p0, Lloops/TestDiscardedSequentialSuspendCompletion;->L$0:Ljava/lang/Object;
    const/4 v5, 0x1
    iput v5, p0, Lloops/TestDiscardedSequentialSuspendCompletion;->label:I
    invoke-interface {v2, v3, p0}, Lkotlin/jvm/functions/Function2;->invoke(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v0
    if-eq v0, v4, :suspended
    goto :second_suspend

    :resume_first
    iget-object v1, p0, Lloops/TestDiscardedSequentialSuspendCompletion;->L$0:Ljava/lang/Object;
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :second_suspend

    :second_suspend
    iget-object v2, p0, Lloops/TestDiscardedSequentialSuspendCompletion;->second:Lkotlin/jvm/functions/Function1;
    const/4 v1, 0x0
    iput-object v1, p0, Lloops/TestDiscardedSequentialSuspendCompletion;->L$0:Ljava/lang/Object;
    const/4 v5, 0x2
    iput v5, p0, Lloops/TestDiscardedSequentialSuspendCompletion;->label:I
    invoke-interface {v2, p0}, Lkotlin/jvm/functions/Function1;->invoke(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v0
    if-eq v0, v4, :suspended
    goto :done

    :resume_second
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V

    :done
    sget-object v0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object v0

    :suspended
    return-object v4

    :bad_state
    new-instance v0, Ljava/lang/IllegalStateException;
    invoke-direct {v0}, Ljava/lang/IllegalStateException;-><init>()V
    throw v0

    :state_switch
    .packed-switch 0x0
        :initial
        :resume_first
        :resume_second
    .end packed-switch
.end method
