.class public final Lloops/TestTryProtectedChannelIteratorTopSplitter;
.super Lkotlin/coroutines/jvm/internal/SuspendLambda;

.field private L$0:Ljava/lang/Object;
.field private L$1:Ljava/lang/Object;
.field private label:I

.method public constructor <init>(Lkotlin/coroutines/Continuation;)V
    .registers 3
    const/4 v0, 0x2
    invoke-direct {p0, v0, p1}, Lkotlin/coroutines/jvm/internal/SuspendLambda;-><init>(ILkotlin/coroutines/Continuation;)V
    return-void
.end method

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .locals 8

    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v0
    iget v1, p0, Lloops/TestTryProtectedChannelIteratorTopSplitter;->label:I
    const/4 v2, 0x1

    if-eqz v1, :initial
    if-ne v1, v2, :bad_state
    iget-object v3, p0, Lloops/TestTryProtectedChannelIteratorTopSplitter;->L$0:Ljava/lang/Object;
    check-cast v3, Lkotlinx/coroutines/channels/ReceiveChannel;
    iget-object v4, p0, Lloops/TestTryProtectedChannelIteratorTopSplitter;->L$1:Ljava/lang/Object;
    check-cast v4, Lkotlinx/coroutines/channels/ChannelIterator;
    :try_start_resume
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    :try_end_resume
    .catchall {:try_start_resume .. :try_end_resume} :resume_cleanup
    move-object v5, p1
    goto :result_join

    :bad_state
    new-instance v1, Ljava/lang/IllegalStateException;
    invoke-direct {v1}, Ljava/lang/IllegalStateException;-><init>()V
    throw v1

    :initial
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    const/4 v3, 0x0
    const/4 v4, 0x0

    :suspend_call
    :try_start_body
    iput-object v3, p0, Lloops/TestTryProtectedChannelIteratorTopSplitter;->L$0:Ljava/lang/Object;
    iput-object v4, p0, Lloops/TestTryProtectedChannelIteratorTopSplitter;->L$1:Ljava/lang/Object;
    iput v2, p0, Lloops/TestTryProtectedChannelIteratorTopSplitter;->label:I
    invoke-interface {v4, p0}, Lkotlinx/coroutines/channels/ChannelIterator;->hasNext(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v5
    if-ne v5, v0, :result_join
    return-object v0

    :result_join
    check-cast v5, Ljava/lang/Boolean;
    invoke-virtual {v5}, Ljava/lang/Boolean;->booleanValue()Z
    move-result v6
    if-eqz v6, :done
    invoke-interface {v4}, Lkotlinx/coroutines/channels/ChannelIterator;->next()Ljava/lang/Object;
    goto :suspend_call

    :done
    sget-object v5, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    const/4 v6, 0x0
    invoke-static {v3, v6}, Lkotlinx/coroutines/channels/ChannelsKt;->cancelConsumed(Lkotlinx/coroutines/channels/ReceiveChannel;Ljava/lang/Throwable;)V
    :try_end_body
    .catchall {:try_start_body .. :try_end_body} :body_cleanup
    return-object v5

    :resume_cleanup
    move-exception v7
    invoke-static {v3, v7}, Lkotlinx/coroutines/channels/ChannelsKt;->cancelConsumed(Lkotlinx/coroutines/channels/ReceiveChannel;Ljava/lang/Throwable;)V
    throw v7

    :body_cleanup
    move-exception v7
    invoke-static {v3, v7}, Lkotlinx/coroutines/channels/ChannelsKt;->cancelConsumed(Lkotlinx/coroutines/channels/ReceiveChannel;Ljava/lang/Throwable;)V
    throw v7
.end method
