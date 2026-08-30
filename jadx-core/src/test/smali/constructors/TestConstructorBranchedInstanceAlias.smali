.class public Lconstructors/TestConstructorBranchedInstanceAlias;
.super Ljava/lang/Object;

.method private static touch()V
    .locals 0

    return-void
.end method

.method public static make(Z[C)Ljava/lang/String;
    .locals 3

    new-instance v0, Ljava/lang/String;
    if-eqz p0, :direct_alias

    move-object v2, v0
    invoke-static {}, Lconstructors/TestConstructorBranchedInstanceAlias;->touch()V
    const-string v0, "clobber"
    move-object v1, v2
    goto :construct

    :direct_alias
    move-object v1, v0

    :construct
    invoke-direct {v1, p1}, Ljava/lang/String;-><init>([C)V
    return-object v1
.end method
