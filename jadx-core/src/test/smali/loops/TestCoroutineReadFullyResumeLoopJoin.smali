.class public final Lio/ktor/utils/io/ByteReadChannelOperationsKt;
.super Ljava/lang/Object;

.method public static final readFully(Lio/ktor/utils/io/ByteReadChannel;[BIILkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 12

    instance-of v0, p4, Lio/ktor/utils/io/ByteReadChannelOperationsKt$readFully$1;
    if-eqz v0, :new_continuation
    move-object v0, p4
    check-cast v0, Lio/ktor/utils/io/ByteReadChannelOperationsKt$readFully$1;
    iget v1, v0, Lio/ktor/utils/io/ByteReadChannelOperationsKt$readFully$1;->label:I
    const/high16 v2, -0x80000000
    and-int v3, v1, v2
    if-eqz v3, :new_continuation
    sub-int/2addr v1, v2
    iput v1, v0, Lio/ktor/utils/io/ByteReadChannelOperationsKt$readFully$1;->label:I
    goto :continuation_ready

    :new_continuation
    new-instance v0, Lio/ktor/utils/io/ByteReadChannelOperationsKt$readFully$1;
    invoke-direct {v0, p4}, Lkotlin/coroutines/jvm/internal/ContinuationImpl;-><init>(Lkotlin/coroutines/Continuation;)V

    :continuation_ready
    iget-object p4, v0, Lio/ktor/utils/io/ByteReadChannelOperationsKt$readFully$1;->result:Ljava/lang/Object;
    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v1
    iget v2, v0, Lio/ktor/utils/io/ByteReadChannelOperationsKt$readFully$1;->label:I
    const-string v3, "channel closed"
    const/4 v4, 0x1
    if-eqz v2, :initial
    if-ne v2, v4, :invalid_state

    iget p0, v0, Lio/ktor/utils/io/ByteReadChannelOperationsKt$readFully$1;->I$2:I
    iget p1, v0, Lio/ktor/utils/io/ByteReadChannelOperationsKt$readFully$1;->I$1:I
    iget p2, v0, Lio/ktor/utils/io/ByteReadChannelOperationsKt$readFully$1;->I$0:I
    iget-object p3, v0, Lio/ktor/utils/io/ByteReadChannelOperationsKt$readFully$1;->L$1:Ljava/lang/Object;
    check-cast p3, [B
    iget-object v2, v0, Lio/ktor/utils/io/ByteReadChannelOperationsKt$readFully$1;->L$0:Ljava/lang/Object;
    check-cast v2, Lio/ktor/utils/io/ByteReadChannel;
    invoke-static {p4}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :resume_join

    :invalid_state
    new-instance p0, Ljava/lang/IllegalStateException;
    const-string p1, "call to resume before invoke"
    invoke-direct {p0, p1}, Ljava/lang/IllegalStateException;-><init>(Ljava/lang/String;)V
    throw p0

    :initial
    invoke-static {p4}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    if-le p3, p2, :initial_join
    invoke-interface {p0}, Lio/ktor/utils/io/ByteReadChannel;->isClosedForRead()Z
    move-result p4
    if-eqz p4, :initial_join
    new-instance p0, Ljava/io/EOFException;
    invoke-direct {p0, v3}, Ljava/io/EOFException;-><init>(Ljava/lang/String;)V
    throw p0

    :initial_join
    move p4, p3
    move p3, p2

    :loop_header
    if-ge p2, p4, :done
    invoke-interface {p0}, Lio/ktor/utils/io/ByteReadChannel;->getReadBuffer()Lkotlinx/io/Source;
    move-result-object v2
    invoke-interface {v2}, Lkotlinx/io/Source;->exhausted()Z
    move-result v2
    if-eqz v2, :consume

    iput-object p0, v0, Lio/ktor/utils/io/ByteReadChannelOperationsKt$readFully$1;->L$0:Ljava/lang/Object;
    iput-object p1, v0, Lio/ktor/utils/io/ByteReadChannelOperationsKt$readFully$1;->L$1:Ljava/lang/Object;
    iput p3, v0, Lio/ktor/utils/io/ByteReadChannelOperationsKt$readFully$1;->I$0:I
    iput p4, v0, Lio/ktor/utils/io/ByteReadChannelOperationsKt$readFully$1;->I$1:I
    iput p2, v0, Lio/ktor/utils/io/ByteReadChannelOperationsKt$readFully$1;->I$2:I
    iput v4, v0, Lio/ktor/utils/io/ByteReadChannelOperationsKt$readFully$1;->label:I
    const/4 v2, 0x0
    const/4 v5, 0x0
    invoke-static {p0, v2, v0, v4, v5}, Lio/ktor/utils/io/ByteReadChannel;->awaitContent$default(Lio/ktor/utils/io/ByteReadChannel;ILkotlin/coroutines/Continuation;ILjava/lang/Object;)Ljava/lang/Object;
    move-result-object v2
    if-ne v2, v1, :direct_completion
    return-object v1

    :direct_completion
    move-object v2, p0
    move p0, p2
    move p2, p3
    move-object p3, p1
    move p1, p4

    :resume_join
    move p4, p1
    move-object p1, p3
    move p3, p2
    move p2, p0
    move-object p0, v2

    :consume
    invoke-interface {p0}, Lio/ktor/utils/io/ByteReadChannel;->isClosedForRead()Z
    move-result v2
    if-nez v2, :closed
    sub-int v2, p4, p2
    invoke-interface {p0}, Lio/ktor/utils/io/ByteReadChannel;->getReadBuffer()Lkotlinx/io/Source;
    move-result-object v5
    invoke-static {v5}, Lio/ktor/utils/io/core/ByteReadPacketKt;->getRemaining(Lkotlinx/io/Source;)J
    move-result-wide v5
    long-to-int v5, v5
    invoke-static {v2, v5}, Ljava/lang/Math;->min(II)I
    move-result v2
    invoke-interface {p0}, Lio/ktor/utils/io/ByteReadChannel;->getReadBuffer()Lkotlinx/io/Source;
    move-result-object v5
    add-int/2addr v2, p2
    invoke-static {v5, p1, p2, v2}, Lkotlinx/io/SourcesKt;->readTo(Lkotlinx/io/Source;[BII)V
    move p2, v2
    goto :loop_header

    :closed
    new-instance p0, Ljava/io/EOFException;
    invoke-direct {p0, v3}, Ljava/io/EOFException;-><init>(Ljava/lang/String;)V
    throw p0

    :done
    sget-object p0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object p0
.end method
