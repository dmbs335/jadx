.class public Ltrycatch/TestConditionalCancelCatch;
.super Ljava/lang/Object;

.annotation system Ldalvik/annotation/Signature;
    value = {
        "<D:",
        "Ljava/lang/Object;",
        ">Ljava/lang/Object;"
    }
.end annotation

.field public cancelled:Z

.method public constructor <init>()V
    .registers 1
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    return-void
.end method

.method public load()Ljava/lang/Object;
    .registers 2
    .annotation system Ldalvik/annotation/Signature;
        value = { "()TD;" }
    .end annotation
    const-string v0, "loaded"
    return-object v0
.end method

.method public isCancelled()Z
    .registers 2
    iget-boolean v0, p0, Ltrycatch/TestConditionalCancelCatch;->cancelled:Z
    return v0
.end method

.method public run()Ljava/lang/Object;
    .registers 3
    .annotation system Ldalvik/annotation/Signature;
        value = { "()TD;" }
    .end annotation

    :try_start
    invoke-virtual {p0}, Ltrycatch/TestConditionalCancelCatch;->load()Ljava/lang/Object;
    move-result-object v0
    :try_end
    .catch Ljava/lang/RuntimeException; {:try_start .. :try_end} :handler
    return-object v0

    :handler
    move-exception v0
    invoke-virtual {p0}, Ltrycatch/TestConditionalCancelCatch;->isCancelled()Z
    move-result v1
    if-eqz v1, :rethrow
    const/4 v0, 0x0
    return-object v0

    :rethrow
    throw v0
.end method
