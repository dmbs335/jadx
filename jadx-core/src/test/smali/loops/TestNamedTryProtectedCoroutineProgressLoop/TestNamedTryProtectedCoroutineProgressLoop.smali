.class public Ljadx/tests/integration/loops/TestNamedTryProtectedCoroutineProgressLoop;
.super Ljava/lang/Object;
.source "TestNamedTryProtectedCoroutineProgressLoop.kt"


.method public constructor <init>()V
    .registers 1

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method

.method public static final touch(Ljava/lang/Object;)V
    .registers 1

    return-void
.end method

.method public final run(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 20

    move-object/from16 v14, p0
    move-object/from16 v15, p2

    instance-of v0, v15, Ljadx/tests/integration/loops/TestNamedTryProtectedCoroutineProgressLoop$run$1;

    if-eqz v0, :new_continuation

    move-object v0, v15
    check-cast v0, Ljadx/tests/integration/loops/TestNamedTryProtectedCoroutineProgressLoop$run$1;

    iget v1, v0, Ljadx/tests/integration/loops/TestNamedTryProtectedCoroutineProgressLoop$run$1;->label:I

    const/high16 v4, -0x80000000
    and-int v5, v1, v4

    if-eqz v5, :new_continuation

    sub-int/2addr v1, v4
    iput v1, v0, Ljadx/tests/integration/loops/TestNamedTryProtectedCoroutineProgressLoop$run$1;->label:I

    goto :continuation_ready

    :new_continuation
    new-instance v0, Ljadx/tests/integration/loops/TestNamedTryProtectedCoroutineProgressLoop$run$1;

    invoke-direct {v0, v14, v15}, Ljadx/tests/integration/loops/TestNamedTryProtectedCoroutineProgressLoop$run$1;-><init>(Ljadx/tests/integration/loops/TestNamedTryProtectedCoroutineProgressLoop;Lkotlin/coroutines/Continuation;)V

    :continuation_ready
    iget-object v2, v0, Ljadx/tests/integration/loops/TestNamedTryProtectedCoroutineProgressLoop$run$1;->result:Ljava/lang/Object;

    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v3

    iget v4, v0, Ljadx/tests/integration/loops/TestNamedTryProtectedCoroutineProgressLoop$run$1;->label:I

    if-eqz v4, :state_zero

    const/4 v5, 0x1
    if-eq v4, v5, :state_one

    const/4 v5, 0x2
    if-eq v4, v5, :state_two

    const/4 v0, 0x0
    return-object v0

    :state_two
    iget-object v6, v0, Ljadx/tests/integration/loops/TestNamedTryProtectedCoroutineProgressLoop$run$1;->L$0:Ljava/lang/Object;

    invoke-static {v2}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V

    goto :done

    :state_one
    iget-object v11, v0, Ljadx/tests/integration/loops/TestNamedTryProtectedCoroutineProgressLoop$run$1;->L$0:Ljava/lang/Object;
    iget v9, v0, Ljadx/tests/integration/loops/TestNamedTryProtectedCoroutineProgressLoop$run$1;->I$0:I
    iget v10, v0, Ljadx/tests/integration/loops/TestNamedTryProtectedCoroutineProgressLoop$run$1;->I$1:I

    invoke-static {v2}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V

    goto :result_join

    :state_zero
    invoke-static {v2}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V

    move-object/from16 v6, p1
    const/4 v7, 0x0
    const/4 v8, 0x4

    goto :loop_merge

    :loop_header
    if-ge v7, v8, :terminal_delay

    and-int/lit8 v9, v7, 0x1
    if-eqz v9, :no_delay

    :try_start_1
    iput-object v6, v0, Ljadx/tests/integration/loops/TestNamedTryProtectedCoroutineProgressLoop$run$1;->L$0:Ljava/lang/Object;
    iput v7, v0, Ljadx/tests/integration/loops/TestNamedTryProtectedCoroutineProgressLoop$run$1;->I$0:I
    iput v8, v0, Ljadx/tests/integration/loops/TestNamedTryProtectedCoroutineProgressLoop$run$1;->I$1:I
    const/4 v9, 0x1
    iput v9, v0, Ljadx/tests/integration/loops/TestNamedTryProtectedCoroutineProgressLoop$run$1;->label:I

    const-wide/16 v12, 0x1
    invoke-static {v12, v13, v0}, Lkotlinx/coroutines/DelayKt;->delay(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v2

    invoke-static {v2}, Ljadx/tests/integration/loops/TestNamedTryProtectedCoroutineProgressLoop;->touch(Ljava/lang/Object;)V

    move-object v5, v3
    :try_end_1
    .catchall {:try_start_1 .. :try_end_1} :progress_error

    if-ne v2, v5, :direct_success

    goto :suspended_return

    :direct_success
    move-object v11, v6
    move v9, v7
    move v10, v8
    move-object v11, v11
    move v9, v9
    move v10, v10
    move-object v11, v11
    move v9, v9
    move v10, v10
    move-object v11, v11
    move v9, v9
    move v10, v10
    move-object v11, v11
    move v9, v9
    move v10, v10
    move-object v11, v11

    goto :result_join

    :no_delay
    move-object v11, v6
    move v9, v7
    move v10, v8

    goto :progress_try_start

    :result_join
    move-object v6, v11
    move v7, v9
    move v8, v10

    goto :progress_try_start

    :progress_try_start
    :try_start_0
    invoke-static {v6}, Ljadx/tests/integration/loops/TestNamedTryProtectedCoroutineProgressLoop;->touch(Ljava/lang/Object;)V

    add-int/lit8 v7, v7, 0x1

    :loop_merge
    invoke-static {v6}, Ljadx/tests/integration/loops/TestNamedTryProtectedCoroutineProgressLoop;->touch(Ljava/lang/Object;)V
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :progress_error

    goto :loop_header

    :terminal_delay
    iput-object v6, v0, Ljadx/tests/integration/loops/TestNamedTryProtectedCoroutineProgressLoop$run$1;->L$0:Ljava/lang/Object;
    const/4 v9, 0x2
    iput v9, v0, Ljadx/tests/integration/loops/TestNamedTryProtectedCoroutineProgressLoop$run$1;->label:I

    const-wide/16 v12, 0x1
    invoke-static {v12, v13, v0}, Lkotlinx/coroutines/DelayKt;->delay(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v2

    if-ne v2, v3, :done

    goto :suspended_return

    :done
    sget-object v0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;

    return-object v0

    :suspended_return
    return-object v3

    :progress_error
    move-exception v0

    throw v0
.end method
