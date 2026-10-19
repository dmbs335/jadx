.class public Ltypes/TestInvalidInvokeArgument;
.super Ljava/lang/Object;

.method public static getString()Ljava/lang/String;
    .locals 1
    const-string v0, "text"
    return-object v0
.end method

.method public static getInt()I
    .locals 1
    const/4 v0, 0x1
    return v0
.end method

.method public static consumeInt(I)V
    .locals 0
    return-void
.end method

.method public static consumeString(Ljava/lang/String;)V
    .locals 0
    return-void
.end method

.method public static consumeNumber(Ljava/lang/Number;)V
    .locals 0
    return-void
.end method

.method public static consumeObject(Ljava/lang/Object;)V
    .locals 0
    return-void
.end method

.method public static stringToInt()V
    .locals 1
    invoke-static {}, Ltypes/TestInvalidInvokeArgument;->getString()Ljava/lang/String;
    move-result-object v0
    invoke-static {v0}, Ltypes/TestInvalidInvokeArgument;->consumeInt(I)V
    return-void
.end method

.method public static intToString()V
    .locals 1
    invoke-static {}, Ltypes/TestInvalidInvokeArgument;->getInt()I
    move-result v0
    invoke-static {v0}, Ltypes/TestInvalidInvokeArgument;->consumeString(Ljava/lang/String;)V
    return-void
.end method

.method public static parameterStringToInt(Ljava/lang/String;)V
    .locals 0
    invoke-static {p0}, Ltypes/TestInvalidInvokeArgument;->consumeInt(I)V
    return-void
.end method

.method public static parameterStringToNumber(Ljava/lang/String;)V
    .locals 0
    invoke-static {p0}, Ltypes/TestInvalidInvokeArgument;->consumeNumber(Ljava/lang/Number;)V
    return-void
.end method

.method public static stringToObject()V
    .locals 1
    invoke-static {}, Ltypes/TestInvalidInvokeArgument;->getString()Ljava/lang/String;
    move-result-object v0
    invoke-static {v0}, Ltypes/TestInvalidInvokeArgument;->consumeObject(Ljava/lang/Object;)V
    return-void
.end method

.method public static intToObject()V
    .locals 1
    invoke-static {}, Ltypes/TestInvalidInvokeArgument;->getInt()I
    move-result v0
    invoke-static {v0}, Ltypes/TestInvalidInvokeArgument;->consumeObject(Ljava/lang/Object;)V
    return-void
.end method
