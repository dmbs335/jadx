.class public final Lloops/TestCoroutineMutexTryCompletion;
.super Lkotlin/coroutines/jvm/internal/SuspendLambda;

.field private I$0:I
.field private L$0:Ljava/lang/Object;
.field private label:I
.field private mutex:Lkotlinx/coroutines/sync/Mutex;

.method public constructor <init>(Lkotlinx/coroutines/sync/Mutex;Lkotlin/coroutines/Continuation;)V
    .locals 1
    const/4 v0, 0x1
    invoke-direct {p0, v0, p2}, Lkotlin/coroutines/jvm/internal/SuspendLambda;-><init>(ILkotlin/coroutines/Continuation;)V
    iput-object p1, p0, Lloops/TestCoroutineMutexTryCompletion;->mutex:Lkotlinx/coroutines/sync/Mutex;
    return-void
.end method

.method private static work(I)V
    .locals 0
    return-void
.end method

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .locals 6

    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v0
    iget v1, p0, Lloops/TestCoroutineMutexTryCompletion;->label:I
    const/4 v4, 0x0
    const/4 v5, 0x1
    if-eqz v1, :initial
    if-ne v1, v5, :bad_state

    iget v2, p0, Lloops/TestCoroutineMutexTryCompletion;->I$0:I
    iget-object v3, p0, Lloops/TestCoroutineMutexTryCompletion;->L$0:Ljava/lang/Object;
    check-cast v3, Lkotlinx/coroutines/sync/Mutex;
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :locked

    :bad_state
    new-instance v1, Ljava/lang/IllegalStateException;
    invoke-direct {v1}, Ljava/lang/IllegalStateException;-><init>()V
    throw v1

    :initial
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    const/4 v2, 0x0

    :loop
    const/4 v1, 0x4
    if-ge v2, v1, :done
    and-int/lit8 v1, v2, 0x1
    if-eqz v1, :advance
    const/4 v1, 0x3
    if-eq v2, v1, :advance
    iget-object v3, p0, Lloops/TestCoroutineMutexTryCompletion;->mutex:Lkotlinx/coroutines/sync/Mutex;
    iput v2, p0, Lloops/TestCoroutineMutexTryCompletion;->I$0:I
    iput-object v3, p0, Lloops/TestCoroutineMutexTryCompletion;->L$0:Ljava/lang/Object;
    iput v5, p0, Lloops/TestCoroutineMutexTryCompletion;->label:I
    invoke-interface {v3, v4, p0}, Lkotlinx/coroutines/sync/Mutex;->lock(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-eq p1, v0, :suspended

    :locked
    :try_start
    invoke-static {v2}, Lloops/TestCoroutineMutexTryCompletion;->work(I)V
    :try_end
    invoke-interface {v3, v4}, Lkotlinx/coroutines/sync/Mutex;->unlock(Ljava/lang/Object;)V

    :advance
    add-int/lit8 v2, v2, 0x1
    goto :loop

    :catch
    move-exception p1
    invoke-interface {v3, v4}, Lkotlinx/coroutines/sync/Mutex;->unlock(Ljava/lang/Object;)V
    throw p1

    :suspended
    return-object v0

    :done
    sget-object p1, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object p1

    .catchall {:try_start .. :try_end} :catch
.end method
