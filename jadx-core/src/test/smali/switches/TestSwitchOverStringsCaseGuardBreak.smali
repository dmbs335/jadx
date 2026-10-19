.class public Lswitches/TestSwitchOverStringsCaseGuardBreak;
.super Ljava/lang/Object;

.method public static test(Ljava/lang/String;I)I
    .registers 5

    invoke-virtual {p0}, Ljava/lang/String;->hashCode()I
    move-result v0
    sparse-switch v0, :hash_switch
    goto :unsupported

    :concat_hash
    const-string v1, "concat"
    invoke-virtual {p0, v1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
    move-result v1
    if-eqz v1, :unsupported
    const/4 v0, 0x0
    :concat_loop
    if-ge v0, p1, :concat_done
    add-int/lit8 v0, v0, 0x1
    goto :concat_loop
    :concat_done
    return v0

    :filter_hash
    const-string v1, "filter"
    invoke-virtual {p0, v1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
    move-result v1
    if-eqz v1, :unsupported
    mul-int/lit8 v0, p1, 0x2
    return v0

    :unsupported
    new-instance v0, Ljava/lang/IllegalArgumentException;
    invoke-direct {v0}, Ljava/lang/IllegalArgumentException;-><init>()V
    throw v0

    :hash_switch
    .sparse-switch
        -0x50c088ec -> :concat_hash
        -0x4bf73488 -> :filter_hash
    .end sparse-switch
.end method
