.class public final Lloops/TestCoroutineAwaitPointerEventDuplicateTerminalDiamond;
.super Lkotlin/coroutines/jvm/internal/RestrictedSuspendLambda;

.field private L$0:Ljava/lang/Object;
.field private L$1:Ljava/lang/Object;
.field private label:I

.method public constructor <init>(Lkotlin/coroutines/Continuation;)V
    .registers 4

    const/4 v0, 0x2
    invoke-direct {p0, v0, p1}, Lkotlin/coroutines/jvm/internal/RestrictedSuspendLambda;-><init>(ILkotlin/coroutines/Continuation;)V
    return-void
.end method

.method public static native firstDown(Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
.end method

.method public static native setTouchDown(Z)V
.end method

.method public static native onInteracting(Z)V
.end method

.method public static native isStarted()Z
.end method

.method public static native setStarted(Z)V
.end method

.method public static native onStop()V
.end method

.method public static native warning()V
.end method

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 12

    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v0
    iget v1, p0, Lloops/TestCoroutineAwaitPointerEventDuplicateTerminalDiamond;->label:I
    const/4 v2, 0x2
    const/4 v3, 0x0
    const/4 v4, 0x1
    if-eqz v1, :initial
    if-eq v1, v4, :resume_first
    if-ne v1, v2, :bad_state

    iget-object v4, p0, Lloops/TestCoroutineAwaitPointerEventDuplicateTerminalDiamond;->L$1:Ljava/lang/Object;
    check-cast v4, Landroidx/compose/ui/input/pointer/PointerInputChange;
    iget-object v5, p0, Lloops/TestCoroutineAwaitPointerEventDuplicateTerminalDiamond;->L$0:Ljava/lang/Object;
    check-cast v5, Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :event_body

    :resume_first
    iget-object v1, p0, Lloops/TestCoroutineAwaitPointerEventDuplicateTerminalDiamond;->L$0:Ljava/lang/Object;
    check-cast v1, Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :first_result

    :initial
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    iget-object v1, p0, Lloops/TestCoroutineAwaitPointerEventDuplicateTerminalDiamond;->L$0:Ljava/lang/Object;
    check-cast v1, Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;
    iput-object v1, p0, Lloops/TestCoroutineAwaitPointerEventDuplicateTerminalDiamond;->L$0:Ljava/lang/Object;
    iput v4, p0, Lloops/TestCoroutineAwaitPointerEventDuplicateTerminalDiamond;->label:I
    invoke-static {v1, p0}, Lloops/TestCoroutineAwaitPointerEventDuplicateTerminalDiamond;->firstDown(Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-eq p1, v0, :suspended

    :first_result
    check-cast p1, Landroidx/compose/ui/input/pointer/PointerInputChange;
    move-object v4, p1
    move-object v5, v1
    const/4 v1, 0x0

    :loop_check
    if-nez v1, :cleanup_second

    sget-object p1, Landroidx/compose/ui/input/pointer/PointerEventPass;->Initial:Landroidx/compose/ui/input/pointer/PointerEventPass;
    iput-object v5, p0, Lloops/TestCoroutineAwaitPointerEventDuplicateTerminalDiamond;->L$0:Ljava/lang/Object;
    iput-object v4, p0, Lloops/TestCoroutineAwaitPointerEventDuplicateTerminalDiamond;->L$1:Ljava/lang/Object;
    iput v2, p0, Lloops/TestCoroutineAwaitPointerEventDuplicateTerminalDiamond;->label:I
    invoke-interface {v5, p1, p0}, Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;->awaitPointerEvent(Landroidx/compose/ui/input/pointer/PointerEventPass;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-ne p1, v0, :event_body

    :suspended
    return-object v0

    :event_body
    check-cast p1, Landroidx/compose/ui/input/pointer/PointerEvent;
    invoke-virtual {p1}, Landroidx/compose/ui/input/pointer/PointerEvent;->getChanges()Ljava/util/List;
    move-result-object v6
    invoke-interface {v6}, Ljava/util/List;->isEmpty()Z
    move-result v7
    if-nez v7, :cleanup_second
    invoke-interface {v6, v3}, Ljava/util/List;->get(I)Ljava/lang/Object;
    move-result-object v7
    check-cast v7, Landroidx/compose/ui/input/pointer/PointerInputChange;
    invoke-virtual {v7}, Landroidx/compose/ui/input/pointer/PointerInputChange;->isConsumed()Z
    move-result v7
    if-eqz v7, :loop_check

    :cleanup_first
    const/4 v8, 0x0
    invoke-static {v8}, Lloops/TestCoroutineAwaitPointerEventDuplicateTerminalDiamond;->setTouchDown(Z)V
    invoke-static {v8}, Lloops/TestCoroutineAwaitPointerEventDuplicateTerminalDiamond;->onInteracting(Z)V
    invoke-static {}, Lloops/TestCoroutineAwaitPointerEventDuplicateTerminalDiamond;->isStarted()Z
    move-result v9
    if-eqz v9, :warning_first
    invoke-static {v8}, Lloops/TestCoroutineAwaitPointerEventDuplicateTerminalDiamond;->setStarted(Z)V
    invoke-static {}, Lloops/TestCoroutineAwaitPointerEventDuplicateTerminalDiamond;->onStop()V
    goto :done

    :warning_first
    invoke-static {}, Lloops/TestCoroutineAwaitPointerEventDuplicateTerminalDiamond;->warning()V
    goto :done

    :cleanup_second
    const/4 v8, 0x0
    invoke-static {v8}, Lloops/TestCoroutineAwaitPointerEventDuplicateTerminalDiamond;->setTouchDown(Z)V
    invoke-static {v8}, Lloops/TestCoroutineAwaitPointerEventDuplicateTerminalDiamond;->onInteracting(Z)V
    invoke-static {}, Lloops/TestCoroutineAwaitPointerEventDuplicateTerminalDiamond;->isStarted()Z
    move-result v9
    if-eqz v9, :warning_second
    invoke-static {v8}, Lloops/TestCoroutineAwaitPointerEventDuplicateTerminalDiamond;->setStarted(Z)V
    invoke-static {}, Lloops/TestCoroutineAwaitPointerEventDuplicateTerminalDiamond;->onStop()V
    goto :done

    :warning_second
    invoke-static {}, Lloops/TestCoroutineAwaitPointerEventDuplicateTerminalDiamond;->warning()V

    :done
    sget-object p1, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object p1

    :bad_state
    new-instance p1, Ljava/lang/IllegalStateException;
    invoke-direct {p1}, Ljava/lang/IllegalStateException;-><init>()V
    throw p1
.end method
