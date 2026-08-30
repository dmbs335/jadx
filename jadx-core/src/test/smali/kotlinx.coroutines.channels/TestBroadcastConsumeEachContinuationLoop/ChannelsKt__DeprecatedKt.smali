.class public final Lkotlinx/coroutines/channels/ChannelsKt__DeprecatedKt;
.super Ljava/lang/Object;

.method public static final consumeEach(Lkotlinx/coroutines/channels/BroadcastChannel;Lkotlin/jvm/functions/Function1;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 9

    instance-of v0, p2, Lkotlinx/coroutines/channels/ChannelsKt__DeprecatedKt$consumeEach$1;
    if-eqz v0, :new_continuation
    move-object v0, p2
    check-cast v0, Lkotlinx/coroutines/channels/ChannelsKt__DeprecatedKt$consumeEach$1;
    iget v1, v0, Lkotlinx/coroutines/channels/ChannelsKt__DeprecatedKt$consumeEach$1;->label:I
    const/high16 v2, -0x80000000
    and-int v3, v1, v2
    if-eqz v3, :new_continuation
    sub-int/2addr v1, v2
    iput v1, v0, Lkotlinx/coroutines/channels/ChannelsKt__DeprecatedKt$consumeEach$1;->label:I
    goto :load_state

    :new_continuation
    new-instance v0, Lkotlinx/coroutines/channels/ChannelsKt__DeprecatedKt$consumeEach$1;
    invoke-direct {v0, p2}, Lkotlinx/coroutines/jvm/internal/ContinuationImpl;-><init>(Lkotlin/coroutines/Continuation;)V

    :load_state
    iget-object p2, v0, Lkotlinx/coroutines/channels/ChannelsKt__DeprecatedKt$consumeEach$1;->result:Ljava/lang/Object;
    invoke-static {}, Lkotlin/coroutines/intrinsics/a;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v1
    iget v2, v0, Lkotlinx/coroutines/channels/ChannelsKt__DeprecatedKt$consumeEach$1;->label:I
    const/4 v3, 0x0
    const/4 v4, 0x1
    if-eqz v2, :initial
    if-ne v2, v4, :bad_state

    iget-object p0, v0, Lkotlinx/coroutines/channels/ChannelsKt__DeprecatedKt$consumeEach$1;->L$2:Ljava/lang/Object;
    check-cast p0, Lkotlinx/coroutines/channels/ChannelIterator;
    iget-object p1, v0, Lkotlinx/coroutines/channels/ChannelsKt__DeprecatedKt$consumeEach$1;->L$1:Ljava/lang/Object;
    check-cast p1, Lkotlinx/coroutines/channels/ReceiveChannel;
    iget-object v2, v0, Lkotlinx/coroutines/channels/ChannelsKt__DeprecatedKt$consumeEach$1;->L$0:Ljava/lang/Object;
    check-cast v2, Lkotlin/jvm/functions/Function1;
    :try_start_resume
    invoke-static {p2}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    :try_end_resume
    .catchall {:try_start_resume .. :try_end_resume} :catch_resume
    move-object v5, v0
    move-object v0, p1
    move-object p1, v2

    :result_bridge
    move-object v2, v5
    goto :has_next_result

    :catch_resume
    move-exception p0
    goto :cleanup_throw

    :bad_state
    new-instance p0, Ljava/lang/IllegalStateException;
    invoke-direct {p0}, Ljava/lang/IllegalStateException;-><init>()V
    throw p0

    :initial
    invoke-static {p2}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    invoke-interface {p0}, Lkotlinx/coroutines/channels/BroadcastChannel;->openSubscription()Lkotlinx/coroutines/channels/ReceiveChannel;
    move-result-object p0
    :try_start_iterator
    invoke-interface {p0}, Lkotlinx/coroutines/channels/ReceiveChannel;->iterator()Lkotlinx/coroutines/channels/ChannelIterator;
    move-result-object p2
    :try_end_iterator
    .catchall {:try_start_iterator .. :try_end_iterator} :catch_iterator
    move-object v5, p2
    move-object p2, p0
    move-object p0, v5

    :iterator_loop
    :try_start_has_next
    iput-object p1, v0, Lkotlinx/coroutines/channels/ChannelsKt__DeprecatedKt$consumeEach$1;->L$0:Ljava/lang/Object;
    iput-object p2, v0, Lkotlinx/coroutines/channels/ChannelsKt__DeprecatedKt$consumeEach$1;->L$1:Ljava/lang/Object;
    iput-object p0, v0, Lkotlinx/coroutines/channels/ChannelsKt__DeprecatedKt$consumeEach$1;->L$2:Ljava/lang/Object;
    iput v4, v0, Lkotlinx/coroutines/channels/ChannelsKt__DeprecatedKt$consumeEach$1;->label:I
    invoke-interface {p0, v0}, Lkotlinx/coroutines/channels/ChannelIterator;->hasNext(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v2
    :try_end_has_next
    .catchall {:try_start_has_next .. :try_end_has_next} :catch_has_next
    if-ne v2, v1, :direct_result
    return-object v1

    :direct_result
    move-object v5, v0
    move-object v0, p2
    move-object p2, v2
    goto :result_bridge

    :has_next_result
    :try_start_result
    check-cast p2, Ljava/lang/Boolean;
    invoke-virtual {p2}, Ljava/lang/Boolean;->booleanValue()Z
    move-result p2
    if-eqz p2, :done
    invoke-interface {p0}, Lkotlinx/coroutines/channels/ChannelIterator;->next()Ljava/lang/Object;
    move-result-object p2
    invoke-interface {p1, p2}, Lkotlin/jvm/functions/Function1;->invoke(Ljava/lang/Object;)Ljava/lang/Object;
    move-object p2, v0
    move-object v0, v2
    goto :iterator_loop

    :catch_result
    move-exception p0
    move-object p1, v0
    goto :cleanup_throw

    :done
    sget-object p0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    :try_end_result
    .catchall {:try_start_result .. :try_end_result} :catch_result
    invoke-static {v4}, Lkotlin/jvm/internal/InlineMarker;->finallyStart(I)V
    invoke-static {v0, v3, v4, v3}, Lkotlinx/coroutines/channels/ReceiveChannel$DefaultImpls;->cancel$default(Lkotlinx/coroutines/channels/ReceiveChannel;Ljava/util/concurrent/CancellationException;ILjava/lang/Object;)V
    invoke-static {v4}, Lkotlin/jvm/internal/InlineMarker;->finallyEnd(I)V
    return-object p0

    :catch_has_next
    move-exception p0
    move-object p1, p2
    goto :cleanup_throw

    :catch_iterator
    move-exception p1
    move-object v5, p1
    move-object p1, p0
    move-object p0, v5

    :cleanup_throw
    invoke-static {v4}, Lkotlin/jvm/internal/InlineMarker;->finallyStart(I)V
    invoke-static {p1, v3, v4, v3}, Lkotlinx/coroutines/channels/ReceiveChannel$DefaultImpls;->cancel$default(Lkotlinx/coroutines/channels/ReceiveChannel;Ljava/util/concurrent/CancellationException;ILjava/lang/Object;)V
    invoke-static {v4}, Lkotlin/jvm/internal/InlineMarker;->finallyEnd(I)V
    throw p0
.end method
