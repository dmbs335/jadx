.class public Lloops/TestCoroutineSelectRetryBackEdge;
.super Ljava/lang/Object;
.implements Lkotlin/coroutines/Continuation;

.field private selector:Lkotlin/jvm/functions/Function1;
.field private attempt:Lkotlin/jvm/functions/Function0;
.field private result:Ljava/lang/Object;
.field private label:I

.method public getContext()Lkotlin/coroutines/CoroutineContext;
    .registers 2
    const/4 v0, 0x0
    return-object v0
.end method

.method public resumeWith(Ljava/lang/Object;)V
    .registers 2
    return-void
.end method

.method private final select(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 3
    iget-object v0, p0, Lloops/TestCoroutineSelectRetryBackEdge;->selector:Lkotlin/jvm/functions/Function1;
    invoke-interface {v0, p1}, Lkotlin/jvm/functions/Function1;->invoke(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v0
    return-object v0
.end method

.method private final tryAccept()Ljava/lang/Object;
    .registers 2
    iget-object v0, p0, Lloops/TestCoroutineSelectRetryBackEdge;->attempt:Lkotlin/jvm/functions/Function0;
    invoke-interface {v0}, Lkotlin/jvm/functions/Function0;->invoke()Ljava/lang/Object;
    move-result-object v0
    return-object v0
.end method

.method public final run(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 6

    iget-object v0, p0, Lloops/TestCoroutineSelectRetryBackEdge;->result:Ljava/lang/Object;
    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v1
    iget v2, p0, Lloops/TestCoroutineSelectRetryBackEdge;->label:I
    const/4 v3, 0x1
    if-eqz v2, :initial
    if-ne v2, v3, :bad_state

    invoke-static {v0}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :retry

    :bad_state
    new-instance v0, Ljava/lang/IllegalStateException;
    invoke-direct {v0}, Ljava/lang/IllegalStateException;-><init>()V
    throw v0

    :initial
    invoke-static {v0}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V

    :select_entry
    iput v3, p0, Lloops/TestCoroutineSelectRetryBackEdge;->label:I
    invoke-direct {p0, p0}, Lloops/TestCoroutineSelectRetryBackEdge;->select(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v0
    if-eq v0, v1, :suspended

    :retry
    invoke-direct {p0}, Lloops/TestCoroutineSelectRetryBackEdge;->tryAccept()Ljava/lang/Object;
    move-result-object v2
    if-eqz v2, :select_entry
    return-object v2

    :suspended
    return-object v1
.end method
