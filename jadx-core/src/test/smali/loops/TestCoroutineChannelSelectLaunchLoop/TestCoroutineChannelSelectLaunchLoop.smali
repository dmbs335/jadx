.class public final Lloops/TestCoroutineChannelSelectLaunchLoop;
.super Ljava/lang/Object;

.method public static final selectLoop(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 9

    instance-of v0, p0, Lloops/TestCoroutineChannelSelectLaunchLoop$selectLoop$1;
    if-eqz v0, :new_state

    move-object v0, p0
    check-cast v0, Lloops/TestCoroutineChannelSelectLaunchLoop$selectLoop$1;
    iget v2, v0, Lloops/TestCoroutineChannelSelectLaunchLoop$selectLoop$1;->label:I
    const/high16 v3, -0x80000000
    and-int v7, v2, v3
    if-eqz v7, :new_state

    sub-int/2addr v2, v3
    iput v2, v0, Lloops/TestCoroutineChannelSelectLaunchLoop$selectLoop$1;->label:I
    goto :state_ready

    :new_state
    new-instance v0, Lloops/TestCoroutineChannelSelectLaunchLoop$selectLoop$1;
    invoke-direct {v0, p0}, Lloops/TestCoroutineChannelSelectLaunchLoop$selectLoop$1;-><init>(Lkotlin/coroutines/Continuation;)V

    :state_ready
    iget-object p0, v0, Lloops/TestCoroutineChannelSelectLaunchLoop$selectLoop$1;->result:Ljava/lang/Object;
    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v1
    iget v2, v0, Lloops/TestCoroutineChannelSelectLaunchLoop$selectLoop$1;->label:I
    const/4 v3, 0x1

    if-eqz v2, :initial
    if-ne v2, v3, :bad_state

    iget-object v4, v0, Lloops/TestCoroutineChannelSelectLaunchLoop$selectLoop$1;->L$0:Ljava/lang/Object;
    invoke-static {p0}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :completion

    :bad_state
    new-instance p0, Ljava/lang/IllegalStateException;
    const-string v0, "call to 'resume' before 'invoke' with coroutine"
    invoke-direct {p0, v0}, Ljava/lang/IllegalStateException;-><init>(Ljava/lang/String;)V
    throw p0

    :initial
    invoke-static {p0}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    sget-object v4, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;

    :loop
    iput-object v4, v0, Lloops/TestCoroutineChannelSelectLaunchLoop$selectLoop$1;->L$0:Ljava/lang/Object;
    iput v3, v0, Lloops/TestCoroutineChannelSelectLaunchLoop$selectLoop$1;->label:I
    invoke-static {v4, v0}, Lloops/TestCoroutineChannelSelectLaunchLoop;->hasNext(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p0
    if-ne p0, v1, :completion
    return-object v1

    :completion
    check-cast p0, Ljava/lang/Boolean;
    invoke-virtual {p0}, Ljava/lang/Boolean;->booleanValue()Z
    move-result v2
    if-eqz v2, :done

    invoke-static {v4}, Lloops/TestCoroutineChannelSelectLaunchLoop;->next(Ljava/lang/Object;)Ljava/lang/Boolean;
    move-result-object v5
    invoke-virtual {v5}, Ljava/lang/Boolean;->booleanValue()Z
    move-result v2
    invoke-static {v4}, Lloops/TestCoroutineChannelSelectLaunchLoop;->tryReceive(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v6
    invoke-static {v6}, Lloops/TestCoroutineChannelSelectLaunchLoop;->getOrNull(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v6
    check-cast v6, Ljava/lang/Boolean;
    goto :decision

    :decision
    if-eqz v6, :launch
    invoke-virtual {v6}, Ljava/lang/Boolean;->booleanValue()Z
    move-result v2

    :launch
    invoke-static {v2}, Lloops/TestCoroutineChannelSelectLaunchLoop;->launch(Z)V
    goto :loop

    :done
    sget-object p0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object p0
.end method

.method private static getOrNull(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 1
    return-object p0
.end method

.method private static hasNext(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 3
    sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
    return-object v0
.end method

.method private static launch(Z)V
    .registers 1
    return-void
.end method

.method private static next(Ljava/lang/Object;)Ljava/lang/Boolean;
    .registers 2
    sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
    return-object v0
.end method

.method private static tryReceive(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 1
    return-object p0
.end method
