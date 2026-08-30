.class public final Lconstructors/ConstructorFluentBuilderBranchSideEffectHelper;
.super Ljava/lang/Object;

.method public static make(I)Ljava/lang/StringBuilder;
    .locals 1

    new-instance v0, Ljava/lang/StringBuilder;
    invoke-direct {v0}, Ljava/lang/StringBuilder;-><init>()V
    invoke-virtual {v0, p0}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;
    return-object v0
.end method

.method public static touch()V
    .locals 0

    return-void
.end method
