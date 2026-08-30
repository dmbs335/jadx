.class public final Lio/ktor/utils/io/jvm/javaio/WritingKt;
.super Ljava/lang/Object;

.method public static final copyTo(Lio/ktor/utils/io/ByteReadChannel;Ljava/io/OutputStream;JLkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .registers 21

    move-object/from16 v0, p4
    check-cast v0, Lio/ktor/utils/io/jvm/javaio/WritingKt$copyTo$1;
    iget-object v1, v0, Lio/ktor/utils/io/jvm/javaio/WritingKt$copyTo$1;->result:Ljava/lang/Object;
    invoke-static {}, Lkotlin/coroutines/intrinsics/IntrinsicsKt;->getCOROUTINE_SUSPENDED()Ljava/lang/Object;
    move-result-object v2
    const/4 v3, 0x1
    iget v4, v0, Lio/ktor/utils/io/jvm/javaio/WritingKt$copyTo$1;->label:I
    if-eqz v4, :initial
    if-ne v4, v3, :invalid_state

    iget-wide v13, v0, Lio/ktor/utils/io/jvm/javaio/WritingKt$copyTo$1;->J$1:J
    iget-object v12, v0, Lio/ktor/utils/io/jvm/javaio/WritingKt$copyTo$1;->L$1:Ljava/lang/Object;
    check-cast v12, Ljava/io/OutputStream;
    iget-object v10, v0, Lio/ktor/utils/io/jvm/javaio/WritingKt$copyTo$1;->L$0:Ljava/lang/Object;
    check-cast v10, Lio/ktor/utils/io/ByteReadChannel;
    move-object v11, v0
    invoke-static {v1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    goto :resume_join

    :invalid_state
    new-instance v0, Ljava/lang/IllegalStateException;
    const-string v1, "call to resume before invoke"
    invoke-direct {v0, v1}, Ljava/lang/IllegalStateException;-><init>(Ljava/lang/String;)V
    throw v0

    :initial
    invoke-static {v1}, Lkotlin/ResultKt;->throwOnFailure(Ljava/lang/Object;)V
    const-wide/16 v6, 0
    move-object/from16 v8, p1
    move-object/from16 v9, p0
    goto :loop_header

    :resume_join
    move-object v9, v10
    move-object v0, v11
    move-object v8, v12
    move-wide v6, v13

    :drain
    invoke-interface {v9}, Lio/ktor/utils/io/ByteReadChannel;->getReadBuffer()Lkotlinx/io/Source;
    move-result-object v10
    invoke-interface {v10}, Lkotlinx/io/Source;->getBuffer()Lkotlinx/io/Buffer;
    move-result-object v11
    invoke-virtual {v11}, Lkotlinx/io/Buffer;->getSize()J
    move-result-wide v12
    add-long/2addr v6, v12
    invoke-interface {v9}, Lio/ktor/utils/io/ByteReadChannel;->getReadBuffer()Lkotlinx/io/Source;
    move-result-object v10
    invoke-interface {v10}, Lkotlinx/io/Source;->getBuffer()Lkotlinx/io/Buffer;
    move-result-object v10
    move-object v11, v8
    const-wide/16 v12, 0
    const/4 v14, 0x2
    const/4 v15, 0x0
    invoke-static/range {v10 .. v15}, Lkotlinx/io/BuffersJvmKt;->readTo$default(Lkotlinx/io/Buffer;Ljava/io/OutputStream;JILjava/lang/Object;)V

    :loop_header
    invoke-interface {v9}, Lio/ktor/utils/io/ByteReadChannel;->isClosedForRead()Z
    move-result v10
    if-nez v10, :done
    invoke-interface {v9}, Lio/ktor/utils/io/ByteReadChannel;->getReadBuffer()Lkotlinx/io/Source;
    move-result-object v10
    invoke-interface {v10}, Lkotlinx/io/Source;->exhausted()Z
    move-result v10
    if-eqz v10, :drain

    iput-object v9, v0, Lio/ktor/utils/io/jvm/javaio/WritingKt$copyTo$1;->L$0:Ljava/lang/Object;
    iput-object v8, v0, Lio/ktor/utils/io/jvm/javaio/WritingKt$copyTo$1;->L$1:Ljava/lang/Object;
    iput-wide v6, v0, Lio/ktor/utils/io/jvm/javaio/WritingKt$copyTo$1;->J$1:J
    iput v3, v0, Lio/ktor/utils/io/jvm/javaio/WritingKt$copyTo$1;->label:I
    const/4 v10, 0x0
    const/4 v11, 0x0
    invoke-static {v9, v10, v0, v3, v11}, Lio/ktor/utils/io/ByteReadChannel;->awaitContent$default(Lio/ktor/utils/io/ByteReadChannel;ILkotlin/coroutines/Continuation;ILjava/lang/Object;)Ljava/lang/Object;
    move-result-object v10
    if-eq v10, v2, :suspended

    move-object v10, v9
    move-object v11, v0
    move-object v12, v8
    move-wide v13, v6
    goto :resume_join

    :suspended
    return-object v2

    :done
    invoke-static {v6, v7}, Ljava/lang/Long;->valueOf(J)Ljava/lang/Long;
    move-result-object v12
    return-object v12
.end method
