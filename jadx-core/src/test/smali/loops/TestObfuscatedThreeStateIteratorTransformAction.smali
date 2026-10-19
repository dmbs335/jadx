.class public final Lloops/TestObfuscatedThreeStateIteratorTransformAction;
.super Lkotlin/coroutines/jvm/internal/SuspendLambda;

.field private transform:Lkotlin/jvm/functions/Function2;
.field private channel:Lkotlinx/coroutines/channels/ReceiveChannel;
.field private L$0:Ljava/lang/Object;
.field private L$1:Ljava/lang/Object;
.field private L$2:Ljava/lang/Object;
.field private L$3:Ljava/lang/Object;
.field private L$4:Ljava/lang/Object;
.field private label:I

.method public final P(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 13

    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v0
    iget v1, p0, Lloops/TestObfuscatedThreeStateIteratorTransformAction;->label:I
    const/4 v2, 0x3
    const/4 v3, 0x2
    const/4 v4, 0x1
    if-eqz v1, :initial
    if-eq v1, v4, :resume_has_next
    if-eq v1, v3, :resume_transform
    if-ne v1, v2, :bad_state

    :resume_send
    iget-object v8, p0, Lloops/TestObfuscatedThreeStateIteratorTransformAction;->L$3:Ljava/lang/Object;
    check-cast v8, Lkotlinx/coroutines/channels/ChannelIterator;
    iget-object v7, p0, Lloops/TestObfuscatedThreeStateIteratorTransformAction;->L$2:Ljava/lang/Object;
    check-cast v7, Lkotlinx/coroutines/channels/ReceiveChannel;
    iget-object v6, p0, Lloops/TestObfuscatedThreeStateIteratorTransformAction;->L$1:Ljava/lang/Object;
    check-cast v6, Lkotlin/jvm/functions/Function2;
    iget-object v5, p0, Lloops/TestObfuscatedThreeStateIteratorTransformAction;->L$0:Ljava/lang/Object;
    check-cast v5, Lkotlinx/coroutines/channels/ProducerScope;
    invoke-static {p1}, Lkotlin/ResultKt;->b(Ljava/lang/Object;)V
    goto :iterator_loop

    :resume_transform
    iget-object v5, p0, Lloops/TestObfuscatedThreeStateIteratorTransformAction;->L$4:Ljava/lang/Object;
    check-cast v5, Lkotlinx/coroutines/channels/ProducerScope;
    iget-object v8, p0, Lloops/TestObfuscatedThreeStateIteratorTransformAction;->L$3:Ljava/lang/Object;
    check-cast v8, Lkotlinx/coroutines/channels/ChannelIterator;
    iget-object v7, p0, Lloops/TestObfuscatedThreeStateIteratorTransformAction;->L$2:Ljava/lang/Object;
    check-cast v7, Lkotlinx/coroutines/channels/ReceiveChannel;
    iget-object v6, p0, Lloops/TestObfuscatedThreeStateIteratorTransformAction;->L$1:Ljava/lang/Object;
    check-cast v6, Lkotlin/jvm/functions/Function2;
    invoke-static {p1}, Lkotlin/ResultKt;->b(Ljava/lang/Object;)V
    goto :send_value

    :resume_has_next
    iget-object v8, p0, Lloops/TestObfuscatedThreeStateIteratorTransformAction;->L$3:Ljava/lang/Object;
    check-cast v8, Lkotlinx/coroutines/channels/ChannelIterator;
    iget-object v7, p0, Lloops/TestObfuscatedThreeStateIteratorTransformAction;->L$2:Ljava/lang/Object;
    check-cast v7, Lkotlinx/coroutines/channels/ReceiveChannel;
    iget-object v6, p0, Lloops/TestObfuscatedThreeStateIteratorTransformAction;->L$1:Ljava/lang/Object;
    check-cast v6, Lkotlin/jvm/functions/Function2;
    iget-object v5, p0, Lloops/TestObfuscatedThreeStateIteratorTransformAction;->L$0:Ljava/lang/Object;
    check-cast v5, Lkotlinx/coroutines/channels/ProducerScope;
    invoke-static {p1}, Lkotlin/ResultKt;->b(Ljava/lang/Object;)V
    goto :has_next_result

    :bad_state
    new-instance p1, Ljava/lang/IllegalStateException;
    invoke-direct {p1}, Ljava/lang/IllegalStateException;-><init>()V
    throw p1

    :initial
    invoke-static {p1}, Lkotlin/ResultKt;->b(Ljava/lang/Object;)V
    iget-object v5, p0, Lloops/TestObfuscatedThreeStateIteratorTransformAction;->L$0:Ljava/lang/Object;
    check-cast v5, Lkotlinx/coroutines/channels/ProducerScope;
    iget-object v7, p0, Lloops/TestObfuscatedThreeStateIteratorTransformAction;->channel:Lkotlinx/coroutines/channels/ReceiveChannel;
    iget-object v6, p0, Lloops/TestObfuscatedThreeStateIteratorTransformAction;->transform:Lkotlin/jvm/functions/Function2;
    invoke-interface {v7}, Lkotlinx/coroutines/channels/ReceiveChannel;->iterator()Lkotlinx/coroutines/channels/ChannelIterator;
    move-result-object v8

    :try_start
    :iterator_loop
    iput-object v5, p0, Lloops/TestObfuscatedThreeStateIteratorTransformAction;->L$0:Ljava/lang/Object;
    iput-object v6, p0, Lloops/TestObfuscatedThreeStateIteratorTransformAction;->L$1:Ljava/lang/Object;
    iput-object v7, p0, Lloops/TestObfuscatedThreeStateIteratorTransformAction;->L$2:Ljava/lang/Object;
    iput-object v8, p0, Lloops/TestObfuscatedThreeStateIteratorTransformAction;->L$3:Ljava/lang/Object;
    iput v4, p0, Lloops/TestObfuscatedThreeStateIteratorTransformAction;->label:I
    invoke-interface {v8, p0}, Lkotlinx/coroutines/channels/ChannelIterator;->a(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-ne p1, v0, :has_next_result
    goto :suspended

    :has_next_result
    check-cast p1, Ljava/lang/Boolean;
    invoke-virtual {p1}, Ljava/lang/Boolean;->booleanValue()Z
    move-result p1
    if-eqz p1, :done
    invoke-interface {v8}, Lkotlinx/coroutines/channels/ChannelIterator;->next()Ljava/lang/Object;
    move-result-object v9
    iput-object v5, p0, Lloops/TestObfuscatedThreeStateIteratorTransformAction;->L$0:Ljava/lang/Object;
    iput-object v6, p0, Lloops/TestObfuscatedThreeStateIteratorTransformAction;->L$1:Ljava/lang/Object;
    iput-object v7, p0, Lloops/TestObfuscatedThreeStateIteratorTransformAction;->L$2:Ljava/lang/Object;
    iput-object v8, p0, Lloops/TestObfuscatedThreeStateIteratorTransformAction;->L$3:Ljava/lang/Object;
    iput-object v5, p0, Lloops/TestObfuscatedThreeStateIteratorTransformAction;->L$4:Ljava/lang/Object;
    iput v3, p0, Lloops/TestObfuscatedThreeStateIteratorTransformAction;->label:I
    invoke-interface {v6, v9, p0}, Lkotlin/jvm/functions/Function2;->M(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object p1
    if-ne p1, v0, :transform_result
    goto :suspended

    :transform_result
    move-object v9, p1
    move-object p1, v9
    goto :send_value

    :send_value
    iput-object v5, p0, Lloops/TestObfuscatedThreeStateIteratorTransformAction;->L$0:Ljava/lang/Object;
    iput-object v6, p0, Lloops/TestObfuscatedThreeStateIteratorTransformAction;->L$1:Ljava/lang/Object;
    iput-object v7, p0, Lloops/TestObfuscatedThreeStateIteratorTransformAction;->L$2:Ljava/lang/Object;
    iput-object v8, p0, Lloops/TestObfuscatedThreeStateIteratorTransformAction;->L$3:Ljava/lang/Object;
    const/4 v9, 0x0
    iput-object v9, p0, Lloops/TestObfuscatedThreeStateIteratorTransformAction;->L$4:Ljava/lang/Object;
    iput v2, p0, Lloops/TestObfuscatedThreeStateIteratorTransformAction;->label:I
    invoke-interface {v5, p1, p0}, Lkotlinx/coroutines/channels/SendChannel;->F(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-ne p1, v0, :send_result

    :suspended
    return-object v0

    :send_result
    move-object v9, v8
    move-object v8, v9
    goto :iterator_loop

    :done
    sget-object p1, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    :try_end
    return-object p1

    :catchall
    move-exception v10
    invoke-static {v7, v10}, Lkotlinx/coroutines/channels/ChannelsKt;->a(Lkotlinx/coroutines/channels/ReceiveChannel;Ljava/lang/Throwable;)V
    throw v10

    .catchall {:try_start .. :try_end} :catchall
.end method
