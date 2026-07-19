.class public Lconstructors/TestConstructorBranchLiteralTernaryInline;
.super Ljava/lang/Object;

.method private static box(J)J
    .locals 0

    return-wide p0
.end method

.method public constructor <init>(JI)V
    .locals 6

    and-int/lit8 v0, p3, 0x1
    if-eqz v0, :use_arg
    const/4 v0, 0x0
    int-to-long v0, v0
    const/16 v4, 0x20
    shl-long v2, v0, v4
    const-wide v4, 0xffffffffL
    and-long/2addr v0, v4
    or-long/2addr v0, v2
    invoke-static {v0, v1}, Lconstructors/TestConstructorBranchLiteralTernaryInline;->box(J)J
    move-result-wide v0
    goto :join

    :use_arg
    move-wide v0, p1

    :join
    invoke-direct {p0, v0, v1}, Lconstructors/TestConstructorBranchLiteralTernaryInline;-><init>(J)V
    return-void
.end method

.method public constructor <init>(J)V
    .locals 0

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    return-void
.end method
