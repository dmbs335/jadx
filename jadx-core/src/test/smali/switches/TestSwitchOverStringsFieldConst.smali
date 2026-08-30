.class public Lswitches/TestSwitchOverStringsFieldConst;
.super Ljava/lang/Object;

.field public static final CASE_A:C = 0x12

.method public static test(Ljava/lang/String;)I
	.registers 4

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
	sget-char v0, Lswitches/TestSwitchOverStringsFieldConst;->CASE_A:C
	goto :hash_end

	:case_b_hash
	const-string v1, "b"
	invoke-virtual {p0, v1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
	move-result v1
	if-eqz v1, :hash_end
	const/16 v0, 0x13

	:hash_end
	packed-switch v0, :value_switch
	const/4 v0, -0x1
	return v0

	:case_a
	const/16 v0, 0xa
	return v0

	:case_b
	const/16 v0, 0x14
	return v0

	:hash_switch
	.sparse-switch
		0x61 -> :case_a_hash
		0x62 -> :case_b_hash
	.end sparse-switch

	:value_switch
	.packed-switch 0x12
		:case_a
		:case_b
	.end packed-switch
.end method
