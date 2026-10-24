.class public Lothers/TestMultipleInstanceFinalForwardReads;
.super Ljava/lang/Object;

.field public final first:I
.field public final second:I
.field public observedFirst:I
.field public observedSecond:I

.method public constructor <init>()V
    .registers 2

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    iget v0, p0, Lothers/TestMultipleInstanceFinalForwardReads;->first:I
    iput v0, p0, Lothers/TestMultipleInstanceFinalForwardReads;->observedFirst:I
    iget v0, p0, Lothers/TestMultipleInstanceFinalForwardReads;->second:I
    iput v0, p0, Lothers/TestMultipleInstanceFinalForwardReads;->observedSecond:I
    const/16 v0, 0x9
    iput v0, p0, Lothers/TestMultipleInstanceFinalForwardReads;->first:I
    const/16 v0, 0xa
    iput v0, p0, Lothers/TestMultipleInstanceFinalForwardReads;->second:I
    return-void
.end method

.method public check()V
    .registers 3

    iget v0, p0, Lothers/TestMultipleInstanceFinalForwardReads;->observedFirst:I
    if-nez v0, :fail
    iget v0, p0, Lothers/TestMultipleInstanceFinalForwardReads;->observedSecond:I
    if-nez v0, :fail
    iget v0, p0, Lothers/TestMultipleInstanceFinalForwardReads;->first:I
    const/16 v1, 0x9
    if-ne v0, v1, :fail
    iget v0, p0, Lothers/TestMultipleInstanceFinalForwardReads;->second:I
    const/16 v1, 0xa
    if-ne v0, v1, :fail
    return-void

    :fail
    new-instance v0, Ljava/lang/AssertionError;
    invoke-direct {v0}, Ljava/lang/AssertionError;-><init>()V
    throw v0
.end method
