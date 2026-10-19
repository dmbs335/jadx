.class public Lloops/TestProtectedCoroutineThreeStateRetry;
.super Lkotlin/coroutines/jvm/internal/SuspendLambda;

.field public delay:Lkotlin/jvm/functions/Function2;
.field public call:Lkotlin/jvm/functions/Function2;
.field public retryCount:I
.field public index:I
.field public label:I

.method public constructor <init>(Lkotlin/coroutines/Continuation;)V
    .registers 3
    const/4 v0, 0x2
    invoke-direct {p0, v0, p1}, Lkotlin/coroutines/jvm/internal/SuspendLambda;-><init>(ILkotlin/coroutines/Continuation;)V
    return-void
.end method

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 9

    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v0
    iget v1, p0, Lloops/TestProtectedCoroutineThreeStateRetry;->label:I
    const/4 v2, 0x1
    const/4 v3, 0x2
    if-eqz v1, :initial
    if-eq v1, v2, :resume_delay
    if-ne v1, v3, :bad_state

    iget v4, p0, Lloops/TestProtectedCoroutineThreeStateRetry;->index:I
    :resume_call_try_start
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    :resume_call_try_end
    goto :call_done

    :resume_delay
    iget v4, p0, Lloops/TestProtectedCoroutineThreeStateRetry;->index:I
    :resume_delay_try_start
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    :resume_delay_try_end
    goto :after_delay

    :bad_state
    new-instance p1, Ljava/lang/IllegalStateException;
    invoke-direct {p1}, Ljava/lang/IllegalStateException;-><init>()V
    throw p1

    :initial
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    const/4 v4, 0x0

    :retry_guard
    iget v5, p0, Lloops/TestProtectedCoroutineThreeStateRetry;->retryCount:I
    if-gt v4, v5, :done

    :body_try_start
    iput v4, p0, Lloops/TestProtectedCoroutineThreeStateRetry;->index:I
    iput v2, p0, Lloops/TestProtectedCoroutineThreeStateRetry;->label:I
    iget-object v5, p0, Lloops/TestProtectedCoroutineThreeStateRetry;->delay:Lkotlin/jvm/functions/Function2;
    invoke-static {v4}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
    move-result-object v6
    invoke-interface {v5, v6, p0}, Lkotlin/jvm/functions/Function2;->invoke(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object p1
    if-eq p1, v0, :suspended

    :after_delay
    iput v3, p0, Lloops/TestProtectedCoroutineThreeStateRetry;->label:I
    iget-object v5, p0, Lloops/TestProtectedCoroutineThreeStateRetry;->call:Lkotlin/jvm/functions/Function2;
    invoke-static {v4}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
    move-result-object v6
    invoke-interface {v5, v6, p0}, Lkotlin/jvm/functions/Function2;->invoke(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object p1
    if-eq p1, v0, :suspended

    :call_done
    :body_try_end
    return-object p1

    :suspended
    return-object v0

    :handler
    move-exception p1
    add-int/lit8 v4, v4, 0x1
    iget v5, p0, Lloops/TestProtectedCoroutineThreeStateRetry;->retryCount:I
    if-gt v4, v5, :rethrow
    goto :retry_guard

    :rethrow
    throw p1

    :done
    const/4 p1, 0x0
    return-object p1

    .catchall {:resume_call_try_start .. :resume_call_try_end} :handler
    .catchall {:resume_delay_try_start .. :resume_delay_try_end} :handler
    .catchall {:body_try_start .. :body_try_end} :handler
.end method
