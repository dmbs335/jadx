.class public final Lio/ktor/utils/io/ByteReadChannelOperationsKt;
.super Ljava/lang/Object;

.method public static final readPacket(Lio/ktor/utils/io/ByteReadChannel;ILkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 14

    instance-of v0, p2, Lio/ktor/utils/io/ByteReadChannelOperationsKt$readPacket$1;
    if-eqz v0, :new_continuation
    move-object v0, p2
    check-cast v0, Lio/ktor/utils/io/ByteReadChannelOperationsKt$readPacket$1;
    iget v1, v0, Lio/ktor/utils/io/ByteReadChannelOperationsKt$readPacket$1;->label:I
    const/high16 v2, -0x80000000
    and-int v3, v1, v2
    if-eqz v3, :new_continuation
    sub-int/2addr v1, v2
    iput v1, v0, Lio/ktor/utils/io/ByteReadChannelOperationsKt$readPacket$1;->label:I
    goto :continuation_ready

    :new_continuation
    new-instance v0, Lio/ktor/utils/io/ByteReadChannelOperationsKt$readPacket$1;
    invoke-direct {v0, p2}, Lkotlin/coroutines/jvm/internal/ContinuationImpl;-><init>(Lkotlin/coroutines/Continuation;)V

    :continuation_ready
    iget-object p2, v0, Lio/ktor/utils/io/ByteReadChannelOperationsKt$readPacket$1;->result:Ljava/lang/Object;
    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v1
    iget v2, v0, Lio/ktor/utils/io/ByteReadChannelOperationsKt$readPacket$1;->label:I
    const/4 v3, 0x1
    if-eqz v2, :initial
    if-ne v2, v3, :invalid_state

    iget p0, v0, Lio/ktor/utils/io/ByteReadChannelOperationsKt$readPacket$1;->I$0:I
    iget-object p1, v0, Lio/ktor/utils/io/ByteReadChannelOperationsKt$readPacket$1;->L$1:Ljava/lang/Object;
    check-cast p1, Lkotlinx/io/Buffer;
    iget-object v2, v0, Lio/ktor/utils/io/ByteReadChannelOperationsKt$readPacket$1;->L$0:Ljava/lang/Object;
    check-cast v2, Lio/ktor/utils/io/ByteReadChannel;
    invoke-static {p2}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :resume_join

    :invalid_state
    new-instance p0, Ljava/lang/IllegalStateException;
    const-string p1, "call to resume before invoke"
    invoke-direct {p0, p1}, Ljava/lang/IllegalStateException;-><init>(Ljava/lang/String;)V
    throw p0

    :initial
    invoke-static {p2}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    new-instance p2, Lkotlinx/io/Buffer;
    invoke-direct {p2}, Lkotlinx/io/Buffer;-><init>()V
    move-object v10, p2
    move p2, p1
    move-object p1, v10

    :loop_header
    invoke-virtual {p1}, Lkotlinx/io/Buffer;->getSize()J
    move-result-wide v4
    int-to-long v6, p2
    cmp-long v2, v4, v6
    if-gez v2, :final_check
    invoke-interface {p0}, Lio/ktor/utils/io/ByteReadChannel;->getReadBuffer()Lkotlinx/io/Source;
    move-result-object v2
    invoke-interface {v2}, Lkotlinx/io/Source;->exhausted()Z
    move-result v2
    if-eqz v2, :consume

    iput-object p0, v0, Lio/ktor/utils/io/ByteReadChannelOperationsKt$readPacket$1;->L$0:Ljava/lang/Object;
    iput-object p1, v0, Lio/ktor/utils/io/ByteReadChannelOperationsKt$readPacket$1;->L$1:Ljava/lang/Object;
    iput p2, v0, Lio/ktor/utils/io/ByteReadChannelOperationsKt$readPacket$1;->I$0:I
    iput v3, v0, Lio/ktor/utils/io/ByteReadChannelOperationsKt$readPacket$1;->label:I
    const/4 v2, 0x0
    const/4 v4, 0x0
    invoke-static {p0, v2, v0, v3, v4}, Lio/ktor/utils/io/ByteReadChannel;->awaitContent$default(Lio/ktor/utils/io/ByteReadChannel;ILkotlin/coroutines/Continuation;ILjava/lang/Object;)Ljava/lang/Object;
    move-result-object v2
    if-ne v2, v1, :direct_completion
    return-object v1

    :direct_completion
    move-object v2, p0
    move p0, p2

    :resume_join
    move p2, p0
    move-object p0, v2

    :consume
    invoke-interface {p0}, Lio/ktor/utils/io/ByteReadChannel;->isClosedForRead()Z
    move-result v2
    if-nez v2, :final_check
    invoke-interface {p0}, Lio/ktor/utils/io/ByteReadChannel;->getReadBuffer()Lkotlinx/io/Source;
    move-result-object v2
    invoke-static {v2}, Lio/ktor/utils/io/core/ByteReadPacketKt;->getRemaining(Lkotlinx/io/Source;)J
    move-result-wide v4
    int-to-long v6, p2
    invoke-virtual {p1}, Lkotlinx/io/Buffer;->getSize()J
    move-result-wide v8
    sub-long v8, v6, v8
    cmp-long v2, v4, v8
    if-lez v2, :transfer
    invoke-interface {p0}, Lio/ktor/utils/io/ByteReadChannel;->getReadBuffer()Lkotlinx/io/Source;
    move-result-object v2
    invoke-virtual {p1}, Lkotlinx/io/Buffer;->getSize()J
    move-result-wide v4
    sub-long/2addr v6, v4
    invoke-interface {v2, p1, v6, v7}, Lkotlinx/io/Source;->readTo(Lkotlinx/io/RawSink;J)V
    goto :loop_header

    :transfer
    invoke-interface {p0}, Lio/ktor/utils/io/ByteReadChannel;->getReadBuffer()Lkotlinx/io/Source;
    move-result-object v2
    invoke-interface {v2, p1}, Lkotlinx/io/Source;->transferTo(Lkotlinx/io/RawSink;)J
    move-result-wide v4
    invoke-static {v4, v5}, Lkotlin/coroutines/jvm/internal/Boxing;->boxLong(J)Ljava/lang/Long;
    goto :loop_header

    :final_check
    invoke-virtual {p1}, Lkotlinx/io/Buffer;->getSize()J
    move-result-wide v0
    int-to-long v2, p2
    cmp-long p0, v0, v2
    if-ltz p0, :eof
    return-object p1

    :eof
    new-instance p0, Ljava/io/EOFException;
    const-string p2, "not enough bytes"
    invoke-direct {p0, p2}, Ljava/io/EOFException;-><init>(Ljava/lang/String;)V
    throw p0
.end method
