.class public final Lloops/TestCoroutineInfiniteFrameScaleLoop;
.super Ljava/lang/Object;

.field private label:I
.field private scale:F

.method public final invokeSuspend(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 8

    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v4
    iget v0, p0, Lloops/TestCoroutineInfiniteFrameScaleLoop;->label:I
    if-eqz v0, :initial
    const/4 v1, 0x1
    if-eq v0, v1, :resume_frame
    const/4 v1, 0x2
    if-eq v0, v1, :resume_scale
    new-instance v0, Ljava/lang/IllegalStateException;
    invoke-direct {v0}, Ljava/lang/IllegalStateException;-><init>()V
    throw v0

    :resume_scale
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :frame_loop

    :resume_frame
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :after_frame

    :initial
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    const/high16 v0, 0x3f800000    # 1.0f
    iput v0, p0, Lloops/TestCoroutineInfiniteFrameScaleLoop;->scale:F

    :frame_loop
    const/4 v0, 0x1
    iput v0, p0, Lloops/TestCoroutineInfiniteFrameScaleLoop;->label:I
    const/4 v0, 0x0
    invoke-static {v0, p2}, Landroidx/compose/animation/core/InfiniteAnimationPolicyKt;->withInfiniteAnimationFrameNanos(Lkotlin/jvm/functions/Function1;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v3
    if-eq v3, v4, :suspended

    :after_frame
    iget v0, p0, Lloops/TestCoroutineInfiniteFrameScaleLoop;->scale:F
    const/4 v1, 0x0
    cmpl-float v2, v0, v1
    if-nez v2, :frame_loop
    const/4 v0, 0x0
    invoke-static {v0}, Landroidx/compose/runtime/SnapshotStateKt;->snapshotFlow(Lkotlin/jvm/functions/Function0;)Lkotlinx/coroutines/flow/Flow;
    move-result-object v1
    const/4 v0, 0x2
    iput v0, p0, Lloops/TestCoroutineInfiniteFrameScaleLoop;->label:I
    const/4 v0, 0x0
    invoke-static {v1, v0, p2}, Lkotlinx/coroutines/flow/FlowKt;->first(Lkotlinx/coroutines/flow/Flow;Lkotlin/jvm/functions/Function2;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v3
    if-eq v3, v4, :suspended
    goto :frame_loop

    :suspended
    return-object v4
.end method
