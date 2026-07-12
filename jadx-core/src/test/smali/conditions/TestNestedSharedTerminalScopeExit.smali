.class public Lconditions/TestNestedSharedTerminalScopeExit;
.super Ljava/lang/Object;

.method public static select(Ljava/lang/Object;ZZZ)I
    .locals 1

    invoke-static {p0}, Lconditions/TestNestedSharedTerminalScopeExit;->enabled(Ljava/lang/Object;)Z
    move-result v0
    if-nez v0, :access_ready
    const/4 p0, 0x0

    :access_ready
    if-eqz p0, :focus
    invoke-static {}, Lconditions/TestNestedSharedTerminalScopeExit;->touch()V
    if-eqz p1, :focus
    if-eqz p2, :focus
    goto :next

    :focus
    if-eqz p3, :focus_other
    const/4 v0, 0x1
    return v0

    :focus_other
    const/4 v0, 0x4
    return v0

    :next
    if-eqz p3, :other
    const/4 v0, 0x2
    return v0

    :other
    const/4 v0, 0x3
    return v0
.end method

.method public static enabled(Ljava/lang/Object;)Z
    .locals 1

    const/4 v0, 0x1
    return v0
.end method

.method private static touch()V
    .locals 0

    return-void
.end method
