.class public interface abstract Ltypes/TestInvalidInterfaceInvokeOwner$I;
.super Ljava/lang/Object;

.method public abstract value()I
.end method

.method private privateValue()I
    .locals 1
    const/4 v0, 0x1
    return v0
.end method

.method public callOwnPrivate()I
    .locals 1
    invoke-direct {p0}, Ltypes/TestInvalidInterfaceInvokeOwner$I;->privateValue()I
    move-result v0
    return v0
.end method
