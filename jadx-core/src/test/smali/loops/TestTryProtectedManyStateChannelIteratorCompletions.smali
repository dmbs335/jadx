.class public final Lloops/TestTryProtectedManyStateChannelIteratorCompletions;
.super Lkotlin/coroutines/jvm/internal/SuspendLambda;

.field private label:I

.method private static decodeString()Ljava/lang/String;
    .locals 1
    const-string v0, "dispatch side effect"
    return-object v0
.end method

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .locals 10

    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v0
    iget v1, p0, Lloops/TestTryProtectedManyStateChannelIteratorCompletions;->label:I
    invoke-static {}, Lloops/TestTryProtectedManyStateChannelIteratorCompletions;->decodeString()Ljava/lang/String;
    move-result-object v2
    packed-switch v1, :state_switch

    new-instance v1, Ljava/lang/IllegalStateException;
    invoke-direct {v1}, Ljava/lang/IllegalStateException;-><init>()V
    throw v1

    :state_zero
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    const/4 v6, 0x0
    const/4 v7, 0x0
    goto :call_has_next

    :state_one
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    move-object v5, p1
    goto :join_has_next

    :state_two
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    move-object v5, p1
    goto :join_send_one

    :state_three
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    move-object v5, p1
    goto :join_send_two

    :state_four
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    move-object v5, p1
    goto :join_send_three

    :try_start
    :call_has_next
    const/4 v3, 0x1
    iput v3, p0, Lloops/TestTryProtectedManyStateChannelIteratorCompletions;->label:I
    invoke-interface {v7, p0}, Lkotlinx/coroutines/channels/ChannelIterator;->hasNext(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v4
    if-eq v4, v0, :suspended
    move-object v5, v4

    :join_has_next
    check-cast v5, Ljava/lang/Boolean;
    invoke-virtual {v5}, Ljava/lang/Boolean;->booleanValue()Z
    move-result v3
    if-eqz v3, :done
    invoke-interface {v7}, Lkotlinx/coroutines/channels/ChannelIterator;->next()Ljava/lang/Object;
    move-result-object v8

    const/4 v3, 0x2
    iput v3, p0, Lloops/TestTryProtectedManyStateChannelIteratorCompletions;->label:I
    invoke-interface {v6, v8, p0}, Lkotlinx/coroutines/channels/SendChannel;->send(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v4
    if-eq v4, v0, :suspended
    move-object v5, v4

    :join_send_one
    const/4 v3, 0x3
    iput v3, p0, Lloops/TestTryProtectedManyStateChannelIteratorCompletions;->label:I
    invoke-interface {v6, v8, p0}, Lkotlinx/coroutines/channels/SendChannel;->send(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v4
    if-eq v4, v0, :suspended
    move-object v5, v4

    :join_send_two
    const/4 v3, 0x4
    iput v3, p0, Lloops/TestTryProtectedManyStateChannelIteratorCompletions;->label:I
    invoke-interface {v6, v8, p0}, Lkotlinx/coroutines/channels/SendChannel;->send(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v4
    if-eq v4, v0, :suspended
    move-object v5, v4

    :join_send_three
    goto :call_has_next

    :done
    sget-object v5, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    const/4 v9, 0x0
    invoke-static {v6, v9}, Lkotlinx/coroutines/channels/ChannelsKt;->cancelConsumed(Lkotlinx/coroutines/channels/ReceiveChannel;Ljava/lang/Throwable;)V
    return-object v5

    :suspended
    return-object v0
    :try_end
    .catchall {:try_start .. :try_end} :cleanup

    :cleanup
    move-exception v9
    invoke-static {v6, v9}, Lkotlinx/coroutines/channels/ChannelsKt;->cancelConsumed(Lkotlinx/coroutines/channels/ReceiveChannel;Ljava/lang/Throwable;)V
    throw v9

    :state_switch
    .packed-switch 0x0
        :state_zero
        :state_one
        :state_two
        :state_three
        :state_four
    .end packed-switch
.end method
