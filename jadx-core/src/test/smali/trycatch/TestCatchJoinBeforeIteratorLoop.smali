.class public Ltrycatch/TestCatchJoinBeforeIteratorLoop;
.super Ljava/lang/Object;

.method private static use(Ljava/lang/String;)V
    .locals 0

    return-void
.end method

.method public static test(Landroid/content/SharedPreferences;Ljava/util/Set;)I
    .locals 6

    const/4 v0, -0x1

    :try_start
    const-string v1, "key"
    invoke-interface {p0, v1, v0}, Landroid/content/SharedPreferences;->getInt(Ljava/lang/String;I)I
    move-result v0
    :try_end
    goto :after_catch

    :catch_class_cast
    move-exception v1
    const/4 v0, -0x1

    :after_catch
    move-object v3, p1
    if-nez v3, :have_set
    invoke-static {}, Ljava/util/Collections;->emptySet()Ljava/util/Set;
    move-result-object v3

    :have_set
    invoke-interface {v3}, Ljava/util/Set;->iterator()Ljava/util/Iterator;
    move-result-object v1

    :loop
    move-object v4, v1
    invoke-interface {v4}, Ljava/util/Iterator;->hasNext()Z
    move-result v5
    if-eqz v5, :done
    invoke-interface {v4}, Ljava/util/Iterator;->next()Ljava/lang/Object;
    move-result-object v5
    check-cast v5, Ljava/lang/String;
    invoke-static {v5}, Ltrycatch/TestCatchJoinBeforeIteratorLoop;->use(Ljava/lang/String;)V
    move-object v1, v4
    goto :loop

    :done
    return v0

    .catch Ljava/lang/ClassCastException; {:try_start .. :try_end} :catch_class_cast
.end method
