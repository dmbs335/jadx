.class public final Lloops/TestCoroutineNestedYieldOuterLoop;
.super Lkotlin/coroutines/jvm/internal/RestrictedSuspendLambda;

.field I$0:I
.field I$1:I
.field I$2:I
.field I$3:I
.field J$0:J
.field private synthetic L$0:Ljava/lang/Object;
.field L$1:Ljava/lang/Object;
.field L$2:Ljava/lang/Object;
.field label:I

.method public constructor <init>(Lkotlin/coroutines/Continuation;)V
    .registers 4
    const/4 v0, 0x2
    invoke-direct {p0, v0, p1}, Lkotlin/coroutines/jvm/internal/RestrictedSuspendLambda;-><init>(ILkotlin/coroutines/Continuation;)V
    return-void
.end method

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 22

    move-object/from16 v0, p0
    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v1
    iget v2, v0, Lloops/TestCoroutineNestedYieldOuterLoop;->label:I
    const/4 v3, 0x0
    const/16 v4, 0x8
    const/4 v5, 0x1
    if-eqz v2, :initial
    if-ne v2, v5, :invalid_state

    iget v2, v0, Lloops/TestCoroutineNestedYieldOuterLoop;->I$3:I
    iget v6, v0, Lloops/TestCoroutineNestedYieldOuterLoop;->I$2:I
    iget-wide v7, v0, Lloops/TestCoroutineNestedYieldOuterLoop;->J$0:J
    iget v9, v0, Lloops/TestCoroutineNestedYieldOuterLoop;->I$1:I
    iget v10, v0, Lloops/TestCoroutineNestedYieldOuterLoop;->I$0:I
    iget-object v11, v0, Lloops/TestCoroutineNestedYieldOuterLoop;->L$2:Ljava/lang/Object;
    check-cast v11, [J
    iget-object v12, v0, Lloops/TestCoroutineNestedYieldOuterLoop;->L$1:Ljava/lang/Object;
    check-cast v12, [Ljava/lang/Object;
    iget-object v13, v0, Lloops/TestCoroutineNestedYieldOuterLoop;->L$0:Ljava/lang/Object;
    check-cast v13, Lkotlin/sequences/SequenceScope;
    invoke-static/range {p1 .. p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :inner_advance

    :invalid_state
    new-instance v1, Ljava/lang/IllegalStateException;
    const-string v2, "call to resume before invoke"
    invoke-direct {v1, v2}, Ljava/lang/IllegalStateException;-><init>(Ljava/lang/String;)V
    throw v1

    :initial
    invoke-static/range {p1 .. p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    iget-object v2, v0, Lloops/TestCoroutineNestedYieldOuterLoop;->L$0:Ljava/lang/Object;
    check-cast v2, Lkotlin/sequences/SequenceScope;
    const/16 v6, 0x18
    new-array v7, v6, [Ljava/lang/Object;
    const/4 v6, 0x4
    new-array v6, v6, [J
    array-length v8, v6
    add-int/lit8 v8, v8, -0x2
    if-ltz v8, :done
    move v9, v3

    :outer_header
    aget-wide v10, v6, v9
    not-long v12, v10
    const/4 v14, 0x7
    shl-long/2addr v12, v14
    and-long/2addr v12, v10
    const-wide v14, -0x7f7f7f7f7f7f7f80L
    and-long/2addr v12, v14
    cmp-long v12, v12, v14
    if-eqz v12, :outer_end_check

    sub-int v12, v9, v8
    not-int v12, v12
    ushr-int/lit8 v12, v12, 0x1f
    rsub-int/lit8 v12, v12, 0x8
    move-object v13, v2
    move v2, v3
    move-wide/from16 v18, v10
    move-object v11, v6
    move v10, v8
    move v6, v12
    move-object v12, v7
    move-wide/from16 v7, v18

    :inner_header
    if-ge v2, v6, :inner_done
    const-wide/16 v14, 0xff
    and-long/2addr v14, v7
    const-wide/16 v16, 0x80
    cmp-long v14, v14, v16
    if-gez v14, :inner_advance

    shl-int/lit8 v14, v9, 0x3
    add-int/2addr v14, v2
    aget-object v14, v12, v14
    iput-object v13, v0, Lloops/TestCoroutineNestedYieldOuterLoop;->L$0:Ljava/lang/Object;
    iput-object v12, v0, Lloops/TestCoroutineNestedYieldOuterLoop;->L$1:Ljava/lang/Object;
    iput-object v11, v0, Lloops/TestCoroutineNestedYieldOuterLoop;->L$2:Ljava/lang/Object;
    iput v10, v0, Lloops/TestCoroutineNestedYieldOuterLoop;->I$0:I
    iput v9, v0, Lloops/TestCoroutineNestedYieldOuterLoop;->I$1:I
    iput-wide v7, v0, Lloops/TestCoroutineNestedYieldOuterLoop;->J$0:J
    iput v6, v0, Lloops/TestCoroutineNestedYieldOuterLoop;->I$2:I
    iput v2, v0, Lloops/TestCoroutineNestedYieldOuterLoop;->I$3:I
    iput v5, v0, Lloops/TestCoroutineNestedYieldOuterLoop;->label:I
    invoke-virtual {v13, v14, v0}, Lkotlin/sequences/SequenceScope;->yield(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v14
    if-ne v14, v1, :inner_advance
    return-object v1

    :inner_advance
    shr-long/2addr v7, v4
    add-int/2addr v2, v5
    goto :inner_header

    :inner_done
    if-ne v6, v4, :done
    move v8, v10
    move-object v6, v11
    move-object v7, v12
    move-object v2, v13

    :outer_end_check
    if-eq v9, v8, :done
    add-int/lit8 v9, v9, 0x1
    goto :outer_header

    :done
    sget-object v1, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object v1
.end method
