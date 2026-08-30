.class public Lswitches/TestSwitchOverStringsSharedDefaultJoin;
.super Ljava/lang/Object;

.method private static consume(I)V
    .registers 1
    return-void
.end method

.method public static test(Ljava/lang/String;)I
    .registers 4

    invoke-virtual {p0}, Ljava/lang/String;->hashCode()I
    move-result v0

    sparse-switch v0, :hash_switch
    goto :default_hash

    :case_a_hash
    const-string v1, "a"
    invoke-virtual {p0, v1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
    move-result v1
    if-eqz v1, :default_hash
    const/4 v0, 0x0
    goto :hash_join

    :case_b_hash
    const-string v1, "b"
    invoke-virtual {p0, v1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
    move-result v1
    if-eqz v1, :default_hash
    const/4 v0, 0x1
    goto :hash_join

    :default_hash
    const/4 v0, -0x1

    :hash_join
    packed-switch v0, :value_switch
    const/4 v2, -0x1
    goto :tail

    :case_a
    const/16 v2, 0xa
    goto :tail

    :case_b
    const/16 v2, 0x14

    :tail
    invoke-static {v2}, Lswitches/TestSwitchOverStringsSharedDefaultJoin;->consume(I)V
    return v2

    :hash_switch
    .sparse-switch
        0x61 -> :case_a_hash
        0x62 -> :case_b_hash
    .end sparse-switch

    :value_switch
    .packed-switch 0x0
        :case_a
        :case_b
    .end packed-switch
.end method
