.class public Ltrycatch/TestSnapshotFlowSharedCleanup;
.super Lkotlin/coroutines/jvm/internal/SuspendLambda;

.field final synthetic $block:Lkotlin/jvm/functions/Function0;
.field final synthetic $externalManager:Landroidx/compose/runtime/SnapshotFlowManager;
.field private synthetic L$0:Ljava/lang/Object;
.field L$1:Ljava/lang/Object;
.field L$2:Ljava/lang/Object;
.field L$3:Ljava/lang/Object;
.field label:I

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 11

    #@0
    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;

    #@3
    move-result-object v0

    #@4
    .line 469
    iget v1, p0, Ltrycatch/TestSnapshotFlowSharedCleanup;->label:I

    #@6
    packed-switch v1, :pswitch_data_d6

    #@9
    .end local p0    # "this":Ltrycatch/TestSnapshotFlowSharedCleanup;
    new-instance p1, Ljava/lang/IllegalStateException;

    #@b
    const-string v0, "call to \'resume\' before \'invoke\' with coroutine"

    #@d
    invoke-direct {p1, v0}, Ljava/lang/IllegalStateException;-><init>(Ljava/lang/String;)V

    #@10
    throw p1

    #@11
    .restart local p0    # "this":Ltrycatch/TestSnapshotFlowSharedCleanup;
    .local p1, "$result":Ljava/lang/Object;
    :pswitch_11
    iget-object v1, p0, Ltrycatch/TestSnapshotFlowSharedCleanup;->L$3:Ljava/lang/Object;

    #@13
    .local v1, "lastValue":Ljava/lang/Object;
    iget-object v2, p0, Ltrycatch/TestSnapshotFlowSharedCleanup;->L$2:Ljava/lang/Object;

    #@15
    check-cast v2, Lkotlinx/coroutines/channels/Channel;

    #@17
    .local v2, "needToRerunBlock":Lkotlinx/coroutines/channels/Channel;
    iget-object v3, p0, Ltrycatch/TestSnapshotFlowSharedCleanup;->L$1:Ljava/lang/Object;

    #@19
    check-cast v3, Landroidx/compose/runtime/SnapshotFlowManager;

    #@1b
    .local v3, "manager":Landroidx/compose/runtime/SnapshotFlowManager;
    iget-object v4, p0, Ltrycatch/TestSnapshotFlowSharedCleanup;->L$0:Ljava/lang/Object;

    #@1d
    check-cast v4, Lkotlinx/coroutines/flow/FlowCollector;

    #@1f
    .local v4, "$this$flow":Lkotlinx/coroutines/flow/FlowCollector;
    :try_start_1f
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    :try_end_22
    .catchall {:try_start_1f .. :try_end_22} :catchall_4a

    #@22
    move-object v5, p0

    #@23
    goto/16 :goto_c2

    #@25
    .end local v1    # "lastValue":Ljava/lang/Object;
    .end local v2    # "needToRerunBlock":Lkotlinx/coroutines/channels/Channel;
    .end local v3    # "manager":Landroidx/compose/runtime/SnapshotFlowManager;
    .end local v4    # "$this$flow":Lkotlinx/coroutines/flow/FlowCollector;
    :pswitch_25
    iget-object v1, p0, Ltrycatch/TestSnapshotFlowSharedCleanup;->L$3:Ljava/lang/Object;

    #@27
    .restart local v1    # "lastValue":Ljava/lang/Object;
    iget-object v2, p0, Ltrycatch/TestSnapshotFlowSharedCleanup;->L$2:Ljava/lang/Object;

    #@29
    check-cast v2, Lkotlinx/coroutines/channels/Channel;

    #@2b
    .restart local v2    # "needToRerunBlock":Lkotlinx/coroutines/channels/Channel;
    iget-object v3, p0, Ltrycatch/TestSnapshotFlowSharedCleanup;->L$1:Ljava/lang/Object;

    #@2d
    check-cast v3, Landroidx/compose/runtime/SnapshotFlowManager;

    #@2f
    .restart local v3    # "manager":Landroidx/compose/runtime/SnapshotFlowManager;
    iget-object v4, p0, Ltrycatch/TestSnapshotFlowSharedCleanup;->L$0:Ljava/lang/Object;

    #@31
    check-cast v4, Lkotlinx/coroutines/flow/FlowCollector;

    #@33
    .restart local v4    # "$this$flow":Lkotlinx/coroutines/flow/FlowCollector;
    :try_start_33
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    :try_end_36
    .catchall {:try_start_33 .. :try_end_36} :catchall_4a

    #@36
    move-object v5, p0

    #@37
    goto :goto_9d

    #@38
    .end local v1    # "lastValue":Ljava/lang/Object;
    .end local v2    # "needToRerunBlock":Lkotlinx/coroutines/channels/Channel;
    .end local v3    # "manager":Landroidx/compose/runtime/SnapshotFlowManager;
    .end local v4    # "$this$flow":Lkotlinx/coroutines/flow/FlowCollector;
    :pswitch_38
    iget-object v1, p0, Ltrycatch/TestSnapshotFlowSharedCleanup;->L$3:Ljava/lang/Object;

    #@3a
    .restart local v1    # "lastValue":Ljava/lang/Object;
    iget-object v2, p0, Ltrycatch/TestSnapshotFlowSharedCleanup;->L$2:Ljava/lang/Object;

    #@3c
    check-cast v2, Lkotlinx/coroutines/channels/Channel;

    #@3e
    .restart local v2    # "needToRerunBlock":Lkotlinx/coroutines/channels/Channel;
    iget-object v3, p0, Ltrycatch/TestSnapshotFlowSharedCleanup;->L$1:Ljava/lang/Object;

    #@40
    check-cast v3, Landroidx/compose/runtime/SnapshotFlowManager;

    #@42
    .restart local v3    # "manager":Landroidx/compose/runtime/SnapshotFlowManager;
    iget-object v4, p0, Ltrycatch/TestSnapshotFlowSharedCleanup;->L$0:Ljava/lang/Object;

    #@44
    check-cast v4, Lkotlinx/coroutines/flow/FlowCollector;

    #@46
    .restart local v4    # "$this$flow":Lkotlinx/coroutines/flow/FlowCollector;
    :try_start_46
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    :try_end_49
    .catchall {:try_start_46 .. :try_end_49} :catchall_4a

    #@49
    goto :goto_87

    #@4a
    .line 488
    .end local v1    # "lastValue":Ljava/lang/Object;
    .end local v4    # "$this$flow":Lkotlinx/coroutines/flow/FlowCollector;
    :catchall_4a
    move-exception v0

    #@4b
    move-object v5, p0

    #@4c
    goto/16 :goto_c8

    #@4e
    .line 469
    .end local v2    # "needToRerunBlock":Lkotlinx/coroutines/channels/Channel;
    .end local v3    # "manager":Landroidx/compose/runtime/SnapshotFlowManager;
    .end local p1    # "$result":Ljava/lang/Object;
    :pswitch_4e
    invoke-static {p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V

    #@51
    .restart local p1    # "$result":Ljava/lang/Object;
    iget-object v1, p0, Ltrycatch/TestSnapshotFlowSharedCleanup;->L$0:Ljava/lang/Object;

    #@53
    move-object v4, v1

    #@54
    check-cast v4, Lkotlinx/coroutines/flow/FlowCollector;

    #@56
    .line 470
    .restart local v4    # "$this$flow":Lkotlinx/coroutines/flow/FlowCollector;
    iget-object v1, p0, Ltrycatch/TestSnapshotFlowSharedCleanup;->$externalManager:Landroidx/compose/runtime/SnapshotFlowManager;

    #@58
    if-nez v1, :cond_5f

    #@5a
    new-instance v1, Landroidx/compose/runtime/SnapshotFlowManager;

    #@5c
    invoke-direct {v1}, Landroidx/compose/runtime/SnapshotFlowManager;-><init>()V

    #@5f
    :cond_5f
    move-object v3, v1

    #@60
    .line 472
    .restart local v3    # "manager":Landroidx/compose/runtime/SnapshotFlowManager;
    const/4 v1, 0x6

    #@61
    const/4 v2, 0x1

    #@62
    const/4 v5, 0x0

    #@63
    invoke-static {v2, v5, v5, v1, v5}, Lkotlinx/coroutines/channels/ChannelKt;->Channel$default(ILkotlinx/coroutines/channels/BufferOverflow;Lkotlin/jvm/functions/Function1;ILjava/lang/Object;)Lkotlinx/coroutines/channels/Channel;

    #@66
    move-result-object v1

    #@67
    .line 474
    .local v1, "needToRerunBlock":Lkotlinx/coroutines/channels/Channel;
    nop

    #@68
    .line 475
    :try_start_68
    move-object v5, v1

    #@69
    check-cast v5, Lkotlinx/coroutines/channels/SendChannel;

    #@6b
    iget-object v6, p0, Ltrycatch/TestSnapshotFlowSharedCleanup;->$block:Lkotlin/jvm/functions/Function0;

    #@6d
    invoke-virtual {v3, v5, v6}, Landroidx/compose/runtime/SnapshotFlowManager;->runAndWatch$runtime(Lkotlinx/coroutines/channels/SendChannel;Lkotlin/jvm/functions/Function0;)Ljava/lang/Object;

    #@70
    move-result-object v5

    #@71
    .line 476
    .local v5, "lastValue":Ljava/lang/Object;
    move-object v6, p0

    #@72
    check-cast v6, Lkotlin/coroutines/Continuation;

    #@74
    iput-object v4, p0, Ltrycatch/TestSnapshotFlowSharedCleanup;->L$0:Ljava/lang/Object;

    #@76
    iput-object v3, p0, Ltrycatch/TestSnapshotFlowSharedCleanup;->L$1:Ljava/lang/Object;

    #@78
    iput-object v1, p0, Ltrycatch/TestSnapshotFlowSharedCleanup;->L$2:Ljava/lang/Object;

    #@7a
    iput-object v5, p0, Ltrycatch/TestSnapshotFlowSharedCleanup;->L$3:Ljava/lang/Object;

    #@7c
    iput v2, p0, Ltrycatch/TestSnapshotFlowSharedCleanup;->label:I

    #@7e
    invoke-interface {v4, v5, v6}, Lkotlinx/coroutines/flow/FlowCollector;->emit(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;

    #@81
    move-result-object v2
    :try_end_82
    .catchall {:try_start_68 .. :try_end_82} :catchall_c5

    #@82
    if-ne v2, v0, :cond_85

    #@84
    .line 469
    return-object v0

    #@85
    .line 476
    :cond_85
    move-object v2, v1

    #@86
    move-object v1, v5

    #@87
    .line 478
    .end local v5    # "lastValue":Ljava/lang/Object;
    .local v1, "lastValue":Ljava/lang/Object;
    .restart local v2    # "needToRerunBlock":Lkotlinx/coroutines/channels/Channel;
    :goto_87
    move-object v5, p0

    #@88
    .line 479
    .end local p0    # "this":Ltrycatch/TestSnapshotFlowSharedCleanup;
    .local v5, "this":Ltrycatch/TestSnapshotFlowSharedCleanup;
    :cond_88
    :goto_88
    :try_start_88
    move-object v6, v5

    #@89
    check-cast v6, Lkotlin/coroutines/Continuation;

    #@8b
    iput-object v4, v5, Ltrycatch/TestSnapshotFlowSharedCleanup;->L$0:Ljava/lang/Object;

    #@8d
    iput-object v3, v5, Ltrycatch/TestSnapshotFlowSharedCleanup;->L$1:Ljava/lang/Object;

    #@8f
    iput-object v2, v5, Ltrycatch/TestSnapshotFlowSharedCleanup;->L$2:Ljava/lang/Object;

    #@91
    iput-object v1, v5, Ltrycatch/TestSnapshotFlowSharedCleanup;->L$3:Ljava/lang/Object;

    #@93
    const/4 v7, 0x2

    #@94
    iput v7, v5, Ltrycatch/TestSnapshotFlowSharedCleanup;->label:I

    #@96
    invoke-interface {v2, v6}, Lkotlinx/coroutines/channels/Channel;->receive(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;

    #@99
    move-result-object v6

    #@9a
    if-ne v6, v0, :cond_9d

    #@9c
    .line 469
    return-object v0

    #@9d
    .line 481
    :cond_9d
    :goto_9d
    move-object v6, v2

    #@9e
    check-cast v6, Lkotlinx/coroutines/channels/SendChannel;

    #@a0
    iget-object v7, v5, Ltrycatch/TestSnapshotFlowSharedCleanup;->$block:Lkotlin/jvm/functions/Function0;

    #@a2
    invoke-virtual {v3, v6, v7}, Landroidx/compose/runtime/SnapshotFlowManager;->runAndWatch$runtime(Lkotlinx/coroutines/channels/SendChannel;Lkotlin/jvm/functions/Function0;)Ljava/lang/Object;

    #@a5
    move-result-object v6

    #@a6
    .line 482
    .local v6, "newValue":Ljava/lang/Object;
    invoke-static {v6, v1}, Lkotlin/jvm/internal/Intrinsics;->areEqual(Ljava/lang/Object;Ljava/lang/Object;)Z

    #@a9
    move-result v7

    #@aa
    if-nez v7, :cond_88

    #@ac
    .line 483
    move-object v1, v6

    #@ad
    .line 484
    move-object v7, v5

    #@ae
    check-cast v7, Lkotlin/coroutines/Continuation;

    #@b0
    iput-object v4, v5, Ltrycatch/TestSnapshotFlowSharedCleanup;->L$0:Ljava/lang/Object;

    #@b2
    iput-object v3, v5, Ltrycatch/TestSnapshotFlowSharedCleanup;->L$1:Ljava/lang/Object;

    #@b4
    iput-object v2, v5, Ltrycatch/TestSnapshotFlowSharedCleanup;->L$2:Ljava/lang/Object;

    #@b6
    iput-object v1, v5, Ltrycatch/TestSnapshotFlowSharedCleanup;->L$3:Ljava/lang/Object;

    #@b8
    const/4 v8, 0x3

    #@b9
    iput v8, v5, Ltrycatch/TestSnapshotFlowSharedCleanup;->label:I

    #@bb
    invoke-interface {v4, v6, v7}, Lkotlinx/coroutines/flow/FlowCollector;->emit(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;

    #@be
    move-result-object v7
    :try_end_bf
    .catchall {:try_start_88 .. :try_end_bf} :catchall_c3

    #@bf
    .end local v6    # "newValue":Ljava/lang/Object;
    if-ne v7, v0, :cond_c2

    #@c1
    .line 469
    return-object v0

    #@c2
    .line 484
    :cond_c2
    :goto_c2
    goto :goto_88

    #@c3
    .line 488
    .end local v1    # "lastValue":Ljava/lang/Object;
    .end local v4    # "$this$flow":Lkotlinx/coroutines/flow/FlowCollector;
    :catchall_c3
    move-exception v0

    #@c4
    goto :goto_c8

    #@c5
    .end local v2    # "needToRerunBlock":Lkotlinx/coroutines/channels/Channel;
    .end local v5    # "this":Ltrycatch/TestSnapshotFlowSharedCleanup;
    .local v1, "needToRerunBlock":Lkotlinx/coroutines/channels/Channel;
    :catchall_c5
    move-exception v0

    #@c6
    move-object v5, p0

    #@c7
    move-object v2, v1

    #@c8
    .end local v1    # "needToRerunBlock":Lkotlinx/coroutines/channels/Channel;
    .restart local v2    # "needToRerunBlock":Lkotlinx/coroutines/channels/Channel;
    .restart local v5    # "this":Ltrycatch/TestSnapshotFlowSharedCleanup;
    :goto_c8
    move-object v1, v2

    #@c9
    check-cast v1, Lkotlinx/coroutines/channels/SendChannel;

    #@cb
    invoke-virtual {v3, v1}, Landroidx/compose/runtime/SnapshotFlowManager;->reportSnapshotFlowCancellation$runtime(Lkotlinx/coroutines/channels/SendChannel;)V

    #@ce
    .line 489
    .end local v2    # "needToRerunBlock":Lkotlinx/coroutines/channels/Channel;
    iget-object v1, v5, Ltrycatch/TestSnapshotFlowSharedCleanup;->$externalManager:Landroidx/compose/runtime/SnapshotFlowManager;

    #@d0
    if-nez v1, :cond_d5

    #@d2
    .line 491
    invoke-virtual {v3}, Landroidx/compose/runtime/SnapshotFlowManager;->dispose()V

    #@d5
    .end local v3    # "manager":Landroidx/compose/runtime/SnapshotFlowManager;
    :cond_d5
    throw v0

    #@d6
    :pswitch_data_d6
    .packed-switch 0x0
        :pswitch_4e
        :pswitch_38
        :pswitch_25
        :pswitch_11
    .end packed-switch
.end method
