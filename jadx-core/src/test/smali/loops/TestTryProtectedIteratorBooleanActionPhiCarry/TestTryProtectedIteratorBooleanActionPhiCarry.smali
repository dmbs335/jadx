.class public final Lloops/TestTryProtectedIteratorBooleanActionPhiCarry;
.super Ljava/lang/Object;

.method private static action(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 1
    sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
    return-object v0
.end method

.method private static prepare(Ljava/util/List;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 0
    return-object p0
.end method

.method private static cleanupSuspend(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 1
    sget-object v0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object v0
.end method

.method private static check(Ljava/lang/Object;)V
    .locals 0
    return-void
.end method

.method private static cleanup()V
    .locals 0
    return-void
.end method

.method private static getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    .locals 1
    const-string v0, "suspended"
    return-object v0
.end method

.method public static loop(Ljava/util/List;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 10

    instance-of v0, p1, Lloops/TestTryProtectedIteratorBooleanActionPhiCarry$loop$1;
    if-eqz v0, :new_state
    move-object v0, p1
    check-cast v0, Lloops/TestTryProtectedIteratorBooleanActionPhiCarry$loop$1;
    iget v3, v0, Lloops/TestTryProtectedIteratorBooleanActionPhiCarry$loop$1;->x:I
    const/high16 v4, -0x80000000
    and-int v4, v3, v4
    if-eqz v4, :new_state
    const/high16 v4, -0x80000000
    sub-int/2addr v3, v4
    iput v3, v0, Lloops/TestTryProtectedIteratorBooleanActionPhiCarry$loop$1;->x:I
    goto :state_ready

    :new_state
    new-instance v0, Lloops/TestTryProtectedIteratorBooleanActionPhiCarry$loop$1;
    invoke-direct {v0, p1}, Lloops/TestTryProtectedIteratorBooleanActionPhiCarry$loop$1;-><init>(Lkotlin/coroutines/Continuation;)V

    :state_ready
    iget-object v1, v0, Lloops/TestTryProtectedIteratorBooleanActionPhiCarry$loop$1;->result:Ljava/lang/Object;
    invoke-static {}, Lloops/TestTryProtectedIteratorBooleanActionPhiCarry;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v2
    iget v3, v0, Lloops/TestTryProtectedIteratorBooleanActionPhiCarry$loop$1;->x:I

    :try_start
    if-eqz v3, :initial
    const/4 v4, 0x1
    if-eq v3, v4, :resume_prepared
    const/4 v4, 0x2
    if-eq v3, v4, :resume_action
    const/4 v4, 0x3
    if-eq v3, v4, :resume_cleanup
    new-instance v8, Ljava/lang/IllegalStateException;
    invoke-direct {v8}, Ljava/lang/IllegalStateException;-><init>()V
    throw v8

    :resume_cleanup
    invoke-static {v1}, Lloops/TestTryProtectedIteratorBooleanActionPhiCarry;->check(Ljava/lang/Object;)V
    return-object v1

    :resume_action
    iget v7, v0, Lloops/TestTryProtectedIteratorBooleanActionPhiCarry$loop$1;->I$0:I
    iget-boolean v9, v0, Lloops/TestTryProtectedIteratorBooleanActionPhiCarry$loop$1;->Z$0:Z
    iget-object v6, v0, Lloops/TestTryProtectedIteratorBooleanActionPhiCarry$loop$1;->L$1:Ljava/lang/Object;
    check-cast v6, Ljava/util/Iterator;
    iget-object v5, v0, Lloops/TestTryProtectedIteratorBooleanActionPhiCarry$loop$1;->L$0:Ljava/lang/Object;
    check-cast v5, Ljava/util/List;
    invoke-static {v1}, Lloops/TestTryProtectedIteratorBooleanActionPhiCarry;->check(Ljava/lang/Object;)V
    goto :action_result

    :resume_prepared
    invoke-static {v1}, Lloops/TestTryProtectedIteratorBooleanActionPhiCarry;->check(Ljava/lang/Object;)V
    move-object v5, v1
    check-cast v5, Ljava/util/List;
    goto :list_ready

    :initial
    invoke-static {v1}, Lloops/TestTryProtectedIteratorBooleanActionPhiCarry;->check(Ljava/lang/Object;)V
    const/4 v4, 0x1
    iput v4, v0, Lloops/TestTryProtectedIteratorBooleanActionPhiCarry$loop$1;->x:I
    invoke-static {p0, v0}, Lloops/TestTryProtectedIteratorBooleanActionPhiCarry;->prepare(Ljava/util/List;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v1
    if-eq v1, v2, :suspended
    move-object v5, v1
    check-cast v5, Ljava/util/List;

    :list_ready
    invoke-interface {v5}, Ljava/util/List;->iterator()Ljava/util/Iterator;
    move-result-object v6
    const/4 v7, 0x0
    const/4 v9, 0x1

    :carry
    add-int/lit8 v7, v7, 0x0
    invoke-interface {v6}, Ljava/util/Iterator;->hasNext()Z
    move-result v4
    if-eqz v4, :exhausted
    add-int/lit8 v7, v7, 0x1
    invoke-interface {v6}, Ljava/util/Iterator;->next()Ljava/lang/Object;
    move-result-object v8
    iput-object v5, v0, Lloops/TestTryProtectedIteratorBooleanActionPhiCarry$loop$1;->L$0:Ljava/lang/Object;
    iput-object v6, v0, Lloops/TestTryProtectedIteratorBooleanActionPhiCarry$loop$1;->L$1:Ljava/lang/Object;
    iput-boolean v9, v0, Lloops/TestTryProtectedIteratorBooleanActionPhiCarry$loop$1;->Z$0:Z
    iput v7, v0, Lloops/TestTryProtectedIteratorBooleanActionPhiCarry$loop$1;->I$0:I
    const/4 v4, 0x2
    iput v4, v0, Lloops/TestTryProtectedIteratorBooleanActionPhiCarry$loop$1;->x:I
    invoke-static {v8, v0}, Lloops/TestTryProtectedIteratorBooleanActionPhiCarry;->action(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v1
    if-eq v1, v2, :suspended

    :action_result
    check-cast v1, Ljava/lang/Boolean;
    invoke-virtual {v1}, Ljava/lang/Boolean;->booleanValue()Z
    move-result v4
    if-eqz v4, :carry
    goto :terminal

    :exhausted
    const/4 v9, 0x0

    :terminal
    if-eqz v9, :cleanup_suspend
    sget-object v8, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object v8

    :cleanup_suspend
    const/4 v4, 0x3
    iput v4, v0, Lloops/TestTryProtectedIteratorBooleanActionPhiCarry$loop$1;->x:I
    invoke-static {v0}, Lloops/TestTryProtectedIteratorBooleanActionPhiCarry;->cleanupSuspend(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v1
    if-eq v1, v2, :suspended
    return-object v1
    :try_end
    .catchall {:try_start .. :try_end} :catch_all

    :suspended
    return-object v2

    :catch_all
    move-exception v8
    invoke-static {}, Lloops/TestTryProtectedIteratorBooleanActionPhiCarry;->cleanup()V
    throw v8
.end method
