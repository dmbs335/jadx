.class public Lswitches/TestSwitchOverStringsSharedJoin;
.super Ljava/lang/Object;

.method private static consume(Ljava/lang/Object;)V
    .registers 1
    return-void
.end method

.method public static test(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
    .registers 4

    :try_start
    invoke-virtual {p0}, Ljava/lang/String;->hashCode()I
    move-result v0

    sparse-switch v0, :hash_switch
    goto :default_case

    :case_a_hash
    const-string v0, "a"
    invoke-virtual {p0, v0}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
    move-result v0
    if-eqz v0, :default_case
    const-string v1, "A"
    goto :join

    :case_b_hash
    const-string v0, "b"
    invoke-virtual {p0, v0}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
    move-result v0
    if-eqz v0, :default_case
    const-string v1, "B"
    goto :join

    :default_case
    move-object v1, p1
    if-nez v1, :join
    new-instance v1, Ljava/lang/IllegalArgumentException;
    invoke-direct {v1}, Ljava/lang/IllegalArgumentException;-><init>()V
    throw v1

    :join
    invoke-static {v1}, Lswitches/TestSwitchOverStringsSharedJoin;->consume(Ljava/lang/Object;)V
    :try_end
    .catch Ljava/lang/RuntimeException; {:try_start .. :try_end} :catch
    return-object v1

    :catch
    return-object p1

    :hash_switch
    .sparse-switch
        0x61 -> :case_a_hash
        0x62 -> :case_b_hash
    .end sparse-switch
.end method
