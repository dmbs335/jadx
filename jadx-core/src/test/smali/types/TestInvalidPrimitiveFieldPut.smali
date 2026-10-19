.class public Ltypes/TestInvalidPrimitiveFieldPut;
.super Ljava/lang/Object;

.field public static floatValue:F
.field public static intValue:I
.field public doubleValue:D

.method public static intToStaticFloat(I)V
    .locals 0

    sput p0, Ltypes/TestInvalidPrimitiveFieldPut;->floatValue:F
    return-void
.end method

.method public static longToInstanceDouble(Ltypes/TestInvalidPrimitiveFieldPut;J)V
    .locals 0

    iput-wide p1, p0, Ltypes/TestInvalidPrimitiveFieldPut;->doubleValue:D
    return-void
.end method

.method public static floatToStaticInt(F)V
    .locals 0

    sput p0, Ltypes/TestInvalidPrimitiveFieldPut;->intValue:I
    return-void
.end method

.method public static validFloatToStaticFloat(F)V
    .locals 0

    sput p0, Ltypes/TestInvalidPrimitiveFieldPut;->floatValue:F
    return-void
.end method

.method public static validDoubleToInstanceDouble(Ltypes/TestInvalidPrimitiveFieldPut;D)V
    .locals 0

    iput-wide p1, p0, Ltypes/TestInvalidPrimitiveFieldPut;->doubleValue:D
    return-void
.end method

.method public static validConvertedIntToStaticFloat(I)V
    .locals 1

    int-to-float v0, p0
    sput v0, Ltypes/TestInvalidPrimitiveFieldPut;->floatValue:F
    return-void
.end method
