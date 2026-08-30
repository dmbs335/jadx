.class public final Ljadx/tests/integration/loops/TestTryProtectedCoroutineLoopActionResumeTail;
.super Lkotlin/coroutines/jvm/internal/SuspendLambda;
.source "TestTryProtectedCoroutineLoopActionResumeTail.kt"

.implements Lkotlin/jvm/functions/Function2;


.field I$0:I

.field L$0:Ljava/lang/Object;

.field label:I


.method public constructor <init>(Lkotlin/coroutines/Continuation;)V
    .registers 3

    const/4 v0, 0x2

    invoke-direct {p0, v0, p1}, Lkotlin/coroutines/jvm/internal/SuspendLambda;-><init>(ILkotlin/coroutines/Continuation;)V

    return-void
.end method

.method public static awaitStep(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 2

    sget-object v0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;

    return-object v0
.end method

.method public static touch(I)V
    .registers 1

    return-void
.end method

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 10

    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;

    move-result-object v0

    iget v1, p0, Ljadx/tests/integration/loops/TestTryProtectedCoroutineLoopActionResumeTail;->label:I

    if-eqz v1, :state_zero

    const/4 v2, 0x1

    if-ne v1, v2, :suspended_return

    iget v2, p0, Ljadx/tests/integration/loops/TestTryProtectedCoroutineLoopActionResumeTail;->I$0:I

    iget-object v3, p0, Ljadx/tests/integration/loops/TestTryProtectedCoroutineLoopActionResumeTail;->L$0:Ljava/lang/Object;

    :try_start_resume
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    :try_end_resume
    .catch Ljava/lang/Exception; {:try_start_resume .. :try_end_resume} :resume_error

    move v4, v2

    move-object v5, v3

    goto :action_start

    :state_zero
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V

    const/4 v2, 0x0

    new-instance v3, Ljava/lang/Object;

    invoke-direct {v3}, Ljava/lang/Object;-><init>()V

    :loop_header
    const/4 v6, 0x3

    if-ge v2, v6, :done

    iput v2, p0, Ljadx/tests/integration/loops/TestTryProtectedCoroutineLoopActionResumeTail;->I$0:I

    iput-object v3, p0, Ljadx/tests/integration/loops/TestTryProtectedCoroutineLoopActionResumeTail;->L$0:Ljava/lang/Object;

    const/4 v6, 0x1

    iput v6, p0, Ljadx/tests/integration/loops/TestTryProtectedCoroutineLoopActionResumeTail;->label:I

    invoke-static {v2, p0}, Ljadx/tests/integration/loops/TestTryProtectedCoroutineLoopActionResumeTail;->awaitStep(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;

    move-result-object v6

    if-ne v6, v0, :direct_success

    goto :suspended_return

    :direct_success
    move v4, v2

    move-object v5, v3

    :action_start
    :try_start_action
    invoke-static {v4}, Ljadx/tests/integration/loops/TestTryProtectedCoroutineLoopActionResumeTail;->touch(I)V
    :try_end_action
    .catch Ljava/lang/Exception; {:try_start_action .. :try_end_action} :action_error

    move v2, v4

    move-object v3, v5

    add-int/lit8 v2, v2, 0x1

    goto :loop_header

    :resume_error
    move-exception v1

    goto :done

    :action_error
    move-exception v1

    :done
    sget-object v1, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;

    return-object v1

    :suspended_return
    return-object v0
.end method
