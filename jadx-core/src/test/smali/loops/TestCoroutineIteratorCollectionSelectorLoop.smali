.class public final Lloops/TestCoroutineIteratorCollectionSelectorLoop;
.super Lkotlin/coroutines/jvm/internal/SuspendLambda;

.field private selector:Lkotlin/jvm/functions/Function2;
.field private channel:Lkotlinx/coroutines/channels/ReceiveChannel;
.field private L$0:Ljava/lang/Object;
.field private L$1:Ljava/lang/Object;
.field private L$2:Ljava/lang/Object;
.field private L$3:Ljava/lang/Object;
.field private label:I

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 12

    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v0

    iget v1, p0, Lloops/TestCoroutineIteratorCollectionSelectorLoop;->label:I
    const/4 v2, 0x3
    const/4 v3, 0x2
    const/4 v4, 0x1

    if-eqz v1, :initial
    if-eq v1, v4, :resume_has_next
    if-eq v1, v3, :resume_selector
    if-ne v1, v2, :bad_state

    :resume_send
    iget-object v1, p0, Lloops/TestCoroutineIteratorCollectionSelectorLoop;->L$3:Ljava/lang/Object;
    iget-object v5, p0, Lloops/TestCoroutineIteratorCollectionSelectorLoop;->L$2:Ljava/lang/Object;
    check-cast v5, Lkotlinx/coroutines/channels/ChannelIterator;
    iget-object v6, p0, Lloops/TestCoroutineIteratorCollectionSelectorLoop;->L$1:Ljava/lang/Object;
    check-cast v6, Ljava/util/HashSet;
    iget-object v7, p0, Lloops/TestCoroutineIteratorCollectionSelectorLoop;->L$0:Ljava/lang/Object;
    check-cast v7, Lkotlinx/coroutines/channels/ProducerScope;
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :add_key

    :bad_state
    new-instance p1, Ljava/lang/IllegalStateException;
    invoke-direct {p1}, Ljava/lang/IllegalStateException;-><init>()V
    throw p1

    :resume_selector
    iget-object v1, p0, Lloops/TestCoroutineIteratorCollectionSelectorLoop;->L$3:Ljava/lang/Object;
    iget-object v5, p0, Lloops/TestCoroutineIteratorCollectionSelectorLoop;->L$2:Ljava/lang/Object;
    check-cast v5, Lkotlinx/coroutines/channels/ChannelIterator;
    iget-object v6, p0, Lloops/TestCoroutineIteratorCollectionSelectorLoop;->L$1:Ljava/lang/Object;
    check-cast v6, Ljava/util/HashSet;
    iget-object v7, p0, Lloops/TestCoroutineIteratorCollectionSelectorLoop;->L$0:Ljava/lang/Object;
    check-cast v7, Lkotlinx/coroutines/channels/ProducerScope;
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    move-object v9, v5
    move-object v5, v1
    move-object v1, v9
    goto :contains_key

    :resume_has_next
    iget-object v1, p0, Lloops/TestCoroutineIteratorCollectionSelectorLoop;->L$2:Ljava/lang/Object;
    check-cast v1, Lkotlinx/coroutines/channels/ChannelIterator;
    iget-object v5, p0, Lloops/TestCoroutineIteratorCollectionSelectorLoop;->L$1:Ljava/lang/Object;
    check-cast v5, Ljava/util/HashSet;
    iget-object v6, p0, Lloops/TestCoroutineIteratorCollectionSelectorLoop;->L$0:Ljava/lang/Object;
    check-cast v6, Lkotlinx/coroutines/channels/ProducerScope;
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :has_next_result

    :initial
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    iget-object p1, p0, Lloops/TestCoroutineIteratorCollectionSelectorLoop;->L$0:Ljava/lang/Object;
    check-cast p1, Lkotlinx/coroutines/channels/ProducerScope;
    new-instance v1, Ljava/util/HashSet;
    invoke-direct {v1}, Ljava/util/HashSet;-><init>()V
    iget-object v5, p0, Lloops/TestCoroutineIteratorCollectionSelectorLoop;->channel:Lkotlinx/coroutines/channels/ReceiveChannel;
    invoke-interface {v5}, Lkotlinx/coroutines/channels/ReceiveChannel;->iterator()Lkotlinx/coroutines/channels/ChannelIterator;
    move-result-object v5
    move-object v6, v5
    move-object v5, v1
    move-object v1, v6
    move-object v6, p1

    :iterator_loop
    iput-object v6, p0, Lloops/TestCoroutineIteratorCollectionSelectorLoop;->L$0:Ljava/lang/Object;
    iput-object v5, p0, Lloops/TestCoroutineIteratorCollectionSelectorLoop;->L$1:Ljava/lang/Object;
    iput-object v1, p0, Lloops/TestCoroutineIteratorCollectionSelectorLoop;->L$2:Ljava/lang/Object;
    const/4 p1, 0x0
    iput-object p1, p0, Lloops/TestCoroutineIteratorCollectionSelectorLoop;->L$3:Ljava/lang/Object;
    iput v4, p0, Lloops/TestCoroutineIteratorCollectionSelectorLoop;->label:I
    invoke-interface {v1, p0}, Lkotlinx/coroutines/channels/ChannelIterator;->hasNext(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-ne p1, v0, :has_next_result
    goto :suspended

    :has_next_result
    check-cast p1, Ljava/lang/Boolean;
    invoke-virtual {p1}, Ljava/lang/Boolean;->booleanValue()Z
    move-result p1
    if-eqz p1, :done

    invoke-interface {v1}, Lkotlinx/coroutines/channels/ChannelIterator;->next()Ljava/lang/Object;
    move-result-object p1
    iget-object v7, p0, Lloops/TestCoroutineIteratorCollectionSelectorLoop;->selector:Lkotlin/jvm/functions/Function2;
    iput-object v6, p0, Lloops/TestCoroutineIteratorCollectionSelectorLoop;->L$0:Ljava/lang/Object;
    iput-object v5, p0, Lloops/TestCoroutineIteratorCollectionSelectorLoop;->L$1:Ljava/lang/Object;
    iput-object v1, p0, Lloops/TestCoroutineIteratorCollectionSelectorLoop;->L$2:Ljava/lang/Object;
    iput-object p1, p0, Lloops/TestCoroutineIteratorCollectionSelectorLoop;->L$3:Ljava/lang/Object;
    iput v3, p0, Lloops/TestCoroutineIteratorCollectionSelectorLoop;->label:I
    invoke-interface {v7, p1, p0}, Lkotlin/jvm/functions/Function2;->invoke(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v7
    if-ne v7, v0, :selector_result
    goto :suspended

    :selector_result
    move-object v9, v5
    move-object v5, p1
    move-object p1, v7
    move-object v7, v6
    move-object v6, v9

    :contains_key
    invoke-virtual {v6, p1}, Ljava/util/HashSet;->contains(Ljava/lang/Object;)Z
    move-result v8
    if-nez v8, :next_iteration

    iput-object v7, p0, Lloops/TestCoroutineIteratorCollectionSelectorLoop;->L$0:Ljava/lang/Object;
    iput-object v6, p0, Lloops/TestCoroutineIteratorCollectionSelectorLoop;->L$1:Ljava/lang/Object;
    iput-object v1, p0, Lloops/TestCoroutineIteratorCollectionSelectorLoop;->L$2:Ljava/lang/Object;
    iput-object p1, p0, Lloops/TestCoroutineIteratorCollectionSelectorLoop;->L$3:Ljava/lang/Object;
    iput v2, p0, Lloops/TestCoroutineIteratorCollectionSelectorLoop;->label:I
    invoke-interface {v7, v5, p0}, Lkotlinx/coroutines/channels/SendChannel;->send(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v5
    if-ne v5, v0, :send_result

    :suspended
    return-object v0

    :send_result
    move-object v5, v1
    move-object v1, p1

    :add_key
    invoke-interface {v6, v1}, Ljava/util/Collection;->add(Ljava/lang/Object;)Z
    move-object v1, v5

    :next_iteration
    move-object v5, v6
    move-object v6, v7
    goto :iterator_loop

    :done
    sget-object p1, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object p1
.end method
