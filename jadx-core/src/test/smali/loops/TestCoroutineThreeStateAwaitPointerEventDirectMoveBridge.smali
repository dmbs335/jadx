.class public final Lloops/TestCoroutineThreeStateAwaitPointerEventDirectMoveBridge;
.super Ljava/lang/Object;

.method private static native firstDown(Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
.end method

.method private static native keepGoing(Landroidx/compose/ui/input/pointer/PointerEvent;)Z
.end method

.method private static native finish(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
.end method

.method public static final detect(Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 12

    instance-of v0, p1, Lloops/TestCoroutineThreeStateAwaitPointerEventDirectMoveBridge$State;
    if-eqz v0, :new_state
    move-object v1, p1
    check-cast v1, Lloops/TestCoroutineThreeStateAwaitPointerEventDirectMoveBridge$State;
    iget v2, v1, Lloops/TestCoroutineThreeStateAwaitPointerEventDirectMoveBridge$State;->label:I
    const/high16 v3, -0x80000000
    and-int v4, v2, v3
    if-eqz v4, :new_state
    sub-int/2addr v2, v3
    iput v2, v1, Lloops/TestCoroutineThreeStateAwaitPointerEventDirectMoveBridge$State;->label:I
    goto :dispatch

    :new_state
    new-instance v1, Lloops/TestCoroutineThreeStateAwaitPointerEventDirectMoveBridge$State;
    invoke-direct {v1, p1}, Lloops/TestCoroutineThreeStateAwaitPointerEventDirectMoveBridge$State;-><init>(Lkotlin/coroutines/Continuation;)V

    :dispatch
    iget-object v4, v1, Lloops/TestCoroutineThreeStateAwaitPointerEventDirectMoveBridge$State;->result:Ljava/lang/Object;
    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v0
    iget v2, v1, Lloops/TestCoroutineThreeStateAwaitPointerEventDirectMoveBridge$State;->label:I
    if-eqz v2, :initial
    const/4 v3, 0x1
    if-eq v2, v3, :resume_first
    const/4 v3, 0x2
    if-eq v2, v3, :resume_event
    const/4 v3, 0x3
    if-ne v2, v3, :bad_state

    :resume_final
    iget-object v5, v1, Lloops/TestCoroutineThreeStateAwaitPointerEventDirectMoveBridge$State;->L$0:Ljava/lang/Object;
    check-cast v5, Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;
    invoke-static {v4}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :final_body

    :bad_state
    new-instance v0, Ljava/lang/IllegalStateException;
    invoke-direct {v0}, Ljava/lang/IllegalStateException;-><init>()V
    throw v0

    :resume_event
    iget-object v5, v1, Lloops/TestCoroutineThreeStateAwaitPointerEventDirectMoveBridge$State;->L$0:Ljava/lang/Object;
    check-cast v5, Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;
    invoke-static {v4}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :event_body

    :resume_first
    iget-object v5, v1, Lloops/TestCoroutineThreeStateAwaitPointerEventDirectMoveBridge$State;->L$0:Ljava/lang/Object;
    check-cast v5, Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;
    invoke-static {v4}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :loop

    :initial
    invoke-static {v4}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    iput-object p0, v1, Lloops/TestCoroutineThreeStateAwaitPointerEventDirectMoveBridge$State;->L$0:Ljava/lang/Object;
    const/4 v2, 0x1
    iput v2, v1, Lloops/TestCoroutineThreeStateAwaitPointerEventDirectMoveBridge$State;->label:I
    invoke-static {p0, v1}, Lloops/TestCoroutineThreeStateAwaitPointerEventDirectMoveBridge;->firstDown(Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v4
    if-eq v4, v0, :suspended
    move-object v5, p0

    :loop
    iput-object v5, v1, Lloops/TestCoroutineThreeStateAwaitPointerEventDirectMoveBridge$State;->L$0:Ljava/lang/Object;
    const/4 v2, 0x2
    iput v2, v1, Lloops/TestCoroutineThreeStateAwaitPointerEventDirectMoveBridge$State;->label:I
    sget-object v6, Landroidx/compose/ui/input/pointer/PointerEventPass;->Main:Landroidx/compose/ui/input/pointer/PointerEventPass;
    invoke-interface {v5, v6, v1}, Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;->awaitPointerEvent(Landroidx/compose/ui/input/pointer/PointerEventPass;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v4
    if-eq v4, v0, :suspended

    :event_body
    check-cast v4, Landroidx/compose/ui/input/pointer/PointerEvent;

    const/4 v2, 0x3
    iput-object v5, v1, Lloops/TestCoroutineThreeStateAwaitPointerEventDirectMoveBridge$State;->L$0:Ljava/lang/Object;
    iput v2, v1, Lloops/TestCoroutineThreeStateAwaitPointerEventDirectMoveBridge$State;->label:I
    sget-object v6, Landroidx/compose/ui/input/pointer/PointerEventPass;->Final:Landroidx/compose/ui/input/pointer/PointerEventPass;
    invoke-interface {v5, v6, v1}, Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;->awaitPointerEvent(Landroidx/compose/ui/input/pointer/PointerEventPass;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v4
    if-eq v4, v0, :suspended

    # State 3 resumes directly at final_body. Immediate completion reaches it through the
    # move-only bridge, matching the Compose direct/resume join seen in TransformableKt.
    move-object v7, v5
    move-object v5, v7
    goto :final_body

    :final_body
    check-cast v4, Landroidx/compose/ui/input/pointer/PointerEvent;
    invoke-static {v4}, Lloops/TestCoroutineThreeStateAwaitPointerEventDirectMoveBridge;->keepGoing(Landroidx/compose/ui/input/pointer/PointerEvent;)Z
    move-result v2
    if-nez v2, :loop
    invoke-static {v1}, Lloops/TestCoroutineThreeStateAwaitPointerEventDirectMoveBridge;->finish(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v4

    :done
    sget-object v4, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object v4

    :suspended
    return-object v0

.end method
