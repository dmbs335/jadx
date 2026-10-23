.class public Ltypes/TestInvalidPayloadControlFlow;
.super Ljava/lang/Object;

.method public static branchToArrayPayload()[I
    .locals 2
    const/4 v0, 0x1
    new-array v1, v0, [I
    fill-array-data v1, :data
    goto :data
    return-object v1

    :data
    .array-data 4
        0x7
    .end array-data
.end method

.method public static branchToSwitchPayload(I)I
    .locals 1
    packed-switch p0, :switch_data
    goto :switch_data
    :case_zero
    const/4 v0, 0x0
    return v0

    :switch_data
    .packed-switch 0x0
        :case_zero
    .end packed-switch
.end method

.method public static validArray()[I
    .locals 2
    const/4 v0, 0x1
    new-array v1, v0, [I
    fill-array-data v1, :valid_data
    return-object v1

    :valid_data
    .array-data 4
        0x7
    .end array-data
.end method

.method public static fallthroughIntoArrayPayload()[I
    .locals 2
    const/4 v0, 0x1
    new-array v1, v0, [I
    fill-array-data v1, :fallthrough_data

    :fallthrough_data
    .array-data 4
        0x7
    .end array-data
    return-object v1
.end method

.method public static fallthroughIntoSwitchPayload(I)I
    .locals 1
    packed-switch p0, :fallthrough_switch_data

    :fallthrough_switch_data
    .packed-switch 0x0
        :case_zero
    .end packed-switch

    :case_zero
    const/4 v0, 0x0
    return v0
.end method
