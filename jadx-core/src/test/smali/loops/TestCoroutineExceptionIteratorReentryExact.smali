.class public final Lloops/TestCoroutineExceptionIteratorReentryExact;
.super Ljava/lang/Object;

.method public constructor <init>()V
    .registers 1
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    return-void
.end method

.method private final runMigrations(Ljava/util/List;Landroidx/datastore/core/InitializerApi;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 10
    instance-of v0, p3, Landroidx/datastore/core/DataMigrationInitializer$Companion$runMigrations$1;
    if-eqz v0, :cond_13
    move-object v0, p3
    check-cast v0, Landroidx/datastore/core/DataMigrationInitializer$Companion$runMigrations$1;
    iget v1, v0, Landroidx/datastore/core/DataMigrationInitializer$Companion$runMigrations$1;->label:I
    const/high16 v2, -0x80000000
    and-int v3, v1, v2
    if-eqz v3, :cond_13
    sub-int/2addr v1, v2
    iput v1, v0, Landroidx/datastore/core/DataMigrationInitializer$Companion$runMigrations$1;->label:I
    goto :goto_18
    :cond_13
    new-instance v0, Landroidx/datastore/core/DataMigrationInitializer$Companion$runMigrations$1;
    invoke-direct {v0, p0, p3}, Landroidx/datastore/core/DataMigrationInitializer$Companion$runMigrations$1;-><init>(Lloops/TestCoroutineExceptionIteratorReentryExact;Lkotlin/coroutines/Continuation;)V
    :goto_18
    iget-object p3, v0, Landroidx/datastore/core/DataMigrationInitializer$Companion$runMigrations$1;->result:Ljava/lang/Object;
    invoke-static {}, Lkotlin/coroutines/intrinsics/a;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v1
    iget v2, v0, Landroidx/datastore/core/DataMigrationInitializer$Companion$runMigrations$1;->label:I
    const/4 v3, 0x2
    const/4 v4, 0x1
    if-eqz v2, :cond_46
    if-eq v2, v4, :cond_3e
    if-ne v2, v3, :cond_36
    iget-object p1, v0, Landroidx/datastore/core/DataMigrationInitializer$Companion$runMigrations$1;->L$1:Ljava/lang/Object;
    check-cast p1, Ljava/util/Iterator;
    iget-object p2, v0, Landroidx/datastore/core/DataMigrationInitializer$Companion$runMigrations$1;->L$0:Ljava/lang/Object;
    check-cast p2, Lkotlin/jvm/internal/Ref$ObjectRef;
    :try_start_30
    invoke-static {p3}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    :try_end_33
    .catchall {:try_start_30 .. :try_end_33} :catchall_34
    goto :goto_67
    :catchall_34
    move-exception p3
    goto :goto_80
    :cond_36
    new-instance p1, Ljava/lang/IllegalStateException;
    const-string p2, "call to \'resume\' before \'invoke\' with coroutine"
    invoke-direct {p1, p2}, Ljava/lang/IllegalStateException;-><init>(Ljava/lang/String;)V
    throw p1
    :cond_3e
    iget-object p1, v0, Landroidx/datastore/core/DataMigrationInitializer$Companion$runMigrations$1;->L$0:Ljava/lang/Object;
    check-cast p1, Ljava/util/List;
    invoke-static {p3}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :goto_5c
    :cond_46
    invoke-static {p3}, Lfixtures/playground/cap/vehicle/experimental/property/a;->a(Ljava/lang/Object;)Ljava/util/ArrayList;
    move-result-object p3
    new-instance v2, Landroidx/datastore/core/DataMigrationInitializer$Companion$runMigrations$2;
    const/4 v5, 0x0
    invoke-direct {v2, p1, p3, v5}, Landroidx/datastore/core/DataMigrationInitializer$Companion$runMigrations$2;-><init>(Ljava/util/List;Ljava/util/List;Lkotlin/coroutines/Continuation;)V
    iput-object p3, v0, Landroidx/datastore/core/DataMigrationInitializer$Companion$runMigrations$1;->L$0:Ljava/lang/Object;
    iput v4, v0, Landroidx/datastore/core/DataMigrationInitializer$Companion$runMigrations$1;->label:I
    invoke-interface {p2, v2, v0}, Landroidx/datastore/core/InitializerApi;->updateData(Lkotlin/jvm/functions/Function2;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object p1
    if-ne p1, v1, :cond_5b
    goto :goto_7f
    :cond_5b
    move-object p1, p3
    :goto_5c
    new-instance p2, Lkotlin/jvm/internal/Ref$ObjectRef;
    invoke-direct {p2}, Lkotlin/jvm/internal/Ref$ObjectRef;-><init>()V
    check-cast p1, Ljava/lang/Iterable;
    invoke-interface {p1}, Ljava/lang/Iterable;->iterator()Ljava/util/Iterator;
    move-result-object p1
    :cond_67
    :goto_67
    invoke-interface {p1}, Ljava/util/Iterator;->hasNext()Z
    move-result p3
    if-eqz p3, :cond_90
    invoke-interface {p1}, Ljava/util/Iterator;->next()Ljava/lang/Object;
    move-result-object p3
    check-cast p3, Lkotlin/jvm/functions/Function1;
    :try_start_73
    iput-object p2, v0, Landroidx/datastore/core/DataMigrationInitializer$Companion$runMigrations$1;->L$0:Ljava/lang/Object;
    iput-object p1, v0, Landroidx/datastore/core/DataMigrationInitializer$Companion$runMigrations$1;->L$1:Ljava/lang/Object;
    iput v3, v0, Landroidx/datastore/core/DataMigrationInitializer$Companion$runMigrations$1;->label:I
    invoke-interface {p3, v0}, Lkotlin/jvm/functions/Function1;->invoke(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object p3
    :try_end_7d
    .catchall {:try_start_73 .. :try_end_7d} :catchall_34
    if-ne p3, v1, :cond_67
    :goto_7f
    return-object v1
    :goto_80
    iget-object v2, p2, Lkotlin/jvm/internal/Ref$ObjectRef;->element:Ljava/lang/Object;
    if-nez v2, :cond_87
    iput-object p3, p2, Lkotlin/jvm/internal/Ref$ObjectRef;->element:Ljava/lang/Object;
    goto :goto_67
    :cond_87
    invoke-static {v2}, Lkotlin/jvm/internal/Intrinsics;->checkNotNull(Ljava/lang/Object;)V
    check-cast v2, Ljava/lang/Throwable;
    invoke-static {v2, p3}, Lkotlin/g;->addSuppressed(Ljava/lang/Throwable;Ljava/lang/Throwable;)V
    goto :goto_67
    :cond_90
    iget-object p1, p2, Lkotlin/jvm/internal/Ref$ObjectRef;->element:Ljava/lang/Object;
    check-cast p1, Ljava/lang/Throwable;
    if-nez p1, :cond_99
    sget-object p1, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object p1
    :cond_99
    throw p1
.end method
