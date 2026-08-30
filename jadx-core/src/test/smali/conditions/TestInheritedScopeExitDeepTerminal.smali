.class public final Lconditions/TestInheritedScopeExitDeepTerminal;
.super Ljava/lang/Object;

.method public static touch()V
    .registers 0

    return-void
.end method

.method public static test(III)Z
    .registers 5

    if-lez p0, :outside
    const/16 v0, 0x64
    if-ge p0, v0, :outside

    invoke-static {}, Lconditions/TestInheritedScopeExitDeepTerminal;->touch()V

    if-ltz p1, :outside
    if-ge p1, v0, :outside

    if-eqz p2, :inside
    add-int/lit8 p2, p2, -0x1
    if-eqz p2, :inside
    add-int/lit8 p2, p2, -0x1
    if-eqz p2, :inside
    add-int/lit8 p2, p2, -0x1
    if-eqz p2, :inside
    add-int/lit8 p2, p2, -0x1
    if-eqz p2, :inside
    add-int/lit8 p2, p2, -0x1
    if-eqz p2, :inside
    add-int/lit8 p2, p2, -0x1
    if-eqz p2, :inside
    add-int/lit8 p2, p2, -0x1
    if-eqz p2, :inside
    add-int/lit8 p2, p2, -0x1
    if-eqz p2, :inside
    add-int/lit8 p2, p2, -0x1
    if-eqz p2, :inside
    add-int/lit8 p2, p2, -0x1
    if-eqz p2, :inside
    add-int/lit8 p2, p2, -0x1
    if-eqz p2, :inside
    add-int/lit8 p2, p2, -0x1
    if-eqz p2, :inside
    add-int/lit8 p2, p2, -0x1
    if-eqz p2, :inside
    add-int/lit8 p2, p2, -0x1

    :inside
    const/4 v0, 0x1
    return v0

    :outside
    const/4 v0, 0x0
    return v0
.end method
