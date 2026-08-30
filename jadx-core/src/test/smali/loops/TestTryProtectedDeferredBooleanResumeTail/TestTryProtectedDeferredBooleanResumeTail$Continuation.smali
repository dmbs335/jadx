.class final Lloops/TestTryProtectedDeferredBooleanResumeTail$Continuation;
.super Lkotlin/coroutines/jvm/internal/ContinuationImpl;

.field public I$0:I
.field public I$1:I
.field public I$2:I
.field public J$0:J
.field public J$1:J
.field public L$0:Ljava/lang/Object;
.field public L$1:Ljava/lang/Object;
.field public L$2:Ljava/lang/Object;
.field public L$3:Ljava/lang/Object;
.field public L$4:Ljava/lang/Object;
.field public L$5:Ljava/lang/Object;
.field public L$6:Ljava/lang/Object;
.field public Z$0:Z
.field public label:I
.field public result:Ljava/lang/Object;

.method public constructor <init>(Lkotlin/coroutines/Continuation;)V
    .locals 0

    invoke-direct {p0, p1}, Lkotlin/coroutines/jvm/internal/ContinuationImpl;-><init>(Lkotlin/coroutines/Continuation;)V
    return-void
.end method

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .locals 4

    iput-object p1, p0, Lloops/TestTryProtectedDeferredBooleanResumeTail$Continuation;->result:Ljava/lang/Object;
    iget v0, p0, Lloops/TestTryProtectedDeferredBooleanResumeTail$Continuation;->label:I
    const/high16 v1, -0x80000000
    or-int/2addr v0, v1
    iput v0, p0, Lloops/TestTryProtectedDeferredBooleanResumeTail$Continuation;->label:I
    const/4 v0, 0x0
    const-wide/16 v1, 0x0
    invoke-static {v0, v1, v2, p0}, Lloops/TestTryProtectedDeferredBooleanResumeTail;->clear(Ljava/util/concurrent/ConcurrentHashMap;JLkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v3
    return-object v3
.end method
