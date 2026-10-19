.class public Lconditions/TestSharedBooleanCarrierThreeGuards;
.super Ljava/lang/Object;

.method public static select(Landroidx/compose/ui/text/SpanStyle;Landroidx/compose/ui/graphics/Shadow;Landroidx/compose/ui/text/PlatformSpanStyle;Landroidx/compose/ui/graphics/drawscope/DrawStyle;)[Ljava/lang/Object;
    .locals 8

    const/4 v1, 0x1
    const/4 v2, 0x0
    move-object v0, p3

    if-eqz p1, :shadow_null
    invoke-virtual {p0}, Landroidx/compose/ui/text/SpanStyle;->getShadow()Landroidx/compose/ui/graphics/Shadow;
    move-result-object v5
    move-object v4, p1
    invoke-static {v4, v5}, Lkotlin/jvm/internal/Intrinsics;->areEqual(Ljava/lang/Object;Ljava/lang/Object;)Z
    move-result v5
    if-eqz v5, :shadow_changed
    goto :platform_check

    :shadow_changed
    move-object v6, p2
    goto :changed

    :shadow_null
    move-object v4, p1

    :platform_check
    if-eqz p2, :platform_null
    invoke-virtual {p0}, Landroidx/compose/ui/text/SpanStyle;->getPlatformStyle()Landroidx/compose/ui/text/PlatformSpanStyle;
    move-result-object v5
    move-object v6, p2
    invoke-static {v6, v5}, Lkotlin/jvm/internal/Intrinsics;->areEqual(Ljava/lang/Object;Ljava/lang/Object;)Z
    move-result v5
    if-eqz v5, :changed
    goto :draw_check

    :platform_null
    move-object v6, p2

    :draw_check
    if-eqz v0, :unchanged
    invoke-virtual {p0}, Landroidx/compose/ui/text/SpanStyle;->getDrawStyle()Landroidx/compose/ui/graphics/drawscope/DrawStyle;
    move-result-object v5
    invoke-static {v0, v5}, Lkotlin/jvm/internal/Intrinsics;->areEqual(Ljava/lang/Object;Ljava/lang/Object;)Z
    move-result v5
    if-nez v5, :unchanged
    goto :changed

    :unchanged
    move v5, v2
    goto :join

    :changed
    move v5, v1

    :join
    if-nez v5, :alloc
    const/4 v2, 0x0
    return-object v2

    :alloc
    const/4 v2, 0x2
    new-array v2, v2, [Ljava/lang/Object;
    const/4 v3, 0x0
    aput-object v4, v2, v3
    const/4 v3, 0x1
    aput-object v6, v2, v3
    return-object v2
.end method
