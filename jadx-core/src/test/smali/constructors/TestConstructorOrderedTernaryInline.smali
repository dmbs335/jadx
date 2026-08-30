.class public Lconstructors/TestConstructorOrderedTernaryInline;
.super Ljava/lang/Object;

.field private static order:I

.method private static first()I
    .locals 1

    sget v0, Lconstructors/TestConstructorOrderedTernaryInline;->order:I
    mul-int/lit8 v0, v0, 0xa
    add-int/lit8 v0, v0, 0x1
    sput v0, Lconstructors/TestConstructorOrderedTernaryInline;->order:I
    const/16 v0, 0xa
    return v0
.end method

.method private static second()I
    .locals 1

    sget v0, Lconstructors/TestConstructorOrderedTernaryInline;->order:I
    mul-int/lit8 v0, v0, 0xa
    add-int/lit8 v0, v0, 0x2
    sput v0, Lconstructors/TestConstructorOrderedTernaryInline;->order:I
    const/16 v0, 0x14
    return v0
.end method

.method public static check()Z
    .locals 4

    const/4 v0, 0x0
    sput v0, Lconstructors/TestConstructorOrderedTernaryInline;->order:I
    new-instance v0, Lconstructors/TestConstructorOrderedTernaryInline;
    const/4 v1, 0x0
    const/4 v2, 0x0
    const/4 v3, 0x3
    invoke-direct {v0, v1, v2, v3}, Lconstructors/TestConstructorOrderedTernaryInline;-><init>(III)V
    sget v0, Lconstructors/TestConstructorOrderedTernaryInline;->order:I
    const/16 v1, 0xc
    if-ne v0, v1, :fail
    const/4 v0, 0x1
    return v0

    :fail
    const/4 v0, 0x0
    return v0
.end method

.method public constructor <init>(III)V
    .locals 1

    and-int/lit8 v0, p3, 0x1
    if-eqz v0, :first_done
    invoke-static {}, Lconstructors/TestConstructorOrderedTernaryInline;->first()I
    move-result p1

    :first_done
    and-int/lit8 v0, p3, 0x2
    if-eqz v0, :second_done
    invoke-static {}, Lconstructors/TestConstructorOrderedTernaryInline;->second()I
    move-result p2

    :second_done
    invoke-direct {p0, p1, p2}, Lconstructors/TestConstructorOrderedTernaryInline;-><init>(II)V
    return-void
.end method

.method public constructor <init>(II)V
    .locals 0

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    return-void
.end method
