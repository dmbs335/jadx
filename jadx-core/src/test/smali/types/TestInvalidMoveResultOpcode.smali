.class public Ltypes/TestInvalidMoveResultOpcode;
.super Ljava/lang/Object;

.method private static getInt()I
    .locals 1
    const/4 v0, 0x1
    return v0
.end method

.method private static getObject()Ljava/lang/Object;
    .locals 1
    const/4 v0, 0x0
    return-object v0
.end method

.method private static getLong()J
    .locals 2
    const-wide/16 v0, 0x1
    return-wide v0
.end method

.method private static doNothing()V
    .locals 0
    return-void
.end method

.method public static objectFromInt()I
    .locals 1
    invoke-static {}, Ltypes/TestInvalidMoveResultOpcode;->getInt()I
    move-result-object v0
    return v0
.end method

.method public static narrowFromObject()Ljava/lang/Object;
    .locals 1
    invoke-static {}, Ltypes/TestInvalidMoveResultOpcode;->getObject()Ljava/lang/Object;
    move-result v0
    return-object v0
.end method

.method public static wideFromInt()J
    .locals 2
    invoke-static {}, Ltypes/TestInvalidMoveResultOpcode;->getInt()I
    move-result-wide v0
    return-wide v0
.end method

.method public static narrowFromFilledArray(I)[I
    .locals 1
    filled-new-array {p0}, [I
    move-result v0
    return-object v0
.end method

.method public static resultFromVoid()I
    .locals 1
    invoke-static {}, Ltypes/TestInvalidMoveResultOpcode;->doNothing()V
    move-result v0
    return v0
.end method

.method public static unusedObjectResult()V
    .locals 1
    invoke-static {}, Ltypes/TestInvalidMoveResultOpcode;->getInt()I
    move-result-object v0
    return-void
.end method

.method public static validInt()I
    .locals 1
    invoke-static {}, Ltypes/TestInvalidMoveResultOpcode;->getInt()I
    move-result v0
    return v0
.end method

.method public static validObject()Ljava/lang/Object;
    .locals 1
    invoke-static {}, Ltypes/TestInvalidMoveResultOpcode;->getObject()Ljava/lang/Object;
    move-result-object v0
    return-object v0
.end method

.method public static validLong()J
    .locals 2
    invoke-static {}, Ltypes/TestInvalidMoveResultOpcode;->getLong()J
    move-result-wide v0
    return-wide v0
.end method
