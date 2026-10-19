.class public Ltypes/TestInvalidFieldReceiver;
.super Ljava/lang/Object;

.field public value:I

.method public static getLiteral()I
    .locals 2

    const/4 v0, 0x1
    iget v1, v0, Ltypes/TestInvalidFieldReceiver;->value:I
    return v1
.end method

.method public static putLiteral()V
    .locals 2

    const/4 v0, 0x1
    const/4 v1, 0x2
    iput v1, v0, Ltypes/TestInvalidFieldReceiver;->value:I
    return-void
.end method

.method public static getPrimitive(I)I
    .locals 1

    iget v0, p0, Ltypes/TestInvalidFieldReceiver;->value:I
    return v0
.end method

.method public static putPrimitive(I)V
    .locals 1

    const/4 v0, 0x2
    iput v0, p0, Ltypes/TestInvalidFieldReceiver;->value:I
    return-void
.end method

.method public static getValid(Ltypes/TestInvalidFieldReceiver;)I
    .locals 1

    iget v0, p0, Ltypes/TestInvalidFieldReceiver;->value:I
    return v0
.end method

.method public static getNull()I
    .locals 2

    const/4 v0, 0x0
    iget v1, v0, Ltypes/TestInvalidFieldReceiver;->value:I
    return v1
.end method

.method public static putNull()V
    .locals 2

    const/4 v0, 0x0
    const/4 v1, 0x2
    iput v1, v0, Ltypes/TestInvalidFieldReceiver;->value:I
    return-void
.end method

.method public static getObject(Ljava/lang/Object;)I
    .locals 1

    iget v0, p0, Ltypes/TestInvalidFieldReceiver;->value:I
    return v0
.end method

.method public static putObject(Ljava/lang/Object;)V
    .locals 1

    const/4 v0, 0x2
    iput v0, p0, Ltypes/TestInvalidFieldReceiver;->value:I
    return-void
.end method

.method public static getInt()I
    .locals 1

    const/4 v0, 0x1
    return v0
.end method

.method public static getInvokeReceiver()I
    .locals 2

    invoke-static {}, Ltypes/TestInvalidFieldReceiver;->getInt()I
    move-result v0
    iget v1, v0, Ltypes/TestInvalidFieldReceiver;->value:I
    return v1
.end method
