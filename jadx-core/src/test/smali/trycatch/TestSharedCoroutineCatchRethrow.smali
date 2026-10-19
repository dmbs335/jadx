.class public final Ltrycatch/TestSharedCoroutineCatchRethrow;
.super Lkotlin/coroutines/jvm/internal/SuspendLambda;

.field private final state:I
.field private final failLocation:I
.field private final suspend:Z

.method public constructor <init>()V
    .registers 4
    const/4 v0, 0x0
    const/4 v1, -0x1
    const/4 v2, 0x0
    invoke-direct {p0, v0, v1, v2}, Ltrycatch/TestSharedCoroutineCatchRethrow;-><init>(IIZ)V
    return-void
.end method

.method public constructor <init>(IIZ)V
    .registers 6
    const/4 v0, 0x2
    const/4 v1, 0x0
    invoke-direct {p0, v0, v1}, Lkotlin/coroutines/jvm/internal/SuspendLambda;-><init>(ILkotlin/coroutines/Continuation;)V
    iput p1, p0, Ltrycatch/TestSharedCoroutineCatchRethrow;->state:I
    iput p2, p0, Ltrycatch/TestSharedCoroutineCatchRethrow;->failLocation:I
    iput-boolean p3, p0, Ltrycatch/TestSharedCoroutineCatchRethrow;->suspend:Z
    return-void
.end method

.method public invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 7
    invoke-static {}, Ljadx/tests/integration/trycatch/TestSharedCoroutineCatchRethrow;->suspended()Ljava/lang/Object;
    move-result-object v4
    iget v0, p0, Ltrycatch/TestSharedCoroutineCatchRethrow;->state:I
    iget v3, p0, Ltrycatch/TestSharedCoroutineCatchRethrow;->failLocation:I
    packed-switch v0, :states
    new-instance v0, Ljava/lang/IllegalStateException;
    invoke-direct {v0}, Ljava/lang/IllegalStateException;-><init>()V
    throw v0

    :state_zero
    :try_start
    const/4 v0, 0x0
    invoke-static {v0, v3}, Ljadx/tests/integration/trycatch/TestSharedCoroutineCatchRethrow;->probe(II)V
    goto :normal

    :state_one
    const/4 v0, 0x1
    invoke-static {v0, v3}, Ljadx/tests/integration/trycatch/TestSharedCoroutineCatchRethrow;->probe(II)V
    goto :normal
    :try_end
    .catch Ljava/lang/RuntimeException; {:try_start .. :try_end} :catch

    :normal
    const/4 v0, 0x2
    invoke-static {v0, v3}, Ljadx/tests/integration/trycatch/TestSharedCoroutineCatchRethrow;->probe(II)V
    invoke-static {}, Ljadx/tests/integration/trycatch/TestSharedCoroutineCatchRethrow;->done()Ljava/lang/Object;
    move-result-object v0
    return-object v0

    :state_two
    invoke-static {}, Ljadx/tests/integration/trycatch/TestSharedCoroutineCatchRethrow;->saved()Ljava/lang/RuntimeException;
    move-result-object v0
    goto :shared

    :catch
    move-exception v1
    invoke-static {v1}, Ljadx/tests/integration/trycatch/TestSharedCoroutineCatchRethrow;->save(Ljava/lang/RuntimeException;)V
    iget-boolean v2, p0, Ltrycatch/TestSharedCoroutineCatchRethrow;->suspend:Z
    invoke-static {v2}, Ljadx/tests/integration/trycatch/TestSharedCoroutineCatchRethrow;->cleanup(Z)Ljava/lang/Object;
    move-result-object v2
    if-ne v2, v4, :direct
    return-object v4

    :direct
    move-object v0, v1
    :shared
    move-object v1, v0
    throw v1

    :states
    .packed-switch 0x0
        :state_zero
        :state_one
        :state_two
    .end packed-switch
.end method

.method public static check()V
    .registers 1
    .annotation system Ldalvik/annotation/Throws;
        value = { Ljava/lang/Exception; }
    .end annotation
    const-class v0, Ltrycatch/TestSharedCoroutineCatchRethrow;
    invoke-static {v0}, Ljadx/tests/integration/trycatch/TestSharedCoroutineCatchRethrow;->check(Ljava/lang/Class;)V
    return-void
.end method
