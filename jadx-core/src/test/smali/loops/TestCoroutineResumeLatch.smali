.class public Lloops/TestCoroutineResumeLatch;
.super Ljava/lang/Object;

.field private index:I
.field private label:I

.method private static throwOnFailure(Ljava/lang/Object;)V
    .locals 0
    return-void
.end method

.method private static suspend(Ljava/lang/Object;)Ljava/lang/Object;
    .locals 0
    return-object p0
.end method

.method public run(Ljava/lang/Object;Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 4

    iget v0, p0, Lloops/TestCoroutineResumeLatch;->label:I
    if-eqz v0, :initial
    const/4 v1, 0x1
    if-ne v0, v1, :bad_state
    iget v2, p0, Lloops/TestCoroutineResumeLatch;->index:I
    invoke-static {p1}, Lloops/TestCoroutineResumeLatch;->throwOnFailure(Ljava/lang/Object;)V
    goto :latch

    :bad_state
    new-instance v0, Ljava/lang/IllegalStateException;
    invoke-direct {v0}, Ljava/lang/IllegalStateException;-><init>()V
    throw v0

    :initial
    invoke-static {p1}, Lloops/TestCoroutineResumeLatch;->throwOnFailure(Ljava/lang/Object;)V
    const/4 v2, 0x0

    :header
    const/4 v3, 0x4
    if-ge v2, v3, :done
    iput v2, p0, Lloops/TestCoroutineResumeLatch;->index:I
    const/4 v0, 0x1
    iput v0, p0, Lloops/TestCoroutineResumeLatch;->label:I
    invoke-static {p1}, Lloops/TestCoroutineResumeLatch;->suspend(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v0
    if-ne v0, p2, :latch
    return-object p2

    :latch
    add-int/lit8 v2, v2, 0x1
    goto :header

    :done
    return-object p1
.end method
