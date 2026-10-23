.class public Lloops/TestProtectedGestureResumeExecution;
.super Lkotlin/coroutines/jvm/internal/SuspendLambda;

.field private label:I
.field private L$0:Ljava/lang/Object;
.field private callCount:I
.field private awaitCount:I
.field private limit:I
.field private failCall:I

.method public constructor <init>()V
    .registers 3
    const/4 v0, 0x2
    const/4 v1, 0x0
    invoke-direct {p0, v0, v1}, Lkotlin/coroutines/jvm/internal/SuspendLambda;-><init>(ILkotlin/coroutines/Continuation;)V
    iput-object p0, p0, Lloops/TestProtectedGestureResumeExecution;->L$0:Ljava/lang/Object;
    return-void
.end method

.method private isActive()Z
    .registers 3
    iget v0, p0, Lloops/TestProtectedGestureResumeExecution;->callCount:I
    iget v1, p0, Lloops/TestProtectedGestureResumeExecution;->limit:I
    if-lt v0, v1, :active
    const/4 v0, 0x0
    return v0
    :active
    const/4 v0, 0x1
    return v0
.end method

.method private block()Ljava/lang/Object;
    .registers 4
    iget v0, p0, Lloops/TestProtectedGestureResumeExecution;->callCount:I
    add-int/lit8 v0, v0, 0x1
    iput v0, p0, Lloops/TestProtectedGestureResumeExecution;->callCount:I
    iget v1, p0, Lloops/TestProtectedGestureResumeExecution;->failCall:I
    if-ne v0, v1, :success
    new-instance v2, Ljava/util/concurrent/CancellationException;
    invoke-direct {v2}, Ljava/util/concurrent/CancellationException;-><init>()V
    throw v2
    :success
    sget-object v0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object v0
.end method

.method private await()Ljava/lang/Object;
    .registers 3
    iget v0, p0, Lloops/TestProtectedGestureResumeExecution;->awaitCount:I
    add-int/lit8 v0, v0, 0x1
    iput v0, p0, Lloops/TestProtectedGestureResumeExecution;->awaitCount:I
    sget-object v1, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object v1
.end method

.method public check()V
    .registers 4
    const/4 v0, 0x0
    invoke-static {v0, v0, v0}, Lloops/TestProtectedGestureResumeExecution;->checkState(III)V
    const/4 v0, 0x1
    const/4 v1, 0x0
    invoke-static {v0, v0, v1}, Lloops/TestProtectedGestureResumeExecution;->checkState(III)V
    const/4 v2, 0x2
    invoke-static {v2, v0, v0}, Lloops/TestProtectedGestureResumeExecution;->checkState(III)V
    const/4 v0, 0x3
    invoke-static {v0, v2, v2}, Lloops/TestProtectedGestureResumeExecution;->checkState(III)V
    return-void
.end method

.method private static checkState(III)V
    .registers 8
    new-instance v0, Lloops/TestProtectedGestureResumeExecution;
    invoke-direct {v0}, Lloops/TestProtectedGestureResumeExecution;-><init>()V
    const/4 v1, 0x3
    iput v1, v0, Lloops/TestProtectedGestureResumeExecution;->limit:I
    const/4 v1, 0x2
    iput v1, v0, Lloops/TestProtectedGestureResumeExecution;->failCall:I
    iput p1, v0, Lloops/TestProtectedGestureResumeExecution;->callCount:I
    iput p2, v0, Lloops/TestProtectedGestureResumeExecution;->awaitCount:I
    iput p0, v0, Lloops/TestProtectedGestureResumeExecution;->label:I
    sget-object v1, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    invoke-virtual {v0, v1}, Lloops/TestProtectedGestureResumeExecution;->invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v2
    if-ne v1, v2, :failed
    iget v2, v0, Lloops/TestProtectedGestureResumeExecution;->callCount:I
    const/4 v3, 0x3
    if-ne v2, v3, :failed
    iget v2, v0, Lloops/TestProtectedGestureResumeExecution;->awaitCount:I
    if-ne v2, v3, :failed
    return-void
    :failed
    new-instance v0, Ljava/lang/AssertionError;
    invoke-direct {v0}, Ljava/lang/AssertionError;-><init>()V
    throw v0
