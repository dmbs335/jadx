.class public Lothers/TestInstanceCrossClassRead;
.super Ljava/lang/Object;

.field public final INITIAL:I
.field public observed:I

.method public constructor <init>()V
    .registers 2

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    invoke-static {p0}, Lothers/InstanceReadHelper;->read(Lothers/TestInstanceCrossClassRead;)I
    move-result v0
    iput v0, p0, Lothers/TestInstanceCrossClassRead;->observed:I
    const/16 v0, 0x9
    iput v0, p0, Lothers/TestInstanceCrossClassRead;->INITIAL:I
    return-void
.end method

.method public check()V
    .registers 3

    iget v0, p0, Lothers/TestInstanceCrossClassRead;->observed:I
    if-nez v0, :fail
    iget v0, p0, Lothers/TestInstanceCrossClassRead;->INITIAL:I
    const/16 v1, 0x9
    if-ne v0, v1, :fail
    return-void

    :fail
    new-instance v0, Ljava/lang/AssertionError;
    invoke-direct {v0}, Ljava/lang/AssertionError;-><init>()V
    throw v0
.end method
