.class public Lloops/TestManyDiscardedSequentialSuspendCompletions;
.super Lkotlin/coroutines/jvm/internal/SuspendLambda;

.field private label:I
.field private final step:Lkotlin/jvm/functions/Function1;

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 8

    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v4
    iget v5, p0, Lloops/TestManyDiscardedSequentialSuspendCompletions;->label:I
    packed-switch v5, :state_switch
    goto :bad_state

    :initial
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :setup_1

    :resume_1
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :setup_2

    :resume_2
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :setup_3

    :resume_3
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :setup_4

    :resume_4
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :setup_5

    :resume_5
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :setup_6

    :resume_6
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :setup_7

    :resume_7
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :setup_8

    :resume_8
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :setup_9

    :resume_9
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :setup_10

    :resume_10
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :done

    :setup_1
    iget-object v2, p0, Lloops/TestManyDiscardedSequentialSuspendCompletions;->step:Lkotlin/jvm/functions/Function1;
    const/4 v5, 0x1
    iput v5, p0, Lloops/TestManyDiscardedSequentialSuspendCompletions;->label:I
    invoke-interface {v2, p0}, Lkotlin/jvm/functions/Function1;->invoke(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v0
    if-eq v0, v4, :suspended
    goto :setup_2

    :setup_2
    iget-object v2, p0, Lloops/TestManyDiscardedSequentialSuspendCompletions;->step:Lkotlin/jvm/functions/Function1;
    const/4 v5, 0x2
    iput v5, p0, Lloops/TestManyDiscardedSequentialSuspendCompletions;->label:I
    invoke-interface {v2, p0}, Lkotlin/jvm/functions/Function1;->invoke(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v0
    if-eq v0, v4, :suspended
    goto :setup_3

    :setup_3
    iget-object v2, p0, Lloops/TestManyDiscardedSequentialSuspendCompletions;->step:Lkotlin/jvm/functions/Function1;
    const/4 v5, 0x3
    iput v5, p0, Lloops/TestManyDiscardedSequentialSuspendCompletions;->label:I
    invoke-interface {v2, p0}, Lkotlin/jvm/functions/Function1;->invoke(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v0
    if-eq v0, v4, :suspended
    goto :setup_4

    :setup_4
    iget-object v2, p0, Lloops/TestManyDiscardedSequentialSuspendCompletions;->step:Lkotlin/jvm/functions/Function1;
    const/4 v5, 0x4
    iput v5, p0, Lloops/TestManyDiscardedSequentialSuspendCompletions;->label:I
    invoke-interface {v2, p0}, Lkotlin/jvm/functions/Function1;->invoke(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v0
    if-eq v0, v4, :suspended
    goto :setup_5

    :setup_5
    iget-object v2, p0, Lloops/TestManyDiscardedSequentialSuspendCompletions;->step:Lkotlin/jvm/functions/Function1;
    const/4 v5, 0x5
    iput v5, p0, Lloops/TestManyDiscardedSequentialSuspendCompletions;->label:I
    invoke-interface {v2, p0}, Lkotlin/jvm/functions/Function1;->invoke(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v0
    if-eq v0, v4, :suspended
    goto :setup_6

    :setup_6
    iget-object v2, p0, Lloops/TestManyDiscardedSequentialSuspendCompletions;->step:Lkotlin/jvm/functions/Function1;
    const/4 v5, 0x6
    iput v5, p0, Lloops/TestManyDiscardedSequentialSuspendCompletions;->label:I
    invoke-interface {v2, p0}, Lkotlin/jvm/functions/Function1;->invoke(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v0
    if-eq v0, v4, :suspended
    goto :setup_7

    :setup_7
    iget-object v2, p0, Lloops/TestManyDiscardedSequentialSuspendCompletions;->step:Lkotlin/jvm/functions/Function1;
    const/4 v5, 0x7
    iput v5, p0, Lloops/TestManyDiscardedSequentialSuspendCompletions;->label:I
    invoke-interface {v2, p0}, Lkotlin/jvm/functions/Function1;->invoke(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v0
    if-eq v0, v4, :suspended
    goto :setup_8

    :setup_8
    iget-object v2, p0, Lloops/TestManyDiscardedSequentialSuspendCompletions;->step:Lkotlin/jvm/functions/Function1;
    const/16 v5, 0x8
    iput v5, p0, Lloops/TestManyDiscardedSequentialSuspendCompletions;->label:I
    invoke-interface {v2, p0}, Lkotlin/jvm/functions/Function1;->invoke(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v0
    if-eq v0, v4, :suspended
    goto :setup_9

    :setup_9
    iget-object v2, p0, Lloops/TestManyDiscardedSequentialSuspendCompletions;->step:Lkotlin/jvm/functions/Function1;
    const/16 v5, 0x9
    iput v5, p0, Lloops/TestManyDiscardedSequentialSuspendCompletions;->label:I
    invoke-interface {v2, p0}, Lkotlin/jvm/functions/Function1;->invoke(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v0
    if-eq v0, v4, :suspended
    goto :setup_10

    :setup_10
    iget-object v2, p0, Lloops/TestManyDiscardedSequentialSuspendCompletions;->step:Lkotlin/jvm/functions/Function1;
    const/16 v5, 0xa
    iput v5, p0, Lloops/TestManyDiscardedSequentialSuspendCompletions;->label:I
    invoke-interface {v2, p0}, Lkotlin/jvm/functions/Function1;->invoke(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v0
    if-eq v0, v4, :suspended
    goto :done

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
        :resume_1
        :resume_2
        :resume_3
        :resume_4
        :resume_5
        :resume_6
        :resume_7
        :resume_8
        :resume_9
        :resume_10
    .end packed-switch
.end method
