.class public Lswitches/TestSwitchOverStringsNestedCaseLoops;
.super Ljava/lang/Object;

.method public static test(Ljava/lang/String;II)I
    .registers 9

    invoke-virtual {p0}, Ljava/lang/String;->hashCode()I
    move-result v0
    sparse-switch v0, :hash_switch
    goto :default

    :concat_hash
    const-string v1, "concat"
    invoke-virtual {p0, v1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
    move-result v1
    if-eqz v1, :default
    const/4 v0, 0x0
    const/4 v2, 0x0
    :outer
    if-ge v2, p1, :concat_done
    const/4 v3, 0x0
    :inner
    if-ge v3, p2, :inner_done
    add-int/lit8 v0, v0, 0xa
    add-int/lit8 v3, v3, 0x1
    goto :inner
    :inner_done
    add-int/lit8 v0, v0, 0x1
    add-int/lit8 v2, v2, 0x1
    goto :outer
    :concat_done
    return v0

    :filter_hash
    const-string v1, "filter"
    invoke-virtual {p0, v1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
    move-result v1
    if-eqz v1, :default
    const/4 v0, 0x0
    const/4 v2, 0x0
    :filter_loop
    if-ge v2, p1, :filter_done
    add-int/lit8 v0, v0, 0x2
    add-int/lit8 v2, v2, 0x1
    goto :filter_loop
    :filter_done
    return v0

    :default
    const/4 v0, -0x1
    return v0

    :hash_switch
    .sparse-switch
        -0x50c088ec -> :concat_hash
        -0x4bf73488 -> :filter_hash
    .end sparse-switch
.end method
