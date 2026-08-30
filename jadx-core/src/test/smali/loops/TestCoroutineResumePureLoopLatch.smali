.class public final Lloops/TestCoroutineResumePureLoopLatch;
.super Lkotlin/coroutines/jvm/internal/SuspendLambda;

.field private I$0:I
.field private label:I

.method public constructor <init>(Lkotlin/coroutines/Continuation;)V
    .registers 4

    const/4 v0, 0x2
    invoke-direct {p0, v0, p1}, Lkotlin/coroutines/jvm/internal/SuspendLambda;-><init>(ILkotlin/coroutines/Continuation;)V
    return-void
.end method

.method private static native itemAt(I)Ljava/lang/Object;
.end method

.method private static native onStartedOrNot$default(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
.end method

.method private static native onStoppedOrNot$default(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
.end method

.method private static native onDestroyOrNot$default(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
.end method

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 9

    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v0
    iget v1, p0, Lloops/TestCoroutineResumePureLoopLatch;->label:I
    packed-switch v1, :state_table

    new-instance p1, Ljava/lang/IllegalStateException;
    invoke-direct {p1}, Ljava/lang/IllegalStateException;-><init>()V
    throw p1

    :state_zero
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    const/4 v2, 0x2
    goto :first_header

    :state_one
    iget v2, p0, Lloops/TestCoroutineResumePureLoopLatch;->I$0:I
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    move-object v7, p1
    goto :first_latch

    :state_two
    iget v2, p0, Lloops/TestCoroutineResumePureLoopLatch;->I$0:I
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    move-object v7, p1
    goto :second_latch

    :state_three
    iget v2, p0, Lloops/TestCoroutineResumePureLoopLatch;->I$0:I
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    move-object v7, p1
    goto :third_latch

    :state_four
    iget v2, p0, Lloops/TestCoroutineResumePureLoopLatch;->I$0:I
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    move-object v7, p1
    goto :fourth_latch

    :state_five
    iget v2, p0, Lloops/TestCoroutineResumePureLoopLatch;->I$0:I
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    move-object v7, p1
    goto :fifth_latch

    :first_header
    if-ltz v2, :second_init
    invoke-static {v2}, Lloops/TestCoroutineResumePureLoopLatch;->itemAt(I)Ljava/lang/Object;
    move-result-object v3
    if-eqz v3, :first_latch
    iput v2, p0, Lloops/TestCoroutineResumePureLoopLatch;->I$0:I
    const/4 v4, 0x1
    iput v4, p0, Lloops/TestCoroutineResumePureLoopLatch;->label:I
    invoke-static {v3, p0}, Lloops/TestCoroutineResumePureLoopLatch;->onStoppedOrNot$default(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-eq p1, v0, :suspended
    move-object v7, p1

    :first_latch
    add-int/lit8 v2, v2, -0x1
    goto :first_header

    :second_init
    const/4 v2, 0x2

    :second_header
    if-ltz v2, :third_init
    invoke-static {v2}, Lloops/TestCoroutineResumePureLoopLatch;->itemAt(I)Ljava/lang/Object;
    move-result-object v3
    if-eqz v3, :second_latch
    iput v2, p0, Lloops/TestCoroutineResumePureLoopLatch;->I$0:I
    const/4 v4, 0x2
    iput v4, p0, Lloops/TestCoroutineResumePureLoopLatch;->label:I
    invoke-static {v3, p0}, Lloops/TestCoroutineResumePureLoopLatch;->onStartedOrNot$default(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-eq p1, v0, :suspended
    move-object v7, p1

    :second_latch
    add-int/lit8 v2, v2, -0x1
    goto :second_header

    :third_init
    const/4 v2, 0x2

    :third_header
    if-ltz v2, :fourth_init
    invoke-static {v2}, Lloops/TestCoroutineResumePureLoopLatch;->itemAt(I)Ljava/lang/Object;
    move-result-object v3
    if-eqz v3, :third_latch
    iput v2, p0, Lloops/TestCoroutineResumePureLoopLatch;->I$0:I
    const/4 v4, 0x3
    iput v4, p0, Lloops/TestCoroutineResumePureLoopLatch;->label:I
    invoke-static {v3, p0}, Lloops/TestCoroutineResumePureLoopLatch;->onDestroyOrNot$default(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-eq p1, v0, :suspended
    move-object v7, p1

    :third_latch
    add-int/lit8 v2, v2, -0x1
    goto :third_header

    :fourth_init
    const/4 v2, 0x2

    :fourth_header
    if-ltz v2, :fifth_init
    invoke-static {v2}, Lloops/TestCoroutineResumePureLoopLatch;->itemAt(I)Ljava/lang/Object;
    move-result-object v3
    if-eqz v3, :fourth_latch
    iput v2, p0, Lloops/TestCoroutineResumePureLoopLatch;->I$0:I
    const/4 v4, 0x4
    iput v4, p0, Lloops/TestCoroutineResumePureLoopLatch;->label:I
    invoke-static {v3, p0}, Lloops/TestCoroutineResumePureLoopLatch;->onStoppedOrNot$default(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-eq p1, v0, :suspended
    move-object v7, p1

    :fourth_latch
    add-int/lit8 v2, v2, -0x1
    goto :fourth_header

    :fifth_init
    const/4 v2, 0x2

    :fifth_header
    if-ltz v2, :done
    invoke-static {v2}, Lloops/TestCoroutineResumePureLoopLatch;->itemAt(I)Ljava/lang/Object;
    move-result-object v3
    if-eqz v3, :fifth_latch
    iput v2, p0, Lloops/TestCoroutineResumePureLoopLatch;->I$0:I
    const/4 v4, 0x5
    iput v4, p0, Lloops/TestCoroutineResumePureLoopLatch;->label:I
    invoke-static {v3, p0}, Lloops/TestCoroutineResumePureLoopLatch;->onStartedOrNot$default(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-eq p1, v0, :suspended
    move-object v7, p1

    :fifth_latch
    add-int/lit8 v2, v2, -0x1
    goto :fifth_header

    :suspended
    return-object v0

    :done
    sget-object p1, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object p1

    :state_table
    .packed-switch 0x0
        :state_zero
        :state_one
        :state_two
        :state_three
        :state_four
        :state_five
    .end packed-switch
.end method
