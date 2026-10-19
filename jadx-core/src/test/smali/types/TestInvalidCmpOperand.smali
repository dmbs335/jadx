.class public Ltypes/TestInvalidCmpOperand;
.super Ljava/lang/Object;

.field public static invalidValue:Ljava/lang/String;

.method public static badLong(Ljava/lang/Object;J)I
    .locals 1

    cmp-long v0, p0, p1
    return v0
.end method

.method public static badDouble(Ljava/lang/Object;D)I
    .locals 1

    cmpg-double v0, p0, p1
    return v0
.end method

.method public static badString(Ljava/lang/String;J)I
    .locals 1

    cmp-long v0, p0, p1
    return v0
.end method

.method public static validLong(JJ)I
    .locals 1

    cmp-long v0, p0, p2
    return v0
.end method

.method public static getString()Ljava/lang/String;
    .locals 1

    const-string v0, "not a number"
    return-object v0
.end method

.method public static badInvoke(J)I
    .locals 2

    invoke-static {}, Ltypes/TestInvalidCmpOperand;->getString()Ljava/lang/String;
    move-result-object v0
    cmp-long v1, v0, p0
    return v1
.end method

.method public static badField(J)I
    .locals 2

    sget-object v0, Ltypes/TestInvalidCmpOperand;->invalidValue:Ljava/lang/String;
    cmp-long v1, v0, p0
    return v1
.end method

.method public static badMoveAlias(J)I
    .locals 3

    invoke-static {}, Ltypes/TestInvalidCmpOperand;->getString()Ljava/lang/String;
    move-result-object v0
    move-object v1, v0
    cmp-long v2, v1, p0
    return v2
.end method

.method public static badPhiAlias(JZ)I
    .locals 2

    if-eqz p2, :field
    invoke-static {}, Ltypes/TestInvalidCmpOperand;->getString()Ljava/lang/String;
    move-result-object v0
    goto :merge

    :field
    sget-object v0, Ltypes/TestInvalidCmpOperand;->invalidValue:Ljava/lang/String;

    :merge
    cmp-long v1, v0, p0
    return v1
.end method
