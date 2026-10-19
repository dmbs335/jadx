.class public Lloops/TestDuplicatedIndexedLoopInit;
.super Ljava/lang/Object;

.field public start:J
.field public scale:F
.field public values:[I

.method public static native visit(I)V
.end method

.method public run(JF)V
    .registers 18

    move-object/from16 v0, p0
    move/from16 v3, p3
    iget-wide v4, v0, Lloops/TestDuplicatedIndexedLoopInit;->start:J
    const-wide/high16 v6, -0x8000000000000000L
    cmp-long v4, v4, v6
    const/4 v5, 0x0
    const/4 v6, 0x1
    if-eqz v4, :reset

    iget v4, v0, Lloops/TestDuplicatedIndexedLoopInit;->scale:F
    cmpl-float v4, v4, v3
    if-nez v4, :different
    move v4, v6
    goto :checked

    :different
    move v4, v5

    :checked
    if-nez v4, :unchanged
    goto :reset

    :unchanged
    move-wide/from16 v7, p1
    goto :after

    :reset
    move-wide/from16 v7, p1
    iput-wide v7, v0, Lloops/TestDuplicatedIndexedLoopInit;->start:J
    iget-object v4, v0, Lloops/TestDuplicatedIndexedLoopInit;->values:[I
    const/4 v9, 0x0
    const/4 v10, 0x0
    array-length v12, v4

    :loop
    if-ge v10, v12, :loop_end
    aget v13, v4, v10
    invoke-static {v13}, Lloops/TestDuplicatedIndexedLoopInit;->visit(I)V
    add-int/lit8 v10, v10, 0x1
    goto :loop

    :loop_end
    iput v3, v0, Lloops/TestDuplicatedIndexedLoopInit;->scale:F

    :after
    iget v4, v0, Lloops/TestDuplicatedIndexedLoopInit;->scale:F
    const/4 v9, 0x0
    cmpl-float v4, v4, v9
    if-nez v4, :done
    iget-object v4, v0, Lloops/TestDuplicatedIndexedLoopInit;->values:[I
    const/4 v10, 0x0
    array-length v12, v4

    :second_loop
    if-ge v10, v12, :done
    aget v13, v4, v10
    invoke-static {v13}, Lloops/TestDuplicatedIndexedLoopInit;->visit(I)V
    add-int/lit8 v10, v10, 0x1
    goto :second_loop

    :done
    return-void
.end method
