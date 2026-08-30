.class final Ljadx/tests/integration/loops/TestNamedTryProtectedCoroutineProgressLoop$run$1;
.super Lkotlin/coroutines/jvm/internal/ContinuationImpl;
.source "TestNamedTryProtectedCoroutineProgressLoop.kt"

.field I$0:I

.field I$1:I

.field L$0:Ljava/lang/Object;

.field label:I

.field result:Ljava/lang/Object;

.field final synthetic this$0:Ljadx/tests/integration/loops/TestNamedTryProtectedCoroutineProgressLoop;


.method constructor <init>(Ljadx/tests/integration/loops/TestNamedTryProtectedCoroutineProgressLoop;Lkotlin/coroutines/Continuation;)V
    .registers 3

    iput-object p1, p0, Ljadx/tests/integration/loops/TestNamedTryProtectedCoroutineProgressLoop$run$1;->this$0:Ljadx/tests/integration/loops/TestNamedTryProtectedCoroutineProgressLoop;

    invoke-direct {p0, p2}, Lkotlin/coroutines/jvm/internal/ContinuationImpl;-><init>(Lkotlin/coroutines/Continuation;)V

    return-void
.end method

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 4

    iput-object p1, p0, Ljadx/tests/integration/loops/TestNamedTryProtectedCoroutineProgressLoop$run$1;->result:Ljava/lang/Object;

    iget v0, p0, Ljadx/tests/integration/loops/TestNamedTryProtectedCoroutineProgressLoop$run$1;->label:I
    const/high16 v1, -0x80000000
    or-int/2addr v0, v1
    iput v0, p0, Ljadx/tests/integration/loops/TestNamedTryProtectedCoroutineProgressLoop$run$1;->label:I

    iget-object v0, p0, Ljadx/tests/integration/loops/TestNamedTryProtectedCoroutineProgressLoop$run$1;->this$0:Ljadx/tests/integration/loops/TestNamedTryProtectedCoroutineProgressLoop;
    const/4 v1, 0x0

    invoke-virtual {v0, v1, p0}, Ljadx/tests/integration/loops/TestNamedTryProtectedCoroutineProgressLoop;->run(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v0

    return-object v0
.end method
