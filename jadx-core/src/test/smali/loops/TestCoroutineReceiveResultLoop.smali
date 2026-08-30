.class public Lloops/TestCoroutineReceiveResultLoop;
.super Ljava/lang/Object;

.field private label:I

.method private static getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    .locals 1
    const/4 v0, 0x0
    return-object v0
.end method

.method private static receive(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 1
    const-string v0, "ok"
    return-object v0
.end method

.method private static send(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 1
    const/4 v0, 0x0
    return-object v0
.end method

.method private static throwOnFailure(Ljava/lang/Object;)V
    .locals 0
    return-void
.end method

.method public run(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 4

    invoke-static {}, Lloops/TestCoroutineReceiveResultLoop;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v0
    iget v1, p0, Lloops/TestCoroutineReceiveResultLoop;->label:I
    const/4 v2, 0x2
    const/4 v3, 0x1
    if-eqz v1, :initial
    if-eq v1, v3, :resume_send
    if-ne v1, v2, :bad_state

    :resume_receive
    invoke-static {p1}, Lloops/TestCoroutineReceiveResultLoop;->throwOnFailure(Ljava/lang/Object;)V
    goto :consume

    :resume_send
    invoke-static {p1}, Lloops/TestCoroutineReceiveResultLoop;->throwOnFailure(Ljava/lang/Object;)V
    goto :receive_loop

    :bad_state
    new-instance v1, Ljava/lang/IllegalStateException;
    invoke-direct {v1}, Ljava/lang/IllegalStateException;-><init>()V
    throw v1

    :initial
    invoke-static {p1}, Lloops/TestCoroutineReceiveResultLoop;->throwOnFailure(Ljava/lang/Object;)V
    iput v3, p0, Lloops/TestCoroutineReceiveResultLoop;->label:I
    invoke-static {p0}, Lloops/TestCoroutineReceiveResultLoop;->send(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-eq p1, v0, :suspended

    :receive_loop
    iput v2, p0, Lloops/TestCoroutineReceiveResultLoop;->label:I
    invoke-static {p0}, Lloops/TestCoroutineReceiveResultLoop;->receive(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-ne p1, v0, :consume
    goto :suspended

    :consume
    check-cast p1, Ljava/lang/String;
    const-string v1, "ok"
    invoke-virtual {p1, v1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
    move-result v1
    if-eqz v1, :receive_loop
    return-object p1

    :suspended
    return-object v0
.end method
