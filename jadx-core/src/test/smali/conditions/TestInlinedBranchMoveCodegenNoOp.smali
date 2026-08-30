.class public Lconditions/TestInlinedBranchMoveCodegenNoOp;
.super Ljava/lang/Object;

.field private newPath:Ljava/lang/String;
.field private orgPath:Ljava/lang/String;

.method public test()V
    .registers 5

    new-instance v0, Ljava/io/File;
    iget-object v1, p0, Lconditions/TestInlinedBranchMoveCodegenNoOp;->orgPath:Ljava/lang/String;
    invoke-direct {v0, v1}, Ljava/io/File;-><init>(Ljava/lang/String;)V
    invoke-virtual {v0}, Ljava/io/File;->exists()Z
    move-result v1
    if-eqz v1, :done

    iget-object v1, p0, Lconditions/TestInlinedBranchMoveCodegenNoOp;->newPath:Ljava/lang/String;
    invoke-static {v1}, Lconditions/TestInlinedBranchMoveCodegenNoOp;->isEmpty(Ljava/lang/CharSequence;)Z
    move-result v1
    move v2, v1
    const-string v1, ".mp4"
    if-nez v2, :done

    invoke-static {v1}, Lconditions/TestInlinedBranchMoveCodegenNoOp;->consume(Ljava/lang/String;)V

    :done
    return-void
.end method

.method private static isEmpty(Ljava/lang/CharSequence;)Z
    .registers 1
    if-nez p0, :not_empty
    const/4 p0, 0x1
    return p0
    :not_empty
    const/4 p0, 0x0
    return p0
.end method

.method private static consume(Ljava/lang/String;)V
    .registers 1
    return-void
.end method
