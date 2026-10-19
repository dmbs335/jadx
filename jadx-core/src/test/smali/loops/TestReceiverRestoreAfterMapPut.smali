.class public Lloops/TestReceiverRestoreAfterMapPut;
.super Ljava/lang/Object;

.method private step()I
    .registers 2
    const/4 v0, 0x1
    return v0
.end method

.method public run(Ljava/util/Map;[Ljava/lang/String;)I
    .registers 8

    move-object v0, p0
    const/4 v1, 0x0

    :loop
    array-length v2, p2
    if-ge v1, v2, :done
    const/4 v4, 0x0

    :inner_loop
    array-length v2, p2
    if-ge v4, v2, :next_outer
    invoke-direct {v0}, Lloops/TestReceiverRestoreAfterMapPut;->step()I
    move-result v2
    aget-object v3, p2, v4
    invoke-interface {p1, v3, v3}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v0
    if-eqz v0, :skip_hash
    invoke-virtual {v0}, Ljava/lang/Object;->hashCode()I
    move-result v2

    :skip_hash
    add-int/lit8 v4, v4, 0x1
    move-object v0, p0
    goto :inner_loop

    :next_outer
    add-int/lit8 v1, v1, 0x1
    move-object v0, p0
    goto :loop

    :done
    return v1
.end method
