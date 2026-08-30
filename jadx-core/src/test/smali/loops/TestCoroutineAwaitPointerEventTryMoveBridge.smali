.class public final Lloops/TestCoroutineAwaitPointerEventTryMoveBridge;
.super Lkotlin/coroutines/jvm/internal/RestrictedSuspendLambda;

.field private L$0:Ljava/lang/Object;
.field private L$1:Ljava/lang/Object;
.field private label:I

.method public static native cleanup()V
.end method

.method public static native keepGoing(Landroidx/compose/ui/input/pointer/PointerEvent;)Z
.end method

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 7

    iget-object v0, p0, Lloops/TestCoroutineAwaitPointerEventTryMoveBridge;->L$0:Ljava/lang/Object;
    check-cast v0, Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;
    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v1
    iget v2, p0, Lloops/TestCoroutineAwaitPointerEventTryMoveBridge;->label:I
    const/4 v3, 0x1
    if-eqz v2, :initial
    if-ne v2, v3, :bad_state

    iget-object v4, p0, Lloops/TestCoroutineAwaitPointerEventTryMoveBridge;->L$1:Ljava/lang/Object;
    check-cast v4, Landroidx/compose/ui/input/pointer/PointerInputChange;
    goto :resume

    :bad_state
    new-instance p1, Ljava/lang/IllegalStateException;
    invoke-direct {p1}, Ljava/lang/IllegalStateException;-><init>()V
    throw p1

    :initial
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    const/4 v4, 0x0
    goto :await_event

    :try_start
    :resume
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :resume_bridge

    :await_event
    sget-object p1, Landroidx/compose/ui/input/pointer/PointerEventPass;->Initial:Landroidx/compose/ui/input/pointer/PointerEventPass;
    iput-object v0, p0, Lloops/TestCoroutineAwaitPointerEventTryMoveBridge;->L$0:Ljava/lang/Object;
    iput-object v4, p0, Lloops/TestCoroutineAwaitPointerEventTryMoveBridge;->L$1:Ljava/lang/Object;
    iput v3, p0, Lloops/TestCoroutineAwaitPointerEventTryMoveBridge;->label:I
    invoke-interface {v0, p1, p0}, Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;->awaitPointerEvent(Landroidx/compose/ui/input/pointer/PointerEventPass;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v5
    if-ne v5, v1, :event_body
    return-object v1

    :resume_bridge
    move-object v5, p1

    :event_body
    check-cast v5, Landroidx/compose/ui/input/pointer/PointerEvent;
    invoke-static {v5}, Lloops/TestCoroutineAwaitPointerEventTryMoveBridge;->keepGoing(Landroidx/compose/ui/input/pointer/PointerEvent;)Z
    move-result v2
    if-nez v2, :await_event
    goto :normal_exit
    :try_end

    :normal_exit
    invoke-static {}, Lloops/TestCoroutineAwaitPointerEventTryMoveBridge;->cleanup()V
    sget-object p1, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object p1

    :catch_all
    move-exception v0
    invoke-static {}, Lloops/TestCoroutineAwaitPointerEventTryMoveBridge;->cleanup()V
    throw v0

    .catchall {:try_start .. :try_end} :catch_all
.end method
