.class public Lconditions/TestSharedReadOnlyReturn;
.super Ljava/lang/Object;

.field public label:I

.method public test(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 6

    invoke-static {}, Lconditions/TestSharedReadOnlyReturn;->suspended()Ljava/lang/Object;
    move-result-object v0

    iget v1, p0, Lconditions/TestSharedReadOnlyReturn;->label:I
    const/4 v2, 0x2
    const/4 v3, 0x1

    if-eqz v1, :initial_state
    if-eq v1, v3, :resume_one
    if-ne v1, v2, :invalid_state

	invoke-static {p1}, Lconditions/TestSharedReadOnlyReturn;->failure(Ljava/lang/Object;)V
    goto :shared_return

	:invalid_state
	new-instance p1, Ljava/lang/IllegalStateException;
	invoke-direct {p1}, Ljava/lang/IllegalStateException;-><init>()V
	throw p1

	:resume_one
	invoke-static {p1}, Lconditions/TestSharedReadOnlyReturn;->failure(Ljava/lang/Object;)V
	goto :second_call

	:initial_state
	invoke-static {p1}, Lconditions/TestSharedReadOnlyReturn;->failure(Ljava/lang/Object;)V
	iput v3, p0, Lconditions/TestSharedReadOnlyReturn;->label:I
	invoke-static {}, Lconditions/TestSharedReadOnlyReturn;->first()Ljava/lang/Object;
	move-result-object p1
	if-ne p1, v0, :second_call
	goto :suspended_return

	:second_call
	iput v2, p0, Lconditions/TestSharedReadOnlyReturn;->label:I
	invoke-static {}, Lconditions/TestSharedReadOnlyReturn;->second()Ljava/lang/Object;
	move-result-object p1
	if-ne p1, v0, :shared_return

	:suspended_return
	return-object v0

    :shared_return
	sget-object p1, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
	return-object p1
.end method

.method private static suspended()Ljava/lang/Object;
    .registers 1
    const/4 v0, 0x0
    return-object v0
.end method

.method private static first()Ljava/lang/Object;
    .registers 1
    const/4 v0, 0x0
    return-object v0
.end method

.method private static second()Ljava/lang/Object;
    .registers 1
    const/4 v0, 0x0
    return-object v0
.end method

.method private static failure(Ljava/lang/Object;)V
    .registers 1
    return-void
.end method
