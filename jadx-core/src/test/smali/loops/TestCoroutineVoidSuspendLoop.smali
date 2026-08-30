.class public Lloops/TestCoroutineVoidSuspendLoop;
.super Ljava/lang/Object;

.field private L$0:Ljava/lang/Object;
.field private L$1:Ljava/lang/Object;
.field private label:I

.method private static getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    .locals 1
    const/4 v0, 0x0
    return-object v0
.end method

.method private static receive(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 1
    const-string v0, "packet"
    return-object v0
.end method

.method private static send(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 1
    const-string v0, "sent"
    return-object v0
.end method

.method private static throwOnFailure(Ljava/lang/Object;)V
    .locals 0
    return-void
.end method

.method public invokeSuspend(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 4

    invoke-static {}, Lloops/TestCoroutineVoidSuspendLoop;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v0
    iget v1, p0, Lloops/TestCoroutineVoidSuspendLoop;->label:I
    const/4 v2, 0x2
    const/4 v3, 0x1
    if-eqz v1, :initial
    if-eq v1, v3, :resume_receive
    if-ne v1, v2, :bad_state

    :resume_send
    :try_start_resume_send
    invoke-static {p1}, Lloops/TestCoroutineVoidSuspendLoop;->throwOnFailure(Ljava/lang/Object;)V
    :try_end_resume_send
    .catch Ljava/lang/Exception; {:try_start_resume_send .. :try_end_resume_send} :closed
    goto :receive_loop

    :resume_receive
    :try_start_resume_receive
    invoke-static {p1}, Lloops/TestCoroutineVoidSuspendLoop;->throwOnFailure(Ljava/lang/Object;)V
    :try_end_resume_receive
    .catch Ljava/lang/Exception; {:try_start_resume_receive .. :try_end_resume_receive} :closed
    goto :send_result

    :bad_state
    new-instance v1, Ljava/lang/IllegalStateException;
    invoke-direct {v1}, Ljava/lang/IllegalStateException;-><init>()V
    throw v1

    :initial
    invoke-static {p1}, Lloops/TestCoroutineVoidSuspendLoop;->throwOnFailure(Ljava/lang/Object;)V

    :receive_loop
    :try_start_loop
    iput v3, p0, Lloops/TestCoroutineVoidSuspendLoop;->label:I
    invoke-static {p2}, Lloops/TestCoroutineVoidSuspendLoop;->receive(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-eq p1, v0, :suspended

    :send_result
    iput v2, p0, Lloops/TestCoroutineVoidSuspendLoop;->label:I
    invoke-static {p1, p2}, Lloops/TestCoroutineVoidSuspendLoop;->send(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    :try_end_loop
    .catch Ljava/lang/Exception; {:try_start_loop .. :try_end_loop} :closed
    if-ne p1, v0, :receive_loop

    :suspended
    return-object v0

    :closed
    const-string p1, "closed"
    return-object p1
.end method
