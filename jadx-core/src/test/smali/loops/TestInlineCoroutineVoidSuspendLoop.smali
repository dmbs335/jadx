.class public final Lloops/TestInlineCoroutineVoidSuspendLoop;
.super Lkotlin/coroutines/jvm/internal/SuspendLambda;

.field L$0:Ljava/lang/Object;
.field label:I
.field final synthetic this$0:Lio/ktor/websocket/RawWebSocketCommon;

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 11
    invoke-static {}, Lkotlin/coroutines/intrinsics/a;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v0
    iget v1, p0, Lloops/TestInlineCoroutineVoidSuspendLoop;->label:I
    const/4 v2, 0x4
    const/4 v3, 0x3
    const/4 v4, 0x2
    const/4 v5, 0x1
    const/4 v6, 0x0
    if-eqz v1, :cond_4e
    if-eq v1, v5, :cond_4a
    if-eq v1, v4, :cond_37
    if-eq v1, v3, :cond_2e
    if-ne v1, v2, :cond_21
    iget-object v0, p0, Lloops/TestInlineCoroutineVoidSuspendLoop;->L$0:Ljava/lang/Object;
    check-cast v0, Lio/ktor/websocket/ProtocolViolationException;
    :try_start_19
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    :try_end_1c
    .catchall {:try_start_19 .. :try_end_1c} :catchall_1e
    goto/16 :goto_e5
    :catchall_1e
    move-exception p1
    goto/16 :goto_11e
    :cond_21
    new-instance p1, Ljava/lang/IllegalStateException;
    const-string v0, "call to resume before invoke with coroutine"
    invoke-direct {p1, v0}, Ljava/lang/IllegalStateException;-><init>(Ljava/lang/String;)V
    throw p1
    :cond_2e
    iget-object v0, p0, Lloops/TestInlineCoroutineVoidSuspendLoop;->L$0:Ljava/lang/Object;
    check-cast v0, Lio/ktor/websocket/FrameTooBigException;
    :try_start_32
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    :try_end_35
    .catchall {:try_start_32 .. :try_end_35} :catchall_1e
    goto/16 :goto_111
    :cond_37
    iget-object v1, p0, Lloops/TestInlineCoroutineVoidSuspendLoop;->L$0:Ljava/lang/Object;
    check-cast v1, Lio/ktor/websocket/Frame;
    :try_start_3b
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :goto_51
    :catchall_3f
    move-exception p1
    goto :goto_a5
    :catch_41
    move-exception p1
    goto/16 :goto_b9
    :catch_44
    move-exception p1
    goto/16 :goto_c3
    :catch_47
    move-exception p1
    goto/16 :goto_ef
    :cond_4a
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    :try_end_4d
    .catch Lio/ktor/websocket/FrameTooBigException; {:try_start_3b .. :try_end_4d} :catch_47
    .catch Lio/ktor/websocket/ProtocolViolationException; {:try_start_3b .. :try_end_4d} :catch_44
    .catch Ljava/util/concurrent/CancellationException; {:try_start_3b .. :try_end_4d} :catch_41
    .catch Ljava/io/EOFException; {:try_start_3b .. :try_end_4d} :catch_af
    .catch Lkotlinx/coroutines/channels/ClosedReceiveChannelException; {:try_start_3b .. :try_end_4d} :catch_af
    .catchall {:try_start_3b .. :try_end_4d} :catchall_3f
    goto :goto_6f
    :cond_4e
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    :cond_51
    :goto_51
    :try_start_51
    iget-object p1, p0, Lloops/TestInlineCoroutineVoidSuspendLoop;->this$0:Lio/ktor/websocket/RawWebSocketCommon;
    # getter for: Lio/ktor/websocket/RawWebSocketCommon;->d:Lio/ktor/utils/io/ByteReadChannel;
    invoke-static {p1}, Lio/ktor/websocket/RawWebSocketCommon;->access$getInput$p(Lio/ktor/websocket/RawWebSocketCommon;)Lio/ktor/utils/io/ByteReadChannel;
    move-result-object p1
    iget-object v1, p0, Lloops/TestInlineCoroutineVoidSuspendLoop;->this$0:Lio/ktor/websocket/RawWebSocketCommon;
    invoke-virtual {v1}, Lio/ktor/websocket/RawWebSocketCommon;->getMaxFrameSize()J
    move-result-wide v7
    iget-object v1, p0, Lloops/TestInlineCoroutineVoidSuspendLoop;->this$0:Lio/ktor/websocket/RawWebSocketCommon;
    # getter for: Lio/ktor/websocket/RawWebSocketCommon;->k:I
    invoke-static {v1}, Lio/ktor/websocket/RawWebSocketCommon;->access$getLastOpcode$p(Lio/ktor/websocket/RawWebSocketCommon;)I
    move-result v1
    iput-object v6, p0, Lloops/TestInlineCoroutineVoidSuspendLoop;->L$0:Ljava/lang/Object;
    iput v5, p0, Lloops/TestInlineCoroutineVoidSuspendLoop;->label:I
    invoke-static {p1, v7, v8, v1, p0}, Lio/ktor/websocket/RawWebSocketCommonKt;->readFrame(Lio/ktor/utils/io/ByteReadChannel;JILkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-ne p1, v0, :cond_6f
    goto/16 :goto_10f
    :cond_6f
    :goto_6f
    check-cast p1, Lio/ktor/websocket/Frame;
    invoke-virtual {p1}, Lio/ktor/websocket/Frame;->getFrameType()Lio/ktor/websocket/FrameType;
    move-result-object v1
    invoke-virtual {v1}, Lio/ktor/websocket/FrameType;->getControlFrame()Z
    move-result v1
    if-nez v1, :cond_90
    iget-object v1, p0, Lloops/TestInlineCoroutineVoidSuspendLoop;->this$0:Lio/ktor/websocket/RawWebSocketCommon;
    invoke-virtual {p1}, Lio/ktor/websocket/Frame;->getFin()Z
    move-result v7
    if-eqz v7, :cond_85
    const/4 v7, 0x0
    goto :goto_8d
    :cond_85
    invoke-virtual {p1}, Lio/ktor/websocket/Frame;->getFrameType()Lio/ktor/websocket/FrameType;
    move-result-object v7
    invoke-virtual {v7}, Lio/ktor/websocket/FrameType;->getOpcode()I
    move-result v7
    :goto_8d
    invoke-static {v1, v7}, Lio/ktor/websocket/RawWebSocketCommon;->access$setLastOpcode$p(Lio/ktor/websocket/RawWebSocketCommon;I)V
    :cond_90
    iget-object v1, p0, Lloops/TestInlineCoroutineVoidSuspendLoop;->this$0:Lio/ktor/websocket/RawWebSocketCommon;
    # getter for: Lio/ktor/websocket/RawWebSocketCommon;->i:Lkotlinx/coroutines/channels/Channel;
    invoke-static {v1}, Lio/ktor/websocket/RawWebSocketCommon;->access$get_incoming$p(Lio/ktor/websocket/RawWebSocketCommon;)Lkotlinx/coroutines/channels/Channel;
    move-result-object v1
    invoke-static {p1}, Lkotlin/coroutines/jvm/internal/SpillingKt;->nullOutSpilledVariable(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v7
    iput-object v7, p0, Lloops/TestInlineCoroutineVoidSuspendLoop;->L$0:Ljava/lang/Object;
    iput v4, p0, Lloops/TestInlineCoroutineVoidSuspendLoop;->label:I
    invoke-interface {v1, p1, p0}, Lkotlinx/coroutines/channels/SendChannel;->send(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    :try_end_a2
    .catch Lio/ktor/websocket/FrameTooBigException; {:try_start_51 .. :try_end_a2} :catch_47
    .catch Lio/ktor/websocket/ProtocolViolationException; {:try_start_51 .. :try_end_a2} :catch_44
    .catch Ljava/util/concurrent/CancellationException; {:try_start_51 .. :try_end_a2} :catch_41
    .catch Ljava/io/EOFException; {:try_start_51 .. :try_end_a2} :catch_af
    .catch Lkotlinx/coroutines/channels/ClosedReceiveChannelException; {:try_start_51 .. :try_end_a2} :catch_af
    .catchall {:try_start_51 .. :try_end_a2} :catchall_3f
    if-ne p1, v0, :cond_51
    goto :goto_10f
    :goto_a5
    :try_start_a5
    iget-object v0, p0, Lloops/TestInlineCoroutineVoidSuspendLoop;->this$0:Lio/ktor/websocket/RawWebSocketCommon;
    # getter for: Lio/ktor/websocket/RawWebSocketCommon;->i:Lkotlinx/coroutines/channels/Channel;
    invoke-static {v0}, Lio/ktor/websocket/RawWebSocketCommon;->access$get_incoming$p(Lio/ktor/websocket/RawWebSocketCommon;)Lkotlinx/coroutines/channels/Channel;
    move-result-object v0
    invoke-interface {v0, p1}, Lkotlinx/coroutines/channels/SendChannel;->close(Ljava/lang/Throwable;)Z
    throw p1
    :try_end_af
    .catchall {:try_start_a5 .. :try_end_af} :catchall_1e
    :catch_af
    :goto_af
    iget-object p1, p0, Lloops/TestInlineCoroutineVoidSuspendLoop;->this$0:Lio/ktor/websocket/RawWebSocketCommon;
    # getter for: Lio/ktor/websocket/RawWebSocketCommon;->i:Lkotlinx/coroutines/channels/Channel;
    invoke-static {p1}, Lio/ktor/websocket/RawWebSocketCommon;->access$get_incoming$p(Lio/ktor/websocket/RawWebSocketCommon;)Lkotlinx/coroutines/channels/Channel;
    move-result-object p1
    invoke-static {p1, v6, v5, v6}, Lkotlinx/coroutines/channels/SendChannel$DefaultImpls;->close$default(Lkotlinx/coroutines/channels/SendChannel;Ljava/lang/Throwable;ILjava/lang/Object;)Z
    goto :goto_11b
    :goto_b9
    :try_start_b9
    iget-object v0, p0, Lloops/TestInlineCoroutineVoidSuspendLoop;->this$0:Lio/ktor/websocket/RawWebSocketCommon;
    # getter for: Lio/ktor/websocket/RawWebSocketCommon;->i:Lkotlinx/coroutines/channels/Channel;
    invoke-static {v0}, Lio/ktor/websocket/RawWebSocketCommon;->access$get_incoming$p(Lio/ktor/websocket/RawWebSocketCommon;)Lkotlinx/coroutines/channels/Channel;
    move-result-object v0
    invoke-interface {v0, p1}, Lkotlinx/coroutines/channels/ReceiveChannel;->cancel(Ljava/util/concurrent/CancellationException;)V
    goto :goto_af
    :goto_c3
    iget-object v1, p0, Lloops/TestInlineCoroutineVoidSuspendLoop;->this$0:Lio/ktor/websocket/RawWebSocketCommon;
    invoke-virtual {v1}, Lio/ktor/websocket/RawWebSocketCommon;->getOutgoing()Lkotlinx/coroutines/channels/SendChannel;
    move-result-object v1
    new-instance v3, Lio/ktor/websocket/Frame$Close;
    new-instance v4, Lio/ktor/websocket/CloseReason;
    sget-object v7, Lio/ktor/websocket/CloseReason$Codes;->PROTOCOL_ERROR:Lio/ktor/websocket/CloseReason$Codes;
    invoke-virtual {p1}, Lio/ktor/websocket/ProtocolViolationException;->getMessage()Ljava/lang/String;
    move-result-object v8
    invoke-direct {v4, v7, v8}, Lio/ktor/websocket/CloseReason;-><init>(Lio/ktor/websocket/CloseReason$Codes;Ljava/lang/String;)V
    invoke-direct {v3, v4}, Lio/ktor/websocket/Frame$Close;-><init>(Lio/ktor/websocket/CloseReason;)V
    iput-object p1, p0, Lloops/TestInlineCoroutineVoidSuspendLoop;->L$0:Ljava/lang/Object;
    iput v2, p0, Lloops/TestInlineCoroutineVoidSuspendLoop;->label:I
    invoke-interface {v1, v3, p0}, Lkotlinx/coroutines/channels/SendChannel;->send(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v1
    if-ne v1, v0, :cond_e4
    goto :goto_10f
    :cond_e4
    move-object v0, p1
    :goto_e5
    iget-object p1, p0, Lloops/TestInlineCoroutineVoidSuspendLoop;->this$0:Lio/ktor/websocket/RawWebSocketCommon;
    # getter for: Lio/ktor/websocket/RawWebSocketCommon;->i:Lkotlinx/coroutines/channels/Channel;
    invoke-static {p1}, Lio/ktor/websocket/RawWebSocketCommon;->access$get_incoming$p(Lio/ktor/websocket/RawWebSocketCommon;)Lkotlinx/coroutines/channels/Channel;
    move-result-object p1
    invoke-interface {p1, v0}, Lkotlinx/coroutines/channels/SendChannel;->close(Ljava/lang/Throwable;)Z
    goto :goto_af
    :goto_ef
    iget-object v1, p0, Lloops/TestInlineCoroutineVoidSuspendLoop;->this$0:Lio/ktor/websocket/RawWebSocketCommon;
    invoke-virtual {v1}, Lio/ktor/websocket/RawWebSocketCommon;->getOutgoing()Lkotlinx/coroutines/channels/SendChannel;
    move-result-object v1
    new-instance v2, Lio/ktor/websocket/Frame$Close;
    new-instance v4, Lio/ktor/websocket/CloseReason;
    sget-object v7, Lio/ktor/websocket/CloseReason$Codes;->TOO_BIG:Lio/ktor/websocket/CloseReason$Codes;
    invoke-virtual {p1}, Lio/ktor/websocket/FrameTooBigException;->getMessage()Ljava/lang/String;
    move-result-object v8
    invoke-direct {v4, v7, v8}, Lio/ktor/websocket/CloseReason;-><init>(Lio/ktor/websocket/CloseReason$Codes;Ljava/lang/String;)V
    invoke-direct {v2, v4}, Lio/ktor/websocket/Frame$Close;-><init>(Lio/ktor/websocket/CloseReason;)V
    iput-object p1, p0, Lloops/TestInlineCoroutineVoidSuspendLoop;->L$0:Ljava/lang/Object;
    iput v3, p0, Lloops/TestInlineCoroutineVoidSuspendLoop;->label:I
    invoke-interface {v1, v2, p0}, Lkotlinx/coroutines/channels/SendChannel;->send(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v1
    if-ne v1, v0, :cond_110
    :goto_10f
    return-object v0
    :cond_110
    move-object v0, p1
    :goto_111
    iget-object p1, p0, Lloops/TestInlineCoroutineVoidSuspendLoop;->this$0:Lio/ktor/websocket/RawWebSocketCommon;
    # getter for: Lio/ktor/websocket/RawWebSocketCommon;->i:Lkotlinx/coroutines/channels/Channel;
    invoke-static {p1}, Lio/ktor/websocket/RawWebSocketCommon;->access$get_incoming$p(Lio/ktor/websocket/RawWebSocketCommon;)Lkotlinx/coroutines/channels/Channel;
    move-result-object p1
    invoke-interface {p1, v0}, Lkotlinx/coroutines/channels/SendChannel;->close(Ljava/lang/Throwable;)Z
    :try_end_11a
    .catchall {:try_start_b9 .. :try_end_11a} :catchall_1e
    goto :goto_af
    :goto_11b
    sget-object p1, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object p1
    :goto_11e
    iget-object v0, p0, Lloops/TestInlineCoroutineVoidSuspendLoop;->this$0:Lio/ktor/websocket/RawWebSocketCommon;
    # getter for: Lio/ktor/websocket/RawWebSocketCommon;->i:Lkotlinx/coroutines/channels/Channel;
    invoke-static {v0}, Lio/ktor/websocket/RawWebSocketCommon;->access$get_incoming$p(Lio/ktor/websocket/RawWebSocketCommon;)Lkotlinx/coroutines/channels/Channel;
    move-result-object v0
    invoke-static {v0, v6, v5, v6}, Lkotlinx/coroutines/channels/SendChannel$DefaultImpls;->close$default(Lkotlinx/coroutines/channels/SendChannel;Ljava/lang/Throwable;ILjava/lang/Object;)Z
    throw p1
.end method
