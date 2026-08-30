.class public Lio/ktor/network/sockets/CIOReaderKt$attachForReadingImpl$1;
.super Lkotlin/coroutines/jvm/internal/SuspendLambda;

.field private $channel:Ljava/lang/Object;
.field private $nioChannel:Ljava/lang/Object;
.field private $selectable:Ljava/lang/Object;
.field private $selector:Ljava/lang/Object;
.field private label:I
.field private readResult:I
.field private timeout:Lio/ktor/network/util/Timeout;

.method private static suspendCall(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 1
    const/4 v0, 0x0
    return-object v0
.end method

.method private static recycle()V
    .locals 0
    return-void
.end method

.method private static recordMode(I)V
    .locals 0
    return-void
.end method

.method private static shutdownInput()V
    .locals 0
    return-void
.end method

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .locals 8

    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v0

    :dispatch
    iget v1, p0, Lio/ktor/network/sockets/CIOReaderKt$attachForReadingImpl$1;->label:I
    const/4 v2, 0x1
    const/4 v3, 0x2
    const/4 v4, 0x3
    const/4 v5, 0x4
    const/4 v6, 0x5
    if-eqz v1, :state_zero
    if-eq v1, v2, :state_one
    if-eq v1, v3, :state_two
    if-eq v1, v4, :state_three
    if-eq v1, v5, :state_four
    if-ne v1, v6, :bad_state

    :state_five
    iget-object v7, p0, Lio/ktor/network/sockets/CIOReaderKt$attachForReadingImpl$1;->$channel:Ljava/lang/Object;
    check-cast v7, Ljava/lang/Object;
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    const/4 v7, -0x1
    iput v7, p0, Lio/ktor/network/sockets/CIOReaderKt$attachForReadingImpl$1;->readResult:I
    goto :read_complete

    :state_four
    iget-object v7, p0, Lio/ktor/network/sockets/CIOReaderKt$attachForReadingImpl$1;->$selector:Ljava/lang/Object;
    check-cast v7, Ljava/lang/Object;
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :after_four

    :state_three
    iget-object v7, p0, Lio/ktor/network/sockets/CIOReaderKt$attachForReadingImpl$1;->$selectable:Ljava/lang/Object;
    check-cast v7, Ljava/lang/Object;
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :after_three

    :state_two
    iget-object v7, p0, Lio/ktor/network/sockets/CIOReaderKt$attachForReadingImpl$1;->$nioChannel:Ljava/lang/Object;
    check-cast v7, Ljava/lang/Object;
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :after_two

    :state_one
    iget-object v7, p0, Lio/ktor/network/sockets/CIOReaderKt$attachForReadingImpl$1;->$channel:Ljava/lang/Object;
    check-cast v7, Ljava/lang/Object;
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :after_one

    :state_zero
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    iput v2, p0, Lio/ktor/network/sockets/CIOReaderKt$attachForReadingImpl$1;->label:I
    invoke-static {v2, p0}, Lio/ktor/network/sockets/CIOReaderKt$attachForReadingImpl$1;->suspendCall(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v7
    if-eq v7, v0, :suspended
    move-object p1, v7

    :after_one
    iput v3, p0, Lio/ktor/network/sockets/CIOReaderKt$attachForReadingImpl$1;->label:I
    invoke-static {v3, p0}, Lio/ktor/network/sockets/CIOReaderKt$attachForReadingImpl$1;->suspendCall(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v7
    if-eq v7, v0, :suspended
    move-object p1, v7

    :after_two
    iput v4, p0, Lio/ktor/network/sockets/CIOReaderKt$attachForReadingImpl$1;->label:I
    invoke-static {v4, p0}, Lio/ktor/network/sockets/CIOReaderKt$attachForReadingImpl$1;->suspendCall(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v7
    if-eq v7, v0, :suspended
    move-object p1, v7

    :after_three
    iput v5, p0, Lio/ktor/network/sockets/CIOReaderKt$attachForReadingImpl$1;->label:I
    invoke-static {v5, p0}, Lio/ktor/network/sockets/CIOReaderKt$attachForReadingImpl$1;->suspendCall(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v7
    if-eq v7, v0, :suspended
    move-object p1, v7

    :after_four
    const/4 v7, 0x0
    iput v7, p0, Lio/ktor/network/sockets/CIOReaderKt$attachForReadingImpl$1;->readResult:I

    :read_complete
    iget-object v7, p0, Lio/ktor/network/sockets/CIOReaderKt$attachForReadingImpl$1;->timeout:Lio/ktor/network/util/Timeout;
    if-eqz v7, :without_timeout
    invoke-virtual {v7}, Lio/ktor/network/util/Timeout;->stop()V
    const/4 v6, 0x1
    goto :cleanup_join

    :without_timeout
    const/4 v6, 0x0

    :cleanup_join
    invoke-static {v6}, Lio/ktor/network/sockets/CIOReaderKt$attachForReadingImpl$1;->recordMode(I)V
    iget v7, p0, Lio/ktor/network/sockets/CIOReaderKt$attachForReadingImpl$1;->readResult:I
    const/4 v6, -0x1
    if-ne v7, v6, :read_more
    iget-object v7, p0, Lio/ktor/network/sockets/CIOReaderKt$attachForReadingImpl$1;->timeout:Lio/ktor/network/util/Timeout;
    if-eqz v7, :recycle
    invoke-virtual {v7}, Lio/ktor/network/util/Timeout;->finish()V

    :recycle
    invoke-static {}, Lio/ktor/network/sockets/CIOReaderKt$attachForReadingImpl$1;->recycle()V
    invoke-static {}, Lio/ktor/network/sockets/CIOReaderKt$attachForReadingImpl$1;->shutdownInput()V
    return-object p1

    :read_more
    const/4 v6, 0x5
    iput v6, p0, Lio/ktor/network/sockets/CIOReaderKt$attachForReadingImpl$1;->label:I
    invoke-static {v6, p0}, Lio/ktor/network/sockets/CIOReaderKt$attachForReadingImpl$1;->suspendCall(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v7
    if-eq v7, v0, :suspended
    move-object p1, v7
    goto :dispatch

    :suspended
    return-object v0

    :bad_state
    new-instance v7, Ljava/lang/IllegalStateException;
    invoke-direct {v7}, Ljava/lang/IllegalStateException;-><init>()V
    throw v7
.end method
