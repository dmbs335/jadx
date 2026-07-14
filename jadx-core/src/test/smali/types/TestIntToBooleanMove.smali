.class public Ltypes/TestIntToBooleanMove;
.super Ljava/lang/Object;

.method public final a(Ljava/lang/String;Ljava/lang/String;)Z
    .registers 20

    invoke-virtual/range {p1 .. p1}, Ljava/lang/String;->toCharArray()[C

    move-result-object v0

    invoke-virtual/range {p2 .. p2}, Ljava/lang/String;->toCharArray()[C

    move-result-object v1

    array-length v2, v0

    const/4 v3, 0x1

    sub-int/2addr v2, v3

    array-length v4, v1

    sub-int/2addr v4, v3

    array-length v5, v0

    const/4 v6, 0x0

    move v7, v6

    :goto_10
    const/16 v8, 0x3f

    if-ge v7, v5, :cond_d2

    aget-char v9, v0, v7

    const/16 v10, 0x2a

    if-ne v9, v10, :cond_ca

    if-nez v2, :cond_1d

    return v3

    :cond_1d
    move v5, v6

    move v7, v5

    :goto_1f
    aget-char v9, v0, v5

    if-eq v9, v10, :cond_31

    if-gt v7, v4, :cond_31

    if-eq v9, v8, :cond_2c

    aget-char v11, v1, v7

    if-eq v9, v11, :cond_2c

    return v6

    :cond_2c
    add-int/lit8 v5, v5, 0x1

    add-int/lit8 v7, v7, 0x1

    goto :goto_1f

    :cond_31
    if-le v7, v4, :cond_3e

    :goto_33
    if-gt v5, v2, :cond_3d

    aget-char v1, v0, v5

    if-eq v1, v10, :cond_3a

    return v6

    :cond_3a
    add-int/lit8 v5, v5, 0x1

    goto :goto_33

    :cond_3d
    return v3

    :cond_3e
    :goto_3e
    aget-char v9, v0, v2

    if-eq v9, v10, :cond_50

    if-gt v7, v4, :cond_50

    if-eq v9, v8, :cond_4b

    aget-char v11, v1, v4

    if-eq v9, v11, :cond_4b

    return v6

    :cond_4b
    add-int/lit8 v2, v2, -0x1

    add-int/lit8 v4, v4, -0x1

    goto :goto_3e

    :cond_50
    if-le v7, v4, :cond_5d

    :goto_52
    if-gt v5, v2, :cond_5c

    aget-char v1, v0, v5

    if-eq v1, v10, :cond_59

    return v6

    :cond_59
    add-int/lit8 v5, v5, 0x1

    goto :goto_52

    :cond_5c
    return v3

    :cond_5d
    :goto_5d
    if-eq v5, v2, :cond_bb

    if-gt v7, v4, :cond_bb

    add-int/lit8 v9, v5, 0x1

    move v11, v9

    :goto_64
    const/4 v12, -0x1

    if-gt v11, v2, :cond_6f

    aget-char v13, v0, v11

    if-ne v13, v10, :cond_6c

    goto :goto_70

    :cond_6c
    add-int/lit8 v11, v11, 0x1

    goto :goto_64

    :cond_6f
    move v11, v12

    :goto_70
    if-ne v11, v9, :cond_74

    move v5, v9

    goto :goto_5d

    :cond_74
    sub-int v9, v11, v5

    sub-int/2addr v9, v3

    sub-int v13, v4, v7

    add-int/2addr v13, v3

    move v14, v6

    :goto_7b
    sub-int v15, v13, v9

    if-gt v14, v15, :cond_ac

    move v15, v6

    :goto_80
    if-ge v15, v9, :cond_a6

    add-int v16, v5, v15

    add-int/lit8 v16, v16, 0x1

    move/from16 p1, v3

    aget-char v3, v0, v16

    if-eq v3, v8, :cond_9d

    add-int v16, v7, v14

    add-int v16, v16, v15

    move/from16 p2, v6

    aget-char v6, v1, v16

    if-eq v3, v6, :cond_9f

    add-int/lit8 v14, v14, 0x1

    move/from16 v3, p1

    move/from16 v6, p2

    goto :goto_7b

    :cond_9d
    move/from16 p2, v6

    :cond_9f
    add-int/lit8 v15, v15, 0x1

    move/from16 v3, p1

    move/from16 v6, p2

    goto :goto_80

    :cond_a6
    move/from16 p1, v3

    move/from16 p2, v6

    add-int/2addr v7, v14

    goto :goto_b1

    :cond_ac
    move/from16 p1, v3

    move/from16 p2, v6

    move v7, v12

    :goto_b1
    if-ne v7, v12, :cond_b4

    return p2

    :cond_b4
    add-int/2addr v7, v9

    move/from16 v3, p1

    move/from16 v6, p2

    move v5, v11

    goto :goto_5d

    :cond_bb
    move/from16 p1, v3

    move/from16 p2, v6

    :goto_bf
    if-gt v5, v2, :cond_c9

    aget-char v1, v0, v5

    if-eq v1, v10, :cond_c6

    return p2

    :cond_c6
    add-int/lit8 v5, v5, 0x1

    goto :goto_bf

    :cond_c9
    return p1

    :cond_ca
    move/from16 p1, v3

    move/from16 p2, v6

    add-int/lit8 v7, v7, 0x1

    goto/16 :goto_10

    :cond_d2
    move/from16 p1, v3

    move/from16 p2, v6

    if-eq v2, v4, :cond_d9

    return p2

    :cond_d9
    move/from16 v3, p2

    :goto_db
    if-gt v3, v2, :cond_e9

    aget-char v4, v0, v3

    if-eq v4, v8, :cond_e6

    aget-char v5, v1, v3

    if-eq v4, v5, :cond_e6

    return p2

    :cond_e6
    add-int/lit8 v3, v3, 0x1

    goto :goto_db

    :cond_e9
    return p1
.end method

