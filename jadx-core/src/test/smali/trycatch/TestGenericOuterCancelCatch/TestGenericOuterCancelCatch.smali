.class public abstract Ltrycatch/TestGenericOuterCancelCatch;
.super Ljava/lang/Object;

.annotation system Ldalvik/annotation/Signature;
    value = { "<D:Ljava/lang/Object;>Ljava/lang/Object;" }
.end annotation

.method public constructor <init>()V
    .registers 1
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    return-void
.end method

.method public onLoadInBackground()Ljava/lang/Object;
    .registers 2
    .annotation system Ldalvik/annotation/Signature;
        value = { "()TD;" }
    .end annotation
    const-string v0, "loaded"
    return-object v0
.end method
