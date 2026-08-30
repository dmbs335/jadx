.class public Lconditions/TestSameSourceMovePhi;
.super Ljava/lang/Object;

.field public value:Ljava/lang/String;

.method public constructor <init>(Ljava/lang/String;)V
    .registers 5

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    const-string v0, "*."
    invoke-virtual {p1, v0}, Ljava/lang/String;->startsWith(Ljava/lang/String;)Z
    move-result v0
    if-eqz v0, :plain

    move-object v1, p1
    const-string v0, "."
    const/4 v2, 0x1
    invoke-virtual {v1, v0, v2}, Ljava/lang/String;->indexOf(Ljava/lang/String;I)I
    move-result v0
    if-ltz v0, :accept
    goto :checks

    :plain
    move-object v1, p1

    :checks
    const-string v0, "**."
    invoke-virtual {v1, v0}, Ljava/lang/String;->startsWith(Ljava/lang/String;)Z
    move-result v0
    if-eqz v0, :accept

    const-string v0, "."
    const/4 v2, 0x2
    invoke-virtual {v1, v0, v2}, Ljava/lang/String;->indexOf(Ljava/lang/String;I)I
    move-result v0
    if-ltz v0, :accept

    new-instance v0, Ljava/lang/IllegalArgumentException;
    invoke-direct {v0}, Ljava/lang/IllegalArgumentException;-><init>()V
    throw v0

    :accept
    iput-object v1, p0, Lconditions/TestSameSourceMovePhi;->value:Ljava/lang/String;
    return-void
.end method
