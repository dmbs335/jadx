.class final Lconditions/TestCoroutineResumeExitDecisionTail$lastSuccess$1;
.super Lkotlin/coroutines/jvm/internal/ContinuationImpl;

.field L$0:Ljava/lang/Object;
.field I$0:I
.field label:I
.field synthetic result:Ljava/lang/Object;

.method public constructor <init>(Lkotlin/coroutines/Continuation;)V
    .registers 2
    invoke-direct {p0, p1}, Lkotlin/coroutines/jvm/internal/ContinuationImpl;-><init>(Lkotlin/coroutines/Continuation;)V
    return-void
.end method

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 4
    iput-object p1, p0, Lconditions/TestCoroutineResumeExitDecisionTail$lastSuccess$1;->result:Ljava/lang/Object;
    iget p1, p0, Lconditions/TestCoroutineResumeExitDecisionTail$lastSuccess$1;->label:I
    const/high16 v0, -0x80000000
    or-int/2addr p1, v0
    iput p1, p0, Lconditions/TestCoroutineResumeExitDecisionTail$lastSuccess$1;->label:I
    const/4 v0, 0x0
    invoke-static {v0, p0}, Lconditions/TestCoroutineResumeExitDecisionTail;->lastSuccess([Lkotlin/jvm/functions/Function1;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    return-object p1
.end method
