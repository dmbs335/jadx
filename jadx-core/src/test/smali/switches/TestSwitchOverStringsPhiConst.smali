.class public Lswitches/TestSwitchOverStringsPhiConst;
.super Ljava/lang/Object;

.method public static test(Ljava/lang/String;I)I
	.registers 7

	const/4 v2, 0x0
	const/4 v4, -0x1

	:loop
	if-lez p1, :exit
	add-int/lit8 p1, p1, -0x1
	const/4 v0, -0x1
	invoke-virtual {p0}, Ljava/lang/String;->hashCode()I
	move-result v1
	sparse-switch v1, :hash_switch
	goto :hash_end

	:case_a_hash
	const-string v1, "a"
	invoke-virtual {p0, v1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
	move-result v1
	if-eqz v1, :hash_end
	move v0, v2
	goto :hash_end

	:case_b_hash
	const-string v1, "b"
	invoke-virtual {p0, v1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
	move-result v1
	if-eqz v1, :hash_end
	const/4 v0, 0x1

	:hash_end
	packed-switch v0, :value_switch
	const/4 v4, -0x1
	goto :back_edge

	:case_a
	const/16 v4, 0xa
	goto :back_edge

	:case_b
	const/16 v4, 0x14

	:back_edge
	const/4 v3, 0x0
	move v2, v3
	goto :loop

	:exit
	return v4

	:hash_switch
	.sparse-switch
		0x61 -> :case_a_hash
		0x62 -> :case_b_hash
	.end sparse-switch

	:value_switch
	.packed-switch 0x0
		:case_a
		:case_b
	.end packed-switch
.end method
