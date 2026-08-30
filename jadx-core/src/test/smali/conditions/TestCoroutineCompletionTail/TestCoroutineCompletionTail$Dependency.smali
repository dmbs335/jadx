.class final Lconditions/TestCoroutineCompletionTail$Dependency;
.super Ljava/lang/Object;

.field private final mutex:Lkotlinx/coroutines/sync/Mutex;

.method public constructor <init>(Lkotlinx/coroutines/sync/Mutex;)V
    .registers 2

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    iput-object p1, p0, Lconditions/TestCoroutineCompletionTail$Dependency;->mutex:Lkotlinx/coroutines/sync/Mutex;
    return-void
.end method

.method public final getMutex()Lkotlinx/coroutines/sync/Mutex;
    .registers 2

    iget-object v0, p0, Lconditions/TestCoroutineCompletionTail$Dependency;->mutex:Lkotlinx/coroutines/sync/Mutex;
    return-object v0
.end method
