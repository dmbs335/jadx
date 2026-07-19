.class public Lswitches/TestSwitchOverStringsDirectActions;
.super Ljava/lang/Object;

.field public static final GZIP:Ljava/lang/String; = "gzip"

.method public static test(Ljava/lang/String;)I
    .registers 3

    invoke-virtual {p0}, Ljava/lang/String;->hashCode()I
    move-result v0

    sparse-switch v0, :hash_switch
    goto :default_case

    :snappy_hash
    const-string v0, "snappy"
    invoke-virtual {p0, v0}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
    move-result v0
    if-eqz v0, :default_case
    new-instance p0, Ljava/lang/IllegalArgumentException;
    invoke-direct {p0}, Ljava/lang/IllegalArgumentException;-><init>()V
    throw p0

    :identity_hash
    const-string v0, "identity"
    invoke-virtual {p0, v0}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
    move-result v0
    if-eqz v0, :default_case
    const/4 v0, 0x1
    return v0

    :gzip_hash
    sget-object v0, Lswitches/TestSwitchOverStringsDirectActions;->GZIP:Ljava/lang/String;
    invoke-virtual {p0, v0}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
    move-result v0
    if-eqz v0, :default_case
    const/4 v0, 0x2
    return v0

    :deflate_hash
    const-string v0, "deflate"
    invoke-virtual {p0, v0}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
    move-result v0
    if-eqz v0, :default_case
    new-instance p0, Ljava/lang/UnsupportedOperationException;
    invoke-direct {p0}, Ljava/lang/UnsupportedOperationException;-><init>()V
    throw p0

    :default_case
    new-instance p0, Ljava/lang/IllegalStateException;
    invoke-direct {p0}, Ljava/lang/IllegalStateException;-><init>()V
    throw p0

    :hash_switch
    .sparse-switch
        -0x3586ccad -> :snappy_hash
        -0x08178f42 -> :identity_hash
        0x0030a95a -> :gzip_hash
        0x5c188c2b -> :deflate_hash
    .end sparse-switch
.end method
