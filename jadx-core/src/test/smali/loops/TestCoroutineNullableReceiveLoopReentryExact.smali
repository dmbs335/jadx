.class public final Lloops/TestCoroutineNullableReceiveLoopReentryExact;
.super Lkotlin/coroutines/jvm/internal/SuspendLambda;

.field final synthetic $event:Lkotlin/jvm/internal/Ref$ObjectRef;
.field synthetic L$0:Ljava/lang/Object;
.field L$1:Ljava/lang/Object;
.field label:I
.field final synthetic this$0:Landroidx/compose/foundation/gestures/DragGestureNode;

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 7

    invoke-static {}, Lkotlin/coroutines/intrinsics/a;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v0

    iget v1, p0, Lloops/TestCoroutineNullableReceiveLoopReentryExact;->label:I
    const/4 v2, 0x1
    if-eqz v1, :cond_24
    if-ne v1, v2, :cond_17

    iget-object v1, p0, Lloops/TestCoroutineNullableReceiveLoopReentryExact;->L$1:Ljava/lang/Object;
    check-cast v1, Lkotlin/jvm/internal/Ref$ObjectRef;
    iget-object v3, p0, Lloops/TestCoroutineNullableReceiveLoopReentryExact;->L$0:Ljava/lang/Object;
    check-cast v3, Lkotlin/jvm/functions/Function1;
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :goto_5d

    :cond_17
    new-instance p1, Ljava/lang/IllegalStateException;
    const-string v0, "call to resume before invoke"
    invoke-direct {p1, v0}, Ljava/lang/IllegalStateException;-><init>(Ljava/lang/String;)V
    throw p1

    :cond_24
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    iget-object p1, p0, Lloops/TestCoroutineNullableReceiveLoopReentryExact;->L$0:Ljava/lang/Object;
    check-cast p1, Lkotlin/jvm/functions/Function1;
    move-object v3, p1

    :goto_2c
    iget-object p1, p0, Lloops/TestCoroutineNullableReceiveLoopReentryExact;->$event:Lkotlin/jvm/internal/Ref$ObjectRef;
    iget-object p1, p1, Lkotlin/jvm/internal/Ref$ObjectRef;->element:Ljava/lang/Object;
    instance-of v1, p1, Landroidx/compose/foundation/gestures/DragEvent$DragStopped;
    if-nez v1, :cond_63
    instance-of v1, p1, Landroidx/compose/foundation/gestures/DragEvent$DragCancelled;
    if-nez v1, :cond_63
    instance-of v1, p1, Landroidx/compose/foundation/gestures/DragEvent$DragDelta;
    const/4 v4, 0x0
    if-eqz v1, :cond_40
    check-cast p1, Landroidx/compose/foundation/gestures/DragEvent$DragDelta;
    goto :goto_41

    :cond_40
    move-object p1, v4

    :goto_41
    if-eqz p1, :cond_46
    invoke-interface {v3, p1}, Lkotlin/jvm/functions/Function1;->invoke(Ljava/lang/Object;)Ljava/lang/Object;

    :cond_46
    iget-object v1, p0, Lloops/TestCoroutineNullableReceiveLoopReentryExact;->$event:Lkotlin/jvm/internal/Ref$ObjectRef;
    iget-object p1, p0, Lloops/TestCoroutineNullableReceiveLoopReentryExact;->this$0:Landroidx/compose/foundation/gestures/DragGestureNode;
    invoke-static {p1}, Landroidx/compose/foundation/gestures/DragGestureNode;->access$getChannel$p(Landroidx/compose/foundation/gestures/DragGestureNode;)Lkotlinx/coroutines/channels/Channel;
    move-result-object p1
    if-eqz p1, :cond_60

    iput-object v3, p0, Lloops/TestCoroutineNullableReceiveLoopReentryExact;->L$0:Ljava/lang/Object;
    iput-object v1, p0, Lloops/TestCoroutineNullableReceiveLoopReentryExact;->L$1:Ljava/lang/Object;
    iput v2, p0, Lloops/TestCoroutineNullableReceiveLoopReentryExact;->label:I
    invoke-interface {p1, p0}, Lkotlinx/coroutines/channels/ReceiveChannel;->receive(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-ne p1, v0, :cond_5d
    return-object v0

    :cond_5d
    :goto_5d
    move-object v4, p1
    check-cast v4, Landroidx/compose/foundation/gestures/DragEvent;

    :cond_60
    iput-object v4, v1, Lkotlin/jvm/internal/Ref$ObjectRef;->element:Ljava/lang/Object;
    goto :goto_2c

    :cond_63
    sget-object p1, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object p1
.end method
