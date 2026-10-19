.class public Lsynchronize/TestInvalidMonitorOperand;
.super Ljava/lang/Object;

.method public static invalid(I)V
    .locals 0

    monitor-enter p0
    monitor-exit p0
    return-void
.end method

.method public static boxed()V
    .locals 1

    const/4 v0, 0x1
    invoke-static {v0}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
    move-result-object v0
    monitor-enter v0
    monitor-exit v0
    return-void
.end method

.method public static getInt()I
    .locals 1

    const/4 v0, 0x1
    return v0
.end method

.method public static invalidInvoke()V
    .locals 1

    invoke-static {}, Lsynchronize/TestInvalidMonitorOperand;->getInt()I
    move-result v0
    monitor-enter v0
    monitor-exit v0
    return-void
.end method
