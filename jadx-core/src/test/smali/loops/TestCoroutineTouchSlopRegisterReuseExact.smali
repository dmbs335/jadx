.class public final Lloops/TestCoroutineTouchSlopRegisterReuseExact;
.super Ljava/lang/Object;

.method public static final awaitHorizontalTouchSlopOrCancellation-jO51t88(Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;JLkotlin/jvm/functions/Function2;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 24
    .param p0    # Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;
        .annotation build Lorg/jetbrains/annotations/NotNull;
        .end annotation
    .end param
    .param p3    # Lkotlin/jvm/functions/Function2;
        .annotation build Lorg/jetbrains/annotations/NotNull;
        .end annotation
    .end param
    .param p4    # Lkotlin/coroutines/Continuation;
        .annotation build Lorg/jetbrains/annotations/NotNull;
        .end annotation
    .end param
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "(",
            "Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;",
            "J",
            "Lkotlin/jvm/functions/Function2<",
            "-",
            "Landroidx/compose/ui/input/pointer/PointerInputChange;",
            "-",
            "Ljava/lang/Float;",
            "Lkotlin/Unit;",
            ">;",
            "Lkotlin/coroutines/Continuation<",
            "-",
            "Landroidx/compose/ui/input/pointer/PointerInputChange;",
            ">;)",
            "Ljava/lang/Object;"
        }
    .end annotation

    .annotation build Lorg/jetbrains/annotations/Nullable;
    .end annotation

    .line 1
    move-wide/from16 v0, p1

    .line 2
    .line 3
    move-object/from16 v2, p4

    .line 4
    .line 5
    instance-of v3, v2, Landroidx/compose/foundation/gestures/DragGestureDetectorKt$awaitHorizontalTouchSlopOrCancellation$1;

    .line 6
    .line 7
    if-eqz v3, :cond_17

    .line 8
    .line 9
    move-object v3, v2

    .line 10
    check-cast v3, Landroidx/compose/foundation/gestures/DragGestureDetectorKt$awaitHorizontalTouchSlopOrCancellation$1;

    .line 11
    .line 12
    iget v4, v3, Landroidx/compose/foundation/gestures/DragGestureDetectorKt$awaitHorizontalTouchSlopOrCancellation$1;->label:I

    .line 13
    .line 14
    const/high16 v5, -0x80000000

    .line 15
    .line 16
    and-int v6, v4, v5

    .line 17
    .line 18
    if-eqz v6, :cond_17

    .line 19
    .line 20
    sub-int/2addr v4, v5

    .line 21
    iput v4, v3, Landroidx/compose/foundation/gestures/DragGestureDetectorKt$awaitHorizontalTouchSlopOrCancellation$1;->label:I

    .line 22
    .line 23
    goto :goto_1c

    .line 24
    :cond_17
    new-instance v3, Landroidx/compose/foundation/gestures/DragGestureDetectorKt$awaitHorizontalTouchSlopOrCancellation$1;

    .line 25
    .line 26
    invoke-direct {v3, v2}, Lkotlin/coroutines/jvm/internal/ContinuationImpl;-><init>(Lkotlin/coroutines/Continuation;)V

    .line 27
    .line 28
    .line 29
    :goto_1c
    iget-object v2, v3, Landroidx/compose/foundation/gestures/DragGestureDetectorKt$awaitHorizontalTouchSlopOrCancellation$1;->result:Ljava/lang/Object;

    .line 30
    .line 31
    invoke-static {}, Lkotlin/coroutines/intrinsics/a;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;

    .line 32
    .line 33
    .line 34
    move-result-object v4

    .line 35
    iget v5, v3, Landroidx/compose/foundation/gestures/DragGestureDetectorKt$awaitHorizontalTouchSlopOrCancellation$1;->label:I

    .line 36
    .line 37
    const/4 v6, 0x2

    .line 38
    const/4 v7, 0x1

    .line 39
    const/4 v8, 0x0

    .line 40
    if-eqz v5, :cond_79

    .line 41
    .line 42
    if-eq v5, v7, :cond_5c

    .line 43
    .line 44
    if-ne v5, v6, :cond_4f

    .line 45
    .line 46
    iget v0, v3, Landroidx/compose/foundation/gestures/DragGestureDetectorKt$awaitHorizontalTouchSlopOrCancellation$1;->F$0:F

    .line 47
    .line 48
    iget-object v1, v3, Landroidx/compose/foundation/gestures/DragGestureDetectorKt$awaitHorizontalTouchSlopOrCancellation$1;->L$4:Ljava/lang/Object;

    .line 49
    .line 50
    check-cast v1, Landroidx/compose/ui/input/pointer/PointerInputChange;

    .line 51
    .line 52
    iget-object v5, v3, Landroidx/compose/foundation/gestures/DragGestureDetectorKt$awaitHorizontalTouchSlopOrCancellation$1;->L$3:Ljava/lang/Object;

    .line 53
    .line 54
    check-cast v5, Landroidx/compose/foundation/gestures/TouchSlopDetector;

    .line 55
    .line 56
    iget-object v9, v3, Landroidx/compose/foundation/gestures/DragGestureDetectorKt$awaitHorizontalTouchSlopOrCancellation$1;->L$2:Ljava/lang/Object;

    .line 57
    .line 58
    check-cast v9, Lkotlin/jvm/internal/Ref$LongRef;

    .line 59
    .line 60
    iget-object v10, v3, Landroidx/compose/foundation/gestures/DragGestureDetectorKt$awaitHorizontalTouchSlopOrCancellation$1;->L$1:Ljava/lang/Object;

    .line 61
    .line 62
    check-cast v10, Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;

    .line 63
    .line 64
    iget-object v11, v3, Landroidx/compose/foundation/gestures/DragGestureDetectorKt$awaitHorizontalTouchSlopOrCancellation$1;->L$0:Ljava/lang/Object;

    .line 65
    .line 66
    check-cast v11, Lkotlin/jvm/functions/Function2;

    .line 67
    .line 68
    invoke-static {v2}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V

    .line 69
    .line 70
    .line 71
    move-object v2, v5

    .line 72
    move-object v5, v3

    .line 73
    move-object v3, v2

    .line 74
    move v2, v0

    .line 75
    move v8, v7

    .line 76
    move-object v0, v10

    .line 77
    move v7, v6

    .line 78
    goto/16 :goto_19f

    .line 79
    .line 80
    :cond_4f
    new-instance v0, Ljava/lang/IllegalStateException;

    .line 81
    .line 82
    const v1, 0x624cfed3

    invoke-static {v1}, Lfixtures/obfuscation/StringDecoder;->decode(I)Ljava/lang/String;

    move-result-object v1

    .line 83
    .line 84
    invoke-direct {v0, v1}, Ljava/lang/IllegalStateException;-><init>(Ljava/lang/String;)V

    .line 85
    .line 86
    .line 87
    throw v0

    .line 88
    :cond_5c
    iget v0, v3, Landroidx/compose/foundation/gestures/DragGestureDetectorKt$awaitHorizontalTouchSlopOrCancellation$1;->F$0:F

    .line 89
    .line 90
    iget-object v1, v3, Landroidx/compose/foundation/gestures/DragGestureDetectorKt$awaitHorizontalTouchSlopOrCancellation$1;->L$3:Ljava/lang/Object;

    .line 91
    .line 92
    check-cast v1, Landroidx/compose/foundation/gestures/TouchSlopDetector;

    .line 93
    .line 94
    iget-object v5, v3, Landroidx/compose/foundation/gestures/DragGestureDetectorKt$awaitHorizontalTouchSlopOrCancellation$1;->L$2:Ljava/lang/Object;

    .line 95
    .line 96
    check-cast v5, Lkotlin/jvm/internal/Ref$LongRef;

    .line 97
    .line 98
    iget-object v9, v3, Landroidx/compose/foundation/gestures/DragGestureDetectorKt$awaitHorizontalTouchSlopOrCancellation$1;->L$1:Ljava/lang/Object;

    .line 99
    .line 100
    check-cast v9, Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;

    .line 101
    .line 102
    iget-object v10, v3, Landroidx/compose/foundation/gestures/DragGestureDetectorKt$awaitHorizontalTouchSlopOrCancellation$1;->L$0:Ljava/lang/Object;

    .line 103
    .line 104
    check-cast v10, Lkotlin/jvm/functions/Function2;

    .line 105
    .line 106
    invoke-static {v2}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V

    .line 107
    .line 108
    .line 109
    move/from16 v17, v0

    .line 110
    .line 111
    move-object v12, v1

    .line 112
    move-object v11, v5

    .line 113
    move-object v0, v9

    .line 114
    move-object v1, v10

    .line 115
    move-object v5, v3

    .line 116
    goto :goto_c9

    .line 117
    :cond_79
    invoke-static {v2}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V

    .line 118
    .line 119
    .line 120
    sget-object v2, Landroidx/compose/ui/input/pointer/PointerType;->Companion:Landroidx/compose/ui/input/pointer/PointerType$Companion;

    .line 121
    .line 122
    invoke-virtual {v2}, Landroidx/compose/ui/input/pointer/PointerType$Companion;->getTouch-T8wyACA()I

    .line 123
    .line 124
    .line 125
    move-result v2

    .line 126
    sget-object v5, Landroidx/compose/foundation/gestures/Orientation;->Horizontal:Landroidx/compose/foundation/gestures/Orientation;

    .line 127
    .line 128
    sget-object v9, Landroidx/compose/ui/geometry/Offset;->Companion:Landroidx/compose/ui/geometry/Offset$Companion;

    .line 129
    .line 130
    invoke-virtual {v9}, Landroidx/compose/ui/geometry/Offset$Companion;->getZero-F1C5BW0()J

    .line 131
    .line 132
    .line 133
    move-result-wide v9

    .line 134
    invoke-interface/range {p0 .. p0}, Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;->getCurrentEvent()Landroidx/compose/ui/input/pointer/PointerEvent;

    .line 135
    .line 136
    .line 137
    move-result-object v11

    .line 138
    invoke-static {v11, v0, v1}, Landroidx/compose/foundation/gestures/DragGestureDetectorKt;->access$isPointerUp-DmW0f2w(Landroidx/compose/ui/input/pointer/PointerEvent;J)Z

    .line 139
    .line 140
    .line 141
    move-result v11

    .line 142
    if-eqz v11, :cond_95

    .line 143
    .line 144
    return-object v8

    .line 145
    :cond_95
    invoke-interface/range {p0 .. p0}, Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;->getViewConfiguration()Landroidx/compose/ui/platform/ViewConfiguration;

    .line 146
    .line 147
    .line 148
    move-result-object v11

    .line 149
    invoke-static {v11, v2}, Landroidx/compose/foundation/gestures/DragGestureDetectorKt;->pointerSlop-E8SPZFQ(Landroidx/compose/ui/platform/ViewConfiguration;I)F

    .line 150
    .line 151
    .line 152
    move-result v2

    .line 153
    new-instance v11, Lkotlin/jvm/internal/Ref$LongRef;

    .line 154
    .line 155
    invoke-direct {v11}, Lkotlin/jvm/internal/Ref$LongRef;-><init>()V

    .line 156
    .line 157
    .line 158
    iput-wide v0, v11, Lkotlin/jvm/internal/Ref$LongRef;->element:J

    .line 159
    .line 160
    new-instance v0, Landroidx/compose/foundation/gestures/TouchSlopDetector;

    .line 161
    .line 162
    invoke-direct {v0, v5, v9, v10, v8}, Landroidx/compose/foundation/gestures/TouchSlopDetector;-><init>(Landroidx/compose/foundation/gestures/Orientation;JLkotlin/jvm/internal/DefaultConstructorMarker;)V

    .line 163
    .line 164
    .line 165
    move-object/from16 v1, p3

    .line 166
    .line 167
    move-object v5, v3

    .line 168
    move-object v3, v0

    .line 169
    move-object/from16 v0, p0

    .line 170
    .line 171
    :goto_af
    iput-object v1, v5, Landroidx/compose/foundation/gestures/DragGestureDetectorKt$awaitHorizontalTouchSlopOrCancellation$1;->L$0:Ljava/lang/Object;

    .line 172
    .line 173
    iput-object v0, v5, Landroidx/compose/foundation/gestures/DragGestureDetectorKt$awaitHorizontalTouchSlopOrCancellation$1;->L$1:Ljava/lang/Object;

    .line 174
    .line 175
    iput-object v11, v5, Landroidx/compose/foundation/gestures/DragGestureDetectorKt$awaitHorizontalTouchSlopOrCancellation$1;->L$2:Ljava/lang/Object;

    .line 176
    .line 177
    iput-object v3, v5, Landroidx/compose/foundation/gestures/DragGestureDetectorKt$awaitHorizontalTouchSlopOrCancellation$1;->L$3:Ljava/lang/Object;

    .line 178
    .line 179
    iput-object v8, v5, Landroidx/compose/foundation/gestures/DragGestureDetectorKt$awaitHorizontalTouchSlopOrCancellation$1;->L$4:Ljava/lang/Object;

    .line 180
    .line 181
    iput v2, v5, Landroidx/compose/foundation/gestures/DragGestureDetectorKt$awaitHorizontalTouchSlopOrCancellation$1;->F$0:F

    .line 182
    .line 183
    iput v7, v5, Landroidx/compose/foundation/gestures/DragGestureDetectorKt$awaitHorizontalTouchSlopOrCancellation$1;->label:I

    .line 184
    .line 185
    invoke-static {v0, v8, v5, v7, v8}, Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;->awaitPointerEvent$default(Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;Landroidx/compose/ui/input/pointer/PointerEventPass;Lkotlin/coroutines/Continuation;ILjava/lang/Object;)Ljava/lang/Object;

    .line 186
    .line 187
    .line 188
    move-result-object v9

    .line 189
    if-ne v9, v4, :cond_c5

    .line 190
    .line 191
    goto/16 :goto_19a

    .line 192
    .line 193
    :cond_c5
    move/from16 v17, v2

    .line 194
    .line 195
    move-object v12, v3

    .line 196
    move-object v2, v9

    .line 197
    :goto_c9
    check-cast v2, Landroidx/compose/ui/input/pointer/PointerEvent;

    .line 198
    .line 199
    invoke-virtual {v2}, Landroidx/compose/ui/input/pointer/PointerEvent;->getChanges()Ljava/util/List;

    .line 200
    .line 201
    .line 202
    move-result-object v3

    .line 203
    move-object v9, v3

    .line 204
    check-cast v9, Ljava/util/Collection;

    .line 205
    .line 206
    invoke-interface {v9}, Ljava/util/Collection;->size()I

    .line 207
    .line 208
    .line 209
    move-result v9

    .line 210
    const/4 v10, 0x0

    .line 211
    move v13, v10

    .line 212
    :goto_d8
    if-ge v13, v9, :cond_fb

    .line 213
    .line 214
    invoke-interface {v3, v13}, Ljava/util/List;->get(I)Ljava/lang/Object;

    .line 215
    .line 216
    .line 217
    move-result-object v14

    .line 218
    move-object v15, v14

    .line 219
    check-cast v15, Landroidx/compose/ui/input/pointer/PointerInputChange;

    .line 220
    .line 221
    invoke-virtual {v15}, Landroidx/compose/ui/input/pointer/PointerInputChange;->getId-J3iCeTQ()J

    .line 222
    .line 223
    .line 224
    move-result-wide v6

    .line 225
    move-object/from16 v18, v8

    .line 226
    .line 227
    move/from16 p0, v9

    .line 228
    .line 229
    iget-wide v8, v11, Lkotlin/jvm/internal/Ref$LongRef;->element:J

    .line 230
    .line 231
    invoke-static {v6, v7, v8, v9}, Landroidx/compose/ui/input/pointer/PointerId;->equals-impl0(JJ)Z

    .line 232
    .line 233
    .line 234
    move-result v6

    .line 235
    if-eqz v6, :cond_f2

    .line 236
    .line 237
    goto :goto_ff

    .line 238
    :cond_f2
    add-int/lit8 v13, v13, 0x1

    .line 239
    .line 240
    move/from16 v9, p0

    .line 241
    .line 242
    move-object/from16 v8, v18

    .line 243
    .line 244
    const/4 v6, 0x2

    .line 245
    const/4 v7, 0x1

    .line 246
    goto :goto_d8

    .line 247
    :cond_fb
    move-object/from16 v18, v8

    .line 248
    .line 249
    move-object/from16 v14, v18

    .line 250
    .line 251
    :goto_ff
    move-object v3, v14

    .line 252
    check-cast v3, Landroidx/compose/ui/input/pointer/PointerInputChange;

    .line 253
    .line 254
    if-nez v3, :cond_105

    .line 255
    .line 256
    return-object v18

    .line 257
    :cond_105
    invoke-virtual {v3}, Landroidx/compose/ui/input/pointer/PointerInputChange;->isConsumed()Z

    .line 258
    .line 259
    .line 260
    move-result v6

    .line 261
    if-eqz v6, :cond_10c

    .line 262
    .line 263
    return-object v18

    .line 264
    :cond_10c
    invoke-static {v3}, Landroidx/compose/ui/input/pointer/PointerEventKt;->changedToUpIgnoreConsumed(Landroidx/compose/ui/input/pointer/PointerInputChange;)Z

    .line 265
    .line 266
    .line 267
    move-result v6

    .line 268
    if-eqz v6, :cond_141

    .line 269
    .line 270
    invoke-virtual {v2}, Landroidx/compose/ui/input/pointer/PointerEvent;->getChanges()Ljava/util/List;

    .line 271
    .line 272
    .line 273
    move-result-object v2

    .line 274
    move-object v3, v2

    .line 275
    check-cast v3, Ljava/util/Collection;

    .line 276
    .line 277
    invoke-interface {v3}, Ljava/util/Collection;->size()I

    .line 278
    .line 279
    .line 280
    move-result v3

    .line 281
    :goto_11d
    if-ge v10, v3, :cond_130

    .line 282
    .line 283
    invoke-interface {v2, v10}, Ljava/util/List;->get(I)Ljava/lang/Object;

    .line 284
    .line 285
    .line 286
    move-result-object v6

    .line 287
    move-object v7, v6

    .line 288
    check-cast v7, Landroidx/compose/ui/input/pointer/PointerInputChange;

    .line 289
    .line 290
    invoke-virtual {v7}, Landroidx/compose/ui/input/pointer/PointerInputChange;->getPressed()Z

    .line 291
    .line 292
    .line 293
    move-result v7

    .line 294
    if-eqz v7, :cond_12d

    .line 295
    .line 296
    goto :goto_132

    .line 297
    :cond_12d
    add-int/lit8 v10, v10, 0x1

    .line 298
    .line 299
    goto :goto_11d

    .line 300
    :cond_130
    move-object/from16 v6, v18

    .line 301
    .line 302
    :goto_132
    check-cast v6, Landroidx/compose/ui/input/pointer/PointerInputChange;

    .line 303
    .line 304
    if-nez v6, :cond_137

    .line 305
    .line 306
    return-object v18

    .line 307
    :cond_137
    invoke-virtual {v6}, Landroidx/compose/ui/input/pointer/PointerInputChange;->getId-J3iCeTQ()J

    .line 308
    .line 309
    .line 310
    move-result-wide v2

    .line 311
    iput-wide v2, v11, Lkotlin/jvm/internal/Ref$LongRef;->element:J

    .line 312
    .line 313
    move/from16 v2, v17

    .line 314
    .line 315
    const/4 v8, 0x1

    .line 316
    goto :goto_17c

    .line 317
    :cond_141
    invoke-virtual {v3}, Landroidx/compose/ui/input/pointer/PointerInputChange;->getPosition-F1C5BW0()J

    .line 318
    .line 319
    .line 320
    move-result-wide v13

    .line 321
    invoke-virtual {v3}, Landroidx/compose/ui/input/pointer/PointerInputChange;->getPreviousPosition-F1C5BW0()J

    .line 322
    .line 323
    .line 324
    move-result-wide v15

    .line 325
    invoke-virtual/range {v12 .. v17}, Landroidx/compose/foundation/gestures/TouchSlopDetector;->addPositions-akrDWew(JJF)J

    .line 326
    .line 327
    .line 328
    move-result-wide v6

    .line 329
    move/from16 v2, v17

    .line 330
    .line 331
    const-wide v8, 0x7fffffff7fffffffL

    .line 332
    .line 333
    .line 334
    .line 335
    .line 336
    and-long/2addr v8, v6

    .line 337
    const-wide v13, 0x7fc000007fc00000L    # 2.247117487993712E307

    .line 338
    .line 339
    .line 340
    .line 341
    .line 342
    cmp-long v8, v8, v13

    .line 343
    .line 344
    if-eqz v8, :cond_182

    .line 345
    .line 346
    const/16 v8, 0x20

    .line 347
    .line 348
    shr-long/2addr v6, v8

    .line 349
    long-to-int v6, v6

    .line 350
    invoke-static {v6}, Ljava/lang/Float;->intBitsToFloat(I)F

    .line 351
    .line 352
    .line 353
    move-result v6

    .line 354
    invoke-static {v6}, Lkotlin/coroutines/jvm/internal/Boxing;->boxFloat(F)Ljava/lang/Float;

    .line 355
    .line 356
    .line 357
    move-result-object v6

    .line 358
    invoke-interface {v1, v3, v6}, Lkotlin/jvm/functions/Function2;->invoke(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

    .line 359
    .line 360
    .line 361
    invoke-virtual {v3}, Landroidx/compose/ui/input/pointer/PointerInputChange;->isConsumed()Z

    .line 362
    .line 363
    .line 364
    move-result v6

    .line 365
    if-eqz v6, :cond_174

    .line 366
    .line 367
    return-object v3

    .line 368
    :cond_174
    const-wide/16 v6, 0x0

    .line 369
    .line 370
    move-object/from16 v3, v18

    .line 371
    .line 372
    const/4 v8, 0x1

    .line 373
    invoke-static {v12, v6, v7, v8, v3}, Landroidx/compose/foundation/gestures/TouchSlopDetector;->reset-k-4lQ0M$default(Landroidx/compose/foundation/gestures/TouchSlopDetector;JILjava/lang/Object;)V

    .line 374
    .line 375
    .line 376
    :goto_17c
    move v7, v8

    .line 377
    move-object v3, v12

    .line 378
    const/4 v6, 0x2

    .line 379
    const/4 v8, 0x0

    .line 380
    goto/16 :goto_af

    .line 381
    .line 382
    :cond_182
    const/4 v8, 0x1

    .line 383
    sget-object v6, Landroidx/compose/ui/input/pointer/PointerEventPass;->Final:Landroidx/compose/ui/input/pointer/PointerEventPass;

    .line 384
    .line 385
    iput-object v1, v5, Landroidx/compose/foundation/gestures/DragGestureDetectorKt$awaitHorizontalTouchSlopOrCancellation$1;->L$0:Ljava/lang/Object;

    .line 386
    .line 387
    iput-object v0, v5, Landroidx/compose/foundation/gestures/DragGestureDetectorKt$awaitHorizontalTouchSlopOrCancellation$1;->L$1:Ljava/lang/Object;

    .line 388
    .line 389
    iput-object v11, v5, Landroidx/compose/foundation/gestures/DragGestureDetectorKt$awaitHorizontalTouchSlopOrCancellation$1;->L$2:Ljava/lang/Object;

    .line 390
    .line 391
    iput-object v12, v5, Landroidx/compose/foundation/gestures/DragGestureDetectorKt$awaitHorizontalTouchSlopOrCancellation$1;->L$3:Ljava/lang/Object;

    .line 392
    .line 393
    iput-object v3, v5, Landroidx/compose/foundation/gestures/DragGestureDetectorKt$awaitHorizontalTouchSlopOrCancellation$1;->L$4:Ljava/lang/Object;

    .line 394
    .line 395
    iput v2, v5, Landroidx/compose/foundation/gestures/DragGestureDetectorKt$awaitHorizontalTouchSlopOrCancellation$1;->F$0:F

    .line 396
    .line 397
    const/4 v7, 0x2

    .line 398
    iput v7, v5, Landroidx/compose/foundation/gestures/DragGestureDetectorKt$awaitHorizontalTouchSlopOrCancellation$1;->label:I

    .line 399
    .line 400
    invoke-interface {v0, v6, v5}, Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;->awaitPointerEvent(Landroidx/compose/ui/input/pointer/PointerEventPass;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;

    .line 401
    .line 402
    .line 403
    move-result-object v6

    .line 404
    if-ne v6, v4, :cond_19b

    .line 405
    .line 406
    :goto_19a
    return-object v4

    .line 407
    :cond_19b
    move-object v9, v11

    .line 408
    move-object v11, v1

    .line 409
    move-object v1, v3

    .line 410
    move-object v3, v12

    .line 411
    :goto_19f
    invoke-virtual {v1}, Landroidx/compose/ui/input/pointer/PointerInputChange;->isConsumed()Z

    .line 412
    .line 413
    .line 414
    move-result v1

    .line 415
    const/16 v18, 0x0

    .line 416
    .line 417
    if-eqz v1, :cond_1a8

    .line 418
    .line 419
    return-object v18

    .line 420
    :cond_1a8
    move v6, v7

    .line 421
    move v7, v8

    .line 422
    move-object v1, v11

    .line 423
    move-object/from16 v8, v18

    .line 424
    .line 425
    move-object v11, v9

    .line 426
    goto/16 :goto_af
.end method
