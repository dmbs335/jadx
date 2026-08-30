.class public Lconstructors/TestConstructorMultiUsePrimitiveCastInline;
.super Lconstructors/TestConstructorMultiUsePrimitiveCastInlineParent;

.method public constructor <init>()V
    .locals 2

    const-wide/high16 v0, 0x400c000000000000L
    double-to-float v0, v0
    invoke-direct {p0, v0, v0}, Lconstructors/TestConstructorMultiUsePrimitiveCastInlineParent;-><init>(FF)V
    return-void
.end method
