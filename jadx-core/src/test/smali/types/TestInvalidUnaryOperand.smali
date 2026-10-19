.class public Ltypes/TestInvalidUnaryOperand;
.super Ljava/lang/Object;

.method public static negObject(Ljava/lang/Object;)I
    .locals 1

    neg-int v0, p0
    return v0
.end method

.method public static notObject(Ljava/lang/Object;)I
    .locals 1

    not-int v0, p0
    return v0
.end method

.method public static notFloat(F)I
    .locals 1

    not-int v0, p0
    return v0
.end method

.method public static validNeg(I)I
    .locals 1

    neg-int v0, p0
    return v0
.end method

.method public static validNot(I)I
    .locals 1

    not-int v0, p0
    return v0
.end method

.method public static getString()Ljava/lang/String;
    .locals 1

    const-string v0, "text"
    return-object v0
.end method

.method public static negInvoke()I
    .locals 2

    invoke-static {}, Ltypes/TestInvalidUnaryOperand;->getString()Ljava/lang/String;
    move-result-object v0
    neg-int v1, v0
    return v1
.end method

.method public static notInvoke()I
    .locals 2

    invoke-static {}, Ltypes/TestInvalidUnaryOperand;->getString()Ljava/lang/String;
    move-result-object v0
    not-int v1, v0
    return v1
.end method
