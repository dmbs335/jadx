.class public Lothers/TestVirtualInstanceFinalRead;
.super Lothers/VirtualReadBase;

.method public constructor <init>()V
    .registers 1

    invoke-direct {p0}, Lothers/VirtualReadBase;-><init>()V
    return-void
.end method

.method public readInitial()I
    .registers 2

    iget v0, p0, Lothers/VirtualReadBase;->INITIAL:I
    return v0
.end method

.method public check()V
    .registers 3

    iget v0, p0, Lothers/VirtualReadBase;->observed:I
    if-nez v0, :fail
    iget v0, p0, Lothers/VirtualReadBase;->INITIAL:I
    const/16 v1, 0x9
    if-ne v0, v1, :fail
    return-void

    :fail
    new-instance v0, Ljava/lang/AssertionError;
    invoke-direct {v0}, Ljava/lang/AssertionError;-><init>()V
    throw v0
.end method