.end method

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 10
    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v0
    iget v1, p0, Lloops/TestProtectedGestureResumeExecution;->label:I
    const/4 v2, 0x1
    const/4 v3, 0x0
    packed-switch v1, :states
    new-instance p1, Ljava/lang/IllegalStateException;
    invoke-direct {p1}, Ljava/lang/IllegalStateException;-><init>()V
    throw p1

    :state3
    iget-object v1, p0, Lloops/TestProtectedGestureResumeExecution;->L$0:Ljava/lang/Object;
    check-cast v1, Lloops/TestProtectedGestureResumeExecution;
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    move-object v4, p0
    goto :join

    :state2
    iget-object v1, p0, Lloops/TestProtectedGestureResumeExecution;->L$0:Ljava/lang/Object;
    check-cast v1, Lloops/TestProtectedGestureResumeExecution;
    :try_start_state2
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    :try_end_state2
    .catch Ljava/util/concurrent/CancellationException; {:try_start_state2 .. :try_end_state2} :catch_state
    move-object v4, p0
    goto :continue

    :state1
    iget-object v1, p0, Lloops/TestProtectedGestureResumeExecution;->L$0:Ljava/lang/Object;
    check-cast v1, Lloops/TestProtectedGestureResumeExecution;
    :try_start_state1
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    :try_end_state1
    .catch Ljava/util/concurrent/CancellationException; {:try_start_state1 .. :try_end_state1} :catch_state
    move-object v4, p0
    goto :await_call

    :catch_state
    move-exception v4
    move-object v5, p0
    goto :catch_join

    :state0
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    iget-object v1, p0, Lloops/TestProtectedGestureResumeExecution;->L$0:Ljava/lang/Object;
    check-cast v1, Lloops/TestProtectedGestureResumeExecution;
    move-object v4, p0

    :loop_header
    invoke-direct {v4}, Lloops/TestProtectedGestureResumeExecution;->isActive()Z
    move-result v5
    if-eqz v5, :done

    :try_start_main
    iput-object v1, v4, Lloops/TestProtectedGestureResumeExecution;->L$0:Ljava/lang/Object;
    iput v2, v4, Lloops/TestProtectedGestureResumeExecution;->label:I
    invoke-direct {v4}, Lloops/TestProtectedGestureResumeExecution;->block()Ljava/lang/Object;
    move-result-object v5
    if-ne v5, v0, :await_call
    return-object v0

    :await_call
    iput-object v1, v4, Lloops/TestProtectedGestureResumeExecution;->L$0:Ljava/lang/Object;
    const/4 v6, 0x2
    iput v6, v4, Lloops/TestProtectedGestureResumeExecution;->label:I
    invoke-direct {v4}, Lloops/TestProtectedGestureResumeExecution;->await()Ljava/lang/Object;
    move-result-object v5
    :try_end_main
    .catch Ljava/util/concurrent/CancellationException; {:try_start_main .. :try_end_main} :catch_main
    if-ne v5, v0, :continue
    return-object v0

    :continue
    goto :loop_header

    :catch_main
    move-exception v5
    move-object v7, v5
    move-object v5, v4
    move-object v4, v7

    :catch_join
    invoke-direct {v5}, Lloops/TestProtectedGestureResumeExecution;->isActive()Z
    move-result v6
    if-eqz v6, :rethrow
    move-object v4, v5
    iput-object v1, v5, Lloops/TestProtectedGestureResumeExecution;->L$0:Ljava/lang/Object;
    const/4 v6, 0x3
    iput v6, v5, Lloops/TestProtectedGestureResumeExecution;->label:I
    invoke-direct {v5}, Lloops/TestProtectedGestureResumeExecution;->await()Ljava/lang/Object;
    move-result-object v4
    if-ne v4, v0, :after_catch_await
    return-object v0

    :after_catch_await
    move-object v4, v5
    :join
    goto :loop_header

    :rethrow
    throw v4

    :done
    sget-object v0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object v0

    :states
    .packed-switch 0x0
        :state0
        :state1
        :state2
        :state3
    .end packed-switch
.end method
