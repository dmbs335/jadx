.class public Lswitches/TestSwitchFallThroughLoopSideEffect;
.super Ljava/lang/Object;

.method public static test(Ljava/lang/String;)Ljava/lang/String;
    .registers 6

    new-instance v0, Ljava/lang/StringBuilder;
    const/16 v1, 0x40
    invoke-direct {v0, v1}, Ljava/lang/StringBuilder;-><init>(I)V
    const/16 v1, 0x5e
    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(C)Ljava/lang/StringBuilder;
    const/4 v1, 0x0

    :loop
    invoke-virtual {p0}, Ljava/lang/String;->length()I
    move-result v2
    const/16 v3, 0x24
    if-ge v1, v2, :done
    invoke-virtual {p0, v1}, Ljava/lang/String;->charAt(I)C
    move-result v2
    if-eq v2, v3, :escape
    const/16 v3, 0x2e
    if-eq v2, v3, :escape
    const/16 v4, 0x3f
    if-eq v2, v4, :question
    packed-switch v2, :switch_one
    packed-switch v2, :switch_two
    packed-switch v2, :switch_three
    invoke-virtual {v0, v2}, Ljava/lang/StringBuilder;->append(C)Ljava/lang/StringBuilder;
    goto :next

    :star
    const-string v2, ".*"
    invoke-virtual {v0, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
    goto :next

    :question
    invoke-virtual {v0, v3}, Ljava/lang/StringBuilder;->append(C)Ljava/lang/StringBuilder;
    goto :next

    :escape
    :escape_switch
    const-string v3, "\\"
    invoke-virtual {v0, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
    invoke-virtual {v0, v2}, Ljava/lang/StringBuilder;->append(C)Ljava/lang/StringBuilder;

    :next
    add-int/lit8 v1, v1, 0x1
    goto :loop

    :done
    invoke-virtual {v0, v3}, Ljava/lang/StringBuilder;->append(C)Ljava/lang/StringBuilder;
    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;
    move-result-object p0
    return-object p0

    :switch_one
    .packed-switch 0x28
        :escape_switch
        :escape_switch
        :star
    .end packed-switch

    :switch_two
    .packed-switch 0x5b
        :escape_switch
        :escape_switch
        :escape_switch
        :escape_switch
    .end packed-switch

    :switch_three
    .packed-switch 0x7b
        :escape_switch
        :escape_switch
        :escape_switch
    .end packed-switch
.end method
