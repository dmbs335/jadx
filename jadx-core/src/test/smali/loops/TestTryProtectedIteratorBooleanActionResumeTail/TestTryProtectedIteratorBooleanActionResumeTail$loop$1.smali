.class final Lloops/TestTryProtectedIteratorBooleanActionResumeTail$loop$1;
.super Lkotlin/coroutines/jvm/internal/ContinuationImpl;

.field I$0:I
.field L$0:Ljava/lang/Object;
.field L$1:Ljava/lang/Object;
.field Z$0:Z
.field synthetic result:Ljava/lang/Object;
.field x:I

.method public constructor <init>(Lkotlin/coroutines/Continuation;)V
    .locals 0
    invoke-direct {p0, p1}, Lkotlin/coroutines/jvm/internal/ContinuationImpl;-><init>(Lkotlin/coroutines/Continuation;)V
    return-void
.end method

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .locals 2
    iput-object p1, p0, Lloops/TestTryProtectedIteratorBooleanActionResumeTail$loop$1;->result:Ljava/lang/Object;
    iget p1, p0, Lloops/TestTryProtectedIteratorBooleanActionResumeTail$loop$1;->x:I
    const/high16 v0, -0x80000000
    or-int/2addr p1, v0
    iput p1, p0, Lloops/TestTryProtectedIteratorBooleanActionResumeTail$loop$1;->x:I
    const/4 p1, 0x0
    const/4 v0, 0x0
    invoke-static {p1, v0, p0}, Lloops/TestTryProtectedIteratorBooleanActionResumeTail;->loop(Ljava/util/List;ZLkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    return-object p1
.end method
