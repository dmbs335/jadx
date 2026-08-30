.class public final Lloops/TestCoroutineTwoStateIteratorActionLoop;
.super Lkotlin/coroutines/jvm/internal/SuspendLambda;

.field private L$0:Ljava/lang/Object;
.field private label:I
.field private direction:I

.method private static hasNext(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 1
    sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
    return-object v0
.end method

.method private static next()Ljava/lang/Object;
    .locals 1
    const-string v0, "item"
    return-object v0
.end method

.method private static animate(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 0
    return-object p0
.end method

.method private static throwOnFailure(Ljava/lang/Object;)V
    .locals 0
    return-void
.end method

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 16

    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v0
    iget v1, p0, Lloops/TestCoroutineTwoStateIteratorActionLoop;->label:I
    const/4 v2, 0x2
    const/4 v3, 0x1

    if-eqz v1, :initial
    if-eq v1, v3, :resume_has_next
    if-ne v1, v2, :bad_state

    :resume_action
    iget-object v1, p0, Lloops/TestCoroutineTwoStateIteratorActionLoop;->L$0:Ljava/lang/Object;
    invoke-static {p1}, Lloops/TestCoroutineTwoStateIteratorActionLoop;->throwOnFailure(Ljava/lang/Object;)V
    move-object v10, p0

    :action_direct
    move-object p1, v1
    goto :after_action

    :resume_has_next
    iget-object v1, p0, Lloops/TestCoroutineTwoStateIteratorActionLoop;->L$0:Ljava/lang/Object;
    invoke-static {p1}, Lloops/TestCoroutineTwoStateIteratorActionLoop;->throwOnFailure(Ljava/lang/Object;)V
    goto :has_next_result

    :bad_state
    new-instance p1, Ljava/lang/IllegalStateException;
    invoke-direct {p1}, Ljava/lang/IllegalStateException;-><init>()V
    throw p1

    :initial
    invoke-static {p1}, Lloops/TestCoroutineTwoStateIteratorActionLoop;->throwOnFailure(Ljava/lang/Object;)V
    const-string p1, "iterator"

    :loop
    iput-object p1, p0, Lloops/TestCoroutineTwoStateIteratorActionLoop;->L$0:Ljava/lang/Object;
    iput v3, p0, Lloops/TestCoroutineTwoStateIteratorActionLoop;->label:I
    invoke-static {p0}, Lloops/TestCoroutineTwoStateIteratorActionLoop;->hasNext(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v1
    if-ne v1, v0, :has_next_direct
    move-object v10, p0
    goto :suspended

    :has_next_direct
    move-object v13, v1
    move-object v1, p1
    move-object p1, v13

    :has_next_result
    check-cast p1, Ljava/lang/Boolean;
    invoke-virtual {p1}, Ljava/lang/Boolean;->booleanValue()Z
    move-result p1
    if-eqz p1, :done
    invoke-static {}, Lloops/TestCoroutineTwoStateIteratorActionLoop;->next()Ljava/lang/Object;
    move-result-object p1
    move-object v4, p1
    const-string p1, "skip"
    invoke-virtual {v4, p1}, Ljava/lang/Object;->equals(Ljava/lang/Object;)Z
    move-result p1
    if-eqz p1, :invoke_action
    move-object v10, p0
    move-object p1, v1
    goto :loop

    :invoke_action
    iput-object v1, p0, Lloops/TestCoroutineTwoStateIteratorActionLoop;->L$0:Ljava/lang/Object;
    iput v2, p0, Lloops/TestCoroutineTwoStateIteratorActionLoop;->label:I
    move-object v10, p0
    invoke-static {v4, p0}, Lloops/TestCoroutineTwoStateIteratorActionLoop;->animate(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-ne p1, v0, :action_direct
    goto :suspended

    :after_action
    iget v4, v10, Lloops/TestCoroutineTwoStateIteratorActionLoop;->direction:I
    xor-int/2addr v4, v3
    iput v4, v10, Lloops/TestCoroutineTwoStateIteratorActionLoop;->direction:I
    move-object p1, v1
    goto :loop

    :suspended
    return-object v0

    :done
    sget-object p1, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object p1
.end method
