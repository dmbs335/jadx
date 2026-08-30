.class final Lconditions/TestCoroutineCompletionTail$getAll$1;
.super Lkotlin/coroutines/jvm/internal/ContinuationImpl;

.field L$0:Ljava/lang/Object;
.field L$1:Ljava/lang/Object;
.field L$2:Ljava/lang/Object;
.field L$3:Ljava/lang/Object;
.field L$4:Ljava/lang/Object;
.field L$5:Ljava/lang/Object;
.field label:I
.field synthetic result:Ljava/lang/Object;
.field final synthetic this$0:Lconditions/TestCoroutineCompletionTail;

.method public constructor <init>(Lconditions/TestCoroutineCompletionTail;Lkotlin/coroutines/Continuation;)V
    .registers 3

    iput-object p1, p0, Lconditions/TestCoroutineCompletionTail$getAll$1;->this$0:Lconditions/TestCoroutineCompletionTail;
    invoke-direct {p0, p2}, Lkotlin/coroutines/jvm/internal/ContinuationImpl;-><init>(Lkotlin/coroutines/Continuation;)V
    return-void
.end method

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 3

    iput-object p1, p0, Lconditions/TestCoroutineCompletionTail$getAll$1;->result:Ljava/lang/Object;
    iget p1, p0, Lconditions/TestCoroutineCompletionTail$getAll$1;->label:I
    const/high16 v0, -0x80000000
    or-int/2addr p1, v0
    iput p1, p0, Lconditions/TestCoroutineCompletionTail$getAll$1;->label:I
    iget-object p1, p0, Lconditions/TestCoroutineCompletionTail$getAll$1;->this$0:Lconditions/TestCoroutineCompletionTail;
    invoke-virtual {p1, p0}, Lconditions/TestCoroutineCompletionTail;->getAll(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    return-object p1
.end method
