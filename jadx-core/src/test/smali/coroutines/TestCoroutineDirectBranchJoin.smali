.class public Lcoroutines/TestCoroutineDirectBranchJoin;
.super Lkotlin/coroutines/jvm/internal/SuspendLambda;

.annotation runtime Lkotlin/coroutines/jvm/internal/DebugMetadata;
    c = "coroutines.TestCoroutineDirectBranchJoin"
    f = "TestCoroutineDirectBranchJoin.kt"
    i = {}
    l = {}
    m = "invokeSuspend"
    n = {}
    s = {}
    v = 1
.end annotation

.method private static sideEffect(I)V
    .locals 0
    return-void
.end method

.method public invokeSuspend(III)I
    .locals 1

    if-eqz p1, :shared_tail

    if-eqz p2, :step_1
    const/4 v0, 0x1
    invoke-static {v0}, Lcoroutines/TestCoroutineDirectBranchJoin;->sideEffect(I)V
    :step_1
    if-eqz p2, :step_2
    const/4 v0, 0x2
    invoke-static {v0}, Lcoroutines/TestCoroutineDirectBranchJoin;->sideEffect(I)V
    :step_2
    if-eqz p2, :step_3
    const/4 v0, 0x3
    invoke-static {v0}, Lcoroutines/TestCoroutineDirectBranchJoin;->sideEffect(I)V
    :step_3
    if-eqz p2, :step_4
    const/4 v0, 0x4
    invoke-static {v0}, Lcoroutines/TestCoroutineDirectBranchJoin;->sideEffect(I)V
    :step_4
    if-eqz p2, :step_5
    const/4 v0, 0x5
    invoke-static {v0}, Lcoroutines/TestCoroutineDirectBranchJoin;->sideEffect(I)V
    :step_5
    if-eqz p2, :step_6
    const/4 v0, 0x6
    invoke-static {v0}, Lcoroutines/TestCoroutineDirectBranchJoin;->sideEffect(I)V
    :step_6
    if-eqz p2, :step_7
    const/4 v0, 0x7
    invoke-static {v0}, Lcoroutines/TestCoroutineDirectBranchJoin;->sideEffect(I)V
    :step_7
    if-eqz p2, :step_8
    const/16 v0, 0x8
    invoke-static {v0}, Lcoroutines/TestCoroutineDirectBranchJoin;->sideEffect(I)V
    :step_8
    if-eqz p2, :step_9
    const/16 v0, 0x9
    invoke-static {v0}, Lcoroutines/TestCoroutineDirectBranchJoin;->sideEffect(I)V
    :step_9
    if-eqz p2, :step_10
    const/16 v0, 0xa
    invoke-static {v0}, Lcoroutines/TestCoroutineDirectBranchJoin;->sideEffect(I)V
    :step_10
    if-eqz p2, :step_11
    const/16 v0, 0xb
    invoke-static {v0}, Lcoroutines/TestCoroutineDirectBranchJoin;->sideEffect(I)V
    :step_11
    if-eqz p2, :step_12
    const/16 v0, 0xc
    invoke-static {v0}, Lcoroutines/TestCoroutineDirectBranchJoin;->sideEffect(I)V
    :step_12
    if-eqz p2, :shared_tail
    const/16 v0, 0xd
    invoke-static {v0}, Lcoroutines/TestCoroutineDirectBranchJoin;->sideEffect(I)V

    :shared_tail
    const/16 v0, 0x63
    invoke-static {v0}, Lcoroutines/TestCoroutineDirectBranchJoin;->sideEffect(I)V
    if-eqz p3, :zero
    const/4 v0, 0x1
    return v0

    :zero
    const/4 v0, 0x0
    return v0
.end method
