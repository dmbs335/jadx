.class public final Lloops/TestCoroutineInlinedPendingSlotLoop;
.super Lkotlin/coroutines/jvm/internal/ContinuationImpl;

.field private static final NONE:Ljava/lang/Object;
.field private static final PENDING:Ljava/lang/Object;

.field private a:Ljava/lang/Object;
.field private b:Ljava/util/concurrent/atomic/AtomicReference;
.field private c:Ljava/util/concurrent/atomic/AtomicReference;
.field private d:Ljava/lang/Object;
.field private e:Ljava/util/concurrent/atomic/AtomicBoolean;
.field private y:I

.method static constructor <clinit>()V
    .registers 1

    new-instance v0, Ljava/lang/Object;
    invoke-direct {v0}, Ljava/lang/Object;-><init>()V
    sput-object v0, Lloops/TestCoroutineInlinedPendingSlotLoop;->NONE:Ljava/lang/Object;

    new-instance v0, Ljava/lang/Object;
    invoke-direct {v0}, Ljava/lang/Object;-><init>()V
    sput-object v0, Lloops/TestCoroutineInlinedPendingSlotLoop;->PENDING:Ljava/lang/Object;
    return-void
.end method

.method public constructor <init>(Lkotlin/coroutines/Continuation;)V
    .registers 2

    invoke-direct {p0, p1}, Lkotlin/coroutines/jvm/internal/ContinuationImpl;-><init>(Lkotlin/coroutines/Continuation;)V
    return-void
