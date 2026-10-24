.class public Lothers/VirtualReadBase;
.super Ljava/lang/Object;

.field public final INITIAL:I
.field public observed:I

.method public constructor <init>()V
    .registers 2

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    invoke-virtual {p0}, Lothers/VirtualReadBase;->readInitial()I
    move-result v0
    iput v0, p0, Lothers/VirtualReadBase;->observed:I
    const/16 v0, 0x9
    iput v0, p0, Lothers/VirtualReadBase;->INITIAL:I
    return-void
.end method

.method public readInitial()I
    .registers 2

    const/4 v0, 0x0
    return v0
.end method
