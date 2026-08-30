.class public final Lloops/TestTryProtectedTwoStateIteratorActionLoop;
.super Ljava/lang/Object;

.field private static cleaned:Z

.method private static action(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 0
    return-object p0
.end method

.method private static cleanup()V
    .locals 1
    const/4 v0, 0x1
    sput-boolean v0, Lloops/TestTryProtectedTwoStateIteratorActionLoop;->cleaned:Z
    return-void
.end method

.method private static getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    .locals 1
    const-string v0, "suspended"
    return-object v0
.end method

.method private static hasNext(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 1
    sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
    return-object v0
.end method

.method private static next(Ljava/lang/Object;)Ljava/lang/Object;
    .locals 1
    const-string v0, "element"
    return-object v0
.end method

.method private static throwOnFailure(Ljava/lang/Object;)V
    .locals 0
    return-void
.end method

.method private static touch(Ljava/lang/Object;)V
    .locals 0
    return-void
.end method

.method public static loop(Ljava/lang/Object;Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 10

    instance-of v1, p2, Lloops/TestTryProtectedTwoStateIteratorActionLoop$loop$1;
    if-eqz v1, :new_state
    move-object v1, p2
    check-cast v1, Lloops/TestTryProtectedTwoStateIteratorActionLoop$loop$1;
    iget v3, v1, Lloops/TestTryProtectedTwoStateIteratorActionLoop$loop$1;->label:I
    const/high16 v6, -0x80000000
    and-int v6, v3, v6
    if-eqz v6, :new_state
    const/high16 v6, -0x80000000
    sub-int/2addr v3, v6
    iput v3, v1, Lloops/TestTryProtectedTwoStateIteratorActionLoop$loop$1;->label:I
    goto :state_ready

    :new_state
    new-instance v1, Lloops/TestTryProtectedTwoStateIteratorActionLoop$loop$1;
    invoke-direct {v1, p2}, Lloops/TestTryProtectedTwoStateIteratorActionLoop$loop$1;-><init>(Lkotlin/coroutines/Continuation;)V

    :state_ready
    iget-object v2, v1, Lloops/TestTryProtectedTwoStateIteratorActionLoop$loop$1;->result:Ljava/lang/Object;
    invoke-static {}, Lloops/TestTryProtectedTwoStateIteratorActionLoop;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v0
    iget v3, v1, Lloops/TestTryProtectedTwoStateIteratorActionLoop$loop$1;->label:I
    const/4 v4, 0x1
    const/4 v5, 0x2

    :try_start
    if-eqz v3, :initial
    if-eq v3, v4, :resume_has_next
    if-ne v3, v5, :bad_state

    :resume_action
    iget-object v7, v1, Lloops/TestTryProtectedTwoStateIteratorActionLoop$loop$1;->L$1:Ljava/lang/Object;
    iget-object v8, v1, Lloops/TestTryProtectedTwoStateIteratorActionLoop$loop$1;->L$0:Ljava/lang/Object;
    invoke-static {v2}, Lloops/TestTryProtectedTwoStateIteratorActionLoop;->throwOnFailure(Ljava/lang/Object;)V
    goto :loop_preheader

    :resume_has_next
    iget-object v7, v1, Lloops/TestTryProtectedTwoStateIteratorActionLoop$loop$1;->L$1:Ljava/lang/Object;
    iget-object v8, v1, Lloops/TestTryProtectedTwoStateIteratorActionLoop$loop$1;->L$0:Ljava/lang/Object;
    invoke-static {v2}, Lloops/TestTryProtectedTwoStateIteratorActionLoop;->throwOnFailure(Ljava/lang/Object;)V
    goto :has_next_result

    :bad_state
    new-instance v6, Ljava/lang/IllegalStateException;
    invoke-direct {v6}, Ljava/lang/IllegalStateException;-><init>()V
    throw v6

    :initial
    invoke-static {v2}, Lloops/TestTryProtectedTwoStateIteratorActionLoop;->throwOnFailure(Ljava/lang/Object;)V
    move-object v7, p0
    move-object v8, p1
    goto :loop_preheader

    :action_call
    iput-object v8, v1, Lloops/TestTryProtectedTwoStateIteratorActionLoop$loop$1;->L$0:Ljava/lang/Object;
    iput-object v7, v1, Lloops/TestTryProtectedTwoStateIteratorActionLoop$loop$1;->L$1:Ljava/lang/Object;
    iput v5, v1, Lloops/TestTryProtectedTwoStateIteratorActionLoop$loop$1;->label:I
    invoke-static {v9, v1}, Lloops/TestTryProtectedTwoStateIteratorActionLoop;->action(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v2
    if-eq v2, v0, :suspended
    move-object v2, v2

    :loop_preheader
    move-object v8, v8
    goto :loop

    :loop
    iput-object v8, v1, Lloops/TestTryProtectedTwoStateIteratorActionLoop$loop$1;->L$0:Ljava/lang/Object;
    iput-object v7, v1, Lloops/TestTryProtectedTwoStateIteratorActionLoop$loop$1;->L$1:Ljava/lang/Object;
    iput v4, v1, Lloops/TestTryProtectedTwoStateIteratorActionLoop$loop$1;->label:I
    invoke-static {v7, v1}, Lloops/TestTryProtectedTwoStateIteratorActionLoop;->hasNext(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v2
    if-eq v2, v0, :suspended
    move-object v2, v2

    :has_next_result
    check-cast v2, Ljava/lang/Boolean;
    invoke-virtual {v2}, Ljava/lang/Boolean;->booleanValue()Z
    move-result v6
    if-eqz v6, :done
    invoke-static {v7}, Lloops/TestTryProtectedTwoStateIteratorActionLoop;->next(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v9
    invoke-static {v9}, Lloops/TestTryProtectedTwoStateIteratorActionLoop;->touch(Ljava/lang/Object;)V
    goto :action_call
    :try_end
    .catchall {:try_start .. :try_end} :catch_all

    :done
    sget-object v6, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object v6

    :suspended
    return-object v0

    :catch_all
    move-exception v6
    invoke-static {}, Lloops/TestTryProtectedTwoStateIteratorActionLoop;->cleanup()V
    throw v6
.end method
