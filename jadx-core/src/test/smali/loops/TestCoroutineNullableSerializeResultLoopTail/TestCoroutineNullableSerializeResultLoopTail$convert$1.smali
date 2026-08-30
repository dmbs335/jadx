.class final Lloops/TestCoroutineNullableSerializeResultLoopTail$convert$1;
.super Lkotlin/coroutines/jvm/internal/ContinuationImpl;

.field L$0:Ljava/lang/Object;
.field L$1:Ljava/lang/Object;
.field L$2:Ljava/lang/Object;
.field label:I
.field synthetic result:Ljava/lang/Object;

.method constructor <init>(Lkotlin/coroutines/Continuation;)V
    .registers 2
    invoke-direct {p0, p1}, Lkotlin/coroutines/jvm/internal/ContinuationImpl;-><init>(Lkotlin/coroutines/Continuation;)V
    return-void
.end method

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 5
    iput-object p1, p0, Lloops/TestCoroutineNullableSerializeResultLoopTail$convert$1;->result:Ljava/lang/Object;
    iget v0, p0, Lloops/TestCoroutineNullableSerializeResultLoopTail$convert$1;->label:I
    const/high16 v1, -0x80000000
    or-int/2addr v0, v1
    iput v0, p0, Lloops/TestCoroutineNullableSerializeResultLoopTail$convert$1;->label:I
    const/4 v0, 0x0
    invoke-static {v0, v0, v0, p0}, Lloops/TestCoroutineNullableSerializeResultLoopTail;->convert(Ljava/util/Iterator;Ltest/ContentConverter;Lorg/slf4j/Logger;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v0
    return-object v0
.end method
