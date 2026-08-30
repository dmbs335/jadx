.class public final Ltypes/TestGenericOverrideWideCast$Factory;
.super Ljava/lang/Object;

.method public static create()Ljava/lang/Object;
    .registers 1
    new-instance v0, Ltypes/TestGenericOverrideWideCast$Model;
    invoke-direct {v0}, Ltypes/TestGenericOverrideWideCast$Model;-><init>()V
    return-object v0
.end method

.method public static createParcelable()Ltypes/TestGenericOverrideWideCast$Parcelable;
    .registers 1
    new-instance v0, Ltypes/TestGenericOverrideWideCast$Model;
    invoke-direct {v0}, Ltypes/TestGenericOverrideWideCast$Model;-><init>()V
    return-object v0
.end method

.method public static useDirect()Z
    .registers 1
    const/4 v0, 0x0
    return v0
.end method
