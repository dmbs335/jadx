.class public Ltrycatch/TestNullableCastCatch;
.super Ljava/lang/Object;

.method public static test(Landroid/os/Bundle;Ljava/lang/Class;)Ljava/lang/Object;
    .locals 2

    const/4 v0, 0x0
    if-eqz p0, :return

    const-string v1, "r"
    invoke-virtual {p0, v1}, Landroid/os/BaseBundle;->get(Ljava/lang/String;)Ljava/lang/Object;
    move-result-object p0
    if-eqz p0, :return

    :try_start
    invoke-virtual {p1, p0}, Ljava/lang/Class;->cast(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object p0
    :try_end
    return-object p0

    :catch
    move-exception v0
    invoke-virtual {p1}, Ljava/lang/Class;->getCanonicalName()Ljava/lang/String;
    move-result-object p1
    invoke-virtual {p0}, Ljava/lang/Object;->getClass()Ljava/lang/Class;
    move-result-object p0
    invoke-virtual {p0}, Ljava/lang/Class;->getCanonicalName()Ljava/lang/String;
    move-result-object p0
    filled-new-array {p1, p0}, [Ljava/lang/Object;
    move-result-object p0
    const-string p1, "Unexpected object type. Expected, Received: %s, %s"
    invoke-static {p1, p0}, Ljava/lang/String;->format(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;
    move-result-object p0
    const-string p1, "AM"
    invoke-static {p1, p0, v0}, Landroid/util/Log;->w(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I
    throw v0

    :return
    return-object v0

    .catch Ljava/lang/ClassCastException; {:try_start .. :try_end} :catch
.end method

.method public static getString(Landroid/os/Bundle;)Ljava/lang/String;
    .locals 1

    const-class v0, Ljava/lang/String;
    invoke-static {p0, v0}, Ltrycatch/TestNullableCastCatch;->test(Landroid/os/Bundle;Ljava/lang/Class;)Ljava/lang/Object;
    move-result-object p0
    check-cast p0, Ljava/lang/String;
    return-object p0
.end method

.method public static getLong(Landroid/os/Bundle;)Ljava/lang/Long;
    .locals 1

    const-class v0, Ljava/lang/Long;
    invoke-static {p0, v0}, Ltrycatch/TestNullableCastCatch;->test(Landroid/os/Bundle;Ljava/lang/Class;)Ljava/lang/Object;
    move-result-object p0
    check-cast p0, Ljava/lang/Long;
    return-object p0
.end method
