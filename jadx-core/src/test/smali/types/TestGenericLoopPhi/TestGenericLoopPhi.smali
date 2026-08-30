.class public Ltypes/TestGenericLoopPhi;
.super Ljava/lang/Object;

.method public static maxByOrNull(Ltypes/TestGenericLoopPhi$Sequence;Ljava/util/function/Function;)Ljava/lang/Object;
    .registers 7
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "<T:",
            "Ljava/lang/Object;R::",
            "Ljava/lang/Comparable<",
            "-TR;>;>(",
            "Ltypes/TestGenericLoopPhi$Sequence<",
            "+TT;>;",
            "Ljava/util/function/Function<",
            "-TT;+TR;>;)TT;"
        }
    .end annotation

    invoke-interface {p0}, Ltypes/TestGenericLoopPhi$Sequence;->iterator()Ljava/util/Iterator;
    move-result-object p0
    invoke-interface {p0}, Ljava/util/Iterator;->hasNext()Z
    move-result v0
    if-nez v0, :has_first
    const/4 p0, 0x0
    return-object p0

    :has_first
    invoke-interface {p0}, Ljava/util/Iterator;->next()Ljava/lang/Object;
    move-result-object v0
    invoke-interface {p0}, Ljava/util/Iterator;->hasNext()Z
    move-result v1
    if-nez v1, :init_selector
    return-object v0

    :init_selector
    invoke-interface {p1, v0}, Ljava/util/function/Function;->apply(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v1
    check-cast v1, Ljava/lang/Comparable;

    :loop
    invoke-interface {p0}, Ljava/util/Iterator;->next()Ljava/lang/Object;
    move-result-object v2
    invoke-interface {p1, v2}, Ljava/util/function/Function;->apply(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v3
    check-cast v3, Ljava/lang/Comparable;
    invoke-interface {v1, v3}, Ljava/lang/Comparable;->compareTo(Ljava/lang/Object;)I
    move-result v4
    if-gez v4, :loop_tail
    move-object v0, v2
    move-object v1, v3

    :loop_tail
    invoke-interface {p0}, Ljava/util/Iterator;->hasNext()Z
    move-result v2
    if-nez v2, :loop
    return-object v0
.end method
