.class public Ltypes/TestInvalidMoveOpcode;
.super Ljava/lang/Object;

.method public static intViaObject(I)I
    .locals 1
    move-object v0, p0
    return v0
.end method

.method public static objectViaNarrow(Ljava/lang/Object;)Ljava/lang/Object;
    .locals 1
    move v0, p0
    return-object v0
.end method

.method public static intViaWide(I)I
    .locals 2
    move-wide v0, p0
    return v0
.end method

.method private static makeInt()I
    .locals 1
    const/4 v0, 0x1
    return v0
.end method

.method public static objectViaIntInvoke()Ljava/lang/Object;
    .locals 2
    invoke-static {}, Ltypes/TestInvalidMoveOpcode;->makeInt()I
    move-result v0
    move-object v1, v0
    return-object v1
.end method

.method public static unusedInvalidMove(I)V
    .locals 1
    move-object v0, p0
    return-void
.end method

.method public static validInt(I)I
    .locals 1
    move v0, p0
    return v0
.end method

.method public static validObject(Ljava/lang/Object;)Ljava/lang/Object;
    .locals 1
    move-object v0, p0
    return-object v0
.end method

.method public static validLong(J)J
    .locals 2
    move-wide v0, p0
    return-wide v0
.end method

.method public static validFloat(F)F
    .locals 1
    move v0, p0
    return v0
.end method

.method public static validDouble(D)D
    .locals 2
    move-wide v0, p0
    return-wide v0
.end method

.method public static validNull()Ljava/lang/Object;
    .locals 2
    const/4 v0, 0x0
    move-object v1, v0
    return-object v1
.end method
