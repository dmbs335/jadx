.class public Lio/ktor/network/sockets/CIOReaderKt$attachForReadingDirectImpl$1;
.super Lkotlin/coroutines/jvm/internal/SuspendLambda;

.field private $channel:Lio/ktor/utils/io/ByteChannel;
.field private $timeout:Lio/ktor/network/util/Timeout;
.field private label:I

.method private static recordState(I)V
    .locals 0
    return-void
.end method

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .locals 4

    iget v0, p0, Lio/ktor/network/sockets/CIOReaderKt$attachForReadingDirectImpl$1;->label:I
    packed-switch v0, :state_switch

    new-instance v0, Ljava/lang/IllegalStateException;
    invoke-direct {v0}, Ljava/lang/IllegalStateException;-><init>()V
    throw v0

    :state_zero
    const/4 v0, 0x0
    invoke-static {v0}, Lio/ktor/network/sockets/CIOReaderKt$attachForReadingDirectImpl$1;->recordState(I)V
    iget-object v1, p0, Lio/ktor/network/sockets/CIOReaderKt$attachForReadingDirectImpl$1;->$timeout:Lio/ktor/network/util/Timeout;
    invoke-virtual {v1}, Lio/ktor/network/util/Timeout;->stop()V
    goto :timeout_join

    :state_one
    const/4 v0, 0x1
    invoke-static {v0}, Lio/ktor/network/sockets/CIOReaderKt$attachForReadingDirectImpl$1;->recordState(I)V
    const/4 v1, 0x0
    goto :timeout_join

    :state_two
    const/4 v0, 0x2
    invoke-static {v0}, Lio/ktor/network/sockets/CIOReaderKt$attachForReadingDirectImpl$1;->recordState(I)V
    return-object p1

    :state_three
    const/4 v0, 0x3
    invoke-static {v0}, Lio/ktor/network/sockets/CIOReaderKt$attachForReadingDirectImpl$1;->recordState(I)V
    return-object p1

    :state_four
    const/4 v0, 0x4
    invoke-static {v0}, Lio/ktor/network/sockets/CIOReaderKt$attachForReadingDirectImpl$1;->recordState(I)V
    iget-object v1, p0, Lio/ktor/network/sockets/CIOReaderKt$attachForReadingDirectImpl$1;->$timeout:Lio/ktor/network/util/Timeout;
    goto :timeout_join

    :state_five
    const/4 v0, 0x5
    invoke-static {v0}, Lio/ktor/network/sockets/CIOReaderKt$attachForReadingDirectImpl$1;->recordState(I)V
    const/4 v1, 0x0
    goto :timeout_join

    :state_six
    const/4 v0, 0x6
    invoke-static {v0}, Lio/ktor/network/sockets/CIOReaderKt$attachForReadingDirectImpl$1;->recordState(I)V
    return-object p1

    :state_seven
    const/4 v0, 0x7
    invoke-static {v0}, Lio/ktor/network/sockets/CIOReaderKt$attachForReadingDirectImpl$1;->recordState(I)V
    return-object p1

    :state_eight
    const/16 v0, 0x8
    invoke-static {v0}, Lio/ktor/network/sockets/CIOReaderKt$attachForReadingDirectImpl$1;->recordState(I)V
    iget-object v1, p0, Lio/ktor/network/sockets/CIOReaderKt$attachForReadingDirectImpl$1;->$timeout:Lio/ktor/network/util/Timeout;

    :timeout_join
    iget-object v2, p0, Lio/ktor/network/sockets/CIOReaderKt$attachForReadingDirectImpl$1;->$channel:Lio/ktor/utils/io/ByteChannel;
    invoke-virtual {v2}, Lio/ktor/utils/io/ByteChannel;->isClosedForWrite()Z
    move-result v3
    if-eqz v3, :open_return

    if-eqz v1, :finish_done
    invoke-virtual {v1}, Lio/ktor/network/util/Timeout;->finish()V

    :finish_done
    invoke-virtual {v2}, Lio/ktor/utils/io/ByteChannel;->close()V

    :open_return
    return-object p1

    :state_switch
    .packed-switch 0x0
        :state_zero
        :state_one
        :state_two
        :state_three
        :state_four
        :state_five
        :state_six
        :state_seven
        :state_eight
    .end packed-switch
.end method
