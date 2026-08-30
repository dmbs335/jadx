.class public Lconstructors/TestConstructorSharedTernaryAssignInline;
.super Ljava/lang/Object;

.method public constructor <init>(ZZI)V
    .locals 1

    and-int/lit8 v0, p3, 0x1
    if-eqz v0, :first_done
    const/4 p1, 0x0

    :first_done
    and-int/lit8 v0, p3, 0x2
    if-eqz v0, :second_done
    move p2, p1

    :second_done
    invoke-direct {p0, p1, p2}, Lconstructors/TestConstructorSharedTernaryAssignInline;-><init>(ZZ)V
    return-void
.end method

.method public constructor <init>(ZZ)V
    .locals 0

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    return-void
.end method
