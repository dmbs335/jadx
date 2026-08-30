.class public Lconditions/TestSwitchMergedConditionSharedCaseAction;
.super Ljava/lang/Object;

.method public static scan(Ljava/lang/String;[I)I
    .locals 10

    const/4 v0, 0x0

    :try_start
    :outer
    invoke-virtual {p0}, Ljava/lang/String;->length()I
    move-result v1
    if-ge v0, v1, :done

    const/4 v2, 0x0
    const/4 v3, 0x0
    const/4 v4, 0x0
    move v5, v0

    :inner
    invoke-virtual {p0}, Ljava/lang/String;->length()I
    move-result v1
    if-ge v5, v1, :next_outer

    invoke-virtual {p0, v5}, Ljava/lang/String;->charAt(I)C
    move-result v6
    const/16 v8, 0x45
    if-eq v6, v8, :exponent
    const/16 v8, 0x65
    if-eq v6, v8, :exponent

    packed-switch v6, :switch_data
    goto :after_switch

    :case_minus
    if-eq v5, v0, :after_switch
    if-nez v3, :after_switch
    goto :shared_action

    :case_dot
    if-nez v2, :shared_action
    const/4 v2, 0x1
    goto :after_switch

    :case_comma
    :shared_action
    const/4 v8, 0x0
    aget v9, p1, v8
    add-int/lit8 v9, v9, 0x1
    aput v9, p1, v8
    const/4 v4, 0x1

    :after_switch
    const/4 v3, 0x0
    if-nez v4, :next_outer
    add-int/lit8 v5, v5, 0x1
    goto :inner

    :exponent
    const/4 v3, 0x1
    add-int/lit8 v5, v5, 0x1
    goto :inner

    :next_outer
    add-int/lit8 v0, v0, 0x1
    goto :outer

    :done
    const/4 v8, 0x0
    aget v0, p1, v8
    :try_end
    return v0

    :catch
    const/4 v0, -0x1
    return v0

    .catch Ljava/lang/RuntimeException; {:try_start .. :try_end} :catch

    :switch_data
    .packed-switch 0x2c
        :case_comma
        :case_minus
        :case_dot
    .end packed-switch
.end method
