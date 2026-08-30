.class public Lloops/TestCoroutineSharedTryResumeSwitch;
.super Lkotlin/coroutines/jvm/internal/SuspendLambda;

.field private label:I
.field private rounds:I

.method private static suspendCallOne(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 1
    const/4 v0, 0x0
    return-object v0
.end method

.method private static suspendCallTwo(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 1
    const/4 v0, 0x0
    return-object v0
.end method

.method private static markOne()V
    .locals 0
    return-void
.end method

.method private static markTwo()V
    .locals 0
    return-void
.end method

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .locals 5

    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v0
    iget v1, p0, Lloops/TestCoroutineSharedTryResumeSwitch;->label:I
    packed-switch v1, :state_switch

    new-instance p1, Ljava/lang/IllegalStateException;
    invoke-direct {p1}, Ljava/lang/IllegalStateException;-><init>()V
    throw p1

    :state_zero
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :loop_header

    :shared_resume
    :try_start
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    :try_end
    .catch Ljava/util/concurrent/CancellationException; {:try_start .. :try_end} :resume_cancelled
    goto :loop_tail

    :resume_cancelled
    move-exception v4
    goto :loop_tail

    :loop_header
    iget v1, p0, Lloops/TestCoroutineSharedTryResumeSwitch;->rounds:I
    if-lez v1, :done
    and-int/lit8 v2, v1, 0x1
    if-eqz v2, :call_one

    const/4 v2, 0x2
    iput v2, p0, Lloops/TestCoroutineSharedTryResumeSwitch;->label:I
    invoke-static {v2, p0}, Lloops/TestCoroutineSharedTryResumeSwitch;->suspendCallTwo(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v3
    invoke-static {}, Lloops/TestCoroutineSharedTryResumeSwitch;->markTwo()V
    if-ne v3, v0, :loop_tail
    goto :suspended

    :call_one
    const/4 v2, 0x1
    iput v2, p0, Lloops/TestCoroutineSharedTryResumeSwitch;->label:I
    invoke-static {v2, p0}, Lloops/TestCoroutineSharedTryResumeSwitch;->suspendCallOne(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v3
    invoke-static {}, Lloops/TestCoroutineSharedTryResumeSwitch;->markOne()V
    if-eq v3, v0, :suspended

    :loop_tail
    iget v1, p0, Lloops/TestCoroutineSharedTryResumeSwitch;->rounds:I
    add-int/lit8 v1, v1, -0x1
    iput v1, p0, Lloops/TestCoroutineSharedTryResumeSwitch;->rounds:I
    goto :loop_header

    :done
    return-object p1

    :suspended
    return-object v0

    :state_switch
    .packed-switch 0x0
        :state_zero
        :shared_resume
        :shared_resume
    .end packed-switch
.end method
