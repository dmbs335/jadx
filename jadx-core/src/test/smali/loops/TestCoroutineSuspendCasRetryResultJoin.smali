.class public final Lloops/TestCoroutineSuspendCasRetryResultJoin;
.super Lkotlin/coroutines/jvm/internal/ContinuationImpl;

.field private result:Ljava/lang/Object;
.field private label:I
.field private flow:Ljava/util/concurrent/atomic/AtomicReference;
.field private old:Ljava/lang/Object;

.method public constructor <init>(Lkotlin/coroutines/Continuation;)V
    .registers 2

    invoke-direct {p0, p1}, Lkotlin/coroutines/jvm/internal/ContinuationImpl;-><init>(Lkotlin/coroutines/Continuation;)V
    return-void
.end method

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 3

    iput-object p1, p0, Lloops/TestCoroutineSuspendCasRetryResultJoin;->result:Ljava/lang/Object;
    iget v0, p0, Lloops/TestCoroutineSuspendCasRetryResultJoin;->label:I
    const/high16 v1, -0x80000000
    or-int/2addr v0, v1
    iput v0, p0, Lloops/TestCoroutineSuspendCasRetryResultJoin;->label:I
    invoke-static {p0}, Lloops/TestCoroutineSuspendCasRetryResultJoin;->run(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v0
    return-object v0
.end method

.method public static run(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 10

    instance-of v0, p0, Lloops/TestCoroutineSuspendCasRetryResultJoin;
    if-eqz v0, :new_continuation
    move-object v0, p0
    check-cast v0, Lloops/TestCoroutineSuspendCasRetryResultJoin;
    iget v1, v0, Lloops/TestCoroutineSuspendCasRetryResultJoin;->label:I
    const/high16 v2, -0x80000000
    and-int v3, v1, v2
    if-eqz v3, :new_continuation
    sub-int/2addr v1, v2
    iput v1, v0, Lloops/TestCoroutineSuspendCasRetryResultJoin;->label:I
    goto :dispatch

    :new_continuation
    new-instance v0, Lloops/TestCoroutineSuspendCasRetryResultJoin;
    invoke-direct {v0, p0}, Lloops/TestCoroutineSuspendCasRetryResultJoin;-><init>(Lkotlin/coroutines/Continuation;)V

    :dispatch
    iget-object v1, v0, Lloops/TestCoroutineSuspendCasRetryResultJoin;->result:Ljava/lang/Object;
    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v2
    iget v3, v0, Lloops/TestCoroutineSuspendCasRetryResultJoin;->label:I
    if-eqz v3, :initial
    const/4 v8, 0x1
    if-ne v3, v8, :bad_state

    iget-object v4, v0, Lloops/TestCoroutineSuspendCasRetryResultJoin;->flow:Ljava/util/concurrent/atomic/AtomicReference;
    iget-object v5, v0, Lloops/TestCoroutineSuspendCasRetryResultJoin;->old:Ljava/lang/Object;
    move-object v8, v4
    invoke-static {v1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    move-object v3, v1
    goto :result_join

    :bad_state
    new-instance v1, Ljava/lang/IllegalStateException;
    invoke-direct {v1}, Ljava/lang/IllegalStateException;-><init>()V
    throw v1

    :initial
    invoke-static {v1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    invoke-static {}, Lloops/TestCoroutineSuspendCasRetryResultJoin;->flow()Ljava/util/concurrent/atomic/AtomicReference;
    move-result-object v4
    move-object v8, v4

    :retry
    invoke-virtual {v4}, Ljava/util/concurrent/atomic/AtomicReference;->get()Ljava/lang/Object;
    move-result-object v5
    iput-object v4, v0, Lloops/TestCoroutineSuspendCasRetryResultJoin;->flow:Ljava/util/concurrent/atomic/AtomicReference;
    iput-object v5, v0, Lloops/TestCoroutineSuspendCasRetryResultJoin;->old:Ljava/lang/Object;
    const/4 v3, 0x1
    iput v3, v0, Lloops/TestCoroutineSuspendCasRetryResultJoin;->label:I
    invoke-static {v0}, Lloops/TestCoroutineSuspendCasRetryResultJoin;->withTimeoutOrNull(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v1
    if-eq v1, v2, :suspended

    move-object v3, v1

    :result_join
    invoke-static {v3}, Lloops/TestCoroutineSuspendCasRetryResultJoin;->toState(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v6
    invoke-virtual {v4, v5, v6}, Ljava/util/concurrent/atomic/AtomicReference;->compareAndSet(Ljava/lang/Object;Ljava/lang/Object;)Z
    move-result v7
    if-nez v7, :success
    move-object v4, v8
    goto :retry

    :success
    sget-object v1, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object v1

    :suspended
    return-object v2
.end method

.method private static flow()Ljava/util/concurrent/atomic/AtomicReference;
    .registers 1

    const/4 v0, 0x0
    return-object v0
.end method

.method private static withTimeoutOrNull(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 1

    const/4 v0, 0x0
    return-object v0
.end method

.method private static toState(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 1

    return-object p0
.end method
