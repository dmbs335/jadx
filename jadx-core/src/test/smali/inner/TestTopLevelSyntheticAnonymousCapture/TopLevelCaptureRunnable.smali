.class public final synthetic Linner/TopLevelCaptureRunnable;
.super Ljava/lang/Object;

.implements Ljava/lang/Runnable;

.field public final synthetic d:Linner/TopLevelCaptureCaller;

.method public synthetic constructor <init>(Linner/TopLevelCaptureCaller;)V
    .registers 2

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    iput-object p1, p0, Linner/TopLevelCaptureRunnable;->d:Linner/TopLevelCaptureCaller;
    return-void
.end method

.method public final run()V
    .registers 2

    iget-object v0, p0, Linner/TopLevelCaptureRunnable;->d:Linner/TopLevelCaptureCaller;
    invoke-virtual {v0}, Linner/TopLevelCaptureCaller;->print()V
    return-void
.end method
