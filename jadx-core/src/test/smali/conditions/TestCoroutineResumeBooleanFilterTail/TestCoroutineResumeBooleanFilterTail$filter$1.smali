.class final Lconditions/TestCoroutineResumeBooleanFilterTail$filter$1;
.super Lkotlin/coroutines/jvm/internal/ContinuationImpl;

.field L$0:Ljava/lang/Object;
.field L$1:Ljava/lang/Object;
.field L$2:Ljava/lang/Object;
.field L$3:Ljava/lang/Object;
.field L$4:Ljava/lang/Object;
.field L$5:Ljava/lang/Object;
.field label:I
.field synthetic result:Ljava/lang/Object;

.method public constructor <init>(Lkotlin/coroutines/Continuation;)V
    .registers 2
    invoke-direct {p0, p1}, Lkotlin/coroutines/jvm/internal/ContinuationImpl;-><init>(Lkotlin/coroutines/Continuation;)V
    return-void
.end method

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 6
    iput-object p1, p0, Lconditions/TestCoroutineResumeBooleanFilterTail$filter$1;->result:Ljava/lang/Object;
    iget p1, p0, Lconditions/TestCoroutineResumeBooleanFilterTail$filter$1;->label:I
    const/high16 v0, -0x80000000
    or-int/2addr p1, v0
    iput p1, p0, Lconditions/TestCoroutineResumeBooleanFilterTail$filter$1;->label:I
    const/4 v0, 0x0
    invoke-static {v0, v0, v0, p0}, Lconditions/TestCoroutineResumeBooleanFilterTail;->filter(Ljava/lang/Iterable;Lkotlin/jvm/functions/Function2;Lkotlin/jvm/functions/Function2;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    return-object p1
.end method
