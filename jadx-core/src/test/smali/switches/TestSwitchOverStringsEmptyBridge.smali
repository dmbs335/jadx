.class public Lswitches/TestSwitchOverStringsEmptyBridge;
.super Ljava/lang/Object;

.method private static nullable()Ljava/lang/String;
	.registers 1
	const/4 v0, 0x0
	return-object v0
.end method

.method public static test(Ljava/lang/String;I)Ljava/lang/String;
    .registers 7

	const-string v3, "D"

	:loop
	if-lez p1, :exit
	add-int/lit8 p1, p1, -0x1

    const/4 v0, -0x1

    invoke-virtual {p0}, Ljava/lang/String;->hashCode()I
    move-result v1

    sparse-switch v1, :hash_switch

	:hash_end
	const-string v2, ""
	packed-switch v0, :value_switch

	goto :loop

    :case_a_hash
    const-string v1, "a"
    invoke-virtual {p0, v1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
    move-result v1
    if-eqz v1, :hash_end
    const/4 v0, 0x0
    goto :hash_end

    :case_b_hash
    const-string v1, "b"
    invoke-virtual {p0, v1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
    move-result v1
    if-eqz v1, :hash_end
    const/4 v0, 0x1
    goto :hash_end

    :case_a
	const-string v3, "A"
	goto :loop

    :case_b
	invoke-static {}, Lswitches/TestSwitchOverStringsEmptyBridge;->nullable()Ljava/lang/String;
	move-result-object v4
	if-nez v4, :case_b_value
	move-object v3, v2
	goto :loop

	:case_b_value
	move-object v3, v4
	goto :loop

	:exit
	return-object v3

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
