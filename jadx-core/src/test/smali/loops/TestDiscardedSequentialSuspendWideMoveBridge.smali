.class public Lloops/TestDiscardedSequentialSuspendWideMoveBridge;
.super Lkotlin/coroutines/jvm/internal/SuspendLambda;

.field private label:I
.field private L$0:Ljava/lang/Object;
.field private L$1:Ljava/lang/Object;
.field private L$2:Ljava/lang/Object;
.field private L$3:Ljava/lang/Object;
.field private L$4:Ljava/lang/Object;
.field private L$5:Ljava/lang/Object;
.field private L$6:Ljava/lang/Object;
.field private L$7:Ljava/lang/Object;
.field private final first:Lkotlin/jvm/functions/Function2;
.field private final second:Lkotlin/jvm/functions/Function2;
.field private final source:Ljava/lang/Object;

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 16

    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v13
    iget v12, p0, Lloops/TestDiscardedSequentialSuspendWideMoveBridge;->label:I
    packed-switch v12, :state_switch
    goto :bad_state

    :initial
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    iget-object v0, p0, Lloops/TestDiscardedSequentialSuspendWideMoveBridge;->first:Lkotlin/jvm/functions/Function2;
    iget-object v1, p0, Lloops/TestDiscardedSequentialSuspendWideMoveBridge;->source:Ljava/lang/Object;
    iput-object v1, p0, Lloops/TestDiscardedSequentialSuspendWideMoveBridge;->L$0:Ljava/lang/Object;
    iput-object v1, p0, Lloops/TestDiscardedSequentialSuspendWideMoveBridge;->L$1:Ljava/lang/Object;
    iput-object v1, p0, Lloops/TestDiscardedSequentialSuspendWideMoveBridge;->L$2:Ljava/lang/Object;
    iput-object v1, p0, Lloops/TestDiscardedSequentialSuspendWideMoveBridge;->L$3:Ljava/lang/Object;
    iput-object v1, p0, Lloops/TestDiscardedSequentialSuspendWideMoveBridge;->L$4:Ljava/lang/Object;
    iput-object v1, p0, Lloops/TestDiscardedSequentialSuspendWideMoveBridge;->L$5:Ljava/lang/Object;
    iput-object v1, p0, Lloops/TestDiscardedSequentialSuspendWideMoveBridge;->L$6:Ljava/lang/Object;
    iput-object v1, p0, Lloops/TestDiscardedSequentialSuspendWideMoveBridge;->L$7:Ljava/lang/Object;
    const/4 v12, 0x1
    iput v12, p0, Lloops/TestDiscardedSequentialSuspendWideMoveBridge;->label:I
    invoke-interface {v0, v1, p0}, Lkotlin/jvm/functions/Function2;->invoke(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v2
    if-eq v2, v13, :suspended

    :direct_bridge
    move-object v3, v1
    move-object v4, v1
    move-object v5, v1
    move-object v6, v1
    move-object v7, v1
    move-object v8, v1
    move-object v9, v1
    move-object v10, v1
    goto :second_suspend

    :resume_first
    iget-object v3, p0, Lloops/TestDiscardedSequentialSuspendWideMoveBridge;->L$0:Ljava/lang/Object;
    iget-object v4, p0, Lloops/TestDiscardedSequentialSuspendWideMoveBridge;->L$1:Ljava/lang/Object;
    iget-object v5, p0, Lloops/TestDiscardedSequentialSuspendWideMoveBridge;->L$2:Ljava/lang/Object;
    iget-object v6, p0, Lloops/TestDiscardedSequentialSuspendWideMoveBridge;->L$3:Ljava/lang/Object;
    iget-object v7, p0, Lloops/TestDiscardedSequentialSuspendWideMoveBridge;->L$4:Ljava/lang/Object;
    iget-object v8, p0, Lloops/TestDiscardedSequentialSuspendWideMoveBridge;->L$5:Ljava/lang/Object;
    iget-object v9, p0, Lloops/TestDiscardedSequentialSuspendWideMoveBridge;->L$6:Ljava/lang/Object;
    iget-object v10, p0, Lloops/TestDiscardedSequentialSuspendWideMoveBridge;->L$7:Ljava/lang/Object;
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V

    :second_suspend
    iget-object v11, p0, Lloops/TestDiscardedSequentialSuspendWideMoveBridge;->second:Lkotlin/jvm/functions/Function2;
    const/4 v12, 0x2
    iput v12, p0, Lloops/TestDiscardedSequentialSuspendWideMoveBridge;->label:I
    invoke-interface {v11, v3, p0}, Lkotlin/jvm/functions/Function2;->invoke(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v2
    if-eq v2, v13, :suspended
    goto :done

    :resume_second
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V

    :done
    sget-object v0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object v0

    :suspended
    return-object v13

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
