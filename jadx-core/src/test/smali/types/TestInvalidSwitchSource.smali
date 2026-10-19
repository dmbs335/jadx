.class public Ltypes/TestInvalidSwitchSource;
.super Ljava/lang/Object;

.method public static floatSwitch(F)I
    .locals 1
    packed-switch p0, :switch_data
    const/4 v0, 0x0
    return v0
    :case_1
    const/4 v0, 0x1
    return v0
    :switch_data
    .packed-switch 0x1
        :case_1
    .end packed-switch
.end method

.method public static validIntSwitch(I)I
    .locals 1
    packed-switch p0, :switch_data
    const/4 v0, 0x0
    return v0
    :case_1
    const/4 v0, 0x1
    return v0
    :switch_data
    .packed-switch 0x1
        :case_1
    .end packed-switch
.end method

.method public static booleanSwitch(Z)I
    .locals 1
    packed-switch p0, :switch_data
    const/4 v0, 0x0
    return v0
    :case_1
    const/4 v0, 0x1
    return v0
    :switch_data
    .packed-switch 0x1
        :case_1
    .end packed-switch
.end method
