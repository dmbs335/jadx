.class public Ltypes/TestInvalidFieldOpcode;
.super Ljava/lang/Object;

.field public static number:I
.field public instanceNumber:I

.method public static objectGetIntField()I
    .locals 1
    sget-object v0, Ltypes/TestInvalidFieldOpcode;->number:I
    return v0
.end method

.method public static objectPutIntField(I)V
    .locals 0
    sput-object p0, Ltypes/TestInvalidFieldOpcode;->number:I
    return-void
.end method

.method public static wideGetIntField()I
    .locals 2
    sget-wide v0, Ltypes/TestInvalidFieldOpcode;->number:I
    return v0
.end method

.method public static validGet()I
    .locals 1
    sget v0, Ltypes/TestInvalidFieldOpcode;->number:I
    return v0
.end method

.method public static validPut(I)V
    .locals 0
    sput p0, Ltypes/TestInvalidFieldOpcode;->number:I
    return-void
.end method

.method public instanceGetStatic()I
    .locals 1
    iget v0, p0, Ltypes/TestInvalidFieldOpcode;->number:I
    return v0
.end method

.method public instancePutStatic(I)V
    .locals 0
    iput p1, p0, Ltypes/TestInvalidFieldOpcode;->number:I
    return-void
.end method

.method public static staticGetInstance()I
    .locals 1
    sget v0, Ltypes/TestInvalidFieldOpcode;->instanceNumber:I
    return v0
.end method

.method public static staticPutInstance(I)V
    .locals 0
    sput p0, Ltypes/TestInvalidFieldOpcode;->instanceNumber:I
    return-void
.end method
