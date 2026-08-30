.class public final Lloops/TestCoroutineThreeStateSharedRestoreIfChain;
.super Lkotlin/coroutines/jvm/internal/SuspendLambda;

.field public label:I
.field public result:Ljava/lang/Object;
.field public L$0:Ljava/lang/Object;

.method private static native scope()Ljava/util/List;
.end method

.method private static native emit(Ljava/util/List;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
.end method

.method private static native receive(Ljava/util/List;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
.end method

.method private static native changed(Ljava/lang/Object;)Z
.end method

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 10

    move-object v1, p0
    move-object v4, p1
    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v0
    iget v2, v1, Lloops/TestCoroutineThreeStateSharedRestoreIfChain;->label:I
    if-eqz v2, :initial
    const/4 v3, 0x1
    if-eq v2, v3, :shared_restore
    const/4 v3, 0x2
    if-eq v2, v3, :resume_event
    const/4 v3, 0x3
    if-eq v2, v3, :shared_restore
    goto :bad_state

    # Labels 1 and 3 deliberately share a typed spill restore before throwOnFailure.
    :shared_restore
    iget-object v5, v1, Lloops/TestCoroutineThreeStateSharedRestoreIfChain;->L$0:Ljava/lang/Object;
    check-cast v5, Ljava/util/List;
    goto :shared_resume

    :try_start
    :shared_resume
    invoke-static {v4}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :loop

    :resume_event
    iget-object v5, v1, Lloops/TestCoroutineThreeStateSharedRestoreIfChain;->L$0:Ljava/lang/Object;
    check-cast v5, Ljava/util/List;
    invoke-static {v4}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :event_body

    :initial
    invoke-static {v4}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    invoke-static {}, Lloops/TestCoroutineThreeStateSharedRestoreIfChain;->scope()Ljava/util/List;
    move-result-object v5
    iput-object v5, v1, Lloops/TestCoroutineThreeStateSharedRestoreIfChain;->L$0:Ljava/lang/Object;
    const/4 v2, 0x1
    iput v2, v1, Lloops/TestCoroutineThreeStateSharedRestoreIfChain;->label:I
    invoke-static {v5, v1}, Lloops/TestCoroutineThreeStateSharedRestoreIfChain;->emit(Ljava/util/List;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v4
    if-eq v4, v0, :suspended

    :loop
    iput-object v5, v1, Lloops/TestCoroutineThreeStateSharedRestoreIfChain;->L$0:Ljava/lang/Object;
    const/4 v2, 0x2
    iput v2, v1, Lloops/TestCoroutineThreeStateSharedRestoreIfChain;->label:I
    invoke-static {v5, v1}, Lloops/TestCoroutineThreeStateSharedRestoreIfChain;->receive(Ljava/util/List;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v4
    if-eq v4, v0, :suspended

    :event_body
    invoke-static {v4}, Lloops/TestCoroutineThreeStateSharedRestoreIfChain;->changed(Ljava/lang/Object;)Z
    move-result v2
    if-eqz v2, :loop

    iput-object v5, v1, Lloops/TestCoroutineThreeStateSharedRestoreIfChain;->L$0:Ljava/lang/Object;
    const/4 v2, 0x3
    iput v2, v1, Lloops/TestCoroutineThreeStateSharedRestoreIfChain;->label:I
    invoke-static {v5, v1}, Lloops/TestCoroutineThreeStateSharedRestoreIfChain;->emit(Ljava/util/List;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v4
    if-eq v4, v0, :suspended

    # Immediate state-3 completion uses a move-only bridge before loop re-entry.
    move-object v7, v5
    move-object v5, v7
    goto :loop
    :try_end
    .catchall {:try_start .. :try_end} :catch_all

    :suspended
    return-object v0

    :catch_all
    move-exception v8
    throw v8

    :bad_state
    new-instance v8, Ljava/lang/IllegalStateException;
    invoke-direct {v8}, Ljava/lang/IllegalStateException;-><init>()V
    throw v8
.end method
