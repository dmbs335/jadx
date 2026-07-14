.class public Lconditions/TestBranchStartJoinWithTerminalPath;
.super Ljava/lang/Object;

.method public static test(ILjava/lang/Object;Ljava/lang/Object;)V
    .registers 6

    and-int/lit8 v1, p0, 0x1
    if-eqz v1, :first_default
    move-object v0, p1
    goto :first_join
    :first_default
    if-eqz p1, :first_fallback
    invoke-static {p1}, Lconditions/TestBranchStartJoinWithTerminalPath;->firstDefault(Ljava/lang/Object;)V
    move-object v0, p1
    goto :first_join
    :first_fallback
    if-nez p2, :first_use_fallback
    new-instance v2, Ljava/lang/IllegalStateException;
    invoke-direct {v2}, Ljava/lang/IllegalStateException;-><init>()V
    throw v2
    :first_use_fallback
    invoke-static {p2}, Lconditions/TestBranchStartJoinWithTerminalPath;->firstDefault(Ljava/lang/Object;)V
    move-object v0, p2
    :first_join

    and-int/lit8 v1, p0, 0x2
    if-eqz v1, :second_default
    move-object v0, p1
    goto :second_join
    :second_default
    if-eqz p1, :second_fallback
    invoke-static {p1}, Lconditions/TestBranchStartJoinWithTerminalPath;->secondDefault(Ljava/lang/Object;)V
    move-object v0, p1
    goto :second_join
    :second_fallback
    if-nez p2, :second_use_fallback
    new-instance v2, Ljava/lang/IllegalStateException;
    invoke-direct {v2}, Ljava/lang/IllegalStateException;-><init>()V
    throw v2
    :second_use_fallback
    invoke-static {p2}, Lconditions/TestBranchStartJoinWithTerminalPath;->secondDefault(Ljava/lang/Object;)V
    move-object v0, p2
    :second_join

    and-int/lit8 v1, p0, 0x4
    if-eqz v1, :third_default
    move-object v0, p1
    goto :third_join
    :third_default
    if-eqz p1, :third_fallback
    invoke-static {p1}, Lconditions/TestBranchStartJoinWithTerminalPath;->thirdDefault(Ljava/lang/Object;)V
    move-object v0, p1
    goto :third_join
    :third_fallback
    if-nez p2, :third_use_fallback
    new-instance v2, Ljava/lang/IllegalStateException;
    invoke-direct {v2}, Ljava/lang/IllegalStateException;-><init>()V
    throw v2
    :third_use_fallback
    invoke-static {p2}, Lconditions/TestBranchStartJoinWithTerminalPath;->thirdDefault(Ljava/lang/Object;)V
    move-object v0, p2
    :third_join

    and-int/lit8 v1, p0, 0x8
    if-eqz v1, :fourth_default
    move-object v0, p1
    goto :fourth_join
    :fourth_default
    if-eqz p1, :fourth_fallback
    invoke-static {p1}, Lconditions/TestBranchStartJoinWithTerminalPath;->fourthDefault(Ljava/lang/Object;)V
    move-object v0, p1
    goto :fourth_join
    :fourth_fallback
    if-nez p2, :fourth_use_fallback
    new-instance v2, Ljava/lang/IllegalStateException;
    invoke-direct {v2}, Ljava/lang/IllegalStateException;-><init>()V
    throw v2
    :fourth_use_fallback
    invoke-static {p2}, Lconditions/TestBranchStartJoinWithTerminalPath;->fourthDefault(Ljava/lang/Object;)V
    move-object v0, p2
    :fourth_join

    invoke-static {v0}, Lconditions/TestBranchStartJoinWithTerminalPath;->tail(Ljava/lang/Object;)V
    return-void
.end method

.method private static firstDefault(Ljava/lang/Object;)V
    .registers 1
    return-void
.end method

.method private static secondDefault(Ljava/lang/Object;)V
    .registers 1
    return-void
.end method

.method private static thirdDefault(Ljava/lang/Object;)V
    .registers 1
    return-void
.end method

.method private static fourthDefault(Ljava/lang/Object;)V
    .registers 1
    return-void
.end method

.method private static tail(Ljava/lang/Object;)V
    .registers 1
    return-void
.end method
