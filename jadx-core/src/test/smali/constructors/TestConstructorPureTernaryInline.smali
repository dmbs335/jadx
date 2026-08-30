.class public Lconstructors/TestConstructorPureTernaryInline;
.super Ljava/lang/Object;

.method public constructor <init>(ZI)V
    .locals 1

    and-int/lit8 v0, p2, 0x1
    if-eqz v0, :keep
    const/4 p1, 0x0

    :keep
    invoke-direct {p0, p1}, Lconstructors/TestConstructorPureTernaryInline;-><init>(Z)V
    return-void
.end method

.method public constructor <init>(Z)V
    .locals 0

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    return-void
.end method
