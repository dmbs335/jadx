.class public Ltypes/TestInvalidMoveException;
.super Ljava/lang/Object;

.method public static orphan()Ljava/lang/Object;
    .locals 1
    move-exception v0
    return-object v0
.end method

.method public static delayedHandler()Ljava/lang/Object;
    .locals 1
    :try_start
    const/4 v0, 0x0
    throw v0
    :try_end
    .catchall {:try_start .. :try_end} :handler

    :handler
    nop
    move-exception v0
    return-object v0
.end method

.method public static branchToHandler()Ljava/lang/Object;
    .locals 1
    goto :handler
    :try_start
    const/4 v0, 0x0
    throw v0
    :try_end
    .catchall {:try_start .. :try_end} :handler

    :handler
    move-exception v0
    return-object v0
.end method

.method public static conditionalBranchToHandler(Z)Ljava/lang/Object;
    .locals 1
    if-eqz p0, :handler
    :try_start
    const/4 v0, 0x0
    throw v0
    :try_end
    .catchall {:try_start .. :try_end} :handler

    :handler
    move-exception v0
    return-object v0
.end method

.method public static fallthroughIntoHandler()Ljava/lang/Object;
    .locals 1
    :try_start
    invoke-static {}, Ljava/lang/System;->gc()V
    :try_end
    .catchall {:try_start .. :try_end} :handler

    :handler
    move-exception v0
    return-object v0
.end method

.method public static validHandler()Ljava/lang/Object;
    .locals 1
    :try_start
    invoke-static {}, Ljava/lang/System;->gc()V
    :try_end
    .catchall {:try_start .. :try_end} :handler
    const/4 v0, 0x0
    return-object v0

    :handler
    move-exception v0
    return-object v0
.end method
