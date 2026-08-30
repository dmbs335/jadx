.class public final Lconditions/TestShortCircuitFinalFieldPrelude;
.super Ljava/lang/Object;

.field private final top:F

.method public constructor <init>(F)V
    .registers 2

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    iput p1, p0, Lconditions/TestShortCircuitFinalFieldPrelude;->top:F
    return-void
.end method

.method public test(II)Z
    .registers 9

    invoke-static {p1}, Ljava/lang/Float;->intBitsToFloat(I)F
    move-result v0
    const/high16 v1, 0x41200000    # 10.0f
    cmpg-float v2, v0, v1
    if-gez v2, :second_corner

    invoke-static {p2}, Ljava/lang/Float;->intBitsToFloat(I)F
    move-result v0
    iget v1, p0, Lconditions/TestShortCircuitFinalFieldPrelude;->top:F
    cmpg-float v2, v0, v1
    if-gez v2, :second_corner

    const/high16 v3, 0x3f800000    # 1.0f
    const/high16 v4, 0x40000000    # 2.0f
    goto :join

    :second_corner
    invoke-static {p1}, Ljava/lang/Float;->intBitsToFloat(I)F
    move-result v0
    const/high16 v1, 0x41a00000    # 20.0f
    cmpl-float v2, v0, v1
    if-lez v2, :inside

    invoke-static {p2}, Ljava/lang/Float;->intBitsToFloat(I)F
    move-result v0
    iget v1, p0, Lconditions/TestShortCircuitFinalFieldPrelude;->top:F
    cmpg-float v2, v0, v1
    if-gez v2, :inside

    const/high16 v3, 0x40400000    # 3.0f
    const/high16 v4, 0x40800000    # 4.0f
    goto :join

    :inside
    const/4 v0, 0x1
    return v0

    :join
    div-float/2addr v3, v4
    const/high16 v4, 0x3f800000    # 1.0f
    cmpg-float v0, v3, v4
    if-gtz v0, :outside
    const/4 v0, 0x1
    return v0

    :outside
    const/4 v0, 0x0
    return v0
.end method
