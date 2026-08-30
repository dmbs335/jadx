.class public final Lconditions/TestSharedGuardFailureLadder;
.super Ljava/lang/Object;

.method public static test(Ljava/lang/String;)Ljava/lang/String;
    .registers 5

    const-string v0, ""
    if-eqz p0, :second_case

    invoke-virtual {p0}, Ljava/lang/String;->trim()Ljava/lang/String;
    move-result-object v1
    if-eqz v1, :first_null
    invoke-virtual {v1}, Ljava/lang/String;->toString()Ljava/lang/String;
    move-result-object v1
    goto :first_join

    :first_null
    const/4 v1, 0x0

    :first_join
    if-nez v1, :first_value
    move-object v1, v0

    :first_value
    const-string v2, "A"
    invoke-static {v1, v2}, Ljava/util/Objects;->equals(Ljava/lang/Object;Ljava/lang/Object;)Z
    move-result v3
    if-eqz v3, :second_case

    invoke-virtual {p0}, Ljava/lang/String;->trim()Ljava/lang/String;
    move-result-object v1
    if-eqz v1, :status_null
    invoke-virtual {v1}, Ljava/lang/String;->intern()Ljava/lang/String;
    move-result-object v1
    goto :status_join

    :status_null
    const/4 v1, 0x0

    :status_join
    if-nez v1, :status_value
    move-object v1, v0

    :status_value
    const-string v2, "READY"
    invoke-static {v1, v2}, Ljava/util/Objects;->equals(Ljava/lang/Object;Ljava/lang/Object;)Z
    move-result v3
    if-eqz v3, :second_case

    invoke-virtual {p0}, Ljava/lang/String;->isEmpty()Z
    move-result v1
    if-nez v1, :second_case

    const-string v0, "FIRST"
    goto :tail

    :second_case
    if-eqz p0, :tail
    invoke-virtual {p0}, Ljava/lang/String;->length()I
    move-result v1
    if-lez v1, :tail
    const-string v0, "SECOND"

    :tail
    return-object v0
.end method
