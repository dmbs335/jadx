.class public Lloops/TestCoroutineTwoUnconditionalSuspendLoop;
.super Ljava/lang/Object;

.field private animatable:Landroidx/compose/animation/core/Animatable;
.field private label:I

.method private static animateTo$default(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 0
    return-object p0
.end method

.method private static isActive(Ljava/lang/Object;)Z
    .locals 1
    const/4 v0, 0x1
    return v0
.end method

.method private static snapTo(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 0
    return-object p0
.end method

.method private static throwOnFailure(Ljava/lang/Object;)V
    .locals 0
    return-void
.end method

.method public invokeSuspend(Ljava/lang/Object;Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 4

    iget v0, p0, Lloops/TestCoroutineTwoUnconditionalSuspendLoop;->label:I
    if-eqz v0, :initial
    const/4 v1, 0x1
    if-eq v0, v1, :resume_reset
    const/4 v1, 0x2
    if-eq v0, v1, :resume_animate
    new-instance v0, Ljava/lang/IllegalStateException;
    invoke-direct {v0}, Ljava/lang/IllegalStateException;-><init>()V
    throw v0

    :resume_animate
    invoke-static {p1}, Lloops/TestCoroutineTwoUnconditionalSuspendLoop;->throwOnFailure(Ljava/lang/Object;)V
    const/4 v2, 0x2
    goto :loop_latch

    :resume_reset
    invoke-static {p1}, Lloops/TestCoroutineTwoUnconditionalSuspendLoop;->throwOnFailure(Ljava/lang/Object;)V
    const/4 v2, 0x2
    goto :call_animate

    :initial
    invoke-static {p1}, Lloops/TestCoroutineTwoUnconditionalSuspendLoop;->throwOnFailure(Ljava/lang/Object;)V
    const/4 v2, 0x2

    :loop_header
    invoke-static {p1}, Lloops/TestCoroutineTwoUnconditionalSuspendLoop;->isActive(Ljava/lang/Object;)Z
    move-result v0
    if-eqz v0, :done

    const/4 v0, 0x1
    iput v0, p0, Lloops/TestCoroutineTwoUnconditionalSuspendLoop;->label:I
    invoke-static {p1, p3}, Lloops/TestCoroutineTwoUnconditionalSuspendLoop;->snapTo(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v3
    if-eq v3, p2, :suspended

    :call_animate
    iput v2, p0, Lloops/TestCoroutineTwoUnconditionalSuspendLoop;->label:I
    invoke-static {p1, p3}, Lloops/TestCoroutineTwoUnconditionalSuspendLoop;->animateTo$default(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v3
    if-eq v3, p2, :suspended

    :loop_latch
    move v1, v2
    move v2, v1
    goto :loop_header

    :suspended
    return-object p2

    :done
    return-object p1
.end method
