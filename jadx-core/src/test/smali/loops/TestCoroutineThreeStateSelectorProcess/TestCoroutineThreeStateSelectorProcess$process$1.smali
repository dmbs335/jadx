.class final Lloops/TestCoroutineThreeStateSelectorProcess$process$1;
.super Lkotlin/coroutines/jvm/internal/ContinuationImpl;

.field L$0:Ljava/lang/Object;
.field L$1:Ljava/lang/Object;
.field L$2:Ljava/lang/Object;
.field label:I
.field synthetic result:Ljava/lang/Object;
.field final synthetic this$0:Lloops/TestCoroutineThreeStateSelectorProcess;

.method public constructor <init>(Lloops/TestCoroutineThreeStateSelectorProcess;Lkotlin/coroutines/Continuation;)V
    .registers 3
    iput-object p1, p0, Lloops/TestCoroutineThreeStateSelectorProcess$process$1;->this$0:Lloops/TestCoroutineThreeStateSelectorProcess;
    invoke-direct {p0, p2}, Lkotlin/coroutines/jvm/internal/ContinuationImpl;-><init>(Lkotlin/coroutines/Continuation;)V
    return-void
.end method

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 6
    iput-object p1, p0, Lloops/TestCoroutineThreeStateSelectorProcess$process$1;->result:Ljava/lang/Object;
    iget p1, p0, Lloops/TestCoroutineThreeStateSelectorProcess$process$1;->label:I
    const/high16 v0, -0x80000000
    or-int/2addr p1, v0
    iput p1, p0, Lloops/TestCoroutineThreeStateSelectorProcess$process$1;->label:I
    iget-object v0, p0, Lloops/TestCoroutineThreeStateSelectorProcess$process$1;->this$0:Lloops/TestCoroutineThreeStateSelectorProcess;
    const/4 v1, 0x0
    move-object v2, v1
    move-object v3, p0
    invoke-virtual {v0, v1, v2, v3}, Lloops/TestCoroutineThreeStateSelectorProcess;->process(Lio/ktor/network/selector/LockFreeMPSCQueue;Ljava/nio/channels/Selector;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    return-object p1
.end method
