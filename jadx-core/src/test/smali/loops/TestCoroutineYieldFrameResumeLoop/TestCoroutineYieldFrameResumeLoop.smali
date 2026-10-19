.class public final Lloops/TestCoroutineYieldFrameResumeLoop;
.super Ljava/lang/Object;

.method public static run(Lkotlin/sequences/SequenceScope;Lkotlin/coroutines/jvm/internal/CoroutineStackFrame;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 8

    instance-of v0, p2, Lloops/TestCoroutineYieldFrameResumeLoop$State;
    if-eqz v0, :new_state
    move-object v0, p2
    check-cast v0, Lloops/TestCoroutineYieldFrameResumeLoop$State;
    iget v1, v0, Lloops/TestCoroutineYieldFrameResumeLoop$State;->label:I
    const/high16 v2, -0x80000000
    and-int v6, v1, v2
    if-eqz v6, :new_state
    sub-int/2addr v1, v2
    iput v1, v0, Lloops/TestCoroutineYieldFrameResumeLoop$State;->label:I
    goto :dispatch

    :new_state
    new-instance v0, Lloops/TestCoroutineYieldFrameResumeLoop$State;
    invoke-direct {v0, p2}, Lloops/TestCoroutineYieldFrameResumeLoop$State;-><init>(Lkotlin/coroutines/Continuation;)V

    :dispatch
    iget-object v1, v0, Lloops/TestCoroutineYieldFrameResumeLoop$State;->result:Ljava/lang/Object;
    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v2
    iget v6, v0, Lloops/TestCoroutineYieldFrameResumeLoop$State;->label:I
    packed-switch v6, :state_table
    new-instance v5, Ljava/lang/IllegalStateException;
    invoke-direct {v5}, Ljava/lang/IllegalStateException;-><init>()V
    throw v5

    :state_resume
    iget-object v3, v0, Lloops/TestCoroutineYieldFrameResumeLoop$State;->L$1:Ljava/lang/Object;
    check-cast v3, Lkotlin/coroutines/jvm/internal/CoroutineStackFrame;
    iget-object v4, v0, Lloops/TestCoroutineYieldFrameResumeLoop$State;->L$0:Ljava/lang/Object;
    check-cast v4, Lkotlin/sequences/SequenceScope;
    invoke-static {v1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :caller_advance

    :state_initial
    invoke-static {v1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    move-object v4, p0
    move-object v3, p1

    :frame_check
    if-eqz v3, :return_unit
    move-object v7, v3
    invoke-interface {v3}, Lkotlin/coroutines/jvm/internal/CoroutineStackFrame;->getStackTraceElement()Ljava/lang/StackTraceElement;
    move-result-object v5
    if-eqz v5, :caller_advance
    iput-object v4, v0, Lloops/TestCoroutineYieldFrameResumeLoop$State;->L$0:Ljava/lang/Object;
    iput-object v3, v0, Lloops/TestCoroutineYieldFrameResumeLoop$State;->L$1:Ljava/lang/Object;
    const/4 v6, 0x1
    iput v6, v0, Lloops/TestCoroutineYieldFrameResumeLoop$State;->label:I
    invoke-virtual {v4, v5, v0}, Lkotlin/sequences/SequenceScope;->yield(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v5
    if-eq v5, v2, :return_suspend
    move-object v3, v7

    :caller_advance
    invoke-interface {v3}, Lkotlin/coroutines/jvm/internal/CoroutineStackFrame;->getCallerFrame()Lkotlin/coroutines/jvm/internal/CoroutineStackFrame;
    move-result-object v5
    if-eqz v5, :return_unit
    move-object v3, v5
    goto :frame_check

    :return_suspend
    return-object v2

    :return_unit
    sget-object v5, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object v5

    :state_table
    .packed-switch 0x0
        :state_initial
        :state_resume
    .end packed-switch
.end method
