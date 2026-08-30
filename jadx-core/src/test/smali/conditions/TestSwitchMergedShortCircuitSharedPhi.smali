.class public Lconditions/TestSwitchMergedShortCircuitSharedPhi;
.super Ljava/lang/Object;

.method public static match(IIIIZ)I
    .locals 2

    const/4 v0, 0x4

    :loop
    if-lez p4, :out
    if-eqz p1, :failure

    packed-switch p0, :switch_data

    const/16 v0, 0x9
    goto :success

    :case_53
    if-eq p1, p2, :success
    if-eq p1, p3, :success
    goto :failure

    :case_54
    invoke-static {p1}, Lconditions/TestSwitchMergedShortCircuitSharedPhi;->upcase(I)I
    move-result p1
    if-eq p1, p2, :success
    if-eq p1, p3, :success
    goto :failure

    :case_55
    if-eq p1, p2, :success
    invoke-static {p1, p3}, Lconditions/TestSwitchMergedShortCircuitSharedPhi;->classMatcher(II)Z
    move-result v1
    if-nez v1, :success

    :failure
    const/4 v0, 0x0
	add-int/lit8 p0, p0, -0x1
	goto :advance

    :success
	add-int/lit8 v0, v0, 0x1
	add-int/lit8 p0, p0, 0x1

	:advance
	add-int/lit8 p4, p4, -0x1
	goto :loop

	:out
    return v0

    :switch_data
    .packed-switch 0x35
        :case_53
        :case_54
        :case_55
    .end packed-switch
.end method

.method private static classMatcher(II)Z
    .locals 1
    if-ne p0, p1, :not_equal
    const/4 v0, 0x1
    return v0
    :not_equal
    const/4 v0, 0x0
    return v0
.end method

.method private static upcase(I)I
    .locals 0
    return p0
.end method
