.class public final Lloops/TestCoroutineYieldFrameResumeLoop$State;
.super Lkotlin/coroutines/jvm/internal/ContinuationImpl;

.annotation system Ldalvik/annotation/EnclosingClass;
    value = Lloops/TestCoroutineYieldFrameResumeLoop;
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x19
    name = "State"
.end annotation

.field public label:I
.field public result:Ljava/lang/Object;
.field public L$0:Ljava/lang/Object;
.field public L$1:Ljava/lang/Object;

.method public constructor <init>(Lkotlin/coroutines/Continuation;)V
    .locals 0
    invoke-direct {p0, p1}, Lkotlin/coroutines/jvm/internal/ContinuationImpl;-><init>(Lkotlin/coroutines/Continuation;)V
    return-void
.end method

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .locals 2
    iput-object p1, p0, Lloops/TestCoroutineYieldFrameResumeLoop$State;->result:Ljava/lang/Object;
    iget v0, p0, Lloops/TestCoroutineYieldFrameResumeLoop$State;->label:I
    const/high16 v1, -0x80000000
    or-int/2addr v0, v1
    iput v0, p0, Lloops/TestCoroutineYieldFrameResumeLoop$State;->label:I
    const/4 v0, 0x0
    invoke-static {v0, v0, p0}, Lloops/TestCoroutineYieldFrameResumeLoop;->run(Lkotlin/sequences/SequenceScope;Lkotlin/coroutines/jvm/internal/CoroutineStackFrame;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v0
    return-object v0
.end method
