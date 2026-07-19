.class public Lswitches/TestSwitchOverStringsExternalFallthrough;
.super Ljava/lang/Object;

.method public static test(Ljava/lang/String;I)I
    .registers 4

    if-eqz p0, :fallback

    invoke-virtual {p0}, Ljava/lang/String;->hashCode()I
    move-result v0

    sparse-switch v0, :hash_switch
    goto :fallback

    :alpha_hash
    const-string v0, "alpha"
    invoke-virtual {p0, v0}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
    move-result v0
    if-eqz v0, :fallback
    const/4 v0, 0x1
    return v0

    :beta_hash
    const-string v0, "beta"
    invoke-virtual {p0, v0}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
    move-result v0
    if-eqz v0, :fallback
    const/4 v0, 0x2
    return v0

    :fallback
    add-int/lit8 p1, p1, 0x1
    if-lez p1, :negative
    return p1

    :negative
    const/4 v0, -0x1
    return v0

    :hash_switch
    .sparse-switch
        0x0589b15e -> :alpha_hash
        0x002e15f0 -> :beta_hash
    .end sparse-switch
.end method
