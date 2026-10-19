.class public Lconditions/TestSharedBooleanCarrierColorGate;
.super Ljava/lang/Object;

.method public static select(Landroidx/compose/ui/text/SpanStyle;JLandroidx/compose/ui/graphics/Brush;FJLandroidx/compose/ui/text/font/FontWeight;Landroidx/compose/ui/text/font/FontStyle;Landroidx/compose/ui/text/font/FontSynthesis;Landroidx/compose/ui/text/font/FontFamily;Ljava/lang/String;JLandroidx/compose/ui/text/style/BaselineShift;Landroidx/compose/ui/text/style/TextGeometricTransform;Landroidx/compose/ui/text/intl/LocaleList;JLandroidx/compose/ui/text/style/TextDecoration;Landroidx/compose/ui/graphics/Shadow;Landroidx/compose/ui/text/PlatformSpanStyle;Landroidx/compose/ui/graphics/drawscope/DrawStyle;)Z
    .registers 71
    const/16 v23, 0x1
    const/16 v26, 0x0
    const-wide/16 v16, 0x0
    const-wide/16 v24, 0x0
    move-object/from16 v0, p22
    move-object/from16 v10, p14
    move-object/from16 v9, p11
    move-object/from16 v7, p9
    move-object/from16 v3, p3
    move/from16 v4, p4
    move-object/from16 v13, p19
    move-object/from16 v8, p10
    move-object/from16 v5, p7
    move-object/from16 v6, p8
    move-wide/from16 v1, p1
    :goto_4f
    if-nez v3, :cond_79

    move-wide/from16 v14, p1

    .local v14, "$this$isSpecified$iv":J
    const/16 v18, 0x0

    .line 944
    .local v18, "$i$f$isSpecified-8_81llA":I
    cmp-long v19, v14, v16

    if-eqz v19, :cond_5c

    move/from16 v14, v23

    goto :goto_5e

    :cond_5c
    move/from16 v14, v26

    .line 868
    .end local v14    # "$this$isSpecified$iv":J
    .end local v18    # "$i$f$isSpecified-8_81llA":I
    :goto_5e
    if-eqz v14, :cond_79

    invoke-virtual/range {p0 .. p0}, Landroidx/compose/ui/text/SpanStyle;->getTextForegroundStyle$ui_text()Landroidx/compose/ui/text/style/TextForegroundStyle;

    move-result-object v14

    invoke-interface {v14}, Landroidx/compose/ui/text/style/TextForegroundStyle;->getColor-0d7_KjU()J

    move-result-wide v14

    invoke-static {v1, v2, v14, v15}, Landroidx/compose/ui/graphics/Color;->equals-impl0(JJ)Z

    move-result v14

    if-eqz v14, :cond_6f

    goto :goto_79

    :cond_6f
    move-object/from16 v15, p15

    move-wide/from16 v7, p17

    move-object/from16 v14, p20

    move-object/from16 v6, p21

    goto/16 :goto_206

    .line 869
    :cond_79
    :goto_79
    if-eqz v6, :cond_90

    invoke-virtual/range {p0 .. p0}, Landroidx/compose/ui/text/SpanStyle;->getFontStyle-4Lr2A7w()Landroidx/compose/ui/text/font/FontStyle;

    move-result-object v14

    invoke-static {v6, v14}, Lkotlin/jvm/internal/Intrinsics;->areEqual(Ljava/lang/Object;Ljava/lang/Object;)Z

    move-result v14

    if-eqz v14, :cond_86

    goto :goto_90

    :cond_86
    move-object/from16 v15, p15

    move-wide/from16 v7, p17

    move-object/from16 v14, p20

    move-object/from16 v6, p21

    goto/16 :goto_206

    .line 870
    :cond_90
    :goto_90
    if-eqz v5, :cond_a7

    invoke-virtual/range {p0 .. p0}, Landroidx/compose/ui/text/SpanStyle;->getFontWeight()Landroidx/compose/ui/text/font/FontWeight;

    move-result-object v14

    invoke-static {v5, v14}, Lkotlin/jvm/internal/Intrinsics;->areEqual(Ljava/lang/Object;Ljava/lang/Object;)Z

    move-result v14

    if-eqz v14, :cond_9d

    goto :goto_a7

    :cond_9d
    move-object/from16 v15, p15

    move-wide/from16 v7, p17

    move-object/from16 v14, p20

    move-object/from16 v6, p21

    goto/16 :goto_206

    .line 872
    :cond_a7
    :goto_a7
    if-eqz v8, :cond_ba

    invoke-virtual/range {p0 .. p0}, Landroidx/compose/ui/text/SpanStyle;->getFontFamily()Landroidx/compose/ui/text/font/FontFamily;

    move-result-object v14

    if-ne v8, v14, :cond_b0

    goto :goto_ba

    :cond_b0
    move-object/from16 v15, p15

    move-wide/from16 v7, p17

    move-object/from16 v14, p20

    move-object/from16 v6, p21

    goto/16 :goto_206

    .line 873
    :cond_ba
    :goto_ba
    move-wide/from16 v14, p12

    .restart local v14    # "$this$isSpecified$iv":J
    const/16 v18, 0x0

    .line 945
    .local v18, "$i$f$isSpecified--R2X_6o":I
    move-wide/from16 v19, v14

    .restart local v19    # "$this$isUnspecified$iv$iv":J
    const/16 v21, 0x0

    .line 950
    .restart local v21    # "$i$f$isUnspecified--R2X_6o":I
    invoke-static/range {v19 .. v20}, Landroidx/compose/ui/unit/TextUnit;->getRawType-impl(J)J

    move-result-wide v27

    cmp-long v22, v27, v24

    if-nez v22, :cond_cd

    move/from16 v19, v23

    goto :goto_cf

    :cond_cd
    move/from16 v19, v26

    .line 945
    .end local v19    # "$this$isUnspecified$iv$iv":J
    .end local v21    # "$i$f$isUnspecified--R2X_6o":I
    :goto_cf
    nop

    .line 873
    .end local v14    # "$this$isSpecified$iv":J
    .end local v18    # "$i$f$isSpecified--R2X_6o":I
    if-nez v19, :cond_e9

    invoke-virtual/range {p0 .. p0}, Landroidx/compose/ui/text/SpanStyle;->getLetterSpacing-XSAIIZE()J

    move-result-wide v14

    move-wide/from16 v5, p12

    invoke-static {v5, v6, v14, v15}, Landroidx/compose/ui/unit/TextUnit;->equals-impl0(JJ)Z

    move-result v14

    if-eqz v14, :cond_df

    goto :goto_eb

    :cond_df
    move-object/from16 v15, p15

    move-wide/from16 v7, p17

    move-object/from16 v14, p20

    move-object/from16 v6, p21

    goto/16 :goto_206

    :cond_e9
    move-wide/from16 v5, p12

    .line 874
    :goto_eb
    if-eqz v13, :cond_102

    invoke-virtual/range {p0 .. p0}, Landroidx/compose/ui/text/SpanStyle;->getTextDecoration()Landroidx/compose/ui/text/style/TextDecoration;

    move-result-object v14

    invoke-static {v13, v14}, Lkotlin/jvm/internal/Intrinsics;->areEqual(Ljava/lang/Object;Ljava/lang/Object;)Z

    move-result v14

    if-eqz v14, :cond_f8

    goto :goto_102

    :cond_f8
    move-object/from16 v15, p15

    move-wide/from16 v7, p17

    move-object/from16 v14, p20

    move-object/from16 v6, p21

    goto/16 :goto_206

    .line 876
    :cond_102
    :goto_102
    invoke-virtual/range {p0 .. p0}, Landroidx/compose/ui/text/SpanStyle;->getTextForegroundStyle$ui_text()Landroidx/compose/ui/text/style/TextForegroundStyle;

    move-result-object v14

    invoke-interface {v14}, Landroidx/compose/ui/text/style/TextForegroundStyle;->getBrush()Landroidx/compose/ui/graphics/Brush;

    move-result-object v14

    invoke-static {v3, v14}, Lkotlin/jvm/internal/Intrinsics;->areEqual(Ljava/lang/Object;Ljava/lang/Object;)Z

    move-result v14

    if-eqz v14, :cond_1fe

    .line 877
    if-eqz v3, :cond_130

    invoke-virtual/range {p0 .. p0}, Landroidx/compose/ui/text/SpanStyle;->getTextForegroundStyle$ui_text()Landroidx/compose/ui/text/style/TextForegroundStyle;

    move-result-object v14

    invoke-interface {v14}, Landroidx/compose/ui/text/style/TextForegroundStyle;->getAlpha()F

    move-result v14

    cmpg-float v14, v4, v14

    if-nez v14, :cond_121

    move/from16 v14, v23

    goto :goto_123

    :cond_121
    move/from16 v14, v26

    :goto_123
    if-eqz v14, :cond_126

    goto :goto_130

    :cond_126
    move-object/from16 v15, p15

    move-wide/from16 v7, p17

    move-object/from16 v14, p20

    move-object/from16 v6, p21

    goto/16 :goto_206

    .line 878
    :cond_130
    :goto_130
    if-eqz v7, :cond_147

    invoke-virtual/range {p0 .. p0}, Landroidx/compose/ui/text/SpanStyle;->getFontSynthesis-ZQGJjVo()Landroidx/compose/ui/text/font/FontSynthesis;

    move-result-object v14

    invoke-static {v7, v14}, Lkotlin/jvm/internal/Intrinsics;->areEqual(Ljava/lang/Object;Ljava/lang/Object;)Z

    move-result v14

    if-eqz v14, :cond_13d

    goto :goto_147

    :cond_13d
    move-object/from16 v15, p15

    move-wide/from16 v7, p17

    move-object/from16 v14, p20

    move-object/from16 v6, p21

    goto/16 :goto_206

    .line 879
    :cond_147
    :goto_147
    if-eqz v9, :cond_15e

    invoke-virtual/range {p0 .. p0}, Landroidx/compose/ui/text/SpanStyle;->getFontFeatureSettings()Ljava/lang/String;

    move-result-object v14

    invoke-static {v9, v14}, Lkotlin/jvm/internal/Intrinsics;->areEqual(Ljava/lang/Object;Ljava/lang/Object;)Z

    move-result v14

    if-eqz v14, :cond_154

    goto :goto_15e

    :cond_154
    move-object/from16 v15, p15

    move-wide/from16 v7, p17

    move-object/from16 v14, p20

    move-object/from16 v6, p21

    goto/16 :goto_206

    .line 880
    :cond_15e
    :goto_15e
    if-eqz v10, :cond_175

    invoke-virtual/range {p0 .. p0}, Landroidx/compose/ui/text/SpanStyle;->getBaselineShift-5SSeXJ0()Landroidx/compose/ui/text/style/BaselineShift;

    move-result-object v14

    invoke-static {v10, v14}, Lkotlin/jvm/internal/Intrinsics;->areEqual(Ljava/lang/Object;Ljava/lang/Object;)Z

    move-result v14

    if-eqz v14, :cond_16b

    goto :goto_175

    :cond_16b
    move-object/from16 v15, p15

    move-wide/from16 v7, p17

    move-object/from16 v14, p20

    move-object/from16 v6, p21

    goto/16 :goto_206

    .line 881
    :cond_175
    :goto_175
    if-eqz p15, :cond_18c

    .line 882
    invoke-virtual/range {p0 .. p0}, Landroidx/compose/ui/text/SpanStyle;->getTextGeometricTransform()Landroidx/compose/ui/text/style/TextGeometricTransform;

    move-result-object v14

    move-object/from16 v15, p15

    invoke-static {v15, v14}, Lkotlin/jvm/internal/Intrinsics;->areEqual(Ljava/lang/Object;Ljava/lang/Object;)Z

    move-result v14

    if-eqz v14, :cond_184

    goto :goto_18e

    :cond_184
    move-wide/from16 v7, p17

    move-object/from16 v14, p20

    move-object/from16 v6, p21

    goto/16 :goto_206

    .line 881
    :cond_18c
    move-object/from16 v15, p15

    .line 883
    :goto_18e
    if-eqz p16, :cond_1a5

    invoke-virtual/range {p0 .. p0}, Landroidx/compose/ui/text/SpanStyle;->getLocaleList()Landroidx/compose/ui/text/intl/LocaleList;

    move-result-object v14

    move-object/from16 v5, p16

    invoke-static {v5, v14}, Lkotlin/jvm/internal/Intrinsics;->areEqual(Ljava/lang/Object;Ljava/lang/Object;)Z

    move-result v6

    if-eqz v6, :cond_19d

    goto :goto_1a7

    :cond_19d
    move-wide/from16 v7, p17

    move-object/from16 v14, p20

    move-object/from16 v6, p21

    goto/16 :goto_206

    :cond_1a5
    move-object/from16 v5, p16

    .line 884
    :goto_1a7
    move-wide/from16 v18, p17

    .local v18, "$this$isSpecified$iv":J
    const/4 v6, 0x0

    .line 951
    .local v6, "$i$f$isSpecified-8_81llA":I
    cmp-long v14, v18, v16

    if-eqz v14, :cond_1b1

    move/from16 v6, v23

    goto :goto_1b3

    :cond_1b1
    move/from16 v6, v26

    .line 884
    .end local v6    # "$i$f$isSpecified-8_81llA":I
    .end local v18    # "$this$isSpecified$iv":J
    :goto_1b3
    if-eqz v6, :cond_1c7

    invoke-virtual/range {p0 .. p0}, Landroidx/compose/ui/text/SpanStyle;->getBackground-0d7_KjU()J

    move-result-wide v5

    move-wide/from16 v7, p17

    invoke-static {v7, v8, v5, v6}, Landroidx/compose/ui/graphics/Color;->equals-impl0(JJ)Z

    move-result v5

    if-eqz v5, :cond_1c2

    goto :goto_1c9

    :cond_1c2
    move-object/from16 v14, p20

    move-object/from16 v6, p21

    goto :goto_206

    :cond_1c7
    move-wide/from16 v7, p17

    .line 885
    :goto_1c9
    if-eqz p20, :cond_1db

    invoke-virtual/range {p0 .. p0}, Landroidx/compose/ui/text/SpanStyle;->getShadow()Landroidx/compose/ui/graphics/Shadow;

    move-result-object v5

    move-object/from16 v14, p20

    invoke-static {v14, v5}, Lkotlin/jvm/internal/Intrinsics;->areEqual(Ljava/lang/Object;Ljava/lang/Object;)Z

    move-result v5

    if-eqz v5, :cond_1d8

    goto :goto_1dd

    :cond_1d8
    move-object/from16 v6, p21

    goto :goto_206

    :cond_1db
    move-object/from16 v14, p20

    .line 886
    :goto_1dd
    if-eqz p21, :cond_1ec

    invoke-virtual/range {p0 .. p0}, Landroidx/compose/ui/text/SpanStyle;->getPlatformStyle()Landroidx/compose/ui/text/PlatformSpanStyle;

    move-result-object v5

    move-object/from16 v6, p21

    invoke-static {v6, v5}, Lkotlin/jvm/internal/Intrinsics;->areEqual(Ljava/lang/Object;Ljava/lang/Object;)Z

    move-result v5

    if-eqz v5, :cond_206

    goto :goto_1ee

    :cond_1ec
    move-object/from16 v6, p21

    .line 887
    :goto_1ee
    if-eqz v0, :cond_1fb

    invoke-virtual/range {p0 .. p0}, Landroidx/compose/ui/text/SpanStyle;->getDrawStyle()Landroidx/compose/ui/graphics/drawscope/DrawStyle;

    move-result-object v5

    invoke-static {v0, v5}, Lkotlin/jvm/internal/Intrinsics;->areEqual(Ljava/lang/Object;Ljava/lang/Object;)Z

    move-result v5

    if-nez v5, :cond_1fb

    goto :goto_206

    :cond_1fb
    move/from16 v5, v26

    goto :goto_208

    .line 876
    :cond_1fe
    move-object/from16 v15, p15

    move-wide/from16 v7, p17

    move-object/from16 v14, p20

    move-object/from16 v6, p21

    .line 887
    :cond_206
    :goto_206
    move/from16 v5, v23

    .line 866
    :goto_208
    nop

    .line 889
    .local v5, "requiresAlloc":Z
    if-nez v5, :cond_20c

    .line 891
    return v5

    .line 895
    :cond_20c
    return v5
.end method
