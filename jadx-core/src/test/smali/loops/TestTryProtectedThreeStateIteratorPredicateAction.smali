.class public final Lloops/TestTryProtectedThreeStateIteratorPredicateAction;
.super Lkotlin/coroutines/jvm/internal/SuspendLambda;

.field private L$0:Ljava/lang/Object;
.field private L$1:Ljava/lang/Object;
.field private L$2:Ljava/lang/Object;
.field private L$3:Ljava/lang/Object;
.field private label:I

.method public constructor <init>(Lkotlin/coroutines/Continuation;)V
    .locals 1

    const/4 v0, 0x2
    invoke-direct {p0, v0, p1}, Lkotlin/coroutines/jvm/internal/SuspendLambda;-><init>(ILkotlin/coroutines/Continuation;)V
    return-void
.end method

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .locals 12

    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v0
    iget v1, p0, Lloops/TestTryProtectedThreeStateIteratorPredicateAction;->label:I
    const/4 v2, 0x1
    const/4 v3, 0x2
    const/4 v4, 0x3

    :try_start
    if-eqz v1, :initial
    if-eq v1, v2, :resume_outer_has_next
    if-eq v1, v3, :resume_paired_has_next
    if-ne v1, v4, :bad_state

    :resume_send
    iget-object v5, p0, Lloops/TestTryProtectedThreeStateIteratorPredicateAction;->L$0:Ljava/lang/Object;
    check-cast v5, Lkotlinx/coroutines/channels/SendChannel;
    iget-object v6, p0, Lloops/TestTryProtectedThreeStateIteratorPredicateAction;->L$1:Ljava/lang/Object;
    check-cast v6, Lkotlinx/coroutines/channels/ChannelIterator;
    iget-object v7, p0, Lloops/TestTryProtectedThreeStateIteratorPredicateAction;->L$2:Ljava/lang/Object;
    check-cast v7, Lkotlinx/coroutines/channels/ChannelIterator;
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :outer_header

    :resume_paired_has_next
    iget-object v8, p0, Lloops/TestTryProtectedThreeStateIteratorPredicateAction;->L$0:Ljava/lang/Object;
    check-cast v8, Lkotlinx/coroutines/channels/SendChannel;
    iget-object v9, p0, Lloops/TestTryProtectedThreeStateIteratorPredicateAction;->L$1:Ljava/lang/Object;
    check-cast v9, Lkotlinx/coroutines/channels/ChannelIterator;
    iget-object v10, p0, Lloops/TestTryProtectedThreeStateIteratorPredicateAction;->L$2:Ljava/lang/Object;
    check-cast v10, Lkotlinx/coroutines/channels/ChannelIterator;
    iget-object v11, p0, Lloops/TestTryProtectedThreeStateIteratorPredicateAction;->L$3:Ljava/lang/Object;
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :paired_result

    :resume_outer_has_next
    iget-object v5, p0, Lloops/TestTryProtectedThreeStateIteratorPredicateAction;->L$0:Ljava/lang/Object;
    check-cast v5, Lkotlinx/coroutines/channels/SendChannel;
    iget-object v6, p0, Lloops/TestTryProtectedThreeStateIteratorPredicateAction;->L$1:Ljava/lang/Object;
    check-cast v6, Lkotlinx/coroutines/channels/ChannelIterator;
    iget-object v7, p0, Lloops/TestTryProtectedThreeStateIteratorPredicateAction;->L$2:Ljava/lang/Object;
    check-cast v7, Lkotlinx/coroutines/channels/ChannelIterator;
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :outer_result

    :bad_state
    new-instance p1, Ljava/lang/IllegalStateException;
    invoke-direct {p1}, Ljava/lang/IllegalStateException;-><init>()V
    throw p1

    :initial
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    const/4 v5, 0x0
    check-cast v5, Lkotlinx/coroutines/channels/SendChannel;
    const/4 v6, 0x0
    check-cast v6, Lkotlinx/coroutines/channels/ChannelIterator;
    const/4 v7, 0x0
    check-cast v7, Lkotlinx/coroutines/channels/ChannelIterator;

    :outer_header
    iput-object v5, p0, Lloops/TestTryProtectedThreeStateIteratorPredicateAction;->L$0:Ljava/lang/Object;
    iput-object v6, p0, Lloops/TestTryProtectedThreeStateIteratorPredicateAction;->L$1:Ljava/lang/Object;
    iput-object v7, p0, Lloops/TestTryProtectedThreeStateIteratorPredicateAction;->L$2:Ljava/lang/Object;
    const/4 v1, 0x0
    iput-object v1, p0, Lloops/TestTryProtectedThreeStateIteratorPredicateAction;->L$3:Ljava/lang/Object;
    iput v2, p0, Lloops/TestTryProtectedThreeStateIteratorPredicateAction;->label:I
    invoke-interface {v6, p0}, Lkotlinx/coroutines/channels/ChannelIterator;->hasNext(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    goto :outer_suspend_check

    :outer_suspend_check
    if-eq p1, v0, :suspended

    :outer_result
    check-cast p1, Ljava/lang/Boolean;
    invoke-virtual {p1}, Ljava/lang/Boolean;->booleanValue()Z
    move-result p1
    if-eqz p1, :done
    invoke-interface {v6}, Lkotlinx/coroutines/channels/ChannelIterator;->next()Ljava/lang/Object;
    move-result-object v11

    iput-object v5, p0, Lloops/TestTryProtectedThreeStateIteratorPredicateAction;->L$0:Ljava/lang/Object;
    iput-object v6, p0, Lloops/TestTryProtectedThreeStateIteratorPredicateAction;->L$1:Ljava/lang/Object;
    iput-object v7, p0, Lloops/TestTryProtectedThreeStateIteratorPredicateAction;->L$2:Ljava/lang/Object;
    iput-object v11, p0, Lloops/TestTryProtectedThreeStateIteratorPredicateAction;->L$3:Ljava/lang/Object;
    iput v3, p0, Lloops/TestTryProtectedThreeStateIteratorPredicateAction;->label:I
    invoke-interface {v7, p0}, Lkotlinx/coroutines/channels/ChannelIterator;->hasNext(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    goto :paired_suspend_check

    :paired_suspend_check
    if-eq p1, v0, :suspended

    move-object v8, v5
    move-object v9, v6
    move-object v10, v7

    :paired_result
    check-cast p1, Ljava/lang/Boolean;
    invoke-virtual {p1}, Ljava/lang/Boolean;->booleanValue()Z
    move-result p1
    if-eqz p1, :shared_latch

    invoke-interface {v10}, Lkotlinx/coroutines/channels/ChannelIterator;->next()Ljava/lang/Object;
    move-result-object v11
    iput-object v8, p0, Lloops/TestTryProtectedThreeStateIteratorPredicateAction;->L$0:Ljava/lang/Object;
    iput-object v9, p0, Lloops/TestTryProtectedThreeStateIteratorPredicateAction;->L$1:Ljava/lang/Object;
    iput-object v10, p0, Lloops/TestTryProtectedThreeStateIteratorPredicateAction;->L$2:Ljava/lang/Object;
    const/4 v1, 0x0
    iput-object v1, p0, Lloops/TestTryProtectedThreeStateIteratorPredicateAction;->L$3:Ljava/lang/Object;
    iput v4, p0, Lloops/TestTryProtectedThreeStateIteratorPredicateAction;->label:I
    invoke-interface {v8, v11, p0}, Lkotlinx/coroutines/channels/SendChannel;->send(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    goto :send_suspend_check

    :send_suspend_check
    if-eq p1, v0, :suspended

    :shared_latch
    move-object v5, v8
    move-object v6, v9
    move-object v7, v10
    goto :outer_header

    :done
    sget-object p1, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    :try_end
    return-object p1

    :suspended
    return-object v0

    :catchall
    move-exception p1
    throw p1

    .catchall {:try_start .. :try_end} :catchall
.end method
