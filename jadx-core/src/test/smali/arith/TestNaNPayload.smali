.class public LTestNaNPayload;
.super Ljava/lang/Object;

.field public static final NAN:D = 0x7ff8000000000001L
.field public static final NAN_FLOAT:F = 0x7fc00001

.method public constructor <init>()V
    .locals 0

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    return-void
.end method

.method public static rawDouble()J
    .locals 2

    const-wide v0, 0x7ff8000000000001L
    invoke-static {v0, v1}, Ljava/lang/Double;->doubleToRawLongBits(D)J
    move-result-wide v0
    return-wide v0
.end method

.method public static rawField()J
    .locals 2

    sget-wide v0, LTestNaNPayload;->NAN:D
    invoke-static {v0, v1}, Ljava/lang/Double;->doubleToRawLongBits(D)J
    move-result-wide v0
    return-wide v0
.end method

.method public static rawFloatField()I
    .locals 1

    sget v0, LTestNaNPayload;->NAN_FLOAT:F
    invoke-static {v0}, Ljava/lang/Float;->floatToRawIntBits(F)I
    move-result v0
    return v0
.end method

.method public check()V
    .locals 4

    invoke-static {}, LTestNaNPayload;->rawDouble()J
    move-result-wide v0
    const-wide v2, 0x7ff8000000000001L
    cmp-long v0, v0, v2
    if-nez v0, :fail

    invoke-static {}, LTestNaNPayload;->rawField()J
    move-result-wide v0
    cmp-long v0, v0, v2
    if-nez v0, :fail

    invoke-static {}, LTestNaNPayload;->rawFloatField()I
    move-result v0
    const v1, 0x7fc00001
    if-eq v0, v1, :ok

    :fail
    new-instance v0, Ljava/lang/AssertionError;
    invoke-direct {v0}, Ljava/lang/AssertionError;-><init>()V
    throw v0

    :ok
    return-void
.end method
