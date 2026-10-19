.class public Ltypes/TestInvalidInvokeReceiver;
.super Ljava/lang/Object;

.method public target()I
    .locals 1

    const/4 v0, 0x2
    return v0
.end method

.method public static literal()I
    .locals 2

    const/4 v0, 0x1
    invoke-virtual {v0}, Ltypes/TestInvalidInvokeReceiver;->target()I
    move-result v1
    return v1
.end method

.method public static primitive(I)I
    .locals 1

    invoke-virtual {p0}, Ltypes/TestInvalidInvokeReceiver;->target()I
    move-result v0
    return v0
.end method

.method public static object(Ljava/lang/Object;)I
    .locals 1

    invoke-virtual {p0}, Ltypes/TestInvalidInvokeReceiver;->target()I
    move-result v0
    return v0
.end method

.method public static valid(Ltypes/TestInvalidInvokeReceiver;)I
    .locals 1

    invoke-virtual {p0}, Ltypes/TestInvalidInvokeReceiver;->target()I
    move-result v0
    return v0
.end method

.method public static boxed()I
    .locals 2

    const/4 v0, 0x1
    invoke-static {v0}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
    move-result-object v0
    invoke-virtual {v0}, Ljava/lang/Integer;->intValue()I
    move-result v1
    return v1
.end method

.method public static nullValue()I
    .locals 2

    const/4 v0, 0x0
    invoke-virtual {v0}, Ltypes/TestInvalidInvokeReceiver;->target()I
    move-result v1
    return v1
.end method

.method public static getInt()I
    .locals 1

    const/4 v0, 0x1
    return v0
.end method

.method public static invokeReceiver()I
    .locals 2

    invoke-static {}, Ltypes/TestInvalidInvokeReceiver;->getInt()I
    move-result v0
    invoke-virtual {v0}, Ltypes/TestInvalidInvokeReceiver;->target()I
    move-result v1
    return v1
.end method
