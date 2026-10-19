# Method-only regression fixture from AndroidX Compose ui-text (Apache-2.0).
# The class descriptor is changed; no application-specific code is included.
.class public Lconditions/TestSharedBooleanCarrierFromDex;
.super Ljava/lang/Object;

.method public static final fastMerge-dSHsh3o(Landroidx/compose/ui/text/SpanStyle;JLandroidx/compose/ui/graphics/Brush;FJLandroidx/compose/ui/text/font/FontWeight;Landroidx/compose/ui/text/font/FontStyle;Landroidx/compose/ui/text/font/FontSynthesis;Landroidx/compose/ui/text/font/FontFamily;Ljava/lang/String;JLandroidx/compose/ui/text/style/BaselineShift;Landroidx/compose/ui/text/style/TextGeometricTransform;Landroidx/compose/ui/text/intl/LocaleList;JLandroidx/compose/ui/text/style/TextDecoration;Landroidx/compose/ui/graphics/Shadow;Landroidx/compose/ui/text/PlatformSpanStyle;Landroidx/compose/ui/graphics/drawscope/DrawStyle;)Landroidx/compose/ui/text/SpanStyle;
    .registers 71
    .param p0, "$this$fastMerge_u2ddSHsh3o"    # Landroidx/compose/ui/text/SpanStyle;
    .param p1, "color"    # J
    .param p3, "brush"    # Landroidx/compose/ui/graphics/Brush;
    .param p4, "alpha"    # F
    .param p5, "fontSize"    # J
    .param p7, "fontWeight"    # Landroidx/compose/ui/text/font/FontWeight;
    .param p8, "fontStyle"    # Landroidx/compose/ui/text/font/FontStyle;
    .param p9, "fontSynthesis"    # Landroidx/compose/ui/text/font/FontSynthesis;
    .param p10, "fontFamily"    # Landroidx/compose/ui/text/font/FontFamily;
    .param p11, "fontFeatureSettings"    # Ljava/lang/String;
    .param p12, "letterSpacing"    # J
    .param p14, "baselineShift"    # Landroidx/compose/ui/text/style/BaselineShift;
    .param p15, "textGeometricTransform"    # Landroidx/compose/ui/text/style/TextGeometricTransform;
    .param p16, "localeList"    # Landroidx/compose/ui/text/intl/LocaleList;
    .param p17, "background"    # J
    .param p19, "textDecoration"    # Landroidx/compose/ui/text/style/TextDecoration;
    .param p20, "shadow"    # Landroidx/compose/ui/graphics/Shadow;
    .param p21, "platformStyle"    # Landroidx/compose/ui/text/PlatformSpanStyle;
    .param p22, "drawStyle"    # Landroidx/compose/ui/graphics/drawscope/DrawStyle;

    .line 867
    move-wide/from16 v1, p1

    move-object/from16 v3, p3

    move/from16 v4, p4

    move-object/from16 v5, p7

    move-object/from16 v6, p8

    move-object/from16 v7, p9

    move-object/from16 v8, p10

    move-object/from16 v9, p11

    move-object/from16 v10, p14

    move-object/from16 v13, p19

    move-object/from16 v0, p22

    move-wide/from16 v16, p5

    .local v16, "$this$isSpecified$iv":J
    const/16 v18, 0x0

    .line 938
    .local v18, "$i$f$isSpecified--R2X_6o":I
    move-wide/from16 v19, v16

    .local v19, "$this$isUnspecified$iv$iv":J
    const/16 v21, 0x0

    .line 943
    .local v21, "$i$f$isUnspecified--R2X_6o":I
    invoke-static/range {v19 .. v20}, Landroidx/compose/ui/unit/TextUnit;->getRawType-impl(J)J

    move-result-wide v22

    const-wide/16 v24, 0x0

    cmp-long v22, v22, v24

    const/16 v23, 0x1

    const/16 v26, 0x0

    if-nez v22, :cond_2f

    move/from16 v19, v23

    goto :goto_31

    :cond_2f
    move/from16 v19, v26

    .line 938
    .end local v19    # "$this$isUnspecified$iv$iv":J
    .end local v21    # "$i$f$isUnspecified--R2X_6o":I
    :goto_31
    nop

    .line 867
    .end local v16    # "$this$isSpecified$iv":J
    .end local v18    # "$i$f$isSpecified--R2X_6o":I
    const-wide/16 v16, 0x10

    if-nez v19, :cond_4d

    invoke-virtual/range {p0 .. p0}, Landroidx/compose/ui/text/SpanStyle;->getFontSize-XSAIIZE()J

    move-result-wide v14

    move-wide/from16 v11, p5

    invoke-static {v11, v12, v14, v15}, Landroidx/compose/ui/unit/TextUnit;->equals-impl0(JJ)Z

    move-result v14

    if-eqz v14, :cond_43

    goto :goto_4f

    :cond_43
    move-object/from16 v15, p15

    move-wide/from16 v7, p17

    move-object/from16 v14, p20

    move-object/from16 v6, p21

    goto/16 :goto_206

    :cond_4d
    move-wide/from16 v11, p5

    .line 868
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
    return-object p0

    .line 895
    :cond_20c
    if-eqz v3, :cond_215

    .line 896
    sget-object v0, Landroidx/compose/ui/text/style/TextForegroundStyle;->Companion:Landroidx/compose/ui/text/style/TextForegroundStyle$Companion;

    invoke-virtual {v0, v3, v4}, Landroidx/compose/ui/text/style/TextForegroundStyle$Companion;->from(Landroidx/compose/ui/graphics/Brush;F)Landroidx/compose/ui/text/style/TextForegroundStyle;

    move-result-object v0

    goto :goto_21b

    .line 898
    :cond_215
    sget-object v0, Landroidx/compose/ui/text/style/TextForegroundStyle;->Companion:Landroidx/compose/ui/text/style/TextForegroundStyle$Companion;

    invoke-virtual {v0, v1, v2}, Landroidx/compose/ui/text/style/TextForegroundStyle$Companion;->from-8_81llA(J)Landroidx/compose/ui/text/style/TextForegroundStyle;

    move-result-object v0

    .line 895
    :goto_21b
    nop

    .line 894
    nop

    .line 902
    .local v0, "otherTextForegroundStyle":Landroidx/compose/ui/text/style/TextForegroundStyle;
    invoke-virtual/range {p0 .. p0}, Landroidx/compose/ui/text/SpanStyle;->getTextForegroundStyle$ui_text()Landroidx/compose/ui/text/style/TextForegroundStyle;

    move-result-object v1

    invoke-interface {v1, v0}, Landroidx/compose/ui/text/style/TextForegroundStyle;->merge(Landroidx/compose/ui/text/style/TextForegroundStyle;)Landroidx/compose/ui/text/style/TextForegroundStyle;

    move-result-object v28

    .line 903
    if-nez p10, :cond_22e

    invoke-virtual/range {p0 .. p0}, Landroidx/compose/ui/text/SpanStyle;->getFontFamily()Landroidx/compose/ui/text/font/FontFamily;

    move-result-object v1

    move-object/from16 v34, v1

    goto :goto_230

    :cond_22e
    move-object/from16 v34, p10

    .line 904
    :goto_230
    move-wide/from16 v1, p5

    .local v1, "$this$isUnspecified$iv":J
    const/16 v18, 0x0

    .line 952
    .local v18, "$i$f$isUnspecified--R2X_6o":I
    invoke-static {v1, v2}, Landroidx/compose/ui/unit/TextUnit;->getRawType-impl(J)J

    move-result-wide v19

    cmp-long v19, v19, v24

    if-nez v19, :cond_23f

    move/from16 v1, v23

    goto :goto_241

    :cond_23f
    move/from16 v1, v26

    .line 904
    .end local v1    # "$this$isUnspecified$iv":J
    .end local v18    # "$i$f$isUnspecified--R2X_6o":I
    :goto_241
    if-nez v1, :cond_246

    move-wide/from16 v29, v11

    goto :goto_24c

    :cond_246
    invoke-virtual/range {p0 .. p0}, Landroidx/compose/ui/text/SpanStyle;->getFontSize-XSAIIZE()J

    move-result-wide v1

    move-wide/from16 v29, v1

    .line 905
    :goto_24c
    if-nez p7, :cond_255

    invoke-virtual/range {p0 .. p0}, Landroidx/compose/ui/text/SpanStyle;->getFontWeight()Landroidx/compose/ui/text/font/FontWeight;

    move-result-object v1

    move-object/from16 v31, v1

    goto :goto_257

    :cond_255
    move-object/from16 v31, p7

    .line 906
    :goto_257
    if-nez p8, :cond_260

    invoke-virtual/range {p0 .. p0}, Landroidx/compose/ui/text/SpanStyle;->getFontStyle-4Lr2A7w()Landroidx/compose/ui/text/font/FontStyle;

    move-result-object v1

    move-object/from16 v32, v1

    goto :goto_262

    :cond_260
    move-object/from16 v32, p8

    .line 907
    :goto_262
    if-nez p9, :cond_26b

    invoke-virtual/range {p0 .. p0}, Landroidx/compose/ui/text/SpanStyle;->getFontSynthesis-ZQGJjVo()Landroidx/compose/ui/text/font/FontSynthesis;

    move-result-object v1

    move-object/from16 v33, v1

    goto :goto_26d

    :cond_26b
    move-object/from16 v33, p9

    .line 908
    :goto_26d
    if-nez v9, :cond_276

    invoke-virtual/range {p0 .. p0}, Landroidx/compose/ui/text/SpanStyle;->getFontFeatureSettings()Ljava/lang/String;

    move-result-object v1

    move-object/from16 v35, v1

    goto :goto_278

    :cond_276
    move-object/from16 v35, v9

    .line 910
    :goto_278
    move-wide/from16 v1, p12

    .restart local v1    # "$this$isUnspecified$iv":J
    const/16 v18, 0x0

    .line 953
    .restart local v18    # "$i$f$isUnspecified--R2X_6o":I
    invoke-static {v1, v2}, Landroidx/compose/ui/unit/TextUnit;->getRawType-impl(J)J

    move-result-wide v19

    cmp-long v19, v19, v24

    if-nez v19, :cond_287

    move/from16 v1, v23

    goto :goto_289

    :cond_287
    move/from16 v1, v26

    .line 910
    .end local v1    # "$this$isUnspecified$iv":J
    .end local v18    # "$i$f$isUnspecified--R2X_6o":I
    :goto_289
    if-nez v1, :cond_28e

    .line 911
    move-wide/from16 v36, p12

    goto :goto_294

    .line 913
    :cond_28e
    invoke-virtual/range {p0 .. p0}, Landroidx/compose/ui/text/SpanStyle;->getLetterSpacing-XSAIIZE()J

    move-result-wide v1

    move-wide/from16 v36, v1

    .line 910
    :goto_294
    nop

    .line 915
    if-nez v10, :cond_29e

    invoke-virtual/range {p0 .. p0}, Landroidx/compose/ui/text/SpanStyle;->getBaselineShift-5SSeXJ0()Landroidx/compose/ui/text/style/BaselineShift;

    move-result-object v1

    move-object/from16 v38, v1

    goto :goto_2a0

    :cond_29e
    move-object/from16 v38, v10

    .line 916
    :goto_2a0
    if-nez v15, :cond_2a9

    invoke-virtual/range {p0 .. p0}, Landroidx/compose/ui/text/SpanStyle;->getTextGeometricTransform()Landroidx/compose/ui/text/style/TextGeometricTransform;

    move-result-object v1

    move-object/from16 v39, v1

    goto :goto_2ab

    :cond_2a9
    move-object/from16 v39, v15

    .line 917
    :goto_2ab
    if-nez p16, :cond_2b4

    invoke-virtual/range {p0 .. p0}, Landroidx/compose/ui/text/SpanStyle;->getLocaleList()Landroidx/compose/ui/text/intl/LocaleList;

    move-result-object v1

    move-object/from16 v40, v1

    goto :goto_2b6

    :cond_2b4
    move-object/from16 v40, p16

    .line 918
    :goto_2b6
    move-wide/from16 v1, p17

    .local v1, "$this$takeOrElse_u2dDxMtmZc$iv":J
    const/16 v18, 0x0

    .line 954
    .local v18, "$i$f$takeOrElse-DxMtmZc":I
    move-wide/from16 v19, v1

    .local v19, "$this$isSpecified$iv$iv":J
    const/16 v21, 0x0

    .line 955
    .local v21, "$i$f$isSpecified-8_81llA":I
    cmp-long v16, v19, v16

    if-eqz v16, :cond_2c3

    goto :goto_2c5

    :cond_2c3
    move/from16 v23, v26

    .line 954
    .end local v19    # "$this$isSpecified$iv$iv":J
    .end local v21    # "$i$f$isSpecified-8_81llA":I
    :goto_2c5
    if-eqz v23, :cond_2ca

    move-wide/from16 v41, v1

    goto :goto_2d2

    :cond_2ca
    const/16 v16, 0x0

    .line 918
    .local v16, "$i$a$-takeOrElse-DxMtmZc-SpanStyleKt$fastMerge$1":I
    invoke-virtual/range {p0 .. p0}, Landroidx/compose/ui/text/SpanStyle;->getBackground-0d7_KjU()J

    move-result-wide v16

    .line 954
    .end local v16    # "$i$a$-takeOrElse-DxMtmZc-SpanStyleKt$fastMerge$1":I
    move-wide/from16 v41, v16

    .line 918
    .end local v1    # "$this$takeOrElse_u2dDxMtmZc$iv":J
    .end local v18    # "$i$f$takeOrElse-DxMtmZc":I
    :goto_2d2
    nop

    .line 919
    if-nez v13, :cond_2dc

    invoke-virtual/range {p0 .. p0}, Landroidx/compose/ui/text/SpanStyle;->getTextDecoration()Landroidx/compose/ui/text/style/TextDecoration;

    move-result-object v1

    move-object/from16 v43, v1

    goto :goto_2de

    :cond_2dc
    move-object/from16 v43, v13

    .line 920
    :goto_2de
    if-nez v14, :cond_2e7

    invoke-virtual/range {p0 .. p0}, Landroidx/compose/ui/text/SpanStyle;->getShadow()Landroidx/compose/ui/graphics/Shadow;

    move-result-object v1

    move-object/from16 v44, v1

    goto :goto_2e9

    :cond_2e7
    move-object/from16 v44, v14

    .line 921
    :goto_2e9
    move-object/from16 v1, p0

    invoke-static {v1, v6}, Landroidx/compose/ui/text/SpanStyleKt;->mergePlatformStyle(Landroidx/compose/ui/text/SpanStyle;Landroidx/compose/ui/text/PlatformSpanStyle;)Landroidx/compose/ui/text/PlatformSpanStyle;

    move-result-object v45

    .line 922
    if-nez p22, :cond_2f8

    invoke-virtual {v1}, Landroidx/compose/ui/text/SpanStyle;->getDrawStyle()Landroidx/compose/ui/graphics/drawscope/DrawStyle;

    move-result-object v2

    move-object/from16 v46, v2

    goto :goto_2fa

    :cond_2f8
    move-object/from16 v46, p22

    .line 901
    :goto_2fa
    new-instance v27, Landroidx/compose/ui/text/SpanStyle;

    .line 902
    nop

    .line 904
    nop

    .line 905
    nop

    .line 906
    nop

    .line 907
    nop

    .line 903
    nop

    .line 908
    nop

    .line 910
    nop

    .line 915
    nop

    .line 916
    nop

    .line 917
    nop

    .line 918
    nop

    .line 919
    nop

    .line 920
    nop

    .line 921
    nop

    .line 922
    nop

    .line 901
    const/16 v47, 0x0

    invoke-direct/range {v27 .. v47}, Landroidx/compose/ui/text/SpanStyle;-><init>(Landroidx/compose/ui/text/style/TextForegroundStyle;JLandroidx/compose/ui/text/font/FontWeight;Landroidx/compose/ui/text/font/FontStyle;Landroidx/compose/ui/text/font/FontSynthesis;Landroidx/compose/ui/text/font/FontFamily;Ljava/lang/String;JLandroidx/compose/ui/text/style/BaselineShift;Landroidx/compose/ui/text/style/TextGeometricTransform;Landroidx/compose/ui/text/intl/LocaleList;JLandroidx/compose/ui/text/style/TextDecoration;Landroidx/compose/ui/graphics/Shadow;Landroidx/compose/ui/text/PlatformSpanStyle;Landroidx/compose/ui/graphics/drawscope/DrawStyle;Lkotlin/jvm/internal/DefaultConstructorMarker;)V

    return-object v27
.end method
