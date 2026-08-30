.class public Ltrycatch/TestExceptionHandlerTypeSplit;
.super Ljava/lang/Object;

.field private static CHARSET:Ljava/nio/charset/Charset;

.method public static test(Ljava/io/File;Ljava/lang/String;)V
    .locals 3

    const/4 v0, 0x0

    :outer_create_start
    new-instance v1, Ljava/io/FileOutputStream;
    invoke-direct {v1, p0}, Ljava/io/FileOutputStream;-><init>(Ljava/io/File;)V
    :outer_create_end
    .catch Ljava/lang/Exception; {:outer_create_start .. :outer_create_end} :outer_exception
    .catchall {:outer_create_start .. :outer_create_end} :outer_all

    :inner_start
    sget-object v0, Ltrycatch/TestExceptionHandlerTypeSplit;->CHARSET:Ljava/nio/charset/Charset;
    invoke-virtual {p1, v0}, Ljava/lang/String;->getBytes(Ljava/nio/charset/Charset;)[B
    move-result-object p1
    invoke-virtual {v1, p1}, Ljava/io/FileOutputStream;->write([B)V
    :inner_end
    .catch Ljava/lang/Exception; {:inner_start .. :inner_end} :inner_exception
    .catchall {:inner_start .. :inner_end} :inner_all

    goto :cleanup

    :inner_exception
    move-object v0, v1

    :outer_exception
    :outer_log_start
    invoke-static {}, Ltrycatch/TestExceptionHandlerTypeSplit;->log()V
    :outer_log_end
    .catchall {:outer_log_start .. :outer_log_end} :outer_all

    move-object v1, v0

    :cleanup
    invoke-static {v1}, Ltrycatch/TestExceptionHandlerTypeSplit;->close(Ljava/io/Closeable;)V
    return-void

    :inner_all
    move-exception v2
    goto :close_and_throw

    :outer_all
    move-exception v2
    move-object v1, v0

    :close_and_throw
    invoke-static {v1}, Ltrycatch/TestExceptionHandlerTypeSplit;->close(Ljava/io/Closeable;)V
    throw v2
.end method

.method private static close(Ljava/io/Closeable;)V
    .locals 0

    return-void
.end method

.method private static log()V
    .locals 0

    return-void
.end method
