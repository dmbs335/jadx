.class public Ltypes/TestBooleanCharSwitchPhi;
.super Ljava/lang/Object;

.method public static test(ZC)I
    .registers 4

    if-eqz p0, :use_char
    move v0, p0
    goto :selector

    :use_char
    move v0, p1

    :selector
    packed-switch v0, :switch_data
    const/4 v1, -0x1
    return v1

    :case_zero
    const/4 v1, 0x0
    return v1

    :case_one
    const/4 v1, 0x1
    return v1

    :switch_data
    .packed-switch 0x0
        :case_zero
        :case_one
    .end packed-switch
.end method
