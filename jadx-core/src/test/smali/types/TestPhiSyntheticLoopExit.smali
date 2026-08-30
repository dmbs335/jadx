.class public Ltypes/TestPhiSyntheticLoopExit;
.super Ljava/lang/Object;

.method private static fallback()Ljava/lang/Object;
    .registers 1
    const/4 v0, 0x0
    return-object v0
.end method

.method public static test(Ljava/lang/Iterable;I)Ljava/util/List;
    .registers 6

    :try_start
    if-eqz p1, :build
    const/4 v2, 0x1
    if-ne p1, v2, :unwrap
    invoke-static {}, Ltypes/TestPhiSyntheticLoopExit;->fallback()Ljava/lang/Object;
    move-result-object v0
    check-cast v0, Ljava/util/List;
    goto :try_merge

    :unwrap
    invoke-static {}, Ltypes/TestPhiSyntheticLoopExit;->fallback()Ljava/lang/Object;
    move-result-object v2
    goto :try_copy

    :build

    new-instance v0, Ljava/util/ArrayList;
    invoke-direct {v0}, Ljava/util/ArrayList;-><init>()V
    invoke-interface {p0}, Ljava/lang/Iterable;->iterator()Ljava/util/Iterator;
    move-result-object v1

    :loop
    invoke-interface {v1}, Ljava/util/Iterator;->hasNext()Z
    move-result v2
    if-eqz v2, :try_merge
    invoke-interface {v1}, Ljava/util/Iterator;->next()Ljava/lang/Object;
    move-result-object v2
    invoke-interface {v0, v2}, Ljava/util/Collection;->add(Ljava/lang/Object;)Z
    goto :loop

    :try_copy
    move-object v0, v2

    :try_merge
    :try_end
    .catch Ljava/lang/RuntimeException; {:try_start .. :try_end} :catch
    goto :merge

    :catch
    move-exception v1
    const/4 v2, 0x0
    move-object v0, v2

    :merge
    check-cast v0, Ljava/util/List;
    return-object v0
.end method
