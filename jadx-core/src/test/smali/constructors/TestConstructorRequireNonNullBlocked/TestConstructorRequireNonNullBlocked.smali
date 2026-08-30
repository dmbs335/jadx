.class public Lconstructors/TestConstructorRequireNonNullBlocked;
.super Ljava/lang/Object;

.field private final first:I
.field private final second:I

.method private constructor <init>(II)V
    .locals 0

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
    iput p1, p0, Lconstructors/TestConstructorRequireNonNullBlocked;->first:I
    iput p2, p0, Lconstructors/TestConstructorRequireNonNullBlocked;->second:I
    return-void
.end method

.method public constructor <init>(Ljava/lang/String;)V
    .locals 3

    const-string v0, "value"
    invoke-static {p1, v0}, Ljava/util/Objects;->requireNonNull(Ljava/lang/Object;Ljava/lang/String;)Ljava/lang/Object;
    invoke-static {}, Lconstructors/TestConstructorRequireNonNullBlocked;->sideEffect()I
    move-result v1
    invoke-virtual {p1}, Ljava/lang/String;->length()I
    move-result v2
    invoke-direct {p0, v1, v2}, Lconstructors/TestConstructorRequireNonNullBlocked;-><init>(II)V
    return-void
.end method

.method private static sideEffect()I
    .locals 1

    const/4 v0, 0x1
    return v0
.end method
