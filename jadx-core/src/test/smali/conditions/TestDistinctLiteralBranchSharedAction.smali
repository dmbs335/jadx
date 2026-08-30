.class public Lconditions/TestDistinctLiteralBranchSharedAction;
.super Ljava/lang/Object;

.method public static test(ZIZZZ)Z
    .registers 7

	const/4 v0, 0x0
	if-eqz p0, :return_false
    const/4 v0, 0x1
	if-eqz p1, :initial
	if-eq p1, v0, :shared
	const/4 v1, 0x2
	if-eq p1, v1, :move
	const/4 v1, 0x3
	if-eq p1, v1, :shared
    goto :out

    :shared
    invoke-static {}, Lconditions/TestDistinctLiteralBranchSharedAction;->sharedAction()V
	goto :out

	:initial
	invoke-static {}, Lconditions/TestDistinctLiteralBranchSharedAction;->initialAction()V

	:move
	invoke-static {}, Lconditions/TestDistinctLiteralBranchSharedAction;->moveAction()V
	if-nez p3, :out
	if-eqz p4, :out
	invoke-static {}, Lconditions/TestDistinctLiteralBranchSharedAction;->startAction()V

    :out
	if-eqz p2, :return_false
	if-eqz p3, :return_false
	return v0

	:return_false
	const/4 v0, 0x0
	return v0
.end method

.method private static sharedAction()V
    .registers 0
    return-void
.end method

.method private static initialAction()V
    .registers 0
    return-void
.end method

.method private static moveAction()V
    .registers 0
    return-void
.end method

.method private static startAction()V
    .registers 0
    return-void
.end method
