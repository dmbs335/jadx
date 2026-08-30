.class public final Lloops/TestCoroutineFrameAnimationCompletionLoop;
.super Lkotlin/coroutines/jvm/internal/ContinuationImpl;

.field private result:Ljava/lang/Object;
.field private label:I
.field private start:J

.method public constructor <init>(Lkotlin/coroutines/Continuation;)V
    .registers 2

    invoke-direct {p0, p1}, Lkotlin/coroutines/jvm/internal/ContinuationImpl;-><init>(Lkotlin/coroutines/Continuation;)V
    return-void
.end method

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 3

    iput-object p1, p0, Lloops/TestCoroutineFrameAnimationCompletionLoop;->result:Ljava/lang/Object;
    iget v0, p0, Lloops/TestCoroutineFrameAnimationCompletionLoop;->label:I
    const/high16 v1, -0x80000000
    or-int/2addr v0, v1
    iput v0, p0, Lloops/TestCoroutineFrameAnimationCompletionLoop;->label:I
    invoke-static {p0}, Lloops/TestCoroutineFrameAnimationCompletionLoop;->run(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v0
    return-object v0
.end method

.method public static run(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 10

    instance-of v0, p0, Lloops/TestCoroutineFrameAnimationCompletionLoop;
    if-eqz v0, :new_continuation
    move-object v0, p0
    check-cast v0, Lloops/TestCoroutineFrameAnimationCompletionLoop;
    iget v1, v0, Lloops/TestCoroutineFrameAnimationCompletionLoop;->label:I
    const/high16 v2, -0x80000000
    and-int v3, v1, v2
    if-eqz v3, :new_continuation
    sub-int/2addr v1, v2
    iput v1, v0, Lloops/TestCoroutineFrameAnimationCompletionLoop;->label:I
    goto :dispatch

    :new_continuation
    new-instance v0, Lloops/TestCoroutineFrameAnimationCompletionLoop;
    invoke-direct {v0, p0}, Lloops/TestCoroutineFrameAnimationCompletionLoop;-><init>(Lkotlin/coroutines/Continuation;)V

    :dispatch
    iget-object v1, v0, Lloops/TestCoroutineFrameAnimationCompletionLoop;->result:Ljava/lang/Object;
    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v2
    iget v3, v0, Lloops/TestCoroutineFrameAnimationCompletionLoop;->label:I
    if-eqz v3, :initial
    const/4 v8, 0x1
    if-eq v3, v8, :resume_start
    const/4 v8, 0x2
    if-ne v3, v8, :bad_state

    iget-wide v4, v0, Lloops/TestCoroutineFrameAnimationCompletionLoop;->start:J
    invoke-static {v1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :frame_result

    :resume_start
    invoke-static {v1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :start_result

    :bad_state
    new-instance v1, Ljava/lang/IllegalStateException;
    invoke-direct {v1}, Ljava/lang/IllegalStateException;-><init>()V
    throw v1

    :initial
    invoke-static {v1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    const/4 v3, 0x1
    iput v3, v0, Lloops/TestCoroutineFrameAnimationCompletionLoop;->label:I
    invoke-static {v0}, Lloops/TestCoroutineFrameAnimationCompletionLoop;->withFrameNanos(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v1
    if-eq v1, v2, :suspended

    :start_result
    check-cast v1, Ljava/lang/Number;
    invoke-virtual {v1}, Ljava/lang/Number;->longValue()J
    move-result-wide v4

    :frame_loop
    iput-wide v4, v0, Lloops/TestCoroutineFrameAnimationCompletionLoop;->start:J
    const/4 v3, 0x2
    iput v3, v0, Lloops/TestCoroutineFrameAnimationCompletionLoop;->label:I
    invoke-static {v0}, Lloops/TestCoroutineFrameAnimationCompletionLoop;->withFrameNanos(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v1
    if-eq v1, v2, :suspended

    :frame_result
    check-cast v1, Ljava/lang/Number;
    invoke-virtual {v1}, Ljava/lang/Number;->longValue()J
    move-result-wide v6
    sub-long/2addr v6, v4
    invoke-static {v6, v7}, Lloops/TestCoroutineFrameAnimationCompletionLoop;->getValueFromNanos(J)I
    move-result v3
    invoke-static {v3}, Lloops/TestCoroutineFrameAnimationCompletionLoop;->consume(I)V
    invoke-static {v6, v7}, Lloops/TestCoroutineFrameAnimationCompletionLoop;->isFinishedFromNanos(J)Z
    move-result v8
    if-eqz v8, :frame_loop
    sget-object v1, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object v1

    :suspended
    return-object v2
.end method

.method private static withFrameNanos(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 2

    const-wide/16 v0, 0x0
    invoke-static {v0, v1}, Ljava/lang/Long;->valueOf(J)Ljava/lang/Long;
    move-result-object v0
    return-object v0
.end method

.method private static getValueFromNanos(J)I
    .registers 3

    long-to-int v0, p0
    return v0
.end method

.method private static consume(I)V
    .registers 1

    return-void
.end method

.method private static isFinishedFromNanos(J)Z
    .registers 3

    const/4 v0, 0x0
    return v0
.end method
