.class public Ltypes/TestMixedBooleanBitOp;
.super Ljava/lang/Object;

.field public static bits:I
.field public value:I

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
