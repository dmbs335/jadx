.class public Ltrycatch/TestExceptionHandlerPhi;
.super Ljava/lang/Object;

.field private J$0:J

.method public test(I)J
    .locals 3

    if-eqz p1, :resume

    const-wide/16 v0, 0x2a
    iput-wide v0, p0, Ltrycatch/TestExceptionHandlerPhi;->J$0:J

    :try_start_call
    invoke-static {}, Ltrycatch/TestExceptionHandlerPhi;->mayThrow()V
    :try_end_call
    return-wide v0

    :resume
    iget-wide v0, p0, Ltrycatch/TestExceptionHandlerPhi;->J$0:J

    :try_start_resume
    invoke-static {}, Ltrycatch/TestExceptionHandlerPhi;->mayThrow()V
    :try_end_resume
    return-wide v0

    :catch
    move-exception v2
    iput-wide v0, p0, Ltrycatch/TestExceptionHandlerPhi;->J$0:J
    return-wide v0

    .catch Ljava/lang/Exception; {:try_start_call .. :try_end_call} :catch
    .catch Ljava/lang/Exception; {:try_start_resume .. :try_end_resume} :catch
.end method

.method private static mayThrow()V
    .locals 0

    return-void
.end method
