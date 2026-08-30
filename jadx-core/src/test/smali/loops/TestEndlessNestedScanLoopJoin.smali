.class public Lloops/TestEndlessNestedScanLoopJoin;
.super Ljava/lang/Object;

.field private buffer:[C
.field private limit:I
.field private pos:I

.method private fillBuffer(I)Z
    .registers 2
    const/4 v0, 0x0
    return v0
.end method

.method private checkLenient()V
    .registers 1
    return-void
.end method

.method private nextUnquotedValue()Ljava/lang/String;
    .registers 7

    const/4 v0, 0x0
    const/4 v1, 0x0

    :outer_loop
    move v2, v1

    :scan_loop
    iget v3, p0, Lloops/TestEndlessNestedScanLoopJoin;->pos:I
    add-int v4, v3, v2
    iget v5, p0, Lloops/TestEndlessNestedScanLoopJoin;->limit:I
    if-ge v4, v5, :at_buffer_end

    iget-object v4, p0, Lloops/TestEndlessNestedScanLoopJoin;->buffer:[C
    add-int/2addr v3, v2
    aget-char v3, v4, v3

    const/16 v4, 0x9
    if-eq v3, v4, :scan_done
    const/16 v4, 0xa
    if-eq v3, v4, :scan_done
    const/16 v4, 0x23
    if-eq v3, v4, :lenient_delimiter
    const/16 v4, 0x2c
    if-eq v3, v4, :scan_done
    const/16 v4, 0x2f
    if-eq v3, v4, :lenient_delimiter

    packed-switch v3, :switch_data

    add-int/lit8 v2, v2, 0x1
    goto :scan_loop

    :lenient_delimiter
    :switch_lenient
    invoke-direct {p0}, Lloops/TestEndlessNestedScanLoopJoin;->checkLenient()V
    goto :scan_done

    :at_buffer_end
    iget-object v3, p0, Lloops/TestEndlessNestedScanLoopJoin;->buffer:[C
    array-length v3, v3
    if-ge v2, v3, :append_buffer

    add-int/lit8 v3, v2, 0x1
    invoke-direct {p0, v3}, Lloops/TestEndlessNestedScanLoopJoin;->fillBuffer(I)Z
    move-result v3
    if-eqz v3, :scan_done
    goto :scan_loop

    :scan_done
    :switch_done
    move v1, v2
    goto :build_result

    :append_buffer
    if-nez v0, :append_chunk
    new-instance v0, Ljava/lang/StringBuilder;
    const/16 v3, 0x10
    invoke-static {v2, v3}, Ljava/lang/Math;->max(II)I
    move-result v3
    invoke-direct {v0, v3}, Ljava/lang/StringBuilder;-><init>(I)V

    :append_chunk
    iget-object v3, p0, Lloops/TestEndlessNestedScanLoopJoin;->buffer:[C
    iget v4, p0, Lloops/TestEndlessNestedScanLoopJoin;->pos:I
    invoke-virtual {v0, v3, v4, v2}, Ljava/lang/StringBuilder;->append([CII)Ljava/lang/StringBuilder;
    iget v3, p0, Lloops/TestEndlessNestedScanLoopJoin;->pos:I
    add-int/2addr v3, v2
    iput v3, p0, Lloops/TestEndlessNestedScanLoopJoin;->pos:I

    const/4 v2, 0x1
    invoke-direct {p0, v2}, Lloops/TestEndlessNestedScanLoopJoin;->fillBuffer(I)Z
    move-result v2
    if-nez v2, :outer_loop

    :build_result
    if-nez v0, :from_builder
    new-instance v0, Ljava/lang/String;
    iget-object v2, p0, Lloops/TestEndlessNestedScanLoopJoin;->buffer:[C
    iget v3, p0, Lloops/TestEndlessNestedScanLoopJoin;->pos:I
    invoke-direct {v0, v2, v3, v1}, Ljava/lang/String;-><init>([CII)V
    goto :return_result

    :from_builder
    iget-object v2, p0, Lloops/TestEndlessNestedScanLoopJoin;->buffer:[C
    iget v3, p0, Lloops/TestEndlessNestedScanLoopJoin;->pos:I
    invoke-virtual {v0, v2, v3, v1}, Ljava/lang/StringBuilder;->append([CII)Ljava/lang/StringBuilder;
    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;
    move-result-object v0

    :return_result
    iget v2, p0, Lloops/TestEndlessNestedScanLoopJoin;->pos:I
    add-int/2addr v2, v1
    iput v2, p0, Lloops/TestEndlessNestedScanLoopJoin;->pos:I
    return-object v0

    :switch_data
    .packed-switch 0x5b
        :switch_done
        :switch_lenient
        :switch_done
    .end packed-switch
.end method
