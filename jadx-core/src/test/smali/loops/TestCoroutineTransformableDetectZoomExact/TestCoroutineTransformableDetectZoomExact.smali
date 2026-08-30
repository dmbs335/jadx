.class public final Lloops/TestCoroutineTransformableDetectZoomExact;
.super Ljava/lang/Object;

.method public static final d(Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;ZLkotlinx/coroutines/channels/Channel;Lkotlin/jvm/functions/Function1;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 34
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "(",
            "Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;",
            "Z",
            "Lkotlinx/coroutines/channels/Channel<",
            "Landroidx/compose/foundation/gestures/TransformEvent;",
            ">;",
            "Lkotlin/jvm/functions/Function1<",
            "-",
            "Landroidx/compose/ui/geometry/Offset;",
            "Ljava/lang/Boolean;",
            ">;",
            "Lkotlin/coroutines/Continuation<",
            "-",
            "Lkotlin/Unit;",
            ">;)",
            "Ljava/lang/Object;"
        }
    .end annotation

    move-object/from16 v0, p4

    instance-of v1, v0, Lloops/TestCoroutineTransformableDetectZoomExact$State;

    if-eqz v1, :cond_16

    move-object v1, v0

    check-cast v1, Lloops/TestCoroutineTransformableDetectZoomExact$State;

    iget v2, v1, Lloops/TestCoroutineTransformableDetectZoomExact$State;->label:I

    const/high16 v3, -0x80000000

    and-int v4, v2, v3

    if-eqz v4, :cond_16

    sub-int/2addr v2, v3

    iput v2, v1, Lloops/TestCoroutineTransformableDetectZoomExact$State;->label:I

    :goto_14
    move-object v5, v1

    goto :goto_1c

    :cond_16
    new-instance v1, Lloops/TestCoroutineTransformableDetectZoomExact$State;

    .line 1
    invoke-direct {v1, v0}, Lkotlin/coroutines/jvm/internal/ContinuationImpl;-><init>(Lkotlin/coroutines/Continuation;)V

    goto :goto_14

    .line 2
    :goto_1c
    iget-object v0, v5, Lloops/TestCoroutineTransformableDetectZoomExact$State;->result:Ljava/lang/Object;

    invoke-static {}, Lkotlin/coroutines/intrinsics/a;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;

    move-result-object v1

    .line 3
    iget v2, v5, Lloops/TestCoroutineTransformableDetectZoomExact$State;->label:I

    const/4 v9, 0x3

    const/4 v10, 0x2

    const/4 v13, 0x1

    if-eqz v2, :cond_b7

    if-eq v2, v13, :cond_94

    if-eq v2, v10, :cond_6c

    if-ne v2, v9, :cond_64

    iget v2, v5, Lloops/TestCoroutineTransformableDetectZoomExact$State;->I$2:I

    iget v3, v5, Lloops/TestCoroutineTransformableDetectZoomExact$State;->I$1:I

    iget v4, v5, Lloops/TestCoroutineTransformableDetectZoomExact$State;->F$2:F

    iget v6, v5, Lloops/TestCoroutineTransformableDetectZoomExact$State;->I$0:I

    iget-wide v14, v5, Lloops/TestCoroutineTransformableDetectZoomExact$State;->J$0:J

    iget v7, v5, Lloops/TestCoroutineTransformableDetectZoomExact$State;->F$1:F

    iget v9, v5, Lloops/TestCoroutineTransformableDetectZoomExact$State;->F$0:F

    iget-boolean v10, v5, Lloops/TestCoroutineTransformableDetectZoomExact$State;->Z$0:Z

    iget-object v13, v5, Lloops/TestCoroutineTransformableDetectZoomExact$State;->L$3:Ljava/lang/Object;

    check-cast v13, Landroidx/compose/ui/input/pointer/PointerEvent;

    iget-object v12, v5, Lloops/TestCoroutineTransformableDetectZoomExact$State;->L$2:Ljava/lang/Object;

    check-cast v12, Lkotlin/jvm/functions/Function1;

    iget-object v8, v5, Lloops/TestCoroutineTransformableDetectZoomExact$State;->L$1:Ljava/lang/Object;

    check-cast v8, Lkotlinx/coroutines/channels/Channel;

    iget-object v11, v5, Lloops/TestCoroutineTransformableDetectZoomExact$State;->L$0:Ljava/lang/Object;

    check-cast v11, Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;

    invoke-static {v0}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V

    move-object/from16 v16, v8

    move v8, v7

    move-object/from16 v7, v16

    move-object/from16 v16, v11

    move-object v11, v12

    const/high16 v19, 0x3f800000    # 1.0f

    const/16 v20, 0x0

    move v12, v10

    move-object v10, v5

    move-object v5, v0

    const/4 v0, 0x3

    goto/16 :goto_2a5

    :cond_64
    new-instance v0, Ljava/lang/IllegalStateException;

    const-string v1, "call to \'resume\' before \'invoke\' with coroutine"

    invoke-direct {v0, v1}, Ljava/lang/IllegalStateException;-><init>(Ljava/lang/String;)V

    throw v0

    :cond_6c
    iget v2, v5, Lloops/TestCoroutineTransformableDetectZoomExact$State;->I$1:I

    iget v3, v5, Lloops/TestCoroutineTransformableDetectZoomExact$State;->F$2:F

    iget v4, v5, Lloops/TestCoroutineTransformableDetectZoomExact$State;->I$0:I

    iget-wide v6, v5, Lloops/TestCoroutineTransformableDetectZoomExact$State;->J$0:J

    iget v8, v5, Lloops/TestCoroutineTransformableDetectZoomExact$State;->F$1:F

    iget v9, v5, Lloops/TestCoroutineTransformableDetectZoomExact$State;->F$0:F

    iget-boolean v10, v5, Lloops/TestCoroutineTransformableDetectZoomExact$State;->Z$0:Z

    iget-object v11, v5, Lloops/TestCoroutineTransformableDetectZoomExact$State;->L$2:Ljava/lang/Object;

    check-cast v11, Lkotlin/jvm/functions/Function1;

    iget-object v12, v5, Lloops/TestCoroutineTransformableDetectZoomExact$State;->L$1:Ljava/lang/Object;

    check-cast v12, Lkotlinx/coroutines/channels/Channel;

    iget-object v13, v5, Lloops/TestCoroutineTransformableDetectZoomExact$State;->L$0:Ljava/lang/Object;

    check-cast v13, Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;

    invoke-static {v0}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V

    move v14, v9

    move v9, v8

    move-object v8, v12

    move v12, v10

    move v10, v14

    move-object v14, v13

    move-object v13, v11

    move-object v11, v14

    const/4 v14, 0x2

    goto/16 :goto_12e

    :cond_94
    iget v2, v5, Lloops/TestCoroutineTransformableDetectZoomExact$State;->I$1:I

    iget v3, v5, Lloops/TestCoroutineTransformableDetectZoomExact$State;->F$2:F

    iget v4, v5, Lloops/TestCoroutineTransformableDetectZoomExact$State;->I$0:I

    iget-wide v6, v5, Lloops/TestCoroutineTransformableDetectZoomExact$State;->J$0:J

    iget v8, v5, Lloops/TestCoroutineTransformableDetectZoomExact$State;->F$1:F

    iget v9, v5, Lloops/TestCoroutineTransformableDetectZoomExact$State;->F$0:F

    iget-boolean v10, v5, Lloops/TestCoroutineTransformableDetectZoomExact$State;->Z$0:Z

    iget-object v11, v5, Lloops/TestCoroutineTransformableDetectZoomExact$State;->L$2:Ljava/lang/Object;

    check-cast v11, Lkotlin/jvm/functions/Function1;

    iget-object v12, v5, Lloops/TestCoroutineTransformableDetectZoomExact$State;->L$1:Ljava/lang/Object;

    check-cast v12, Lkotlinx/coroutines/channels/Channel;

    iget-object v13, v5, Lloops/TestCoroutineTransformableDetectZoomExact$State;->L$0:Ljava/lang/Object;

    check-cast v13, Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;

    invoke-static {v0}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V

    move-object/from16 v28, v12

    move v12, v10

    move-object/from16 v10, v28

    goto :goto_100

    :cond_b7
    invoke-static {v0}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V

    .line 4
    sget-object v0, Landroidx/compose/ui/geometry/Offset;->Companion:Landroidx/compose/ui/geometry/Offset$Companion;

    invoke-virtual {v0}, Landroidx/compose/ui/geometry/Offset$Companion;->getZero-F1C5BW0()J

    move-result-wide v8

    .line 5
    invoke-interface/range {p0 .. p0}, Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;->getViewConfiguration()Landroidx/compose/ui/platform/ViewConfiguration;

    move-result-object v0

    invoke-interface {v0}, Landroidx/compose/ui/platform/ViewConfiguration;->getTouchSlop()F

    move-result v0

    move-object/from16 v2, p0

    .line 6
    iput-object v2, v5, Lloops/TestCoroutineTransformableDetectZoomExact$State;->L$0:Ljava/lang/Object;

    move-object/from16 v10, p2

    iput-object v10, v5, Lloops/TestCoroutineTransformableDetectZoomExact$State;->L$1:Ljava/lang/Object;

    move-object/from16 v11, p3

    iput-object v11, v5, Lloops/TestCoroutineTransformableDetectZoomExact$State;->L$2:Ljava/lang/Object;

    move/from16 v12, p1

    iput-boolean v12, v5, Lloops/TestCoroutineTransformableDetectZoomExact$State;->Z$0:Z

    const/4 v3, 0x0

    iput v3, v5, Lloops/TestCoroutineTransformableDetectZoomExact$State;->F$0:F

    const/high16 v3, 0x3f800000    # 1.0f

    iput v3, v5, Lloops/TestCoroutineTransformableDetectZoomExact$State;->F$1:F

    iput-wide v8, v5, Lloops/TestCoroutineTransformableDetectZoomExact$State;->J$0:J

    const/4 v3, 0x0

    iput v3, v5, Lloops/TestCoroutineTransformableDetectZoomExact$State;->I$0:I

    iput v0, v5, Lloops/TestCoroutineTransformableDetectZoomExact$State;->F$2:F

    iput v3, v5, Lloops/TestCoroutineTransformableDetectZoomExact$State;->I$1:I

    const/4 v3, 0x1

    iput v3, v5, Lloops/TestCoroutineTransformableDetectZoomExact$State;->label:I

    const/4 v3, 0x0

    const/4 v4, 0x0

    const/4 v6, 0x2

    const/4 v7, 0x0

    invoke-static/range {v2 .. v7}, Landroidx/compose/foundation/gestures/TapGestureDetectorKt;->awaitFirstDown$default(Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;ZLandroidx/compose/ui/input/pointer/PointerEventPass;Lkotlin/coroutines/Continuation;ILjava/lang/Object;)Ljava/lang/Object;

    move-result-object v3

    if-ne v3, v1, :cond_f7

    goto/16 :goto_297

    :cond_f7
    move v3, v0

    move-wide v6, v8

    const/4 v2, 0x0

    const/4 v4, 0x0

    const/high16 v8, 0x3f800000    # 1.0f

    const/4 v9, 0x0

    move-object/from16 v13, p0

    .line 7
    :goto_100
    iput-object v13, v5, Lloops/TestCoroutineTransformableDetectZoomExact$State;->L$0:Ljava/lang/Object;

    iput-object v10, v5, Lloops/TestCoroutineTransformableDetectZoomExact$State;->L$1:Ljava/lang/Object;

    iput-object v11, v5, Lloops/TestCoroutineTransformableDetectZoomExact$State;->L$2:Ljava/lang/Object;

    const/4 v0, 0x0

    iput-object v0, v5, Lloops/TestCoroutineTransformableDetectZoomExact$State;->L$3:Ljava/lang/Object;

    iput-boolean v12, v5, Lloops/TestCoroutineTransformableDetectZoomExact$State;->Z$0:Z

    iput v9, v5, Lloops/TestCoroutineTransformableDetectZoomExact$State;->F$0:F

    iput v8, v5, Lloops/TestCoroutineTransformableDetectZoomExact$State;->F$1:F

    iput-wide v6, v5, Lloops/TestCoroutineTransformableDetectZoomExact$State;->J$0:J

    iput v4, v5, Lloops/TestCoroutineTransformableDetectZoomExact$State;->I$0:I

    iput v3, v5, Lloops/TestCoroutineTransformableDetectZoomExact$State;->F$2:F

    iput v2, v5, Lloops/TestCoroutineTransformableDetectZoomExact$State;->I$1:I

    const/4 v14, 0x2

    iput v14, v5, Lloops/TestCoroutineTransformableDetectZoomExact$State;->label:I

    const/4 v15, 0x1

    invoke-static {v13, v0, v5, v15, v0}, Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;->awaitPointerEvent$default(Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;Landroidx/compose/ui/input/pointer/PointerEventPass;Lkotlin/coroutines/Continuation;ILjava/lang/Object;)Ljava/lang/Object;

    move-result-object v0

    if-ne v0, v1, :cond_123

    goto/16 :goto_297

    :cond_123
    move/from16 v28, v9

    move v9, v8

    move-object v8, v10

    move/from16 v10, v28

    move-object/from16 v28, v13

    move-object v13, v11

    move-object/from16 v11, v28

    .line 8
    :goto_12e
    check-cast v0, Landroidx/compose/ui/input/pointer/PointerEvent;

    .line 9
    invoke-virtual {v0}, Landroidx/compose/ui/input/pointer/PointerEvent;->getChanges()Ljava/util/List;

    move-result-object v15

    .line 10
    move-object/from16 v16, v15

    check-cast v16, Ljava/util/Collection;

    invoke-interface/range {v16 .. v16}, Ljava/util/Collection;->size()I

    move-result v14

    move/from16 p1, v2

    const/4 v2, 0x0

    :goto_13f
    if-ge v2, v14, :cond_152

    .line 11
    invoke-interface {v15, v2}, Ljava/util/List;->get(I)Ljava/lang/Object;

    move-result-object v16

    .line 12
    check-cast v16, Landroidx/compose/ui/input/pointer/PointerInputChange;

    .line 13
    invoke-virtual/range {v16 .. v16}, Landroidx/compose/ui/input/pointer/PointerInputChange;->isConsumed()Z

    move-result v16

    if-eqz v16, :cond_14f

    const/4 v2, 0x1

    goto :goto_153

    :cond_14f
    add-int/lit8 v2, v2, 0x1

    goto :goto_13f

    :cond_152
    const/4 v2, 0x0

    :goto_153
    if-nez v2, :cond_252

    .line 14
    invoke-static {v0}, Landroidx/compose/foundation/gestures/TransformGestureDetectorKt;->calculateZoom(Landroidx/compose/ui/input/pointer/PointerEvent;)F

    move-result v22

    .line 15
    invoke-static {v0}, Landroidx/compose/foundation/gestures/TransformGestureDetectorKt;->calculateRotation(Landroidx/compose/ui/input/pointer/PointerEvent;)F

    move-result v14

    move/from16 p2, v9

    move/from16 p3, v10

    .line 16
    invoke-static {v0}, Landroidx/compose/foundation/gestures/TransformGestureDetectorKt;->calculatePan(Landroidx/compose/ui/input/pointer/PointerEvent;)J

    move-result-wide v9

    if-nez v4, :cond_1d0

    mul-float v15, p2, v22

    add-float v16, p3, v14

    .line 17
    invoke-static {v6, v7, v9, v10}, Landroidx/compose/ui/geometry/Offset;->plus-MK-Hz9U(JJ)J

    move-result-wide v6

    move/from16 v21, v4

    const/4 v4, 0x0

    .line 18
    invoke-static {v0, v4}, Landroidx/compose/foundation/gestures/TransformGestureDetectorKt;->calculateCentroidSize(Landroidx/compose/ui/input/pointer/PointerEvent;Z)F

    move-result v18

    move-wide/from16 p2, v6

    const/4 v4, 0x1

    int-to-float v6, v4

    sub-float/2addr v6, v15

    .line 19
    invoke-static {v6}, Ljava/lang/Math;->abs(F)F

    move-result v6

    mul-float v6, v6, v18

    const v7, 0x40490fdb    # (float)Math.PI

    mul-float v7, v7, v16

    mul-float v7, v7, v18

    const/high16 v17, 0x43340000    # 180.0f

    div-float v7, v7, v17

    .line 20
    invoke-static {v7}, Ljava/lang/Math;->abs(F)F

    move-result v7

    .line 21
    invoke-static/range {p2 .. p3}, Landroidx/compose/ui/geometry/Offset;->getDistance-impl(J)F

    move-result v17

    cmpl-float v6, v6, v3

    if-gtz v6, :cond_1bc

    cmpl-float v6, v7, v3

    if-gtz v6, :cond_1bc

    cmpl-float v6, v17, v3

    if-lez v6, :cond_1b1

    .line 22
    invoke-static {v9, v10}, Landroidx/compose/ui/geometry/Offset;->box-impl(J)Landroidx/compose/ui/geometry/Offset;

    move-result-object v6

    invoke-interface {v13, v6}, Lkotlin/jvm/functions/Function1;->invoke(Ljava/lang/Object;)Ljava/lang/Object;

    move-result-object v6

    check-cast v6, Ljava/lang/Boolean;

    invoke-virtual {v6}, Ljava/lang/Boolean;->booleanValue()Z

    move-result v6

    if-eqz v6, :cond_1b1

    goto :goto_1bc

    :cond_1b1
    move/from16 v6, p1

    move/from16 v17, v15

    move/from16 v18, v16

    move/from16 v7, v21

    :goto_1b9
    move-wide/from16 v15, p2

    goto :goto_1dc

    :cond_1bc
    :goto_1bc
    if-eqz v12, :cond_1c4

    cmpg-float v6, v7, v3

    if-gez v6, :cond_1c4

    move v6, v4

    goto :goto_1c5

    :cond_1c4
    const/4 v6, 0x0

    .line 23
    :goto_1c5
    sget-object v7, Landroidx/compose/foundation/gestures/TransformEvent$TransformStarted;->INSTANCE:Landroidx/compose/foundation/gestures/TransformEvent$TransformStarted;

    invoke-interface {v8, v7}, Lkotlinx/coroutines/channels/SendChannel;->trySend-JP2dKIU(Ljava/lang/Object;)Ljava/lang/Object;

    move v7, v4

    move/from16 v17, v15

    move/from16 v18, v16

    goto :goto_1b9

    :cond_1d0
    move/from16 v21, v4

    const/4 v4, 0x1

    move/from16 v17, p2

    move/from16 v18, p3

    move-wide v15, v6

    move/from16 v7, v21

    move/from16 v6, p1

    :goto_1dc
    if-eqz v7, :cond_246

    if-eqz v6, :cond_1e5

    const/16 v25, 0x0

    :goto_1e2
    const/16 v20, 0x0

    goto :goto_1e8

    :cond_1e5
    move/from16 v25, v14

    goto :goto_1e2

    :goto_1e8
    cmpg-float v14, v25, v20

    if-nez v14, :cond_214

    const/high16 v19, 0x3f800000    # 1.0f

    cmpg-float v14, v22, v19

    if-nez v14, :cond_211

    .line 24
    sget-object v14, Landroidx/compose/ui/geometry/Offset;->Companion:Landroidx/compose/ui/geometry/Offset$Companion;

    move-object/from16 v27, v5

    invoke-virtual {v14}, Landroidx/compose/ui/geometry/Offset$Companion;->getZero-F1C5BW0()J

    move-result-wide v4

    invoke-static {v9, v10, v4, v5}, Landroidx/compose/ui/geometry/Offset;->equals-impl0(JJ)Z

    move-result v4

    if-nez v4, :cond_226

    invoke-static {v9, v10}, Landroidx/compose/ui/geometry/Offset;->box-impl(J)Landroidx/compose/ui/geometry/Offset;

    move-result-object v4

    invoke-interface {v13, v4}, Lkotlin/jvm/functions/Function1;->invoke(Ljava/lang/Object;)Ljava/lang/Object;

    move-result-object v4

    check-cast v4, Ljava/lang/Boolean;

    invoke-virtual {v4}, Ljava/lang/Boolean;->booleanValue()Z

    move-result v4

    if-eqz v4, :cond_226

    goto :goto_218

    :cond_211
    move-object/from16 v27, v5

    goto :goto_218

    :cond_214
    move-object/from16 v27, v5

    const/high16 v19, 0x3f800000    # 1.0f

    .line 25
    :goto_218
    new-instance v21, Landroidx/compose/foundation/gestures/TransformEvent$TransformDelta;

    const/16 v26, 0x0

    move-wide/from16 v23, v9

    invoke-direct/range {v21 .. v26}, Landroidx/compose/foundation/gestures/TransformEvent$TransformDelta;-><init>(FJFLkotlin/jvm/internal/DefaultConstructorMarker;)V

    move-object/from16 v4, v21

    invoke-interface {v8, v4}, Lkotlinx/coroutines/channels/SendChannel;->trySend-JP2dKIU(Ljava/lang/Object;)Ljava/lang/Object;

    .line 26
    :cond_226
    invoke-virtual {v0}, Landroidx/compose/ui/input/pointer/PointerEvent;->getChanges()Ljava/util/List;

    move-result-object v4

    .line 27
    move-object v5, v4

    check-cast v5, Ljava/util/Collection;

    invoke-interface {v5}, Ljava/util/Collection;->size()I

    move-result v5

    const/4 v9, 0x0

    :goto_232
    if-ge v9, v5, :cond_24c

    .line 28
    invoke-interface {v4, v9}, Ljava/util/List;->get(I)Ljava/lang/Object;

    move-result-object v10

    .line 29
    check-cast v10, Landroidx/compose/ui/input/pointer/PointerInputChange;

    .line 30
    invoke-static {v10}, Landroidx/compose/ui/input/pointer/PointerEventKt;->positionChanged(Landroidx/compose/ui/input/pointer/PointerInputChange;)Z

    move-result v14

    if-eqz v14, :cond_243

    .line 31
    invoke-virtual {v10}, Landroidx/compose/ui/input/pointer/PointerInputChange;->consume()V

    :cond_243
    add-int/lit8 v9, v9, 0x1

    goto :goto_232

    :cond_246
    move-object/from16 v27, v5

    const/high16 v19, 0x3f800000    # 1.0f

    const/16 v20, 0x0

    :cond_24c
    move-wide v14, v15

    move/from16 v4, v17

    move/from16 v9, v18

    goto :goto_270

    :cond_252
    move/from16 v21, v4

    move-object/from16 v27, v5

    move/from16 p2, v9

    move/from16 p3, v10

    const/high16 v19, 0x3f800000    # 1.0f

    const/16 v20, 0x0

    .line 32
    sget-object v4, Landroidx/compose/foundation/gestures/TransformEvent$TransformStopped;->INSTANCE:Landroidx/compose/foundation/gestures/TransformEvent$TransformStopped;

    invoke-interface {v8, v4}, Lkotlinx/coroutines/channels/SendChannel;->trySend-JP2dKIU(Ljava/lang/Object;)Ljava/lang/Object;

    move-result-object v4

    invoke-static {v4}, Lkotlinx/coroutines/channels/ChannelResult;->box-impl(Ljava/lang/Object;)Lkotlinx/coroutines/channels/ChannelResult;

    move/from16 v4, p2

    move/from16 v9, p3

    move-wide v14, v6

    move/from16 v7, v21

    move/from16 v6, p1

    .line 33
    :goto_270
    sget-object v5, Landroidx/compose/ui/input/pointer/PointerEventPass;->Final:Landroidx/compose/ui/input/pointer/PointerEventPass;

    move-object/from16 v10, v27

    iput-object v11, v10, Lloops/TestCoroutineTransformableDetectZoomExact$State;->L$0:Ljava/lang/Object;

    iput-object v8, v10, Lloops/TestCoroutineTransformableDetectZoomExact$State;->L$1:Ljava/lang/Object;

    iput-object v13, v10, Lloops/TestCoroutineTransformableDetectZoomExact$State;->L$2:Ljava/lang/Object;

    iput-object v0, v10, Lloops/TestCoroutineTransformableDetectZoomExact$State;->L$3:Ljava/lang/Object;

    iput-boolean v12, v10, Lloops/TestCoroutineTransformableDetectZoomExact$State;->Z$0:Z

    iput v9, v10, Lloops/TestCoroutineTransformableDetectZoomExact$State;->F$0:F

    iput v4, v10, Lloops/TestCoroutineTransformableDetectZoomExact$State;->F$1:F

    iput-wide v14, v10, Lloops/TestCoroutineTransformableDetectZoomExact$State;->J$0:J

    iput v7, v10, Lloops/TestCoroutineTransformableDetectZoomExact$State;->I$0:I

    iput v3, v10, Lloops/TestCoroutineTransformableDetectZoomExact$State;->F$2:F

    iput v6, v10, Lloops/TestCoroutineTransformableDetectZoomExact$State;->I$1:I

    iput v2, v10, Lloops/TestCoroutineTransformableDetectZoomExact$State;->I$2:I

    move-object/from16 p1, v0

    const/4 v0, 0x3

    iput v0, v10, Lloops/TestCoroutineTransformableDetectZoomExact$State;->label:I

    invoke-interface {v11, v5, v10}, Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;->awaitPointerEvent(Landroidx/compose/ui/input/pointer/PointerEventPass;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;

    move-result-object v5

    if-ne v5, v1, :cond_298

    :goto_297
    return-object v1

    :cond_298
    move/from16 v16, v4

    move v4, v3

    move v3, v6

    move v6, v7

    move-object v7, v8

    move/from16 v8, v16

    move-object/from16 v16, v11

    move-object v11, v13

    move-object/from16 v13, p1

    .line 34
    :goto_2a5
    check-cast v5, Landroidx/compose/ui/input/pointer/PointerEvent;

    .line 35
    invoke-virtual {v5}, Landroidx/compose/ui/input/pointer/PointerEvent;->getChanges()Ljava/util/List;

    move-result-object v5

    .line 36
    move-object/from16 v17, v5

    check-cast v17, Ljava/util/Collection;

    invoke-interface/range {v17 .. v17}, Ljava/util/Collection;->size()I

    move-result v0

    move-object/from16 v17, v1

    const/4 v1, 0x0

    :goto_2b6
    if-ge v1, v0, :cond_2cb

    .line 37
    invoke-interface {v5, v1}, Ljava/util/List;->get(I)Ljava/lang/Object;

    move-result-object v18

    .line 38
    check-cast v18, Landroidx/compose/ui/input/pointer/PointerInputChange;

    .line 39
    invoke-virtual/range {v18 .. v18}, Landroidx/compose/ui/input/pointer/PointerInputChange;->isConsumed()Z

    move-result v18

    if-eqz v18, :cond_2c8

    if-nez v6, :cond_2cb

    const/4 v0, 0x1

    goto :goto_2cc

    :cond_2c8
    add-int/lit8 v1, v1, 0x1

    goto :goto_2b6

    :cond_2cb
    const/4 v0, 0x0

    :goto_2cc
    if-nez v2, :cond_2f9

    if-nez v0, :cond_2f9

    .line 40
    invoke-virtual {v13}, Landroidx/compose/ui/input/pointer/PointerEvent;->getChanges()Ljava/util/List;

    move-result-object v0

    .line 41
    move-object v1, v0

    check-cast v1, Ljava/util/Collection;

    invoke-interface {v1}, Ljava/util/Collection;->size()I

    move-result v1

    const/4 v2, 0x0

    :goto_2dc
    if-ge v2, v1, :cond_2f9

    .line 42
    invoke-interface {v0, v2}, Ljava/util/List;->get(I)Ljava/lang/Object;

    move-result-object v5

    .line 43
    check-cast v5, Landroidx/compose/ui/input/pointer/PointerInputChange;

    .line 44
    invoke-virtual {v5}, Landroidx/compose/ui/input/pointer/PointerInputChange;->getPressed()Z

    move-result v5

    if-eqz v5, :cond_2f6

    move v2, v3

    move v3, v4

    move v4, v6

    move-object v5, v10

    move-object/from16 v13, v16

    move-object/from16 v1, v17

    move-object v10, v7

    move-wide v6, v14

    goto/16 :goto_100

    :cond_2f6
    add-int/lit8 v2, v2, 0x1

    goto :goto_2dc

    .line 45
    :cond_2f9
    sget-object v0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;

    return-object v0
.end method
