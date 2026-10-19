.class public final Lloops/TestCoroutinePeriodicDelaySendLoop;
.super Lkotlin/coroutines/jvm/internal/ContinuationImpl;

.field private L$0:Ljava/lang/Object;
.field private label:I
.field private period:J
.field private result:Ljava/lang/Object;

.method public final P(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 12

    iput-object p1, p0, Lloops/TestCoroutinePeriodicDelaySendLoop;->result:Ljava/lang/Object;
    invoke-static {}, Lkotlin/coroutines/intrinsics/a;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v0
    iget v1, p0, Lloops/TestCoroutinePeriodicDelaySendLoop;->label:I
    const/4 v2, 0x3
    const/4 v3, 0x2
    const/4 v4, 0x1
    if-eqz v1, :initial
    if-eq v1, v4, :shared_resume
    if-eq v1, v3, :send_resume
    if-eq v1, v2, :shared_resume

    new-instance p1, Ljava/lang/IllegalStateException;
    invoke-direct {p1}, Ljava/lang/IllegalStateException;-><init>()V
    throw p1

    :send_resume
    iget-object v5, p0, Lloops/TestCoroutinePeriodicDelaySendLoop;->L$0:Ljava/lang/Object;
    check-cast v5, Lkotlinx/coroutines/channels/ProducerScope;
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :repeat_delay

    :shared_resume
    iget-object v5, p0, Lloops/TestCoroutinePeriodicDelaySendLoop;->L$0:Ljava/lang/Object;
    check-cast v5, Lkotlinx/coroutines/channels/ProducerScope;
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :send

    :initial
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    iget-object v5, p0, Lloops/TestCoroutinePeriodicDelaySendLoop;->L$0:Ljava/lang/Object;
    check-cast v5, Lkotlinx/coroutines/channels/ProducerScope;
    iget-wide v6, p0, Lloops/TestCoroutinePeriodicDelaySendLoop;->period:J
    iput-object v5, p0, Lloops/TestCoroutinePeriodicDelaySendLoop;->L$0:Ljava/lang/Object;
    iput v4, p0, Lloops/TestCoroutinePeriodicDelaySendLoop;->label:I
    invoke-static {v6, v7, p0}, Lkotlinx/coroutines/DelayKt;->delay(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-eq p1, v0, :suspended

    :send
    invoke-interface {v5}, Lkotlinx/coroutines/channels/ProducerScope;->getChannel()Lkotlinx/coroutines/channels/SendChannel;
    move-result-object v8
    iput-object v5, p0, Lloops/TestCoroutinePeriodicDelaySendLoop;->L$0:Ljava/lang/Object;
    iput v3, p0, Lloops/TestCoroutinePeriodicDelaySendLoop;->label:I
    sget-object v9, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    invoke-interface {v8, v9, p0}, Lkotlinx/coroutines/channels/SendChannel;->send(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-eq p1, v0, :suspended

    :repeat_delay
    iget-wide v6, p0, Lloops/TestCoroutinePeriodicDelaySendLoop;->period:J
    iput-object v5, p0, Lloops/TestCoroutinePeriodicDelaySendLoop;->L$0:Ljava/lang/Object;
    iput v2, p0, Lloops/TestCoroutinePeriodicDelaySendLoop;->label:I
    invoke-static {v6, v7, p0}, Lkotlinx/coroutines/DelayKt;->delay(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-ne p1, v0, :send

    :suspended
    return-object v0
.end method
