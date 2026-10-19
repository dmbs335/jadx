.class public Ltypes/TestInvalidThrowOperand;
.super Ljava/lang/Object;

.method public static primitive(I)V
    .locals 0

    throw p0
.end method

.method public static object(Ljava/lang/Object;)V
    .locals 0

    throw p0
.end method

.method public static string(Ljava/lang/String;)V
    .locals 0

    throw p0
.end method

.method public static array([Ljava/lang/Throwable;)V
    .locals 0

    throw p0
.end method

.method public static valid(Ljava/lang/Throwable;)V
    .locals 0

    throw p0
.end method

.method public static validSubtype(Ljava/lang/RuntimeException;)V
    .locals 0

    throw p0
.end method

.method public static nullValue()V
    .locals 1

    const/4 v0, 0x0
    throw v0
.end method

.method public static getString()Ljava/lang/String;
    .locals 1

    const-string v0, "not throwable"
    return-object v0
.end method

.method public static invoke()V
    .locals 1

    invoke-static {}, Ltypes/TestInvalidThrowOperand;->getString()Ljava/lang/String;
    move-result-object v0
    throw v0
.end method
