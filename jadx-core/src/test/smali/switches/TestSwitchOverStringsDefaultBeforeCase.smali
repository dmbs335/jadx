.class public Lswitches/TestSwitchOverStringsDefaultBeforeCase;
.super Ljava/lang/Object;

.method public static test(Ljava/lang/String;)I
	.registers 4

	const/4 v0, -0x1
	invoke-virtual {p0}, Ljava/lang/String;->hashCode()I
	move-result v1
	sparse-switch v1, :hash_switch
	goto :hash_end

	:hash_a
	const-string v1, "a"
	invoke-virtual {p0, v1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
	move-result v1
	if-eqz v1, :hash_end
	const/4 v0, 0x0
	goto :hash_end

	:hash_b
	const-string v1, "b"
	invoke-virtual {p0, v1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
	move-result v1
	if-eqz v1, :hash_end
	const/4 v0, 0x1
	goto :hash_end

	:hash_c
	const-string v1, "c"
	invoke-virtual {p0, v1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
	move-result v1
	if-eqz v1, :hash_end
	const/4 v0, 0x2
	goto :hash_end

	:hash_d
	const-string v1, "d"
	invoke-virtual {p0, v1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
	move-result v1
	if-eqz v1, :hash_end
	const/4 v0, 0x3

	:hash_end
	packed-switch v0, :value_switch

	sget-object v1, Ljava/lang/System;->out:Ljava/io/PrintStream;
	const-string v2, "unknown"
	invoke-virtual {v1, v2}, Ljava/io/PrintStream;->println(Ljava/lang/String;)V

	:return_one
	const/4 v0, 0x1
	return v0

	:return_two
	const/4 v0, 0x2
	return v0

	:hash_switch
	.sparse-switch
		0x61 -> :hash_a
		0x62 -> :hash_b
		0x63 -> :hash_c
		0x64 -> :hash_d
	.end sparse-switch

	:value_switch
	.packed-switch 0x0
		:return_two
		:return_two
		:return_one
		:return_one
	.end packed-switch
.end method
