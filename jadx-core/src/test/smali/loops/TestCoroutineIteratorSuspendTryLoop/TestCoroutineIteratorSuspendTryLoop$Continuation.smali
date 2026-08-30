.class final Lloops/TestCoroutineIteratorSuspendTryLoop$Continuation;
.super Lkotlin/coroutines/jvm/internal/ContinuationImpl;

.field public isCancelled:Z
.field public iterator:Ljava/util/Iterator;
.field public key:Ljava/lang/Object;
.field public label:I
.field public map:Ljava/util/Map;
.field public result:Ljava/lang/Object;
.field public targetTime:J

.method public constructor <init>(Lkotlin/coroutines/Continuation;)V
    .locals 0

    invoke-direct {p0, p1}, Lkotlin/coroutines/jvm/internal/ContinuationImpl;-><init>(Lkotlin/coroutines/Continuation;)V
    return-void
.end method

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .locals 3

    iput-object p1, p0, Lloops/TestCoroutineIteratorSuspendTryLoop$Continuation;->result:Ljava/lang/Object;
    iget v0, p0, Lloops/TestCoroutineIteratorSuspendTryLoop$Continuation;->label:I
    const/high16 v1, -0x80000000
    or-int/2addr v0, v1
    iput v0, p0, Lloops/TestCoroutineIteratorSuspendTryLoop$Continuation;->label:I
    const/4 v0, 0x0
    const-wide/16 v1, 0x0
    invoke-static {v0, v1, v2, p0}, Lloops/TestCoroutineIteratorSuspendTryLoop;->clear(Ljava/util/Map;JLkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v0
    return-object v0
.end method
