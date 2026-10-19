.class public Ltypes/TestMixedBooleanBitOp;
.super Ljava/lang/Object;

.field public static bits:I
.field public value:I

.method public constructor <init>()V
    .locals 0
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    return-void
.end method

.method public constructor <init>(I)V
    .locals 0
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    iput p1, p0, Ltypes/TestMixedBooleanBitOp;->value:I
    return-void
.end method

.method public static andBooleanInt(ZI)I
    .locals 1
    and-int v0, p0, p1
    return v0
.end method

.method public static orIntBoolean(IZ)I
    .locals 1
    or-int v0, p0, p1
    return v0
.end method

.method public static xorBooleanInt(ZI)I
    .locals 1
    xor-int v0, p0, p1
    return v0
.end method

.method public static andBooleans(ZZ)I
    .locals 1
    and-int v0, p0, p1
    return v0
.end method

.method public static storeOrIntBoolean(IZ)V
    .locals 1
    or-int v0, p0, p1
    sput v0, Ltypes/TestMixedBooleanBitOp;->bits:I
    return-void
.end method

.method public static storeInstanceOrIntBoolean(Ltypes/TestMixedBooleanBitOp;IZ)V
    .locals 1
    or-int v0, p1, p2
    iput v0, p0, Ltypes/TestMixedBooleanBitOp;->value:I
    return-void
.end method

.method public static arrayOrIntBoolean([IIZ)V
    .locals 1
    or-int v0, p1, p2
    aput v0, p0, p1
    return-void
.end method

.method public static moveOrIntBoolean(IZ)I
    .locals 2
    or-int v0, p0, p1
    move v1, v0
    return v1
.end method

.method public static consume(I)V
    .locals 0
    return-void
.end method

.method public static invokeOrIntBoolean(IZ)V
    .locals 1
    or-int v0, p0, p1
    invoke-static {v0}, Ltypes/TestMixedBooleanBitOp;->consume(I)V
    return-void
.end method

.method public consumeInstance(I)V
    .locals 0
    return-void
.end method

.method public static invokeInstanceOrIntBoolean(Ltypes/TestMixedBooleanBitOp;IZ)V
    .locals 1
    or-int v0, p1, p2
    invoke-virtual {p0, v0}, Ltypes/TestMixedBooleanBitOp;->consumeInstance(I)V
    return-void
.end method

.method public static consumePair(ZI)V
    .locals 0
    return-void
.end method

.method public static invokeSecondArgOrIntBoolean(IZ)V
    .locals 1
    or-int v0, p0, p1
    invoke-static {p1, v0}, Ltypes/TestMixedBooleanBitOp;->consumePair(ZI)V
    return-void
.end method

.method public static constructOrIntBoolean(IZ)Ltypes/TestMixedBooleanBitOp;
    .locals 2
    or-int v0, p0, p1
    new-instance v1, Ltypes/TestMixedBooleanBitOp;
    invoke-direct {v1, v0}, Ltypes/TestMixedBooleanBitOp;-><init>(I)V
    return-object v1
.end method

.method public static widenOrIntBoolean(IZ)J
    .locals 3

    or-int v0, p0, p1

    int-to-long v1, v0

    return-wide v1
.end method

.method public static narrowOrIntBoolean(IZ)B
    .locals 2

    or-int v0, p0, p1

    int-to-byte v1, v0

    return v1
.end method

.method public static floatOrIntBoolean(IZ)F
    .locals 2

    or-int v0, p0, p1

    int-to-float v1, v0

    return v1
.end method

.method public static storeAndWidenOrIntBoolean(IZ[I)J
    .locals 3

    or-int v0, p0, p1

    aput v0, p2, p0

    int-to-long v1, v0

    return-wide v1
.end method

.method public static branchAndWidenOrIntBoolean(IZ)J
    .locals 3

    or-int v0, p0, p1

    if-eqz v0, :zero

    int-to-long v1, v0

    return-wide v1

    :zero
    const-wide/16 v1, 0x0

    return-wide v1
.end method

.method public static compareAndWidenOrIntBoolean(IZ)J
    .locals 3

    or-int v0, p0, p1

    if-eq v0, p0, :zero

    int-to-long v1, v0

    return-wide v1

    :zero
    const-wide/16 v1, 0x0

    return-wide v1
.end method

.method public static reverseCompareAndWidenOrIntBoolean(IZ)J
    .locals 3

    or-int v0, p0, p1

    if-eq p0, v0, :zero

    int-to-long v1, v0

    return-wide v1

    :zero
    const-wide/16 v1, 0x0

    return-wide v1
.end method

.method public static booleanCompareAndWidenOrIntBoolean(IZ)J
    .locals 3

    or-int v0, p0, p1

    if-eq v0, p1, :zero

    int-to-long v1, v0

    return-wide v1

    :zero
    const-wide/16 v1, 0x0

    return-wide v1
.end method

.method public static reverseBooleanCompareAndWidenOrIntBoolean(IZ)J
    .locals 3

    or-int v0, p0, p1

    if-eq p1, v0, :zero

    int-to-long v1, v0

    return-wide v1

    :zero
    const-wide/16 v1, 0x0

    return-wide v1
.end method

.method public static relationalCompareAndWidenOrIntBoolean(IZ)J
    .locals 3

    or-int v0, p0, p1

    if-lt v0, p0, :negative

    int-to-long v1, v0

    return-wide v1

    :negative
    const-wide/16 v1, -0x1

    return-wide v1
.end method

.method public static charOrIntBoolean(IZ)C
    .locals 2

    or-int v0, p0, p1

    int-to-char v1, v0

    return v1
.end method

.method public static doubleOrIntBoolean(IZ)D
    .locals 3

    or-int v0, p0, p1

    int-to-double v1, v0

    return-wide v1
.end method

.method public check()V
    .locals 6

    const/4 v0, -0x4
    const/4 v1, 0x1

    invoke-static {v0, v1}, Ltypes/TestMixedBooleanBitOp;->orIntBoolean(IZ)I
    move-result v2
    const/4 v3, -0x3
    if-ne v2, v3, :fail

    invoke-static {v1, v0}, Ltypes/TestMixedBooleanBitOp;->xorBooleanInt(ZI)I
    move-result v2
    const/4 v3, -0x3
    if-ne v2, v3, :fail

    invoke-static {v0, v1}, Ltypes/TestMixedBooleanBitOp;->widenOrIntBoolean(IZ)J
    move-result-wide v2
    const-wide/16 v4, -0x3
    cmp-long v2, v2, v4
    if-nez v2, :fail

    invoke-static {v0, v1}, Ltypes/TestMixedBooleanBitOp;->booleanCompareAndWidenOrIntBoolean(IZ)J
    move-result-wide v2
    const-wide/16 v4, -0x3
    cmp-long v2, v2, v4
    if-nez v2, :fail

    const/16 v0, 0x100
    invoke-static {v0, v1}, Ltypes/TestMixedBooleanBitOp;->narrowOrIntBoolean(IZ)B
    move-result v2
    if-ne v2, v1, :fail

    const v0, 0x10000
    invoke-static {v0, v1}, Ltypes/TestMixedBooleanBitOp;->charOrIntBoolean(IZ)C
    move-result v2
    if-ne v2, v1, :fail

    return-void

    :fail
    new-instance v0, Ljava/lang/AssertionError;
    invoke-direct {v0}, Ljava/lang/AssertionError;-><init>()V
    throw v0
.end method
