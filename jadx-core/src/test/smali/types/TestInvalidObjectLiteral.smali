.class public Ltypes/TestInvalidObjectLiteral;
.super Ljava/lang/Object;

.field private static target:Ljava/lang/Object;

.method public static value()Ljava/lang/Object;
    .locals 1

    const/4 v0, 0x1
    return-object v0
.end method

.method public static arrayValue()[Ljava/lang/Object;
    .locals 1

    const/4 v0, 0x1
    return-object v0
.end method

.method public static invokeValue()V
    .locals 1

    const/4 v0, 0x1
    invoke-static {v0}, Ltypes/TestInvalidObjectLiteral;->consume(Ljava/lang/Object;)V
    return-void
.end method

.method public static storeValue()V
    .locals 1

    const/4 v0, 0x1
    sput-object v0, Ltypes/TestInvalidObjectLiteral;->target:Ljava/lang/Object;
    return-void
.end method

.method public static nullValue()Ljava/lang/Object;
    .locals 1

    const/4 v0, 0x0
    return-object v0
.end method

.method public static throwValue()V
    .locals 1

    const/4 v0, 0x1
    throw v0
.end method

.method public static throwNull()V
    .locals 1

    const/4 v0, 0x0
    throw v0
.end method

.method private static consume(Ljava/lang/Object;)V
    .locals 0

    return-void
.end method
