.class public final Lsplitties/permissions/EnsureAllPermissionsKt;
.super Ljava/lang/Object;

.method public static final ensurePermissions(Landroid/app/Activity;Landroidx/fragment/app/FragmentManager;Landroidx/lifecycle/Lifecycle;[Ljava/lang/String;Lkotlin/jvm/functions/Function0;ZLkotlin/jvm/functions/Function0;Lkotlin/jvm/functions/Function0;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 26
    .param p0    # Landroid/app/Activity;
        .annotation build Lorg/jetbrains/annotations/NotNull;
        .end annotation
    .end param
    .param p1    # Landroidx/fragment/app/FragmentManager;
        .annotation build Lorg/jetbrains/annotations/NotNull;
        .end annotation
    .end param
    .param p2    # Landroidx/lifecycle/Lifecycle;
        .annotation build Lorg/jetbrains/annotations/NotNull;
        .end annotation
    .end param
    .param p3    # [Ljava/lang/String;
        .annotation build Lorg/jetbrains/annotations/NotNull;
        .end annotation
    .end param
    .param p4    # Lkotlin/jvm/functions/Function0;
        .annotation build Lorg/jetbrains/annotations/NotNull;
        .end annotation
    .end param
    .param p6    # Lkotlin/jvm/functions/Function0;
        .annotation build Lorg/jetbrains/annotations/NotNull;
        .end annotation
    .end param
    .param p7    # Lkotlin/jvm/functions/Function0;
        .annotation build Lorg/jetbrains/annotations/NotNull;
        .end annotation
    .end param
    .param p8    # Lkotlin/coroutines/Continuation;
        .annotation build Lorg/jetbrains/annotations/NotNull;
        .end annotation
    .end param
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "(",
            "Landroid/app/Activity;",
            "Landroidx/fragment/app/FragmentManager;",
            "Landroidx/lifecycle/Lifecycle;",
            "[",
            "Ljava/lang/String;",
            "Lkotlin/jvm/functions/Function0<",
            "Ljava/lang/Boolean;",
            ">;Z",
            "Lkotlin/jvm/functions/Function0<",
            "Ljava/lang/Boolean;",
            ">;",
            "Lkotlin/jvm/functions/Function0;",
            "Lkotlin/coroutines/Continuation<",
            "-",
            "Lkotlin/Unit;",
            ">;)",
            "Ljava/lang/Object;"
        }
    .end annotation

    .annotation build Lkotlin/jvm/JvmName;
        name = "ensurePermissions"
    .end annotation

    .annotation build Lkotlin/y;
    .end annotation

    .annotation build Lorg/jetbrains/annotations/Nullable;
    .end annotation

    .annotation build Lsplitties/experimental/a;
    .end annotation

    move-object/from16 v0, p8

    instance-of v1, v0, Lsplitties/permissions/EnsureAllPermissionsKt$ensureAllPermissions$7;

    if-eqz v1, :cond_15

    move-object v1, v0

    check-cast v1, Lsplitties/permissions/EnsureAllPermissionsKt$ensureAllPermissions$7;

    iget v2, v1, Lsplitties/permissions/EnsureAllPermissionsKt$ensureAllPermissions$7;->label:I

    const/high16 v3, -0x80000000

    and-int v4, v2, v3

    if-eqz v4, :cond_15

    sub-int/2addr v2, v3

    iput v2, v1, Lsplitties/permissions/EnsureAllPermissionsKt$ensureAllPermissions$7;->label:I

    goto :goto_1a

    :cond_15
    new-instance v1, Lsplitties/permissions/EnsureAllPermissionsKt$ensureAllPermissions$7;

    invoke-direct {v1, v0}, Lkotlin/coroutines/jvm/internal/ContinuationImpl;-><init>(Lkotlin/coroutines/Continuation;)V

    :goto_1a
    iget-object v0, v1, Lsplitties/permissions/EnsureAllPermissionsKt$ensureAllPermissions$7;->result:Ljava/lang/Object;

    invoke-static {}, Lkotlin/coroutines/intrinsics/a;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;

    move-result-object v2

    iget v3, v1, Lsplitties/permissions/EnsureAllPermissionsKt$ensureAllPermissions$7;->label:I

    const/4 v4, 0x2

    const/4 v6, 0x1

    if-eqz v3, :cond_94

    if-eq v3, v6, :cond_67

    if-ne v3, v4, :cond_5a

    iget v3, v1, Lsplitties/permissions/EnsureAllPermissionsKt$ensureAllPermissions$7;->I$0:I

    iget-boolean v7, v1, Lsplitties/permissions/EnsureAllPermissionsKt$ensureAllPermissions$7;->Z$0:Z

    iget-object v8, v1, Lsplitties/permissions/EnsureAllPermissionsKt$ensureAllPermissions$7;->L$6:Ljava/lang/Object;

    check-cast v8, Lkotlin/jvm/functions/Function0;

    iget-object v9, v1, Lsplitties/permissions/EnsureAllPermissionsKt$ensureAllPermissions$7;->L$5:Ljava/lang/Object;

    check-cast v9, Lkotlin/jvm/functions/Function0;

    iget-object v10, v1, Lsplitties/permissions/EnsureAllPermissionsKt$ensureAllPermissions$7;->L$4:Ljava/lang/Object;

    check-cast v10, Lkotlin/jvm/functions/Function0;

    iget-object v11, v1, Lsplitties/permissions/EnsureAllPermissionsKt$ensureAllPermissions$7;->L$3:Ljava/lang/Object;

    check-cast v11, [Ljava/lang/String;

    iget-object v12, v1, Lsplitties/permissions/EnsureAllPermissionsKt$ensureAllPermissions$7;->L$2:Ljava/lang/Object;

    check-cast v12, Landroidx/lifecycle/Lifecycle;

    iget-object v13, v1, Lsplitties/permissions/EnsureAllPermissionsKt$ensureAllPermissions$7;->L$1:Ljava/lang/Object;

    check-cast v13, Landroidx/fragment/app/FragmentManager;

    iget-object v14, v1, Lsplitties/permissions/EnsureAllPermissionsKt$ensureAllPermissions$7;->L$0:Ljava/lang/Object;

    check-cast v14, Landroid/app/Activity;

    invoke-static {v0}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V

    move-object v0, v12

    move-object v12, v1

    move-object v1, v13

    move v13, v3

    move-object v3, v0

    move-object v0, v9

    move v9, v7

    move-object v7, v11

    move-object v11, v8

    move-object v8, v10

    move-object v10, v0

    :goto_58
    move-object v0, v14

    goto :goto_a9

    :cond_5a
    new-instance v0, Ljava/lang/IllegalStateException;

    const v1, 0x624cfed3

    invoke-static {v1}, Lfixtures/obfuscation/StringDecoder;->decode(I)Ljava/lang/String;

    move-result-object v1

    invoke-direct {v0, v1}, Ljava/lang/IllegalStateException;-><init>(Ljava/lang/String;)V

    throw v0

    :cond_67
    iget v3, v1, Lsplitties/permissions/EnsureAllPermissionsKt$ensureAllPermissions$7;->I$0:I

    iget-boolean v7, v1, Lsplitties/permissions/EnsureAllPermissionsKt$ensureAllPermissions$7;->Z$0:Z

    iget-object v8, v1, Lsplitties/permissions/EnsureAllPermissionsKt$ensureAllPermissions$7;->L$6:Ljava/lang/Object;

    check-cast v8, Lkotlin/jvm/functions/Function0;

    iget-object v9, v1, Lsplitties/permissions/EnsureAllPermissionsKt$ensureAllPermissions$7;->L$5:Ljava/lang/Object;

    check-cast v9, Lkotlin/jvm/functions/Function0;

    iget-object v10, v1, Lsplitties/permissions/EnsureAllPermissionsKt$ensureAllPermissions$7;->L$4:Ljava/lang/Object;

    check-cast v10, Lkotlin/jvm/functions/Function0;

    iget-object v11, v1, Lsplitties/permissions/EnsureAllPermissionsKt$ensureAllPermissions$7;->L$3:Ljava/lang/Object;

    check-cast v11, [Ljava/lang/String;

    iget-object v12, v1, Lsplitties/permissions/EnsureAllPermissionsKt$ensureAllPermissions$7;->L$2:Ljava/lang/Object;

    check-cast v12, Landroidx/lifecycle/Lifecycle;

    iget-object v13, v1, Lsplitties/permissions/EnsureAllPermissionsKt$ensureAllPermissions$7;->L$1:Ljava/lang/Object;

    check-cast v13, Landroidx/fragment/app/FragmentManager;

    iget-object v14, v1, Lsplitties/permissions/EnsureAllPermissionsKt$ensureAllPermissions$7;->L$0:Ljava/lang/Object;

    check-cast v14, Landroid/app/Activity;

    invoke-static {v0}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V

    move-object v5, v9

    move v9, v7

    move-object v7, v11

    move-object v11, v8

    move-object v8, v10

    move-object v10, v5

    move-object v5, v1

    move-object v1, v13

    goto/16 :goto_107

    :cond_94
    invoke-static {v0}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V

    move-object/from16 v0, p0

    move-object/from16 v3, p2

    move-object/from16 v7, p3

    move-object/from16 v8, p4

    move/from16 v9, p5

    move-object/from16 v10, p6

    move-object/from16 v11, p7

    move-object v12, v1

    const/4 v13, 0x0

    move-object/from16 v1, p1

    :goto_a9
    array-length v14, v7

    const/4 v15, 0x0

    :goto_ab
    if-ge v15, v14, :cond_15b

    aget-object v16, v7, v15

    invoke-static/range {v16 .. v16}, Lsplitties/permissions/SuspendPermissionRequestKt;->hasPermission(Ljava/lang/String;)Z

    move-result v16

    xor-int/lit8 v16, v16, 0x1

    invoke-static/range {v16 .. v16}, Lkotlin/coroutines/jvm/internal/Boxing;->boxBoolean(Z)Ljava/lang/Boolean;

    move-result-object v16

    invoke-virtual/range {v16 .. v16}, Ljava/lang/Boolean;->booleanValue()Z

    move-result v16

    if-eqz v16, :cond_157

    if-gtz v13, :cond_db

    if-nez v9, :cond_db

    array-length v14, v7

    const/4 v15, 0x0

    :goto_c5
    if-ge v15, v14, :cond_e7

    aget-object v5, v7, v15

    invoke-virtual {v0, v5}, Landroid/app/Activity;->shouldShowRequestPermissionRationale(Ljava/lang/String;)Z

    move-result v5

    invoke-static {v5}, Lkotlin/coroutines/jvm/internal/Boxing;->boxBoolean(Z)Ljava/lang/Boolean;

    move-result-object v5

    invoke-virtual {v5}, Ljava/lang/Boolean;->booleanValue()Z

    move-result v5

    if-eqz v5, :cond_d8

    goto :goto_db

    :cond_d8
    add-int/lit8 v15, v15, 0x1

    goto :goto_c5

    :cond_db
    :goto_db
    invoke-interface {v8}, Lkotlin/jvm/functions/Function0;->invoke()Ljava/lang/Object;

    move-result-object v5

    check-cast v5, Ljava/lang/Boolean;

    invoke-virtual {v5}, Ljava/lang/Boolean;->booleanValue()Z

    move-result v5

    if-eqz v5, :cond_152

    :cond_e7
    iput-object v0, v12, Lsplitties/permissions/EnsureAllPermissionsKt$ensureAllPermissions$7;->L$0:Ljava/lang/Object;

    iput-object v1, v12, Lsplitties/permissions/EnsureAllPermissionsKt$ensureAllPermissions$7;->L$1:Ljava/lang/Object;

    iput-object v3, v12, Lsplitties/permissions/EnsureAllPermissionsKt$ensureAllPermissions$7;->L$2:Ljava/lang/Object;

    iput-object v7, v12, Lsplitties/permissions/EnsureAllPermissionsKt$ensureAllPermissions$7;->L$3:Ljava/lang/Object;

    iput-object v8, v12, Lsplitties/permissions/EnsureAllPermissionsKt$ensureAllPermissions$7;->L$4:Ljava/lang/Object;

    iput-object v10, v12, Lsplitties/permissions/EnsureAllPermissionsKt$ensureAllPermissions$7;->L$5:Ljava/lang/Object;

    iput-object v11, v12, Lsplitties/permissions/EnsureAllPermissionsKt$ensureAllPermissions$7;->L$6:Ljava/lang/Object;

    iput-boolean v9, v12, Lsplitties/permissions/EnsureAllPermissionsKt$ensureAllPermissions$7;->Z$0:Z

    iput v13, v12, Lsplitties/permissions/EnsureAllPermissionsKt$ensureAllPermissions$7;->I$0:I

    iput v6, v12, Lsplitties/permissions/EnsureAllPermissionsKt$ensureAllPermissions$7;->label:I

    invoke-static {v1, v3, v7, v12}, Lsplitties/permissions/SuspendPermissionRequestKt;->requestPermissions(Landroidx/fragment/app/FragmentManager;Landroidx/lifecycle/Lifecycle;[Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;

    move-result-object v5

    if-ne v5, v2, :cond_102

    goto :goto_143

    :cond_102
    move-object v14, v0

    move-object v0, v5

    move-object v5, v12

    move-object v12, v3

    move v3, v13

    :goto_107
    check-cast v0, Lsplitties/permissions/PermissionRequestResult;

    add-int/lit8 v13, v3, 0x1

    sget-object v3, Lsplitties/permissions/PermissionRequestResult$Granted;->INSTANCE:Lsplitties/permissions/PermissionRequestResult$Granted;

    invoke-static {v0, v3}, Lkotlin/jvm/internal/Intrinsics;->areEqual(Ljava/lang/Object;Ljava/lang/Object;)Z

    move-result v3

    if-eqz v3, :cond_114

    goto :goto_15b

    :cond_114
    instance-of v3, v0, Lsplitties/permissions/PermissionRequestResult$Denied$MayAskAgain;

    if-eqz v3, :cond_119

    goto :goto_14d

    :cond_119
    instance-of v0, v0, Lsplitties/permissions/PermissionRequestResult$Denied$DoNotAskAgain;

    if-eqz v0, :cond_14d

    invoke-interface {v10}, Lkotlin/jvm/functions/Function0;->invoke()Ljava/lang/Object;

    move-result-object v0

    check-cast v0, Ljava/lang/Boolean;

    invoke-virtual {v0}, Ljava/lang/Boolean;->booleanValue()Z

    move-result v0

    if-eqz v0, :cond_148

    iput-object v14, v5, Lsplitties/permissions/EnsureAllPermissionsKt$ensureAllPermissions$7;->L$0:Ljava/lang/Object;

    iput-object v1, v5, Lsplitties/permissions/EnsureAllPermissionsKt$ensureAllPermissions$7;->L$1:Ljava/lang/Object;

    iput-object v12, v5, Lsplitties/permissions/EnsureAllPermissionsKt$ensureAllPermissions$7;->L$2:Ljava/lang/Object;

    iput-object v7, v5, Lsplitties/permissions/EnsureAllPermissionsKt$ensureAllPermissions$7;->L$3:Ljava/lang/Object;

    iput-object v8, v5, Lsplitties/permissions/EnsureAllPermissionsKt$ensureAllPermissions$7;->L$4:Ljava/lang/Object;

    iput-object v10, v5, Lsplitties/permissions/EnsureAllPermissionsKt$ensureAllPermissions$7;->L$5:Ljava/lang/Object;

    iput-object v11, v5, Lsplitties/permissions/EnsureAllPermissionsKt$ensureAllPermissions$7;->L$6:Ljava/lang/Object;

    iput-boolean v9, v5, Lsplitties/permissions/EnsureAllPermissionsKt$ensureAllPermissions$7;->Z$0:Z

    iput v13, v5, Lsplitties/permissions/EnsureAllPermissionsKt$ensureAllPermissions$7;->I$0:I

    iput v4, v5, Lsplitties/permissions/EnsureAllPermissionsKt$ensureAllPermissions$7;->label:I

    invoke-static {v14, v12, v5}, Lsplitties/permissions/EnsurePermissionKt;->openApplicationDetailsSettingsAndAwaitResumed(Landroid/content/Context;Landroidx/lifecycle/Lifecycle;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;

    move-result-object v0

    if-ne v0, v2, :cond_144

    :goto_143
    return-object v2

    :cond_144
    move-object v3, v12

    move-object v12, v5

    goto/16 :goto_58

    :cond_148
    invoke-static {v11}, Lkotlin/time/j;->a(Lkotlin/jvm/functions/Function0;)Lkotlin/KotlinNothingValueException;

    move-result-object v0

    throw v0

    :cond_14d
    :goto_14d
    move-object v3, v12

    move-object v0, v14

    move-object v12, v5

    goto/16 :goto_a9

    :cond_152
    invoke-static {v11}, Lkotlin/time/j;->a(Lkotlin/jvm/functions/Function0;)Lkotlin/KotlinNothingValueException;

    move-result-object v0

    throw v0

    :cond_157
    add-int/lit8 v15, v15, 0x1

    goto/16 :goto_ab

    :cond_15b
    :goto_15b
    sget-object v0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;

    return-object v0
.end method
