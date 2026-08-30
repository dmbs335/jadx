.class public final Landroidx/compose/foundation/gestures/TapGestureDetectorKt;
.super Ljava/lang/Object;

.method public static final consumeUntilUp(Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 10
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "(",
            "Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;",
            "Lkotlin/coroutines/Continuation<",
            "-",
            "Lkotlin/Unit;",
            ">;)",
            "Ljava/lang/Object;"
        }
    .end annotation

    instance-of v0, p1, Landroidx/compose/foundation/gestures/TapGestureDetectorKt$consumeUntilUp$1;
    if-eqz v0, :cond_13
    move-object v0, p1
    check-cast v0, Landroidx/compose/foundation/gestures/TapGestureDetectorKt$consumeUntilUp$1;
    iget v1, v0, Landroidx/compose/foundation/gestures/TapGestureDetectorKt$consumeUntilUp$1;->label:I
    const/high16 v2, -0x80000000
    and-int v3, v1, v2
    if-eqz v3, :cond_13
    sub-int/2addr v1, v2
    iput v1, v0, Landroidx/compose/foundation/gestures/TapGestureDetectorKt$consumeUntilUp$1;->label:I
    goto :goto_18

    :cond_13
    new-instance v0, Landroidx/compose/foundation/gestures/TapGestureDetectorKt$consumeUntilUp$1;
    invoke-direct {v0, p1}, Lkotlin/coroutines/jvm/internal/ContinuationImpl;-><init>(Lkotlin/coroutines/Continuation;)V

    :goto_18
    iget-object p1, v0, Landroidx/compose/foundation/gestures/TapGestureDetectorKt$consumeUntilUp$1;->result:Ljava/lang/Object;
    invoke-static {}, Lkotlin/coroutines/intrinsics/a;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v1
    iget v2, v0, Landroidx/compose/foundation/gestures/TapGestureDetectorKt$consumeUntilUp$1;->label:I
    const/4 v3, 0x1
    if-eqz v2, :cond_35
    if-ne v2, v3, :cond_2d
    iget-object p0, v0, Landroidx/compose/foundation/gestures/TapGestureDetectorKt$consumeUntilUp$1;->L$0:Ljava/lang/Object;
    check-cast p0, Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :goto_44

    :cond_2d
    new-instance p0, Ljava/lang/IllegalStateException;
    const-string p1, "call to 'resume' before 'invoke' with coroutine"
    invoke-direct {p0, p1}, Ljava/lang/IllegalStateException;-><init>(Ljava/lang/String;)V
    throw p0

    :cond_35
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V

    :goto_38
    iput-object p0, v0, Landroidx/compose/foundation/gestures/TapGestureDetectorKt$consumeUntilUp$1;->L$0:Ljava/lang/Object;
    iput v3, v0, Landroidx/compose/foundation/gestures/TapGestureDetectorKt$consumeUntilUp$1;->label:I
    const/4 p1, 0x0
    invoke-static {p0, p1, v0, v3, p1}, Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;->awaitPointerEvent$default(Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;Landroidx/compose/ui/input/pointer/PointerEventPass;Lkotlin/coroutines/Continuation;ILjava/lang/Object;)Ljava/lang/Object;
    move-result-object p1
    if-ne p1, v1, :cond_44
    return-object v1

    :cond_44
    :goto_44
    check-cast p1, Landroidx/compose/ui/input/pointer/PointerEvent;
    invoke-virtual {p1}, Landroidx/compose/ui/input/pointer/PointerEvent;->getChanges()Ljava/util/List;
    move-result-object v2
    move-object v4, v2
    check-cast v4, Ljava/util/Collection;
    invoke-interface {v4}, Ljava/util/Collection;->size()I
    move-result v4
    const/4 v5, 0x0
    move v6, v5

    :goto_53
    if-ge v6, v4, :cond_61
    invoke-interface {v2, v6}, Ljava/util/List;->get(I)Ljava/lang/Object;
    move-result-object v7
    check-cast v7, Landroidx/compose/ui/input/pointer/PointerInputChange;
    invoke-virtual {v7}, Landroidx/compose/ui/input/pointer/PointerInputChange;->consume()V
    add-int/lit8 v6, v6, 0x1
    goto :goto_53

    :cond_61
    invoke-virtual {p1}, Landroidx/compose/ui/input/pointer/PointerEvent;->getChanges()Ljava/util/List;
    move-result-object p1
    move-object v2, p1
    check-cast v2, Ljava/util/Collection;
    invoke-interface {v2}, Ljava/util/Collection;->size()I
    move-result v2

    :goto_6c
    if-ge v5, v2, :cond_7e
    invoke-interface {p1, v5}, Ljava/util/List;->get(I)Ljava/lang/Object;
    move-result-object v4
    check-cast v4, Landroidx/compose/ui/input/pointer/PointerInputChange;
    invoke-virtual {v4}, Landroidx/compose/ui/input/pointer/PointerInputChange;->getPressed()Z
    move-result v4
    if-eqz v4, :cond_7b
    goto :goto_38

    :cond_7b
    add-int/lit8 v5, v5, 0x1
    goto :goto_6c

    :cond_7e
    sget-object p0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object p0
.end method
