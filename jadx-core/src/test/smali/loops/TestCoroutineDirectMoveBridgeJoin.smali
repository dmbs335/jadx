.class public Lloops/TestCoroutineDirectMoveBridgeJoin;
.super Ljava/lang/Object;

.method public static readByteArray(Lio/ktor/utils/io/ByteReadChannel;ILkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 11

    instance-of v0, p2, Lio/ktor/utils/io/ByteReadChannelOperationsKt$readByteArray$1;
    if-eqz v0, :new_continuation
    move-object v0, p2
    check-cast v0, Lio/ktor/utils/io/ByteReadChannelOperationsKt$readByteArray$1;
    iget v1, v0, Lio/ktor/utils/io/ByteReadChannelOperationsKt$readByteArray$1;->label:I
    const/high16 v2, -0x80000000
    and-int v3, v1, v2
    if-eqz v3, :new_continuation
    sub-int/2addr v1, v2
    iput v1, v0, Lio/ktor/utils/io/ByteReadChannelOperationsKt$readByteArray$1;->label:I
    goto :continuation_ready

    :new_continuation
    new-instance v0, Lio/ktor/utils/io/ByteReadChannelOperationsKt$readByteArray$1;
    invoke-direct {v0, p2}, Lkotlin/coroutines/jvm/internal/ContinuationImpl;-><init>(Lkotlin/coroutines/Continuation;)V

    :continuation_ready
    iget-object p2, v0, Lio/ktor/utils/io/ByteReadChannelOperationsKt$readByteArray$1;->result:Ljava/lang/Object;
    invoke-static {}, Lkotlin/coroutines/intrinsics/a;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v1
    iget v2, v0, Lio/ktor/utils/io/ByteReadChannelOperationsKt$readByteArray$1;->label:I
    const/4 v3, 0x1
    if-eqz v2, :initial
    if-ne v2, v3, :bad_state

    iget p0, v0, Lio/ktor/utils/io/ByteReadChannelOperationsKt$readByteArray$1;->I$2:I
    iget p1, v0, Lio/ktor/utils/io/ByteReadChannelOperationsKt$readByteArray$1;->I$1:I
    iget v2, v0, Lio/ktor/utils/io/ByteReadChannelOperationsKt$readByteArray$1;->I$0:I
    iget-object v4, v0, Lio/ktor/utils/io/ByteReadChannelOperationsKt$readByteArray$1;->L$2:Ljava/lang/Object;
    check-cast v4, Lkotlinx/io/Sink;
    iget-object v5, v0, Lio/ktor/utils/io/ByteReadChannelOperationsKt$readByteArray$1;->L$1:Ljava/lang/Object;
    check-cast v5, Lkotlinx/io/Buffer;
    iget-object v6, v0, Lio/ktor/utils/io/ByteReadChannelOperationsKt$readByteArray$1;->L$0:Ljava/lang/Object;
    check-cast v6, Lio/ktor/utils/io/ByteReadChannel;

    :try_start
    invoke-static {p2}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    move-object v7, v4
    move v4, p0
    move-object p0, v6
    move-object v6, v5
    move-object v5, v7
    move-object v7, v0
    move v0, p1
    move p1, v2

    :try_end

    :result_join
    move-object v2, v7
    goto :result_tail

    :bad_state
    new-instance p0, Ljava/lang/IllegalStateException;
    const-string p1, "call to 'resume' before 'invoke' with coroutine"
    invoke-direct {p0, p1}, Ljava/lang/IllegalStateException;-><init>(Ljava/lang/String;)V
    throw p0

    :initial
    invoke-static {p2}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    new-instance p2, Lkotlinx/io/Buffer;
    invoke-direct {p2}, Lkotlinx/io/Buffer;-><init>()V
    const/4 v2, 0x0
    move-object v4, p2
    move-object v5, v4
    move p2, v2

    :loop_header
    invoke-static {v4}, Lio/ktor/utils/io/core/BytePacketBuilderKt;->getSize(Lkotlinx/io/Sink;)I
    move-result v6
    if-ge v6, p1, :done
    invoke-static {v4}, Lio/ktor/utils/io/core/BytePacketBuilderKt;->getSize(Lkotlinx/io/Sink;)I
    move-result v6
    sub-int v6, p1, v6
    iput-object p0, v0, Lio/ktor/utils/io/ByteReadChannelOperationsKt$readByteArray$1;->L$0:Ljava/lang/Object;
    iput-object v5, v0, Lio/ktor/utils/io/ByteReadChannelOperationsKt$readByteArray$1;->L$1:Ljava/lang/Object;
    iput-object v4, v0, Lio/ktor/utils/io/ByteReadChannelOperationsKt$readByteArray$1;->L$2:Ljava/lang/Object;
    iput p1, v0, Lio/ktor/utils/io/ByteReadChannelOperationsKt$readByteArray$1;->I$0:I
    iput p2, v0, Lio/ktor/utils/io/ByteReadChannelOperationsKt$readByteArray$1;->I$1:I
    iput v2, v0, Lio/ktor/utils/io/ByteReadChannelOperationsKt$readByteArray$1;->I$2:I
    iput v3, v0, Lio/ktor/utils/io/ByteReadChannelOperationsKt$readByteArray$1;->label:I
    invoke-static {p0, v6, v0}, Lio/ktor/utils/io/ByteReadChannelOperationsKt;->readPacket(Lio/ktor/utils/io/ByteReadChannel;ILkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v6
    if-ne v6, v1, :direct_bridge
    return-object v1

    :direct_bridge
    move-object v7, v0
    move v0, p2
    move-object p2, v6
    move-object v6, v5
    move-object v5, v4
    move v4, v2
    goto :result_join

    :result_tail
    check-cast p2, Lkotlinx/io/Source;
    invoke-static {v5, p2}, Lio/ktor/utils/io/core/BytePacketBuilderKt;->writePacket(Lkotlinx/io/Sink;Lkotlinx/io/Source;)V
    move p2, v0
    move-object v0, v2
    move v2, v4
    move-object v4, v5
    move-object v5, v6
    goto :loop_header

    :done
    invoke-static {v5}, Lkotlinx/io/SourcesKt;->readByteArray(Lkotlinx/io/Source;)[B
    move-result-object p0
    return-object p0

    :failure
    move-exception p0
    throw p0

    .catchall {:try_start .. :try_end} :failure
.end method
