.class public Lconstructors/TestConstructorLoopCarriedInstanceAlias;
.super Ljava/lang/Object;

.method public static make(I)Ljava/lang/StringBuilder;
    .locals 5

    new-instance v0, Ljava/lang/StringBuilder;
    const/4 v1, 0x0

    :loop
    if-ge v1, p0, :construct
    and-int/lit8 v2, v1, 0x1
    if-eqz v2, :even

    move-object v3, v0
    move-object v0, v3
    goto :next

    :even
    move-object v4, v0
    move-object v0, v4

    :next
    add-int/lit8 v1, v1, 0x1
    goto :loop

    :construct
    invoke-direct {v0}, Ljava/lang/StringBuilder;-><init>()V
    return-object v0
.end method
