.class public final Lloops/TestCoroutineTwoStateFrameRequestLoop;
.super Ljava/lang/Object;

.field private final stateLock:Ljava/lang/Object;

.method public constructor <init>()V
    .registers 2

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    new-instance v0, Ljava/lang/Object;
    invoke-direct {v0}, Ljava/lang/Object;-><init>()V
    iput-object v0, p0, Lloops/TestCoroutineTwoStateFrameRequestLoop;->stateLock:Ljava/lang/Object;

    return-void
.end method

.method public static onFrame(Lloops/TestCoroutineTwoStateFrameRequestLoop;Ljava/util/List;Ljava/util/List;Landroidx/compose/runtime/ProduceFrameSignal;Ljava/lang/Object;)Ljava/lang/Object;
    .registers 5

    sget-object p0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object p0
.end method

.method public final runFrameLoop(Landroidx/compose/runtime/MonotonicFrameClock;Landroidx/compose/runtime/ProduceFrameSignal;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 10

    instance-of v0, p3, Lloops/FrameLoopState;
    if-eqz v0, :new_state

    move-object v0, p3
    check-cast v0, Lloops/FrameLoopState;
    iget v1, v0, Lloops/FrameLoopState;->label:I
    const/high16 v2, -0x80000000
    and-int v5, v1, v2
    if-eqz v5, :new_state

    sub-int/2addr v1, v2
    iput v1, v0, Lloops/FrameLoopState;->label:I
    goto :state_ready

    :new_state
    new-instance v0, Lloops/FrameLoopState;
    invoke-direct {v0, p0, p3}, Lloops/FrameLoopState;-><init>(Lloops/TestCoroutineTwoStateFrameRequestLoop;Lkotlin/coroutines/Continuation;)V

    :state_ready
    iget-object p3, v0, Lloops/FrameLoopState;->result:Ljava/lang/Object;
    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v1
    iget v2, v0, Lloops/FrameLoopState;->label:I
    const/4 v3, 0x2
    const/4 v4, 0x1
    if-eqz v2, :state_zero
    if-eq v2, v4, :state_one
    if-ne v2, v3, :bad_state

    iget-object p1, v0, Lloops/FrameLoopState;->L$3:Ljava/lang/Object;
    check-cast p1, Ljava/util/List;
    iget-object p2, v0, Lloops/FrameLoopState;->L$2:Ljava/lang/Object;
    check-cast p2, Ljava/util/List;
    iget-object v2, v0, Lloops/FrameLoopState;->L$1:Ljava/lang/Object;
    check-cast v2, Landroidx/compose/runtime/ProduceFrameSignal;
    iget-object v5, v0, Lloops/FrameLoopState;->L$0:Ljava/lang/Object;
    check-cast v5, Landroidx/compose/runtime/MonotonicFrameClock;
    invoke-static {p3}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V

    :frame_complete
    move-object p3, p2
    move-object p2, v2
    move-object v2, p1
    move-object p1, v5
    goto :loop_header

    :bad_state
    new-instance p1, Ljava/lang/IllegalStateException;
    const-string p2, "call to 'resume' before 'invoke' with coroutine"
    invoke-direct {p1, p2}, Ljava/lang/IllegalStateException;-><init>(Ljava/lang/String;)V
    throw p1

    :state_one
    iget-object p1, v0, Lloops/FrameLoopState;->L$3:Ljava/lang/Object;
    check-cast p1, Ljava/util/List;
    iget-object p2, v0, Lloops/FrameLoopState;->L$2:Ljava/lang/Object;
    check-cast p2, Ljava/util/List;
    iget-object v2, v0, Lloops/FrameLoopState;->L$1:Ljava/lang/Object;
    check-cast v2, Landroidx/compose/runtime/ProduceFrameSignal;
    iget-object v5, v0, Lloops/FrameLoopState;->L$0:Ljava/lang/Object;
    check-cast v5, Landroidx/compose/runtime/MonotonicFrameClock;
    invoke-static {p3}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :frame_call

    :state_zero
    invoke-static {p3}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    new-instance p3, Ljava/util/ArrayList;
    invoke-direct {p3}, Ljava/util/ArrayList;-><init>()V
    new-instance v2, Ljava/util/ArrayList;
    invoke-direct {v2}, Ljava/util/ArrayList;-><init>()V

    :loop_header
    iget-object v5, p0, Lloops/TestCoroutineTwoStateFrameRequestLoop;->stateLock:Ljava/lang/Object;
    iput-object p1, v0, Lloops/FrameLoopState;->L$0:Ljava/lang/Object;
    iput-object p2, v0, Lloops/FrameLoopState;->L$1:Ljava/lang/Object;
    iput-object p3, v0, Lloops/FrameLoopState;->L$2:Ljava/lang/Object;
    iput-object v2, v0, Lloops/FrameLoopState;->L$3:Ljava/lang/Object;
    iput v4, v0, Lloops/FrameLoopState;->label:I
    invoke-virtual {p2, v5, v0}, Landroidx/compose/runtime/ProduceFrameSignal;->awaitFrameRequest(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v5
    if-ne v5, v1, :await_complete
    goto :suspended

    :await_complete
    move-object v5, p1
    move-object p1, v2
    move-object v2, p2
    move-object p2, p3

    :frame_call
    new-instance p3, Lloops/Callback;
    invoke-direct {p3, p0, p2, p1, v2}, Lloops/Callback;-><init>(Lloops/TestCoroutineTwoStateFrameRequestLoop;Ljava/util/List;Ljava/util/List;Landroidx/compose/runtime/ProduceFrameSignal;)V
    iput-object v5, v0, Lloops/FrameLoopState;->L$0:Ljava/lang/Object;
    iput-object v2, v0, Lloops/FrameLoopState;->L$1:Ljava/lang/Object;
    iput-object p2, v0, Lloops/FrameLoopState;->L$2:Ljava/lang/Object;
    iput-object p1, v0, Lloops/FrameLoopState;->L$3:Ljava/lang/Object;
    iput v3, v0, Lloops/FrameLoopState;->label:I
    invoke-interface {v5, p3, v0}, Landroidx/compose/runtime/MonotonicFrameClock;->withFrameNanos(Lkotlin/jvm/functions/Function1;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p3
    if-ne p3, v1, :frame_complete

    :suspended
    return-object v1
.end method
