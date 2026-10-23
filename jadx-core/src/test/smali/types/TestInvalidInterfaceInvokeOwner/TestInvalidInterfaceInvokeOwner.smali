.class public Ltypes/TestInvalidInterfaceInvokeOwner;
.super Ljava/lang/Object;

.method public static callAsVirtual(Ltypes/TestInvalidInterfaceInvokeOwner$I;)I
    .locals 1
    invoke-virtual {p0}, Ltypes/TestInvalidInterfaceInvokeOwner$I;->value()I
    move-result v0
    return v0
.end method

.method public static callAsInterface(Ltypes/TestInvalidInterfaceInvokeOwner$I;)I
    .locals 1
    invoke-interface {p0}, Ltypes/TestInvalidInterfaceInvokeOwner$I;->value()I
    move-result v0
    return v0
.end method
