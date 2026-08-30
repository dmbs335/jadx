.class public final Lloops/TestCoroutineProtectedDirectResultJoin;
.super Lkotlin/coroutines/jvm/internal/RestrictedSuspendLambda;

.field final synthetic $channel:Lkotlinx/coroutines/channels/Channel;
.field final synthetic $currentContext:Lkotlin/coroutines/CoroutineContext;
.field final synthetic $scrollConfig:Landroidx/compose/foundation/gestures/ScrollConfig;
.field private synthetic L$0:Ljava/lang/Object;
.field label:I

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 14

    invoke-static {}, Lkotlin/coroutines/intrinsics/a;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;

    move-result-object v0

    iget v1, p0, Lloops/TestCoroutineProtectedDirectResultJoin;->label:I

    const/4 v2, 0x2

    const/4 v3, 0x1

    if-eqz v1, :cond_30

    if-eq v1, v3, :cond_28

    if-ne v1, v2, :cond_1b

    iget-object v1, p0, Lloops/TestCoroutineProtectedDirectResultJoin;->L$0:Ljava/lang/Object;

    check-cast v1, Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;

    :try_start_12
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    :try_end_15
    .catchall {:try_start_12 .. :try_end_15} :catchall_17

    goto/16 :goto_91

    :catchall_17
    move-exception v0

    move-object p1, v0

    goto/16 :goto_a2

    :cond_1b
    new-instance p1, Ljava/lang/IllegalStateException;

    const-string v0, "call to 'resume' before 'invoke' with coroutine"

    invoke-direct {p1, v0}, Ljava/lang/IllegalStateException;-><init>(Ljava/lang/String;)V

    throw p1

    :cond_28
    iget-object v1, p0, Lloops/TestCoroutineProtectedDirectResultJoin;->L$0:Ljava/lang/Object;

    check-cast v1, Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;

    :try_start_2c
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    :try_end_2f
    .catchall {:try_start_2c .. :try_end_2f} :catchall_17

    goto :goto_4d

    :cond_30
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V

    iget-object p1, p0, Lloops/TestCoroutineProtectedDirectResultJoin;->L$0:Ljava/lang/Object;

    check-cast p1, Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;

    move-object v1, p1

    :goto_38
    iget-object p1, p0, Lloops/TestCoroutineProtectedDirectResultJoin;->$currentContext:Lkotlin/coroutines/CoroutineContext;

    invoke-static {p1}, Lkotlinx/coroutines/JobKt;->isActive(Lkotlin/coroutines/CoroutineContext;)Z

    move-result p1

    if-eqz p1, :cond_aa

    :try_start_40
    iget-object p1, p0, Lloops/TestCoroutineProtectedDirectResultJoin;->$scrollConfig:Landroidx/compose/foundation/gestures/ScrollConfig;

    iput-object v1, p0, Lloops/TestCoroutineProtectedDirectResultJoin;->L$0:Ljava/lang/Object;

    iput v3, p0, Lloops/TestCoroutineProtectedDirectResultJoin;->label:I

    # invokes: Landroidx/compose/foundation/gestures/TransformableKt;->c(Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;Landroidx/compose/foundation/gestures/ScrollConfig;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    invoke-static {v1, p1, p0}, Landroidx/compose/foundation/gestures/TransformableKt;->access$awaitFirstCtrlMouseScroll(Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;Landroidx/compose/foundation/gestures/ScrollConfig;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;

    move-result-object p1

    if-ne p1, v0, :cond_4d

    goto :goto_90

    :cond_4d
    :goto_4d
    check-cast p1, Landroidx/compose/ui/geometry/Offset;

    invoke-virtual {p1}, Landroidx/compose/ui/geometry/Offset;->unbox-impl()J

    move-result-wide v4

    iget-object p1, p0, Lloops/TestCoroutineProtectedDirectResultJoin;->$channel:Lkotlinx/coroutines/channels/Channel;

    sget-object v6, Landroidx/compose/foundation/gestures/TransformEvent$TransformStarted;->INSTANCE:Landroidx/compose/foundation/gestures/TransformEvent$TransformStarted;

    invoke-interface {p1, v6}, Lkotlinx/coroutines/channels/SendChannel;->trySend-JP2dKIU(Ljava/lang/Object;)Ljava/lang/Object;

    :goto_5a
    const-wide v6, 0xffffffffL

    and-long/2addr v4, v6

    long-to-int p1, v4

    invoke-static {p1}, Ljava/lang/Float;->intBitsToFloat(I)F

    move-result p1

    const v4, 0x44084000    # 545.0f

    div-float/2addr p1, v4

    const/high16 v4, 0x40000000    # 2.0f

    float-to-double v4, v4

    float-to-double v6, p1

    invoke-static {v4, v5, v6, v7}, Ljava/lang/Math;->pow(DD)D

    move-result-wide v4

    double-to-float v7, v4

    iget-object p1, p0, Lloops/TestCoroutineProtectedDirectResultJoin;->$channel:Lkotlinx/coroutines/channels/Channel;

    new-instance v6, Landroidx/compose/foundation/gestures/TransformEvent$TransformDelta;

    sget-object v4, Landroidx/compose/ui/geometry/Offset;->Companion:Landroidx/compose/ui/geometry/Offset$Companion;

    invoke-virtual {v4}, Landroidx/compose/ui/geometry/Offset$Companion;->getZero-F1C5BW0()J

    move-result-wide v8

    const/4 v10, 0x0

    const/4 v11, 0x0

    invoke-direct/range {v6 .. v11}, Landroidx/compose/foundation/gestures/TransformEvent$TransformDelta;-><init>(FJFLkotlin/jvm/internal/DefaultConstructorMarker;)V

    invoke-interface {p1, v6}, Lkotlinx/coroutines/channels/SendChannel;->trySend-JP2dKIU(Ljava/lang/Object;)Ljava/lang/Object;

    iget-object p1, p0, Lloops/TestCoroutineProtectedDirectResultJoin;->$scrollConfig:Landroidx/compose/foundation/gestures/ScrollConfig;

    iput-object v1, p0, Lloops/TestCoroutineProtectedDirectResultJoin;->L$0:Ljava/lang/Object;

    iput v2, p0, Lloops/TestCoroutineProtectedDirectResultJoin;->label:I

    # invokes: Landroidx/compose/foundation/gestures/TransformableKt;->b(Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;Landroidx/compose/foundation/gestures/ScrollConfig;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    invoke-static {v1, p1, p0}, Landroidx/compose/foundation/gestures/TransformableKt;->access$awaitCtrlMouseScrollOrNull(Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;Landroidx/compose/foundation/gestures/ScrollConfig;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;

    move-result-object p1

    if-ne p1, v0, :cond_91

    :goto_90
    return-object v0

    :cond_91
    :goto_91
    check-cast p1, Landroidx/compose/ui/geometry/Offset;

    if-eqz p1, :cond_9a

    invoke-virtual {p1}, Landroidx/compose/ui/geometry/Offset;->unbox-impl()J

    move-result-wide v4
    :try_end_99
    .catchall {:try_start_40 .. :try_end_99} :catchall_17

    goto :goto_5a

    :cond_9a
    iget-object p1, p0, Lloops/TestCoroutineProtectedDirectResultJoin;->$channel:Lkotlinx/coroutines/channels/Channel;

    sget-object v4, Landroidx/compose/foundation/gestures/TransformEvent$TransformStopped;->INSTANCE:Landroidx/compose/foundation/gestures/TransformEvent$TransformStopped;

    invoke-interface {p1, v4}, Lkotlinx/coroutines/channels/SendChannel;->trySend-JP2dKIU(Ljava/lang/Object;)Ljava/lang/Object;

    goto :goto_38

    :goto_a2
    iget-object v0, p0, Lloops/TestCoroutineProtectedDirectResultJoin;->$channel:Lkotlinx/coroutines/channels/Channel;

    sget-object v1, Landroidx/compose/foundation/gestures/TransformEvent$TransformStopped;->INSTANCE:Landroidx/compose/foundation/gestures/TransformEvent$TransformStopped;

    invoke-interface {v0, v1}, Lkotlinx/coroutines/channels/SendChannel;->trySend-JP2dKIU(Ljava/lang/Object;)Ljava/lang/Object;

    throw p1

    :cond_aa
    sget-object p1, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;

    return-object p1
.end method
