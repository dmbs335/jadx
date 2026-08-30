.class public final Lloops/TestTryProtectedCoroutineRetryResultJoin;
.super Lkotlin/coroutines/jvm/internal/SuspendLambda;

.field private I$0:I
.field private L$0:Ljava/lang/Object;
.field private label:I

.method public constructor <init>(Lkotlin/coroutines/Continuation;)V
    .registers 4

    const/4 v0, 0x2
    invoke-direct {p0, v0, p1}, Lkotlin/coroutines/jvm/internal/SuspendLambda;-><init>(ILkotlin/coroutines/Continuation;)V
    return-void
.end method

.method private static native isDone()Z
.end method

.method private static native load(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
.end method

.method private static native retry(ILjava/lang/Throwable;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
.end method

.method private static native use(Ljava/lang/Object;)V
.end method

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 9

    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v0
    iget v1, p0, Lloops/TestTryProtectedCoroutineRetryResultJoin;->label:I
    const/4 v2, 0x2
    const/4 v3, 0x1
    if-eqz v1, :initial
    if-eq v1, v3, :resume_retry
    if-ne v1, v2, :bad_state

    iget v4, p0, Lloops/TestTryProtectedCoroutineRetryResultJoin;->I$0:I
    iget-object v5, p0, Lloops/TestTryProtectedCoroutineRetryResultJoin;->L$0:Ljava/lang/Object;
    check-cast v5, Ljava/lang/Throwable;
    :try_start_resume
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    :try_end_resume
    .catchall {:try_start_resume .. :try_end_resume} :resume_catch
    move-object v6, p0
    goto :load_result

    :resume_catch
    move-exception v5
    goto :increment

    :resume_retry
    iget v4, p0, Lloops/TestTryProtectedCoroutineRetryResultJoin;->I$0:I
    iget-object v5, p0, Lloops/TestTryProtectedCoroutineRetryResultJoin;->L$0:Ljava/lang/Object;
    check-cast v5, Ljava/lang/Throwable;
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :retry_result

    :initial
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    const/4 v4, 0x0
    const/4 v5, 0x0

    :loop
    invoke-static {}, Lloops/TestTryProtectedCoroutineRetryResultJoin;->isDone()Z
    move-result v1
    if-nez v1, :done
    if-eqz v4, :call

    iput v4, p0, Lloops/TestTryProtectedCoroutineRetryResultJoin;->I$0:I
    iput-object v5, p0, Lloops/TestTryProtectedCoroutineRetryResultJoin;->L$0:Ljava/lang/Object;
    iput v3, p0, Lloops/TestTryProtectedCoroutineRetryResultJoin;->label:I
    invoke-static {v4, v5, p0}, Lloops/TestTryProtectedCoroutineRetryResultJoin;->retry(ILjava/lang/Throwable;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-eq p1, v0, :suspended

    :retry_result
    check-cast p1, Ljava/lang/Boolean;
    invoke-virtual {p1}, Ljava/lang/Boolean;->booleanValue()Z
    move-result v1
    if-eqz v1, :done

    :call
    :try_start_setup
    iput v4, p0, Lloops/TestTryProtectedCoroutineRetryResultJoin;->I$0:I
    iput-object v5, p0, Lloops/TestTryProtectedCoroutineRetryResultJoin;->L$0:Ljava/lang/Object;
    iput v2, p0, Lloops/TestTryProtectedCoroutineRetryResultJoin;->label:I
    :try_end_setup
    .catchall {:try_start_setup .. :try_end_setup} :setup_catch

    move-object v6, p0
    :try_start_call
    invoke-static {v6}, Lloops/TestTryProtectedCoroutineRetryResultJoin;->load(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-eq p1, v0, :suspended

    :load_result
    invoke-static {p1}, Lloops/TestTryProtectedCoroutineRetryResultJoin;->use(Ljava/lang/Object;)V
    :try_end_call
    .catchall {:try_start_call .. :try_end_call} :call_catch
    goto :loop

    :setup_catch
    move-exception v5
    goto :increment

    :call_catch
    move-exception v5

    :increment
    add-int/lit8 v4, v4, 0x1
    goto :loop

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
