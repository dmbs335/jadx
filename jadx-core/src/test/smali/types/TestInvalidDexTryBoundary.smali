.class public Ltypes/TestInvalidDexTryBoundary;
.super Ljava/lang/Object;

.method public static run()I
    .locals 1
    :try_start
    const/16 v0, 0x7
    :try_end
    .catchall {:try_start .. :try_end} :handler
    return v0
    :handler
    const/4 v0, 0x0
    return v0
.end method
