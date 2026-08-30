.class public Lloops/TestCoroutinePollingSuspendCompletions;
.super Lkotlin/coroutines/jvm/internal/SuspendLambda;

.field private label:I
.field private rounds:I
.field private $pollingForInMillis:J
.field private $delayForEachInMillis:J
.field private $onWaitOnNextPollingResponse:Ljava/lang/Object;

.method private static suspendCall(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 1
    const/4 v0, 0x0
    return-object v0
.end method

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .locals 7

    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v0
    iget v1, p0, Lloops/TestCoroutinePollingSuspendCompletions;->label:I
    const/4 v2, 0x1
    const/4 v3, 0x2
    const/4 v4, 0x3
    const/4 v5, 0x4
    if-eqz v1, :initial
    if-eq v1, v2, :resume_one
    if-eq v1, v3, :resume_two
    if-eq v1, v4, :resume_three
    if-ne v1, v5, :bad_state

    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :after_four

    :resume_three
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :after_three

    :resume_two
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :after_two

    :resume_one
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :after_one

    :bad_state
    new-instance p1, Ljava/lang/IllegalStateException;
    invoke-direct {p1}, Ljava/lang/IllegalStateException;-><init>()V
    throw p1

    :initial
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V

    :call_one
    iput v2, p0, Lloops/TestCoroutinePollingSuspendCompletions;->label:I
    invoke-static {v2, p0}, Lloops/TestCoroutinePollingSuspendCompletions;->suspendCall(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-eq p1, v0, :suspended

    :after_one
    iput v3, p0, Lloops/TestCoroutinePollingSuspendCompletions;->label:I
    invoke-static {v3, p0}, Lloops/TestCoroutinePollingSuspendCompletions;->suspendCall(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-eq p1, v0, :suspended

    :after_two
    iput v4, p0, Lloops/TestCoroutinePollingSuspendCompletions;->label:I
    invoke-static {v4, p0}, Lloops/TestCoroutinePollingSuspendCompletions;->suspendCall(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-eq p1, v0, :suspended

    :after_three
    iget v6, p0, Lloops/TestCoroutinePollingSuspendCompletions;->rounds:I
    if-lez v6, :after_four
    iput v5, p0, Lloops/TestCoroutinePollingSuspendCompletions;->label:I
    invoke-static {v5, p0}, Lloops/TestCoroutinePollingSuspendCompletions;->suspendCall(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-eq p1, v0, :suspended

    :after_four
    iget v6, p0, Lloops/TestCoroutinePollingSuspendCompletions;->rounds:I
    add-int/lit8 v6, v6, -0x1
    iput v6, p0, Lloops/TestCoroutinePollingSuspendCompletions;->rounds:I
    if-gez v6, :call_one
    return-object p1

    :suspended
    return-object v0
.end method
