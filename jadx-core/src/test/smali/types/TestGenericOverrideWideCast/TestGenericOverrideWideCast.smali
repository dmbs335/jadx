.class public final Ltypes/TestGenericOverrideWideCast;
.super Ltypes/TestGenericOverrideWideCast$NavType;

.annotation system Ldalvik/annotation/Signature;
    value = {
        "Ltypes/TestGenericOverrideWideCast$NavType<",
        "Ltypes/TestGenericOverrideWideCast$Model;",
        ">;"
    }
.end annotation

.method public constructor <init>()V
    .registers 1
    invoke-direct {p0}, Ltypes/TestGenericOverrideWideCast$NavType;-><init>()V
    return-void
.end method

.method public get()Ltypes/TestGenericOverrideWideCast$Parcelable;
    .registers 1
    invoke-static {}, Ltypes/TestGenericOverrideWideCast$Factory;->useDirect()Z
    move-result v0
    if-eqz v0, :direct

    invoke-static {}, Ltypes/TestGenericOverrideWideCast$Factory;->create()Ljava/lang/Object;
    move-result-object v0
    check-cast v0, Ltypes/TestGenericOverrideWideCast$Parcelable;
    return-object v0

    :direct
    invoke-static {}, Ltypes/TestGenericOverrideWideCast$Factory;->createParcelable()Ltypes/TestGenericOverrideWideCast$Parcelable;
    move-result-object v0
    return-object v0
.end method
