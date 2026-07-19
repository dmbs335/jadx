.class public Lconditions/TestCoroutineContinuationInit;
.super Ljava/lang/Object;

.method public static test(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 4

    instance-of v0, p0, Lconditions/TestCoroutineContinuationInit$test$1;
    if-eqz v0, :new_state

    move-object v0, p0
    check-cast v0, Lconditions/TestCoroutineContinuationInit$test$1;
    iget v1, v0, Lconditions/TestCoroutineContinuationInit$test$1;->label:I
    const v2, -0x80000000
    and-int/2addr v2, v1
    if-eqz v2, :new_state

    const v2, -0x80000000
    sub-int/2addr v1, v2
    iput v1, v0, Lconditions/TestCoroutineContinuationInit$test$1;->label:I
    goto :return_state

    :new_state
    new-instance v0, Lconditions/TestCoroutineContinuationInit$test$1;
    invoke-direct {v0, p0}, Lconditions/TestCoroutineContinuationInit$test$1;-><init>(Lkotlin/coroutines/Continuation;)V

    :return_state
    invoke-static {v0}, Lconditions/TestCoroutineContinuationInit;->consume(Ljava/lang/Object;)V
    return-object v0
.end method

.method public static testDetached(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 4

    instance-of v0, p0, Lconditions/DetachedContinuation;
    if-eqz v0, :new_state

    move-object v0, p0
    check-cast v0, Lconditions/DetachedContinuation;
    iget v1, v0, Lconditions/DetachedContinuation;->label:I
    const v2, -0x80000000
    and-int/2addr v2, v1
    if-eqz v2, :new_state

    const v2, -0x80000000
    sub-int/2addr v1, v2
    iput v1, v0, Lconditions/DetachedContinuation;->label:I
    goto :return_state

    :new_state
    new-instance v0, Lconditions/DetachedContinuation;
    invoke-direct {v0, p0}, Lconditions/DetachedContinuation;-><init>(Lkotlin/coroutines/Continuation;)V

    :return_state
    invoke-static {v0}, Lconditions/TestCoroutineContinuationInit;->consume(Ljava/lang/Object;)V
    return-object v0
.end method

.method public testOwner(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 7

    move-object v2, p1
    instance-of v0, p1, Lconditions/TestCoroutineContinuationInit$OwnerContinuation;
    if-eqz v0, :new_state_owner

    move-object v0, p1
    check-cast v0, Lconditions/TestCoroutineContinuationInit$OwnerContinuation;
    iget v1, v0, Lconditions/TestCoroutineContinuationInit$OwnerContinuation;->label:I
    const v3, -0x80000000
    and-int v4, v1, v3
    if-eqz v4, :new_state_owner

    sub-int/2addr v1, v3
    iput v1, v0, Lconditions/TestCoroutineContinuationInit$OwnerContinuation;->label:I
    move-object v4, p0
    goto :return_state_owner

    :new_state_owner
    new-instance v0, Lconditions/TestCoroutineContinuationInit$OwnerContinuation;
    move-object v4, p0
    invoke-direct {v0, v4, v2}, Lconditions/TestCoroutineContinuationInit$OwnerContinuation;-><init>(Lconditions/TestCoroutineContinuationInit;Lkotlin/coroutines/Continuation;)V

    :return_state_owner
    invoke-static {v0}, Lconditions/TestCoroutineContinuationInit;->consume(Ljava/lang/Object;)V
    return-object v0
.end method

.method private static consume(Ljava/lang/Object;)V
    .registers 1

    return-void
.end method
