.class public final Lloops/TestAwaitAllPointersUpPhiExit;
.super Ljava/lang/Object;

.method public static final awaitAllPointersUp(Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;Landroidx/compose/ui/input/pointer/PointerEventPass;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 21
    .param p2, "$completion"    # Lkotlin/coroutines/Continuation;
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "(",
            "Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;",
            "Landroidx/compose/ui/input/pointer/PointerEventPass;",
            "Lkotlin/coroutines/Continuation<",
            "-",
            "Lkotlin/Unit;",
            ">;)",
            "Ljava/lang/Object;"
        }
    .end annotation

    move-object/from16 v0, p2

    instance-of v1, v0, Landroidx/compose/foundation/gestures/ForEachGestureKt$awaitAllPointersUp$3;

    if-eqz v1, :cond_16

    move-object v1, v0

    check-cast v1, Landroidx/compose/foundation/gestures/ForEachGestureKt$awaitAllPointersUp$3;

    iget v2, v1, Landroidx/compose/foundation/gestures/ForEachGestureKt$awaitAllPointersUp$3;->label:I

    const/high16 v3, -0x80000000

    and-int/2addr v2, v3

    if-eqz v2, :cond_16

    iget v2, v1, Landroidx/compose/foundation/gestures/ForEachGestureKt$awaitAllPointersUp$3;->label:I

    sub-int/2addr v2, v3

    iput v2, v1, Landroidx/compose/foundation/gestures/ForEachGestureKt$awaitAllPointersUp$3;->label:I

    goto :goto_1b

    :cond_16
    new-instance v1, Landroidx/compose/foundation/gestures/ForEachGestureKt$awaitAllPointersUp$3;

    invoke-direct {v1, v0}, Landroidx/compose/foundation/gestures/ForEachGestureKt$awaitAllPointersUp$3;-><init>(Lkotlin/coroutines/Continuation;)V

    .local v1, "$continuation":Lkotlin/coroutines/Continuation;
    :goto_1b
    iget-object v2, v1, Landroidx/compose/foundation/gestures/ForEachGestureKt$awaitAllPointersUp$3;->result:Ljava/lang/Object;

    .local v2, "$result":Ljava/lang/Object;
    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;

    move-result-object v3

    .line 79
    iget v4, v1, Landroidx/compose/foundation/gestures/ForEachGestureKt$awaitAllPointersUp$3;->label:I

    const/4 v5, 0x1

    packed-switch v4, :pswitch_data_9e

    .end local v1    # "$continuation":Lkotlin/coroutines/Continuation;
    .end local v2    # "$result":Ljava/lang/Object;
    new-instance v1, Ljava/lang/IllegalStateException;

    const-string v2, "call to \'resume\' before \'invoke\' with coroutine"

    invoke-direct {v1, v2}, Ljava/lang/IllegalStateException;-><init>(Ljava/lang/String;)V

    throw v1

    .restart local v1    # "$continuation":Lkotlin/coroutines/Continuation;
    .restart local v2    # "$result":Ljava/lang/Object;
    :pswitch_2f
    iget-object v4, v1, Landroidx/compose/foundation/gestures/ForEachGestureKt$awaitAllPointersUp$3;->L$1:Ljava/lang/Object;

    check-cast v4, Landroidx/compose/ui/input/pointer/PointerEventPass;

    .local v4, "pass":Landroidx/compose/ui/input/pointer/PointerEventPass;
    iget-object v6, v1, Landroidx/compose/foundation/gestures/ForEachGestureKt$awaitAllPointersUp$3;->L$0:Ljava/lang/Object;

    check-cast v6, Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;

    .local v6, "$this$awaitAllPointersUp":Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;
    invoke-static {v2}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V

    move-object v7, v6

    move-object v6, v4

    move-object v4, v3

    move-object v3, v2

    goto :goto_66

    .end local v4    # "pass":Landroidx/compose/ui/input/pointer/PointerEventPass;
    .end local v6    # "$this$awaitAllPointersUp":Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;
    :pswitch_3f
    invoke-static {v2}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V

    move-object/from16 v4, p0

    .local v4, "$this$awaitAllPointersUp":Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;
    move-object/from16 v6, p1

    .line 82
    .local v6, "pass":Landroidx/compose/ui/input/pointer/PointerEventPass;
    invoke-static {v4}, Landroidx/compose/foundation/gestures/ForEachGestureKt;->allPointersUp(Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;)Z

    move-result v7

    if-nez v7, :cond_9b

    move-object/from16 v17, v6

    move-object v6, v4

    move-object/from16 v4, v17

    .line 84
    .end local p2    # "$completion":Lkotlin/coroutines/Continuation;
    .local v0, "$completion":Lkotlin/coroutines/Continuation;
    .local v4, "pass":Landroidx/compose/ui/input/pointer/PointerEventPass;
    .local v6, "$this$awaitAllPointersUp":Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;
    :goto_51
    iput-object v6, v1, Landroidx/compose/foundation/gestures/ForEachGestureKt$awaitAllPointersUp$3;->L$0:Ljava/lang/Object;

    iput-object v4, v1, Landroidx/compose/foundation/gestures/ForEachGestureKt$awaitAllPointersUp$3;->L$1:Ljava/lang/Object;

    iput v5, v1, Landroidx/compose/foundation/gestures/ForEachGestureKt$awaitAllPointersUp$3;->label:I

    invoke-interface {v6, v4, v1}, Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;->awaitPointerEvent(Landroidx/compose/ui/input/pointer/PointerEventPass;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;

    move-result-object v7

    if-ne v7, v3, :cond_5e

    .line 79
    return-object v3

    .line 84
    :cond_5e
    move-object/from16 v17, v3

    move-object v3, v2

    move-object v2, v7

    move-object v7, v6

    move-object v6, v4

    move-object/from16 v4, v17

    .line 79
    .end local v2    # "$result":Ljava/lang/Object;
    .end local v4    # "pass":Landroidx/compose/ui/input/pointer/PointerEventPass;
    .local v3, "$result":Ljava/lang/Object;
    .local v6, "pass":Landroidx/compose/ui/input/pointer/PointerEventPass;
    .local v7, "$this$awaitAllPointersUp":Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;
    :goto_66
    check-cast v2, Landroidx/compose/ui/input/pointer/PointerEvent;

    .line 85
    .local v2, "events":Landroidx/compose/ui/input/pointer/PointerEvent;
    invoke-virtual {v2}, Landroidx/compose/ui/input/pointer/PointerEvent;->getChanges()Ljava/util/List;

    move-result-object v2

    .local v2, "$this$fastAny$iv":Ljava/util/List;
    const/4 v8, 0x0

    .line 129
    .local v8, "$i$f$fastAny":I
    nop

    .local v2, "$this$fastForEach$iv$iv":Ljava/util/List;
    const/4 v9, 0x0

    .line 130
    .local v9, "$i$f$fastForEach":I
    const/4 v10, 0x0

    .local v10, "index$iv$iv":I
    move-object v11, v2

    check-cast v11, Ljava/util/Collection;

    invoke-interface {v11}, Ljava/util/Collection;->size()I

    move-result v11

    :goto_77
    if-ge v10, v11, :cond_90

    .line 131
    invoke-interface {v2, v10}, Ljava/util/List;->get(I)Ljava/lang/Object;

    move-result-object v12

    .line 132
    .local v12, "item$iv$iv":Ljava/lang/Object;
    move-object v13, v12

    .local v13, "it$iv":Ljava/lang/Object;
    const/4 v14, 0x0

    .line 129
    .local v14, "$i$a$-fastForEach-ListUtilsKt$fastAny$1$iv":I
    move-object v15, v13

    check-cast v15, Landroidx/compose/ui/input/pointer/PointerInputChange;

    .local v15, "it":Landroidx/compose/ui/input/pointer/PointerInputChange;
    const/16 v16, 0x0

    .line 85
    .local v16, "$i$a$-fastAny-ForEachGestureKt$awaitAllPointersUp$4":I
    invoke-virtual {v15}, Landroidx/compose/ui/input/pointer/PointerInputChange;->getPressed()Z

    move-result v15

    .line 129
    .end local v15    # "it":Landroidx/compose/ui/input/pointer/PointerInputChange;
    .end local v16    # "$i$a$-fastAny-ForEachGestureKt$awaitAllPointersUp$4":I
    if-eqz v15, :cond_8c

    move v2, v5

    goto :goto_92

    .line 132
    .end local v13    # "it$iv":Ljava/lang/Object;
    .end local v14    # "$i$a$-fastForEach-ListUtilsKt$fastAny$1$iv":I
    :cond_8c
    nop

    .line 130
    .end local v12    # "item$iv$iv":Ljava/lang/Object;
    add-int/lit8 v10, v10, 0x1

    goto :goto_77

    .line 134
    .end local v10    # "index$iv$iv":I
    :cond_90
    nop

    .line 135
    .end local v2    # "$this$fastForEach$iv$iv":Ljava/util/List;
    .end local v9    # "$i$f$fastForEach":I
    const/4 v2, 0x0

    .end local v8    # "$i$f$fastAny":I
    :goto_92
    if-nez v2, :cond_96

    move-object v2, v3

    goto :goto_9b

    :cond_96
    move-object v2, v3

    move-object v3, v4

    move-object v4, v6

    move-object v6, v7

    goto :goto_51

    .line 87
    .end local v3    # "$result":Ljava/lang/Object;
    .end local v6    # "pass":Landroidx/compose/ui/input/pointer/PointerEventPass;
    .end local v7    # "$this$awaitAllPointersUp":Landroidx/compose/ui/input/pointer/AwaitPointerEventScope;
    .local v2, "$result":Ljava/lang/Object;
    :cond_9b
    :goto_9b
    sget-object v3, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;

    return-object v3

    :pswitch_data_9e
    .packed-switch 0x0
        :pswitch_3f
        :pswitch_2f
    .end packed-switch
.end method
