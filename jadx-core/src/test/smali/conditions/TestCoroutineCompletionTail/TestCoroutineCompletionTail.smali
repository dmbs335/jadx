.class public final Lconditions/TestCoroutineCompletionTail;
.super Ljava/lang/Object;

.field public static final INSTANCE:Lconditions/TestCoroutineCompletionTail;
.field private static final dependencies:Ljava/util/Map;

.method static constructor <clinit>()V
    .registers 2

    new-instance v0, Lconditions/TestCoroutineCompletionTail;
    invoke-direct {v0}, Lconditions/TestCoroutineCompletionTail;-><init>()V
    sput-object v0, Lconditions/TestCoroutineCompletionTail;->INSTANCE:Lconditions/TestCoroutineCompletionTail;

    new-instance v0, Ljava/util/LinkedHashMap;
    invoke-direct {v0}, Ljava/util/LinkedHashMap;-><init>()V
    sput-object v0, Lconditions/TestCoroutineCompletionTail;->dependencies:Ljava/util/Map;
    return-void
.end method

.method public constructor <init>()V
    .registers 1

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    return-void
.end method

.method public final getAll(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 12

    instance-of v0, p1, Lconditions/TestCoroutineCompletionTail$getAll$1;
    if-eqz v0, :new_state

    move-object v0, p1
    check-cast v0, Lconditions/TestCoroutineCompletionTail$getAll$1;
    iget v1, v0, Lconditions/TestCoroutineCompletionTail$getAll$1;->label:I
    const/high16 v2, -0x80000000
    and-int v3, v1, v2
    if-eqz v3, :new_state

    sub-int/2addr v1, v2
    iput v1, v0, Lconditions/TestCoroutineCompletionTail$getAll$1;->label:I
    goto :state_ready

    :new_state
    new-instance v0, Lconditions/TestCoroutineCompletionTail$getAll$1;
    invoke-direct {v0, p0, p1}, Lconditions/TestCoroutineCompletionTail$getAll$1;-><init>(Lconditions/TestCoroutineCompletionTail;Lkotlin/coroutines/Continuation;)V

    :state_ready
    iget-object p1, v0, Lconditions/TestCoroutineCompletionTail$getAll$1;->result:Ljava/lang/Object;
    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v1
    iget v2, v0, Lconditions/TestCoroutineCompletionTail$getAll$1;->label:I
    const/4 v3, 0x1
    const/4 v4, 0x0

    if-eqz v2, :initial
    if-ne v2, v3, :bad_state

    iget-object v2, v0, Lconditions/TestCoroutineCompletionTail$getAll$1;->L$5:Ljava/lang/Object;
    iget-object v5, v0, Lconditions/TestCoroutineCompletionTail$getAll$1;->L$4:Ljava/lang/Object;
    check-cast v5, Ljava/util/Map;
    iget-object v6, v0, Lconditions/TestCoroutineCompletionTail$getAll$1;->L$3:Ljava/lang/Object;
    check-cast v6, Lkotlinx/coroutines/sync/Mutex;
    iget-object v7, v0, Lconditions/TestCoroutineCompletionTail$getAll$1;->L$2:Ljava/lang/Object;
    iget-object v8, v0, Lconditions/TestCoroutineCompletionTail$getAll$1;->L$1:Ljava/lang/Object;
    check-cast v8, Ljava/util/Iterator;
    iget-object v9, v0, Lconditions/TestCoroutineCompletionTail$getAll$1;->L$0:Ljava/lang/Object;
    check-cast v9, Ljava/util/Map;
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :completion

    :bad_state
    new-instance p1, Ljava/lang/IllegalStateException;
    const-string v0, "call to 'resume' before 'invoke' with coroutine"
    invoke-direct {p1, v0}, Ljava/lang/IllegalStateException;-><init>(Ljava/lang/String;)V
    throw p1

    :initial
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    sget-object p1, Lconditions/TestCoroutineCompletionTail;->dependencies:Ljava/util/Map;
    new-instance v5, Ljava/util/LinkedHashMap;
    invoke-direct {v5}, Ljava/util/LinkedHashMap;-><init>()V
    invoke-interface {p1}, Ljava/util/Map;->entrySet()Ljava/util/Set;
    move-result-object p1
    invoke-interface {p1}, Ljava/lang/Iterable;->iterator()Ljava/util/Iterator;
    move-result-object v8

    :loop
    invoke-interface {v8}, Ljava/util/Iterator;->hasNext()Z
    move-result p1
    if-eqz p1, :done

    invoke-interface {v8}, Ljava/util/Iterator;->next()Ljava/lang/Object;
    move-result-object p1
    check-cast p1, Ljava/util/Map$Entry;
    invoke-interface {p1}, Ljava/util/Map$Entry;->getKey()Ljava/lang/Object;
    move-result-object v2
    move-object v7, v2
    invoke-interface {p1}, Ljava/util/Map$Entry;->getValue()Ljava/lang/Object;
    move-result-object p1
    check-cast p1, Lconditions/TestCoroutineCompletionTail$Dependency;
    invoke-virtual {p1}, Lconditions/TestCoroutineCompletionTail$Dependency;->getMutex()Lkotlinx/coroutines/sync/Mutex;
    move-result-object v6

    iput-object v5, v0, Lconditions/TestCoroutineCompletionTail$getAll$1;->L$0:Ljava/lang/Object;
    iput-object v8, v0, Lconditions/TestCoroutineCompletionTail$getAll$1;->L$1:Ljava/lang/Object;
    iput-object v7, v0, Lconditions/TestCoroutineCompletionTail$getAll$1;->L$2:Ljava/lang/Object;
    iput-object v6, v0, Lconditions/TestCoroutineCompletionTail$getAll$1;->L$3:Ljava/lang/Object;
    iput-object v5, v0, Lconditions/TestCoroutineCompletionTail$getAll$1;->L$4:Ljava/lang/Object;
    iput-object v2, v0, Lconditions/TestCoroutineCompletionTail$getAll$1;->L$5:Ljava/lang/Object;
    iput v3, v0, Lconditions/TestCoroutineCompletionTail$getAll$1;->label:I

    invoke-interface {v6, v4, v0}, Lkotlinx/coroutines/sync/Mutex;->lock(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-ne p1, v1, :direct_completion
    return-object v1

    :direct_completion
    move-object v9, v5

    :completion
    :try_start
    sget-object p1, Lconditions/TestCoroutineCompletionTail;->INSTANCE:Lconditions/TestCoroutineCompletionTail;
    invoke-virtual {p1, v7}, Lconditions/TestCoroutineCompletionTail;->getSubscriber(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object p1
    :try_end
    .catchall {:try_start .. :try_end} :catch

    invoke-interface {v6, v4}, Lkotlinx/coroutines/sync/Mutex;->unlock(Ljava/lang/Object;)V
    invoke-interface {v5, v2, p1}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    move-object v5, v9
    goto :loop

    :catch
    move-exception p1
    invoke-interface {v6, v4}, Lkotlinx/coroutines/sync/Mutex;->unlock(Ljava/lang/Object;)V
    throw p1

    :done
    return-object v5
.end method

.method public final getSubscriber(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 2

    return-object p1
.end method
