.class public final Lloops/TestCoroutineChannelIteratorConditionalActionLoop;
.super Lkotlin/coroutines/jvm/internal/SuspendLambda;

.field private channel:Lkotlinx/coroutines/channels/ReceiveChannel;
.field private L$0:Ljava/lang/Object;
.field private label:I

.method private static shouldSkip(Ljava/lang/Object;)Z
    .registers 2
    const/4 v0, 0x0
    return v0
.end method

.method private static action(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 2
    return-object p0
.end method

.method public final P(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 10

    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v0
    iget v1, p0, Lloops/TestCoroutineChannelIteratorConditionalActionLoop;->label:I
    const/4 v2, 0x2
    const/4 v3, 0x1

    if-eqz v1, :initial
    if-eq v1, v3, :resume_predicate
    if-ne v1, v2, :bad_state

    :resume_action
    iget-object v4, p0, Lloops/TestCoroutineChannelIteratorConditionalActionLoop;->L$0:Ljava/lang/Object;
    check-cast v4, Lkotlinx/coroutines/channels/ChannelIterator;
    invoke-static {p1}, Lkotlin/ResultKt;->b(Ljava/lang/Object;)V
    goto :after_action

    :resume_predicate
    iget-object v4, p0, Lloops/TestCoroutineChannelIteratorConditionalActionLoop;->L$0:Ljava/lang/Object;
    check-cast v4, Lkotlinx/coroutines/channels/ChannelIterator;
    invoke-static {p1}, Lkotlin/ResultKt;->b(Ljava/lang/Object;)V
    goto :predicate_result

    :initial
    invoke-static {p1}, Lkotlin/ResultKt;->b(Ljava/lang/Object;)V
    iget-object v4, p0, Lloops/TestCoroutineChannelIteratorConditionalActionLoop;->channel:Lkotlinx/coroutines/channels/ReceiveChannel;
    invoke-interface {v4}, Lkotlinx/coroutines/channels/ReceiveChannel;->iterator()Lkotlinx/coroutines/channels/ChannelIterator;
    move-result-object v4

    :predicate_call
    iput-object v4, p0, Lloops/TestCoroutineChannelIteratorConditionalActionLoop;->L$0:Ljava/lang/Object;
    iput v3, p0, Lloops/TestCoroutineChannelIteratorConditionalActionLoop;->label:I
    invoke-interface {v4, p0}, Lkotlinx/coroutines/channels/ChannelIterator;->a(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-ne p1, v0, :predicate_result
    goto :suspended

    :predicate_result
    check-cast p1, Ljava/lang/Boolean;
    invoke-virtual {p1}, Ljava/lang/Boolean;->booleanValue()Z
    move-result v5
    if-eqz v5, :done
    invoke-interface {v4}, Lkotlinx/coroutines/channels/ChannelIterator;->next()Ljava/lang/Object;
    move-result-object v6
    invoke-static {v6}, Lloops/TestCoroutineChannelIteratorConditionalActionLoop;->shouldSkip(Ljava/lang/Object;)Z
    move-result v5
    if-nez v5, :next_iteration

    iput-object v4, p0, Lloops/TestCoroutineChannelIteratorConditionalActionLoop;->L$0:Ljava/lang/Object;
    iput v2, p0, Lloops/TestCoroutineChannelIteratorConditionalActionLoop;->label:I
    invoke-static {v6, p0}, Lloops/TestCoroutineChannelIteratorConditionalActionLoop;->action(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-ne p1, v0, :after_action

    :suspended
    return-object v0

    :after_action
    move-object v7, v4
    move-object v4, v7

    :next_iteration
    move-object v7, v4
    move-object v4, v7
    goto :predicate_call

    :done
    sget-object p1, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object p1

    :bad_state
    new-instance p1, Ljava/lang/IllegalStateException;
    invoke-direct {p1}, Ljava/lang/IllegalStateException;-><init>()V
    throw p1
.end method
