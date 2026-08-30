.class public final Lloops/TestCoroutineMergedTwoStateTransformLoop;
.super Lkotlin/coroutines/jvm/internal/SuspendLambda;

.field private count:I
.field private label:I
.field private scope:Ljava/lang/Object;

.method public constructor <init>(Lkotlin/coroutines/Continuation;)V
    .registers 4

    const/4 v0, 0x2
    invoke-direct {p0, v0, p1}, Lkotlin/coroutines/jvm/internal/SuspendLambda;-><init>(ILkotlin/coroutines/Continuation;)V
    return-void
.end method

.method private static native isActive(Ljava/lang/Object;)Z
.end method

.method private static native receive(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
.end method

.method private static native skipTransform(Ljava/lang/Object;)Z
.end method

.method private static native transformCopy(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
.end method

.method private static native transformDirect(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
.end method

.method private static native use(Ljava/lang/Object;)V
.end method

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 10

    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v0
    iget v1, p0, Lloops/TestCoroutineMergedTwoStateTransformLoop;->label:I
    const/4 v2, 0x1
    const/4 v3, 0x2
    const/4 v4, 0x3
    if-eqz v1, :initial
    if-eq v1, v2, :resume_receive
    if-eq v1, v3, :resume_transform
    if-ne v1, v4, :bad_state

    :resume_transform
    iget v6, p0, Lloops/TestCoroutineMergedTwoStateTransformLoop;->count:I
    iget-object v5, p0, Lloops/TestCoroutineMergedTwoStateTransformLoop;->scope:Ljava/lang/Object;
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :transform_complete

    :resume_receive
    iget v6, p0, Lloops/TestCoroutineMergedTwoStateTransformLoop;->count:I
    iget-object v5, p0, Lloops/TestCoroutineMergedTwoStateTransformLoop;->scope:Ljava/lang/Object;
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :receive_result

    :initial
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    iget-object v5, p0, Lloops/TestCoroutineMergedTwoStateTransformLoop;->scope:Ljava/lang/Object;
    const/4 v6, 0x0

    :loop_header
    invoke-static {v5}, Lloops/TestCoroutineMergedTwoStateTransformLoop;->isActive(Ljava/lang/Object;)Z
    move-result v1
    if-eqz v1, :done
    add-int/lit8 v6, v6, 0x1
    iput-object v5, p0, Lloops/TestCoroutineMergedTwoStateTransformLoop;->scope:Ljava/lang/Object;
    iput v6, p0, Lloops/TestCoroutineMergedTwoStateTransformLoop;->count:I
    iput v2, p0, Lloops/TestCoroutineMergedTwoStateTransformLoop;->label:I
    invoke-static {p0}, Lloops/TestCoroutineMergedTwoStateTransformLoop;->receive(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-eq p1, v0, :suspended

    :receive_result
    invoke-static {p1}, Lloops/TestCoroutineMergedTwoStateTransformLoop;->skipTransform(Ljava/lang/Object;)Z
    move-result v1
    if-nez v1, :transform_complete
    and-int/lit8 v1, v6, 0x1
    if-eqz v1, :copy_transform
    iput-object v5, p0, Lloops/TestCoroutineMergedTwoStateTransformLoop;->scope:Ljava/lang/Object;
    iput v6, p0, Lloops/TestCoroutineMergedTwoStateTransformLoop;->count:I
    iput v3, p0, Lloops/TestCoroutineMergedTwoStateTransformLoop;->label:I
    invoke-static {p1, p0}, Lloops/TestCoroutineMergedTwoStateTransformLoop;->transformDirect(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    goto :transform_check

    :copy_transform
    invoke-static {p1}, Lloops/TestCoroutineMergedTwoStateTransformLoop;->use(Ljava/lang/Object;)V
    iput-object v5, p0, Lloops/TestCoroutineMergedTwoStateTransformLoop;->scope:Ljava/lang/Object;
    iput v6, p0, Lloops/TestCoroutineMergedTwoStateTransformLoop;->count:I
    iput v4, p0, Lloops/TestCoroutineMergedTwoStateTransformLoop;->label:I
    invoke-static {p1, p0}, Lloops/TestCoroutineMergedTwoStateTransformLoop;->transformCopy(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1

    :transform_check
    if-eq p1, v0, :suspended

    :transform_complete
    move-object p1, v5
    goto :loop_header

    :suspended
    return-object v0

    :done
    sget-object p1, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object p1

    :bad_state
    new-instance p1, Ljava/lang/IllegalStateException;
    invoke-direct {p1}, Ljava/lang/IllegalStateException;-><init>()V
    throw p1
.end method
