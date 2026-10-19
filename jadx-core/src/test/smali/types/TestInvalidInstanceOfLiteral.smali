.class public Ltypes/TestInvalidInstanceOfLiteral;
.super Ljava/lang/Object;

.method public static invalid()Z
    .locals 2

    const/4 v0, 0x1
    instance-of v1, v0, Ljava/lang/String;
    return v1
.end method

.method public static boxed()Z
    .locals 2

    const/4 v0, 0x1
    invoke-static {v0}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
    move-result-object v0
    instance-of v1, v0, Ljava/lang/Number;
    return v1
.end method

.method public static boxedZero()Z
    .locals 2

    const/4 v0, 0x0
    invoke-static {v0}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
    move-result-object v0
    instance-of v1, v0, Ljava/lang/Number;
    return v1
.end method

.method public static nullValue()Z
    .locals 2

    const/4 v0, 0x0
    instance-of v1, v0, Ljava/lang/String;
    return v1
.end method
