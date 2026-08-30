.class public Lconditions/TestNestedTernaryMergeRollback;
.super Ljava/lang/Object;

.method public static select(IZZZ)I
    .locals 1

    if-eqz p0, :left
    const/4 v0, 0x1
    if-eq p0, v0, :right_value
    const/4 v0, 0x2
    if-eq p0, v0, :start
    const/4 v0, 0x3
    if-eq p0, v0, :end
    const/16 v0, 0x1e
    goto :done

    :left
    const/16 v0, 0x28
    goto :done

    :right_value
    const/16 v0, 0x32
    goto :done

    :start
    if-eqz p1, :rtl

    if-eqz p2, :width
    goto :zero

    :rtl
    if-eqz p3, :width

    :zero
    const/4 v0, 0x0
    goto :done

    :width
    const/16 v0, 0xa
    goto :done

    :end
    const/16 v0, 0x3c

    :done
    return v0
.end method
