.class public Ltrycatch/TestEmptyCatchJoinAssignment;
.super Ljava/lang/Object;

.method public static test(Ljava/lang/String;)Ljava/lang/Object;
    .locals 2

    const/4 v1, 0x0
    invoke-static {p0, v1}, Ltrycatch/TestEmptyCatchJoinAssignment;->lookup(Ljava/lang/String;I)Ljava/lang/Object;
    move-result-object v0
    if-nez v0, :return

    :try_start_1
    const/4 v1, 0x1
    invoke-static {p0, v1}, Ltrycatch/TestEmptyCatchJoinAssignment;->lookup(Ljava/lang/String;I)Ljava/lang/Object;
    move-result-object v0
    :try_end_1
    goto :after_catch_1

    :catch_1
    move-exception v1

    :after_catch_1
    if-nez v0, :return

    const/4 v1, 0x2
    invoke-static {p0, v1}, Ltrycatch/TestEmptyCatchJoinAssignment;->lookup(Ljava/lang/String;I)Ljava/lang/Object;
    move-result-object v0
    if-nez v0, :return

    :try_start_2
    const/4 v1, 0x3
    invoke-static {p0, v1}, Ltrycatch/TestEmptyCatchJoinAssignment;->lookup(Ljava/lang/String;I)Ljava/lang/Object;
    move-result-object v0
    :try_end_2
    goto :return

    :catch_2
    move-exception v1

    :return
    invoke-static {v0, v0}, Ltrycatch/TestEmptyCatchJoinAssignment;->combine(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v0
    return-object v0

    .catch Ljava/lang/IllegalArgumentException; {:try_start_1 .. :try_end_1} :catch_1
    .catch Ljava/lang/IllegalArgumentException; {:try_start_2 .. :try_end_2} :catch_2
.end method

.method private static combine(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    .locals 0

    return-object p0
.end method

.method private static lookup(Ljava/lang/String;I)Ljava/lang/Object;
    .locals 0

    return-object p0
.end method
