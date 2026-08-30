.class public Lconstructors/TestConstructorSafeLocalPrefixMove;
.super Lconstructors/TestConstructorSafeLocalPrefixMoveParent;

.field private first:I
.field private second:I

.method public constructor <init>(II)V
    .locals 1

    add-int v0, p1, p2
    invoke-direct {p0, p1}, Lconstructors/TestConstructorSafeLocalPrefixMoveParent;-><init>(I)V
    iput v0, p0, Lconstructors/TestConstructorSafeLocalPrefixMove;->first:I
    iput v0, p0, Lconstructors/TestConstructorSafeLocalPrefixMove;->second:I
    return-void
.end method
