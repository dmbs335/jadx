.class public Ltypes/TestInvalidPrimitiveInvokeArgument;
.super Ljava/lang/Object;

.method public constructor <init>(F)V
    .locals 0
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    return-void
.end method

.method public static consumeFloat(F)V
    .locals 0
    return-void
.end method

.method public static consumeDouble(D)V
    .locals 0
    return-void
.end method

.method public consumeFloatVirtual(F)V
    .locals 0
    return-void
.end method

.method public static intToFloat(I)V
    .locals 0
    invoke-static {p0}, Ltypes/TestInvalidPrimitiveInvokeArgument;->consumeFloat(F)V
    return-void
.end method

.method public static longToDouble(J)V
    .locals 0
    invoke-static {p0, p1}, Ltypes/TestInvalidPrimitiveInvokeArgument;->consumeDouble(D)V
    return-void
.end method

.method public static intToFloatVirtual(Ltypes/TestInvalidPrimitiveInvokeArgument;I)V
    .locals 0
    invoke-virtual {p0, p1}, Ltypes/TestInvalidPrimitiveInvokeArgument;->consumeFloatVirtual(F)V
    return-void
.end method

.method public static intToFloatConstructor(I)Ltypes/TestInvalidPrimitiveInvokeArgument;
    .locals 1
    new-instance v0, Ltypes/TestInvalidPrimitiveInvokeArgument;
    invoke-direct {v0, p0}, Ltypes/TestInvalidPrimitiveInvokeArgument;-><init>(F)V
    return-object v0
.end method

.method public static validFloat(F)V
    .locals 0
    invoke-static {p0}, Ltypes/TestInvalidPrimitiveInvokeArgument;->consumeFloat(F)V
    return-void
.end method

.method public static validConvertedInt(I)V
    .locals 1
    int-to-float v0, p0
    invoke-static {v0}, Ltypes/TestInvalidPrimitiveInvokeArgument;->consumeFloat(F)V
    return-void
.end method

.method public static validConvertedIntVirtual(Ltypes/TestInvalidPrimitiveInvokeArgument;I)V
    .locals 1
    int-to-float v0, p1
    invoke-virtual {p0, v0}, Ltypes/TestInvalidPrimitiveInvokeArgument;->consumeFloatVirtual(F)V
    return-void
.end method

.method public static validConvertedIntConstructor(I)Ltypes/TestInvalidPrimitiveInvokeArgument;
    .locals 2
    new-instance v0, Ltypes/TestInvalidPrimitiveInvokeArgument;
    int-to-float v1, p0
    invoke-direct {v0, v1}, Ltypes/TestInvalidPrimitiveInvokeArgument;-><init>(F)V
    return-object v0
.end method
