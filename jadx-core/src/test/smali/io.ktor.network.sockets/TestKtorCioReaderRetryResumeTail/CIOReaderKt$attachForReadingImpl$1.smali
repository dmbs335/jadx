.class public Lio/ktor/network/sockets/CIOReaderKt$attachForReadingImpl$1;
.super Ljava/lang/Object;

.field private label:I
.field private readResult:I
.field private rounds:I

.method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
    .locals 4

    iget v0, p0, Lio/ktor/network/sockets/CIOReaderKt$attachForReadingImpl$1;->label:I
    if-nez v0, :resumed

    :initial
    const/4 v1, 0x0
    goto :read_header

    :resumed
    const/4 v1, 0x1
    if-ne v0, v1, :bad_state

    :resume_tail
    move-object v2, p1
    goto :result_load

    :read_header
    move v3, v1
    goto :read_body

    :read_body
    iget v0, p0, Lio/ktor/network/sockets/CIOReaderKt$attachForReadingImpl$1;->rounds:I
    add-int/2addr v0, v3
    iput v0, p0, Lio/ktor/network/sockets/CIOReaderKt$attachForReadingImpl$1;->readResult:I
    goto :result_load

    :result_load
    iget v0, p0, Lio/ktor/network/sockets/CIOReaderKt$attachForReadingImpl$1;->readResult:I
    if-nez v0, :done
    const/4 v1, 0x1
    goto :read_header

    :done
    return-object p1

    :bad_state
    new-instance v2, Ljava/lang/IllegalStateException;
    invoke-direct {v2}, Ljava/lang/IllegalStateException;-><init>()V
    throw v2
.end method
