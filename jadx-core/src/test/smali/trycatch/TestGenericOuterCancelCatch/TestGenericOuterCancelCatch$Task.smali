.class public Ltrycatch/TestGenericOuterCancelCatch$Task;
.super Ltrycatch/TestGenericOuterCancelCatch$Base;

.annotation system Ldalvik/annotation/EnclosingClass;
    value = Ltrycatch/TestGenericOuterCancelCatch;
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x0
    name = "Task"
.end annotation

.field final synthetic this$0:Ltrycatch/TestGenericOuterCancelCatch;

.method public constructor <init>(Ltrycatch/TestGenericOuterCancelCatch;)V
    .registers 2
    iput-object p1, p0, Ltrycatch/TestGenericOuterCancelCatch$Task;->this$0:Ltrycatch/TestGenericOuterCancelCatch;
    invoke-direct {p0}, Ltrycatch/TestGenericOuterCancelCatch$Base;-><init>()V
    return-void
.end method

.method public doInBackground([Ljava/lang/Void;)Ljava/lang/Object;
    .registers 4
    .annotation system Ldalvik/annotation/Signature;
        value = { "([Ljava/lang/Void;)TD;" }
    .end annotation

    :try_start
    iget-object v0, p0, Ltrycatch/TestGenericOuterCancelCatch$Task;->this$0:Ltrycatch/TestGenericOuterCancelCatch;
    invoke-virtual {v0}, Ltrycatch/TestGenericOuterCancelCatch;->onLoadInBackground()Ljava/lang/Object;
    move-result-object v0
    :try_end
    .catch Ljava/lang/IllegalStateException; {:try_start .. :try_end} :handler
    return-object v0

    :handler
    move-exception v0
    invoke-virtual {p0}, Ltrycatch/TestGenericOuterCancelCatch$Task;->isCancelled()Z
    move-result v1
    if-eqz v1, :rethrow
    const/4 v0, 0x0
    return-object v0

    :rethrow
    throw v0
.end method
