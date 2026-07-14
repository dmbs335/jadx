.class public Lconditions/TestVerifiedCommonPostDominator;
.super Ljava/lang/Object;

.method public static test(ZZLjava/lang/Object;)V
    .registers 4

    if-eqz p0, :not_success

    if-eqz p2, :common_empty
    invoke-static {p2}, Lconditions/TestVerifiedCommonPostDominator;->value(Ljava/lang/Object;)V
    goto :success_join

    :not_success
    if-eqz p1, :common_empty
    invoke-static {}, Lconditions/TestVerifiedCommonPostDominator;->loading()V
    goto :final_join

    :common_empty
    invoke-static {}, Lconditions/TestVerifiedCommonPostDominator;->empty()V

    :success_join
    invoke-static {}, Lconditions/TestVerifiedCommonPostDominator;->successJoin()V

    :final_join
    invoke-static {}, Lconditions/TestVerifiedCommonPostDominator;->tailA()V
    invoke-static {}, Lconditions/TestVerifiedCommonPostDominator;->tailB()V
    invoke-static {}, Lconditions/TestVerifiedCommonPostDominator;->tailC()V
    return-void
.end method

.method private static value(Ljava/lang/Object;)V
    .registers 1
    return-void
.end method

.method private static loading()V
    .registers 0
    return-void
.end method

.method private static empty()V
    .registers 0
    return-void
.end method

.method private static successJoin()V
    .registers 0
    return-void
.end method

.method private static tailA()V
    .registers 0
    return-void
.end method

.method private static tailB()V
    .registers 0
    return-void
.end method

.method private static tailC()V
    .registers 0
    return-void
.end method
