.class public final Lloops/TestCoroutineMergedDelayPollingLoop;
.super Lkotlin/coroutines/jvm/internal/SuspendLambda;

.field private count:I
.field private label:I
.field private scope:Ljava/lang/Object;
.field private start:J

.method public constructor <init>(Lkotlin/coroutines/Continuation;)V
    .registers 4

    const/4 v0, 0x2
    invoke-direct {p0, v0, p1}, Lkotlin/coroutines/jvm/internal/SuspendLambda;-><init>(ILkotlin/coroutines/Continuation;)V
    return-void
.end method

.method private static native delay(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;
.end method

.method private static native hasResult(Ljava/lang/Object;)Z
.end method

.method private static native isActive(Ljava/lang/Object;)Z
.end method

.method private static native remaining()J
.end method

.method private static native request(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
.end method

.method private static native use(Ljava/lang/Object;)V
.end method

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 13

    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v0
    iget v1, p0, Lloops/TestCoroutineMergedDelayPollingLoop;->label:I
    const/4 v2, 0x1
    const/4 v3, 0x2
    const/4 v4, 0x3
    if-eqz v1, :initial
    if-eq v1, v2, :resume_request
    if-eq v1, v3, :resume_delay
    if-ne v1, v4, :bad_state

    :resume_delay
    iget v6, p0, Lloops/TestCoroutineMergedDelayPollingLoop;->count:I
    iget-object v5, p0, Lloops/TestCoroutineMergedDelayPollingLoop;->scope:Ljava/lang/Object;
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :delay_merge

    :resume_request
    iget-wide v7, p0, Lloops/TestCoroutineMergedDelayPollingLoop;->start:J
    iget v6, p0, Lloops/TestCoroutineMergedDelayPollingLoop;->count:I
    iget-object v5, p0, Lloops/TestCoroutineMergedDelayPollingLoop;->scope:Ljava/lang/Object;
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :request_result

    :initial
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    iget-object v5, p0, Lloops/TestCoroutineMergedDelayPollingLoop;->scope:Ljava/lang/Object;
    const/4 v6, 0x0
    move-object p1, v5
    goto :loop

    :delay_merge
    move-object p1, v5

    :loop
    add-int/lit8 v6, v6, 0x1
    move-object v5, p1
    iput-object v5, p0, Lloops/TestCoroutineMergedDelayPollingLoop;->scope:Ljava/lang/Object;
    iput v6, p0, Lloops/TestCoroutineMergedDelayPollingLoop;->count:I
    invoke-static {}, Ljava/lang/System;->currentTimeMillis()J
    move-result-wide v7
    iput-wide v7, p0, Lloops/TestCoroutineMergedDelayPollingLoop;->start:J
    iput v2, p0, Lloops/TestCoroutineMergedDelayPollingLoop;->label:I
    invoke-static {p0}, Lloops/TestCoroutineMergedDelayPollingLoop;->request(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-eq p1, v0, :suspended

    :request_result
    invoke-static {p1}, Lloops/TestCoroutineMergedDelayPollingLoop;->hasResult(Ljava/lang/Object;)Z
    move-result v1
    if-eqz v1, :choose_delay
    invoke-static {p1}, Lloops/TestCoroutineMergedDelayPollingLoop;->use(Ljava/lang/Object;)V
    goto :done

    :choose_delay
    invoke-static {v5}, Lloops/TestCoroutineMergedDelayPollingLoop;->isActive(Ljava/lang/Object;)Z
    move-result v1
    if-eqz v1, :short_delay
    invoke-static {}, Lloops/TestCoroutineMergedDelayPollingLoop;->remaining()J
    move-result-wide v7
    iput-object v5, p0, Lloops/TestCoroutineMergedDelayPollingLoop;->scope:Ljava/lang/Object;
    iput v6, p0, Lloops/TestCoroutineMergedDelayPollingLoop;->count:I
    iput v3, p0, Lloops/TestCoroutineMergedDelayPollingLoop;->label:I
    invoke-static {v7, v8, p0}, Lloops/TestCoroutineMergedDelayPollingLoop;->delay(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    goto :delay_check

    :short_delay
    iput-object v5, p0, Lloops/TestCoroutineMergedDelayPollingLoop;->scope:Ljava/lang/Object;
    iput v6, p0, Lloops/TestCoroutineMergedDelayPollingLoop;->count:I
    iput v4, p0, Lloops/TestCoroutineMergedDelayPollingLoop;->label:I
    const-wide/16 v7, 0x1
    invoke-static {v7, v8, p0}, Lloops/TestCoroutineMergedDelayPollingLoop;->delay(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1

    :delay_check
    if-eq p1, v0, :suspended
    goto :delay_merge

    :suspended
    return-object v0

    :done
    sget-object p1, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object p1

    :bad_state
    new-instance p1, Ljava/lang/IllegalStateException;
    invoke-direct {p1}, Ljava/lang/IllegalStateException;-><init>()V
    throw p1
.end method
