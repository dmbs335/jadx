.class public final Ltypes/TestImmutableArrayPhiWidening;
.super Ljava/lang/Object;

.method public static select(I)Ljava/lang/Object;
    .registers 2

    if-eqz p0, :byte_table

    const/4 v0, 0x1
    new-array v0, v0, [S
    goto :return_table

    :byte_table
    const/4 v0, 0x1
    new-array v0, v0, [B

    :return_table
    return-object v0
.end method
