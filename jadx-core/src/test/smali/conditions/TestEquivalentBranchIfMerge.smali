.class public Lconditions/TestEquivalentBranchIfMerge;
.super Ljava/lang/Object;

.method public static test(I)Ljava/lang/Object;
    .registers 4

    if-ltz p0, :negative

    invoke-static {p0}, Lconditions/TestEquivalentBranchIfMerge;->prepare(I)Ljava/lang/Object;
    move-result-object v1
    invoke-static {v1}, Lconditions/TestEquivalentBranchIfMerge;->check(Ljava/lang/Object;)V
    invoke-static {p0}, Lconditions/TestEquivalentBranchIfMerge;->first(I)Z
    move-result v0
    if-eqz v0, :blocked
    goto :allowed

    :negative
    invoke-static {p0}, Lconditions/TestEquivalentBranchIfMerge;->second(I)Z
    move-result v0
    if-eqz v0, :blocked

    :allowed
    invoke-static {}, Lconditions/TestEquivalentBranchIfMerge;->update()Ljava/lang/Object;
    move-result-object v0
    invoke-static {}, Lconditions/TestEquivalentBranchIfMerge;->suspended()Ljava/lang/Object;
    move-result-object v1
    if-ne v0, v1, :unit
    return-object v0

    :unit
    sget-object v0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object v0

    :blocked
    :try_start
    invoke-static {}, Lconditions/TestEquivalentBranchIfMerge;->risky()V
    :try_end
    .catch Ljava/lang/Exception; {:try_start .. :try_end} :catch

    sget-object v0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object v0

    :catch
    move-exception v0
    sget-object v0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object v0
.end method

.method private static prepare(I)Ljava/lang/Object;
    .registers 1
    const/4 p0, 0x0
    return-object p0
.end method

.method private static check(Ljava/lang/Object;)V
    .registers 1
    return-void
.end method

.method private static first(I)Z
    .registers 1
    const/4 p0, 0x1
    return p0
.end method

.method private static second(I)Z
    .registers 1
    const/4 p0, 0x0
    return p0
.end method

.method private static risky()V
    .registers 0
    return-void
.end method

.method private static update()Ljava/lang/Object;
    .registers 1
    sget-object v0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object v0
.end method

.method private static suspended()Ljava/lang/Object;
    .registers 1
    new-instance v0, Ljava/lang/Object;
    invoke-direct {v0}, Ljava/lang/Object;-><init>()V
    return-object v0
.end method
