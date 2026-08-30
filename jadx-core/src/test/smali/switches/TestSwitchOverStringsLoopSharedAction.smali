.class public Lswitches/TestSwitchOverStringsLoopSharedAction;
.super Ljava/lang/Object;

.method public static test(Ljava/lang/String;I)I
    .registers 5

    const/4 v0, 0x0
    const/4 v1, 0x0

    :loop
    if-ge v0, p1, :done
    invoke-virtual {p0}, Ljava/lang/String;->hashCode()I
    move-result v2
    sparse-switch v2, :hash_switch
    goto :next

    :constraint_hash
    const-string v2, "constraint"
    invoke-virtual {p0, v2}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
    move-result v2
    if-nez v2, :shared_action
    goto :next

    :guideline_hash
    const-string v2, "guideline"
    invoke-virtual {p0, v2}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
    move-result v2
    if-nez v2, :shared_action
    goto :next

    :stop_hash
    const-string v2, "stop"
    invoke-virtual {p0, v2}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
    move-result v2
    if-nez v2, :done
    goto :next

    :shared_action
    add-int/lit8 v1, v1, 0x1

    :next
    add-int/lit8 v0, v0, 0x1
    goto :loop

    :done
    return v1

    :hash_switch
    .sparse-switch
        -0x0b58ea23 -> :constraint_hash
        -0x7bb8f310 -> :guideline_hash
        0x00360802 -> :stop_hash
    .end sparse-switch
.end method
