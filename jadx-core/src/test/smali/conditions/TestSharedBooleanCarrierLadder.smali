.class public Lconditions/TestSharedBooleanCarrierLadder;
.super Ljava/lang/Object;

.method public static select(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    .locals 3

    if-eqz p1, :early_1
    move-object/from16 v0, p1
    invoke-static {v0, p0}, Ljava/util/Objects;->equals(Ljava/lang/Object;Ljava/lang/Object;)Z
    move-result v2
    if-eqz v2, :early_1
    if-eqz p2, :early_2
    move-object/from16 v0, p2
    invoke-static {v0, p0}, Ljava/util/Objects;->equals(Ljava/lang/Object;Ljava/lang/Object;)Z
    move-result v2
    if-eqz v2, :early_2
    if-eqz p3, :early_3
    move-object/from16 v0, p3
    invoke-static {v0, p0}, Ljava/util/Objects;->equals(Ljava/lang/Object;Ljava/lang/Object;)Z
    move-result v2
    if-eqz v2, :early_3
    if-eqz p4, :early_4
    move-object/from16 v0, p4
    invoke-static {v0, p0}, Ljava/util/Objects;->equals(Ljava/lang/Object;Ljava/lang/Object;)Z
    move-result v2
    if-eqz v2, :early_4
    if-eqz p5, :early_5
    move-object/from16 v0, p5
    invoke-static {v0, p0}, Ljava/util/Objects;->equals(Ljava/lang/Object;Ljava/lang/Object;)Z
    move-result v2
    if-eqz v2, :early_5
    if-eqz p6, :early_6
    move-object/from16 v0, p6
    invoke-static {v0, p0}, Ljava/util/Objects;->equals(Ljava/lang/Object;Ljava/lang/Object;)Z
    move-result v2
    if-eqz v2, :early_6
    if-eqz p7, :early_7
    move-object/from16 v0, p7
    invoke-static {v0, p0}, Ljava/util/Objects;->equals(Ljava/lang/Object;Ljava/lang/Object;)Z
    move-result v2
    if-eqz v2, :early_7
    if-eqz p8, :early_8
    move-object/from16 v0, p8
    invoke-static {v0, p0}, Ljava/util/Objects;->equals(Ljava/lang/Object;Ljava/lang/Object;)Z
    move-result v2
    if-eqz v2, :early_8
    if-eqz p9, :early_9
    move-object/from16 v0, p9
    invoke-static {v0, p0}, Ljava/util/Objects;->equals(Ljava/lang/Object;Ljava/lang/Object;)Z
    move-result v2
    if-eqz v2, :early_9
    if-eqz p10, :early_10
    move-object/from16 v0, p10
    invoke-static {v0, p0}, Ljava/util/Objects;->equals(Ljava/lang/Object;Ljava/lang/Object;)Z
    move-result v2
    if-eqz v2, :early_10
    if-eqz p11, :early_11
    move-object/from16 v0, p11
    invoke-static {v0, p0}, Ljava/util/Objects;->equals(Ljava/lang/Object;Ljava/lang/Object;)Z
    move-result v2
    if-eqz v2, :early_11
    if-eqz p12, :early_12
    move-object/from16 v0, p12
    invoke-static {v0, p0}, Ljava/util/Objects;->equals(Ljava/lang/Object;Ljava/lang/Object;)Z
    move-result v2
    if-eqz v2, :early_12
    if-eqz p13, :early_13
    move-object/from16 v0, p13
    invoke-static {v0, p0}, Ljava/util/Objects;->equals(Ljava/lang/Object;Ljava/lang/Object;)Z
    move-result v2
    if-eqz v2, :early_13
    if-eqz p14, :early_14
    move-object/from16 v0, p14
    invoke-static {v0, p0}, Ljava/util/Objects;->equals(Ljava/lang/Object;Ljava/lang/Object;)Z
    move-result v2
    if-eqz v2, :early_14
    if-eqz p15, :early_15
    move-object/from16 v0, p15
    invoke-static {v0, p0}, Ljava/util/Objects;->equals(Ljava/lang/Object;Ljava/lang/Object;)Z
    move-result v2
    if-eqz v2, :early_15
    goto :unchanged

    :early_1
    move-object/from16 v1, p16
    goto :changed

    :early_2
    move-object/from16 v1, p16
    goto :changed

    :early_3
    move-object/from16 v1, p16
    goto :changed

    :early_4
    move-object/from16 v1, p16
    goto :changed

    :early_5
    move-object/from16 v1, p16
    goto :changed

    :early_6
    move-object/from16 v1, p16
    goto :changed

    :early_7
    move-object/from16 v1, p16
    goto :changed

    :early_8
    move-object/from16 v1, p16
    goto :changed

    :early_9
    move-object/from16 v1, p16
    goto :changed

    :early_10
    move-object/from16 v1, p16
    goto :changed

    :early_11
    move-object/from16 v1, p16
    goto :changed

    :early_12
    move-object/from16 v1, p16
    goto :changed

    :early_13
    move-object/from16 v1, p16
    goto :changed

    :early_14
    move-object/from16 v1, p16
    goto :changed

    :early_15
    move-object/from16 v1, p16
    goto :changed

    :unchanged
    move-object/from16 v1, p16
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
