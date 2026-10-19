.class public Lloops/TestCoroutineInjectedRetryDelayResultJoin;
.super Lkotlin/coroutines/jvm/internal/SuspendLambda;

.field private call:Lkotlin/jvm/functions/Function3;
.field private delay:Lkotlin/jvm/functions/Function2;
.field private retryCount:I
.field private index:I
.field private result:Ljava/lang/Object;
.field private label:I

.method public constructor <init>(Lkotlin/coroutines/Continuation;)V
    .registers 3
    const/4 v0, 0x2
    invoke-direct {p0, v0, p1}, Lkotlin/coroutines/jvm/internal/SuspendLambda;-><init>(ILkotlin/coroutines/Continuation;)V
    return-void
.end method

.method public constructor <init>()V
    .registers 3
    const/4 v0, 0x2
    const/4 v1, 0x0
    invoke-direct {p0, v0, v1}, Lkotlin/coroutines/jvm/internal/SuspendLambda;-><init>(ILkotlin/coroutines/Continuation;)V
    return-void
.end method

.method public check()V
    .registers 3
    const/4 v0, 0x1
    iput v0, p0, Lloops/TestCoroutineInjectedRetryDelayResultJoin;->label:I
    const-string v0, "resume-result"
    invoke-virtual {p0, v0}, Lloops/TestCoroutineInjectedRetryDelayResultJoin;->invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v1
    if-eq v0, v1, :pass
    new-instance v0, Ljava/lang/AssertionError;
    invoke-direct {v0}, Ljava/lang/AssertionError;-><init>()V
    throw v0
    :pass
    return-void
.end method

.method private static isServerError(Ljava/lang/Object;)Z
    .locals 0
    const/4 p0, 0x1
    return p0
.end method

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 10

    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v0
    iget v1, p0, Lloops/TestCoroutineInjectedRetryDelayResultJoin;->label:I
    const/4 v2, 0x2
    const/4 v3, 0x1
    if-eqz v1, :initial
    if-eq v1, v3, :resume_call
    if-ne v1, v2, :bad_state

    iget v1, p0, Lloops/TestCoroutineInjectedRetryDelayResultJoin;->index:I
    iget-object v4, p0, Lloops/TestCoroutineInjectedRetryDelayResultJoin;->result:Ljava/lang/Object;
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :retry_call_entry

    :bad_state
    new-instance p1, Ljava/lang/IllegalStateException;
    invoke-direct {p1}, Ljava/lang/IllegalStateException;-><init>()V
    throw p1

    :resume_call
    iget v1, p0, Lloops/TestCoroutineInjectedRetryDelayResultJoin;->index:I
    iget-object v4, p0, Lloops/TestCoroutineInjectedRetryDelayResultJoin;->result:Ljava/lang/Object;
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :call_result

    :initial
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    const/4 p1, 0x0
    const/4 v1, 0x0
    const/4 v4, 0x0

    :retry_call_entry
    move-object p1, v4

    :call
    iget-object v4, p0, Lloops/TestCoroutineInjectedRetryDelayResultJoin;->call:Lkotlin/jvm/functions/Function3;
    invoke-static {v1}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
    move-result-object v6
    iput-object p1, p0, Lloops/TestCoroutineInjectedRetryDelayResultJoin;->result:Ljava/lang/Object;
    iput v1, p0, Lloops/TestCoroutineInjectedRetryDelayResultJoin;->index:I
    iput v3, p0, Lloops/TestCoroutineInjectedRetryDelayResultJoin;->label:I
    invoke-interface {v4, p1, v6, p0}, Lkotlin/jvm/functions/Function3;->invoke(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object p1
    if-eq p1, v0, :suspended

    :call_result
    move-object v4, p1
    add-int/lit8 p1, v1, 0x1
    iget v5, p0, Lloops/TestCoroutineInjectedRetryDelayResultJoin;->retryCount:I
    if-ge v1, v5, :done
    if-eqz v4, :done
    invoke-static {v4}, Lloops/TestCoroutineInjectedRetryDelayResultJoin;->isServerError(Ljava/lang/Object;)Z
    move-result v1
    if-ne v1, v3, :done

    iput-object v4, p0, Lloops/TestCoroutineInjectedRetryDelayResultJoin;->result:Ljava/lang/Object;
    iput p1, p0, Lloops/TestCoroutineInjectedRetryDelayResultJoin;->index:I
    iput v2, p0, Lloops/TestCoroutineInjectedRetryDelayResultJoin;->label:I
    iget-object v5, p0, Lloops/TestCoroutineInjectedRetryDelayResultJoin;->delay:Lkotlin/jvm/functions/Function2;
    const-wide/16 v6, 0x1
    invoke-static {v6, v7}, Ljava/lang/Long;->valueOf(J)Ljava/lang/Long;
    move-result-object v6
    invoke-interface {v5, v6, p0}, Lkotlin/jvm/functions/Function2;->invoke(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v1
    if-eq v1, v0, :suspended
    move v1, p1
    goto :retry_call_entry

    :suspended
    return-object v0

    :done
    return-object v4
.end method
