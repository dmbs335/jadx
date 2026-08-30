.class public Lloops/TestCoroutineTwoIndependentUnconditionalSuspendLoops;
.super Ljava/lang/Object;

.field private label:I
.field private animatable:Landroidx/compose/animation/core/Animatable;

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

.method public invokeSuspend(Ljava/lang/Object;Ljava/lang/Object;Lkotlin/coroutines/Continuation;Z)Ljava/lang/Object;
    .locals 4

    iget v0, p0, Lloops/TestCoroutineTwoIndependentUnconditionalSuspendLoops;->label:I
    if-eqz v0, :initial
    const/4 v1, 0x1
    if-eq v0, v1, :resume_first_reset
    const/4 v1, 0x2
    if-eq v0, v1, :resume_first_animate
    const/4 v1, 0x3
    if-eq v0, v1, :resume_second_reset
    const/4 v1, 0x4
    if-eq v0, v1, :resume_second_animate
    new-instance v0, Ljava/lang/IllegalStateException;
    invoke-direct {v0}, Ljava/lang/IllegalStateException;-><init>()V
    throw v0

    :resume_first_animate
    invoke-static {p1}, Lloops/TestCoroutineTwoIndependentUnconditionalSuspendLoops;->throwOnFailure(Ljava/lang/Object;)V
    const/4 v2, 0x2
    goto :first_latch

    :resume_first_reset
    invoke-static {p1}, Lloops/TestCoroutineTwoIndependentUnconditionalSuspendLoops;->throwOnFailure(Ljava/lang/Object;)V
    const/4 v2, 0x2
    goto :call_first_animate

    :resume_second_animate
    invoke-static {p1}, Lloops/TestCoroutineTwoIndependentUnconditionalSuspendLoops;->throwOnFailure(Ljava/lang/Object;)V
    const/4 v2, 0x4
    goto :second_latch

    :resume_second_reset
    invoke-static {p1}, Lloops/TestCoroutineTwoIndependentUnconditionalSuspendLoops;->throwOnFailure(Ljava/lang/Object;)V
    const/4 v2, 0x4
    goto :call_second_animate

    :initial
    invoke-static {p1}, Lloops/TestCoroutineTwoIndependentUnconditionalSuspendLoops;->throwOnFailure(Ljava/lang/Object;)V
    if-eqz p4, :second_setup

    const/4 v2, 0x2
    :first_header
    invoke-static {p1}, Lloops/TestCoroutineTwoIndependentUnconditionalSuspendLoops;->isActive(Ljava/lang/Object;)Z
    move-result v0
    if-eqz v0, :done
    const/4 v0, 0x1
    iput v0, p0, Lloops/TestCoroutineTwoIndependentUnconditionalSuspendLoops;->label:I
    invoke-static {p1, p3}, Lloops/TestCoroutineTwoIndependentUnconditionalSuspendLoops;->snapTo(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v3
    if-eq v3, p2, :suspended

    :call_first_animate
    iput v2, p0, Lloops/TestCoroutineTwoIndependentUnconditionalSuspendLoops;->label:I
    invoke-static {p1, p3}, Lloops/TestCoroutineTwoIndependentUnconditionalSuspendLoops;->animateTo$default(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v3
    if-eq v3, p2, :suspended

    :first_latch
    move v1, v2
    move v2, v1
    goto :first_header

    :second_setup
    const/4 v2, 0x4
    :second_header
    invoke-static {p1}, Lloops/TestCoroutineTwoIndependentUnconditionalSuspendLoops;->isActive(Ljava/lang/Object;)Z
    move-result v0
    if-eqz v0, :done
    const/4 v0, 0x3
    iput v0, p0, Lloops/TestCoroutineTwoIndependentUnconditionalSuspendLoops;->label:I
    invoke-static {p1, p3}, Lloops/TestCoroutineTwoIndependentUnconditionalSuspendLoops;->snapTo(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v3
    if-eq v3, p2, :suspended

    :call_second_animate
    iput v2, p0, Lloops/TestCoroutineTwoIndependentUnconditionalSuspendLoops;->label:I
    invoke-static {p1, p3}, Lloops/TestCoroutineTwoIndependentUnconditionalSuspendLoops;->animateTo$default(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v3
    if-eq v3, p2, :suspended

    :second_latch
    move v1, v2
    move v2, v1
    goto :second_header

    :suspended
    return-object p2

    :done
    return-object p1
.end method
