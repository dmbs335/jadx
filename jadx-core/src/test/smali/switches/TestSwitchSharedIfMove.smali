.class public Lswitches/TestSwitchSharedIfMove;
.super Ljava/lang/Object;

.method public static test(ILjava/lang/String;)I
    .registers 5

    const/4 v0, 0x0
    packed-switch p0, :switch_data
    return v0

    :case_0
    const-string v1, "png"
    invoke-virtual {p1, v1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
    move-result v1
    if-eqz v1, :false_move
    goto :true_move

    :case_1
    const-string v1, "jpeg"
    invoke-virtual {p1, v1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
    move-result v1
    if-eqz v1, :false_move

    :true_move
    move v1, p0
    goto :true_body

    :false_move
    move v1, p0
    goto :false_body

    :true_body
    invoke-static {p0}, Lswitches/TestSwitchSharedIfMove;->trueValue(I)I
    move-result v0
    goto :join

    :false_body
    invoke-static {p0}, Lswitches/TestSwitchSharedIfMove;->falseValue(I)I
    move-result v0

    :join
    add-int/lit8 v0, v0, 0x1
    return v0

    :switch_data
    .packed-switch 0x0
        :case_0
        :case_1
    .end packed-switch
.end method

.method private static trueValue(I)I
    .registers 1
    add-int/2addr p0, p0
    return p0
.end method

.method private static falseValue(I)I
    .registers 2
    const/4 v0, 0x0
    sub-int/2addr v0, p0
    return v0
.end method
