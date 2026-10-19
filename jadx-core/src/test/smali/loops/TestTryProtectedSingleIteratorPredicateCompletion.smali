.class public final Lloops/TestTryProtectedSingleIteratorPredicateCompletion;
.super Lkotlin/coroutines/jvm/internal/SuspendLambda;

.field private channel:Lkotlinx/coroutines/channels/ReceiveChannel;
.field private L$0:Ljava/lang/Object;
.field private L$1:Ljava/lang/Object;
.field private label:I

.method private static consume(Ljava/lang/Object;)V
    .locals 0
    return-void
.end method

.method public final P(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 8

    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v0
    iget v1, p0, Lloops/TestTryProtectedSingleIteratorPredicateCompletion;->label:I
    const/4 v2, 0x1
    :try_start
    if-eqz v1, :initial
    if-ne v1, v2, :bad_state

    iget-object v3, p0, Lloops/TestTryProtectedSingleIteratorPredicateCompletion;->L$1:Ljava/lang/Object;
    check-cast v3, Lkotlinx/coroutines/channels/ChannelIterator;
    iget-object v4, p0, Lloops/TestTryProtectedSingleIteratorPredicateCompletion;->L$0:Ljava/lang/Object;
    check-cast v4, Lkotlinx/coroutines/channels/ReceiveChannel;
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :has_next_result

    :bad_state
    new-instance p1, Ljava/lang/IllegalStateException;
    invoke-direct {p1}, Ljava/lang/IllegalStateException;-><init>()V
    throw p1

    :initial
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    iget-object v4, p0, Lloops/TestTryProtectedSingleIteratorPredicateCompletion;->channel:Lkotlinx/coroutines/channels/ReceiveChannel;
    invoke-interface {v4}, Lkotlinx/coroutines/channels/ReceiveChannel;->iterator()Lkotlinx/coroutines/channels/ChannelIterator;
    move-result-object v3

    :loop
    iput-object v4, p0, Lloops/TestTryProtectedSingleIteratorPredicateCompletion;->L$0:Ljava/lang/Object;
    iput-object v3, p0, Lloops/TestTryProtectedSingleIteratorPredicateCompletion;->L$1:Ljava/lang/Object;
    iput v2, p0, Lloops/TestTryProtectedSingleIteratorPredicateCompletion;->label:I
    invoke-interface {v3, p0}, Lkotlinx/coroutines/channels/ChannelIterator;->a(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-ne p1, v0, :has_next_result
    return-object v0

    :has_next_result
    check-cast p1, Ljava/lang/Boolean;
    invoke-virtual {p1}, Ljava/lang/Boolean;->booleanValue()Z
    move-result p1
    if-eqz p1, :done
    invoke-interface {v3}, Lkotlinx/coroutines/channels/ChannelIterator;->next()Ljava/lang/Object;
    move-result-object v5
    invoke-static {v5}, Lloops/TestTryProtectedSingleIteratorPredicateCompletion;->consume(Ljava/lang/Object;)V
    goto :loop

    :done
    sget-object p1, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    :try_end
    return-object p1

    :catchall
    move-exception v6
    invoke-static {v4, v6}, Lkotlinx/coroutines/channels/ChannelsKt;->a(Lkotlinx/coroutines/channels/ReceiveChannel;Ljava/lang/Throwable;)V
    throw v6

    .catchall {:try_start .. :try_end} :catchall
.end method
