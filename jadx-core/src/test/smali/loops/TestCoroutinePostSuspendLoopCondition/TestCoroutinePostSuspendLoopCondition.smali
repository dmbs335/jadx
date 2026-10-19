.class public final Lloops/TestCoroutinePostSuspendLoopCondition;
.super Ljava/lang/Object;

.method public static P(Lkotlin/jvm/functions/Function2;Lkotlin/jvm/internal/Ref$ObjectRef;Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 12

    instance-of v0, p3, Lloops/TestCoroutinePostSuspendLoopCondition$State;
    if-eqz v0, :new_state
    move-object v4, p3
    check-cast v4, Lloops/TestCoroutinePostSuspendLoopCondition$State;
    iget v0, v4, Lloops/TestCoroutinePostSuspendLoopCondition$State;->label:I
    const/high16 v1, -0x80000000
    and-int v6, v0, v1
    if-eqz v6, :new_state
    sub-int/2addr v0, v1
    iput v0, v4, Lloops/TestCoroutinePostSuspendLoopCondition$State;->label:I
    goto :dispatch

    :new_state
    new-instance v4, Lloops/TestCoroutinePostSuspendLoopCondition$State;
    invoke-direct {v4, p3}, Lloops/TestCoroutinePostSuspendLoopCondition$State;-><init>(Lkotlin/coroutines/Continuation;)V

    :dispatch
    iget-object v0, v4, Lloops/TestCoroutinePostSuspendLoopCondition$State;->result:Ljava/lang/Object;
    invoke-static {}, Lkotlin/coroutines/intrinsics/a;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v1
    iget v6, v4, Lloops/TestCoroutinePostSuspendLoopCondition$State;->label:I
    const/4 v7, 0x1
    if-eqz v6, :initial
    if-ne v6, v7, :invalid

    iget-object v2, v4, Lloops/TestCoroutinePostSuspendLoopCondition$State;->L$0:Ljava/lang/Object;
    check-cast v2, Lkotlin/jvm/functions/Function2;
    iget-object v3, v4, Lloops/TestCoroutinePostSuspendLoopCondition$State;->L$1:Ljava/lang/Object;
    check-cast v3, Lkotlin/jvm/internal/Ref$ObjectRef;
    invoke-static {v0}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    move-object v5, v0
    move-object v6, v3
    move-object v3, v6
    goto :result_test

    :invalid
    new-instance v0, Ljava/lang/IllegalStateException;
    invoke-direct {v0}, Ljava/lang/IllegalStateException;-><init>()V
    throw v0

    :initial
    invoke-static {v0}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    move-object v2, p0
    move-object v3, p1
    move-object v6, v3
    move-object v3, v6

    :loop_body
    iget-object v6, v3, Lkotlin/jvm/internal/Ref$ObjectRef;->element:Ljava/lang/Object;
    iput-object v2, v4, Lloops/TestCoroutinePostSuspendLoopCondition$State;->L$0:Ljava/lang/Object;
    iput-object v3, v4, Lloops/TestCoroutinePostSuspendLoopCondition$State;->L$1:Ljava/lang/Object;
    iput v7, v4, Lloops/TestCoroutinePostSuspendLoopCondition$State;->label:I
    invoke-interface {v2, v6, v4}, Lkotlin/jvm/functions/Function2;->invoke(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v6
    if-eq v6, v1, :suspended

    move-object v5, v6
    move-object v6, v3
    move-object v3, v6

    :result_test
    iput-object v5, v3, Lkotlin/jvm/internal/Ref$ObjectRef;->element:Ljava/lang/Object;
    iget-object v6, v3, Lkotlin/jvm/internal/Ref$ObjectRef;->element:Ljava/lang/Object;
    invoke-static {v6}, Lloops/TestCoroutinePostSuspendLoopCondition;->shouldContinue(Ljava/lang/Object;)Z
    move-result v6
    if-eqz v6, :done

    move-object v6, v3
    move-object v3, v6
    goto :loop_body

    :done
    iget-object v0, v3, Lkotlin/jvm/internal/Ref$ObjectRef;->element:Ljava/lang/Object;
    return-object v0

    :suspended
    return-object v1
.end method

.method private static shouldContinue(Ljava/lang/Object;)Z
    .registers 2
    if-eqz p0, :empty
    const/4 v0, 0x1
    return v0
    :empty
    const/4 v0, 0x0
    return v0
.end method
