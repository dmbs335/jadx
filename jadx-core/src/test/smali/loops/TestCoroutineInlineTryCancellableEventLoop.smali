.class public final Lloops/TestCoroutineInlineTryCancellableEventLoop;
.super Lkotlin/coroutines/jvm/internal/SuspendLambda;

.field private L$0:Ljava/lang/Object;
.field private label:I

.method public constructor <init>(Lkotlin/coroutines/Continuation;)V
    .registers 3
    const/4 v0, 0x1
    invoke-direct {p0, v0, p1}, Lkotlin/coroutines/jvm/internal/SuspendLambda;-><init>(ILkotlin/coroutines/Continuation;)V
    return-void
.end method

.method private static isActive(Ljava/lang/Object;)Z
    .registers 2
    const/4 v0, 0x1
    return v0
.end method

.method private static process(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 2
    sget-object p0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object p0
.end method

.method private static receive(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 1
    sget-object p0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object p0
.end method

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 8

    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v0
    iget v1, p0, Lloops/TestCoroutineInlineTryCancellableEventLoop;->label:I

    if-eqz v1, :initial
    const/4 v2, 0x1
    if-eq v1, v2, :resume_receive
    const/4 v2, 0x2
    if-eq v1, v2, :resume_process
    new-instance p1, Ljava/lang/IllegalStateException;
    const-string v0, "resume before invoke"
    invoke-direct {p1, v0}, Ljava/lang/IllegalStateException;-><init>(Ljava/lang/String;)V
    throw p1

    :initial
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    iget-object v3, p0, Lloops/TestCoroutineInlineTryCancellableEventLoop;->L$0:Ljava/lang/Object;
    goto :loop

    :resume_receive
    iget-object v3, p0, Lloops/TestCoroutineInlineTryCancellableEventLoop;->L$0:Ljava/lang/Object;
    :try_start_receive
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    :try_end_receive
    .catchall {:try_start_receive .. :try_end_receive} :catch_all
    goto :after_receive

    :resume_process
    iget-object v3, p0, Lloops/TestCoroutineInlineTryCancellableEventLoop;->L$0:Ljava/lang/Object;
    :try_start_process_resume
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    :try_end_process_resume
    .catchall {:try_start_process_resume .. :try_end_process_resume} :catch_all
    move-object p1, v3
    goto :loop

    :loop
    :try_start_loop
    invoke-static {v3}, Lloops/TestCoroutineInlineTryCancellableEventLoop;->isActive(Ljava/lang/Object;)Z
    move-result v1
    if-eqz v1, :done

    iput-object v3, p0, Lloops/TestCoroutineInlineTryCancellableEventLoop;->L$0:Ljava/lang/Object;
    const/4 v1, 0x1
    iput v1, p0, Lloops/TestCoroutineInlineTryCancellableEventLoop;->label:I
    invoke-static {p0}, Lloops/TestCoroutineInlineTryCancellableEventLoop;->receive(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-eq p1, v0, :suspended

    :after_receive
    iput-object v3, p0, Lloops/TestCoroutineInlineTryCancellableEventLoop;->L$0:Ljava/lang/Object;
    const/4 v1, 0x2
    iput v1, p0, Lloops/TestCoroutineInlineTryCancellableEventLoop;->label:I
    :try_end_loop
    .catchall {:try_start_loop .. :try_end_loop} :catch_all

    move-object v4, p0
    :try_start_process_call
    invoke-static {v3, v4}, Lloops/TestCoroutineInlineTryCancellableEventLoop;->process(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    :try_end_process_call
    .catchall {:try_start_process_call .. :try_end_process_call} :catch_all
    if-eq p1, v0, :suspended
    move-object p1, v3
    goto :loop

    :done
    sget-object p1, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object p1

    :suspended
    return-object v0

    :catch_all
    move-exception p1
    throw p1
.end method
