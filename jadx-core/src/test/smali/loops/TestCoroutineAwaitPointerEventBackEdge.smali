.class public Lloops/TestCoroutineAwaitPointerEventBackEdge;
.super Lkotlin/coroutines/jvm/internal/RestrictedSuspendLambda;

.field private L$0:Ljava/lang/Object;
.field private label:I

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 6

    iget-object v0, p0, Lloops/TestCoroutineAwaitPointerEventBackEdge;->L$0:Ljava/lang/Object;
    check-cast v0, Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;
    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v1
    iget v2, p0, Lloops/TestCoroutineAwaitPointerEventBackEdge;->label:I
    const/4 v3, 0x1
    if-eqz v2, :initial
    if-ne v2, v3, :bad_state
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :event_body

    :bad_state
    new-instance p1, Ljava/lang/IllegalStateException;
    invoke-direct {p1}, Ljava/lang/IllegalStateException;-><init>()V
    throw p1

    :initial
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V

    :await_event
    sget-object p1, Landroidx/compose/ui/input/pointer/PointerEventPass;->Initial:Landroidx/compose/ui/input/pointer/PointerEventPass;
    iput-object v0, p0, Lloops/TestCoroutineAwaitPointerEventBackEdge;->L$0:Ljava/lang/Object;
    iput v3, p0, Lloops/TestCoroutineAwaitPointerEventBackEdge;->label:I
    invoke-interface {v0, p1, p0}, Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;->awaitPointerEvent(Landroidx/compose/ui/input/pointer/PointerEventPass;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-eq p1, v1, :suspended

    :event_body
    check-cast p1, Landroidx/compose/ui/input/pointer/PointerEvent;
    invoke-virtual {p1}, Landroidx/compose/ui/input/pointer/PointerEvent;->getChanges()Ljava/util/List;
    move-result-object p1
    invoke-interface {p1}, Ljava/util/List;->iterator()Ljava/util/Iterator;
    move-result-object p1

    :changes_loop
    invoke-interface {p1}, Ljava/util/Iterator;->hasNext()Z
    move-result v2
    if-eqz v2, :await_event
    invoke-interface {p1}, Ljava/util/Iterator;->next()Ljava/lang/Object;
    move-result-object v2
    check-cast v2, Landroidx/compose/ui/input/pointer/PointerInputChange;
    invoke-virtual {v2}, Landroidx/compose/ui/input/pointer/PointerInputChange;->consume()V
    goto :changes_loop

    :suspended
    return-object v1
.end method
