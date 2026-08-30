.class public final Lloops/TestCoroutineChannelFrameLoopReentry;
.super Lkotlin/coroutines/jvm/internal/SuspendLambda;

.field private channel:Lkotlinx/coroutines/channels/Channel;
.field private magnifier:Ltest/PlatformMagnifier;
.field private label:I

.method public constructor <init>(Lkotlin/coroutines/Continuation;)V
    .registers 3
    const/4 v0, 0x2
    invoke-direct {p0, v0, p1}, Lkotlin/coroutines/jvm/internal/SuspendLambda;-><init>(ILkotlin/coroutines/Continuation;)V
    return-void
.end method

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .locals 7

    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v0
    iget v1, p0, Lloops/TestCoroutineChannelFrameLoopReentry;->label:I
    if-eqz v1, :state_zero
    const/4 v4, 0x1
    if-eq v1, v4, :state_one
    const/4 v4, 0x2
    if-ne v1, v4, :bad_state

    :state_two
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :state_two_result

    :state_one
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :post_receive

    :state_zero
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :receive_header

    :state_two_result
    iget-object v2, p0, Lloops/TestCoroutineChannelFrameLoopReentry;->magnifier:Ltest/PlatformMagnifier;
    if-eqz v2, :receive_header
    invoke-interface {v2}, Ltest/PlatformMagnifier;->updateContent()V
    goto :receive_header

    :receive_header
    iget-object v2, p0, Lloops/TestCoroutineChannelFrameLoopReentry;->channel:Lkotlinx/coroutines/channels/Channel;
    if-eqz v2, :post_receive
    const/4 v4, 0x1
    iput v4, p0, Lloops/TestCoroutineChannelFrameLoopReentry;->label:I
    invoke-interface {v2, p0}, Lkotlinx/coroutines/channels/ReceiveChannel;->receive(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v3
    if-ne v3, v0, :post_receive
    return-object v0

    :post_receive
    iget-object v2, p0, Lloops/TestCoroutineChannelFrameLoopReentry;->magnifier:Ltest/PlatformMagnifier;
    if-eqz v2, :receive_header
    const/4 v5, 0x0
    const/4 v4, 0x2
    iput v4, p0, Lloops/TestCoroutineChannelFrameLoopReentry;->label:I
    invoke-static {v5, p0}, Landroidx/compose/runtime/MonotonicFrameClockKt;->withFrameMillis(Lkotlin/jvm/functions/Function1;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v3
    if-ne v3, v0, :state_two_result
    return-object v0

    :bad_state
    new-instance v6, Ljava/lang/IllegalStateException;
    invoke-direct {v6}, Ljava/lang/IllegalStateException;-><init>()V
    throw v6
.end method
