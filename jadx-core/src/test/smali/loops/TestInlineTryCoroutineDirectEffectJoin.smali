.class public Lloops/TestInlineTryCoroutineDirectEffectJoin;
.super Ljava/lang/Object;

.method public static run(Landroidx/datastore/core/DataStore;Ljava/util/Iterator;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 18

    move-object/from16 v1, p2
    check-cast v1, Lloops/TestInlineTryCoroutineDirectEffectJoin$State;
    iget-object v0, v1, Lloops/TestInlineTryCoroutineDirectEffectJoin$State;->result:Ljava/lang/Object;
    invoke-static {}, Lkotlin/coroutines/intrinsics/a;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v3
    iget v4, v1, Lloops/TestInlineTryCoroutineDirectEffectJoin$State;->label:I
    const/4 v5, 0x4
    const/4 v6, 0x2
    const/4 v7, 0x1
    const/4 v9, 0x3

    if-eqz v4, :initial
    if-eq v4, v7, :other_state_one
    if-eq v4, v6, :other_state_two
    if-eq v4, v9, :resume_three
    if-ne v4, v5, :bad_state
    return-object v0

    :bad_state
    new-instance v2, Ljava/lang/IllegalStateException;
    invoke-direct {v2}, Ljava/lang/IllegalStateException;-><init>()V
    throw v2

    :resume_three
    iget v4, v1, Lloops/TestInlineTryCoroutineDirectEffectJoin$State;->I$1:I
    iget v7, v1, Lloops/TestInlineTryCoroutineDirectEffectJoin$State;->I$0:I
    iget-boolean v11, v1, Lloops/TestInlineTryCoroutineDirectEffectJoin$State;->Z$0:Z
    iget-object v12, v1, Lloops/TestInlineTryCoroutineDirectEffectJoin$State;->L$10:Ljava/lang/Object;
    check-cast v12, Ljava/util/List;
    iget-object v13, v1, Lloops/TestInlineTryCoroutineDirectEffectJoin$State;->L$6:Ljava/lang/Object;
    check-cast v13, Ljava/util/Iterator;
    iget-object v14, v1, Lloops/TestInlineTryCoroutineDirectEffectJoin$State;->L$5:Ljava/lang/Object;
    check-cast v14, Ljava/lang/Iterable;
    iget-object v2, v1, Lloops/TestInlineTryCoroutineDirectEffectJoin$State;->L$9:Ljava/lang/Object;
    move-object/from16 v17, v2
    check-cast v17, Ljava/lang/Iterable;
    iget-object v2, v1, Lloops/TestInlineTryCoroutineDirectEffectJoin$State;->L$8:Ljava/lang/Object;
    move-object/from16 v16, v2
    check-cast v16, Ljava/lang/Object;
    iget-object v15, v1, Lloops/TestInlineTryCoroutineDirectEffectJoin$State;->L$4:Ljava/lang/Object;
    check-cast v15, Ljava/lang/Object;
    iget-object v5, v1, Lloops/TestInlineTryCoroutineDirectEffectJoin$State;->L$3:Ljava/lang/Object;
    check-cast v5, Ljava/lang/Object;
    iget-object v8, v1, Lloops/TestInlineTryCoroutineDirectEffectJoin$State;->L$2:Ljava/lang/Object;
    check-cast v8, Ljava/lang/String;
    iget-object v6, v1, Lloops/TestInlineTryCoroutineDirectEffectJoin$State;->L$1:Ljava/lang/Object;
    check-cast v6, Ljava/lang/String;
    iget-object v10, v1, Lloops/TestInlineTryCoroutineDirectEffectJoin$State;->L$0:Ljava/lang/Object;
    check-cast v10, Landroidx/datastore/core/DataStore;
    const/4 v2, 0x0

    :try_resume_start
    invoke-static {v0}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    :try_resume_end
    goto :effect_join

    :other_state_two
    return-object v0

    :other_state_one
    return-object v0

    :initial
    :try_body_start
    invoke-static {v0}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    const/4 v4, 0x0
    const/4 v5, 0x0
    const/4 v6, 0x0
    const-string v8, "new"
    const/4 v10, 0x0
    const/4 v11, 0x0
    move-object/from16 v12, p1
    move-object/from16 v14, p0
    move-object/from16 v15, p0
    move-object/from16 v16, p0
    move-object/from16 v17, p1
    goto :loop_header

    :loop_header
    invoke-interface {v12}, Ljava/util/Iterator;->hasNext()Z
    move-result v0
    if-eqz v0, :done

    invoke-interface {v12}, Ljava/util/Iterator;->next()Ljava/lang/Object;
    move-result-object v0
    check-cast v0, Ljava/util/List;
    move-object v6, v0
    move v11, v4

    iput-object v10, v1, Lloops/TestInlineTryCoroutineDirectEffectJoin$State;->L$0:Ljava/lang/Object;
    iput-object v6, v1, Lloops/TestInlineTryCoroutineDirectEffectJoin$State;->L$1:Ljava/lang/Object;
    iput-object v8, v1, Lloops/TestInlineTryCoroutineDirectEffectJoin$State;->L$2:Ljava/lang/Object;
    iput-object v5, v1, Lloops/TestInlineTryCoroutineDirectEffectJoin$State;->L$3:Ljava/lang/Object;
    iput-object v15, v1, Lloops/TestInlineTryCoroutineDirectEffectJoin$State;->L$4:Ljava/lang/Object;
    iput-object v14, v1, Lloops/TestInlineTryCoroutineDirectEffectJoin$State;->L$5:Ljava/lang/Object;
    iput-object v12, v1, Lloops/TestInlineTryCoroutineDirectEffectJoin$State;->L$6:Ljava/lang/Object;
    move-object/16 v2, v16
    iput-object v2, v1, Lloops/TestInlineTryCoroutineDirectEffectJoin$State;->L$8:Ljava/lang/Object;
    move-object/16 v2, v17
    iput-object v2, v1, Lloops/TestInlineTryCoroutineDirectEffectJoin$State;->L$9:Ljava/lang/Object;
    iput-object v0, v1, Lloops/TestInlineTryCoroutineDirectEffectJoin$State;->L$10:Ljava/lang/Object;
    iput-boolean v11, v1, Lloops/TestInlineTryCoroutineDirectEffectJoin$State;->Z$0:Z
    iput v11, v1, Lloops/TestInlineTryCoroutineDirectEffectJoin$State;->I$0:I
    iput v4, v1, Lloops/TestInlineTryCoroutineDirectEffectJoin$State;->I$1:I
    iput v9, v1, Lloops/TestInlineTryCoroutineDirectEffectJoin$State;->label:I

    invoke-static {v14, v5, v1}, Landroidx/datastore/preferences/core/PreferencesKt;->edit(Landroidx/datastore/core/DataStore;Lkotlin/jvm/functions/Function2;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v2
    if-eq v2, v3, :suspended

    move v7, v4
    move-object v13, v12
    move-object/from16 v14, v17
    move-object v12, v0
    move-object v6, v8
    move v11, v10
    move-object v8, v15
    move-object/from16 v15, v16

    :effect_join
    new-instance v0, Ljava/lang/StringBuilder;
    invoke-direct {v0}, Ljava/lang/StringBuilder;-><init>()V
    const-string v2, "migration complete "
    invoke-virtual {v0, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
    invoke-interface {v12}, Ljava/util/List;->size()I
    move-result v2
    invoke-virtual {v0, v2}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;
    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;
    move-result-object v0
    invoke-static {v0}, Ljava/lang/System;->getProperty(Ljava/lang/String;)Ljava/lang/String;

    move v4, v7
    move-object v5, v15
    move-object v6, v8
    move v10, v11
    move-object v12, v13
    move-object v14, v14
    move-object v15, v15
    move-object/from16 v16, v15
    move-object/from16 v17, v14
    goto :loop_header

    :suspended
    return-object v3

    :done
    sget-object v0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    :try_body_end
    return-object v0

    :failure
    move-exception v0
    throw v0

    .catchall {:try_resume_start .. :try_resume_end} :failure
    .catchall {:try_body_start .. :try_body_end} :failure
.end method

.method public static runPureLatch(Landroidx/datastore/core/DataStore;Ljava/util/Iterator;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 10

    move-object/from16 v1, p2
    check-cast v1, Lloops/TestInlineTryCoroutineDirectEffectJoin$State;
    iget-object v0, v1, Lloops/TestInlineTryCoroutineDirectEffectJoin$State;->result:Ljava/lang/Object;
    invoke-static {}, Lkotlin/coroutines/intrinsics/a;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v3
    iget v4, v1, Lloops/TestInlineTryCoroutineDirectEffectJoin$State;->label:I
    const/4 v9, 0x2

    if-eqz v4, :pure_initial
    if-ne v4, v9, :pure_bad_state

    iget v5, v1, Lloops/TestInlineTryCoroutineDirectEffectJoin$State;->I$0:I
    iget-object v6, v1, Lloops/TestInlineTryCoroutineDirectEffectJoin$State;->L$0:Ljava/lang/Object;
    check-cast v6, Ljava/util/Iterator;
    :pure_try_resume_start
    invoke-static {v0}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    :pure_try_resume_end
    move v7, v5
    move-object v8, v6
    goto :pure_latch

    :pure_bad_state
    new-instance v2, Ljava/lang/IllegalStateException;
    invoke-direct {v2}, Ljava/lang/IllegalStateException;-><init>()V
    throw v2

    :pure_initial
    :pure_try_body_start
    invoke-static {v0}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    move-object/from16 v6, p1
    const/4 v5, 0x0

    :pure_loop_header
    invoke-interface {v6}, Ljava/util/Iterator;->hasNext()Z
    move-result v0
    if-eqz v0, :pure_done
    invoke-interface {v6}, Ljava/util/Iterator;->next()Ljava/lang/Object;
    move-result-object v0

    iput-object v6, v1, Lloops/TestInlineTryCoroutineDirectEffectJoin$State;->L$0:Ljava/lang/Object;
    iput v5, v1, Lloops/TestInlineTryCoroutineDirectEffectJoin$State;->I$0:I
    iput v9, v1, Lloops/TestInlineTryCoroutineDirectEffectJoin$State;->label:I
    invoke-static {p0, v0, v1}, Landroidx/datastore/preferences/core/PreferencesKt;->edit(Landroidx/datastore/core/DataStore;Lkotlin/jvm/functions/Function2;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v2
    if-eq v2, v3, :pure_suspended

    move v7, v5
    move-object v8, v6

    :pure_latch
    const/4 v0, 0x1
    move v5, v7
    move-object v6, v8
    goto :pure_loop_header

    :pure_suspended
    return-object v3

    :pure_done
    sget-object v0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    :pure_try_body_end
    return-object v0

    :pure_failure
    move-exception v0
    throw v0

    .catchall {:pure_try_resume_start .. :pure_try_resume_end} :pure_failure
    .catchall {:pure_try_body_start .. :pure_try_body_end} :pure_failure
.end method
