.class public Lconstructors/TestConstructorMultipleSharedTernaryAssignInline;
.super Ljava/lang/Object;

.method public constructor <init>(ZZI)V
    .locals 1

    and-int/lit8 v0, p3, 0x1
    if-eqz v0, :first_done
    const/4 p1, 0x0

    :first_done
    and-int/lit8 v0, p3, 0x2
    if-eqz v0, :second_done
    const/4 p2, 0x1

    :second_done
    invoke-direct {p0, p1, p1, p2, p2}, Lconstructors/TestConstructorMultipleSharedTernaryAssignInline;-><init>(ZZZZ)V
    return-void
.end method

.method public constructor <init>(ZZZZ)V
    .locals 0

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    return-void
.end method
