.class public final Lkotlinx/coroutines/channels/ChannelsKt__DeprecatedKt;
.super Ljava/lang/Object;

.method public static final elementAtOrNull(Lkotlinx/coroutines/channels/ReceiveChannel;ILkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 10

    instance-of v0, p2, Lkotlinx/coroutines/channels/ChannelsKt__DeprecatedKt$elementAtOrNull$1;
    if-eqz v0, :new_state
    move-object v0, p2
    check-cast v0, Lkotlinx/coroutines/channels/ChannelsKt__DeprecatedKt$elementAtOrNull$1;
    iget v2, v0, Lkotlinx/coroutines/channels/ChannelsKt__DeprecatedKt$elementAtOrNull$1;->label:I
    const/high16 v3, -0x80000000
    and-int v4, v2, v3
    if-eqz v4, :new_state
    sub-int/2addr v2, v3
    iput v2, v0, Lkotlinx/coroutines/channels/ChannelsKt__DeprecatedKt$elementAtOrNull$1;->label:I
    goto :state_ready

    :new_state
    new-instance v0, Lkotlinx/coroutines/channels/ChannelsKt__DeprecatedKt$elementAtOrNull$1;
    invoke-direct {v0, p2}, Lkotlinx/coroutines/channels/ChannelsKt__DeprecatedKt$elementAtOrNull$1;-><init>(Lkotlin/coroutines/Continuation;)V

    :state_ready
    iget-object v4, v0, Lkotlinx/coroutines/channels/ChannelsKt__DeprecatedKt$elementAtOrNull$1;->result:Ljava/lang/Object;
    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v1
    iget v2, v0, Lkotlinx/coroutines/channels/ChannelsKt__DeprecatedKt$elementAtOrNull$1;->label:I
    const/4 v3, 0x1
    if-eqz v2, :initial
    if-ne v2, v3, :bad_state
    iget p1, v0, Lkotlinx/coroutines/channels/ChannelsKt__DeprecatedKt$elementAtOrNull$1;->I$0:I
    iget v3, v0, Lkotlinx/coroutines/channels/ChannelsKt__DeprecatedKt$elementAtOrNull$1;->I$1:I
    iget-object p0, v0, Lkotlinx/coroutines/channels/ChannelsKt__DeprecatedKt$elementAtOrNull$1;->L$0:Ljava/lang/Object;
    check-cast p0, Lkotlinx/coroutines/channels/ReceiveChannel;
    move-object v5, p0
    :try_start_resume
    invoke-static {v4}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    :try_end_resume
    .catchall {:try_start_resume .. :try_end_resume} :resume_failure
    move-object v6, v4
    move-object v4, v6
    move-object v6, v4
    move-object v4, v6
    goto :projection

    :bad_state
    new-instance p0, Ljava/lang/IllegalStateException;
    const-string p1, "call to 'resume' before 'invoke' with coroutine"
    invoke-direct {p0, p1}, Ljava/lang/IllegalStateException;-><init>(Ljava/lang/String;)V
    throw p0

    :initial
    invoke-static {v4}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    const/4 v3, 0x0

    :try_start_loop
    invoke-static {}, Lkotlinx/coroutines/channels/ChannelsKt__DeprecatedKt;->iterator()V

    :loop
    iput-object p0, v0, Lkotlinx/coroutines/channels/ChannelsKt__DeprecatedKt$elementAtOrNull$1;->L$0:Ljava/lang/Object;
    iput p1, v0, Lkotlinx/coroutines/channels/ChannelsKt__DeprecatedKt$elementAtOrNull$1;->I$0:I
    iput v3, v0, Lkotlinx/coroutines/channels/ChannelsKt__DeprecatedKt$elementAtOrNull$1;->I$1:I
    const/4 v2, 0x1
    iput v2, v0, Lkotlinx/coroutines/channels/ChannelsKt__DeprecatedKt$elementAtOrNull$1;->label:I
    invoke-static {v0}, Lkotlinx/coroutines/channels/ChannelsKt__DeprecatedKt;->hasNext(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v4
    if-ne v4, v1, :direct_result
    return-object v1

    :direct_result
    move-object v5, v4
    move-object v4, v5

    :projection
    check-cast v4, Ljava/lang/Boolean;
    invoke-virtual {v4}, Ljava/lang/Boolean;->booleanValue()Z
    move-result v2
    if-eqz v2, :exhausted
    invoke-static {}, Lkotlinx/coroutines/channels/ChannelsKt__DeprecatedKt;->next()Ljava/lang/Object;
    move-result-object v6
    :try_end_projection
    .catchall {:try_start_loop .. :try_end_projection} :failure

    move v2, v3
    add-int/lit8 v3, v3, 0x1
    if-ne p1, v2, :loop
    const/4 v2, 0x0
    invoke-static {p0, v2}, Lkotlinx/coroutines/channels/ChannelsKt;->cancelConsumed(Lkotlinx/coroutines/channels/ReceiveChannel;Ljava/lang/Throwable;)V
    return-object v6

    :exhausted
    const/4 v2, 0x0
    invoke-static {p0, v2}, Lkotlinx/coroutines/channels/ChannelsKt;->cancelConsumed(Lkotlinx/coroutines/channels/ReceiveChannel;Ljava/lang/Throwable;)V
    return-object v2

    :failure
    move-exception v6
    goto :failure_moves

    :failure_moves
    move-object v5, p0
    move-object v4, v6
    goto :cleanup

    :resume_failure
    move-exception v4

    :cleanup
    :try_start_cleanup
    throw v4
    :try_end_cleanup
    .catchall {:try_start_cleanup .. :try_end_cleanup} :cleanup_failure

    :cleanup_failure
    move-exception v6
    invoke-static {v5, v4}, Lkotlinx/coroutines/channels/ChannelsKt;->cancelConsumed(Lkotlinx/coroutines/channels/ReceiveChannel;Ljava/lang/Throwable;)V
    throw v6
.end method

.method private static hasNext(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 2
    sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
    return-object v0
.end method

.method private static next()Ljava/lang/Object;
    .registers 1
    const-string v0, "item"
    return-object v0
.end method

.method private static iterator()V
    .registers 0
    return-void
.end method
