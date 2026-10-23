.class public Ltypes/TestInvalidInvokeKind;
.super Ljava/lang/Object;

.method public instanceTarget()I
    .locals 1
    const/4 v0, 0x1
    return v0
.end method

.method public static staticTarget()I
    .locals 1
    const/4 v0, 0x2
    return v0
.end method

.method private privateTarget()I
    .locals 1
    const/4 v0, 0x3
    return v0
.end method

.method public static invokeStaticOnInstance()I
    .locals 1
    invoke-static {}, Ltypes/TestInvalidInvokeKind;->instanceTarget()I
    move-result v0
    return v0
.end method

.method public invokeVirtualOnStatic()I
    .locals 1
    invoke-virtual {p0}, Ltypes/TestInvalidInvokeKind;->staticTarget()I
    move-result v0
    return v0
.end method

.method public invokeDirectOnVirtual()I
    .locals 1
    invoke-direct {p0}, Ltypes/TestInvalidInvokeKind;->instanceTarget()I
    move-result v0
    return v0
.end method

.method public invokeVirtualOnPrivate()I
    .locals 1
    invoke-virtual {p0}, Ltypes/TestInvalidInvokeKind;->privateTarget()I
    move-result v0
    return v0
.end method

.method public invokeInterfaceOnClass()I
    .locals 1
    invoke-interface {p0}, Ltypes/TestInvalidInvokeKind;->instanceTarget()I
    move-result v0
    return v0
.end method

.method public static validStatic()I
    .locals 1
    invoke-static {}, Ltypes/TestInvalidInvokeKind;->staticTarget()I
    move-result v0
    return v0
.end method

.method public validVirtual()I
    .locals 1
    invoke-virtual {p0}, Ltypes/TestInvalidInvokeKind;->instanceTarget()I
    move-result v0
    return v0
.end method

.method public validDirect()I
    .locals 1
    invoke-direct {p0}, Ltypes/TestInvalidInvokeKind;->privateTarget()I
    move-result v0
    return v0
.end method
