.class public Lloops/TestCoroutineTwoSuspendNumberLoop;
.super Ljava/lang/Object;

.field private buffer:Ljava/nio/ByteBuffer;
.field private closed:Z
.field private label:I

.method private static parse(Ljava/nio/ByteBuffer;)Ljava/lang/Object;
    .locals 0
    return-object p0
.end method

.method private static read(Ljava/nio/ByteBuffer;)Ljava/lang/Object;
    .locals 1
    const/4 v0, 0x1
    invoke-static {v0}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
    move-result-object v0
    return-object v0
.end method

.method private static throwOnFailure(Ljava/lang/Object;)V
    .locals 0
    return-void
.end method

.method public run(Ljava/lang/Object;Ljava/nio/ByteBuffer;Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
    .locals 4

    iget v0, p0, Lloops/TestCoroutineTwoSuspendNumberLoop;->label:I
    if-eqz v0, :initial
    const/4 v1, 0x1
    if-eq v0, v1, :resume_read
    const/4 v1, 0x2
    if-eq v0, v1, :resume_parse
    new-instance v0, Ljava/lang/IllegalStateException;
    invoke-direct {v0}, Ljava/lang/IllegalStateException;-><init>()V
    throw v0

    :resume_parse
    iget-object p2, p0, Lloops/TestCoroutineTwoSuspendNumberLoop;->buffer:Ljava/nio/ByteBuffer;
    invoke-static {p1}, Lloops/TestCoroutineTwoSuspendNumberLoop;->throwOnFailure(Ljava/lang/Object;)V
    goto :after_parse

    :resume_read
    iget-object p2, p0, Lloops/TestCoroutineTwoSuspendNumberLoop;->buffer:Ljava/nio/ByteBuffer;
    invoke-static {p1}, Lloops/TestCoroutineTwoSuspendNumberLoop;->throwOnFailure(Ljava/lang/Object;)V
    move-object v2, p1
    goto :read_result

    :initial
    invoke-static {p1}, Lloops/TestCoroutineTwoSuspendNumberLoop;->throwOnFailure(Ljava/lang/Object;)V
    invoke-virtual {p2}, Ljava/nio/ByteBuffer;->clear()Ljava/nio/Buffer;

    :loop_header
    iget-boolean v0, p0, Lloops/TestCoroutineTwoSuspendNumberLoop;->closed:Z
    if-nez v0, :done
    iput-object p2, p0, Lloops/TestCoroutineTwoSuspendNumberLoop;->buffer:Ljava/nio/ByteBuffer;
    const/4 v0, 0x1
    iput v0, p0, Lloops/TestCoroutineTwoSuspendNumberLoop;->label:I
    invoke-static {p2}, Lloops/TestCoroutineTwoSuspendNumberLoop;->read(Ljava/nio/ByteBuffer;)Ljava/lang/Object;
    move-result-object v1
    if-eq v1, p3, :suspended
    move-object v2, v1

    :read_result
    check-cast v2, Ljava/lang/Number;
    invoke-virtual {v2}, Ljava/lang/Number;->intValue()I
    move-result v2
    const/4 v3, -0x1
    if-eq v2, v3, :close
    invoke-virtual {p2}, Ljava/nio/ByteBuffer;->flip()Ljava/nio/Buffer;
    iput-object p2, p0, Lloops/TestCoroutineTwoSuspendNumberLoop;->buffer:Ljava/nio/ByteBuffer;
    const/4 v0, 0x2
    iput v0, p0, Lloops/TestCoroutineTwoSuspendNumberLoop;->label:I
    invoke-static {p2}, Lloops/TestCoroutineTwoSuspendNumberLoop;->parse(Ljava/nio/ByteBuffer;)Ljava/lang/Object;
    move-result-object v1
    if-eq v1, p3, :suspended

    :after_parse
    invoke-virtual {p2}, Ljava/nio/ByteBuffer;->compact()Ljava/nio/ByteBuffer;
    goto :loop_header

    :close
    const/4 v0, 0x1
    iput-boolean v0, p0, Lloops/TestCoroutineTwoSuspendNumberLoop;->closed:Z

    :done
    return-object p1

    :suspended
    return-object p3
.end method
