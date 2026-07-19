.class public Lswitches/TestSwitchFallThroughChainOrder;
.super Ljava/lang/Object;

.method public static test(ILjava/lang/StringBuilder;)Ljava/lang/StringBuilder;
    .registers 3

    sparse-switch p0, :switch_data
    return-object p1

    :case_1
    const-string v0, "1"
    invoke-virtual {p1, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
    goto :case_3

    :case_2
    const-string v0, "2"
    invoke-virtual {p1, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
    return-object p1

    :case_3
    const-string v0, "3"
    invoke-virtual {p1, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
    goto :case_4

    :case_4
    const-string v0, "4"
    invoke-virtual {p1, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
    return-object p1

    :switch_data
    .sparse-switch
        0x1 -> :case_1
        0x2 -> :case_2
        0x3 -> :case_3
        0x4 -> :case_4
    .end sparse-switch
.end method
