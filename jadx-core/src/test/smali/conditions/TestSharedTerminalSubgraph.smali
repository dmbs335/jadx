.class public Lconditions/TestSharedTerminalSubgraph;
.super Lkotlin/coroutines/jvm/internal/SuspendLambda;

.method public invokeSuspend(ZZZ)I
    .locals 2

    const/4 v1, 0x2

    :loop
    if-eqz p1, :finish
    if-eqz p2, :retry
    if-eqz p3, :finish

    :retry
    add-int/lit8 v1, v1, -0x1
    if-gtz v1, :loop
    goto :finish

    :finish
    if-eqz p2, :zero
    const/4 v0, 0x1
    return v0

    :zero
    const/4 v0, 0x0
    return v0
.end method
