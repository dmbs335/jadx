.class public Lcoroutines/TestKotlinSpilling;
.super Ljava/lang/Object;

.method public static spilling(Z)Ljava/lang/Object;
    .locals 1

    if-eqz p0, :call
    const-string v0, "value"
  :call
    invoke-static {v0}, Lkotlin/coroutines/jvm/internal/SpillingKt;->nullOutSpilledVariable(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v0
    return-object v0
.end method

.method public static regular(Z)Ljava/lang/Object;
    .locals 1

    if-eqz p0, :call
    const-string v0, "value"
  :call
    invoke-static {v0}, Lcoroutines/TestKotlinSpilling;->identity(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v0
    return-object v0
.end method

.method private static identity(Ljava/lang/Object;)Ljava/lang/Object;
    .locals 0
    return-object p0
.end method
