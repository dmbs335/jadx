.class final Lloops/TestCoroutineProtectedTypedRestoreCompletion$loop$1;
.super Lkotlin/coroutines/jvm/internal/ContinuationImpl;

.field L$0:Ljava/lang/Object;
.field L$1:Ljava/lang/Object;
.field label:I
.field result:Ljava/lang/Object;
.field final synthetic this$0:Lloops/TestCoroutineProtectedTypedRestoreCompletion;

.method constructor <init>(Lloops/TestCoroutineProtectedTypedRestoreCompletion;Lkotlin/coroutines/Continuation;)V
    .registers 3
    iput-object p1, p0, Lloops/TestCoroutineProtectedTypedRestoreCompletion$loop$1;->this$0:Lloops/TestCoroutineProtectedTypedRestoreCompletion;
    invoke-direct {p0, p2}, Lkotlin/coroutines/jvm/internal/ContinuationImpl;-><init>(Lkotlin/coroutines/Continuation;)V
    return-void
.end method

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 4
    iput-object p1, p0, Lloops/TestCoroutineProtectedTypedRestoreCompletion$loop$1;->result:Ljava/lang/Object;
    iget v0, p0, Lloops/TestCoroutineProtectedTypedRestoreCompletion$loop$1;->label:I
    const/high16 v1, -0x80000000
    or-int/2addr v0, v1
    iput v0, p0, Lloops/TestCoroutineProtectedTypedRestoreCompletion$loop$1;->label:I
    iget-object v0, p0, Lloops/TestCoroutineProtectedTypedRestoreCompletion$loop$1;->this$0:Lloops/TestCoroutineProtectedTypedRestoreCompletion;
    const/4 v1, 0x0
    invoke-virtual {v0, v1, p0}, Lloops/TestCoroutineProtectedTypedRestoreCompletion;->loop(Ljava/lang/StringBuilder;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v0
    return-object v0
.end method
