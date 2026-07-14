.class public Ltrycatch/TestSharedCatchDefaultAssignment;
.super Ljava/lang/Object;

.method public static parse(Ljava/lang/String;)F
    .locals 3

    const/4 v0, 0x0
    if-eqz p0, :use_zero

    :try_start
    invoke-static {p0}, Ljava/lang/Float;->parseFloat(Ljava/lang/String;)F
    move-result v1
    goto :after
    :try_end
    .catch Ljava/lang/NumberFormatException; {:try_start .. :try_end} :catch

    :catch
    move-exception v2

    :use_zero
    move v1, v0

    :after
    cmpl-float v2, v1, v0
    if-lez v2, :return_zero
    return v1

    :return_zero
    return v0
.end method
