.class public Lio/ktor/network/sockets/CIOReaderKt$attachForReadingImpl$1;
.super Lkotlin/coroutines/jvm/internal/SuspendLambda;

.field private $channel:Ljava/lang/Object;
.field private $nioChannel:Ljava/lang/Object;
.field private $selectable:Ljava/lang/Object;
.field private $selector:Ljava/lang/Object;
.field private label:I
.field private rounds:I

.method private static suspendCall(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 1
    const/4 v0, 0x0
    return-object v0
.end method

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .locals 9

    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v0
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
    iget-object v8, p0, Lio/ktor/network/sockets/CIOReaderKt$attachForReadingImpl$1;->$channel:Ljava/lang/Object;
    check-cast v8, Ljava/lang/Object;
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :after_five

    :state_four
    iget-object v8, p0, Lio/ktor/network/sockets/CIOReaderKt$attachForReadingImpl$1;->$selector:Ljava/lang/Object;
    check-cast v8, Ljava/lang/Object;
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :after_four

    :state_three
    iget-object v8, p0, Lio/ktor/network/sockets/CIOReaderKt$attachForReadingImpl$1;->$selectable:Ljava/lang/Object;
    check-cast v8, Ljava/lang/Object;
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :after_three

    :state_two
    iget-object v8, p0, Lio/ktor/network/sockets/CIOReaderKt$attachForReadingImpl$1;->$nioChannel:Ljava/lang/Object;
    check-cast v8, Ljava/lang/Object;
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :after_two

    :state_one
    iget-object v8, p0, Lio/ktor/network/sockets/CIOReaderKt$attachForReadingImpl$1;->$channel:Ljava/lang/Object;
    check-cast v8, Ljava/lang/Object;
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :after_one

    :state_zero
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V

    :call_one
    iput v2, p0, Lio/ktor/network/sockets/CIOReaderKt$attachForReadingImpl$1;->label:I
    invoke-static {v2, p0}, Lio/ktor/network/sockets/CIOReaderKt$attachForReadingImpl$1;->suspendCall(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v7
    if-eq v7, v0, :suspended
    move-object p1, v7
    const/4 v8, 0x0

    :after_one
    iput v3, p0, Lio/ktor/network/sockets/CIOReaderKt$attachForReadingImpl$1;->label:I
    invoke-static {v3, p0}, Lio/ktor/network/sockets/CIOReaderKt$attachForReadingImpl$1;->suspendCall(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v7
    if-eq v7, v0, :suspended
    move-object p1, v7
    const/4 v8, 0x0

    :after_two
    iput v4, p0, Lio/ktor/network/sockets/CIOReaderKt$attachForReadingImpl$1;->label:I
    invoke-static {v4, p0}, Lio/ktor/network/sockets/CIOReaderKt$attachForReadingImpl$1;->suspendCall(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v7
    if-eq v7, v0, :suspended
    move-object p1, v7
    const/4 v8, 0x0

    :after_three
    iput v5, p0, Lio/ktor/network/sockets/CIOReaderKt$attachForReadingImpl$1;->label:I
    invoke-static {v5, p0}, Lio/ktor/network/sockets/CIOReaderKt$attachForReadingImpl$1;->suspendCall(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v7
    if-eq v7, v0, :suspended
    move-object p1, v7
    const/4 v8, 0x0

    :after_four
    iput v6, p0, Lio/ktor/network/sockets/CIOReaderKt$attachForReadingImpl$1;->label:I
    invoke-static {v6, p0}, Lio/ktor/network/sockets/CIOReaderKt$attachForReadingImpl$1;->suspendCall(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v7
    if-eq v7, v0, :suspended
    move-object p1, v7
    const/4 v8, 0x0

    :after_five
    iget v8, p0, Lio/ktor/network/sockets/CIOReaderKt$attachForReadingImpl$1;->rounds:I
    add-int/lit8 v8, v8, -0x1
    iput v8, p0, Lio/ktor/network/sockets/CIOReaderKt$attachForReadingImpl$1;->rounds:I
    if-gez v8, :call_one
    return-object p1

    :suspended
    return-object v0

    :bad_state
    new-instance v8, Ljava/lang/IllegalStateException;
    invoke-direct {v8}, Ljava/lang/IllegalStateException;-><init>()V
    throw v8
.end method
