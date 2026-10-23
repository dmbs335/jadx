.class public Ltypes/TestInvalidMoveResultPlacement;
.super Ljava/lang/Object;

.method private static value()I
    .locals 1
    const/4 v0, 0x1
    return v0
.end method

.method public static orphan()I
    .locals 1
    move-result v0
    return v0
.end method

.method public static interrupted()I
    .locals 1
    invoke-static {}, Ltypes/TestInvalidMoveResultPlacement;->value()I
    nop
    move-result v0
    return v0
.end method

.method public static valid()I
    .locals 1
    invoke-static {}, Ltypes/TestInvalidMoveResultPlacement;->value()I
    move-result v0
    return v0
.end method

.method public static interruptedFilledArray()[I
    .locals 1
    filled-new-array {}, [I
    nop
    move-result-object v0
    return-object v0
.end method

.method public static validFilledArray()[I
    .locals 1
    filled-new-array {}, [I
    move-result-object v0
    return-object v0
.end method

.method public static branchToResult()I
    .locals 1
    goto :result
    invoke-static {}, Ltypes/TestInvalidMoveResultPlacement;->value()I
    :result
    move-result v0
    return v0
.end method

.method public static conditionalBranchToResult(Z)I
    .locals 1
    if-eqz p0, :result
    invoke-static {}, Ltypes/TestInvalidMoveResultPlacement;->value()I
    :result
    move-result v0
    return v0
.end method
