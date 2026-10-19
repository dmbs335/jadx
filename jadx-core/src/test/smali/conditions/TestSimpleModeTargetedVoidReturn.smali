.class public Lconditions/TestSimpleModeTargetedVoidReturn;
.super Ljava/lang/Object;

.method public static test(Z)V
    .registers 1
    if-eqz p0, :done
    invoke-static {}, Lconditions/TestSimpleModeTargetedVoidReturn;->record()V
    :done
    return-void
.end method

.method public static record()V
    .registers 0
    return-void
.end method
