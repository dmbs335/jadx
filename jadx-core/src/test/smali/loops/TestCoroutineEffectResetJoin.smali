.class public Lloops/TestCoroutineEffectResetJoin;
.super Ljava/lang/Object;

.method public static run(Ljava/util/Iterator;Ljava/io/Closeable;Lkotlin/coroutines/Continuation;I)Ljava/lang/Object;
    .locals 3

    if-nez p3, :reset
    goto :loop_header

    :reset
    move-object v2, p0
    const/4 v0, 0x0

    :loop_header
    invoke-interface {p0}, Ljava/util/Iterator;->hasNext()Z
    move-result v0
    if-eqz v0, :done

    invoke-interface {p0}, Ljava/util/Iterator;->next()Ljava/lang/Object;
    move-result-object v1
    invoke-interface {p1}, Ljava/io/Closeable;->close()V
    goto :reset

    :done
    sget-object v1, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object v1
.end method
