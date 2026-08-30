.class public Linline/TestTernaryInlineSkip;
.super Ljava/lang/Object;

.method public static callTwice(I)I
    .registers 3
    invoke-static {p0}, Linline/TestTernaryInlineSkip;->wrapper(I)I
    move-result v0
    invoke-static {p0}, Linline/TestTernaryInlineSkip;->wrapper(I)I
    move-result v1
    add-int/2addr v0, v1
    return v0
.end method

.method public static synthetic wrapper(I)I
    .registers 2
    if-eqz p0, :false_value
    const/4 v0, 0x1
    goto :invoke
    :false_value
    const/4 v0, 0x0
    :invoke
    invoke-static {v0}, Linline/TestTernaryInlineSkip;->target(Z)I
    move-result v0
    return v0
.end method

.method private static target(Z)I
    .registers 1
    if-eqz p0, :zero
    const/4 p0, 0x1
    return p0
    :zero
    const/4 p0, 0x0
    return p0
.end method
