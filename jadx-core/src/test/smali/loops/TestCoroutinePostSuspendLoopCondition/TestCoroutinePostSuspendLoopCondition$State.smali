.class public final Lloops/TestCoroutinePostSuspendLoopCondition$State;
.super Lkotlin/coroutines/jvm/internal/ContinuationImpl;

.field public L$0:Ljava/lang/Object;
.field public L$1:Ljava/lang/Object;
.field public label:I
.field public result:Ljava/lang/Object;

.method public constructor <init>(Lkotlin/coroutines/Continuation;)V
    .registers 2
    invoke-direct {p0, p1}, Lkotlin/coroutines/jvm/internal/ContinuationImpl;-><init>(Lkotlin/coroutines/Continuation;)V
    return-void
.end method

.method protected invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 6
    iput-object p1, p0, Lloops/TestCoroutinePostSuspendLoopCondition$State;->result:Ljava/lang/Object;
    iget v0, p0, Lloops/TestCoroutinePostSuspendLoopCondition$State;->label:I
    const/high16 v1, -0x80000000
    or-int/2addr v0, v1
    iput v0, p0, Lloops/TestCoroutinePostSuspendLoopCondition$State;->label:I
    const/4 v0, 0x0
    invoke-static {v0, v0, p1, p0}, Lloops/TestCoroutinePostSuspendLoopCondition;->P(Lkotlin/jvm/functions/Function2;Lkotlin/jvm/internal/Ref$ObjectRef;Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v0
    return-object v0
.end method
