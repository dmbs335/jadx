.class public Linner/TopLevelCaptureCaller;
.super Ljava/lang/Object;

.method public constructor <init>()V
    .registers 1

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    return-void
.end method

.method public static capture(Linner/TopLevelCaptureCaller;)Ljava/lang/Runnable;
    .registers 2
    .param p0, "caller"

    new-instance v0, Linner/TopLevelCaptureRunnable;
    invoke-direct {v0, p0}, Linner/TopLevelCaptureRunnable;-><init>(Linner/TopLevelCaptureCaller;)V
    return-object v0
.end method

.method public captureThis()Ljava/lang/Runnable;
    .registers 2

    new-instance v0, Linner/TopLevelThisRunnable;
    invoke-direct {v0, p0}, Linner/TopLevelThisRunnable;-><init>(Linner/TopLevelCaptureCaller;)V
    return-object v0
.end method

.method public print()V
    .registers 3

    sget-object v0, Ljava/lang/System;->out:Ljava/io/PrintStream;
    const-string v1, "captured"
    invoke-virtual {v0, v1}, Ljava/io/PrintStream;->println(Ljava/lang/String;)V
    return-void
.end method
