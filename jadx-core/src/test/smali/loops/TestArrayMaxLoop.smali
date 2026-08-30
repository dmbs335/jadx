.class public Lloops/TestArrayMaxLoop;
.super Ljava/lang/Object;

.method public static maxOrThrow([B)B
    .locals 4

    array-length v0, p0
    if-eqz v0, :empty

    const/4 v0, 0x0
    aget-byte v0, p0, v0
    array-length v1, p0
    add-int/lit8 v1, v1, -0x1
    const/4 v2, 0x1
    if-gt v2, v1, :done

    :loop
    aget-byte v3, p0, v2
    if-ge v0, v3, :keep
    move v0, v3

    :keep
    if-eq v2, v1, :done
    add-int/lit8 v2, v2, 0x1
    goto :loop

    :done
    int-to-byte v0, v0
    return v0

    :empty
    new-instance v0, Ljava/util/NoSuchElementException;
    invoke-direct {v0}, Ljava/util/NoSuchElementException;-><init>()V
    throw v0
.end method
