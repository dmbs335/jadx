.class public final Lloops/TestCoroutineResumeMoveArithmeticLatch;
.super Lkotlin/coroutines/jvm/internal/SuspendLambda;

.field I$0:I
.field I$1:I
.field L$0:Ljava/lang/Object;
.field L$1:Ljava/lang/Object;
.field keys:[Ljava/lang/String;
.field label:I
.field map:Ljava/util/HashMap;

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 14

    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v0
    iget v1, p0, Lloops/TestCoroutineResumeMoveArithmeticLatch;->label:I
    const/4 v2, 0x1
    if-eqz v1, :initial
    if-ne v1, v2, :bad_state

    iget v4, p0, Lloops/TestCoroutineResumeMoveArithmeticLatch;->I$1:I
    iget v11, p0, Lloops/TestCoroutineResumeMoveArithmeticLatch;->I$0:I
    iget-object v10, p0, Lloops/TestCoroutineResumeMoveArithmeticLatch;->L$1:Ljava/lang/Object;
    check-cast v10, Ljava/util/HashMap;
    iget-object v9, p0, Lloops/TestCoroutineResumeMoveArithmeticLatch;->L$0:Ljava/lang/Object;
    check-cast v9, [Ljava/lang/String;
    move-object v8, p0
    move-object v7, p1
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :move_join

    :bad_state
    new-instance v0, Ljava/lang/IllegalStateException;
    const-string v1, "call to 'resume' before 'invoke' with coroutine"
    invoke-direct {v0, v1}, Ljava/lang/IllegalStateException;-><init>(Ljava/lang/String;)V
    throw v0

    :initial
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    iget-object v5, p0, Lloops/TestCoroutineResumeMoveArithmeticLatch;->keys:[Ljava/lang/String;
    iget-object v6, p0, Lloops/TestCoroutineResumeMoveArithmeticLatch;->map:Ljava/util/HashMap;
    array-length v4, v5
    const/4 v3, 0x0

    :loop_header
    if-ge v3, v4, :done
    aget-object v7, v5, v3
    invoke-virtual {v6, v7}, Ljava/util/HashMap;->containsKey(Ljava/lang/Object;)Z
    move-result v8
    if-eqz v8, :increment
    invoke-virtual {v6, v7}, Ljava/util/HashMap;->get(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v8
    if-eqz v8, :increment

    iput-object v5, p0, Lloops/TestCoroutineResumeMoveArithmeticLatch;->L$0:Ljava/lang/Object;
    iput-object v6, p0, Lloops/TestCoroutineResumeMoveArithmeticLatch;->L$1:Ljava/lang/Object;
    iput v3, p0, Lloops/TestCoroutineResumeMoveArithmeticLatch;->I$0:I
    iput v4, p0, Lloops/TestCoroutineResumeMoveArithmeticLatch;->I$1:I
    iput v2, p0, Lloops/TestCoroutineResumeMoveArithmeticLatch;->label:I
    invoke-static {v8, p0}, Lloops/TestCoroutineResumeMoveArithmeticLatch;->await(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-ne p1, v0, :direct_bridge
    return-object v0

    :direct_bridge
    move-object v8, p0
    move-object v9, v5
    move-object v10, v6
    move v11, v3
    move-object v7, p1

    :move_join
    move-object p1, v7
    move v3, v11
    move-object v6, v10
    move-object v5, v9
    move-object p0, v8

    :increment
    add-int/lit8 v3, v3, 0x1
    goto :loop_header

    :done
    sget-object p1, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object p1
.end method

.method private static await(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 3

    sget-object v0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object v0
.end method
