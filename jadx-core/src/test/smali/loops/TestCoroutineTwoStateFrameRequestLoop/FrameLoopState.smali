.class final Lloops/FrameLoopState;
.super Lkotlin/coroutines/jvm/internal/ContinuationImpl;

.field public L$0:Ljava/lang/Object;
.field public L$1:Ljava/lang/Object;
.field public L$2:Ljava/lang/Object;
.field public L$3:Ljava/lang/Object;
.field public label:I
.field public result:Ljava/lang/Object;
.field private final owner:Lloops/TestCoroutineTwoStateFrameRequestLoop;

.method public constructor <init>(Lloops/TestCoroutineTwoStateFrameRequestLoop;Lkotlin/coroutines/Continuation;)V
    .registers 3

    iput-object p1, p0, Lloops/FrameLoopState;->owner:Lloops/TestCoroutineTwoStateFrameRequestLoop;
    invoke-direct {p0, p2}, Lkotlin/coroutines/jvm/internal/ContinuationImpl;-><init>(Lkotlin/coroutines/Continuation;)V
    return-void
.end method

.method protected invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 5

    iput-object p1, p0, Lloops/FrameLoopState;->result:Ljava/lang/Object;
    iget v0, p0, Lloops/FrameLoopState;->label:I
    const/high16 v1, -0x80000000
    or-int/2addr v0, v1
    iput v0, p0, Lloops/FrameLoopState;->label:I
    iget-object v0, p0, Lloops/FrameLoopState;->owner:Lloops/TestCoroutineTwoStateFrameRequestLoop;
    const/4 v1, 0x0
    invoke-virtual {v0, v1, v1, p0}, Lloops/TestCoroutineTwoStateFrameRequestLoop;->runFrameLoop(Landroidx/compose/runtime/MonotonicFrameClock;Landroidx/compose/runtime/ProduceFrameSignal;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v0
    return-object v0
.end method
