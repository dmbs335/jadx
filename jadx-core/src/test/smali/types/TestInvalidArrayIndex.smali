.class public Ltypes/TestInvalidArrayIndex;
.super Ljava/lang/Object;

.method public static newArrayObject(Ljava/lang/Object;)[I
    .locals 1

    new-array v0, p0, [I
    return-object v0
.end method

.method public static newArrayLong(J)[I
    .locals 1

    new-array v0, p0, [I
    return-object v0
.end method

.method public static getObject([ILjava/lang/Object;)I
    .locals 1

    aget v0, p0, p1
    return v0
.end method

.method public static putObject([ILjava/lang/Object;)V
    .locals 1

    const/4 v0, 0x1
    aput v0, p0, p1
    return-void
.end method

.method public static getBoolean([IZ)I
    .locals 1

    aget v0, p0, p1
    return v0
.end method

.method public static validByte([IB)I
    .locals 1

    aget v0, p0, p1
    return v0
.end method

.method public static valid([II)I
    .locals 1

    aget v0, p0, p1
    return v0
.end method

.method public static getString()Ljava/lang/String;
    .locals 1

    const-string v0, "index"
    return-object v0
.end method

.method public static getIndexInvoke([I)I
    .locals 2

    invoke-static {}, Ltypes/TestInvalidArrayIndex;->getString()Ljava/lang/String;
    move-result-object v0
    aget v1, p0, v0
    return v1
.end method

.method public static putIndexInvoke([I)V
    .locals 2

    invoke-static {}, Ltypes/TestInvalidArrayIndex;->getString()Ljava/lang/String;
    move-result-object v0
    const/4 v1, 0x1
    aput v1, p0, v0
    return-void
.end method

.method public static newArrayIndexInvoke()[I
    .locals 2

    invoke-static {}, Ltypes/TestInvalidArrayIndex;->getString()Ljava/lang/String;
    move-result-object v0
    new-array v1, v0, [I
    return-object v1
.end method
