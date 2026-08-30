.class public Lconditions/TestSharedTerminalDiscriminator;
.super Ljava/lang/Object;

.method public static test([IIZZZ)V
    .registers 9

    aget p1, p0, p1

    const/4 v0, 0x1
    const/4 v1, 0x2
    const/4 v2, 0x3
    const/4 v3, 0x4

    if-eq p1, v0, :case_one
    if-eq p1, v1, :case_two
    if-eq p1, v2, :case_three
    if-ne p1, v3, :invalid
    goto :terminal

    :case_one
    if-eqz p2, :terminal
    goto :body

    :case_two
    if-eqz p3, :terminal
    goto :body

    :case_three
    if-eqz p4, :terminal
    goto :body

    :invalid
    new-instance v0, Ljava/lang/IllegalStateException;
    invoke-direct {v0}, Ljava/lang/IllegalStateException;-><init>()V
    throw v0

    :terminal
    invoke-static {}, Lconditions/TestSharedTerminalDiscriminator;->cleanup()V
    return-void

    :body
    invoke-static {}, Lconditions/TestSharedTerminalDiscriminator;->body()V
    return-void
.end method

.method private static cleanup()V
    .registers 0
    return-void
.end method

.method private static body()V
    .registers 0
    return-void
.end method
