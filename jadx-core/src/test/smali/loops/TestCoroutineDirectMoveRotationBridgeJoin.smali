.class final Lloops/TestCoroutineDirectMoveRotationBridgeJoin;
.super Lkotlin/coroutines/jvm/internal/RestrictedSuspendLambda;

.field I$0:I
.field I$1:I
.field L$0:Ljava/lang/Object;
.field L$1:Ljava/lang/Object;
.field L$2:Ljava/lang/Object;
.field label:I
.field this$0:Lio/ktor/http/cio/HeadersData;

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 9

    iget-object v0, p0, Lloops/TestCoroutineDirectMoveRotationBridgeJoin;->L$0:Ljava/lang/Object;
    check-cast v0, Lkotlin/sequences/SequenceScope;
    invoke-static {}, Lkotlin/coroutines/intrinsics/a;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v1
    iget v2, p0, Lloops/TestCoroutineDirectMoveRotationBridgeJoin;->label:I
    const/4 v3, 0x0
    const/4 v4, 0x1
    if-eqz v2, :initial
    if-ne v2, v4, :bad_state

    iget v2, p0, Lloops/TestCoroutineDirectMoveRotationBridgeJoin;->I$1:I
    iget v5, p0, Lloops/TestCoroutineDirectMoveRotationBridgeJoin;->I$0:I
    iget-object v6, p0, Lloops/TestCoroutineDirectMoveRotationBridgeJoin;->L$2:Ljava/lang/Object;
    check-cast v6, [I
    iget-object v7, p0, Lloops/TestCoroutineDirectMoveRotationBridgeJoin;->L$1:Ljava/lang/Object;
    check-cast v7, Ljava/util/Iterator;
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :move_join

    :bad_state
    new-instance p1, Ljava/lang/IllegalStateException;
    const-string v0, "bad coroutine state"
    invoke-direct {p1, v0}, Ljava/lang/IllegalStateException;-><init>(Ljava/lang/String;)V
    throw p1

    :initial
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    iget-object p1, p0, Lloops/TestCoroutineDirectMoveRotationBridgeJoin;->this$0:Lio/ktor/http/cio/HeadersData;
    invoke-static {p1}, Lio/ktor/http/cio/HeadersData;->access$getArrays$p(Lio/ktor/http/cio/HeadersData;)Ljava/util/List;
    move-result-object p1
    invoke-interface {p1}, Ljava/util/List;->iterator()Ljava/util/Iterator;
    move-result-object p1
    move v2, v3

    :outer_loop
    invoke-interface {p1}, Ljava/util/Iterator;->hasNext()Z
    move-result v5
    if-eqz v5, :done
    invoke-interface {p1}, Ljava/util/Iterator;->next()Ljava/lang/Object;
    move-result-object v5
    check-cast v5, [I
    move-object v6, v5
    move v5, v2
    move v2, v3

    :loop_header
    array-length v7, v6
    if-ge v2, v7, :next_array
    iget-object v7, p0, Lloops/TestCoroutineDirectMoveRotationBridgeJoin;->this$0:Lio/ktor/http/cio/HeadersData;
    invoke-virtual {v7, v5}, Lio/ktor/http/cio/HeadersData;->at(I)I
    move-result v7
    const/4 v8, -0x1
    if-eq v7, v8, :rotate
    invoke-static {v5}, Lkotlin/coroutines/jvm/internal/Boxing;->boxInt(I)Ljava/lang/Integer;
    move-result-object v7
    iput-object v0, p0, Lloops/TestCoroutineDirectMoveRotationBridgeJoin;->L$0:Ljava/lang/Object;
    iput-object p1, p0, Lloops/TestCoroutineDirectMoveRotationBridgeJoin;->L$1:Ljava/lang/Object;
    iput-object v6, p0, Lloops/TestCoroutineDirectMoveRotationBridgeJoin;->L$2:Ljava/lang/Object;
    iput v5, p0, Lloops/TestCoroutineDirectMoveRotationBridgeJoin;->I$0:I
    iput v2, p0, Lloops/TestCoroutineDirectMoveRotationBridgeJoin;->I$1:I
    iput v4, p0, Lloops/TestCoroutineDirectMoveRotationBridgeJoin;->label:I
    invoke-virtual {v0, v7, p0}, Lkotlin/sequences/SequenceScope;->yield(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v7
    if-ne v7, v1, :direct_bridge
    return-object v1

    :direct_bridge
    move-object v7, p1

    :move_join
    move-object p1, v7

    :rotate
    move-object v7, p1
    move-object p1, v0
    move-object v0, v7
    goto :loop_header

    :next_array
    move v2, v5
    goto :outer_loop

    :done
    sget-object p1, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object p1
.end method
