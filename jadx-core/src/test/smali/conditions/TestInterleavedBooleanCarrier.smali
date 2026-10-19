.class public Lconditions/TestInterleavedBooleanCarrier;
.super Ljava/lang/Object;

.method public static select(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    .locals 3

    if-eqz p1, :next_1
    invoke-static {p1, p0}, Ljava/util/Objects;->equals(Ljava/lang/Object;Ljava/lang/Object;)Z
    move-result v0
    if-nez v0, :next_1
    :fail_1
    move-object v1, p1
    goto :changed

    :next_1
    if-eqz p2, :next_2
    invoke-static {p2, p0}, Ljava/util/Objects;->equals(Ljava/lang/Object;Ljava/lang/Object;)Z
    move-result v0
    if-nez v0, :next_2
    :fail_2
    move-object v1, p2
    goto :changed

    :next_2
    if-eqz p3, :next_3
    invoke-static {p3, p0}, Ljava/util/Objects;->equals(Ljava/lang/Object;Ljava/lang/Object;)Z
    move-result v0
    if-nez v0, :next_3
    :fail_3
    move-object v1, p3
    goto :changed

    :next_3
    if-eqz p4, :next_4
    invoke-static {p4, p0}, Ljava/util/Objects;->equals(Ljava/lang/Object;Ljava/lang/Object;)Z
    move-result v0
    if-nez v0, :next_4
    :fail_4
    move-object v1, p4
    goto :changed

    :next_4
    if-eqz p5, :next_5
    invoke-static {p5, p0}, Ljava/util/Objects;->equals(Ljava/lang/Object;Ljava/lang/Object;)Z
    move-result v0
    if-nez v0, :next_5
    :fail_5
    move-object v1, p5
    goto :changed

    :next_5
    if-eqz p6, :next_6
    invoke-static {p6, p0}, Ljava/util/Objects;->equals(Ljava/lang/Object;Ljava/lang/Object;)Z
    move-result v0
    if-nez v0, :next_6
    :fail_6
    move-object v1, p6
    goto :changed

    :next_6
    if-eqz p7, :next_7
    invoke-static {p7, p0}, Ljava/util/Objects;->equals(Ljava/lang/Object;Ljava/lang/Object;)Z
    move-result v0
    if-nez v0, :next_7
    :fail_7
    move-object v1, p7
    goto :changed

    :next_7
    if-eqz p8, :unchanged
    invoke-static {p8, p0}, Ljava/util/Objects;->equals(Ljava/lang/Object;Ljava/lang/Object;)Z
    move-result v0
    if-nez v0, :unchanged
    :fail_8
    move-object v1, p8
    goto :changed

    :unchanged
    const/4 v2, 0x0
    goto :join

    :changed
    const/4 v2, 0x1

    :join
    if-eqz v2, :same
    return-object v1

    :same
    return-object p0
.end method
