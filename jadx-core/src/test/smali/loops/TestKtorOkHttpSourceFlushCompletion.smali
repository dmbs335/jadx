.class public final Lloops/TestKtorOkHttpSourceFlushCompletion;
.super Lkotlin/coroutines/jvm/internal/SuspendLambda;

.field final synthetic $context:Lkotlin/coroutines/CoroutineContext;
.field final synthetic $requestData:Lio/ktor/client/request/HttpRequestData;
.field final synthetic $this_toChannel:Lokio/BufferedSource;
.field I$0:I
.field I$1:I
.field private synthetic L$0:Ljava/lang/Object;
.field L$1:Ljava/lang/Object;
.field L$2:Ljava/lang/Object;
.field L$3:Ljava/lang/Object;
.field L$4:Ljava/lang/Object;
.field L$5:Ljava/lang/Object;
.field label:I

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .registers 19

    move-object/from16 v3, p0
    iget-object v0, v3, Lloops/TestKtorOkHttpSourceFlushCompletion;->L$0:Ljava/lang/Object;
    check-cast v0, Lio/ktor/utils/io/WriterScope;
    invoke-static {}, Lkotlin/coroutines/intrinsics/a;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v6
    iget v1, v3, Lloops/TestKtorOkHttpSourceFlushCompletion;->label:I
    const/4 v7, 0x2
    const/4 v8, 0x1
    if-eqz v1, :initial
    if-eq v1, v8, :resume_write
    if-ne v1, v7, :bad_state

    iget v1, v3, Lloops/TestKtorOkHttpSourceFlushCompletion;->I$1:I
    iget v2, v3, Lloops/TestKtorOkHttpSourceFlushCompletion;->I$0:I
    iget-object v4, v3, Lloops/TestKtorOkHttpSourceFlushCompletion;->L$5:Ljava/lang/Object;
    check-cast v4, Lkotlin/jvm/internal/Ref$IntRef;
    iget-object v5, v3, Lloops/TestKtorOkHttpSourceFlushCompletion;->L$4:Ljava/lang/Object;
    check-cast v5, Lokio/BufferedSource;
    iget-object v9, v3, Lloops/TestKtorOkHttpSourceFlushCompletion;->L$3:Ljava/lang/Object;
    check-cast v9, Lio/ktor/client/request/HttpRequestData;
    iget-object v10, v3, Lloops/TestKtorOkHttpSourceFlushCompletion;->L$2:Ljava/lang/Object;
    check-cast v10, Lkotlin/coroutines/CoroutineContext;
    iget-object v11, v3, Lloops/TestKtorOkHttpSourceFlushCompletion;->L$1:Ljava/lang/Object;
    check-cast v11, Ljava/io/Closeable;
    :try_start_resume_flush
    invoke-static/range {p1 .. p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    :try_end_resume_flush
    .catchall {:try_start_resume_flush .. :try_end_resume_flush} :body_error

    :resume_flush_join
    move v14, v1
    move v13, v2
    move-object v12, v4
    move-object v15, v5
    move-object v1, v11
    move-object v11, v9
    move-object v9, v0
    goto :loop_test

    :bad_state
    new-instance v0, Ljava/lang/IllegalStateException;
    const-string v1, "bad coroutine state"
    invoke-direct {v0, v1}, Ljava/lang/IllegalStateException;-><init>(Ljava/lang/String;)V
    throw v0

    :resume_write
    iget v1, v3, Lloops/TestKtorOkHttpSourceFlushCompletion;->I$1:I
    iget v2, v3, Lloops/TestKtorOkHttpSourceFlushCompletion;->I$0:I
    iget-object v4, v3, Lloops/TestKtorOkHttpSourceFlushCompletion;->L$5:Ljava/lang/Object;
    check-cast v4, Lkotlin/jvm/internal/Ref$IntRef;
    iget-object v5, v3, Lloops/TestKtorOkHttpSourceFlushCompletion;->L$4:Ljava/lang/Object;
    check-cast v5, Lokio/BufferedSource;
    iget-object v9, v3, Lloops/TestKtorOkHttpSourceFlushCompletion;->L$3:Ljava/lang/Object;
    check-cast v9, Lio/ktor/client/request/HttpRequestData;
    iget-object v10, v3, Lloops/TestKtorOkHttpSourceFlushCompletion;->L$2:Ljava/lang/Object;
    check-cast v10, Lkotlin/coroutines/CoroutineContext;
    iget-object v11, v3, Lloops/TestKtorOkHttpSourceFlushCompletion;->L$1:Ljava/lang/Object;
    check-cast v11, Ljava/io/Closeable;
    :try_start_resume_write
    invoke-static/range {p1 .. p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    :try_end_resume_write
    .catchall {:try_start_resume_write .. :try_end_resume_write} :body_error
    goto :flush_call

    :initial
    invoke-static/range {p1 .. p1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    iget-object v11, v3, Lloops/TestKtorOkHttpSourceFlushCompletion;->$this_toChannel:Lokio/BufferedSource;
    iget-object v1, v3, Lloops/TestKtorOkHttpSourceFlushCompletion;->$context:Lkotlin/coroutines/CoroutineContext;
    iget-object v2, v3, Lloops/TestKtorOkHttpSourceFlushCompletion;->$requestData:Lio/ktor/client/request/HttpRequestData;
    :try_start_init
    new-instance v4, Lkotlin/jvm/internal/Ref$IntRef;
    invoke-direct {v4}, Lkotlin/jvm/internal/Ref$IntRef;-><init>()V
    :try_end_init
    .catchall {:try_start_init .. :try_end_init} :body_error
    const/4 v5, 0x0
    move-object v9, v0
    move-object v10, v1
    move-object v12, v4
    move v13, v5
    move v14, v13
    move-object v1, v11
    move-object v15, v1
    move-object v11, v2

    :loop_test
    :try_start_loop
    invoke-interface {v15}, Ljava/nio/channels/Channel;->isOpen()Z
    move-result v0
    if-eqz v0, :done
    invoke-static {v10}, Lkotlinx/coroutines/JobKt;->isActive(Lkotlin/coroutines/CoroutineContext;)Z
    move-result v0
    if-eqz v0, :done
    iget v0, v12, Lkotlin/jvm/internal/Ref$IntRef;->element:I
    if-ltz v0, :done
    invoke-virtual {v9}, Lio/ktor/utils/io/WriterScope;->getChannel()Lio/ktor/utils/io/ByteWriteChannel;
    move-result-object v0
    const/4 v2, 0x0
    iput-object v9, v3, Lloops/TestKtorOkHttpSourceFlushCompletion;->L$0:Ljava/lang/Object;
    iput-object v1, v3, Lloops/TestKtorOkHttpSourceFlushCompletion;->L$1:Ljava/lang/Object;
    iput-object v10, v3, Lloops/TestKtorOkHttpSourceFlushCompletion;->L$2:Ljava/lang/Object;
    iput-object v11, v3, Lloops/TestKtorOkHttpSourceFlushCompletion;->L$3:Ljava/lang/Object;
    iput-object v15, v3, Lloops/TestKtorOkHttpSourceFlushCompletion;->L$4:Ljava/lang/Object;
    iput-object v12, v3, Lloops/TestKtorOkHttpSourceFlushCompletion;->L$5:Ljava/lang/Object;
    iput v13, v3, Lloops/TestKtorOkHttpSourceFlushCompletion;->I$0:I
    iput v14, v3, Lloops/TestKtorOkHttpSourceFlushCompletion;->I$1:I
    iput v8, v3, Lloops/TestKtorOkHttpSourceFlushCompletion;->label:I
    :try_end_loop
    .catchall {:try_start_loop .. :try_end_loop} :loop_error

    move-object v4, v1
    const/4 v1, 0x0
    move-object v5, v4
    const/4 v4, 0x1
    move-object/from16 v16, v5
    const/4 v5, 0x0
    :try_start_write
    invoke-static/range {v0 .. v5}, Lio/ktor/utils/io/ByteWriteChannelOperations_jvmKt;->write$default(Lio/ktor/utils/io/ByteWriteChannel;ILkotlin/jvm/functions/Function1;Lkotlin/coroutines/Continuation;ILjava/lang/Object;)Ljava/lang/Object;
    move-result-object v0
    :try_end_write
    .catchall {:try_start_write .. :try_end_write} :write_error
    if-ne v0, v6, :write_complete
    goto :suspended

    :write_complete
    move-object v0, v9
    move-object v9, v11
    move-object v4, v12
    move v2, v13
    move v1, v14
    move-object v5, v15
    move-object/from16 v11, v16

    :flush_call
    :try_start_flush
    invoke-virtual {v0}, Lio/ktor/utils/io/WriterScope;->getChannel()Lio/ktor/utils/io/ByteWriteChannel;
    move-result-object v12
    iput-object v0, v3, Lloops/TestKtorOkHttpSourceFlushCompletion;->L$0:Ljava/lang/Object;
    iput-object v11, v3, Lloops/TestKtorOkHttpSourceFlushCompletion;->L$1:Ljava/lang/Object;
    iput-object v10, v3, Lloops/TestKtorOkHttpSourceFlushCompletion;->L$2:Ljava/lang/Object;
    iput-object v9, v3, Lloops/TestKtorOkHttpSourceFlushCompletion;->L$3:Ljava/lang/Object;
    iput-object v5, v3, Lloops/TestKtorOkHttpSourceFlushCompletion;->L$4:Ljava/lang/Object;
    iput-object v4, v3, Lloops/TestKtorOkHttpSourceFlushCompletion;->L$5:Ljava/lang/Object;
    iput v2, v3, Lloops/TestKtorOkHttpSourceFlushCompletion;->I$0:I
    iput v1, v3, Lloops/TestKtorOkHttpSourceFlushCompletion;->I$1:I
    iput v7, v3, Lloops/TestKtorOkHttpSourceFlushCompletion;->label:I
    invoke-interface {v12, v3}, Lio/ktor/utils/io/ByteWriteChannel;->flush(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    move-result-object v12
    :try_end_flush
    .catchall {:try_start_flush .. :try_end_flush} :body_error
    if-ne v12, v6, :resume_flush_join

    :suspended
    return-object v6

    :write_error
    move-exception v0
    move-object v1, v0
    move-object/from16 v11, v16
    goto :cleanup_error

    :loop_error
    move-exception v0
    move-object/from16 v16, v1
    move-object v1, v0
    move-object/from16 v11, v16
    goto :cleanup_error

    :done
    move-object/from16 v16, v1
    :try_start_done
    sget-object v0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    :try_end_done
    .catchall {:try_start_done .. :try_end_done} :write_error
    if-eqz v16, :normal_exit
    :try_start_close
    invoke-interface/range {v16 .. v16}, Ljava/io/Closeable;->close()V
    :try_end_close
    .catchall {:try_start_close .. :try_end_close} :close_error

    :normal_exit
    const/4 v0, 0x0
    goto :finish

    :close_error
    move-exception v0
    goto :finish

    :body_error
    move-exception v0
    move-object v1, v0

    :cleanup_error
    if-eqz v11, :cleanup_exit
    :try_start_cleanup
    invoke-interface {v11}, Ljava/io/Closeable;->close()V
    :try_end_cleanup
    .catchall {:try_start_cleanup .. :try_end_cleanup} :suppressed
    goto :cleanup_exit

    :suppressed
    move-exception v0
    invoke-static {v1, v0}, Lkotlin/g;->addSuppressed(Ljava/lang/Throwable;Ljava/lang/Throwable;)V

    :cleanup_exit
    move-object v0, v1

    :finish
    if-nez v0, :throw_error
    sget-object v0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    return-object v0

    :throw_error
    throw v0
.end method
