.class public final Lio/ktor/utils/io/ByteReadChannelOperationsKt;
.super Ljava/lang/Object;

.method public static final readBuffer(Lio/ktor/utils/io/ByteReadChannel;ILkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 11

    instance-of v0, p2, Lio/ktor/utils/io/ByteReadChannelOperationsKt$readBuffer$3;
    if-eqz v0, :new_continuation
    move-object v0, p2
    check-cast v0, Lio/ktor/utils/io/ByteReadChannelOperationsKt$readBuffer$3;
    iget v1, v0, Lio/ktor/utils/io/ByteReadChannelOperationsKt$readBuffer$3;->label:I
    const/high16 v2, -0x80000000
    and-int v3, v1, v2
    if-eqz v3, :new_continuation
    sub-int/2addr v1, v2
    iput v1, v0, Lio/ktor/utils/io/ByteReadChannelOperationsKt$readBuffer$3;->label:I
    goto :continuation_ready

    :new_continuation
    new-instance v0, Lio/ktor/utils/io/ByteReadChannelOperationsKt$readBuffer$3;
    invoke-direct {v0, p2}, Lkotlin/coroutines/jvm/internal/ContinuationImpl;-><init>(Lkotlin/coroutines/Continuation;)V

    :continuation_ready
    iget-object p2, v0, Lio/ktor/utils/io/ByteReadChannelOperationsKt$readBuffer$3;->result:Ljava/lang/Object;
    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v1
    iget v2, v0, Lio/ktor/utils/io/ByteReadChannelOperationsKt$readBuffer$3;->label:I
    const/4 v3, 0x1
    if-eqz v2, :initial
    if-ne v2, v3, :invalid_state

    iget p0, v0, Lio/ktor/utils/io/ByteReadChannelOperationsKt$readBuffer$3;->I$1:I
    iget p1, v0, Lio/ktor/utils/io/ByteReadChannelOperationsKt$readBuffer$3;->I$0:I
    iget-object v2, v0, Lio/ktor/utils/io/ByteReadChannelOperationsKt$readBuffer$3;->L$1:Ljava/lang/Object;
    check-cast v2, Lkotlinx/io/Buffer;
    iget-object v4, v0, Lio/ktor/utils/io/ByteReadChannelOperationsKt$readBuffer$3;->L$0:Ljava/lang/Object;
    check-cast v4, Lio/ktor/utils/io/ByteReadChannel;
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
    move-object v2, p2
    move p2, p1

    :loop_header
    if-lez p1, :done
    invoke-interface {p0}, Lio/ktor/utils/io/ByteReadChannel;->isClosedForRead()Z
    move-result v4
    if-nez v4, :done
    invoke-interface {p0}, Lio/ktor/utils/io/ByteReadChannel;->getReadBuffer()Lkotlinx/io/Source;
    move-result-object v4
    invoke-interface {v4}, Lkotlinx/io/Source;->exhausted()Z
    move-result v4
    if-eqz v4, :consume

    iput-object p0, v0, Lio/ktor/utils/io/ByteReadChannelOperationsKt$readBuffer$3;->L$0:Ljava/lang/Object;
    iput-object v2, v0, Lio/ktor/utils/io/ByteReadChannelOperationsKt$readBuffer$3;->L$1:Ljava/lang/Object;
    iput p2, v0, Lio/ktor/utils/io/ByteReadChannelOperationsKt$readBuffer$3;->I$0:I
    iput p1, v0, Lio/ktor/utils/io/ByteReadChannelOperationsKt$readBuffer$3;->I$1:I
    iput v3, v0, Lio/ktor/utils/io/ByteReadChannelOperationsKt$readBuffer$3;->label:I
    const/4 v4, 0x0
    const/4 v5, 0x0
    invoke-static {p0, v4, v0, v3, v5}, Lio/ktor/utils/io/ByteReadChannel;->awaitContent$default(Lio/ktor/utils/io/ByteReadChannel;ILkotlin/coroutines/Continuation;ILjava/lang/Object;)Ljava/lang/Object;
    move-result-object v4
    if-ne v4, v1, :direct_completion
    return-object v1

    :direct_completion
    move-object v4, p0
    move p0, p1
    move p1, p2

    :resume_join
    move p2, p1
    move p1, p0
    move-object p0, v4

    :consume
    int-to-long v4, p1
    invoke-interface {p0}, Lio/ktor/utils/io/ByteReadChannel;->getReadBuffer()Lkotlinx/io/Source;
    move-result-object v6
    invoke-static {v6}, Lio/ktor/utils/io/core/ByteReadPacketKt;->getRemaining(Lkotlinx/io/Source;)J
    move-result-wide v6
    invoke-static {v4, v5, v6, v7}, Ljava/lang/Math;->min(JJ)J
    move-result-wide v4
    invoke-interface {p0}, Lio/ktor/utils/io/ByteReadChannel;->getReadBuffer()Lkotlinx/io/Source;
    move-result-object v6
    invoke-interface {v6, v2, v4, v5}, Lkotlinx/io/Source;->readTo(Lkotlinx/io/RawSink;J)V
    long-to-int v4, v4
    sub-int/2addr p1, v4
    goto :loop_header

    :done
    return-object v2
.end method
