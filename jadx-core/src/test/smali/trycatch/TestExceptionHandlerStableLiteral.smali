.class public Ltrycatch/TestExceptionHandlerStableLiteral;
.super Ljava/lang/Object;

.method public static test(Z)Ljava/lang/Object;
    .locals 2

    :try_start_inner
    if-eqz p0, :throw_tail
    invoke-static {}, Ltrycatch/TestExceptionHandlerStableLiteral;->mayThrow()V
    :try_end_inner
    .catch Ljava/lang/Exception; {:try_start_inner .. :try_end_inner} :inner_catch

    const/4 v0, 0x0

    :try_start_outer
    invoke-static {}, Ltrycatch/TestExceptionHandlerStableLiteral;->mayThrow()V
    goto :return

    :throw_tail
    const/4 v0, 0x0
    new-instance v1, Ljava/lang/IllegalStateException;
    invoke-direct {v1}, Ljava/lang/IllegalStateException;-><init>()V
    throw v1
    :try_end_outer
    .catch Ljava/lang/Exception; {:try_start_outer .. :try_end_outer} :outer_catch

    :inner_catch
    const/4 v0, 0x0
    goto :return

    :outer_catch
    goto :return

    :return
    return-object v0
.end method

.method private static mayThrow()V
    .locals 0

    return-void
.end method
