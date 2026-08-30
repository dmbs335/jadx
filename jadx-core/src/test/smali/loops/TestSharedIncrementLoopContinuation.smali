.class public Lloops/TestSharedIncrementLoopContinuation;
.super Ljava/lang/Object;

.method public static restore(Ljava/util/List;ILjava/util/List;)V
    .locals 4

    invoke-interface {p0}, Ljava/util/List;->size()I
    move-result v0
    const/4 v1, 0x0

    :loop
    if-ge v1, v0, :done

    invoke-interface {p0, v1}, Ljava/util/List;->get(I)Ljava/lang/Object;
    move-result-object v2
    instance-of v3, v2, Ljava/lang/Integer;
    if-eqz v3, :advance

    check-cast v2, Ljava/lang/Integer;
    invoke-virtual {v2}, Ljava/lang/Integer;->intValue()I
    move-result v3
    if-ne v3, p1, :advance

    invoke-interface {p2, v2}, Ljava/util/List;->add(Ljava/lang/Object;)Z
    goto :done

    :advance
    add-int/lit8 v1, v1, 0x1
    goto :loop

    :done
    invoke-static {v1}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
    move-result-object v0
    invoke-interface {p2, v0}, Ljava/util/List;->add(Ljava/lang/Object;)Z
    return-void
.end method
