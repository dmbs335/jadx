.class public final Lloops/DeepRecursiveObjectReader;
.super Ljava/lang/Object;

.field public final a:Lkotlinx/serialization/json/internal/AbstractJsonLexer;
.field public final b:Z
.field public final c:Z

.method public final c(Lkotlin/DeepRecursiveScope;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 24

    move-object/from16 v0, p0
    move-object/from16 v1, p2
    instance-of v2, v1, Lloops/DeepRecursiveObjectReader$State;
    if-eqz v2, :new_continuation

    move-object v2, v1
    check-cast v2, Lloops/DeepRecursiveObjectReader$State;
    iget v3, v2, Lloops/DeepRecursiveObjectReader$State;->label:I
    const/high16 v4, -0x80000000
    and-int v5, v3, v4
    if-eqz v5, :new_continuation
    sub-int/2addr v3, v4
    iput v3, v2, Lloops/DeepRecursiveObjectReader$State;->label:I
    goto :dispatch

    :new_continuation
    new-instance v2, Lloops/DeepRecursiveObjectReader$State;
    invoke-direct {v2, v0, v1}, Lloops/DeepRecursiveObjectReader$State;-><init>(Lloops/DeepRecursiveObjectReader;Lkotlin/coroutines/Continuation;)V

    :dispatch
    iget-object v1, v2, Lloops/DeepRecursiveObjectReader$State;->result:Ljava/lang/Object;
    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v3
    iget v4, v2, Lloops/DeepRecursiveObjectReader$State;->label:I
    const/4 v5, 0x6
    const/4 v6, 0x0
    const/4 v7, 0x7
    const/4 v8, 0x4
    const/4 v9, 0x1
    if-eqz v4, :initial
    if-ne v4, v9, :bad_state

    iget v4, v2, Lloops/DeepRecursiveObjectReader$State;->I$0:I
    iget-object v10, v2, Lloops/DeepRecursiveObjectReader$State;->L$3:Ljava/lang/Object;
    check-cast v10, Ljava/lang/String;
    iget-object v11, v2, Lloops/DeepRecursiveObjectReader$State;->L$2:Ljava/lang/Object;
    check-cast v11, Ljava/util/LinkedHashMap;
    iget-object v12, v2, Lloops/DeepRecursiveObjectReader$State;->L$1:Ljava/lang/Object;
    check-cast v12, Lloops/DeepRecursiveObjectReader;
    iget-object v13, v2, Lloops/DeepRecursiveObjectReader$State;->L$0:Ljava/lang/Object;
    check-cast v13, Lkotlin/DeepRecursiveScope;
    invoke-static {v1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    move/from16 v20, v4
    move-object v4, v2
    move/from16 v2, v20
    goto :value_join

    :bad_state
    new-instance v1, Ljava/lang/IllegalStateException;
    invoke-direct {v1}, Ljava/lang/IllegalStateException;-><init>()V
    throw v1

    :initial
    invoke-static {v1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    iget-object v1, v0, Lloops/DeepRecursiveObjectReader;->a:Lkotlinx/serialization/json/internal/AbstractJsonLexer;
    invoke-virtual {v1, v5}, Lkotlinx/serialization/json/internal/AbstractJsonLexer;->consumeNextToken(B)B
    move-result v1
    new-instance v4, Ljava/util/LinkedHashMap;
    invoke-direct {v4}, Ljava/util/LinkedHashMap;-><init>()V
    move-object v12, v0
    move-object v11, v4
    move v10, v6
    move-object v4, v2
    move v2, v1
    move-object/from16 v1, p1

    :loop_header
    iget-object v13, v12, Lloops/DeepRecursiveObjectReader;->a:Lkotlinx/serialization/json/internal/AbstractJsonLexer;
    invoke-virtual {v13}, Lkotlinx/serialization/json/internal/AbstractJsonLexer;->canConsumeValue()Z
    move-result v13
    if-eqz v13, :done
    iget-boolean v13, v12, Lloops/DeepRecursiveObjectReader;->b:Z
    if-eqz v13, :strict_key
    iget-object v13, v12, Lloops/DeepRecursiveObjectReader;->a:Lkotlinx/serialization/json/internal/AbstractJsonLexer;
    invoke-virtual {v13}, Lkotlinx/serialization/json/internal/AbstractJsonLexer;->consumeStringLenient()Ljava/lang/String;
    move-result-object v13
    goto :key_ready

    :strict_key
    iget-object v13, v12, Lloops/DeepRecursiveObjectReader;->a:Lkotlinx/serialization/json/internal/AbstractJsonLexer;
    invoke-virtual {v13}, Lkotlinx/serialization/json/internal/AbstractJsonLexer;->consumeString()Ljava/lang/String;
    move-result-object v13

    :key_ready
    iget-object v14, v12, Lloops/DeepRecursiveObjectReader;->a:Lkotlinx/serialization/json/internal/AbstractJsonLexer;
    const/4 v15, 0x5
    invoke-virtual {v14, v15}, Lkotlinx/serialization/json/internal/AbstractJsonLexer;->consumeNextToken(B)B
    sget-object v14, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    iput-object v1, v4, Lloops/DeepRecursiveObjectReader$State;->L$0:Ljava/lang/Object;
    iput-object v12, v4, Lloops/DeepRecursiveObjectReader$State;->L$1:Ljava/lang/Object;
    iput-object v11, v4, Lloops/DeepRecursiveObjectReader$State;->L$2:Ljava/lang/Object;
    iput-object v13, v4, Lloops/DeepRecursiveObjectReader$State;->L$3:Ljava/lang/Object;
    iput v10, v4, Lloops/DeepRecursiveObjectReader$State;->I$0:I
    iput-byte v2, v4, Lloops/DeepRecursiveObjectReader$State;->B$0:B
    iput v6, v4, Lloops/DeepRecursiveObjectReader$State;->I$1:I
    iput v9, v4, Lloops/DeepRecursiveObjectReader$State;->label:I
    invoke-virtual {v1, v14, v4}, Lkotlin/DeepRecursiveScope;->callRecursive(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v2
    if-ne v2, v3, :direct_value
    return-object v3

    :direct_value
    move-object/from16 v20, v13
    move-object v13, v1
    move-object v1, v2
    move v2, v10
    move-object/from16 v10, v20

    :value_join
    check-cast v1, Lkotlinx/serialization/json/JsonElement;
    invoke-interface {v11, v10, v1}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    iget-object v1, v12, Lloops/DeepRecursiveObjectReader;->a:Lkotlinx/serialization/json/internal/AbstractJsonLexer;
    invoke-virtual {v1}, Lkotlinx/serialization/json/internal/AbstractJsonLexer;->consumeNextToken()B
    move-result v1
    if-ne v1, v7, :continue_loop
    move v2, v1
    goto :done

    :continue_loop
    move v10, v2
    move v2, v1
    move-object v1, v13
    goto :loop_header

    :done
    new-instance v1, Lkotlinx/serialization/json/JsonObject;
    invoke-direct {v1, v11}, Lkotlinx/serialization/json/JsonObject;-><init>(Ljava/util/Map;)V
    return-object v1
.end method
