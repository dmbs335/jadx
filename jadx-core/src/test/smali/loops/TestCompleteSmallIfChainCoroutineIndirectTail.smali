.class public Lloops/TestCompleteSmallIfChainCoroutineIndirectTail;
.super Ljava/lang/Object;
.implements Lkotlin/coroutines/Continuation;

.field private label:I
.field private marker:I
.field private result:Ljava/lang/Object;

.method private static sendValue(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 1
    sget-object v0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object v0
.end method

.method private static shouldCorrect()Z
    .locals 1
    const/4 v0, 0x0
    return v0
.end method

.method private static throwOnFailure(Ljava/lang/Object;)V
    .locals 0
    return-void
.end method

.method private static waitCorrection(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 1
    sget-object v0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object v0
.end method

.method private static waitInitial(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 1
    sget-object v0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object v0
.end method

.method private static waitNormally(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 1
    sget-object v0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object v0
.end method

.method public getContext()Lkotlin/coroutines/CoroutineContext;
    .locals 1
    const/4 v0, 0x0
    return-object v0
.end method

.method public resumeWith(Ljava/lang/Object;)V
    .locals 0
    return-void
.end method

.method public run(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 4

    iget-object v3, p0, Lloops/TestCompleteSmallIfChainCoroutineIndirectTail;->result:Ljava/lang/Object;
    iget v0, p0, Lloops/TestCompleteSmallIfChainCoroutineIndirectTail;->label:I
    if-eqz v0, :initial
    const/4 v1, 0x1
    if-eq v0, v1, :resume_initial
    const/4 v1, 0x2
    if-eq v0, v1, :resume_send
    const/4 v1, 0x3
    if-eq v0, v1, :resume_correction
    const/4 v1, 0x4
    if-eq v0, v1, :resume_normal
    new-instance v0, Ljava/lang/IllegalStateException;
    invoke-direct {v0}, Ljava/lang/IllegalStateException;-><init>()V
    throw v0

    :resume_normal
    invoke-static {v3}, Lloops/TestCompleteSmallIfChainCoroutineIndirectTail;->throwOnFailure(Ljava/lang/Object;)V
    goto :normal_done

    :resume_correction
    invoke-static {v3}, Lloops/TestCompleteSmallIfChainCoroutineIndirectTail;->throwOnFailure(Ljava/lang/Object;)V
    goto :correction_done

    :resume_send
    invoke-static {v3}, Lloops/TestCompleteSmallIfChainCoroutineIndirectTail;->throwOnFailure(Ljava/lang/Object;)V
    goto :send_done

    :resume_initial
    invoke-static {v3}, Lloops/TestCompleteSmallIfChainCoroutineIndirectTail;->throwOnFailure(Ljava/lang/Object;)V
    goto :initial_done

    :initial
    invoke-static {v3}, Lloops/TestCompleteSmallIfChainCoroutineIndirectTail;->throwOnFailure(Ljava/lang/Object;)V
    const/4 v0, 0x1
    iput v0, p0, Lloops/TestCompleteSmallIfChainCoroutineIndirectTail;->label:I
    invoke-static {p0}, Lloops/TestCompleteSmallIfChainCoroutineIndirectTail;->waitInitial(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v2
    if-eq v2, p1, :suspended

    :initial_done
    :send_header
    const/4 v0, 0x2
    iput v0, p0, Lloops/TestCompleteSmallIfChainCoroutineIndirectTail;->label:I
    invoke-static {p0}, Lloops/TestCompleteSmallIfChainCoroutineIndirectTail;->sendValue(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v2
    if-eq v2, p1, :suspended

    :send_done
    invoke-static {}, Lloops/TestCompleteSmallIfChainCoroutineIndirectTail;->shouldCorrect()Z
    move-result v1
    if-eqz v1, :normal_delay

    const/4 v0, 0x3
    iput v0, p0, Lloops/TestCompleteSmallIfChainCoroutineIndirectTail;->label:I
    invoke-static {p0}, Lloops/TestCompleteSmallIfChainCoroutineIndirectTail;->waitCorrection(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v2
    if-eq v2, p1, :suspended

    :correction_done
    const/4 v1, 0x3
    iput v1, p0, Lloops/TestCompleteSmallIfChainCoroutineIndirectTail;->marker:I
    goto :send_header

    :normal_delay
    const/4 v0, 0x4
    iput v0, p0, Lloops/TestCompleteSmallIfChainCoroutineIndirectTail;->label:I
    invoke-static {p0}, Lloops/TestCompleteSmallIfChainCoroutineIndirectTail;->waitNormally(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v2
    if-eq v2, p1, :suspended

    :normal_done
    const/4 v1, 0x4
    iput v1, p0, Lloops/TestCompleteSmallIfChainCoroutineIndirectTail;->marker:I
    goto :send_header

    :suspended
    return-object p1
.end method
