.class public Lloops/TestInheritedScopeLoopBreak;
.super Ljava/lang/Object;

.method private static consume(I)V
    .registers 1
    return-void
.end method

.method public static test(II)I
	.registers 4

    const/4 v0, 0x0

    :loop
    const/4 v1, 0x3
    if-ge v0, v1, :exit

    if-eqz p0, :continuation
    invoke-static {p0}, Lloops/TestInheritedScopeLoopBreak;->consume(I)V
    const/16 v1, 0xa
    if-gt p1, v1, :exit

    :continuation
    add-int/lit8 v0, v0, 0x1
    goto :loop

	:exit
	const/4 v1, 0x0

	:tail_loop
	if-ge v1, p1, :return
	add-int/lit8 v1, v1, 0x1
	goto :tail_loop

	:return
	add-int/2addr v0, v1
	return v0
.end method
