.class public Ltrycatch/TestExceptionHandlerPreTryRegisterState;
.super Ljava/lang/Object;

.field private static VALUE:J

.method public static test(Z)V
    .locals 5

    sget-wide v0, Ltrycatch/TestExceptionHandlerPreTryRegisterState;->VALUE:J
    if-eqz p0, :second_path

    move-wide v2, v0
    const/4 v0, 0x0
    :first_try_start
    invoke-static {}, Ltrycatch/TestExceptionHandlerPreTryRegisterState;->mayThrow()V
    :first_try_end
    .catchall {:first_try_start .. :first_try_end} :handler
    goto :done

    :second_path
    move-wide v2, v0
    const/4 v0, 0x0
    :second_try_start
    invoke-static {}, Ltrycatch/TestExceptionHandlerPreTryRegisterState;->mayThrow()V
    :second_try_end
    .catchall {:second_try_start .. :second_try_end} :handler

    :done
    return-void

    :handler
    move-exception v4
    invoke-static {v2, v3}, Ltrycatch/TestExceptionHandlerPreTryRegisterState;->useLong(J)V
    invoke-static {v0}, Ltrycatch/TestExceptionHandlerPreTryRegisterState;->useObject(Ljava/lang/Object;)V
    throw v4
.end method

.method private static mayThrow()V
    .locals 0
    return-void
.end method

.method private static useLong(J)V
    .locals 0
    return-void
.end method

.method private static useObject(Ljava/lang/Object;)V
    .locals 0
    return-void
.end method
