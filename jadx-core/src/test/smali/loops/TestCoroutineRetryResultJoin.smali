.class public Lloops/TestCoroutineRetryResultJoin;
.super Lkotlin/coroutines/jvm/internal/SuspendLambda;

.field private call:Lkotlin/jvm/functions/Function3;
.field private retryCount:I
.field private index:I
.field private result:Ljava/lang/Object;
.field private label:I

.method private static isServerError(Ljava/lang/Object;)Z
    .locals 0
    const/4 p0, 0x1
    return p0
.end method

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 10

    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v0
    iget v1, p0, Lloops/TestCoroutineRetryResultJoin;->label:I
    const/4 v2, 0x2
    const/4 v3, 0x1
    if-eqz v1, :initial
    if-eq v1, v3, :resume_call
    if-ne v1, v2, :bad_state

    iget v1, p0, Lloops/TestCoroutineRetryResultJoin;->index:I
    iget-object v4, p0, Lloops/TestCoroutineRetryResultJoin;->result:Ljava/lang/Object;
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :retry_call_entry

    :bad_state
    new-instance p1, Ljava/lang/IllegalStateException;
    invoke-direct {p1}, Ljava/lang/IllegalStateException;-><init>()V
    throw p1

    :resume_call
    iget v1, p0, Lloops/TestCoroutineRetryResultJoin;->index:I
    iget-object v4, p0, Lloops/TestCoroutineRetryResultJoin;->result:Ljava/lang/Object;
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :call_result

    :initial
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    const/4 p1, 0x0
    const/4 v1, 0x0
    const/4 v4, 0x0

    :retry_call_entry
    move-object p1, v4
    goto :call

    :call
    iget-object v4, p0, Lloops/TestCoroutineRetryResultJoin;->call:Lkotlin/jvm/functions/Function3;
    invoke-static {v1}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
    move-result-object v6
    iput-object p1, p0, Lloops/TestCoroutineRetryResultJoin;->result:Ljava/lang/Object;
    iput v1, p0, Lloops/TestCoroutineRetryResultJoin;->index:I
    iput v3, p0, Lloops/TestCoroutineRetryResultJoin;->label:I
    invoke-interface {v4, p1, v6, p0}, Lkotlin/jvm/functions/Function3;->invoke(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object p1
    if-eq p1, v0, :suspended

    :call_result
    move-object v4, p1
    add-int/lit8 p1, v1, 0x1
    iget v5, p0, Lloops/TestCoroutineRetryResultJoin;->retryCount:I
    if-ge v1, v5, :done
    if-eqz v4, :done
    invoke-static {v4}, Lloops/TestCoroutineRetryResultJoin;->isServerError(Ljava/lang/Object;)Z
    move-result v1
    if-ne v1, v3, :done

    iput-object v4, p0, Lloops/TestCoroutineRetryResultJoin;->result:Ljava/lang/Object;
    iput p1, p0, Lloops/TestCoroutineRetryResultJoin;->index:I
    iput v2, p0, Lloops/TestCoroutineRetryResultJoin;->label:I
    const-wide/16 v5, 0x1
    invoke-static {v5, v6, p0}, Lkotlinx/coroutines/DelayKt;->delay(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v1
    if-eq v1, v0, :suspended
    move v1, p1
    goto :retry_call_entry

    :suspended
    return-object v0

    :done
    return-object v4
.end method
