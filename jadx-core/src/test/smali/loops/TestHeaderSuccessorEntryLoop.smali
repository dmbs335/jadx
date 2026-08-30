.class public Lloops/TestHeaderSuccessorEntryLoop;
.super Ljava/lang/Object;

.method public static run(II)I
    .locals 1

    if-eqz p0, :initial
    move v0, p1
    goto :header

    :initial
    const/4 v0, 0x2
    if-ltz v0, :done
    goto :body

    :header
    if-ltz v0, :done

    :body
    add-int/lit8 v0, v0, -0x1
    goto :header

    :done
    return v0
.end method
