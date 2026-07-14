.class public Lloops/TestSharedLoopBranchTarget;
.super Ljava/lang/Object;

.method public static read([BIILjava/util/List;)I
    .locals 2

    const-string v1, ""
    aget-byte v0, p0, p1
    add-int/lit8 p1, p1, 0x1
    if-eqz v0, :add_empty
    goto :loop

    :loop
    if-ge p1, p2, :exit
    aget-byte v0, p0, p1
    add-int/lit8 p1, p1, 0x1
    if-eqz v0, :add_empty
    goto :loop

    :add_empty
    invoke-interface {p3, v1}, Ljava/util/List;->add(Ljava/lang/Object;)Z
    goto :loop

    :exit
    return p1
.end method
