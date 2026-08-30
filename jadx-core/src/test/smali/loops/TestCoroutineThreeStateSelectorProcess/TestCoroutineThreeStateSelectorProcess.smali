.class public final Lloops/TestCoroutineThreeStateSelectorProcess;
.super Ljava/lang/Object;

.field private closed:Z
.field private pending:I

.method private static decodeString(I)Ljava/lang/String;
    .registers 2
    const-string v0, "selector"
    return-object v0
.end method

.method public final process(Lio/ktor/network/selector/LockFreeMPSCQueue;Ljava/nio/channels/Selector;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 16

    instance-of v0, p3, Lloops/TestCoroutineThreeStateSelectorProcess$process$1;
    if-eqz v0, :new_state
    move-object v0, p3
    check-cast v0, Lloops/TestCoroutineThreeStateSelectorProcess$process$1;
    iget v2, v0, Lloops/TestCoroutineThreeStateSelectorProcess$process$1;->label:I
    const/high16 v3, -0x80000000
    and-int v4, v2, v3
    if-eqz v4, :new_state
    sub-int/2addr v2, v3
    iput v2, v0, Lloops/TestCoroutineThreeStateSelectorProcess$process$1;->label:I
    goto :state_ready

    :new_state
    new-instance v0, Lloops/TestCoroutineThreeStateSelectorProcess$process$1;
    invoke-direct {v0, p0, p3}, Lloops/TestCoroutineThreeStateSelectorProcess$process$1;-><init>(Lloops/TestCoroutineThreeStateSelectorProcess;Lkotlin/coroutines/Continuation;)V

    :state_ready
    iget-object v12, v0, Lloops/TestCoroutineThreeStateSelectorProcess$process$1;->result:Ljava/lang/Object;
    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v1
    iget v2, v0, Lloops/TestCoroutineThreeStateSelectorProcess$process$1;->label:I
    const/4 v3, 0x1
    invoke-static {v3}, Lloops/TestCoroutineThreeStateSelectorProcess;->decodeString(I)Ljava/lang/String;
    move-result-object v4
    const/4 v5, 0x2
    invoke-static {v5}, Lloops/TestCoroutineThreeStateSelectorProcess;->decodeString(I)Ljava/lang/String;
    move-result-object v6

    if-eqz v2, :initial
    if-ne v2, v3, :check_state_two
    iget-object p2, v0, Lloops/TestCoroutineThreeStateSelectorProcess$process$1;->L$1:Ljava/lang/Object;
    check-cast p2, Ljava/nio/channels/Selector;
    iget-object p1, v0, Lloops/TestCoroutineThreeStateSelectorProcess$process$1;->L$0:Ljava/lang/Object;
    check-cast p1, Lio/ktor/network/selector/LockFreeMPSCQueue;
    invoke-static {v12}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :after_select

    :check_state_two
    if-ne v2, v5, :check_state_three
    iget-object p2, v0, Lloops/TestCoroutineThreeStateSelectorProcess$process$1;->L$1:Ljava/lang/Object;
    check-cast p2, Ljava/nio/channels/Selector;
    iget-object p1, v0, Lloops/TestCoroutineThreeStateSelectorProcess$process$1;->L$0:Ljava/lang/Object;
    check-cast p1, Lio/ktor/network/selector/LockFreeMPSCQueue;
    invoke-static {v12}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :loop

    :check_state_three
    const/4 v7, 0x3
    if-ne v2, v7, :bad_state
    iget-object p2, v0, Lloops/TestCoroutineThreeStateSelectorProcess$process$1;->L$1:Ljava/lang/Object;
    check-cast p2, Ljava/nio/channels/Selector;
    iget-object p1, v0, Lloops/TestCoroutineThreeStateSelectorProcess$process$1;->L$0:Ljava/lang/Object;
    check-cast p1, Lio/ktor/network/selector/LockFreeMPSCQueue;
    invoke-static {v12}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :after_receive

    :bad_state
    new-instance v2, Ljava/lang/IllegalStateException;
    invoke-direct {v2}, Ljava/lang/IllegalStateException;-><init>()V
    throw v2

    :initial
    invoke-static {v12}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V

    :loop
    iget-boolean v7, p0, Lloops/TestCoroutineThreeStateSelectorProcess;->closed:Z
    if-nez v7, :done
    iget v7, p0, Lloops/TestCoroutineThreeStateSelectorProcess;->pending:I
    if-lez v7, :receive_call

    iput-object p1, v0, Lloops/TestCoroutineThreeStateSelectorProcess$process$1;->L$0:Ljava/lang/Object;
    iput-object p2, v0, Lloops/TestCoroutineThreeStateSelectorProcess$process$1;->L$1:Ljava/lang/Object;
    iput v3, v0, Lloops/TestCoroutineThreeStateSelectorProcess$process$1;->label:I
    invoke-virtual {p0, p2, v0}, Lloops/TestCoroutineThreeStateSelectorProcess;->select(Ljava/nio/channels/Selector;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v12
    if-eq v12, v1, :suspended

    :after_select
    check-cast v12, Ljava/lang/Number;
    invoke-virtual {v12}, Ljava/lang/Number;->intValue()I
    move-result v7
    if-lez v7, :queue_fallback
    invoke-virtual {p2}, Ljava/nio/channels/Selector;->selectedKeys()Ljava/util/Set;
    move-result-object v8
    invoke-virtual {p2}, Ljava/nio/channels/Selector;->keys()Ljava/util/Set;
    move-result-object v9
    invoke-static {v8, v9}, Lloops/TestCoroutineThreeStateSelectorProcess;->consumeSets(Ljava/util/Set;Ljava/util/Set;)V
    goto :loop

    :queue_fallback
    invoke-virtual {p1}, Lio/ktor/network/selector/LockFreeMPSCQueue;->removeFirstOrNull()Ljava/lang/Object;
    move-result-object v10
    if-nez v10, :handle
    iput-object p1, v0, Lloops/TestCoroutineThreeStateSelectorProcess$process$1;->L$0:Ljava/lang/Object;
    iput-object p2, v0, Lloops/TestCoroutineThreeStateSelectorProcess$process$1;->L$1:Ljava/lang/Object;
    iput v5, v0, Lloops/TestCoroutineThreeStateSelectorProcess$process$1;->label:I
    invoke-static {v0}, Lkotlinx/coroutines/YieldKt;->yield(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v12
    if-eq v12, v1, :suspended
    goto :loop

    :receive_call
    invoke-virtual {p2}, Ljava/nio/channels/Selector;->selectNow()I
    move-result v7
    iput-object p1, v0, Lloops/TestCoroutineThreeStateSelectorProcess$process$1;->L$0:Ljava/lang/Object;
    iput-object p2, v0, Lloops/TestCoroutineThreeStateSelectorProcess$process$1;->L$1:Ljava/lang/Object;
    const/4 v7, 0x3
    iput v7, v0, Lloops/TestCoroutineThreeStateSelectorProcess$process$1;->label:I
    invoke-virtual {p0, p1, v0}, Lloops/TestCoroutineThreeStateSelectorProcess;->receive(Lio/ktor/network/selector/LockFreeMPSCQueue;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v12
    if-eq v12, v1, :suspended

    :after_receive
    move-object v10, v12
    if-eqz v10, :done

    :handle
    invoke-static {v10}, Lloops/TestCoroutineThreeStateSelectorProcess;->handle(Ljava/lang/Object;)V
    goto :loop

    :suspended
    return-object v1

    :done
    sget-object v12, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object v12
.end method

.method private final select(Ljava/nio/channels/Selector;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 4
    const/4 v0, 0x0
    invoke-static {v0}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
    move-result-object v0
    return-object v0
.end method

.method private final receive(Lio/ktor/network/selector/LockFreeMPSCQueue;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 4
    const/4 v0, 0x0
    return-object v0
.end method

.method private static consumeSets(Ljava/util/Set;Ljava/util/Set;)V
    .registers 2
    return-void
.end method

.method private static handle(Ljava/lang/Object;)V
    .registers 1
    return-void
.end method
