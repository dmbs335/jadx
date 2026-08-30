.class public Lloops/TestEndlessGuardedScanLoop;
.super Ljava/lang/Object;

.field private pos:I

.method public scan(Ljava/lang/String;IZZ)I
	.locals 8

    const/4 v0, 0x0
	if-nez p3, :done

	iget v1, p0, Lloops/TestEndlessGuardedScanLoop;->pos:I
	move v7, v1

    :scan_loop
    iget v1, p0, Lloops/TestEndlessGuardedScanLoop;->pos:I
    if-ge v1, p2, :finish
    invoke-virtual {p1, v1}, Ljava/lang/String;->charAt(I)C
    move-result v2
    const/16 v3, 0x30
    if-lt v2, v3, :finish
    const/16 v3, 0x39
    if-gt v2, v3, :finish
	int-to-long v3, v0
	const-wide v5, 0xcccccccccccccccL
	cmp-long v3, v3, v5
	if-lez v3, :accumulate
	const/4 v3, -0x1
	return v3

    :accumulate
    mul-int/lit8 v0, v0, 0xa
    add-int/lit8 v2, v2, -0x30
    add-int/2addr v0, v2
    add-int/lit8 v1, v1, 0x1
    iput v1, p0, Lloops/TestEndlessGuardedScanLoop;->pos:I
    goto :scan_loop

	:finish
	iget v1, p0, Lloops/TestEndlessGuardedScanLoop;->pos:I
	if-ne v1, v7, :apply_sign
	const/4 v1, -0x1
	return v1

	:apply_sign
	if-eqz p4, :done
	neg-int v0, v0

	:done
    return v0
.end method
