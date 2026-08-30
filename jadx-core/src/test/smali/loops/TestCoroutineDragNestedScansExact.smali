.class public final Lloops/TestCoroutineDragNestedScansExact;
.super Ljava/lang/Object;

.method public static final awaitHorizontalPointerSlopOrCancellation-gDDlDlE(Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;JILkotlin/jvm/functions/Function2;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 23
    move-wide/from16 v0, p1
    move-object/from16 v2, p5
    instance-of v3, v2, Landroidx/compose/foundation/gestures/DragGestureDetectorKt$awaitHorizontalPointerSlopOrCancellation$1;
    if-eqz v3, :cond_17
    move-object v3, v2
    check-cast v3, Landroidx/compose/foundation/gestures/DragGestureDetectorKt$awaitHorizontalPointerSlopOrCancellation$1;
    iget v4, v3, Landroidx/compose/foundation/gestures/DragGestureDetectorKt$awaitHorizontalPointerSlopOrCancellation$1;->label:I
    const/high16 v5, -0x80000000
    and-int v6, v4, v5
    if-eqz v6, :cond_17
    sub-int/2addr v4, v5
    iput v4, v3, Landroidx/compose/foundation/gestures/DragGestureDetectorKt$awaitHorizontalPointerSlopOrCancellation$1;->label:I
    goto :goto_1c
    :cond_17
    new-instance v3, Landroidx/compose/foundation/gestures/DragGestureDetectorKt$awaitHorizontalPointerSlopOrCancellation$1;
    invoke-direct {v3, v2}, Lkotlin/coroutines/jvm/internal/ContinuationImpl;-><init>(Lkotlin/coroutines/Continuation;)V
    :goto_1c
    iget-object v2, v3, Landroidx/compose/foundation/gestures/DragGestureDetectorKt$awaitHorizontalPointerSlopOrCancellation$1;->result:Ljava/lang/Object;
    invoke-static {}, Lkotlin/coroutines/intrinsics/a;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v4
    iget v5, v3, Landroidx/compose/foundation/gestures/DragGestureDetectorKt$awaitHorizontalPointerSlopOrCancellation$1;->label:I
    const/4 v6, 0x2
    const/4 v7, 0x1
    const/4 v8, 0x0
    if-eqz v5, :cond_73
    if-eq v5, v7, :cond_5c
    if-ne v5, v6, :cond_4f
    iget v0, v3, Landroidx/compose/foundation/gestures/DragGestureDetectorKt$awaitHorizontalPointerSlopOrCancellation$1;->F$0:F
    iget-object v1, v3, Landroidx/compose/foundation/gestures/DragGestureDetectorKt$awaitHorizontalPointerSlopOrCancellation$1;->L$4:Ljava/lang/Object;
    check-cast v1, Landroidx/compose/ui/input/pointer/PointerInputChange;
    iget-object v5, v3, Landroidx/compose/foundation/gestures/DragGestureDetectorKt$awaitHorizontalPointerSlopOrCancellation$1;->L$3:Ljava/lang/Object;
    check-cast v5, Landroidx/compose/foundation/gestures/TouchSlopDetector;
    iget-object v9, v3, Landroidx/compose/foundation/gestures/DragGestureDetectorKt$awaitHorizontalPointerSlopOrCancellation$1;->L$2:Ljava/lang/Object;
    check-cast v9, Lkotlin/jvm/internal/Ref$LongRef;
    iget-object v10, v3, Landroidx/compose/foundation/gestures/DragGestureDetectorKt$awaitHorizontalPointerSlopOrCancellation$1;->L$1:Ljava/lang/Object;
    check-cast v10, Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;
    iget-object v11, v3, Landroidx/compose/foundation/gestures/DragGestureDetectorKt$awaitHorizontalPointerSlopOrCancellation$1;->L$0:Ljava/lang/Object;
    check-cast v11, Lkotlin/jvm/functions/Function2;
    invoke-static {v2}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    move-object v2, v5
    move-object v5, v1
    move v1, v6
    move v6, v0
    move-object v0, v10
    move-object v10, v11
    move-object v11, v9
    goto/16 :goto_191
    :cond_4f
    new-instance v0, Ljava/lang/IllegalStateException;
    const v1, 0x624cfed3
    invoke-static {v1}, Lfixtures/obfuscation/StringDecoder;->decode(I)Ljava/lang/String;
    move-result-object v1
    invoke-direct {v0, v1}, Ljava/lang/IllegalStateException;-><init>(Ljava/lang/String;)V
    throw v0
    :cond_5c
    iget v0, v3, Landroidx/compose/foundation/gestures/DragGestureDetectorKt$awaitHorizontalPointerSlopOrCancellation$1;->F$0:F
    iget-object v1, v3, Landroidx/compose/foundation/gestures/DragGestureDetectorKt$awaitHorizontalPointerSlopOrCancellation$1;->L$3:Ljava/lang/Object;
    check-cast v1, Landroidx/compose/foundation/gestures/TouchSlopDetector;
    iget-object v5, v3, Landroidx/compose/foundation/gestures/DragGestureDetectorKt$awaitHorizontalPointerSlopOrCancellation$1;->L$2:Ljava/lang/Object;
    check-cast v5, Lkotlin/jvm/internal/Ref$LongRef;
    iget-object v9, v3, Landroidx/compose/foundation/gestures/DragGestureDetectorKt$awaitHorizontalPointerSlopOrCancellation$1;->L$1:Ljava/lang/Object;
    check-cast v9, Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;
    iget-object v10, v3, Landroidx/compose/foundation/gestures/DragGestureDetectorKt$awaitHorizontalPointerSlopOrCancellation$1;->L$0:Ljava/lang/Object;
    check-cast v10, Lkotlin/jvm/functions/Function2;
    invoke-static {v2}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    move-object v11, v5
    goto :goto_bf
    :cond_73
    invoke-static {v2}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    sget-object v2, Landroidx/compose/foundation/gestures/Orientation;->Horizontal:Landroidx/compose/foundation/gestures/Orientation;
    sget-object v5, Landroidx/compose/ui/geometry/Offset;->Companion:Landroidx/compose/ui/geometry/Offset$Companion;
    invoke-virtual {v5}, Landroidx/compose/ui/geometry/Offset$Companion;->getZero-F1C5BW0()J
    move-result-wide v9
    invoke-interface/range {p0 .. p0}, Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;->getCurrentEvent()Landroidx/compose/ui/input/pointer/PointerEvent;
    move-result-object v5
    invoke-static {v5, v0, v1}, Landroidx/compose/foundation/gestures/DragGestureDetectorKt;->access$isPointerUp-DmW0f2w(Landroidx/compose/ui/input/pointer/PointerEvent;J)Z
    move-result v5
    if-eqz v5, :cond_89
    return-object v8
    :cond_89
    invoke-interface/range {p0 .. p0}, Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;->getViewConfiguration()Landroidx/compose/ui/platform/ViewConfiguration;
    move-result-object v5
    move/from16 v11, p3
    invoke-static {v5, v11}, Landroidx/compose/foundation/gestures/DragGestureDetectorKt;->pointerSlop-E8SPZFQ(Landroidx/compose/ui/platform/ViewConfiguration;I)F
    move-result v5
    new-instance v11, Lkotlin/jvm/internal/Ref$LongRef;
    invoke-direct {v11}, Lkotlin/jvm/internal/Ref$LongRef;-><init>()V
    iput-wide v0, v11, Lkotlin/jvm/internal/Ref$LongRef;->element:J
    new-instance v0, Landroidx/compose/foundation/gestures/TouchSlopDetector;
    invoke-direct {v0, v2, v9, v10, v8}, Landroidx/compose/foundation/gestures/TouchSlopDetector;-><init>(Landroidx/compose/foundation/gestures/Orientation;JLkotlin/jvm/internal/DefaultConstructorMarker;)V
    move-object/from16 v1, p4
    move-object v2, v0
    move-object/from16 v0, p0
    :goto_a4
    iput-object v1, v3, Landroidx/compose/foundation/gestures/DragGestureDetectorKt$awaitHorizontalPointerSlopOrCancellation$1;->L$0:Ljava/lang/Object;
    iput-object v0, v3, Landroidx/compose/foundation/gestures/DragGestureDetectorKt$awaitHorizontalPointerSlopOrCancellation$1;->L$1:Ljava/lang/Object;
    iput-object v11, v3, Landroidx/compose/foundation/gestures/DragGestureDetectorKt$awaitHorizontalPointerSlopOrCancellation$1;->L$2:Ljava/lang/Object;
    iput-object v2, v3, Landroidx/compose/foundation/gestures/DragGestureDetectorKt$awaitHorizontalPointerSlopOrCancellation$1;->L$3:Ljava/lang/Object;
    iput-object v8, v3, Landroidx/compose/foundation/gestures/DragGestureDetectorKt$awaitHorizontalPointerSlopOrCancellation$1;->L$4:Ljava/lang/Object;
    iput v5, v3, Landroidx/compose/foundation/gestures/DragGestureDetectorKt$awaitHorizontalPointerSlopOrCancellation$1;->F$0:F
    iput v7, v3, Landroidx/compose/foundation/gestures/DragGestureDetectorKt$awaitHorizontalPointerSlopOrCancellation$1;->label:I
    invoke-static {v0, v8, v3, v7, v8}, Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;->awaitPointerEvent$default(Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;Landroidx/compose/ui/input/pointer/PointerEventPass;Lkotlin/coroutines/Continuation;ILjava/lang/Object;)Ljava/lang/Object;
    move-result-object v9
    if-ne v9, v4, :cond_ba
    goto/16 :goto_18f
    :cond_ba
    move-object v10, v1
    move-object v1, v2
    move-object v2, v9
    move-object v9, v0
    move v0, v5
    :goto_bf
    check-cast v2, Landroidx/compose/ui/input/pointer/PointerEvent;
    invoke-virtual {v2}, Landroidx/compose/ui/input/pointer/PointerEvent;->getChanges()Ljava/util/List;
    move-result-object v5
    move-object v12, v5
    check-cast v12, Ljava/util/Collection;
    invoke-interface {v12}, Ljava/util/Collection;->size()I
    move-result v12
    const/4 v14, 0x0
    :goto_cd
    if-ge v14, v12, :cond_eb
    invoke-interface {v5, v14}, Ljava/util/List;->get(I)Ljava/lang/Object;
    move-result-object v15
    move-object/from16 v16, v15
    check-cast v16, Landroidx/compose/ui/input/pointer/PointerInputChange;
    move/from16 p1, v14
    invoke-virtual/range {v16 .. v16}, Landroidx/compose/ui/input/pointer/PointerInputChange;->getId-J3iCeTQ()J
    move-result-wide v13
    iget-wide v6, v11, Lkotlin/jvm/internal/Ref$LongRef;->element:J
    invoke-static {v13, v14, v6, v7}, Landroidx/compose/ui/input/pointer/PointerId;->equals-impl0(JJ)Z
    move-result v6
    if-eqz v6, :cond_e6
    goto :goto_ec
    :cond_e6
    add-int/lit8 v14, p1, 0x1
    const/4 v6, 0x2
    const/4 v7, 0x1
    goto :goto_cd
    :cond_eb
    move-object v15, v8
    :goto_ec
    move-object v5, v15
    check-cast v5, Landroidx/compose/ui/input/pointer/PointerInputChange;
    if-nez v5, :cond_f2
    return-object v8
    :cond_f2
    invoke-virtual {v5}, Landroidx/compose/ui/input/pointer/PointerInputChange;->isConsumed()Z
    move-result v6
    if-eqz v6, :cond_f9
    return-object v8
    :cond_f9
    invoke-static {v5}, Landroidx/compose/ui/input/pointer/PointerEventKt;->changedToUpIgnoreConsumed(Landroidx/compose/ui/input/pointer/PointerInputChange;)Z
    move-result v6
    if-eqz v6, :cond_12e
    invoke-virtual {v2}, Landroidx/compose/ui/input/pointer/PointerEvent;->getChanges()Ljava/util/List;
    move-result-object v2
    move-object v5, v2
    check-cast v5, Ljava/util/Collection;
    invoke-interface {v5}, Ljava/util/Collection;->size()I
    move-result v5
    const/4 v13, 0x0
    :goto_10b
    if-ge v13, v5, :cond_11e
    invoke-interface {v2, v13}, Ljava/util/List;->get(I)Ljava/lang/Object;
    move-result-object v6
    move-object v7, v6
    check-cast v7, Landroidx/compose/ui/input/pointer/PointerInputChange;
    invoke-virtual {v7}, Landroidx/compose/ui/input/pointer/PointerInputChange;->getPressed()Z
    move-result v7
    if-eqz v7, :cond_11b
    goto :goto_11f
    :cond_11b
    add-int/lit8 v13, v13, 0x1
    goto :goto_10b
    :cond_11e
    move-object v6, v8
    :goto_11f
    check-cast v6, Landroidx/compose/ui/input/pointer/PointerInputChange;
    if-nez v6, :cond_124
    return-object v8
    :cond_124
    invoke-virtual {v6}, Landroidx/compose/ui/input/pointer/PointerInputChange;->getId-J3iCeTQ()J
    move-result-wide v5
    iput-wide v5, v11, Lkotlin/jvm/internal/Ref$LongRef;->element:J
    move v6, v0
    move-object v2, v1
    const/4 v7, 0x1
    goto :goto_171
    :cond_12e
    invoke-virtual {v5}, Landroidx/compose/ui/input/pointer/PointerInputChange;->getPosition-F1C5BW0()J
    move-result-wide v6
    invoke-virtual {v5}, Landroidx/compose/ui/input/pointer/PointerInputChange;->getPreviousPosition-F1C5BW0()J
    move-result-wide v12
    move/from16 p5, v0
    move-object/from16 p0, v1
    move-wide/from16 p1, v6
    move-wide/from16 p3, v12
    invoke-virtual/range {p0 .. p5}, Landroidx/compose/foundation/gestures/TouchSlopDetector;->addPositions-akrDWew(JJF)J
    move-result-wide v0
    move-object/from16 v2, p0
    move/from16 v6, p5
    const-wide v12, 0x7fffffff7fffffffL
    and-long/2addr v12, v0
    const-wide v14, 0x7fc000007fc00000L    # 2.247117487993712E307
    cmp-long v7, v12, v14
    if-eqz v7, :cond_177
    const/16 v7, 0x20
    shr-long/2addr v0, v7
    long-to-int v0, v0
    invoke-static {v0}, Ljava/lang/Float;->intBitsToFloat(I)F
    move-result v0
    invoke-static {v0}, Lkotlin/coroutines/jvm/internal/Boxing;->boxFloat(F)Ljava/lang/Float;
    move-result-object v0
    invoke-interface {v10, v5, v0}, Lkotlin/jvm/functions/Function2;->invoke(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    invoke-virtual {v5}, Landroidx/compose/ui/input/pointer/PointerInputChange;->isConsumed()Z
    move-result v0
    if-eqz v0, :cond_16b
    return-object v5
    :cond_16b
    const-wide/16 v0, 0x0
    const/4 v7, 0x1
    invoke-static {v2, v0, v1, v7, v8}, Landroidx/compose/foundation/gestures/TouchSlopDetector;->reset-k-4lQ0M$default(Landroidx/compose/foundation/gestures/TouchSlopDetector;JILjava/lang/Object;)V
    :goto_171
    move v5, v6
    move-object v0, v9
    move-object v1, v10
    const/4 v6, 0x2
    goto/16 :goto_a4
    :cond_177
    const/4 v7, 0x1
    sget-object v0, Landroidx/compose/ui/input/pointer/PointerEventPass;->Final:Landroidx/compose/ui/input/pointer/PointerEventPass;
    iput-object v10, v3, Landroidx/compose/foundation/gestures/DragGestureDetectorKt$awaitHorizontalPointerSlopOrCancellation$1;->L$0:Ljava/lang/Object;
    iput-object v9, v3, Landroidx/compose/foundation/gestures/DragGestureDetectorKt$awaitHorizontalPointerSlopOrCancellation$1;->L$1:Ljava/lang/Object;
    iput-object v11, v3, Landroidx/compose/foundation/gestures/DragGestureDetectorKt$awaitHorizontalPointerSlopOrCancellation$1;->L$2:Ljava/lang/Object;
    iput-object v2, v3, Landroidx/compose/foundation/gestures/DragGestureDetectorKt$awaitHorizontalPointerSlopOrCancellation$1;->L$3:Ljava/lang/Object;
    iput-object v5, v3, Landroidx/compose/foundation/gestures/DragGestureDetectorKt$awaitHorizontalPointerSlopOrCancellation$1;->L$4:Ljava/lang/Object;
    iput v6, v3, Landroidx/compose/foundation/gestures/DragGestureDetectorKt$awaitHorizontalPointerSlopOrCancellation$1;->F$0:F
    const/4 v1, 0x2
    iput v1, v3, Landroidx/compose/foundation/gestures/DragGestureDetectorKt$awaitHorizontalPointerSlopOrCancellation$1;->label:I
    invoke-interface {v9, v0, v3}, Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;->awaitPointerEvent(Landroidx/compose/ui/input/pointer/PointerEventPass;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v0
    if-ne v0, v4, :cond_190
    :goto_18f
    return-object v4
    :cond_190
    move-object v0, v9
    :goto_191
    invoke-virtual {v5}, Landroidx/compose/ui/input/pointer/PointerInputChange;->isConsumed()Z
    move-result v5
    if-eqz v5, :cond_198
    return-object v8
    :cond_198
    move v5, v6
    move v6, v1
    move-object v1, v10
    goto/16 :goto_a4
.end method