.end method

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 3

    iput-object p1, p0, Lloops/TestCoroutineInlinedPendingSlotLoop;->a:Ljava/lang/Object;
    iget v0, p0, Lloops/TestCoroutineInlinedPendingSlotLoop;->y:I
    const/high16 v1, -0x80000000
    or-int/2addr v0, v1
    iput v0, p0, Lloops/TestCoroutineInlinedPendingSlotLoop;->y:I
    invoke-static {p0}, Lloops/TestCoroutineInlinedPendingSlotLoop;->collect(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v0
    return-object v0
.end method

.method public static collect(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 16

    instance-of v0, p0, Lloops/TestCoroutineInlinedPendingSlotLoop;
    if-eqz v0, :new_continuation
    move-object v0, p0
    check-cast v0, Lloops/TestCoroutineInlinedPendingSlotLoop;
    iget v3, v0, Lloops/TestCoroutineInlinedPendingSlotLoop;->y:I
    const/high16 v4, -0x80000000
    and-int v5, v3, v4
    if-eqz v5, :new_continuation
    sub-int/2addr v3, v4
    iput v3, v0, Lloops/TestCoroutineInlinedPendingSlotLoop;->y:I
    goto :dispatch

    :new_continuation
    new-instance v0, Lloops/TestCoroutineInlinedPendingSlotLoop;
    invoke-direct {v0, p0}, Lloops/TestCoroutineInlinedPendingSlotLoop;-><init>(Lkotlin/coroutines/Continuation;)V

    :dispatch
    :try_start
    iget-object v1, v0, Lloops/TestCoroutineInlinedPendingSlotLoop;->a:Ljava/lang/Object;
    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v2
    iget v3, v0, Lloops/TestCoroutineInlinedPendingSlotLoop;->y:I
    const/4 v4, 0x2
    const/4 v5, 0x3
    if-eqz v3, :initial
    if-eq v3, v4, :resume_emit
    if-eq v3, v5, :resume_pending
    goto :bad_state

    :resume_emit
    iget-object v6, v0, Lloops/TestCoroutineInlinedPendingSlotLoop;->b:Ljava/util/concurrent/atomic/AtomicReference;
    iget-object v7, v0, Lloops/TestCoroutineInlinedPendingSlotLoop;->c:Ljava/util/concurrent/atomic/AtomicReference;
    iget-object v9, v0, Lloops/TestCoroutineInlinedPendingSlotLoop;->d:Ljava/lang/Object;
    iget-object v14, v0, Lloops/TestCoroutineInlinedPendingSlotLoop;->e:Ljava/util/concurrent/atomic/AtomicBoolean;
    invoke-static {v1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :post_emit

    :resume_pending
    iget-object v6, v0, Lloops/TestCoroutineInlinedPendingSlotLoop;->b:Ljava/util/concurrent/atomic/AtomicReference;
    iget-object v7, v0, Lloops/TestCoroutineInlinedPendingSlotLoop;->c:Ljava/util/concurrent/atomic/AtomicReference;
    iget-object v8, v0, Lloops/TestCoroutineInlinedPendingSlotLoop;->d:Ljava/lang/Object;
    iget-object v14, v0, Lloops/TestCoroutineInlinedPendingSlotLoop;->e:Ljava/util/concurrent/atomic/AtomicBoolean;
    invoke-static {v1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :loop

    :bad_state
    new-instance v1, Ljava/lang/IllegalStateException;
    invoke-direct {v1}, Ljava/lang/IllegalStateException;-><init>()V
    throw v1

    :initial
    invoke-static {v1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    new-instance v6, Ljava/util/concurrent/atomic/AtomicReference;
    sget-object v10, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    invoke-direct {v6, v10}, Ljava/util/concurrent/atomic/AtomicReference;-><init>(Ljava/lang/Object;)V
    new-instance v7, Ljava/util/concurrent/atomic/AtomicReference;
    sget-object v11, Lloops/TestCoroutineInlinedPendingSlotLoop;->NONE:Ljava/lang/Object;
    invoke-direct {v7, v11}, Ljava/util/concurrent/atomic/AtomicReference;-><init>(Ljava/lang/Object;)V
    const/4 v8, 0x0
    const/4 v13, 0x1
    new-instance v14, Ljava/util/concurrent/atomic/AtomicBoolean;
    invoke-direct {v14, v13}, Ljava/util/concurrent/atomic/AtomicBoolean;-><init>(Z)V

    :loop
    invoke-virtual {v6}, Ljava/util/concurrent/atomic/AtomicReference;->get()Ljava/lang/Object;
    move-result-object v9
    if-eqz v14, :active
    invoke-virtual {v14}, Ljava/util/concurrent/atomic/AtomicBoolean;->get()Z
    move-result v13
    if-nez v13, :active
    new-instance v13, Ljava/lang/IllegalStateException;
    invoke-direct {v13}, Ljava/lang/IllegalStateException;-><init>()V
    throw v13

    :active
    if-eqz v8, :emit
    invoke-virtual {v8, v9}, Ljava/lang/Object;->equals(Ljava/lang/Object;)Z
    move-result v13
    if-nez v13, :pending

    :emit
    iput-object v6, v0, Lloops/TestCoroutineInlinedPendingSlotLoop;->b:Ljava/util/concurrent/atomic/AtomicReference;
    iput-object v7, v0, Lloops/TestCoroutineInlinedPendingSlotLoop;->c:Ljava/util/concurrent/atomic/AtomicReference;
    iput-object v9, v0, Lloops/TestCoroutineInlinedPendingSlotLoop;->d:Ljava/lang/Object;
    iput-object v14, v0, Lloops/TestCoroutineInlinedPendingSlotLoop;->e:Ljava/util/concurrent/atomic/AtomicBoolean;
    iput v4, v0, Lloops/TestCoroutineInlinedPendingSlotLoop;->y:I
    invoke-static {v9, v0}, Lloops/TestCoroutineInlinedPendingSlotLoop;->emit(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v10
    if-eq v10, v2, :suspended

    :post_emit
    move-object v8, v9

    :pending
    sget-object v11, Lloops/TestCoroutineInlinedPendingSlotLoop;->NONE:Ljava/lang/Object;
    invoke-virtual {v7, v11}, Ljava/util/concurrent/atomic/AtomicReference;->getAndSet(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v10
    sget-object v12, Lloops/TestCoroutineInlinedPendingSlotLoop;->PENDING:Ljava/lang/Object;
    if-eq v10, v12, :loop

    iput-object v6, v0, Lloops/TestCoroutineInlinedPendingSlotLoop;->b:Ljava/util/concurrent/atomic/AtomicReference;
    iput-object v7, v0, Lloops/TestCoroutineInlinedPendingSlotLoop;->c:Ljava/util/concurrent/atomic/AtomicReference;
    iput-object v8, v0, Lloops/TestCoroutineInlinedPendingSlotLoop;->d:Ljava/lang/Object;
    iput-object v14, v0, Lloops/TestCoroutineInlinedPendingSlotLoop;->e:Ljava/util/concurrent/atomic/AtomicBoolean;
    iput v5, v0, Lloops/TestCoroutineInlinedPendingSlotLoop;->y:I

    :cas
    invoke-virtual {v7, v11, v0}, Ljava/util/concurrent/atomic/AtomicReference;->compareAndSet(Ljava/lang/Object;Ljava/lang/Object;)Z
    move-result v13
    if-nez v13, :await_result
    invoke-virtual {v7}, Ljava/util/concurrent/atomic/AtomicReference;->get()Ljava/lang/Object;
    move-result-object v10
    if-eq v10, v11, :cas

    :await_result
    invoke-static {v0}, Lloops/TestCoroutineInlinedPendingSlotLoop;->awaitResult(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v10
    if-eq v10, v2, :suspended
    goto :loop
    :try_end
    .catchall {:try_start .. :try_end} :handler

    :suspended
    return-object v2

    :handler
    move-exception v14
    invoke-static {}, Lloops/TestCoroutineInlinedPendingSlotLoop;->cleanup()V
    throw v14
.end method

.method private static emit(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 2

    sget-object p0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object p0
.end method

.method private static awaitResult(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 1

    sget-object p0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object p0
.end method

.method private static cleanup()V
    .registers 0

    return-void
.end method
