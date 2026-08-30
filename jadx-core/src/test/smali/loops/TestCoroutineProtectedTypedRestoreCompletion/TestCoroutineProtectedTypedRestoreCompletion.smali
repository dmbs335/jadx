.class public final Lloops/TestCoroutineProtectedTypedRestoreCompletion;
.super Ljava/lang/Object;

.field private d:Lio/ktor/utils/io/ByteWriteChannel;
.field private h:Lkotlinx/coroutines/channels/Channel;

.method public constructor <init>()V
    .registers 1
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    return-void
.end method

.method public static access$writeLoop(Lloops/TestCoroutineProtectedTypedRestoreCompletion;Ljava/nio/ByteBuffer;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 3
    invoke-virtual {p0, p1, p2}, Lloops/TestCoroutineProtectedTypedRestoreCompletion;->c(Ljava/nio/ByteBuffer;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p0
    return-object p0
.end method

.method private static decode(I)Ljava/lang/String;
    .registers 1
    const-string p0, "closed"
    return-object p0
.end method

.method private final a()V
    .registers 1
    return-void
.end method

.method private final b(Lio/ktor/websocket/Frame;Ljava/nio/ByteBuffer;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 4
    sget-object p1, Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;
    return-object p1
.end method

.method public final c(Ljava/nio/ByteBuffer;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 11
    instance-of v0, p2, Lloops/TestCoroutineProtectedTypedRestoreCompletion$loop$1;
    if-eqz v0, :cond_13
    move-object v0, p2
    check-cast v0, Lloops/TestCoroutineProtectedTypedRestoreCompletion$loop$1;
    iget v1, v0, Lloops/TestCoroutineProtectedTypedRestoreCompletion$loop$1;->label:I
    const/high16 v2, -0x80000000
    and-int v3, v1, v2
    if-eqz v3, :cond_13
    sub-int/2addr v1, v2
    iput v1, v0, Lloops/TestCoroutineProtectedTypedRestoreCompletion$loop$1;->label:I
    goto :goto_18
    :cond_13
    new-instance v0, Lloops/TestCoroutineProtectedTypedRestoreCompletion$loop$1;
    invoke-direct {v0, p0, p2}, Lloops/TestCoroutineProtectedTypedRestoreCompletion$loop$1;-><init>(Lloops/TestCoroutineProtectedTypedRestoreCompletion;Lkotlin/coroutines/Continuation;)V
    :goto_18
    iget-object p2, v0, Lloops/TestCoroutineProtectedTypedRestoreCompletion$loop$1;->result:Ljava/lang/Object;
    invoke-static {}, Lkotlin/coroutines/intrinsics/a;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v1
    iget v2, v0, Lloops/TestCoroutineProtectedTypedRestoreCompletion$loop$1;->label:I
    const v3, -0x14e86a1f
    invoke-static {v3}, Lloops/TestCoroutineProtectedTypedRestoreCompletion;->decode(I)Ljava/lang/String;
    move-result-object v3
    const/4 v4, 0x0
    packed-switch v2, :pswitch_data_190
    new-instance p1, Ljava/lang/IllegalStateException;
    const-string p2, "call to \'resume\' before \'invoke\' with coroutine"
    invoke-direct {p1, p2}, Ljava/lang/IllegalStateException;-><init>(Ljava/lang/String;)V
    throw p1
    :pswitch_33
    iget-object p1, v0, Lloops/TestCoroutineProtectedTypedRestoreCompletion$loop$1;->L$1:Ljava/lang/Object;
    check-cast p1, Ljava/lang/Throwable;
    iget-object v0, v0, Lloops/TestCoroutineProtectedTypedRestoreCompletion$loop$1;->L$0:Ljava/lang/Object;
    check-cast v0, Ljava/nio/ByteBuffer;
    invoke-static {p2}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto/16 :goto_18e
    :pswitch_40
    iget-object p1, v0, Lloops/TestCoroutineProtectedTypedRestoreCompletion$loop$1;->L$0:Ljava/lang/Object;
    check-cast p1, Ljava/nio/ByteBuffer;
    invoke-static {p2}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto/16 :goto_169
    :pswitch_49
    iget-object p1, v0, Lloops/TestCoroutineProtectedTypedRestoreCompletion$loop$1;->L$1:Ljava/lang/Object;
    check-cast p1, Lkotlinx/coroutines/channels/ChannelIterator;
    iget-object v2, v0, Lloops/TestCoroutineProtectedTypedRestoreCompletion$loop$1;->L$0:Ljava/lang/Object;
    check-cast v2, Ljava/nio/ByteBuffer;
    :try_start_51
    invoke-static {p2}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    :try_end_54
    .catch Lio/ktor/util/cio/ChannelWriteException; {:try_start_51 .. :try_end_54} :catch_5c
    .catchall {:try_start_51 .. :try_end_54} :catchall_59
    move-object v7, v0
    move-object v0, p1
    move-object p1, v2
    move-object v2, v7
    goto :goto_b8
    :catchall_59
    move-exception p1
    goto/16 :goto_119
    :catch_5c
    move-exception p1
    goto/16 :goto_13f
    :pswitch_5f
    iget-object p1, v0, Lloops/TestCoroutineProtectedTypedRestoreCompletion$loop$1;->L$1:Ljava/lang/Object;
    check-cast p1, Lkotlinx/coroutines/channels/ChannelIterator;
    iget-object v2, v0, Lloops/TestCoroutineProtectedTypedRestoreCompletion$loop$1;->L$0:Ljava/lang/Object;
    check-cast v2, Ljava/nio/ByteBuffer;
    :try_start_67
    invoke-static {p2}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    :try_end_6a
    .catch Lio/ktor/util/cio/ChannelWriteException; {:try_start_67 .. :try_end_6a} :catch_5c
    .catchall {:try_start_67 .. :try_end_6a} :catchall_59
    move-object v7, v0
    move-object v0, p1
    move-object p1, v2
    :goto_6d
    move-object v2, v7
    goto :goto_90
    :pswitch_6f
    invoke-static {p2}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    invoke-virtual {p1}, Ljava/nio/ByteBuffer;->clear()Ljava/nio/Buffer;
    :try_start_75
    iget-object p2, p0, Lloops/TestCoroutineProtectedTypedRestoreCompletion;->h:Lkotlinx/coroutines/channels/Channel;
    invoke-interface {p2}, Lkotlinx/coroutines/channels/ReceiveChannel;->iterator()Lkotlinx/coroutines/channels/ChannelIterator;
    move-result-object p2
    :goto_7b
    iput-object p1, v0, Lloops/TestCoroutineProtectedTypedRestoreCompletion$loop$1;->L$0:Ljava/lang/Object;
    iput-object p2, v0, Lloops/TestCoroutineProtectedTypedRestoreCompletion$loop$1;->L$1:Ljava/lang/Object;
    iput-object v4, v0, Lloops/TestCoroutineProtectedTypedRestoreCompletion$loop$1;->L$2:Ljava/lang/Object;
    const/4 v2, 0x1
    iput v2, v0, Lloops/TestCoroutineProtectedTypedRestoreCompletion$loop$1;->label:I
    invoke-interface {p2, v0}, Lkotlinx/coroutines/channels/ChannelIterator;->hasNext(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v2
    :try_end_88
    .catch Lio/ktor/util/cio/ChannelWriteException; {:try_start_75 .. :try_end_88} :catch_117
    .catchall {:try_start_75 .. :try_end_88} :catchall_115
    if-ne v2, v1, :cond_8c
    goto/16 :goto_18d
    :cond_8c
    move-object v7, v0
    move-object v0, p2
    move-object p2, v2
    goto :goto_6d
    :goto_90
    :try_start_90
    check-cast p2, Ljava/lang/Boolean;
    invoke-virtual {p2}, Ljava/lang/Boolean;->booleanValue()Z
    move-result p2
    if-eqz p2, :cond_f5
    invoke-interface {v0}, Lkotlinx/coroutines/channels/ChannelIterator;->next()Ljava/lang/Object;
    move-result-object p2
    instance-of v5, p2, Lio/ktor/websocket/Frame;
    if-eqz v5, :cond_cf
    move-object v5, p2
    check-cast v5, Lio/ktor/websocket/Frame;
    iput-object p1, v2, Lloops/TestCoroutineProtectedTypedRestoreCompletion$loop$1;->L$0:Ljava/lang/Object;
    iput-object v0, v2, Lloops/TestCoroutineProtectedTypedRestoreCompletion$loop$1;->L$1:Ljava/lang/Object;
    invoke-static {p2}, Lkotlin/coroutines/jvm/internal/SpillingKt;->nullOutSpilledVariable(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object p2
    iput-object p2, v2, Lloops/TestCoroutineProtectedTypedRestoreCompletion$loop$1;->L$2:Ljava/lang/Object;
    const/4 p2, 0x2
    iput p2, v2, Lloops/TestCoroutineProtectedTypedRestoreCompletion$loop$1;->label:I
    invoke-virtual {p0, v5, p1, v2}, Lloops/TestCoroutineProtectedTypedRestoreCompletion;->b(Lio/ktor/websocket/Frame;Ljava/nio/ByteBuffer;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p2
    if-ne p2, v1, :cond_b8
    goto/16 :goto_18d
    :cond_b8
    :goto_b8
    check-cast p2, Ljava/lang/Boolean;
    invoke-virtual {p2}, Ljava/lang/Boolean;->booleanValue()Z
    move-result p2
    if-eqz p2, :cond_c1
    goto :goto_f5
    :cond_c1
    :goto_c1
    move-object p2, v0
    move-object v0, v2
    goto :goto_7b
    :catchall_c4
    move-exception p2
    move-object v0, v2
    :goto_c6
    move-object v2, p1
    move-object p1, p2
    goto :goto_119
    :catch_c9
    move-exception p2
    move-object v0, v2
    :goto_cb
    move-object v2, p1
    move-object p1, p2
    goto/16 :goto_13f
    :cond_cf
    instance-of v5, p2, Lio/ktor/websocket/WebSocketWriter$FlushRequest;
    if-eqz v5, :cond_dd
    check-cast p2, Lio/ktor/websocket/WebSocketWriter$FlushRequest;
    invoke-virtual {p2}, Lio/ktor/websocket/WebSocketWriter$FlushRequest;->complete()Z
    move-result p2
    invoke-static {p2}, Lkotlin/coroutines/jvm/internal/Boxing;->boxBoolean(Z)Ljava/lang/Boolean;
    goto :goto_c1
    :cond_dd
    new-instance v0, Ljava/lang/IllegalArgumentException;
    new-instance v5, Ljava/lang/StringBuilder;
    invoke-direct {v5}, Ljava/lang/StringBuilder;-><init>()V
    const-string/jumbo v6, "unknown message "
    invoke-virtual {v5, v6}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
    invoke-virtual {v5, p2}, Ljava/lang/StringBuilder;->append(Ljava/lang/Object;)Ljava/lang/StringBuilder;
    invoke-virtual {v5}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;
    move-result-object p2
    invoke-direct {v0, p2}, Ljava/lang/IllegalArgumentException;-><init>(Ljava/lang/String;)V
    throw v0
    :try_end_f5
    .catch Lio/ktor/util/cio/ChannelWriteException; {:try_start_90 .. :try_end_f5} :catch_c9
    .catchall {:try_start_90 .. :try_end_f5} :catchall_c4
    :cond_f5
    :goto_f5
    iget-object p2, p0, Lloops/TestCoroutineProtectedTypedRestoreCompletion;->h:Lkotlinx/coroutines/channels/Channel;
    invoke-static {v3, v4}, Lkotlinx/coroutines/ExceptionsKt;->CancellationException(Ljava/lang/String;Ljava/lang/Throwable;)Ljava/util/concurrent/CancellationException;
    move-result-object v0
    invoke-interface {p2, v0}, Lkotlinx/coroutines/channels/SendChannel;->close(Ljava/lang/Throwable;)Z
    iget-object p2, p0, Lloops/TestCoroutineProtectedTypedRestoreCompletion;->d:Lio/ktor/utils/io/ByteWriteChannel;
    invoke-static {p1}, Lkotlin/coroutines/jvm/internal/SpillingKt;->nullOutSpilledVariable(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object p1
    iput-object p1, v2, Lloops/TestCoroutineProtectedTypedRestoreCompletion$loop$1;->L$0:Ljava/lang/Object;
    iput-object v4, v2, Lloops/TestCoroutineProtectedTypedRestoreCompletion$loop$1;->L$1:Ljava/lang/Object;
    iput-object v4, v2, Lloops/TestCoroutineProtectedTypedRestoreCompletion$loop$1;->L$2:Ljava/lang/Object;
    const/4 p1, 0x3
    iput p1, v2, Lloops/TestCoroutineProtectedTypedRestoreCompletion$loop$1;->label:I
    invoke-interface {p2, v2}, Lio/ktor/utils/io/ByteWriteChannel;->flushAndClose(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-ne p1, v1, :cond_169
    goto/16 :goto_18d
    :catchall_115
    move-exception p2
    goto :goto_c6
    :catch_117
    move-exception p2
    goto :goto_cb
    :goto_119
    :try_start_119
    iget-object p2, p0, Lloops/TestCoroutineProtectedTypedRestoreCompletion;->h:Lkotlinx/coroutines/channels/Channel;
    invoke-interface {p2, p1}, Lkotlinx/coroutines/channels/SendChannel;->close(Ljava/lang/Throwable;)Z
    :try_end_11e
    .catchall {:try_start_119 .. :try_end_11e} :catchall_13d
    iget-object p1, p0, Lloops/TestCoroutineProtectedTypedRestoreCompletion;->h:Lkotlinx/coroutines/channels/Channel;
    invoke-static {v3, v4}, Lkotlinx/coroutines/ExceptionsKt;->CancellationException(Ljava/lang/String;Ljava/lang/Throwable;)Ljava/util/concurrent/CancellationException;
    move-result-object p2
    invoke-interface {p1, p2}, Lkotlinx/coroutines/channels/SendChannel;->close(Ljava/lang/Throwable;)Z
    iget-object p1, p0, Lloops/TestCoroutineProtectedTypedRestoreCompletion;->d:Lio/ktor/utils/io/ByteWriteChannel;
    invoke-static {v2}, Lkotlin/coroutines/jvm/internal/SpillingKt;->nullOutSpilledVariable(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object p2
    iput-object p2, v0, Lloops/TestCoroutineProtectedTypedRestoreCompletion$loop$1;->L$0:Ljava/lang/Object;
    iput-object v4, v0, Lloops/TestCoroutineProtectedTypedRestoreCompletion$loop$1;->L$1:Ljava/lang/Object;
    iput-object v4, v0, Lloops/TestCoroutineProtectedTypedRestoreCompletion$loop$1;->L$2:Ljava/lang/Object;
    const/4 p2, 0x5
    iput p2, v0, Lloops/TestCoroutineProtectedTypedRestoreCompletion$loop$1;->label:I
    invoke-interface {p1, v0}, Lio/ktor/utils/io/ByteWriteChannel;->flushAndClose(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-ne p1, v1, :cond_169
    goto :goto_18d
    :catchall_13d
    move-exception p1
    goto :goto_16f
    :goto_13f
    :try_start_13f
    iget-object p2, p0, Lloops/TestCoroutineProtectedTypedRestoreCompletion;->h:Lkotlinx/coroutines/channels/Channel;
    const-string v5, "Failed to write to WebSocket."
    invoke-static {v5, p1}, Lkotlinx/coroutines/ExceptionsKt;->CancellationException(Ljava/lang/String;Ljava/lang/Throwable;)Ljava/util/concurrent/CancellationException;
    move-result-object p1
    invoke-interface {p2, p1}, Lkotlinx/coroutines/channels/SendChannel;->close(Ljava/lang/Throwable;)Z
    :try_end_14a
    .catchall {:try_start_13f .. :try_end_14a} :catchall_13d
    iget-object p1, p0, Lloops/TestCoroutineProtectedTypedRestoreCompletion;->h:Lkotlinx/coroutines/channels/Channel;
    invoke-static {v3, v4}, Lkotlinx/coroutines/ExceptionsKt;->CancellationException(Ljava/lang/String;Ljava/lang/Throwable;)Ljava/util/concurrent/CancellationException;
    move-result-object p2
    invoke-interface {p1, p2}, Lkotlinx/coroutines/channels/SendChannel;->close(Ljava/lang/Throwable;)Z
    iget-object p1, p0, Lloops/TestCoroutineProtectedTypedRestoreCompletion;->d:Lio/ktor/utils/io/ByteWriteChannel;
    invoke-static {v2}, Lkotlin/coroutines/jvm/internal/SpillingKt;->nullOutSpilledVariable(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object p2
    iput-object p2, v0, Lloops/TestCoroutineProtectedTypedRestoreCompletion$loop$1;->L$0:Ljava/lang/Object;
    iput-object v4, v0, Lloops/TestCoroutineProtectedTypedRestoreCompletion$loop$1;->L$1:Ljava/lang/Object;
    iput-object v4, v0, Lloops/TestCoroutineProtectedTypedRestoreCompletion$loop$1;->L$2:Ljava/lang/Object;
    const/4 p2, 0x4
    iput p2, v0, Lloops/TestCoroutineProtectedTypedRestoreCompletion$loop$1;->label:I
    invoke-interface {p1, v0}, Lio/ktor/utils/io/ByteWriteChannel;->flushAndClose(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-ne p1, v1, :cond_169
    goto :goto_18d
    :cond_169
    :goto_169
    invoke-virtual {p0}, Lloops/TestCoroutineProtectedTypedRestoreCompletion;->a()V
    sget-object p1, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object p1
    :goto_16f
    iget-object p2, p0, Lloops/TestCoroutineProtectedTypedRestoreCompletion;->h:Lkotlinx/coroutines/channels/Channel;
    invoke-static {v3, v4}, Lkotlinx/coroutines/ExceptionsKt;->CancellationException(Ljava/lang/String;Ljava/lang/Throwable;)Ljava/util/concurrent/CancellationException;
    move-result-object v3
    invoke-interface {p2, v3}, Lkotlinx/coroutines/channels/SendChannel;->close(Ljava/lang/Throwable;)Z
    iget-object p2, p0, Lloops/TestCoroutineProtectedTypedRestoreCompletion;->d:Lio/ktor/utils/io/ByteWriteChannel;
    invoke-static {v2}, Lkotlin/coroutines/jvm/internal/SpillingKt;->nullOutSpilledVariable(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v2
    iput-object v2, v0, Lloops/TestCoroutineProtectedTypedRestoreCompletion$loop$1;->L$0:Ljava/lang/Object;
    iput-object p1, v0, Lloops/TestCoroutineProtectedTypedRestoreCompletion$loop$1;->L$1:Ljava/lang/Object;
    iput-object v4, v0, Lloops/TestCoroutineProtectedTypedRestoreCompletion$loop$1;->L$2:Ljava/lang/Object;
    const/4 v2, 0x6
    iput v2, v0, Lloops/TestCoroutineProtectedTypedRestoreCompletion$loop$1;->label:I
    invoke-interface {p2, v0}, Lio/ktor/utils/io/ByteWriteChannel;->flushAndClose(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p2
    if-ne p2, v1, :cond_18e
    :goto_18d
    return-object v1
    :cond_18e
    :goto_18e
    throw p1
    nop
    :pswitch_data_190
    .packed-switch 0x0
        :pswitch_6f
        :pswitch_5f
        :pswitch_49
        :pswitch_40
        :pswitch_40
        :pswitch_40
        :pswitch_33
    .end packed-switch
.end method
