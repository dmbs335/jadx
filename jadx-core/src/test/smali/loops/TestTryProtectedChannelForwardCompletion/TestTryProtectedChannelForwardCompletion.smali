.class public final Lloops/TestTryProtectedChannelForwardCompletion;
.super Ljava/lang/Object;

.method public static toChannel(Lkotlinx/coroutines/channels/ReceiveChannel;Lkotlinx/coroutines/channels/SendChannel;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 9

    instance-of v0, p2, Lloops/TestTryProtectedChannelForwardCompletion$toChannel$1;
    if-eqz v0, :new_state
    move-object v0, p2
    check-cast v0, Lloops/TestTryProtectedChannelForwardCompletion$toChannel$1;
    iget v1, v0, Lloops/TestTryProtectedChannelForwardCompletion$toChannel$1;->label:I
    const/high16 v2, -0x80000000
    and-int v3, v1, v2
    if-eqz v3, :new_state
    sub-int/2addr v1, v2
    iput v1, v0, Lloops/TestTryProtectedChannelForwardCompletion$toChannel$1;->label:I
    goto :state_ready

    :new_state
    new-instance v0, Lloops/TestTryProtectedChannelForwardCompletion$toChannel$1;
    invoke-direct {v0, p2}, Lloops/TestTryProtectedChannelForwardCompletion$toChannel$1;-><init>(Lkotlin/coroutines/Continuation;)V

    :state_ready
    iget-object p2, v0, Lloops/TestTryProtectedChannelForwardCompletion$toChannel$1;->result:Ljava/lang/Object;
    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v1
    iget v2, v0, Lloops/TestTryProtectedChannelForwardCompletion$toChannel$1;->label:I
    const/4 v3, 0x2
    const/4 v4, 0x1
    if-eqz v2, :initial
    if-eq v2, v4, :resume_has_next
    if-ne v2, v3, :bad_state

    iget-object p0, v0, Lloops/TestTryProtectedChannelForwardCompletion$toChannel$1;->L$2:Ljava/lang/Object;
    check-cast p0, Lkotlinx/coroutines/channels/ChannelIterator;
    iget-object p1, v0, Lloops/TestTryProtectedChannelForwardCompletion$toChannel$1;->L$1:Ljava/lang/Object;
    check-cast p1, Lkotlinx/coroutines/channels/ReceiveChannel;
    iget-object v2, v0, Lloops/TestTryProtectedChannelForwardCompletion$toChannel$1;->L$0:Ljava/lang/Object;
    check-cast v2, Lkotlinx/coroutines/channels/SendChannel;
    :try_start_resume_send
    invoke-static {p2}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    :try_end_resume_send
    .catchall {:try_start_resume_send .. :try_end_resume_send} :catch_body

    :send_completed
    move-object p2, p0
    move-object p0, p1
    move-object p1, v2
    goto :loop

    :bad_state
    new-instance p0, Ljava/lang/IllegalStateException;
    invoke-direct {p0}, Ljava/lang/IllegalStateException;-><init>()V
    throw p0

    :resume_has_next
    iget-object p0, v0, Lloops/TestTryProtectedChannelForwardCompletion$toChannel$1;->L$2:Ljava/lang/Object;
    check-cast p0, Lkotlinx/coroutines/channels/ChannelIterator;
    iget-object p1, v0, Lloops/TestTryProtectedChannelForwardCompletion$toChannel$1;->L$1:Ljava/lang/Object;
    check-cast p1, Lkotlinx/coroutines/channels/ReceiveChannel;
    iget-object v2, v0, Lloops/TestTryProtectedChannelForwardCompletion$toChannel$1;->L$0:Ljava/lang/Object;
    check-cast v2, Lkotlinx/coroutines/channels/SendChannel;
    :try_start_resume_has_next
    invoke-static {p2}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    :try_end_resume_has_next
    .catchall {:try_start_resume_has_next .. :try_end_resume_has_next} :catch_body
    goto :has_next_result

    :initial
    invoke-static {p2}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    :try_start_loop
    invoke-interface {p0}, Lkotlinx/coroutines/channels/ReceiveChannel;->iterator()Lkotlinx/coroutines/channels/ChannelIterator;
    move-result-object p2

    :loop
    iput-object p1, v0, Lloops/TestTryProtectedChannelForwardCompletion$toChannel$1;->L$0:Ljava/lang/Object;
    iput-object p0, v0, Lloops/TestTryProtectedChannelForwardCompletion$toChannel$1;->L$1:Ljava/lang/Object;
    iput-object p2, v0, Lloops/TestTryProtectedChannelForwardCompletion$toChannel$1;->L$2:Ljava/lang/Object;
    iput v4, v0, Lloops/TestTryProtectedChannelForwardCompletion$toChannel$1;->label:I
    invoke-interface {p2, v0}, Lkotlinx/coroutines/channels/ChannelIterator;->hasNext(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v2
    :try_end_loop
    .catchall {:try_start_loop .. :try_end_loop} :catch_loop
    if-ne v2, v1, :direct_has_next
    goto :suspended

    :direct_has_next
    move-object v5, p1
    move-object p1, p0
    move-object p0, p2
    move-object p2, v2
    move-object v2, v5

    :has_next_result
    :try_start_body
    check-cast p2, Ljava/lang/Boolean;
    invoke-virtual {p2}, Ljava/lang/Boolean;->booleanValue()Z
    move-result p2
    if-eqz p2, :done
    invoke-interface {p0}, Lkotlinx/coroutines/channels/ChannelIterator;->next()Ljava/lang/Object;
    move-result-object p2
    iput-object v2, v0, Lloops/TestTryProtectedChannelForwardCompletion$toChannel$1;->L$0:Ljava/lang/Object;
    iput-object p1, v0, Lloops/TestTryProtectedChannelForwardCompletion$toChannel$1;->L$1:Ljava/lang/Object;
    iput-object p0, v0, Lloops/TestTryProtectedChannelForwardCompletion$toChannel$1;->L$2:Ljava/lang/Object;
    iput v3, v0, Lloops/TestTryProtectedChannelForwardCompletion$toChannel$1;->label:I
    invoke-interface {v2, p2, v0}, Lkotlinx/coroutines/channels/SendChannel;->send(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p2
    if-ne p2, v1, :send_completed

    :suspended
    return-object v1

    :done
    sget-object p0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    :try_end_body
    .catchall {:try_start_body .. :try_end_body} :catch_body
    const/4 p0, 0x0
    invoke-static {p1, p0}, Lkotlinx/coroutines/channels/ChannelsKt;->cancelConsumed(Lkotlinx/coroutines/channels/ReceiveChannel;Ljava/lang/Throwable;)V
    return-object v2

    :catch_loop
    move-exception p1
    move-object v5, p1
    move-object p1, p0
    move-object p0, v5
    goto :cleanup

    :catch_body
    move-exception p0

    :cleanup
    :try_start_cleanup
    throw p0
    :try_end_cleanup
    .catchall {:try_start_cleanup .. :try_end_cleanup} :catch_cleanup

    :catch_cleanup
    move-exception p2
    invoke-static {p1, p0}, Lkotlinx/coroutines/channels/ChannelsKt;->cancelConsumed(Lkotlinx/coroutines/channels/ReceiveChannel;Ljava/lang/Throwable;)V
    throw p2
.end method
