.class public Ltypes/TestInvalidArithOperand;
.super Ljava/lang/Object;

.field public static badField:Ljava/lang/String;

.method public static addObject(Ljava/lang/Object;I)I
    .locals 1

    add-int v0, p0, p1
    return v0
.end method
.method public static addString(Ljava/lang/String;I)I
    .locals 1

    add-int v0, p0, p1
    return v0
.end method

.method public static subObject(Ljava/lang/Object;I)I
    .locals 1

    sub-int v0, p0, p1
    return v0
.end method

.method public static validAdd(II)I
    .locals 1

    add-int v0, p0, p1
    return v0
.end method

.method public static addStringField(I)V
    .locals 1

    sget-object v0, Ltypes/TestInvalidArithOperand;->badField:Ljava/lang/String;
    add-int/2addr v0, p0
    sput-object v0, Ltypes/TestInvalidArithOperand;->badField:Ljava/lang/String;
    return-void
.end method

.method public static getObject()Ljava/lang/Object;
    .locals 1

    const/4 v0, 0x0
    return-object v0
.end method

.method public static addInvokeObject(I)I
    .locals 1

    invoke-static {}, Ltypes/TestInvalidArithOperand;->getObject()Ljava/lang/Object;
    move-result-object v0
    add-int/2addr v0, p0
    return v0
.end method

.method public static getString()Ljava/lang/String;
    .locals 1

    const-string v0, "text"
    return-object v0
.end method

.method public static addInvokeString(I)I
    .locals 1

    invoke-static {}, Ltypes/TestInvalidArithOperand;->getString()Ljava/lang/String;
    move-result-object v0
    add-int/2addr v0, p0
    return v0
.end method

.method public static addArrayObject([Ljava/lang/Object;II)I
    .locals 1

    aget-object v0, p0, p1
    add-int/2addr v0, p2
    return v0
.end method

.method public static addArrayString([Ljava/lang/String;II)I
    .locals 1

    aget-object v0, p0, p1
    add-int/2addr v0, p2
    return v0
.end method

.method public static validArrayInt([III)I
    .locals 1

    aget v0, p0, p1
    add-int/2addr v0, p2
    return v0
.end method

.method public static addCastString(Ljava/lang/Object;I)I
    .locals 1

    move-object v0, p0
    check-cast v0, Ljava/lang/String;
    add-int/2addr v0, p1
    return v0
.end method

.method public static addConstString(I)I
    .locals 1

    const-string v0, "text"
    add-int/2addr v0, p0
    return v0
.end method

.method public static getInteger()Ljava/lang/Integer;
    .locals 1

    const/4 v0, 0x0
    return-object v0
.end method

.method public static addInvokeInteger(I)I
    .locals 1

    invoke-static {}, Ltypes/TestInvalidArithOperand;->getInteger()Ljava/lang/Integer;
    move-result-object v0
    add-int/2addr v0, p0
    return v0
.end method

.method public static validUnboxedInteger(I)I
    .locals 1

    invoke-static {}, Ltypes/TestInvalidArithOperand;->getInteger()Ljava/lang/Integer;
    move-result-object v0
    invoke-virtual {v0}, Ljava/lang/Integer;->intValue()I
    move-result v0
    add-int/2addr v0, p0
    return v0
.end method

.method public static addBoxedParam(Ljava/lang/Integer;I)I
    .locals 1

    add-int v0, p0, p1
    return v0
.end method

.method public static validUnboxedParam(Ljava/lang/Integer;I)I
    .locals 1

    invoke-virtual {p0}, Ljava/lang/Integer;->intValue()I
    move-result v0
    add-int/2addr v0, p1
    return v0
.end method

.method public static addMovedBoxedParam(Ljava/lang/Integer;I)I
    .locals 1

    move-object v0, p0
    add-int/2addr v0, p1
    return v0
.end method

.method public static andBooleanInt(ZI)I
    .locals 1

    and-int v0, p0, p1
    return v0
.end method
